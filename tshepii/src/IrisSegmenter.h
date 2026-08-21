/*******************************************************
* Iris image segmentation for TSHEPII
* This codes use the Open Source for Iris (OSIRIS) software
* Date : February 2018
* Authors : Daniel Moreira, Adam Czajka
********************************************************/

#ifndef IRIS_SEGMENTER_H
#define IRIS_SEGMENTER_H

#include <opencv2/opencv.hpp>
#include "OsiProcessings.h"

using namespace std;
using namespace cv;
using namespace osiris;

/** Implements the iris image segmentation using OSIRIS open software. */
void irisSegmentation(
	const Mat& inputIrisImage,
	Mat& outputIrisMask,
	Mat& outputIrisNormImage,
	Mat& outputIrisNormMask,
	CvPoint& pupilCenter,
	CvPoint& irisCenter,
	int& pupilRadius,
	int& irisRadius,
	int& minIrisDiameter,
	int& minPupilDiameter,
	int& maxIrisDiameter,
	int& maxPupilDiameter);

#endif