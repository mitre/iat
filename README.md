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

This project was built with the following tools and may work with newer versions:

- Development platform: Angular CLI 17.3.1
- Runtime environment: Node.js 20.15.0 or later
- Angular package manager: npm 10.8.1
- Java SE environment: Java 17
- Build tool: Maven 3.9.6
- JavaScript package manager: Yarn 1.22.22

## Quickstart for Users

This section explains how to start the application for users who only need to run it and are not troubleshooting or developing. Because it runs only Docker containers, local source-code changes are not included.

### Set up Environment Variables
1. Start the project.
   **Important:** The Docker Compose files in the `docker` directory and the `application.properties` files in the `web` directory use credentials from a `.env` file in the `docker` directory. For security reasons, the repository includes only a `.env.template` file. To run the application with Docker or locally, follow these steps:
   1. Make a new file in the `docker` directory called `.env` from the `.env.template` file by running:
      ```sh
      cp .env.template .env
      ```
   2. Set the variables in the new `.env` file to usernames and passwords of your choosing.* The database and Artemis passwords do not need to be encrypted.
   
See the [Docker documentation](https://docs.docker.com/compose/environment-variables/set-environment-variables/#compose-file) for details about `.env` files.

*Note: User-role passwords must be encrypted. Follow the next step to encrypt them.*

2. Encrypt user-role passwords.
   1. If needed, download the [Jasypt command-line tool](http://www.jasypt.org/cli.html). If the ZIP download is unavailable, see the [Jasypt releases](https://github.com/jasypt/jasypt/releases).
   2. Use the commands below to encrypt or decrypt passwords. Paste the encrypted string into the `.env` file.
      1. When encrypting a hash on the command line, escape `$` characters with `\`.
      2. The additional options are required because Spring Jasypt uses these defaults for decryption.
      3. Please note that if you use this exact command, you will be setting the secret key to `examplepass`. This must be the same string as the `JASYPT_ENCRYPTOR_PASSWORD` value in the `docker/.env` file.
      4. When filling in the `docker/.env` file, make sure to surround the encrypted output with `"ENC()"`. For the example below, if the output was `123abcENCRYPTEDpass`, the variable would be set to `"ENC(123abcENCRYPTEDpass)"`
   
   The following example encrypts `CHOSENSTRING` with the password `examplepass` and then decrypts the resulting value:
   ```bash
   # Encrypt
   ./encrypt.sh input="CHOSENSTRING" password=examplepass algorithm=PBEWITHHMACSHA512ANDAES_256 verbose=true stringOutputType=base64 providerName=SunJCE saltGeneratorClassName=org.jasypt.salt.RandomSaltGenerator ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator

   >> 123abcENCRYPTEDpass

   # Decrypt
   ./decrypt.sh input="123abcENCRYPTEDpass" password=examplepass algorithm=PBEWITHHMACSHA512ANDAES_256 verbose=true stringOutputType=base64 providerName=SunJCE saltGeneratorClassName=org.jasypt.salt.RandomSaltGenerator ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator
   ```

### Run Application via Docker
The following steps start the application in user mode. This mode runs published containers and does not include local source-code changes. To run local changes, follow the [Run Application From a Script](web/README.md#from-a-script) instructions in the `web/` subdirectory.

*Note: To use a GPU for the Iris Annotation component, comment out the CPU `iwp-annotation` Docker service and uncomment the GPU service in `iat/docker/docker-compose.yml`.*

The default Compose deployment exposes only the web application. ActiveMQ Artemis and MySQL remain on the internal Docker network. Use `docker-compose.local.yml` for local development; its broker and database ports bind only to `127.0.0.1`.

1. In a terminal, run the following commands in the `iat/docker/` subdirectory:
    ```sh
    ./composeScript.sh prune # Removes unused containers, images, and other data.
    docker compose up # Starts the services and user interface with Docker Compose.
    ```

2. In a web browser, navigate to `http://localhost:8080` and log in with your configured username and password.
