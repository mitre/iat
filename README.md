# Iris Analysis Toolkit #

The Iris Analysis Toolkit (IAT), formerly known as the Iris Workstation and Iris Workstation Prototype, is a system that demonstrates the capabilities of an iris examination workstation. It was made to allow users to easily create generic Electronic Biometric Transmission Specification files as well as review and annotate iris images.

## Table of Contents ##

- [Iris Analysis Toolkit](#iris-analysis-toolkit)
  - [Table of Contents](#table-of-contents)
  - [Software Versions](#software-versions)
  - [Quickstart for Users](#quickstart-for-users)
  - [QuickStart for Developers](#quickstart-for-developers)
    - [MacOS Installation](#macos-installation)
    - [Start Up](#start-up)

## Software Versions ##

This program was built using these package managers, but may work with newer versions:

- Development Platform: Angular Cli -v 17.3.1
- Runtime Environment: node -v >= 20.15.0
- Angular Package Manager: npm -v 10.8.1
- Java SE Environment: java -v 17
- Build Package: maven -v 3.9.6

## Quickstart for Users ##

This section gives steps to start the program for users who are simply looking to run the application (i.e., are not troubleshooting or developing). Since this section only runs docker containers, it doesn't run any changes made on the local environment.

1. Start Up Project
   1. **IMPORTANT!** The `docker compose` files in the `iwp-simple` directory will use credentials from a `.env` file in that directory. For security reasons, a `.env` file is not included  in this repo. To use the `docker compose` files you will need to create a `.env` file in the `iwp-simple` directory with the following contents:
		
		DATABASE_PASSWORD=A_STRONG_PASSWORD_OF_YOUR_CHOOSING
		DATABASE_USERNAME=A_USERNAME_OF_YOUR_CHOOSING
		DATABASE_ROOT_PASSWORD=ANOTHER_STRONG_PASSWORD_OF_YOUR_CHOOSING

      See [this link](https://docs.docker.com/compose/environment-variables/set-environment-variables/#compose-file) for details about .env files 
   2. In the terminal, type the following commands to start up docker containers:

    ```bash
    cd iris-workstation/iwp-simple/ #Navigates into to the subdirectory that contains the docker scripts for easy running
    ./composeScript.sh prune #Clear out old/unwanted data and unused containers, images, etc:
    docker compose up #Runs the docker script for easy start up
    ```

2. Use Project
   1. Open choice of web browser and navigate to `localhost:8080`
      1. Log in with username and password
   2. Start generating and annotating files

## QuickStart for Developers ##

This section gives steps to start the program in the "editing mode". The following steps are for developers who are troubleshooting or contributing to the code. This section starts up the program to run only the services in docker containers. The backend and frontend parts are built and ran using the code from the local environment.

### MacOS Installation ###

Follow the steps below to get started with this project's development environment on a Mac.

1. Install Xcode Command Line Tools
   1. Open Terminal and type the following command:

        ```bash
        xcode-select --install
        ```

   2. In the new dialog windows, confirm and agree to the installation and license agreement
2. Install Homebrew
   1. Open Terminal and type the following command:

        ```bash
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        ```

   2. Type your admin password and hit Enter[^1]
   3. Wait a few minutes until you see a "Installation successcul" message
    [^1]: You won't see your keystrokes in the terminal
3. Install Packages
    - Open Terminal and type the following commands to install the packages:

    ```bash
    brew install angular-cli #Angular Cli
    brew install node@20.15.0 #Node
    brew install maven #Maven
    ```

### Start Up ###

1. Clone Project
2. Start Up Project:
   1. In the terminal, type the following commands to start up the docker containers:

        ```bash
        cd iris-workstation/iwp-simple/ #Navigates into to the subdirectory that contains the docker scripts for easy running
        ./composeScript.sh prune #Clear out old/unwanted data and unused containers, images, etc.
        docker compose -f docker-compose.local.yml up #Runs a docker script that only starts up the services from docker containers
        ```

   2. Open a second terminal window and type the following commands to start up the backend:

		**After this step you can access the application at [http://localhost:8080](http://localhost:8080).**

        ```bash
        cd iris-workstation/ #Navigates to the project's home directory
        UNIX: export JASYPT_ENCRYPTOR_PASSWORD="examplepass"
        Windows: $Env:JASYPT_ENCRYPTOR_PASSWORD ="examplepass"
        ./iwp-script.sh full #Runs the local backend services, including any changes just made
        ```

   3. **(Optional)** Open a third terminal window and type the following commands to start up the frontend:

		**You only need this step if you want access to a front end that automatically refreshes when changes are made.**
		
		After this step you can access the front end development server at [http://localhost:4200](http://localhost:4200).

		**Special note about the front end development server:** Due to some Spring Security and webpack-dev-server proxy complexities, the login page does not work through the development server. You likely will see strange and broken behavior on the front end development server. There are two workarounds for this:

		- Disable the login page while doing front end development by setting `iwp.security.secured=false` in the `application.properties` file.

		- Go to the front end hosted by the Java backend [http://localhost:8080](http://localhost:8080) and login there. Requests through the front end development server [http://localhost:4200](http://localhost:4200) should then be authenticated. 

        ```bash
        cd iris-workstation/client/ #Navigates to the project's front end subdirectory
        yarn run local #Runs the local frontend services, including any changes just made
        ```

   4. **(Optional)** Open a fourth terminal window and type the following commands to start up the database management container:

		**You only need this step if you want to access the database for debugging purposes.**

        ```bash
        cd iris-workstation/ #Navigates to the project's home directory
        docker run --name iwp_php_admin --network iwp-simple_default -v phpmyadmin-volume:/etc/phpmyadmin/config.user.inc.php --link iwp_mysql:db -p 82:80 -d phpmyadmin/phpmyadmin #Runs the database container

3. Use Project
    1. Open choice of web browser and navigate to `localhost:4200`
       1. Log in with username and password
    2. To see the database, open choice of web browser and navigate to `localhost:82`
       1. Log in with username and password
    

## NOTICE

#### Approved for Public Release; Distribution Unlimited. Public Release Case Number 23-1989

#### This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) – Alternative IV (Dec 2007)

#### (c) 2024 The MITRE Corporation. All Rights Reserved.