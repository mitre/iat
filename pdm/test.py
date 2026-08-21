import stomp
import Iwp_pb2
import Pdm_pb2
from PIL import Image
import io
import os
from typing import Tuple

PDM_REQUEST_QUEUE = "iris.pdm.request"
DUAL_PDM_REQUEST_QUEUE = "iris.dual.pdm.request"
ARTEMIS_ANYCAST_HEADERS = {"destination-type": "ANYCAST"}

def stomp_host() -> Tuple[str, int]:
    host_and_port = os.getenv("STOMP_HOST")
    if host_and_port:
        if ":" in host_and_port:
            host, port = host_and_port.rsplit(":", 1)
        else:
            host, port = host_and_port, "61613"
    else:
        host = os.getenv("ACTIVEMQ_HOST")
        port = os.getenv("ACTIVEMQ_PORT")

    return host or "activemq", int(port)

def artemis_credentials() -> Tuple[str, str]:
    username = os.getenv("ARTEMIS_USER")
    password = os.getenv("ARTEMIS_PASSWORD")
    
    if not username or not password:
        raise RuntimeError(f"Missing required Artemis credentials")

    return username, password

def connect_to_broker(auto_decode=True):
    conn = stomp.Connection([stomp_host()], auto_decode=auto_decode)
    username, password = artemis_credentials()
    conn.connect(username, password, wait=True)
    return conn


def send_single_pdm_test_message():
    # Prepare the image data
    image_path = "resources/Image.png"
    with open(image_path, "rb") as f:
        image_bytes = f.read()

    # Load the image to extract metadata
    image = Image.open(io.BytesIO(image_bytes))
    width, height = image.size

    # Create the Protobuf message
    image_service_query = Iwp_pb2.ImageServiceQuery()
    image_service_query.imageId = "test_image_id"
    image_service_query.imageBytes = image_bytes
    image_service_query.width = width
    image_service_query.height = height
    image_service_query.imageType = Iwp_pb2.ImageServiceQuery.PNG  # Assuming PNG format

    # Serialize the Protobuf message into a byte array
    serialized_message = image_service_query.SerializeToString()
    print(
        "Serialized Protobuf Message:",
        type(serialized_message),
        len(serialized_message),
    )

    # Connect to ActiveMQ Artemis and send the message as raw bytes
    conn = connect_to_broker()
    conn.send(
        destination=PDM_REQUEST_QUEUE,
        body=serialized_message,
        headers={
            **ARTEMIS_ANYCAST_HEADERS,
            "content-type": "application/octet-stream",
        },
    )
    conn.disconnect()

    print("Test message sent successfully")


def send_dual_pdm_test_message():
    # Prepare the image data
    probe_path = "resources/Image.png"
    cand_path = "resources/Image.png"

    with open(probe_path, "rb") as f:
        probe_bytes = f.read()

    with open(cand_path, "rb") as f:
        candidate_bytes = f.read()

    # Create the Protobuf message
    query = Pdm_pb2.DualPDMComparison()
    query.probe = probe_bytes
    query.candidate = candidate_bytes

    # Serialize
    serialized_message = query.SerializeToString()
    print(
        "Serialized Protobuf Message:",
        type(serialized_message),
        len(serialized_message),
    )


    # Connect to ActiveMQ Artemis and send the message as raw bytes
    conn = connect_to_broker()
    conn.send(
        destination=DUAL_PDM_REQUEST_QUEUE,
        body=serialized_message,
        headers={
            **ARTEMIS_ANYCAST_HEADERS,
            "content-type": "application/octet-stream",
        },
    )
    conn.disconnect()

    print("Test message sent successfully")


#### Test PDM Capabilities ####
send_single_pdm_test_message()
send_dual_pdm_test_message()
