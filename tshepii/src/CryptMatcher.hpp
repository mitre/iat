/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef CRYPT_MATCHER_H
#define CRYPT_MATCHER_H

#include "CryptDetector.hpp"
#include "CryptDetector.hpp"
#include "IrisProcessor.hpp"
#include <list>
#include <array>

using org::mitre::iwp::buffers::TShepiiResponse;
using org::mitre::iwp::buffers::TCircle;

/** Implements a matcher of crypts. */
class CryptMatcher {
public:
	/** Constructor.
	 *  @param [in] detector1 The crypt detector that works on input iris 1.
	 *  @param [in] detector2 The crypt detector that works on input iris 2.
	 *  @param [in] progress A pointer to the variable that holds the progress of crypt detection and matching.
	 *  @param [in] matchRenderCount A pointer to the variable that holds  the amount of crypt matches that must be rendered by the application.
	 *  @param [in] showMatches TRUE if crypt matches must be rendered by the application, FALSE otherwise.
	 */
	CryptMatcher(TShepiiResponse *iris1, TShepiiResponse *iris2);

	/** Constructor.
	 *  @param [in] detector1 The interest point detector that works on input iris 1.
	 *  @param [in] detector2 The interest point detector that works on input iris 2.
	 *  @param [in] distanceType The type of distance calculated between the descriptions of the two iris images.
	 *                           Give cv::NORM_L2 or another cv::one.
	 *  @param [in] progress A pointer to the variable that holds the progress of interest point detection, description, and matching.
	 *  @param [in] matchRenderCount A pointer to the variable that holds the amount of true matches that must be rendered by the application.
	 *  @param [in] stubMatchRenderCount A pointer to the variable that holds the amount of missed matches that must be rendered by the application.
	 *  @param [in] showMatches TRUE if true matches must be rendered by the application, FALSE otherwise.
	 *  @param [in] showStubMatches TRUE if missed matches must be rendered by the application, FALSE otherwise.
	 */
	CryptMatcher(TShepiiResponse *iris1, TShepiiResponse *iris2, int distanceType);

	/** Destructor. */
	~CryptMatcher();


	Mat getCryptMask(TShepiiResponse *iris, int kpIndex, int imageWidth,
			int imageHeight);

	/** Detects, describes, and matches the keypoints belonging to irises 1 and 2.
	 *  It highlights the unmatched interest points by establishing stub matches between them and
	 *  the respective regions on the opposite iris image.
	 *  @return void */
	void detectDescribeAndMatch();

	/** Returns the number of established interest-point true matches between irises 1 and 2.
	 *  @return The number of established interest-point true matches between irises 1 and 2. */
	size_t getMatchCount();

	/** Returns the number of established stub-keypoint (un)matches between irises 1 and 2. 
	 *  @return The number of established stub-keypoint (un)matches between irises 1 and 2. */
	size_t getStubMatchCount();

	/** Return the average distance of the true matches.
	 *  @return The average distance of true matches. */
	double getAvgMatchDistance();

	/** Returns the average distance of the stub matches.
	 *  @return The average distance of stub matches. */
	double getAvgMismatchDistance();

private:
	/** Points to the variable that holds the progress of interest point detection, description, and matching.
	 *  Max value for each matcher: 8 (eight steps). */
	int *progress;

	/** Holds the geometrically sound keypoint matches. */
	vector<DMatch> soundMatches;

	/** Verifies if the given polar coordinates are geometrically sound.
	 *  @param [in] r1 The radius of the polar coordinate over iris 1.
	 *  @param [in] a1 The angle of the polar coordinate over iris 1.
	 *  @param [in] r2 The radius of the polar coordinate over iris 2.
	 *  @param [in] a2 The angle of the polar coordinate over iris 2.
	 *  @return TRUE if the coordinates match, FALSE otherwise. */
	bool areSoundPolarCoordinates(double r1, double a1, double r2, double a2);

	void getPolarCoordinates(TCircle iris, TCircle pupil,
							 double x, double y, double *radius, double *angle);

	/** Image processor of iris 1. */
	TShepiiResponse *iris1;

	/** Image processor of iris 2. */
	TShepiiResponse *iris2;
	/** Type of distance calculated between two interest points. */
	int distanceType;

	/** Holds the amount of true matches that must be rendered by the application.  */
	int *matchRenderCount;

	/** Holds the amount of missed matches that must be rendered by the application.  */
	int *stubMatchRenderCount;

	/** Flags if true matches must be rendered by the application. */
	bool *showMatches;

	/** Flags if missed matches must be rendered by the application. */
	bool *showStubMatches;

	/** Holds the stub matches that help to highlight the unmatched interest points. */
	vector<DMatch> stubMatches;
};

#endif
