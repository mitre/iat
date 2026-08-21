>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>
Need to add new Image.png under resources

# Pupil Dialation Mode Component
Pupil Dialation Mode (PDM) component is a service for the Iris Analysis Toolkit (IAT) that enables the user to change the ratio of the pupil to iris by dilating pupil.

## Starting the Service
This service can be ran two ways:
1. (Recommended) [Docker Image via the IAT](#docker-and-iat)
2. [From a Script](#from-a-script)

If you make changes to the scripts or the models and would like to create a new docker image, please jump to [Rebuild Docker Image](#rebuild-docker-image).

### Docker and IAT
This is the recommended way.
To start the service through docker and run it with the IAT interface, please follow the [Quickstart for Users](../README.md#quickstart-for-users) instructions. 

### From a Script
- An ActiveMQ Artemis-compatible broker reachable over STOMP.
<a id="download-models"></a>
- The PDM model files in `models/`. To obtain these models, run the following command:
   ```sh
   ./download-models.sh
   ```

   *NOTE: If the version for the PDM Models changed, the `VERSION_TAG` variable will need to be updated to the correct version.

- A Python 3.10 environment with the dependencies in `requirements.txt`. To create an active environment with those dependencies, run the following commands:
   ```sh
   python3.10 -m venv <NAME_OF_VIRTUAL_ENVIRONMENT>
      source <NAME_OF_VIRTUAL_ENVIRONMENT>/bin/activate
   pip install -r requirements.txt
   ```
- A local environment file containing the Artemis credentials. For security reasons, only a `.env.template` file is included with all the variables that need to be filled out. To run the application via docker or locally, please follow these steps/commands:
   1. Make a new file in the `pdm` subdirectory called `.env` from the `.env.template` file by running:
      ```sh
      cp .env.template .env
      ```
   2. Fill in the Artemis variables in the new `.env` file with a username and password of your choosing.
   3. Change the STOMP variables if needed.


### Starting the Script
Once the [prerequisites](#prerequisites) are complete, start the ActiveMQ Artemis broker and run
the following commands from the `pdm` directory:

```sh
set -a
. ./.env
set +a
python3 Comparison_GUI.py
```

To run with a CUDA-capable GPU, append `--cuda` to the final command.

The service is now runnign and will continue to run while it waits for messages from `iris.pdm.request` and
`iris.dual.pdm.request`, then produces responses on `iris.pdm.response` and
`iris.dual.pdm.response`. Press `Ctrl-C` to stop the service.

*NOTE: This option is not recommended since a client must publish an `ImageServiceQuery` message to ActiveMQ on `iris.pdm.request` and consume the resulting `DeformerWrapper` message from `iris.pdm.response`. For dual-image processing, the client must publish and consume `DualPDMComparison` messages on `iris.dual.pdm.request` and `iris.dual.pdm.response`, respectively. A complete client script is not currently provided.

## Rebuild Docker Image
*Before building the docker image, the models must be downloaded.* Follow the [download the models](#download-models) instructions.
If a new docker image is needed to be built, run this command from the `pdm/` subdirectory:
  ```sh
  docker build --platform linux/amd64 -t new_pdm_image:YOUR_TAG_HERE \
  -f Dockerfile .
  ```

## Run Smoke Tests
Included in this project is a `test.py` script that contains 2 smoke tests which sends images to the single PDM and dual PDM service. Before running those tests, you must:
1. Set the STOMP_HOST variable section in your new `.env` file from the [From a Script: Prerequisites](#prerequisites)
2. Ensure that at least the activeMQ and PDM services are running.
    - You can start the containers by running `docker compose up` in the `../docker/` directory.
3. **OPTIONAL** You might want to replace the `Image.png` file under the `resources/` directory. Just ensure that you keep the name the same or change the name in the `test.py` file. There are 3 locations that would need to be changed in that file. NOTE: The image must be in PNG format.