>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# BIQT Component

The BIQT component is an Iris Analysis Toolkit (IAT) service that analyzes an iris image and returns its quality score and whether contact lenses are present.

## Starting the Service

This service can be started only with Docker through the IAT interface. Follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.

If you make changes to the scripts and want to create a new Docker image, see [Rebuild Docker Image](#rebuild-docker-image).

## Rebuild Docker Image
### Prerequisites
##### Package Installation
To build this Docker image, complete the following prerequisites.
1. Install Homebrew by following the [environment setup instructions](../web/README.md#environment-set-up), if it is not already installed.
2. Run the following command to install Maven:
    ```sh
    brew install maven
    ```
3. Build and install the public BIQT Java dependency. From the main `iat/` project directory, clone the BIQT repository outside this repository, then install its Java artifact into your local Maven repository:
    ```sh
    git clone https://github.com/mitre/biqt.git ../biqt-core
    cd ../biqt-core/java
    mvn install -DskipTests
    cd ../../iat
    ```
    This installs `org.mitre:biqt:26.05`, which the IAT BIQT component requires. The BIQT Java tests require a separately built native `libbiqtapi` library, so this command skips those upstream tests.
4. Build the IAT BIQT JAR by running the following command in the main `iat/` project directory:
    ```sh
    mvn -pl biqt -am package
    ```

### Building Docker Image
To build a new Docker image, run the following command from the `iat/biqt/` subdirectory:
  ```sh
  # The '--platform linux/amd64' is for building on Apple Silicon Systems
  docker build --platform linux/amd64 \
  -t new_biqt_image:YOUR_TAG_HERE \
  -f Dockerfile .
  ```
