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
This service can be started two ways: 
1. (Recommended) [Docker Image via the IAT](#docker-and-iat)
2. [From a Script](#from-a-script)

If you make changes to the scripts and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
This is the recommended way.
To start the service through docker and run it with the IAT interface, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions. 


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
Once you have the prerequisites, follow the steps below:
1. Start an instance of ActiveMQ. Please note the username and password used to start this.
2. Replace the `temp_value` for the `artemis-user` and `artemis-password` in the `acii-client/config/acii.conf` file with the respective values you used in step 1.
3. Run ACII with the following command:
    ```sh
    /usr/local/bin/aciiComponent /etc/iwp-acii/acii.conf /etc/iwp-acii/logger.conf false /var/log/acii.log
    ```

The acii-client is now running, consuming off a queue named "iris.acii.request" and producing on a queue named "iris.acii.response". 

*NOTE: This option is not recommended since a client must publish an `ImageServiceQuery` message to ActiveMQ on `iris.acii.request` and consume the resulting `AciiServiceResponse` message from `iris.acii.response`. A client script is not currently provided.

## Rebuild Docker Image
If a new docker image is needed to be built, run this command from the main `iat/` project directory:
  ```sh
  # Remove `--secret` argument if organization certificate does not need to be installed.
  # The '--platform linux/amd64' is for building on Apple Silicon Systems
  DOCKER_BUILDKIT=1 docker build --platform linux/amd64 -t new_acii_image:YOUR_TAG_HERE \
  --secret id=ca_file,src=ca_file.crt \
  -f acii/Dockerfile .
  ```