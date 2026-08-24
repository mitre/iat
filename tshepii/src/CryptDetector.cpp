// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

/** @file Implementation of the CryptDetector class. */

#include "CryptDetector.hpp"
#include "Configuration.hpp"
#include "Iris.hpp"
#include "Functions.hpp"

using org::mitre::iwp::buffers::TShepiiResponse;
using std::set;

/* MATLAB inspired functions. */
/** Implements the MATLAB "imreconstruct" function. */
void imreconstruct(Mat *marker, Mat *mask, Mat *output)
{
	Mat dilateKernel = getStructuringElement(MORPH_RECT, Size(3, 3));

	Mat m1 = marker->clone();
	while (true)
	{
		Mat m2;
		dilate(m1, m2, dilateKernel);
		min(m2, *mask, m2);

		if (countNonZero(m1 != m2) > 0)
			m1 = m2.clone();
		else
		{
			m2.copyTo(*output);
			break;
		}
	}
}

/** Implements the MATLAB "imfill" function, for the particular case of masks. */
void maskfill(Mat *mask, Mat *output)
{
	Mat floodedImg = mask->clone();
	threshold(floodedImg, floodedImg, 0, 255, THRESH_BINARY_INV);
	floodFill(floodedImg, Point(0, 0), Scalar(0));

	bitwise_or(floodedImg, *mask, *output);
}

/** Implements the MATLAB "bwconncomp" function. */
void bwconncomp(Mat *inputImage, double minArea, vector<vector<Point>> *components)
{
	Mat *canny = new Mat();
	Canny(*inputImage, *canny, 1, 1);
	morphologyEx(*canny, *canny, MORPH_CLOSE, getStructuringElement(MORPH_RECT, Size(3, 3)));

	vector<vector<Point>> regions;
	vector<Vec4i> *regionHierarchy = new vector<Vec4i>();
	findContours(*canny, regions, *regionHierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
	delete regionHierarchy;
	delete canny;

	for (int i = 0; i < regions.size(); i++)
	{
		double area = contourArea(regions.at(i));
		if (area >= minArea)
			components->push_back(regions.at(i));
	}
}
/** Implements the MATLAB "bwareaopen" function. */
void bwareaopen(Mat *inputImage, double minArea, Mat *output)
{
	vector<vector<Point>> regions;
	bwconncomp(inputImage, minArea, &regions);

	*output = Mat::zeros(inputImage->size(), CV_8UC1);
	for (int i = 0; i < regions.size(); i++)
		drawContours(*output, regions, i, Scalar(255, 255, 255), -1);
}

/** Restores the occlusion mask.
 *  Based on Dr. Feng Shen's MATLAB implementation. */
void restoreOcclusionMask(Mat *inputImage, Mat *occlusionMask, Mat *output)
{
	Mat inputMaskComplement = Scalar::all(255) - *occlusionMask;

	*output = Mat::zeros(inputImage->size(), inputImage->type());
	inputImage->copyTo(*output, inputMaskComplement);
}

/** Implements the proper morpohological operation.
 *  Based on Mr. Jianxu Chen's MATLAB implementation. */
void morphOperator(Mat *inputImage, Mat *structuringElement, Mat *occlusionMask, Mat *output)
{
	// image dilate and complement
	Mat *aux1 = new Mat();
	dilate(*inputImage, *aux1, *structuringElement);

	Mat *aux2 = new Mat();
	restoreOcclusionMask(aux1, occlusionMask, aux2);
	delete aux1;

	// reconstruct
	Mat *markerImage = new Mat(Scalar::all(255) - *aux2);
	Mat *maskImage = new Mat(Scalar::all(255) - *inputImage);
	delete aux2;

	Mat *rctR = new Mat();
	imreconstruct(markerImage, maskImage, rctR);
	delete markerImage;
	delete maskImage;

	Mat *rctRComp = new Mat(Scalar::all(255) - *rctR);
	delete rctR;

	Mat *mm = new Mat(*rctRComp - *inputImage);
	delete rctRComp;

	Mat r;
	threshold(*mm, r, 0, 255, THRESH_BINARY);
	r.convertTo(r, CV_8UC1);
	delete mm;

	// remove masked areas
	restoreOcclusionMask(&r, occlusionMask, output);
}

/** Implements the hierarchical detection process of iris crypts.
 *  Returns a mask containing the crypts.
 *  Based on Mr. Jianxu Chen's MATLAB implementation. */
void hierarchicalSegmentation(Mat *backgroundSubtracted, Mat *inputImage, Mat *occlusionMask, Mat *prevLevelMask, int strElSize,
							  double minArea, double maxAreaLowerBound, double maxAreaUpperBound, double cutStd,
							  Mat *output)
{
	Mat om = occlusionMask->clone();
	Mat raw = inputImage->clone();

	if (raw.channels() == 3)
	{
		om = Mat::zeros(raw.size(), CV_8UC1);
		cvtColor(raw, raw, CV_BGR2GRAY);
	}

	/*********************************/
	/* segmentation at current level */
	/*********************************/
	// morphological reconstruction using the scale of current level
	Mat *li = new Mat();
	Mat *strEl = new Mat(getStructuringElement(MORPH_ELLIPSE, Size(strElSize, strElSize)));
	morphOperator(&raw, strEl, &om, li);
	delete strEl;

	// apply the mask of previous level
	Mat *maskedLi = new Mat();
	li->copyTo(*maskedLi, *prevLevelMask);
	delete li;

	// clear boundary
	maskedLi->row(0) = Mat::zeros(1, maskedLi->cols, maskedLi->type());
	maskedLi->row(maskedLi->rows - 1) = Mat::zeros(1, maskedLi->cols, maskedLi->type());
	maskedLi->col(0) = Mat::zeros(maskedLi->rows, 1, maskedLi->type());
	maskedLi->col(maskedLi->cols - 1) = Mat::zeros(maskedLi->rows, 1, maskedLi->type());

	// break weak connection
	Mat *r = new Mat();
	morphologyEx(*maskedLi, *r, MORPH_OPEN, getStructuringElement(MORPH_ELLIPSE, Size(3, 3)));
	delete maskedLi;

	// remove small regions
	Mat *fea = new Mat();
	bwareaopen(r, minArea, fea);
	delete r;

	// fill holes
	Mat filledFea;
	maskfill(fea, &filledFea);
	delete fea;

	/*********************/
	/* iterative process */
	/*********************/
	Mat iterRegion = Mat::zeros(raw.size(), CV_8UC1);
	bool treatNextLevel = false;

	vector<vector<Point>> *components = new vector<vector<Point>>();
	bwconncomp(&filledFea, minArea, components);

	for (int i = 0; i < components->size(); i++)
	{
		double area = contourArea(components->at(i));
		if (area > maxAreaLowerBound)
		{
			if (area > maxAreaUpperBound)
			{
				drawContours(filledFea, *components, i, Scalar(0, 0, 0), -1);
				drawContours(iterRegion, *components, i, Scalar(255, 255, 255), -1);
				treatNextLevel = true;
			}

			else
			{
				Mat *auxMask = new Mat(Mat::zeros(backgroundSubtracted->size(), CV_8UC1));
				fillConvexPoly(*auxMask, components->at(i), Scalar(255, 255, 255));

				Mat *maskedBackgroundSubtracted = new Mat();
				backgroundSubtracted->copyTo(*maskedBackgroundSubtracted, *auxMask);
				delete auxMask;

				Scalar mean, stdev;
				meanStdDev(*maskedBackgroundSubtracted, mean, stdev);

				delete maskedBackgroundSubtracted;

				if (stdev[0] > cutStd)
				{
					drawContours(filledFea, *components, i, Scalar(0, 0, 0), -1);
					drawContours(iterRegion, *components, i, Scalar(255, 255, 255), -1);

					treatNextLevel = true;
				}
			}
		}
	}
	delete components;

	// need to perform segmentation on the next level
	if (treatNextLevel)
	{
		if (strElSize >= Configuration::CRYPTS_MIN_ITERATIVE_STR_EL_SIZE)
		{
			Mat nextLevelMask;
			hierarchicalSegmentation(backgroundSubtracted, &raw, &om, &iterRegion,
									 strElSize - 2, minArea, maxAreaLowerBound, maxAreaUpperBound, cutStd, &nextLevelMask);

			Mat result = Mat::zeros(filledFea.size(), CV_16UC1);
			result = filledFea + nextLevelMask;

			threshold(result, *output, 0, 255, CV_8UC1);
		}
		else
		{
			Mat result = Mat::zeros(filledFea.size(), CV_16UC1);
			result = filledFea + iterRegion;

			threshold(result, *output, 0, 255, CV_8UC1);
		}
	}

	else
		threshold(filledFea, *output, 0, 255, CV_8UC1);
}

/* Actual CryptDetector class implementation. */
CryptDetector::CryptDetector(Mat *inputIris, Mat *inputIrisMask, Mat *outputIris, Mat *originalIris,
							 double irisRadius, double irisX, double irisY,
							 double pupilRadius, double pupilX, double pupilY,
							 Scalar color)
{
	this->inputIris = inputIris;
	this->inputIrisMask = inputIrisMask;
	this->outputIris = outputIris;
	this->originalIris = originalIris;

	this->irisRadius = irisRadius;
	this->pupilRadius = pupilRadius;
	this->irisX = irisX;
	this->irisY = irisY;
	this->pupilX = pupilX;
	this->pupilY = pupilY;
	this->color = color;

	this->keypointRenderCount = 0;
	this->keypointToMissingRenderCount = 0;
	this->missingKeypointRenderCount = 0;

	this->selectedKeypointId = -1;
	this->selectedKeypointGrp = -1;
}

void CryptDetector::getKeypointIdAt(int x, int y, int *id, int *grp, double *distance)
{
	*id = -1;
	*grp = -1;
	*distance = -1.0;

	double minDistance = DBL_MAX;

	int kpCount = 0;
	for (int i = 0; i < this->matchedKeypointIndices.size() && kpCount < this->keypointRenderCount; i++)
		if (this->removedMatchedKeypointIndices.find(i) == this->removedMatchedKeypointIndices.end())
		{
			kpCount++;

			KeyPoint currentKp = this->sortedKeypoints.at(this->matchedKeypointIndices.at(i));
			double currentDistance = sqrt(pow(currentKp.pt.x - x, 2) + pow(currentKp.pt.y - y, 2));

			if (currentDistance <= currentKp.size / 2.0 && currentDistance < minDistance)
			{
				*id = i;
				*grp = 0;
				*distance = currentDistance;
				minDistance = currentDistance;
			}
		}

	for (int i = 0; i < this->matchedToMissingKeypointIndices.size() && i < this->keypointToMissingRenderCount; i++)
		if (this->removedMatchedToMissingKeypointIndices.find(i) == this->removedMatchedToMissingKeypointIndices.end())
		{
			KeyPoint currentKp = this->sortedKeypoints.at(this->matchedToMissingKeypointIndices.at(i));
			double currentDistance = sqrt(pow(currentKp.pt.x - x, 2) + pow(currentKp.pt.y - y, 2));

			if (currentDistance <= currentKp.size / 2.0 && currentDistance < minDistance)
			{
				*id = i;
				*grp = 1;
				*distance = currentDistance;
				minDistance = currentDistance;
			}
		}

	for (int i = 0; i < this->missingKeypointIndices.size() && i < this->missingKeypointRenderCount; i++)
		if (this->removedMissingKeypointIndices.find(i) == this->removedMissingKeypointIndices.end())
		{
			KeyPoint currentKp = this->missingKeypoints.at(this->missingKeypointIndices.at(i));
			double currentDistance = sqrt(pow(currentKp.pt.x - x, 2) + pow(currentKp.pt.y - y, 2));

			if (currentDistance <= currentKp.size / 2.0 && currentDistance < minDistance)
			{
				*id = i;
				*grp = 2;
				*distance = currentDistance;
				minDistance = currentDistance;
			}
		}
}

void CryptDetector::selectKeypoint(int id, int grp)
{
	this->selectedKeypointId = id;
	this->selectedKeypointGrp = grp;
}

void CryptDetector::resetSelection()
{
	this->selectedKeypointId = -1;
	this->selectedKeypointGrp = -1;
}

void CryptDetector::removeKeypoint(int id, int grp)
{
	if (grp == 0)
	{
		this->removedMatchedKeypointIndices.insert(id);

		array<int, 2> removed = {id, grp};
		this->removalHistory.push_back(removed);
	}

	else if (grp == 1)
	{
		this->removedMatchedToMissingKeypointIndices.insert(id);

		array<int, 2> removed = {id, grp};
		this->removalHistory.push_back(removed);
	}

	else if (grp == 2)
	{
		this->removedMissingKeypointIndices.insert(id);

		array<int, 2> removed = {id, grp};
		this->removalHistory.push_back(removed);
	}
}

void CryptDetector::undoLastRemoval()
{
	if (!this->removalHistory.empty())
	{
		array<int, 2> lastRemoved = this->removalHistory.back();
		this->removalHistory.pop_back();

		if (lastRemoved[1] == 0)
			this->removedMatchedKeypointIndices.erase(lastRemoved[0]);
		else if (lastRemoved[1] == 1)
			this->removedMatchedToMissingKeypointIndices.erase(lastRemoved[0]);
		else if (lastRemoved[1] == 2)
			this->removedMissingKeypointIndices.erase(lastRemoved[0]);
	}
}

void CryptDetector::getPolarCoordinates(double x, double y, double *r, double *a)
{
	// Consider the pupil center as the pole
	x = x - this->pupilX;
	y = y - this->pupilY;

	double angle = atan2(y, x);
	if (angle < 0.0)
		angle = angle + 6.2831853; // two pi

	double irisX = this->irisX - this->pupilX;
	double irisY = this->irisY - this->pupilY;

	double irisPosition = this->irisRadius - sqrt(pow(x - irisX, 2) + pow(y - irisY, 2));
	double pupilPosition = sqrt(pow(x, 2) + pow(y, 2)) - this->pupilRadius;
	double rPosition = pupilPosition / (pupilPosition + irisPosition);

	*r = rPosition;
	*a = angle;
}

void CryptDetector::getCarthesianCoordinates(double r, double a, double *x, double *y)
{
	double hyp = (this->irisRadius - this->pupilRadius) * r + this->pupilRadius;

	*x = cos(a) * hyp + this->pupilX;
	*y = sin(a) * hyp + this->pupilY;
}

void CryptDetector::addMatchedKeypointIndex(int index)
{
	this->matchedKeypointIndices.push_back(index);
}

void CryptDetector::addMatchedToMissingKeypointIndex(int index)
{
	this->matchedToMissingKeypointIndices.push_back(index);
}

void CryptDetector::addMissingKeypointIndex(int index)
{
	this->missingKeypointIndices.push_back(index);
}

void CryptDetector::setKeypointRenderCount(int keypointRenderCount)
{
	this->keypointRenderCount = keypointRenderCount;
}

void CryptDetector::setKeypointToMissingRenderCount(int keypointToMissingRenderCount)
{
	this->keypointToMissingRenderCount = keypointToMissingRenderCount;
}

void CryptDetector::setMissingKeypointRenderCount(int missingKeypointRenderCount)
{
	this->missingKeypointRenderCount = missingKeypointRenderCount;
}

vector<KeyPoint> *CryptDetector::getSortedKeypoints()
{
	return &(this->sortedKeypoints);
}

vector<KeyPoint> *CryptDetector::getMissingKeypoints()
{
	return &(this->missingKeypoints);
}

Scalar CryptDetector::getColor()
{
	return this->color;
}

set<int> CryptDetector::getRemovedMatchedKeypointIndices()
{
	return this->removedMatchedKeypointIndices;
}

set<int> CryptDetector::getRemovedMatchedToMissingKeypointIndices()
{
	return this->removedMatchedToMissingKeypointIndices;
}

set<int> CryptDetector::getRemovedMissingKeypointIndices()
{
	return this->removedMissingKeypointIndices;
}

void CryptDetector::getConnectablePoints(list<Point> *points, bool missingKeypointFirst)
{
	int kpCount = 0;

	kpCount = this->keypointRenderCount;
	for (int i = 0; i < this->matchedKeypointIndices.size() && kpCount > 0; i++)
		if (this->removedMatchedKeypointIndices.find(i) == this->removedMatchedKeypointIndices.end())
		{
			points->push_back(this->sortedKeypoints.at(this->matchedKeypointIndices.at(i)).pt);
			kpCount--;
		}

	if (!missingKeypointFirst)
	{
		kpCount = this->keypointToMissingRenderCount;
		for (int i = 0; i < this->matchedToMissingKeypointIndices.size() && kpCount > 0; i++)
			if (this->removedMatchedToMissingKeypointIndices.find(i) == this->removedMatchedToMissingKeypointIndices.end())
			{
				points->push_back(this->sortedKeypoints.at(this->matchedToMissingKeypointIndices.at(i)).pt);
				kpCount--;
			}

		kpCount = this->missingKeypointRenderCount;
		for (int i = 0; i < this->missingKeypointIndices.size() && kpCount > 0; i++)
			if (this->removedMissingKeypointIndices.find(i) == this->removedMissingKeypointIndices.end())
			{
				points->push_back(this->missingKeypoints.at(this->missingKeypointIndices.at(i)).pt);
				kpCount--;
			}
	}

	else
	{
		kpCount = this->missingKeypointRenderCount;
		for (int i = 0; i < this->missingKeypointIndices.size() && kpCount > 0; i++)
			if (this->removedMissingKeypointIndices.find(i) == this->removedMissingKeypointIndices.end())
			{
				points->push_back(this->missingKeypoints.at(this->missingKeypointIndices.at(i)).pt);
				kpCount--;
			}

		kpCount = this->keypointToMissingRenderCount;
		for (int i = 0; i < this->matchedToMissingKeypointIndices.size() && kpCount > 0; i++)
			if (this->removedMatchedToMissingKeypointIndices.find(i) == this->removedMatchedToMissingKeypointIndices.end())
			{
				points->push_back(this->sortedKeypoints.at(this->matchedToMissingKeypointIndices.at(i)).pt);
				kpCount--;
			}
	}
}

int CryptDetector::getOutputArea()
{
	return this->inputIris->rows * this->inputIris->cols;
}

void CryptDetector::selectAndSortKeypoints(vector<KeyPoint> *sourceKeypoints, vector<KeyPoint> *selectedKeypoints)
{
	sort(sourceKeypoints->begin(), sourceKeypoints->end(), Functions::compareKeypoints);

	int i;
	for (i = 0; i < sourceKeypoints->size(); i++)
		if (sourceKeypoints->at(i).size > Configuration::KP_MIN_SIZE && sourceKeypoints->at(i).size < Configuration::KP_MAX_SIZE)
		{
			selectedKeypoints->push_back(sourceKeypoints->at(i));
			break;
		}

	if (!selectedKeypoints->empty())
	{
		Mat *positions = new Mat(Mat::zeros(1, 2, CV_32FC1));
		positions->at<float>(0, 0) = selectedKeypoints->front().pt.x;
		positions->at<float>(0, 1) = selectedKeypoints->front().pt.y;

		BFMatcher *matcher = new BFMatcher(NORM_L2);

		for (int j = i; j < sourceKeypoints->size(); j++)
			if (sourceKeypoints->at(j).size > Configuration::KP_MIN_SIZE && sourceKeypoints->at(j).size < Configuration::KP_MAX_SIZE)
			{
				Mat currentPosition = Mat::zeros(1, 2, CV_32FC1);
				currentPosition.at<float>(0, 0) = sourceKeypoints->at(j).pt.x;
				currentPosition.at<float>(0, 1) = sourceKeypoints->at(j).pt.y;

				vector<DMatch> match;
				matcher->match(currentPosition, *positions, match);

				double rate = match.front().distance / ((sourceKeypoints->at(i).size + selectedKeypoints->at(match.front().trainIdx).size) / 2.0);
				if (rate > Configuration::KP_COINCIDENCE_TOLERANCE)
				{
					selectedKeypoints->push_back(sourceKeypoints->at(j));
					positions->push_back(currentPosition);
				}
			}

		delete matcher;
		delete positions;
	}
}

void CryptDetector::detectCrypts()
{
	// holds the detected crypts
	vector<vector<Point>> *detectedCrypts = new vector<vector<Point>>();

	// background removal
	Mat *backgroundSubtracted = new Mat();
	this->originalIris->copyTo(*backgroundSubtracted, *this->inputIrisMask);

	// smooth
	Mat *iris = new Mat(Mat::zeros(this->originalIris->size(), CV_64FC3));
	GaussianBlur(*backgroundSubtracted, *iris, Size(Configuration::CRYPTS_SMOOTH_GAUSS_SIZE, Configuration::CRYPTS_SMOOTH_GAUSS_SIZE), Configuration::CRYPTS_SMOOTH_GAUSS_SIGMA);
	normalize(*iris, *iris, 0.0, 255.0, CV_MINMAX);
	iris->convertTo(*iris, CV_8UC3);

	cvtColor(*backgroundSubtracted, *backgroundSubtracted, CV_BGR2GRAY);

	// hierarchical segmentation
	Mat *cryptMask = new Mat();
	Mat *occlusionMask = new Mat(this->inputIrisMask->clone());
	hierarchicalSegmentation(backgroundSubtracted, iris, occlusionMask, this->inputIrisMask,
							 Configuration::CRYPTS_INITIAL_ITERATIVE_STR_EL_SIZE,
							 Configuration::CRYPTS_MIN_CRYPT_AREA,
							 Configuration::CRYPTS_MAX_CRYP_AREA_LB,
							 Configuration::CRYPTS_MIN_CRYP_AREA_UB,
							 Configuration::CRYPTS_CUT_STD,
							 cryptMask);
	delete backgroundSubtracted;
	delete iris;
	delete occlusionMask;

	Mat output;
	cryptMask->copyTo(output, *this->inputIrisMask);
	delete cryptMask;

	// break weak connections
	morphologyEx(output, output, MORPH_OPEN, getStructuringElement(MORPH_ELLIPSE, Size(3, 3)));

	// reject small regions
	bwconncomp(&output, Configuration::CRYPTS_MIN_CRYPT_AREA, detectedCrypts);

	// converts the obtained crypts to keypoints
	vector<KeyPoint> *regionKeypoints = new vector<KeyPoint>();
	vector<KeyPoint> *sortedRegionKeypoints = new vector<KeyPoint>();
	for (vector<vector<Point>>::iterator it = detectedCrypts->begin(); it != detectedCrypts->end(); ++it)
	{
		Point2f kpCenter;
		float kpRadius;
		minEnclosingCircle(*it, kpCenter, kpRadius);
		regionKeypoints->push_back(KeyPoint(kpCenter, 2 * kpRadius, 0.0, kpRadius));
		sortedRegionKeypoints->push_back(KeyPoint(kpCenter, 2 * kpRadius, 0.0, float(contourArea(*it))));
	}
	this->selectAndSortKeypoints(sortedRegionKeypoints, &(this->sortedKeypoints));
	delete sortedRegionKeypoints;

	for (vector<KeyPoint>::iterator it1 = this->sortedKeypoints.begin(); it1 != this->sortedKeypoints.end(); ++it1)
		for (int i = 0; i < regionKeypoints->size(); i++)
			if (it1->pt == regionKeypoints->at(i).pt)
			{
				this->crypts.push_back(detectedCrypts->at(i));
				break;
			}

	delete regionKeypoints;
	delete detectedCrypts;
}
