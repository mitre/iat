>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# TSHEPII Component

The TSHEPII component is an Iris Analysis Toolkit (IAT) service that analyzes an iris image and returns the detected crypts and unrolled the location for the iris. For more information about the TSHEPII's original tool, please see the [Original TSHEPII README](doc/README.md#tshepii).

## Starting the Service
This service can be started through docker and ran with the IAT interface. To start the service that way, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions. 

If you make changes to the scripts and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

## Rebuild Docker Image
If a new docker image is needed to be built, run this command from the main `iat/` project directory:
  ```sh
  DOCKER_BUILDKIT=1 docker build --platform linux/amd64 \
  -t new_tshepii_image:YOUR_TAG_HERE \
  -f tshepii/Dockerfile .
  ```
