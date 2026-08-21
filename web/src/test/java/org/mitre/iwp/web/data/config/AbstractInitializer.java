package org.mitre.iwp.web.data.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

public class AbstractInitializer {

    protected static final MySQLContainer<?> mysql;
    protected static final GenericContainer<?> activeMQContainer;
    private static final Properties APPLICATION_TEST_PROPERTIES = loadProperties("application-test.properties");
    private static final Properties APPLICATION_PROPERTIES = loadApplicationProperties();
    private static final StandardEnvironment ENVIRONMENT = createEnvironment();
    private static final String MYSQL_USERNAME =
            getProperty("spring.datasource.username");
    private static final String MYSQL_PASSWORD =
            getProperty("spring.datasource.password");
    private static final String MYSQL_NAME =
            getProperty("spring.datasource.name");
    private static final String ACTIVE_MQ_USERNAME =
            getProperty("spring.activemq.user", "iris.activeMqUserName");
    private static final String ACTIVE_MQ_PASSWORD =
            getProperty("spring.activemq.password", "iris.activeMqPassword");
    
    static {
        // Starting the MySQL container as a singleton
        mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8"))
            .withExposedPorts(3306)
            .withUsername(MYSQL_USERNAME)
            .withPassword(MYSQL_PASSWORD)
            .withDatabaseName(MYSQL_NAME);
        mysql.start();

        // Start ActiveMQ container
        activeMQContainer = new GenericContainer<>(
                DockerImageName.parse("dhi.io/activemq-artemis:2"))
                .withExposedPorts(61616)
                .withEnv("ARTEMIS_USER", ACTIVE_MQ_USERNAME)
                .withEnv("ARTEMIS_PASSWORD", ACTIVE_MQ_PASSWORD);
        activeMQContainer.start();
    }

    private static Properties loadApplicationProperties() {
        return loadProperties("application.properties");
    }

    private static Properties loadProperties(String propertiesPath) {
        try {
            return PropertiesLoaderUtils.loadAllProperties(propertiesPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + propertiesPath, e);
        }
    }

    private static StandardEnvironment createEnvironment() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(
                new PropertiesPropertySource("applicationTestProperties", APPLICATION_TEST_PROPERTIES));
        environment.getPropertySources().addLast(
                new PropertiesPropertySource("applicationProperties", APPLICATION_PROPERTIES));
        return environment;
    }

    private static String getProperty(String... propertyNames) {
        for (String propertyName : propertyNames) {
            String value = getOptionalProperty(propertyName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        throw new IllegalStateException(
                "Missing required test configuration. Set one of: " + String.join(", ", propertyNames));
    }

    private static String getOptionalProperty(String propertyName) {
        try {
            return ENVIRONMENT.getProperty(propertyName);
        } catch (IllegalArgumentException ignored) {
            // Try the next configured key when a placeholder cannot be resolved.
            return null;
        }
    }
        
    @PreDestroy
    public void cleanup() {
        mysql.close();
        activeMQContainer.close();
    }

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.name", () -> MYSQL_NAME);
    }

    @DynamicPropertySource
    static void registerActiveMQProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.activemq.broker-url", () -> "tcp://" + activeMQContainer.getHost() + ":"
                + activeMQContainer.getMappedPort(61616));
        registry.add("spring.activemq.user", () -> ACTIVE_MQ_USERNAME);
        registry.add("spring.activemq.password", () -> ACTIVE_MQ_PASSWORD);
    }
}
