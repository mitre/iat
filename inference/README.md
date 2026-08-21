>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

# Annotation Component
Iris Annotation component is a service for the Iris Analysis Toolkit (IAT) that provides automated labeling for image segments.

## Starting the Service
This service can be ran two ways: 
1. (Recommended) [Docker Image via the IAT](#docker-and-iat)
2. [From a Script](#from-a-script)

If you make changes to the scripts and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
This is the recommended way.
To start the service through docker and run it with the IAT interface, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions. 

### From a Script
#### Prerequisites
- A Linux environment with Python 3.12 and the CPU or GPU Python dependencies installed. The pinned dependency lists are in `core/requirements/`.
- The Iris Annotation application files installed in `/usr/share/iris-annotation/`, including `mq-main.py`, `main.py`, the`segment/` directory, and the generated `*_pb2.py` files.
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
This service can utilize a CPU or a GPU, by an ENV variable USE_CUDA that is set in the Dockerfile. There are two Dockerfiles, one for CPU and one for GPU, which installs different versions packages depending on their mode. **DO NOT CHANGE THE 'USE_CUDA' VARIABLE IN THOSE FILES.** To build a new docker image, run the following command from the `inference/` subdirectory, please note if you are building on an Apple Silicon Mac:
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