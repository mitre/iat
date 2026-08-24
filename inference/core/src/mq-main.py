#!/usr/bin/env python3

#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import os
import time
import warnings
import stomp
import sys
import logging
import time
import tempfile
from Messages_pb2 import ImageServiceQuery as Query
from IrisAnnotation_pb2 import IrisAnnotationReply

from sys import exit
from config import Options
from main import SegmentAndDetect

warnings.simplefilter(action='ignore', category=FutureWarning)
warnings.filterwarnings("ignore", category=RuntimeWarning)

logging.basicConfig(format='%(asctime)-15s %(message)s', stream=sys.stdout, level=logging.INFO)
logging.getLogger("stomp").setLevel(logging.WARNING)
logging.getLogger("stomp.py").setLevel(logging.WARNING)


###########################################################################################

ANNOTATION_REQUEST_QUEUE = "iris.annotation.request"
ANNOTATION_RESPONSE_QUEUE = "iris.annotation.response"
ARTEMIS_ANYCAST_HEADERS = {"destination-type": "ANYCAST"}
ARTEMIS_ANYCAST_SUBSCRIBE_HEADERS = {"subscription-type": "ANYCAST"}

def stompHost():
    host_and_port = os.getenv('STOMP_HOST', "localhost:61613").split(':')
    host = host_and_port[0] if host_and_port[0] else 'localhost'
    port = host_and_port[1] if len(host_and_port) >= 2 else '61613'
    return (host, port)

def normalizeQueueName(queue):
    if queue and queue.startswith('/queue/'):
        return queue[len('/queue/'):]
    return queue

def username():
    username = ''
    if 'STOMP_USER' in os.environ:
        username = os.environ['STOMP_USER']
    return username

def password():
    password = ''
    if 'STOMP_PASSWORD' in os.environ:
        password = os.environ['STOMP_PASSWORD']
    return password

def stompHeartbeats():
    heartbeats = os.getenv('STOMP_HEARTBEATS', '10000,0').split(',')
    try:
        return (int(heartbeats[0]), int(heartbeats[1]))
    except (IndexError, ValueError):
        return (10000, 0)

def modelDirectory():
    return "/usr/share/models"

def tmpDirectory():
    return "/mnt/host/tmp"

###########################################################################################

def getFilesAndConfigs(cfg, query, reply, headers, conn):
    logger = logging.getLogger('iris-annotation.core.mq_main.getFilesAndConfigs')

    in_file_list, cfg.dataset_file = createTmpFiles(query, reply, headers, conn)
    if cfg.dataset_file is not None:
        # cfg.use_cuda should be False on CPU images and True on GPU images. 
        # USE_CUDA should be set in the respective Dockerfile but will default to False (CPU only) if not set 
        cfg.use_cuda = True if os.environ.get('USE_CUDA', 'False').lower() == 'true' else False

        logger.info("cfg.use_cuda '%s'" % (str(cfg.use_cuda)))
        cfg.dataset_dir_path = tmpDirectory()
        logger.info("cfg.dataset_dir_path '%s'" % (str(cfg.dataset_dir_path)))
        cfg.model_dir_path = modelDirectory()
        logger.info("cfg.model_dir_path '%s'" % (str(cfg.model_dir_path)))

        classOptions = []
        #pupil = 4
        #eyebrow =5
        #iris =1
        #eyelash=2
        #scelara=3
        classOptions.append([str(1), "segmentType", "iris"])
        classOptions.append([str(1), "segmentDependsOn", "4"])
        classOptions.append([str(4), "segmentType", "pupil"])
        classOptions.append([str(3), "segmentType", "sclera"])
        classOptions.append([str(3), "segmentDependsOn", "1-4"])
        #
        classOptions.append([str(2), "segmentType", "eyelash"])
        classOptions.append([str(5), "segmentType", "eyebrow"])

        logger.info("Class options mq-main'%s'" % (str(classOptions)))
        if len(classOptions) > 0:
            cfg.class_options = classOptions

        return in_file_list, cfg
    else:
        return None, None

def createTmpFiles(query, reply, headers, conn):
    logger = logging.getLogger('iris-annotation.core.mq_main.createTmpFiles')
    logger.info("createTmpFiles")
    
    in_file_list = []
    image_paths = [None]

    in_text_fd, in_text_file, errStr = createTmpFile(None)
    if errStr is not None:
        reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
        logger.error(errStr)

        response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
        send(reply, response_queue + "-failures")
        acknowledge(query, headers, conn)
        return None, None
    dataset_file = in_text_file
    in_file_list.append(in_text_file)

    try:
        with os.fdopen(in_text_fd, 'w') as data_file:
            for image_path in image_paths:
                in_fd, in_file, errStr = createTmpFile(tmpDirectory())
                if errStr is not None:
                    reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
                    logger.error(errStr)

                    closeTmpFile(in_text_fd)
                    removeTmpFiles(in_file_list)
                    response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
                    send(reply, response_queue + "-failures")
                    acknowledge(query, headers, conn)
                    return None, None
                in_file_list.append(in_file)
                try:
                    if image_path is not None:
                        if os.path.isfile(image_path) and os.access(image_path, os.R_OK):
                            # get the image from the Docker bind-mount based path
                            with os.fdopen(in_fd, 'wb') as image_file:
                                with open(image_path, 'rb') as host_image:
                                    image_file.write(host_image.read())
                        else:
                            errStr = "Data (Preparation) Error: The original file does not exist or is not readable!"
                            reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
                            logger.error(errStr)

                            closeTmpFile(in_fd)
                            closeTmpFile(in_text_fd)
                            removeTmpFiles(in_file_list)
                            response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
                            send(reply, response_queue + "-failures")
                            acknowledge(query, headers, conn)
                            return None, None
                    else:
                        # get the image from the raw message
                        with os.fdopen(in_fd, 'wb') as image_file:
                            logger.info("Getting Image file from query.imageBytes")
                            image_file.write(query.imageBytes)
                except Exception as e:
                    errStr = "Data (Preparation) Error: " + str(e)
                    reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
                    logger.error(errStr)

                    closeTmpFile(in_fd)
                    closeTmpFile(in_text_fd)
                    removeTmpFiles(in_file_list)
                    response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
                    send(reply, response_queue + "-failures")
                    acknowledge(query, headers, conn)
                    return None, None
                
                data_file.write(os.path.basename(in_file) + ",None\n")    
                closeTmpFile(in_fd)
    except Exception as e:
        errStr = "Data (Preparation) Error: " + str(e)
        reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
        logger.error(errStr)

        closeTmpFile(in_fd)
        closeTmpFile(in_text_fd)
        removeTmpFiles(in_file_list)
        response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
        send(reply, response_queue + "-failures")
        acknowledge(query, headers, conn)
        return None, None

    closeTmpFile(in_text_fd)
    return in_file_list, dataset_file

def createTmpFile(tmpDirectory):
    logger = logging.getLogger('iris-annotation.core.mq_main.createTmpFile')
    errStr = None
    logger.info("createTmpFile: '%s'" % (str(tmpDirectory)))
    tmpArr = []
    try:
        if tmpDirectory is not None:
            tmpArr = tempfile.mkstemp(dir=tmpDirectory)
        else:
            tmpArr = tempfile.mkstemp()

        if len(tmpArr) != 2:
            errStr = "Data (Pre-Preparation) Error: 'mkstemp()' returned a response with an unrecognized format!" 
            return None, None, errStr
    except Exception as e:
        errStr = "Data (Pre-Preparation) Error: " + str(e)
        return None, None, errStr

    return tmpArr[0], tmpArr[1], errStr

def closeTmpFile(fd):
    try:
        os.close(fd)
    except OSError:
        pass

def removeTmpFiles(fname_list):
    for fname in fname_list:
        if os.path.exists(fname):
            os.remove(fname)

###########################################################################################

class StompListenerProto(stomp.ConnectionListener):
    conn = None
    queueReceive = None

    def __init__(self, conn, queueReceive):
        self.conn = conn
        self.queueReceive = queueReceive

        stomp.ConnectionListener.__init__(self)

    def on_error(self, frame):
        headers = frame.headers
        message = frame.body

        logger = logging.getLogger('iris-annotation.core.mq_main.StompListenerProto.on_error')

        logger.error("Received an error '%s'" % (str(message)))

        query = Query()
        query.ParseFromString(message)

        reply = IrisAnnotationReply()
        if len(query.id): reply.id = query.id

        errStr = "Stomp Error: An unknown error occurred!"
        reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
        logger.error(errStr)
        
        response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
        send(reply, response_queue + "-failures")
        acknowledge(query, headers, self.conn)

    def on_message(self, frame):
        logger = logging.getLogger('iris-annotation.core.mq_main.StompListenerProto.on_message')
        headers = frame.headers
        message = frame.body

        # logger.info("Byte Message Recieved: '%s'" % (str(message)))
        logger.info("Byte Message Recieved: ")
        # logger.info("header Info: '%s'" % (str(headers)))

        try:
            
            query = Query()
            
            
            logger.info("Query Message Recieved b4: '%s'" % (str(query)))
            
            query.ParseFromString(message)
            
            if query.id:
                 logger.info("Query id: '%s'" % (str(query.id)))
            if query.path:
                 logger.info("Query path: '%s'" % (str(query.path)))
            if query.imageBytes:
                 logger.info("Query imageBytes: ")
            if query.width:
                 logger.info("Query width: '%s'" % (str(query.width)))


        except Exception as e:
            errStr = "Error with Request Message : " + str(e)
            logger.error(errStr)

        reply = IrisAnnotationReply()

        if len(query.id): reply.id = query.id

        cfgDefault = Options().parse()
        logger.info("cfgDefault: '%s'" % (str(cfgDefault)))
        files_list, cfg = getFilesAndConfigs(cfgDefault, query, reply, headers, self.conn)
        if cfg is None:
            return
            
        try:
            logger.info("Running main iris annotation program mq-main...")
            t0 = int(round(time.time() * 1000))

            segAndDet = SegmentAndDetect(cfg)
            logger.info("segDet complete")
            results = segAndDet.run()

            t1 = int(round(time.time() * 1000))
            logger.info("Main iris annotation program complete mq-main")

            reply = formatResponse(reply, results, "OK", None, 1, t1-t0)

            removeTmpFiles(files_list)
            response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
            send(reply, response_queue)
            acknowledge(query, headers, self.conn)
        except Exception as e:
            errStr = "Error (within main iris annotation program): " + str(e)
            reply = formatResponse(reply, None, "ERROR", errStr, -1, None)
            logger.error(errStr)

            removeTmpFiles(files_list)
            response_queue = normalizeQueueName(ANNOTATION_RESPONSE_QUEUE)
            send(reply, response_queue + "-failures")
            acknowledge(query, headers, self.conn)
            return

    def on_disconnected(self):
        logger = logging.getLogger('iris-annotation.core.mq_main.StompListenerProto.on_disconnected')

        logger.info("Connection to stomp host (request) %s was lost, attempting to reconnect." % (":".join(stompHost())))
        time.sleep(1)
        pullConnection(self.conn, self.queueReceive)

class StompReceiptProto(stomp.ConnectionListener):
    conn = None

    def __init__(self, conn):
        self.conn = conn
        stomp.ConnectionListener.__init__(self)
    
    def on_error(self, frame):
        headers = frame.headers
        message = frame.body

        logger = logging.getLogger('iris-annotation.core.mq_main.StompReceiptProto.on_error')

        logger.info("Received error regarding stomp host (reply) %s\n%s\nError: %s" % (":".join(stompHost()), headers, message))

    def on_receipt(self, frame):
        headers = frame.headers
        message = frame.body

        logger = logging.getLogger('iris-annotation.core.mq_main.StompReceiptProto.on_receipt')

        logger.info("Received receipt from stomp host (reply) %s\n%s" % (":".join(stompHost()), headers))

    def on_disconnected(self):
        logger = logging.getLogger('iris-annotation.core.mq_main.StompReceiptProto.on_disconnected')

        logger.info("Connection to stomp host (reply) %s was lost, attempting to reconnect." % (":".join(stompHost())))
        time.sleep(1)
        pushConnection(self.conn)

class StompDisconnectProto(stomp.ConnectionListener):

    def __init__(self):
        stomp.ConnectionListener.__init__(self)

    def on_error(self, frame):
        headers = frame.headers
        message = frame.body

        logger = logging.getLogger('iris-annotation.core.mq_main.StompDisconnectProto.on_error')

        logger.info("Received error while disconnecting from stomp host %s\n%s\nError: %s" % (":".join(stompHost()), headers, message))

    def on_receipt(self, frame):
        headers = frame.headers
        message = frame.body

        logger = logging.getLogger('iris-annotation.core.mq_main.StompDisconnectProto.on_receipt')

        logger.info("Received receipt for disconnecting from stomp host %s\n%s" % (":".join(stompHost()), headers))

###########################################################################################

def pushConnection(conn):
    conn.connect(username(), password(), wait=True)

def pullConnection(conn, queueReceive):
    logger = logging.getLogger('iris-annotation.core.mq_main.pullConnection')

    conn.connect(username(), password(), wait=True)
    logger.info("Subscribing to %s" % (str(queueReceive)))
    conn.subscribe(
        destination=queueReceive,
        id=1,
        ack='client-individual',
        headers={**ARTEMIS_ANYCAST_SUBSCRIBE_HEADERS, 'activemq.prefetchSize': '1'})

def formatResponse(obj, results, response, message, code, time):
    if results is not None: obj.results.extend(results)
    if response is not None: obj.response = str(response)
    if message is not None: obj.responseMessage = str(message)
    if code is not None: obj.responseCode = str(code)
    if time is not None: obj.responseTimeMS = str(time)

    return obj

def send(query, queue):
    logger = logging.getLogger('iris-annotation.core.mq_main.send')

    receiptStr = query.id + '_reply-receipt'
    wReceipt = stomp.WaitingListener(receiptStr)

    conn = stomp.Connection([stompHost()], heartbeats=stompHeartbeats())
    conn.set_listener('receiptProto', StompReceiptProto(conn))
    conn.set_listener('wReceiptListener', wReceipt)
    pushConnection(conn)

    logger.info("Sending reply to %s" % (str(queue)))
    conn.send(
        body=query.SerializeToString(),
        destination=queue,
        headers={**ARTEMIS_ANYCAST_HEADERS, 'persistent': 'true', 'receipt': receiptStr})
    wReceipt.wait_on_receipt()
    conn.remove_listener('receiptProto')
    conn.remove_listener('wReceiptListener')
    conn.set_listener('', StompDisconnectProto())

    logger.info("Disconnecting from stomp host (reply) %s" % (":".join(stompHost())))
    disconnectStr = query.id + "_reply-disconnect"
    wDisconnect = stomp.WaitingListener(disconnectStr)
    conn.set_listener('wDisconnectListener', wDisconnect)

    conn.disconnect(receipt=disconnectStr)
    wDisconnect.wait_on_disconnected()

def acknowledge(query, headers, conn):
    if 'message-id' in headers and 'subscription' in headers:
        conn.ack(headers['message-id'], headers['subscription'])

def destroyPullConnection(conn):
    logger = logging.getLogger('iris-annotation.core.mq_main.destroyPullConnection')

    conn.remove_listener('listenerProto')
    conn.set_listener('', StompDisconnectProto())

    logger.info("Disconnecting from stomp host (receive) %s" % (":".join(stompHost())))
    disconnectStr = "stomp_receive-disconnect"
    wDisconnect = stomp.WaitingListener(disconnectStr)
    conn.set_listener('wDisconnectListener', wDisconnect)

    conn.disconnect(receipt=disconnectStr)
    wDisconnect.wait_on_disconnected()

###########################################################################################

def main():
    logger = logging.getLogger('iris-annotation.core.mq_main.main')

    request_queue = normalizeQueueName(ANNOTATION_REQUEST_QUEUE)
    logger.info("Request queue: %s" % (str(request_queue)))
    if not request_queue:
        logger.error("The protobuf request queue was not specified, exiting.!")
        exit(-1)

    logger.info("Connecting to stomp host (receive) " + ":".join(stompHost()))
    conn = stomp.Connection([stompHost()], auto_decode=False, heartbeats=stompHeartbeats())
    conn.set_listener('listenerProto', StompListenerProto(conn, request_queue))
    pullConnection(conn, request_queue)

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass
    finally:
        if request_queue:
            destroyPullConnection(conn)

###########################################################################################

if __name__ == '__main__':
    main()
