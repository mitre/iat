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
  - [Build Everything from Source](#build-everything-from-source)
  - [Quickstart for Users](#quickstart-for-users)

## Software Versions

This project was built with the following tools and may work with newer versions:

- Development platform: Angular CLI 17.3.1
- Runtime environment: Node.js 20.15.0 or later
- Angular package manager: npm 10.8.1
- Java SE environment: Java 17
- Build tool: Maven 3.9.6
- JavaScript package manager: Yarn 1.22.22

## Build Everything from Source

Use the provided build script to build every IAT component from a clean source checkout. The script builds the public JET and BIQT dependencies, packages the Maven modules, downloads the PDM models, and builds local CPU Docker images for ACII, BIQT, Iris Annotation, TSHEPII, PDM, and the Web component.

### Prerequisites

- Docker with BuildKit enabled. Docker Desktop users should enable Linux/AMD64 emulation when building on Apple Silicon.
- Git, Java 17, Maven, and Bash.
- Internet access to clone public dependencies and download Maven, Python, PDM-model, and container-image dependencies.
- Enough disk space for Maven caches, model files, and Docker build layers.

If your network uses a TLS-inspecting proxy, provide its PEM root certificate with `IAT_CA_FILE`. The script passes the certificate to image builds as a BuildKit secret so the affected build stages can add it to their trust stores.

### Build

From the repository root, run:

```sh
./scripts/build-all.sh
```

For a TLS-inspecting network, run:

```sh
IAT_CA_FILE=/path/to/organization-root-ca.pem ./scripts/build-all.sh
```

The script clones the public dependencies into a sibling `iat-dependencies/` directory by default. Set `IAT_DEPENDENCY_ROOT` to use another location. It tags the local images with the same names and versions used by `docker/docker-compose.yml`, so Docker Compose uses the images you just built. The default target platform is `linux/amd64`, matching the Compose configuration.

The script builds the CPU Iris Annotation image. To build the GPU variant instead, run the equivalent command after the script completes:

```sh
docker build -t ghcr.io/mitre/iat/iwp-annotation-gpu:26.08 \
  -f inference/core/Dockerfile-GPU inference/core
```

After the build completes, create `docker/.env` from `docker/.env.template`, provide strong non-empty credentials and a database name, and start the local images with `docker compose up` from `docker/`.

## Quickstart for Users

This section explains how to start the application for users who only need to run it and are not troubleshooting or developing. Because it runs only Docker containers, local source-code changes are not included.

### Set up Environment Variables
1. Start the project.
   **Important:** The Docker Compose files in the `docker` directory and the `application.properties` files in the `web` directory use credentials from a `.env` file in the `docker` directory. For security reasons, the repository includes only a `.env.template` file. To run the application with Docker or locally, follow these steps:
   1. Make a new file in the `docker` directory called `.env` from the `.env.template` file by running:
      ```sh
      cp .env.template .env
      ```
   2. Set the variables in the new `.env` file to usernames and passwords of your choosing. The database and Artemis passwords do not need to be encrypted.
   
See the [Docker documentation](https://docs.docker.com/compose/environment-variables/set-environment-variables/#compose-file) for details about `.env` files.

*Note: User-role passwords must be BCrypt hashes encrypted with Jasypt. Follow the next step to prepare them.*

2. Prepare user-role passwords.
   1. Create a BCrypt hash of the password. The following command uses a temporary Docker container and prints only the hash. Replace `YOUR_PASSWORD` with the password to use:
      ```sh
      bcrypt_hash="$(docker run --rm httpd:2.4-alpine \
        htpasswd -bnBC 12 '' 'YOUR_PASSWORD' | cut -d: -f2)"
      printf '%s\n' "$bcrypt_hash"
      ```
   2. Download and unpack the [Jasypt command-line tool](http://www.jasypt.org/cli.html). If the ZIP download is unavailable, see the [Jasypt releases](https://github.com/jasypt/jasypt/releases). From the unpacked Jasypt directory, encrypt the BCrypt hash with the value assigned to `JASYPT_ENCRYPTOR_PASSWORD` in `docker/.env`:
      ```sh
      ./bin/encrypt.sh input="$bcrypt_hash" password="$JASYPT_ENCRYPTOR_PASSWORD" algorithm=PBEWITHHMACSHA512ANDAES_256 verbose=true stringOutputType=base64 providerName=SunJCE saltGeneratorClassName=org.jasypt.salt.RandomSaltGenerator ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator
      ```
      Quoting `"$bcrypt_hash"` preserves the `$` characters in the BCrypt value.
   3. Set the appropriate password variable in `docker/.env` to the encrypted output, wrapped in `ENC(...)`. For example:
      ```dotenv
      DEFAULT_USER_PASSWORD=ENC(123abcENCRYPTEDpass)
      ```
      Repeat these steps for `DEFAULT_REVIEWER_PASSWORD` and `DEFAULT_SUPERVISOR_PASSWORD`.

   Default users are created only when the application initializes an empty database. If containers have already initialized the database with incorrect passwords, changing `.env` does not update those accounts. For a disposable local installation, recreate the database before starting the application:
   ```sh
   docker compose down
   docker volume rm docker_my-db
   docker compose up
   ```
   This permanently deletes the local IAT database volume and its data.

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
