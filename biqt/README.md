>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# BIQT Component

The BIQT component is an Iris Analysis Toolkit (IAT) service that analyzes an iris image and returns it's quality score and whether or not there are contacts present.

## Starting the Service:
This service can only be started through docker and ran with the IAT inferface. To start the service that way, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.

If you make changes to the scripts and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

## Rebuild Docker Image
### Prerequisites
##### Package Installation
To build this docker image, there are a few prerequisites.
1. Get Homebrew set up by following the [Environment Set Up](#../web/README.md#environment-set-up) instructions. If Homebrew is already installed, skip this step.
2. Open Terminal and run the following command to install the maven package:
    ```sh
    brew install maven
    ```
3. Build and install the public BIQT Java dependency. From the main `iat/` project directory, clone the BIQT repository outside this repository, then install its Java artifact into your local Maven repository:
    ```sh
    git clone https://github.com/mitre/biqt.git ../biqt-core
    cd ../biqt-core/java
    mvn install
    cd ../../iat
    ```
    This installs `org.mitre:biqt:26.05`, which the IAT BIQT component requires.
4. Build the IAT BIQT JAR by running the following command in the main `iat/` project directory:
    ```sh
    mvn -pl biqt -am package
    ```

### Building Docker Image
If a new docker image is needed to be built, run the following command from the `iat/biqt/` subdirectory:
  ```sh
  # The '--platform linux/amd64' is for building on Apple Silicon Systems
  docker build --platform linux/amd64 \
  -t new_biqt_image:YOUR_TAG_HERE \
  -f Dockerfile .
  ```
