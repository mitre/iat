/*******************************************************
* Iris image segmentation for TSHEPII
* This codes use the Open Source for Iris (OSIRIS) software
* Date : February 2018
* Authors : Daniel Moreira, Adam Czajka
********************************************************/

#include "IrisSegmenter.h"

using namespace std;
using namespace cv;
using namespace osiris;

/** Implements the iris image segmentation and normalization using OSIRIS open software. */
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
	int& maxPupilDiameter)
{
	// Local IplImage copies of Mat buffers
	IplImage iplInput = (IplImage)inputIrisImage;
	IplImage* pImage = cvCloneImage(&iplInput);
	IplImage* pMask = cvCreateImage(cvSize(inputIrisImage.cols, inputIrisImage.rows), IPL_DEPTH_8U, 1);
	IplImage* pNormImage = cvCreateImage(cvSize(outputIrisNormImage.rows, outputIrisNormImage.cols), IPL_DEPTH_8U, 1);
	IplImage* pNormMask = cvCreateImage(cvSize(outputIrisNormImage.rows, outputIrisNormImage.cols), IPL_DEPTH_8U, 1);

	// OSIRIS output arguments
	OsiCircle rPupil;
	OsiCircle rIris;
	vector<float> rThetaCoarsePupil;
	vector<float> rThetaCoarseIris;
	vector<CvPoint> rCoarsePupilContour;
	vector<CvPoint> rCoarseIrisContour;

	// Call OSIRIS segmentation
	OsiProcessings osiProc;
	osiProc.segment(
		pImage,
		pMask,
		rPupil,
		rIris,
		rThetaCoarsePupil,
		rThetaCoarseIris,
		rCoarsePupilContour,
		rCoarseIrisContour,
		minIrisDiameter,
		minPupilDiameter,
		maxIrisDiameter,
		maxPupilDiameter);

	osiProc.normalizeFromContour(
		pImage,
		pNormImage,
		rPupil,
		rIris,
		rThetaCoarsePupil,
		rThetaCoarseIris,
		rCoarsePupilContour,
		rCoarseIrisContour);

	osiProc.normalizeFromContour(
		pMask,
		pNormMask,
		rPupil,
		rIris,
		rThetaCoarsePupil,
		rThetaCoarseIris,
		rCoarsePupilContour,
		rCoarseIrisContour);

	// Set output arguments
	pupilCenter = rPupil.getCenter();
	irisCenter = rIris.getCenter();
	pupilRadius = rPupil.getRadius();
	irisRadius = rIris.getRadius();

	outputIrisMask = pMask;
	outputIrisNormImage = pNormImage;
	outputIrisNormMask = pNormMask;

	// Release local image buffer
	cvReleaseImage(&pImage);

}

