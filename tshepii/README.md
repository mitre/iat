>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>


# TSHEPII Component

The TSHEPII component is an Iris Analysis Toolkit (IAT) service that analyzes an iris image and returns detected crypts and an unrolled iris location. For more information about the original TSHEPII tool, see the [Original TSHEPII README](doc/README.md#tshepii).

## Starting the Service
This service can be started with Docker through the IAT interface. Follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions.

If you make changes to the scripts and want to create a new Docker image, see [Rebuild Docker Image](#rebuild-docker-image).

## Rebuild Docker Image
To build a new Docker image, run this command from the main `iat/` project directory:
  ```sh
  DOCKER_BUILDKIT=1 docker build --platform linux/amd64 \
  -t new_tshepii_image:YOUR_TAG_HERE \
  -f tshepii/Dockerfile .
  ```

When running the image outside Docker Compose, provide non-empty `ARTEMIS_USER` and `ARTEMIS_PASSWORD` environment variables. The image does not provide default broker credentials.
