#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import numpy
import cv2
import IrisAnnotation_pb2 as IrisAnnotation
import segment.data.info as info

def getClassOptions(classOptions, key):
    options = {}

    classKeys = []
    classValues = []
    for classOption in classOptions:
        if int(classOption[0]) == key:
            classKeys.append(classOption[1])
            classValues.append(classOption[2])

    counter = 0
    for classKey in classKeys:
        options[classKey] = classValues[counter]
        counter += 1

    return options

def splitDependsOnKeys(dependsOnKeys):
    return dependsOnKeys.split('-')

def convert2DTo3D(image2D):
    if (image2D.ndim == 2):
        #labels, rows, columns
        image3D = numpy.zeros((info.num_labels, image2D.shape[0], image2D.shape[1]))

        label = 0
        while label < info.num_labels:
            row = 0
            while row < image2D.shape[0]:
                column = 0
                while column < image2D.shape[1]:
                    if (image2D[row,column] == label):
                        image3D[label,row,column] = 1
                    column += 1
                row += 1
            label += 1

        return image3D
    else:
        return None

def generateScleraOutline(scleraMask, irisMask, pupilMask):
    polygonSclera = generateGeneralOutline(scleraMask + irisMask + pupilMask)
    polygonIris = generateGeneralOutline(irisMask + pupilMask)
    polygonSclera.inline.append(polygonIris.outline)

    return polygonSclera

def generateIrisOutline(irisMask, pupilMask):
    polygonIris = generateGeneralOutline(irisMask + pupilMask)
    polygonPupil = generateGeneralOutline(pupilMask)
    polygonIris.inline.append(polygonPupil.outline)

    return polygonIris

def generatePupilOutline(pupilMask):
    polygonPupil = generateGeneralOutline(pupilMask)

    return polygonPupil

def generateGeneralOutline(imageMask):
    imageMask = (imageMask * 255.0).astype(numpy.uint8)

    contours, hierarchy = cv2.findContours(imageMask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    polygon = IrisAnnotation.Polygon()
    if contours is not None and len(contours):
        maxContour = max(contours, key = cv2.contourArea)

        for k in range(len(maxContour)):
            point = IrisAnnotation.Point()
            point.x = maxContour[k][0][0]
            point.y = maxContour[k][0][1]
            polygon.outline.points.append(point)
    
    return polygon

def savePolygonsOverMaskImage(mask, polygon, path):
    mask = (mask * 255.0).astype(numpy.uint8)
    image = cv2.cvtColor(mask, cv2.COLOR_GRAY2RGB)

    outline = polygon.outline
    for k in range(len(outline.points)):
        image[outline.points[k].y,outline.points[k].x,1] = 0
        image[outline.points[k].y,outline.points[k].x,1] = 255
        image[outline.points[k].y,outline.points[k].x,2] = 0
        if (k + 1 == len(outline.points)):
            cv2.line(image, (outline.points[k].x, outline.points[k].y), (outline.points[0].x, outline.points[0].y), (0, 255, 0), 1)
        else:
            cv2.line(image, (outline.points[k].x, outline.points[k].y), (outline.points[k+1].x, outline.points[k+1].y), (0, 255, 0), 1)

    inlineList = polygon.inline
    for j in range(len(inlineList)):
        for k in range(len(inlineList[j].points)):
            image[inlineList[j].points[k].y,inlineList[j].points[k].x,1] = 0
            image[inlineList[j].points[k].y,inlineList[j].points[k].x,1] = 255
            image[inlineList[j].points[k].y,inlineList[j].points[k].x,2] = 0
            if (k + 1 == len(inlineList[j].points)):
                cv2.line(image, (inlineList[j].points[k].x, inlineList[j].points[k].y), (inlineList[j].points[0].x, inlineList[j].points[0].y), (0, 255, 0), 1)
            else:
                cv2.line(image, (inlineList[j].points[k].x, inlineList[j].points[k].y), (inlineList[j].points[k+1].x, inlineList[j].points[k+1].y), (0, 255, 0), 1)
        
    cv2.imwrite(path, image)