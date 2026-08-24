>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# ACII Component

The ACII component is an Iris Analysis Toolkit (IAT) service that analyzes an iris image and returns its orientation, including whether the iris is left or right.

## Starting the Service
This service can be started in two ways:
1. (Recommended) [Docker Image via the IAT](#docker-and-iat)
2. [From a Script](#from-a-script)

If you make changes to the scripts and want to create a new Docker image, see [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
This is the recommended way.
To start the service with Docker through the IAT interface, follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.


### From a Script
#### Prerequisites
- A Linux environment with `sudo` access. The installation script supports Alpine and APT-based distributions.
- A C++17 compiler, CMake, Make, Autotools, and Curl.
- Protocol Buffers version 3 or later, Apache ActiveMQ-CPP, Apache Log4cxx, APR, and OpenCV 2.4.
- An ActiveMQ-compatible broker reachable from the ACII service.

The installation script installs or builds the required native dependencies and builds the ACII client.

```sh
cd acii/install
sudo ./install.sh
```

#### Starting the Script
Once you have completed the prerequisites, follow these steps:
1. Start an instance of ActiveMQ and note the username and password you configure.
2. Replace `temp_value` for `artemis-user` and `artemis-password` in `acii-client/config/acii.conf` with the values from step 1.
3. Run ACII with the following command:
    ```sh
    /usr/local/bin/aciiComponent /etc/iwp-acii/acii.conf /etc/iwp-acii/logger.conf false /var/log/acii.log
    ```

The ACII client is now running. It consumes messages from `iris.acii.request` and produces messages on `iris.acii.response`.

*Note: This option is not recommended because a client must publish an `ImageServiceQuery` message to ActiveMQ on `iris.acii.request` and consume the resulting `AciiServiceResponse` message from `iris.acii.response`. A client script is not currently provided.*

## Rebuild Docker Image
To build a new Docker image, run this command from the main `iat/` project directory:
  ```sh
  # Remove `--secret` argument if organization certificate does not need to be installed.
  # `--platform linux/amd64` is required when building on Apple Silicon systems.
  DOCKER_BUILDKIT=1 docker build --platform linux/amd64 -t new_acii_image:YOUR_TAG_HERE \
  --secret id=ca_file,src=ca_file.crt \
  -f acii/Dockerfile .
  ```
