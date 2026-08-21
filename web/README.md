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
This interface can be started two ways:
1. (Recommended for Users) [Docker Image via the IAT](#docker-and-iat)
2. (Recommended for Developers) [From a Script](#from-a-script)

If you make changes to the scripts and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
To start the interface through docker, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.

### From a Script

The following steps explain how to start the program in "developer" mode. This mode is for developers who are troubleshooting or contributing to the code, since it only starts up the services in docker containers. The backend and frontend parts are built and ran using the code from the local environment. 

NOTE: If you are using a GPU and would like the Iris Annotation component to be able to use the GPU, you must comment out the iwp-annotation cpu's docker service and un-comment out the iwp-annotation gpu's docker service in the `iat/docker/docker-compose.local.yml` file.

#### Package Installation
##### Environment Set Up
The folloiwng steps explain how to set up your environment for development. If you already have Homebrew installed and are ready to install the packages/tools, skip to [Install Packages](#install-packages)
1. Install Xcode Command Line Tools
   1. Open Terminal and run the following command:
        ```bash
        xcode-select --install
        ```

   2. In the new dialog windows, confirm and agree to the installation and license agreement
2. Install Homebrew
   1. Open Terminal and run the following command:
        ```bash
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        ```

   2. Type your admin password and hit Enter[^1]
   3. Wait a few minutes until you see a "Installation successcul" message
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
1. Start up Services:
   1. Follow the steps from [Set up Environment Variables](../README.md#set-up-environment-variables)
   2. Follow the [support's README](../support/README.md#change-default-iris-image) to change the required default iris image to an image of your choice
   3. In the terminal, run the following commands to start up the docker containers:
        ```sh
        ./composeScript.sh prune # Clear out old/unwanted data and unused containers, images, etc.
        docker compose -f docker-compose.local.yml up # Runs a docker script that only starts up the services from docker containers. Not the Web Interface Component
        ```

   4. Open a second terminal window and run the following commands to set up the environment variables set in the `.env` file:
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
      
2. Start up Backend Locally:
    1. In the second terminal window, run the following command from the main `iat/` project directory:
		**After this step you can access the application at [http://localhost:8080](http://localhost:8080).**
        ```sh
        ./iwp-script.sh full # Runs the local backend services, including any changes just made
        ```
    2. In a web browser, navigate to `localhost:8080`

3. **(Optional)** Start up Frontend Locally:
    **You only need this step if you want access to a front end that automatically refreshes when changes are made.**

    **Special note about the front end development server:** Due to some Spring Security and webpack-dev-server proxy complexities, the login page does not work through the development server. You likely will see strange and broken behavior on the front end development server. There are two workarounds for this:
    
        - Disable Log In page by setting `iwp.security.secured=false` in the `web/src/main/resources/application.properties` file.
        - Log In from the Backend by navigating to the backend url [http://localhost:8080](http://localhost:8080) and login there. Now, requests through the front end development server [http://localhost:4200](http://localhost:4200) should then be authenticated. 

    1. Open a third terminal window and run the following command from the `web/client/` subdirectory:
		**After this step you can access the front end development server at [http://localhost:4200](http://localhost:4200).**
        ```sh
        yarn run local # Runs the local frontend services, including any changes just made
        ```
    2. In a web browser, navigate to `localhost:4200`
        *Note: you don't for get to fix the login page issue.

4. **(Optional)** Start up the Database Management Container:
    **You only need this step if you want to access the database for debugging purposes.**

    1. Open a fourth terminal window and run the following commands from the `web/` subdirectory: 
        ```sh
        docker run --name iwp_php_admin --network iwp-simple_default -v phpmyadmin-volume:/etc/phpmyadmin/config.user.inc.php --link iwp_mysql:db -p 82:80 -d phpmyadmin/phpmyadmin #Runs the database container
        ```
    2. In a web browser, navigate to `localhost:82`


## Run Java Unit Tests
There are a few basic unit tests included in this project. Before running those tests, you must:
1. Set variables the Required Test Variables section in the `src/test/resources/application-test.properties` file
2. Ensure that at least the mysql and activemq containers are running.
    - You can start the containers by running `docker compose up` in the `../docker/` directory.
3. **OPTIONAL** You might want to replace the `Image.png` file under `src/test/resources/` directory. Just ensure that you keep the name the same or change the name in the `src/test/java/org/mitre/iwp/web/data/MessagingTests.java` file. There are 2 locations that would need to be changed in that file.

Once you have done that, you can run the following command:
    ```bash
    mvn -pl web -am -Dbuild.node.skip=true test
    ```

## Rebuild Docker Image
### Prerequisites
To build this docker image, there are a few prerequisites.
1. Install the needed packages and tools needed by folloiwng the [Install Packages Instructions](#install-packages)
2. Build the Jet dependency
    1. Clone the jet repo from https://github.com/ebts/jet
    2. Build Jet by running the following commands in the main `iat/` project directory:
    ```sh
    cd jet/Jet # Navigates into the subdirectory that contains the pom.xml file
    mvn install
    ```
3. Build the Web JAR by running the following commands in the main `iat/` project directory:
    ```sh
    # Build JAR
    mvn -pl web -am -DskipTests package

    #Check the JAR built successfully
    cd web #Navigates to the web subdirectory
    ls -lah target/*.jar
    ```
    NOTE: This automatically builds the otther local Maven modules (buffer, common, support) that the Web component depends on.

    The build JAR is written to `web/target/`.

### Building Docker Image
If a new docker image is needed to be built, run the following command from the `web/` subdirectory:
    ```sh
    # Add --platform linux/amd64 for Apple Silicon Systems
    docker build --platform linux/amd64 \
    -t new_web_image:YOUR_TAG_HERE \
    -f Dockerfile .
    ```
