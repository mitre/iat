>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

# Annotation Component
The Iris Annotation component is a service for the Iris Analysis Toolkit (IAT) that provides automated labeling for image segments.

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
- A Linux environment with Python 3.12 and the CPU or GPU Python dependencies installed. The pinned dependency lists are in `core/requirements/`.
- The Iris Annotation application files installed in `/usr/share/iris-annotation/`, including `mq-main.py`, `main.py`, the `segment/` directory, and the generated `*_pb2.py` files.
- Model checkpoint files installed in `/usr/share/models`. The supplied `6248.pth` checkpoint is expected in this directory.
- A writable `/mnt/host/tmp` directory. The service temporarily stores image data received in requests there.
- An ActiveMQ Artemis-compatible broker reachable over STOMP. By default, the service connects to `localhost:61613`.
- For GPU inference, a CUDA-compatible PyTorch runtime and `USE_CUDA=True`.
  For CPU inference, leave `USE_CUDA` unset or set it to `False`.

#### Starting the Script
1. Start an ActiveMQ Artemis-compatible broker with STOMP enabled.
2. Set the broker connection environment variables. `STOMP_HOST` defaults to `localhost:61613`; provide `STOMP_USER` and `STOMP_PASSWORD` when the broker requires authentication.
3. Start the service:
   ```sh
   export STOMP_HOST="localhost:61613"
   export STOMP_USER="artemis"
   export STOMP_PASSWORD="your-password"
   export USE_CUDA="False"

   python3 /usr/share/iris-annotation/mq-main.py
   ```

The service consumes protobuf `ImageServiceQuery` messages from `iris.annotation.request` and sends `IrisAnnotationReply` messages to `iris.annotation.response`. Failed requests are sent to `iris.annotation.response-failures`. 

*NOTE: This option is not recommended since a client must publish an `ImageServiceQuery` message to ActiveMQ on `iris.annotation.request` and consume the resulting `IrisAnnotationReply` message from `iris.annotation.response`. A client script is not currently provided.

## Rebuild Docker Image
This service can use a CPU or GPU, based on the `USE_CUDA` environment variable set in the Dockerfile. The CPU and GPU Dockerfiles install different dependency versions. **Do not change `USE_CUDA` in these files.** To build a new Docker image from the `inference/` subdirectory, use the applicable command below. Apple Silicon users should retain the `--platform linux/amd64` option for the CPU build.
  CPU Version:
  ```sh
  cd core

  # Add --platform linux/amd64 for Apple Silicon Systems
  # Remove `--secret` argument if organization certificate does not need to be installed.
  docker build --platform linux/amd64 -t new_inference-cpu:YOUR_TAG_HERE \
  --secret id=ca_file,src=ca_file.crt \
  -f Dockerfile-CPU .
  ```

  GPU Version:
  ```sh
  cd core
  
  # Remove `--secret` argument if organization certificate does not need to be installed.
  docker build -t new_inference_gpu:YOUR_TAG_HERE \
  --secret id=ca_file,src=ca_file.crt \
  -f Dockerfile-GPU .
  ```
