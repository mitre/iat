from PIL import Image, UnidentifiedImageError
import torch
import sys
import time
from modules.irisRecognition import irisRecognition
from modules.utils import get_cfg
import argparse
import os
from LinearDeformer import LinearDeformer
from BiomechDeformer import BiomechDeformer
from CircleMaskFinder import CircleMaskFinder
from EyePreserve import EyePreserve
import numpy as np
import Pdm_pb2
import Iwp_pb2
import stomp
import logging
import io
from typing import Optional, Tuple
from concurrent.futures import ThreadPoolExecutor

import warnings
warnings.filterwarnings("ignore", category=FutureWarning)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

PDM_REQUEST_QUEUE = "iris.pdm.request"
PDM_RESPONSE_QUEUE = "iris.pdm.response"
DUAL_PDM_REQUEST_QUEUE = "iris.dual.pdm.request"
DUAL_PDM_RESPONSE_QUEUE = "iris.dual.pdm.response"
ARTEMIS_ANYCAST_HEADERS = {"destination-type": "ANYCAST"}
ARTEMIS_ANYCAST_SUBSCRIBE_HEADERS = {"subscription-type": "ANYCAST"}

def stomp_host() -> Tuple[str, int]:
    host_and_port = os.getenv("STOMP_HOST")
    if host_and_port:
        if ":" in host_and_port:
            host, port = host_and_port.rsplit(":", 1)
        else:
            host, port = host_and_port, "61613"
    else:
        host = os.getenv("ACTIVEMQ_HOST", "activemq")
        port = os.getenv("ACTIVEMQ_PORT", "61613")

    return host or "activemq", int(port)

def artemis_credentials() -> Tuple[str, str]:
    username = os.getenv("ARTEMIS_USER")
    password = os.getenv("ARTEMIS_PASSWORD")
    
    if not username or not password:
        raise RuntimeError(f"Missing required Artemis credentials")

    return username, password

def stomp_heartbeats() -> Optional[Tuple[int, int]]:
    heartbeats = os.getenv("STOMP_HEARTBEATS")
    if not heartbeats:
        return None

    try:
        outgoing, incoming = heartbeats.split(",", 1)
        return int(outgoing), int(incoming)
    except ValueError:
        logger.warning("Invalid STOMP_HEARTBEATS value %r; using stomp.py defaults.", heartbeats)
        return None

def normalize_destination(destination: Optional[str]) -> Optional[str]:
    if destination and destination.startswith("/queue/"):
        return destination.removeprefix("/queue/")
    return destination

## Helper functions
def get_concat_h(images):
    widths = []
    width = 0
    for img in images:
        widths.append(width)
        width += img.width
    height = images[0].height
    dst = Image.new('RGB', (width, height))
    for i, img in enumerate(images):
        dst.paste(img, (widths[i], 0))
    return dst

def get_concat_h_cut_center(im1, im2):
    dst = Image.new('RGB', (im1.width + im2.width, min(im1.height, im2.height)))
    dst.paste(im1, (0, 0))
    dst.paste(im2, (im1.width, (im1.height - im2.height) // 2))
    return dst

def progressbar(it, prefix="", size=60, out=sys.stdout): # Python3.6+
    count = len(it)
    start = time.time() # time estimate start
    def show(j):
        x = int(size*j/count)
        # time estimate calculation and string
        remaining = ((time.time() - start) / j) * (count - j)   
        mins, sec = divmod(remaining, 60) # limited to minutes
        time_str = f"{int(mins):02}:{sec:03.1f}"
        print(f"{prefix}[{u'█'*x}{('.'*(size-x))}] {j}/{count} Est wait {time_str}", end='\r', file=out, flush=True)
    show(0.1) # avoid div/0
    for i, item in enumerate(it):
        yield item
        show(i+1)
    print("\n", flush=True, file=out)

## Stores all the models and all the deformation results

class DeformerWrapper:
    def __init__(self, wide_size, square_size, device):
        super().__init__()
        self.device = device
        self.circlemaskfinder = CircleMaskFinder(mask_net_path = "./models/nestedsharedatrousresunet-156-0.026496-maskIoU-0.943222.pth", circle_net_path = './models/convnext_tiny-1076-0.030622-maskIoU-0.938355.pth', device=device)
        self.ldeformer = LinearDeformer(device=device)
        self.bdeformer = BiomechDeformer(device=device)
        self.eyepreserve = EyePreserve(net_path="./models/0007-val_loss-22.957276336365826+-0.0-val_bit_match-81.10588183031244+-0.0-val_linear_bit_match-75.13033321984577+-0.0.pth", device=device)
        self.irisRec = irisRecognition(get_cfg('./cfg.yaml'), use_hough=False)
        
        self.square_size = square_size
        self.wide_size = wide_size

        self.ir_ratio = 16/14
        self.vis_frac_hal = 0.2
        self.vis_frac_border = 0.5

        self.dnet_im1_alpha_crop = {}
        self.dnet_im1_alpha_nocrop = {}
        self.dnet_im1_alpha_score = {}

        self.converted = False
        self.im1 = None
        self.im2 = None

    def load_images(self, image_data):
        try:
            # Load image from byte array
            self.im1 = Image.open(io.BytesIO(image_data))
            logging.info(f"Given image has mode {self.im1.mode} {self.converted}")

            if self.im1.mode == "I;16":
                self.im1 = self.convert_16bit_gray_to_rgb(self.im1)
                self.converted = True
            else:
                logging.info(f"Image is {self.im1.mode}")

            logging.info("Image loaded successfully from byte array")
        except UnidentifiedImageError:
            logging.error("Failed to open the image from byte array")
        except Exception as e:
            logging.error(f"An unexpected error occurred while loading the image: {e}", exc_info=True)


    def load_dual_images(self, probe_data, candidate_data):
        logging.info(f"Probe and Candidate are of type: {type(probe_data), type(candidate_data)}")

        try:
            self.im1 = Image.open(io.BytesIO(probe_data))
            self.im2 = Image.open(io.BytesIO(candidate_data))

            logging.info("Successfully uploaded probe and candidate")

        except UnidentifiedImageError:
            logging.error("Failed to open the DUAL image from byte array")
        except Exception as e:
            logging.error(f"An unexpected error occurred while loading the DUAL images: {e}", exc_info=True)

    def convert_16bit_gray_to_rgb(self, image):
        # Convert image to numpy array
        gray_array = np.array(image, dtype=np.float32)
        
        # Normalize 16-bit values to [0, 1]
        normalized_gray = gray_array / 65535.0
        
        # Apply gamma correction (linearize)
        linear_gray = np.power(normalized_gray, 2.2)
        
        # Expand to RGB
        linear_rgb = np.stack([linear_gray] * 3, axis=-1)
        
        # Encode gamma (apply sRGB gamma)
        encoded_rgb = np.power(linear_rgb, 1/2.2)
        
        # Scale back to [0, 255] and convert to uint8
        rgb_array = (encoded_rgb * 255).astype(np.uint8)
        
        # Convert back to PIL Image
        rgb_image = Image.fromarray(rgb_array, mode="RGB")
        
        return rgb_image

    def findMaskAndCircles(self, image):
        # Ensures input image has a 4:3 aspect ratio by padding with gray pixels
        image, self.w_pad, self.h_pad = self.circlemaskfinder.fix_image(image)
        
        # Image segmentation into foreground, background, pupil data, iris data
        mask, back_mask, pxyr, ixyr = self.circlemaskfinder.segmentAndCircApprox(image.convert("L"))
        alpha = pxyr[2]/ixyr[2]
        image_crop = image.crop((ixyr[0] - int(round(self.ir_ratio*ixyr[2])), ixyr[1] - int(round(self.ir_ratio*ixyr[2])), ixyr[0] + int(round(self.ir_ratio*ixyr[2])), ixyr[1] + int(round(self.ir_ratio*ixyr[2]))))
    
        return image, image_crop, mask, back_mask, pxyr, ixyr, alpha

    def getCodeAndPolarMask(self, image, mask, pxyr, ixyr):
        image_polar, mask_polar = self.irisRec.cartToPol_torch(image.convert('L'), mask, pxyr, ixyr)
        image_code = self.irisRec.extractCode(image_polar)
        return image_code, mask_polar
    
    def getHDBIFScore(self, code1, code2, mask1, mask2):
        score, _ = self.irisRec.matchCodes(code1, code2, mask1, mask2)
        return score

    def getEyePreserveDeform(self, image, mask, back_mask, pxyr, ixyr, target_image, target_mask, target_back_mask, target_pxyr, target_ixyr, hal_vis=False):
        if hal_vis:
            out_image, hal_mask = self.eyepreserve.deform_with_hal_vis(image, mask, back_mask, pxyr, ixyr, target_image, target_mask, target_back_mask, target_pxyr, target_ixyr, self.device)
            out_image_np_vis = np.stack((np.array(out_image),)*3, axis=-1)
            out_image_vis = Image.fromarray(out_image_np_vis, 'RGB')
            return out_image_vis, out_image
        else:
            return self.eyepreserve.deform(image, mask, pxyr, ixyr, target_image, target_mask, target_pxyr, target_ixyr, self.device)
    
    def process_for_direct_deform(self):
        self.im1, self.im1_crop, self.mask1, self.back_mask1, self.pxyr1, self.ixyr1, self.alpha1 = self.findMaskAndCircles(self.im1)
        self.im1_code, self.mask1_polar = self.getCodeAndPolarMask(self.im1, self.mask1, self.pxyr1, self.ixyr1)

        self.im1_resized = self.im1.resize(self.wide_size)
        self.im1_crop_resized = self.im1_crop.resize(self.square_size)

    def process_for_direct_deform_DUAL(self):
        ## Same pre-processing as Alpha Deformation ##
        # Image 1
        self.im1, self.im1_crop, self.mask1, self.back_mask1, self.pxyr1, self.ixyr1, self.alpha1 = self.findMaskAndCircles(self.im1)
        self.im1_code, self.mask1_polar = self.getCodeAndPolarMask(self.im1, self.mask1, self.pxyr1, self.ixyr1)

        self.im1_resized = self.im1.resize(self.wide_size)
        self.im1_crop_resized = self.im1_crop.resize(self.square_size)

        # Image 2
        self.im2, self.im2_crop, self.mask2, self.back_mask2, self.pxyr2, self.ixyr2, self.alpha2 = self.findMaskAndCircles(self.im2)
        self.im2_code, self.mask2_polar = self.getCodeAndPolarMask(self.im2, self.mask2, self.pxyr2, self.ixyr2)

        self.im2_resized = self.im2.resize(self.wide_size)
        self.im2_crop_resized = self.im2_crop.resize(self.square_size)

        ## Beginning of DUAL PDM ##
        # Image 1 to Image 2
        self.dnet_im1_im2, dnet_im1_im2_novis = self.getEyePreserveDeform(self.im1.convert('L'), self.mask1.copy(), self.back_mask1.copy(), self.pxyr1.copy(), self.ixyr1.copy(), self.im2.convert("L"), self.mask2.copy(), self.back_mask2.copy(), self.pxyr2.copy(), self.ixyr2.copy(), hal_vis=True)
        dnet_im1_im2_novis, _, dnet_im1_im2_mask, _, dnet_im1_im2_pxyr, dnet_im1_im2_ixyr, _ = self.findMaskAndCircles(dnet_im1_im2_novis.convert("L"))

        # Image 2 to Image 1
        self.dnet_im2_im1, dnet_im2_im1_novis = self.getEyePreserveDeform(self.im2.convert('L'), self.mask2.copy(), self.back_mask2.copy(), self.pxyr2.copy(), self.ixyr2.copy(), self.im1.convert("L"), self.mask1.copy(), self.back_mask1.copy(), self.pxyr1.copy(), self.ixyr1.copy(), hal_vis=True)
        dnet_im2_im1_novis, _, dnet_im2_im1_mask, _, dnet_im2_im1_pxyr, dnet_im2_im1_ixyr, _ = self.findMaskAndCircles(dnet_im2_im1_novis.convert("L"))

    
    def process_for_alpha(self, alpha):
        dnet_im1_crop, dnet_im1_nocrop, dnet_im1_highlight = self.eyepreserve.deform_with_alpha(self.im1.convert('L'), self.mask1.copy(), self.back_mask1.copy(), self.pxyr1.copy(), self.ixyr1.copy(), alpha, self.device)
        self.dnet_im1_alpha_crop[alpha] = dnet_im1_crop.resize(self.square_size)
        w,h = self.im1.size
        w2,h2 = dnet_im1_nocrop.size

        self.dnet_im1_alpha_nocrop[alpha] = dnet_im1_nocrop.crop((int(self.w_pad), int(self.h_pad), int((w-self.w_pad)), int(h-self.h_pad)))
        logging.info(f"Outgoing image {self.converted} has width x height at {alpha} {w2,h2}, should be {w,h}. w_pad and h_pad {self.w_pad, self.h_pad}")
        
        dnet_im1_iso, _, _ = self.circlemaskfinder.fix_image(dnet_im1_crop)
        dnet_mask1_iso, _, dnet_pxyr1_iso, dnet_ixyr1_iso = self.circlemaskfinder.segmentAndCircApprox(dnet_im1_iso.convert("L"))
        dnet_orig_im1_crop = self.eyepreserve.deform(dnet_im1_iso, dnet_mask1_iso, dnet_pxyr1_iso, dnet_ixyr1_iso, self.im1.convert('L'), self.mask1.copy(), self.pxyr1.copy(), self.ixyr1.copy(), self.device)
        dnet_orig_im1_iso, _, _ = self.circlemaskfinder.fix_image(dnet_orig_im1_crop)
        dnet_orig_mask1_iso, _, dnet_orig_pxyr1_iso, dnet_orig_ixyr1_iso = self.circlemaskfinder.segmentAndCircApprox(dnet_orig_im1_iso.convert("L"))
        dnet_orig_im1_polar , dnet_orig_mask1_polar = self.irisRec.cartToPol_torch(dnet_orig_im1_iso, dnet_orig_mask1_iso, dnet_orig_pxyr1_iso, dnet_orig_ixyr1_iso)
        dnet_orig_im1_code = self.irisRec.extractCode(dnet_orig_im1_polar)
       
        dnet_im1_score, _ = self.irisRec.matchCodes(dnet_orig_im1_code, self.im1_code, dnet_orig_mask1_polar, self.mask1_polar)
        self.dnet_im1_alpha_score[alpha] = round(dnet_im1_score, 4)

        self.apply_conversion(alpha)

    def process_for_alpha_parallel(self, alpha) -> Tuple[bytearray, float]:
        try: 
            logging.info(f"Processing for alpha:{alpha}")
            dnet_im1_crop, dnet_im1_nocrop, dnet_im1_highlight = self.eyepreserve.deform_with_alpha(self.im1.convert('L'), self.mask1.copy(), self.back_mask1.copy(), self.pxyr1.copy(), self.ixyr1.copy(), alpha, self.device)

            w,h = self.im1.size
            w2,h2 = dnet_im1_nocrop.size
            dnet_im1_score = 0

            logging.info(f"Outgoing image isConverted: {self.converted}, has width x height at alpha:{alpha} {w2,h2}, should be {w,h}. w_pad and h_pad {self.w_pad, self.h_pad} with score {float(round(dnet_im1_score, 4))}")
            logging.info(f"Left, Top, Right, Bottom on {w2,h2}: {int(self.w_pad)}, {int(self.h_pad)}, {int((w-self.w_pad))}, {int((h-self.h_pad))}")
        
            return (self.img_to_bytes(dnet_im1_nocrop.crop((int(self.w_pad), int(self.h_pad), int((w-self.w_pad)), int((h-self.h_pad))))), float(round(dnet_im1_score, 4)))
        
        except Exception as e:
            logging.error(f"Error IN PROCESSING ALPHA {alpha}: {e}")
            return None, None
        

    def img_to_bytes(self, image):
        byte_arr = io.BytesIO()
        image.save(byte_arr, "PNG")
        return byte_arr.getvalue()
    
    def apply_conversion(self, alpha):
        self.dnet_im1_alpha_nocrop[alpha] = self.img_to_bytes(self.dnet_im1_alpha_nocrop[alpha])


class ActiveMQListener(stomp.ConnectionListener):
    def __init__(self, conn, deformer_wrapper):
        self.conn = conn
        self.deformer_wrapper = deformer_wrapper
        logging.info("Initialized ActiveMQListener")

    def on_message(self, frame):
        # Record the start time for the entire method
        start_time = time.time()

        headers = frame.headers
        message = frame.body
        logging.info(f"Received message headers: {headers}")
        logging.info(f"Raw message type: {type(message)}")
        logging.info(f"Raw message length: {len(message)}")

        try:
            # Route based on the destination header
            destination = normalize_destination(headers.get('destination'))
            if destination == DUAL_PDM_REQUEST_QUEUE:
                logging.info("Routing to process_dual_pdm...")
                self.process_dual_pdm(headers, message)
            elif destination == PDM_REQUEST_QUEUE:
                logging.info("Routing to process_single_pdm...")
                self.process_single_pdm(headers, message)  # Call the existing logic for single PDM
            else:
                logging.warning(f"Unknown destination: {destination}. Ignoring message.")
        except Exception as e:
            logging.error(f"Unexpected error from Iwp-PDM: {e}")

        # Record the end time for the entire method
        end_time = time.time()

        # Calculate the elapsed time for the entire method
        elapsed_time = end_time - start_time
        logging.info(f"Building Response time: {elapsed_time:.6f} seconds")
    
    def process_dual_pdm(self, headers, message):
        logging.info("--------------- START OF DUAL-PDM ---------------")
        logging.info(f"Received Header and Message for Dual PDM: {headers}, {len(message)}")
        
        probeId = int(headers['probeId'])
        candidateId = int(headers['candidateId'])

        logging.info(f"IDs are:{probeId}, {candidateId} and of type: {type(probeId)}, {type(candidateId)}")

        if not isinstance(message, bytes):
            raise ValueError("Unexpected message type: Expected bytes")

        # Deserialize the Protobuf message
        logging.info("Attempting to parse Protobuf message...")
        query = Pdm_pb2.DualPDMComparison()
        query.ParseFromString(message) # Deserialize the byte array into a Proto obj
        logging.info(f"Succesfully Parsed Protobuf Request Object")

        # Extract fields from the Protobuf object
        probe_bytes = query.probe
        candidate_bytes = query.candidate

        logging.info(f"Received probe and candidate bytes of length: {len(probe_bytes)}, {len(candidate_bytes)}")
        self.deformer_wrapper.load_dual_images(probe_bytes, candidate_bytes)

        # Kick off direct deformation - LIMITED to only projecting image 1 to image 2 (Does not do Alpha deformation)
        self.deformer_wrapper.process_for_direct_deform_DUAL()

        # Prepare Proto message
        dualComparisonResponse = Pdm_pb2.DualPDMComparison()
        dualComparisonResponse.probe = img_to_bytes(self.deformer_wrapper.dnet_im1_im2)
        dualComparisonResponse.candidate = img_to_bytes(self.deformer_wrapper.dnet_im2_im1)

        # Serialized object
        serialized_message = dualComparisonResponse.SerializeToString()

        # Process the message and send a response
        self.conn.send(
            destination=DUAL_PDM_RESPONSE_QUEUE,
            body=serialized_message, 
            headers={
                **ARTEMIS_ANYCAST_HEADERS,
                'content-type': 'application/octet-stream',
                'probeId': probeId,
                'candidateId': candidateId,
                })
        logging.info(f"Response sent to DUAL PDM RESPONSE queue! Done!")
        logging.info("--------------- END OF DUAL-PDM ---------------")

    def process_single_pdm(self, headers, message):
        # Ensure the message is in bytes format
        if not isinstance(message, bytes):
            raise ValueError("Unexpected message type: Expected bytes")

        # Deserialize the Protobuf message
        logging.info("Attempting to parse Protobuf message...")
        image_service_query = Iwp_pb2.ImageServiceQuery()
        image_service_query.ParseFromString(message)  # Deserialize the byte array into a Protobuf object
        logging.info(f"Succesfully Parsed Protobuf Request Object")

        # Extract fields from the Protobuf object
        image_id = image_service_query.imageId
        image_bytes = image_service_query.imageBytes
        width = image_service_query.width
        height = image_service_query.height

        logging.info(f"Received image ID: {image_id}")
        logging.info(f"Image dimensions: {width}x{height}")

        logging.info("Performing image deformation")
        self.deformer_wrapper.load_images(image_bytes)
        logging.info(f"Incoming image has width x height {self.deformer_wrapper.im1.size}")
        self.deformer_wrapper.process_for_direct_deform()

        alphas_np_array = np.linspace(0.2, 0.7, 51)
        alphas = np.round(alphas_np_array, 2).tolist()
        alphas = [0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7]

        alpha_byte_map = {}
        scores = []

        # Record the start time for the for loop
        loop_start_time = time.time()

        logging.info("BEGINING PARALLELIZATION...")
        with ThreadPoolExecutor() as executor:
            try:
                results = list(executor.map(self.deformer_wrapper.process_for_alpha_parallel, alphas))
            except Exception as e:
                logging.error(f"Error during parallelization: {e}")
                return {}, []
        logging.info(f"FINISHED PARALLELIZATION!")
        

        for alpha, (byte_arr, score) in zip(alphas, results):
            alpha_byte_map[alpha] = byte_arr
            scores.append(score)
        
        # Record the end time for the for loop
        loop_end_time = time.time()


        # Calculate the elapsed time for the for loop
        loop_elapsed_time = loop_end_time - loop_start_time
        logging.info(f"Parallelization execution time: {loop_elapsed_time:.6f} seconds")

        logging.info("Serializing into Pdm_protobuf obj")
        w, h = self.deformer_wrapper.im1.size
        serialized_pdm_data = serialize(w, h, alphas, alpha_byte_map, scores, image_id)
        logging.info(f"Response object of type {type(serialized_pdm_data)}")

        # Process the message and send a response
        self.conn.send(
            destination=PDM_RESPONSE_QUEUE,
            body=serialized_pdm_data,
            headers={
                **ARTEMIS_ANYCAST_HEADERS,
                'content-type': 'application/octet-stream',
            },
        )
        logging.info(f"Response sent to response queue! Done!")



def img_to_bytes(image):
    byte_arr = io.BytesIO()
    image.save(byte_arr, "PNG")
    return byte_arr.getvalue()

def serialize(deformed_width:int, deformed_height:int, alphas:list, mapping:dict, scores:list, image_id:str):
    dw = Pdm_pb2.DeformerWrapper()

    deformedImage = Pdm_pb2.DeformedImage()
    PNG = Pdm_pb2.DeformedImage.PNG

    # Loop through all given alphas
    for i, a in enumerate(alphas):
        deformedImage.imageData = mapping[a]
        deformedImage.score = scores[i]
        deformedImage.imageType = PNG
        deformedImage.width = deformed_width
        deformedImage.height = deformed_height

        # Set DeformedImage into Proto class
        dw.eyePreserveResponse[str(a)].CopyFrom(deformedImage)
    dw.imageId = image_id

    # Begin Serialization
    return dw.SerializeToString()


def main(args):
    # Check for CUDA device
    if args.cuda:
        if torch.cuda.is_available():
            logger.info('Running on CUDA-enabled GPU.')
            device = torch.device('cuda')
        else:
            logger.warning("CUDA specified but not available, running on CPU.")
            device = torch.device('cpu')
    else:
        logger.info('Running on CPU.')
        device = torch.device('cpu')

    deformer_wrapper = DeformerWrapper(
        [int(size) for size in args.wide_size.split('x')],
        [int(size) for size in args.square_size.split('x')],
        device
    )
    logging.info("Initialized Deformer Wrapper")

    # Connect to the ActiveMQ Artemis STOMP broker.
    broker_host = stomp_host()
    username, password = artemis_credentials()
    connection_options = {"auto_decode": False}
    heartbeats = stomp_heartbeats()
    if heartbeats:
        connection_options["heartbeats"] = heartbeats

    logging.info(
        "Connecting to STOMP broker at %s:%s as %s",
        broker_host[0],
        broker_host[1],
        username,
    )
    conn = stomp.Connection([broker_host], **connection_options)
    listener = ActiveMQListener(conn, deformer_wrapper)
    conn.set_listener('', listener)
    conn.connect(username, password, wait=True)
    conn.subscribe(destination=PDM_REQUEST_QUEUE, id=1, ack='auto', headers=ARTEMIS_ANYCAST_SUBSCRIBE_HEADERS)
    conn.subscribe(destination=DUAL_PDM_REQUEST_QUEUE, id=2, ack='auto', headers=ARTEMIS_ANYCAST_SUBSCRIBE_HEADERS)

    # Keep the application running to listen for messages
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        logger.info("Shutting down...")
        conn.disconnect()
        sys.exit(0)
    except Exception as e:
        logger.error("Unexpected error occurred:", exc_info=True)
        conn.disconnect()
        sys.exit(1)


def serialize_and_persist(alphas, deformerWrapper: DeformerWrapper, image_id:str):
    dw = Pdm_pb2.DeformerWrapper()
    deformedWidth, deformedHeight = deformerWrapper.im1.size

    # Loop through all given alphas
    for a in range(len(alphas)):
        logging.info(f"Serialzing alpha val: {alphas[a]}, type {type(alphas[a])}")
        deformedImage = Pdm_pb2.DeformedImage(
            imageData=deformerWrapper.dnet_im1_alpha_nocrop[round(alphas[a], 2)], 
            score=deformerWrapper.dnet_im1_alpha_score[round(alphas[a], 2)],
            imageType=Pdm_pb2.DeformedImage.PNG,
            width=deformedWidth,
            height=deformedHeight
            )
        # Set DeformedImage into Proto class
        dw.eyePreserveResponse[str(round(alphas[a], 2))].CopyFrom(deformedImage)
    dw.imageId = image_id

    # Begin Serialization
    serialized_data = dw.SerializeToString()
    
    return serialized_data


if __name__ == '__main__':
    parser = argparse.ArgumentParser(prog='Iris Texture Deformer',
                    description='This tool implements three different methods for iris texture deformation based on change in pupil size: \
                          i) Linear (Daugman\'s rubber sheet), ii) Biomechanical and iii) EyePreserve.')
    parser.add_argument('--cuda', action='store_true')
    parser.add_argument('--window_size', type=str, default="1280x720", help="This parameter pecifies the initial size of the application window.")
    parser.add_argument('--wide_size', type=str, default="600x450", help="This parameter specifies the resolution of uncropped/ISO iris images as it is shown on the application window.")
    parser.add_argument('--square_size', type=str, default="450x450", help="This parameter specifies the resolution of cropped iris images as it is shown on the application window.")

    args = parser.parse_args()
    main(args)
