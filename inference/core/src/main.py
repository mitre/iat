#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import sys
import torch
import segment.data.info as info
import IrisAnnotation_pb2 as IrisAnnotation
import logging
logging.basicConfig(format='%(asctime)-15s %(message)s', stream=sys.stdout, level=logging.INFO)

from segment.dataset import *
from segment.newNetwork import UNet
from utility import getClassOptions, splitDependsOnKeys, convert2DTo3D, generateScleraOutline, generateIrisOutline, generatePupilOutline, generateGeneralOutline, savePolygonsOverMaskImage

class SegmentAndDetect():
    def __init__(self, cfg):
        logger = logging.getLogger('iris-annotation.core.main.SegmentAndDetect')
        # Model
        self.cfg = cfg
        self.model = UNet(n_classes=info.num_labels)
        if self.cfg.use_cuda:
            self.model = self.model.cuda()
        logger.info("loading model: '%s'" % (str(cfg.model_dir_path)))
        self.model.load_checkpoint(cfg.model_dir_path, self.cfg.use_cuda)
        logger.info("create_dataloader")
        
        self.loader = create_dataloader(self.cfg.dataset_dir_path, self.cfg.batch_size, self.cfg.dataset_file)
        logger.info("evaluate")
        self.model = self.model.eval() #needed for the ASPP to run a batch size of 1

    def run(self):
        logger = logging.getLogger('iris-annotation.core.main.run')
        output_data = []

        for i, (fnames, images) in enumerate(self.loader):
            logger.info("main cuda '%s'" % (str(self.cfg.use_cuda)))
            if self.cfg.use_cuda:
                images = images.cuda()

            # Part 1 - Semantic Segmentation #
            try:
            # https://stackoverflow.com/questions/56557151/how-to-retrieve-with-and-height-of-an-image-tensor-returned-by-tf-image-decode-j
                    
                # get width and height
                width = images.shape[3]
                height = images.shape[2]
                    
                # display width and height
                print("The height of the image is: ", height)
                print("The width of the image is: ", width)

                # the segmentation code works on images larger than 640x480 but can be very 
                # memory intensive, which docker env with limited resources can not support 
                if width and height < 900: 
                    probs = self.model(images)
                    labels = torch.argmax(probs, dim=1)
                else:
                    errStr = "Image size error"
                    raise ValueError(errStr)

            except ValueError as e:
                errStr=(" error was "+ str(type(e))+str(e))
                logger.error(errStr)
                if 'extra_info' in dir(e):
                    # print(e.extra_info)
                    errStr = (e.extra_info)
                    logger.error(errStr)
            for j in range(len(labels)):
                image2D = labels[j].cpu().numpy()
                image3D = convert2DTo3D(image2D)

                # Part 2 - Instance Segmentation #
                logger.info("Instance Segmentation ")
                for k in range(len(image3D)):
                    options = getClassOptions(self.cfg.class_options, k)

                    if "segmentType" in options:
                        if options["segmentType"] == "sclera" and "segmentDependsOn" in options and len(splitDependsOnKeys(options["segmentDependsOn"])) == 2:
                            dependsOnKeys = splitDependsOnKeys(options["segmentDependsOn"])

                            print("-----Performing Sclera Extraction for Class " + str(k) + " (Depends On Iris [Class " + dependsOnKeys[0] + "], Pupil [Class " + dependsOnKeys[1] + "])-----")
                            polygon = generateScleraOutline(image3D[k], image3D[int(dependsOnKeys[0])], image3D[int(dependsOnKeys[1])])
                        elif options["segmentType"] == "iris" and "segmentDependsOn" in options and len(splitDependsOnKeys(options["segmentDependsOn"])) == 1:
                            dependsOnKeys = splitDependsOnKeys(options["segmentDependsOn"])

                            print("-----Performing Iris Extraction for Class " + str(k) + " (Depends On Pupil [Class " + dependsOnKeys[0] + "])-----")
                            polygon = generateIrisOutline(image3D[k], image3D[int(dependsOnKeys[0])])
                        elif options["segmentType"] == "pupil":
                            print("-----Performing Pupil Extraction for Class " + str(k) + "-----")
                            polygon = generatePupilOutline(image3D[k])
                            ##
                        elif options["segmentType"] == "eyelash":
                            print("-----Performing eyelash Extraction for Class " + str(k) + "-----")
                            polygon = generateGeneralOutline(image3D[k])
                        elif options["segmentType"] == "eyebrow":
                            print("-----Performing eyebrow Extraction for Class " + str(k) + "-----")
                            polygon = generateGeneralOutline(image3D[k])       
                            ##
                        elif options["segmentType"] == "highlight":
                            print("-----Performing highlight Extraction for Class " + str(k) + "-----")
                            polygon = generateGeneralOutline(image3D[k])       
                            ##
                        elif options["segmentType"] == "border":
                            print("-----Performing border Extraction for Class " + str(k) + "-----")
                            polygon = generateGeneralOutline(image3D[k])       
                            ##
                        else:
                            print("-----Performing General Extraction for Class " + str(k) + "-----")
                            polygon = generateGeneralOutline(image3D[k])
                    else:
                        print("-----Performing General Extraction for Class " + str(k) + "-----")
                        polygon = generateGeneralOutline(image3D[k])
                    
                    annotation = IrisAnnotation.Annotation()
                    annotation.id = str(k)
                    annotation.colorHex = info.color_hex_map[k]
                    annotation.polygons.append(polygon)

                    output_data.append(annotation)

        return output_data