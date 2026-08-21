// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

/** @file Implementation of Iris class. */

#include "Iris.hpp"
#include "Configuration.hpp"
#include "Functions.hpp"
#include "IrisSegmenter.h"

/**
 * Creates an iris from an image in common formats (PNG, JPG, etc.)
 *
 * @param filepath The path to the image file
 */
Iris::Iris(string filePath)
{
	this->filePath = filePath;

	// configures  the Iris ID
	vector<string> *irisFilePathParts = new vector<string>();
	Functions::split(&filePath, Configuration::FILE_SEPARATOR, irisFilePathParts);

	this->id = irisFilePathParts->back();
	delete irisFilePathParts;

	// reads the iris image file
	this->originalContent = imread(filePath, IMREAD_COLOR);
}

/**
 * Creates an iris from an image in PIXMAP format
 *
 * @param filepath The path to the image file
 * @param width The width of the image in pixels
 * @param height The height of the image in pixels
 */
Iris::Iris(string filepath, int width, int height)
{

	int channels;
	FILE *fp = fopen(filepath.c_str(), "rb");
	if (!fp)
	{
		return;
	}
	// Determine the number of channels
	fseek(fp, 0, SEEK_END);
	channels = ftell(fp) / height / width;
	fseek(fp, 0, SEEK_SET);
	if (channels == 3)
	{
		this->originalContent = Mat::zeros(height, width, CV_8UC3);
		fread(this->originalContent.data, 3, width * height, fp);
	}
	else
	{
		// read in the grayscale image
		Mat imageMat = Mat::zeros(height, width, CV_8UC1);
		fread(imageMat.data, 1, width * height, fp);
		// convert this image to 3-color
		Mat temp[] = {imageMat, imageMat, imageMat};
		cv::merge(temp, 3, this->originalContent);
	}
	fclose(fp);
}

void Iris::osirisPreProcess()
{
	Mat grayScaledIris, mask, stripIris, stripIrisMask;
	int irisRadius, pupilRadius;
	CvPoint irisCenter, pupilCenter;

	cvtColor(this->originalContent, grayScaledIris, CV_BGR2GRAY);
	stripIris = Mat(Configuration::OSIRIS_NORM_IMAGE_COLS, Configuration::OSIRIS_NORM_IMAGE_ROWS, IPL_DEPTH_8U, 1);
	stripIrisMask = Mat(Configuration::OSIRIS_NORM_IMAGE_COLS, Configuration::OSIRIS_NORM_IMAGE_ROWS, IPL_DEPTH_8U, 1);

	irisSegmentation(
		grayScaledIris,
		mask,
		stripIris,
		stripIrisMask,
		pupilCenter,
		irisCenter,
		pupilRadius,
		irisRadius,
		Configuration::OSIRIS_MIN_IRIS_DIAMETER,
		Configuration::OSIRIS_MIN_PUPIL_DIAMETER,
		Configuration::OSIRIS_MAX_IRIS_DIAMETER,
		Configuration::OSIRIS_MAX_PUPIL_DIAMETER);

	this->preProcess(
		&mask, &stripIris, &stripIrisMask,
		Point2d(irisCenter.x, irisCenter.y), irisRadius,
		Point2d(pupilCenter.x, pupilCenter.y), pupilRadius);
}

void Iris::manualPreProcess(
	Mat *contentMask, Mat *stripContent, Mat *stripContentMask,
	Point2d irisCenter, double radius,
	Point2d pupilCenter, double pupilRadius)
{
	this->preProcess(contentMask, stripContent, stripContentMask, irisCenter, radius, pupilCenter, pupilRadius);
}

string Iris::getFilePath()
{
	return this->filePath;
}

string Iris::getId()
{
	return this->id;
}

Mat Iris::getOriginalContent()
{
	return this->originalContent;
}

Mat Iris::getOriginalContentMask()
{
	return this->originalContentMask;
}

Point2d Iris::getOriginalIrisCenter()
{
	return this->originalIrisCenter;
}

double Iris::getOriginalIrisRadius()
{
	return this->originalIrisRadius;
}

Point2d Iris::getOriginalPupilCenter()
{
	return this->originalPupilCenter;
}

double Iris::getOriginalPupilRadius()
{
	return this->originalPupilRadius;
}

Mat Iris::getStripContent()
{
	return this->stripContent;
}

Mat Iris::getStripContentMask()
{
	return this->stripContentMask;
}

Mat Iris::getColorCrop()
{
	return this->colorCrop;
}

Mat Iris::getGrayCrop()
{
	return this->grayCrop;
}

Mat Iris::getGray3CCrop()
{
	return this->gray3CCrop;
}

Mat Iris::getCropMask()
{
	return this->cropMask;
}

double Iris::getCropRadius()
{
	return this->cropRadius;
}

Point2d Iris::getCropCenter()
{
	return this->cropCenter;
}

double Iris::getCropPupilRadius()
{
	return this->cropPupilRadius;
}

Point2d Iris::getCropPupilCenter()
{
	return this->cropPupilCenter;
}

void Iris::preProcess(
	Mat *originalMask, Mat *originalStripContent, Mat *originalStripContentMask,
	Point2d originalCenter, double originalRadius,
	Point2d originalPupilCenter, double originalPupilRadius)
{
	// saves the original data
	this->originalContentMask = originalMask->clone();
	this->originalIrisCenter = originalCenter;
	this->originalIrisRadius = originalRadius;
	this->originalPupilCenter = originalPupilCenter;
	this->originalPupilRadius = originalPupilRadius;

	// auxiliary vesions of the iris
	Mat colorIris, grayScaledIris, grayScaled3CIris, mask;

	// initiates the color iris, its grayscaled version, and mask
	colorIris = this->originalContent.clone();
	cvtColor(colorIris, grayScaledIris, CV_BGR2GRAY);
	mask = originalMask->clone();

	// iris cropping
	int x = int(round(originalCenter.x - (originalRadius + Configuration::IRIS_RADIUS_OFFSET)));
	int y = int(round(originalCenter.y - (originalRadius + Configuration::IRIS_RADIUS_OFFSET)));
	int size = int(round((originalRadius + Configuration::IRIS_RADIUS_OFFSET) * 2.0));
	while (x < 0 || y < 0)
	{
		x++;
		y++;
		size--;
	}

	double newCenterX = originalCenter.x - x;
	double newCenterY = originalCenter.y - y;
	double newPupilCenterX = originalPupilCenter.x - x;
	double newPupilCenterY = originalPupilCenter.y - y;

	colorIris = colorIris(Rect(x, y, size, size));
	grayScaledIris = grayScaledIris(Rect(x, y, size, size));
	mask = mask(Rect(x, y, size, size));

	// iris resizing
	resize(colorIris, colorIris, Size(Configuration::IRIS_HEIGHT, Configuration::IRIS_WIDTH), 0.0, 0.0, CV_INTER_CUBIC);
	resize(grayScaledIris, grayScaledIris, Size(Configuration::IRIS_HEIGHT, Configuration::IRIS_WIDTH), 0.0, 0.0, CV_INTER_CUBIC);
	resize(mask, mask, Size(Configuration::IRIS_HEIGHT, Configuration::IRIS_WIDTH), 0.0, 0.0, CV_INTER_CUBIC);
	blur(mask, mask, Size(Configuration::IRIS_MASK_BLUR_SIZE, Configuration::IRIS_MASK_BLUR_SIZE));
	threshold(mask, mask, 0, 255, CV_THRESH_BINARY);

	// iris CLAHE normalization
	Ptr<CLAHE> normCLAHE = createCLAHE();
	normCLAHE->setTilesGridSize(Size(Configuration::IRIS_CLAHE_TILES, Configuration::IRIS_CLAHE_TILES));
	normCLAHE->setClipLimit(Configuration::IRIS_CLAHE_CLIP);
	normCLAHE->apply(grayScaledIris, grayScaledIris);
	cvtColor(grayScaledIris, grayScaled3CIris, CV_GRAY2BGR);

	// sets iris attributes
	this->colorCrop = colorIris.clone();
	this->grayCrop = grayScaledIris.clone();
	this->gray3CCrop = grayScaled3CIris.clone();
	this->cropMask = mask.clone();
	this->stripContent = originalStripContent->clone();
	this->stripContentMask = originalStripContentMask->clone();

	this->cropRadius = originalRadius * Configuration::IRIS_WIDTH / double(size);
	this->cropCenter = Point2d(
		newCenterX * Configuration::IRIS_WIDTH / double(size),
		newCenterY * Configuration::IRIS_HEIGHT / double(size));

	this->cropPupilRadius = originalPupilRadius * Configuration::IRIS_WIDTH / double(size);
	this->cropPupilCenter = Point2d(
		newPupilCenterX * Configuration::IRIS_WIDTH / double(size),
		newPupilCenterY * Configuration::IRIS_HEIGHT / double(size));
}
