>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# Iris Analysis Toolkit

The Iris Analysis Toolkit (IAT), formerly known as the Iris Workstation and Iris Workstation Prototype, is a system that demonstrates the capabilities of an iris examination workstation. It was made to allow users to easily create generic Electronic Biometric Transmission Specification files as well as review and annotate iris images.

## Table of Contents

- [Iris Analysis Toolkit](#iris-analysis-toolkit)
  - [Table of Contents](#table-of-contents)
  - [Software Versions](#software-versions)
  - [Quickstart for Users](#quickstart-for-users)

## Software Versions

This program was built using these package managers, but may work with newer versions:

- Development Platform: Angular Cli -v 17.3.1
- Runtime Environment: node -v >= 20.15.0
- Angular Package Manager: npm -v 10.8.1
- Java SE Environment: java -v 17
- Build Package: maven -v 3.9.6
- JavaScript Package Manager: yarn -v 1.22.22

## Quickstart for Users

This section gives steps to start the program for users who are simply looking to run the application (i.e., are not troubleshooting or developing). Since this section only runs docker containers, it doesn't run any changes made on the local environment.

### Set up Environment Variables
1. Start Up Project
   **IMPORTANT!** The `docker compose` files in the `docker` directory and the `application.properties` files in the `web` directory will use credentials from a `.env` file in that directory. For security reasons, only a `.env.template` file is included with all the variables that need to be filled out. To run the application via docker or locally, please follow these steps:
   1. Make a new file in the `docker` directory called `.env` from the `.env.template` file by running:
      ```sh
      cp .env.template .env
      ```
   2. Fill in the variables in the new `.env` file with usernames and passwords of your choosing.* The database and artemis passwords don't need an encrypted password.
   
See [this link](https://docs.docker.com/compose/environment-variables/set-environment-variables/#compose-file) for details about .env files 

*NOTE: The User Roles need the passwords encrypted. Follow the next step to encrypt your User Role Passwords.

2. Encrypting User Role Passwords
   1. If needed, download the jasypt tool here (http://www.jasypt.org/cli.html). If the ZIP download is not available, check the jasypt Github at (https://github.com/jasypt/jasypt/releases).
   2. Use the commands below to encrypt, and if need be, decrypt passwords. Once you have the encrypted string, paste it into the `.env` file.
      1. When encryping a hash on the commandline you need to escape the `$` characters with a `\`.
      2. The additional options are needed because the spring jayspt uses those defaults to decrypt and it will break without them. 
      3. Please note that if you use this exact command, you will be setting the secret key to `examplepass`. This must be the same string as the `JASYPT_ENCRYPTOR_PASSWORD` value in the `docker/.env` file.
      4. When filling in the `docker/.env` file, make sure to surround the encrypted output with `"ENC()"`. For the example below, if the output was `123abcENCRYPTEDpass`, the variable would be set to `"ENC(123abcENCRYPTEDpass)"`
   
   Example of encrypting and decrypting the string "CHOSENSTRING" using the password key "examplepass":
   ```bash
   # Encrypt
   ./encrypt.sh input="CHOSENSTRING" password=examplepass algorithm=PBEWITHHMACSHA512ANDAES_256 verbose=true stringOutputType=base64 providerName=SunJCE saltGeneratorClassName=org.jasypt.salt.RandomSaltGenerator ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator

   >> 123abcENCRYPTEDpass

   # Decrypt
   ./decrypt.sh input="CHOSENSTRING" password=examplepass algorithm=PBEWITHHMACSHA512ANDAES_256 verbose=true stringOutputType=base64 providerName=SunJCE saltGeneratorClassName=org.jasypt.salt.RandomSaltGenerator ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator
   ```

### Run Application via Docker
The following steps explain how to start the program in "user" mode. This mode is for users who want to simply start it up and run it. This will not run any changes in the local code. To see the changes in the code, please follow the [Run Application From a Script](web/README.md#from-a-script) instructions under the `web/` subdirectory.

NOTE: If you are using a GPU and would like the Iris Annotation component to be able to use the GPU, you must comment out the iwp-annotation cpu's docker service and un-comment out the iwp-annotation gpu's docker service in the `iat/docker/docker-compose.yml` file.

1. In the terminal, run the following commands in the `iat/docker/` subdirectory: 
    ```sh
    ./composeScript.sh prune # Clear out old/unwanted data and unused containers, images, etc:
    docker compose up # Runs the docker-compose.yml script that starts up the service and user interface via docker for easy start up.
    ```

2. In a web browser, navigate to `localhost:8080`
      1. Log in with username and password

