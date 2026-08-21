// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

/** @file Implementation of CryptMatcher class. */

#include <thread>
#include <math.h>
#include "CryptMatcher.hpp"
#include "Configuration.hpp"
#include "Functions.hpp"

#ifndef M_PI
#define M_PI 3.14159265358979323846264338327950288 /* pi  */
#endif

using org::mitre::iwp::buffers::Crypt;
using org::mitre::iwp::buffers::TCircle;
using org::mitre::iwp::buffers::TPoint;
using org::mitre::iwp::buffers::TShepiiResponse;

CryptMatcher::CryptMatcher(TShepiiResponse *iris1, TShepiiResponse *iris2) : CryptMatcher(iris1, iris2, cv::NORM_L1) {}

CryptMatcher::CryptMatcher(TShepiiResponse *iris1, TShepiiResponse *iris2, int distanceType)
{
	this->iris1 = iris1;
	this->iris2 = iris2;
	this->distanceType = distanceType;
}

/** Destructor. */
CryptMatcher::~CryptMatcher()
{
	this->iris1 = NULL;
	this->iris2 = NULL;
}

size_t CryptMatcher::getMatchCount()
{
	return this->soundMatches.size();
}

size_t CryptMatcher::getStubMatchCount()
{
	return this->stubMatches.size();
}

double CryptMatcher::getAvgMatchDistance()
{
	if (!this->soundMatches.empty())
	{
		double sum = 0.0;
		for (const DMatch &it : this->soundMatches)
			sum = sum + fabs(it.distance);

		return sum / this->soundMatches.size();
	}

	// default: max distance
	else
		return 1.0;
}

double CryptMatcher::getAvgMismatchDistance()
{
	if (!this->stubMatches.empty())
	{
		double sum = 0.0;
		for (const DMatch &it : this->stubMatches)
			sum = sum + fabs(it.distance);

		return sum / this->stubMatches.size();
	}

	// default: max distance
	else
		return 1.0;
}

void CryptMatcher::getPolarCoordinates(TCircle iris, TCircle pupil,
									   double x, double y, double *radius, double *angle)
{

	// Consider the pupil center as the pole
	x = x - pupil.center().x();
	y = y - pupil.center().y();

	*angle = atan2(y, x);
	if ((*angle) < 0.0)
		(*angle) += (2 * M_PI); // two pi

	double irisX = iris.center().x() - pupil.center().x();
	double irisY = iris.center().y() - pupil.center().y();

	double irisPosition = iris.radius() - sqrt(pow(x - irisX, 2) + pow(y - irisY, 2));
	double pupilPosition = sqrt(pow(x, 2) + pow(y, 2)) - pupil.radius();

	*radius = pupilPosition / (pupilPosition + irisPosition);
}

bool CryptMatcher::areSoundPolarCoordinates(double r1, double a1, double r2, double a2)
{
	if (fabs(r1 - r2) > Configuration::KP_MATCHER_POLAR_RADIUS_TOLERANCE)
		return false;

	if (fabs(a1 - a2) > Configuration::KP_MATCHER_POLAR_ANGLE_TOLERANCE)
		return false;

	return true;
}

Mat CryptMatcher::getCryptMask(TShepiiResponse *iris, int kpIndex,
							   int imageWidth, int imageHeight)
{
	Mat mask = Mat::zeros(imageWidth, imageHeight, CV_8UC1);

	TPoint tpoint = iris->keypoints(kpIndex);
	float radius = tpoint.size() / 2;
	int width = int(round(tpoint.size()));
	int height = int(round(tpoint.size()));

	int x = int(round(tpoint.x() - radius));
	while (x < 0)
	{
		x++;
		width--;
	}
	while (x + width > mask.cols)
		width--;

	int y = int(round(tpoint.y() - radius));
	while (y < 0)
	{
		y++;
		height--;
	}
	while (y + height > mask.rows)
		height--;

	vector<vector<Point>> *cvCrypts = new vector<vector<Point>>();
	for (int i = 0; i < iris->crypts_size(); i++)
	{
		Crypt *tCrypt = iris->mutable_crypts(i);
		vector<Point> cvCrypt = vector<Point>();
		for (int j = 0; j < tCrypt->points_size(); j++)
		{
			TPoint tpoint = tCrypt->points(j);
			cvCrypt.push_back(Point2i(tpoint.x(), tpoint.y()));
		}
		cvCrypts->push_back(cvCrypt);
	}
	drawContours(mask, *cvCrypts, kpIndex, Scalar(255, 255, 255), -1);
	return mask(Rect(x, y, width, height));
}

void CryptMatcher::detectDescribeAndMatch()
{

	set<int> *usedKps1 = new set<int>();
	set<int> *usedKps2 = new set<int>();
	// Step 1. Detection
	// detects crypts over irises 1 and 2
	TShepiiResponse *irisData1 = this->iris1;
	TShepiiResponse *irisData2 = this->iris2;

	if (irisData1->keypoints_size() < irisData2->keypoints_size())
	{
		irisData1 = this->iris2;
		irisData2 = this->iris1;
	}

	// takes the centers of the crypts as 2D descriptions
	TPoint tpoint;
	Mat *desc1 = new Mat();
	for (size_t i = 0; i < irisData1->keypoints_size(); i++)
	{
		Mat desc = Mat::zeros(Size(2, 1), CV_32FC1);
		desc.at<float>(0, 0) = tpoint.x();
		desc.at<float>(0, 1) = tpoint.y();

		desc1->push_back(desc);
	}

	Mat *desc2 = new Mat();
	for (size_t i = 0; i < irisData2->keypoints_size(); i++)
	{
		Mat desc = Mat::zeros(Size(2, 1), CV_32FC1);
		desc.at<float>(0, 0) = tpoint.x();
		desc.at<float>(0, 1) = tpoint.y();

		desc2->push_back(desc);
	}

	// Step 2. Establishing matches
	// matches the detected keypoints, based on their positions
	BFMatcher *matcher = new BFMatcher(NORM_L2);
	vector<vector<DMatch>> *matches = new vector<vector<DMatch>>();
	matcher->knnMatch(*desc1, *desc2, *matches, desc2->rows);
	delete matcher;

	int image1Width = irisData1->inputimagewidth();
	int image1Height = irisData1->inputimageheight();
	int image2Width = irisData2->inputimagewidth();
	int image2Height = irisData2->inputimageheight();

	// for each match
	for (vector<vector<DMatch>>::iterator it = matches->begin(); it != matches->end(); ++it)
		for (vector<DMatch>::iterator match = it->begin(); match != it->end(); ++match)
		{
			// if the current keypoints were not used yet
			if (usedKps1->find(match->queryIdx) == usedKps1->end() &&
				usedKps2->find(match->trainIdx) == usedKps2->end())
			{
				// keypoints from images 1 and 2
				TPoint tp1 = irisData1->keypoints(match->queryIdx);
				TPoint tp2 = irisData2->keypoints(match->trainIdx);

				// polar coordinates of the matched keypoints
				double r1, a1, r2, a2;
				this->getPolarCoordinates(irisData1->cropiris(),
										  irisData1->croppupil(),
										  tp1.x(), tp1.y(), &r1, &a1);
				this->getPolarCoordinates(irisData2->cropiris(),
										  irisData2->croppupil(),
										  tp2.x(), tp2.y(), &r2, &a2);

				// if the polar coordinates are sound
				if (this->areSoundPolarCoordinates(r1, a1, r2, a2))
				{
					// obtains masks to crypts 1 and 2, with the same size
					Mat mask1 = this->getCryptMask(irisData1, match->queryIdx,
												   image1Width, image1Height);
					Mat mask2 = this->getCryptMask(irisData2, match->trainIdx,
												   image2Width, image2Height);
					resize(mask2, mask2, mask1.size());

					Mat diffMask = abs(mask1 - mask2) / 255;
					int maskSize = diffMask.rows * diffMask.cols;

					double coincidentCryptArea = maskSize - sum(diffMask)[0];
					double coincidenceRatio = coincidentCryptArea / maskSize;

					// if the content is coincident enough
					if (coincidenceRatio > Configuration::CRYPTS_MATCH_COINCIDENCE_TOLERANCE)
					{
						// registers the use of the current keypoints
						irisData1->add_matchindex(match->queryIdx);
						irisData2->add_matchindex(match->trainIdx);

						// sets the match distance value
						double dist = 1.0 -
									  (Configuration::CRYPTS_MATCH_DIST_ALPHA *
										   coincidenceRatio +
									   (1.0 - Configuration::CRYPTS_MATCH_DIST_ALPHA) *
										   coincidentCryptArea / (image1Width * image1Height));
						match->distance = float(dist);

						// no need for checking the remaining matches
						break;
					}
					// else, next try...
				}
				// else, next try...
			}
			// else, next try...
		}

	// frees some memory
	delete matches;

	// sorts the obtained matches and registers the matched interest points within the iris detectors
	sort(this->soundMatches.begin(), this->soundMatches.end(), Functions::compareMatches);
	for (vector<DMatch>::iterator match = this->soundMatches.begin(); match != this->soundMatches.end(); ++match)
	{
		iris1->add_matchindex(match->queryIdx);
		iris2->add_matchindex(match->trainIdx);
	}

	// frees some memory
	delete usedKps1;
	delete usedKps2;
	delete desc1;
	delete desc2;
}
