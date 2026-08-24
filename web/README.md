>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

## Web Component

The Web component is the user interface used for the Iris Analysis Toolkit (IAT).

## Table of Contents

- [Web Component](#web-component)
  - [Table of Contents](#table-of-contents)
  - [Starting the Interface](#starting-the-interface)
    - [Docker and IAT](#docker-and-iat)
    - [From a Script](#from-a-script)
  - [Run Java Unit Tests](#run-java-unit-tests)
  - [Rebuild Docker Image](#rebuild-docker-image)

## Starting the Interface
This interface can be started in two ways:
1. (Recommended for Users) [Docker Image via the IAT](#docker-and-iat)
2. (Recommended for Developers) [From a Script](#from-a-script)

If you make changes to the scripts and want to create a new Docker image, see [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
To start the interface with Docker, follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.

### From a Script

The following steps start the application in developer mode. This mode is for developers who are troubleshooting or contributing to the code. It starts the supporting services in Docker containers, while the backend and frontend are built and run from the local source tree.

*Note: To use a GPU for the Iris Annotation component, comment out the CPU `iwp-annotation` Docker service and uncomment the GPU service in `iat/docker/docker-compose.local.yml`.*

#### Package Installation
##### Environment Set Up
The following steps explain how to set up your development environment. If Homebrew is already installed, skip to [Install Packages](#install-packages).
1. Install Xcode Command Line Tools
   1. Open Terminal and run the following command:
        ```bash
        xcode-select --install
        ```

   2. In the dialog window, confirm the installation and accept the license agreement.
2. Install Homebrew
   1. Open Terminal and run the following command:
        ```bash
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        ```

   2. Type your admin password and hit Enter[^1]
   3. Wait until you see an "Installation successful" message.
    [^1]: You won't see your keystrokes in the terminal

##### Install Packages
Open Terminal and run the following commands to install the packages:
    ```sh
    brew install angular-cli # Angular Cli
    brew install node@20.15.0 # Node
    brew install maven # Maven
    brew install yarn # yarn
    ```

#### Start Up
1. Start the services:
   1. Follow the steps from [Set up Environment Variables](../README.md#set-up-environment-variables)
   2. Follow the [support's README](../support/README.md#change-default-iris-image) to change the required default iris image to an image of your choice
   3. In a terminal, run the following commands to start the Docker containers:
        ```sh
        ./composeScript.sh prune # Clear out old/unwanted data and unused containers, images, etc.
        docker compose -f docker-compose.local.yml up # Starts only the supporting services in Docker containers, not the Web component.
        ```

   4. Open a second terminal window and run the following command to load the environment variables from the `.env` file:
      **Unix Commands** 
      ```sh
      source docker/.env
      ```
      **Windows Commands**
      ```sh
      Get-Content .\iat\docker\.env | ForEach-Object {
         if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            Set-Item -Path "Env:$($matches[1].Trim())" -Value $matches[2].Trim()
         }
      }
      ```
      
2. Start the backend locally:
    1. In the second terminal window, run the following command from the main `iat/` project directory:
		**After this step you can access the application at [http://localhost:8080](http://localhost:8080).**
        ```sh
        ./iwp-script.sh full # Runs the local backend services, including any changes just made
        ```
    2. In a web browser, navigate to `localhost:8080`

3. **(Optional)** Start up Frontend Locally:
    **You only need this step if you want access to a front end that automatically refreshes when changes are made.**

    **Note about the frontend development server:** Because of Spring Security and `webpack-dev-server` proxy behavior, the login page does not work through the development server. You may see unexpected behavior. Use one of these workarounds:
    
        - Disable the login page by setting `iwp.security.secured=false` in `web/src/main/resources/application.properties`.
        - Log in through the backend at [http://localhost:8080](http://localhost:8080). Requests through the frontend development server at [http://localhost:4200](http://localhost:4200) should then be authenticated.

    1. Open a third terminal window and run the following command from the `web/client/` subdirectory:
		**After this step you can access the front end development server at [http://localhost:4200](http://localhost:4200).**
        ```sh
        yarn run local # Runs the local frontend services, including any changes just made
        ```
    2. In a web browser, navigate to `localhost:4200`
        *Note: Do not forget to address the login-page issue.*

4. **(Optional)** Start the database-management container:
    **You only need this step if you want to access the database for debugging purposes.**

    1. Open a fourth terminal window and run the following commands from the `web/` subdirectory: 
        ```sh
        docker run --name iwp_php_admin --network iwp-simple_default -v phpmyadmin-volume:/etc/phpmyadmin/config.user.inc.php --link iwp_mysql:db -p 82:80 -d phpmyadmin/phpmyadmin #Runs the database container
        ```
    2. In a web browser, navigate to `localhost:82`


## Run Java Unit Tests
There are a few basic unit tests included in this project. Before running those tests, you must:
1. Set the variables in the Required Test Variables section of `src/test/resources/application-test.properties`.
2. Ensure that the MySQL and ActiveMQ containers are running.
    - You can start the containers by running `docker compose up` in the `../docker/` directory.
3. **Optional:** Replace `src/test/resources/Image.png` if needed. Keep the filename unchanged, or update both references in `src/test/java/org/mitre/iwp/web/data/MessagingTests.java`.

Once you have done that, you can run the following command:
    ```bash
    mvn -pl web -am -Dbuild.node.skip=true test
    ```

## Rebuild Docker Image
### Prerequisites
To build this docker image, there are a few prerequisites.
1. Install the required packages and tools by following the [Install Packages instructions](#install-packages).
2. Build the JET dependency.
    1. Clone the JET repository from https://github.com/ebts/jet.
    2. Build JET by running the following commands from the main `iat/` project directory:
    ```sh
    cd jet/Jet # Navigates into the subdirectory that contains the pom.xml file
    mvn install
    ```
3. Build the Web JAR by running the following commands in the main `iat/` project directory:
    ```sh
    # Build JAR
    mvn -pl web -am -DskipTests package

    # Confirm that the JAR was built successfully.
    cd web # Navigate to the web subdirectory.
    ls -lah target/*.jar
    ```
    *Note: This automatically builds the other local Maven modules (buffers, common, and support) required by the Web component.*

    The build JAR is written to `web/target/`.

### Building Docker Image
To build a new Docker image, run the following command from the `web/` subdirectory:
    ```sh
    # Add --platform linux/amd64 for Apple Silicon Systems
    docker build --platform linux/amd64 \
    -t new_web_image:YOUR_TAG_HERE \
    -f Dockerfile .
    ```
