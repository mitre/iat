/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef CRYPT_DETECTOR_H
#define CRYPT_DETECTOR_H

#include "Tshepii.pb.h"
#include <array>
#include <set>
#include <list>
#include <opencv2/opencv.hpp>

using std::array;
using std::set;
using std::runtime_error;
using std::list;
using std::vector;
using cv::Scalar;
using cv::KeyPoint;
using cv::Point;
using cv::Mat;
using org::mitre::iwp::buffers::TShepiiResponse;

/** Implements a detector of crypts over iris images. */
class CryptDetector {
public:
	/** Constructor.
	 *
	 *  @param [in] inputIris The iris to be processed.
	 *  @param [in] inputirisMask A mask to be taken into account when processing the given iris.
	 *  @param [in] outputIris The result of processing the given iris.
	 *  @param [in] originalIris The original version of the input iris image, without any previous processing.
	 *
	 *  @param [in] irisRadius The radius, in pixels, of the iris depicted in the input image.
	 *  @param [in] irisX The x coordinate of the iris depicted in the input image.
	 *  @param [in] irisY The y coordinate of the iris depicted in the input image.
	 *
	 *  @param [in] pupilRadius The radius, in pixels, of the pupil of the iris depicted in the input image.
	 *  @param [in] pupilX The x coordinate of the pupil.
	 *  @param [in] pupilY The y coordinate of the pupil.
	 *
	 *  @param [in] color The color for rendering the detected interest points.
	 */
	CryptDetector(Mat *inputIris, Mat *inputIrisMask, Mat *outputIris, Mat *originalIris,
		double irisRadius, double irisX, double irisY,
		double pupilRadius, double pupilX, double pupilY,
		Scalar color);

	/** Constructor of iris processors.
	 *  @param [in] inputIris The iris to be processed.
	 *  @param [in] inputIrisMask A mask to be taken into account when processing the given iris.
	 *  @param [in] outputIris The result of processing the given iris.
	 *  @param [in] originalIris The original version of the input iris image, without any previous processing.
	 */
	CryptDetector(Mat *inputIris, Mat *inputIrisMask, Mat *outputIris, Mat *originalIris) {
		this->inputIris = inputIris;
		this->inputIrisMask = inputIrisMask;
		this->outputIris = outputIris;
		this->originalIris = originalIris;
	}

	/** Destructor. */
	~CryptDetector() {
		this->inputIris = NULL;
		this->inputIrisMask = NULL;
		this->outputIris = NULL;
		this->originalIris = NULL;
	}

	/** Returns a pointer to the input iris mask.
	 *  @return The input iris mask. */
	Mat* getInputIrisMask() {
		return this->inputIrisMask;
	}
	/** Obtains the id and group of the interest point that occupies the given (<x>, <y>) iris image position.
	 *  @param [in] x The x coordinate of the given target position.
	 *  @param [in] y The y coordinate of the given taget position.
	 *  @param [out] id The id (i.e., the position on the proper list) of the obtained interest point.
	 *                  It will be equal to -1 if no point occupies the given position.
	 *  @param [out] grp The group of the obtained interest point (0: for truly matched, 1: for matched to missing, and 2: for stub interest point).
	 *                   It will be equal to -1 if not applicable.
	 *  @param [out] distance The distance between the interest pointer center and the target position.
	 *                        It will be equal to -1 if not applicable.
	 *  @return void */
	void getKeypointIdAt(int x, int y, int *id, int *grp, double *distance);

	/** Marks the given interest point as currently selected.
	 *  @param [in] id The id (position in the proper list) of the selected interest point.
	 *  @param [in] grp The group of the selected interest point (0: for truly matched, 1: for matched to missing, and 2: for stub interest point).
	 *  @return void */
	void selectKeypoint(int id, int grp);

	/** Marks no interest point as currently selected.
	 *  @return void */
	void resetSelection();

	/** Removes the given interest point from the set of detected points.
	 *  @param [in] id The id (position in the proper list) of the selected interest point.
	 *  @param [in] grp The group of the selected interest point (0: for truly matched, 1: for matched to missing, and 2: for stub interest point).
	 *  @return void */
	void removeKeypoint(int id, int grp);

	/** Cancels the last interest point removal in the removal history.
	 *  @return void */
	void undoLastRemoval();

	/** Computes the polar coordinates of the given <x, y> position, concerning the given input iris.
	 *  @param [in] x The x coordinate of the position to be transformed.
	 *  @param [in] y The y coordinate of the position to be transformed.
	 *  @param [out] r The radial position of the obtained polar coordinate.
	 *  @param [out] a The angle of the obtained polar coordinate.
	 *  @return void */
	void getPolarCoordinates(double x, double y, double *r, double *a);

	/** Computes the carthesian coordinates of the given <r, a> polar position, concerning the given input iris.
	 *  @param [in] r The radial position of the polar coordinate to be transformed.
	 *  @param [in] a The angle of the polar coordinate to be transformed.
	 *  @param [out] x The x coordinate of the obtained carthesian position.
	 *  @param [out] y The y coordinate of the obtained carthesian position.
	 *  @return void */
	void getCarthesianCoordinates(double r, double a, double *x, double *y);

	/** Adds the given index to the list of positions of matched interest points.
	 *  @param [in] index The index of the truly matched interest point, within the list of sorted interest points.
	 *  @return void */
	void addMatchedKeypointIndex(int index);

	/** Adds the given index to the list of positions of interest points that were matched to missing interest points in the other iris image.
	 *  @param [in] index The index of the interest point matched to a missing interest point on the other iris image.
	 *  @return void */
	void addMatchedToMissingKeypointIndex(int index);

	/** Adds the given index to the list of positions of the missing (stub) interest points that were matched to interest points in the other iris image.
	 *  @param [in] index The index of the missing interest point matched to an interest point on the other iris image.
	 *  @return void */
	void addMissingKeypointIndex(int index);

	/** Configures the number of matched interest points that must be rendered.
	 *  @param [in] keypointRenderCount The number of interest points.
	 *  @return void */
	void setKeypointRenderCount(int keypointRenderCount);

	/** Configures the number of interest points matched to missing interest points on the other iris image that must be rendered.
	 *  @param [in] keypointToMissingRenderCount The number of interest points.
	 *  @return void */
	void setKeypointToMissingRenderCount(int keypointToMissingRenderCount);

	/** Configures the number of missing (stub) interest points that must be rendered.
	 *  @param [in] missingKeypointRenderCount The number of interest points.
	 *  @retutn void */
	void setMissingKeypointRenderCount(int missingKeypointRenderCount);

	/** Returns a pointer to the obtained and sorted interest points, according to their strengh.
	 *  @return The obtained and sorted interest points, according to their response. */
	vector<KeyPoint>* getSortedKeypoints();

	/** Returns a pointer to the list of missing (stub) interest points.
	 *  @return The list of missing (stub) interest points. */
	vector<KeyPoint>* getMissingKeypoints();

	/** Returns the color used for rendering the detected interest points.
	 *  @return The color used for rendering the detected interest points. */
	Scalar getColor();

	/** Returns the positions of the removed truly matched interest points.
	 *  @return The positions of the removed truly matched interest points. */
	set<int> getRemovedMatchedKeypointIndices();

	/** Returns the positions of the removed interest points that were matched to missing interest points in the other iris image.
	 *  @return The positions of the removed interest points that were matched to missing interest points in the other iris image. */
	set<int> getRemovedMatchedToMissingKeypointIndices();

	/** Returns the positions of the removed stub (a.k.a. missing) interest points.
	 *  @return The positions of the removed stub (a.k.a. missing) interest points. */
	set<int> getRemovedMissingKeypointIndices();

	/** Returns the interest point positions that must be visually linked in the application interface.
	 *  @param [out] points It will hold a list with the valid interest point centers.
	 *  @param [out] missingKeypointFirst TRUE if the missing interest points must be listed before the true interest points 
	 *                                    that are matched to missing interest points on the other iris image, FALSE otherwise.
	 *  @return void */
	void getConnectablePoints(list<Point> *points, bool missingKeypointFirst);

	/** Return the size, in number of pixels, of the output iris image.
	 *  @return The number of pixels of the output iris image. */
	int getOutputArea();

	/** Detects and describes interest points over the original iris image.
	 *  Each subclass of CryptDetector MUST implement this method.
	 *  @param [out] descriptions The descriptions of the detected interest points.
	 *  @return void */
	virtual void detectAndDescribeSortedKeypoints(Mat *descriptions) {
		throw runtime_error("Don't forget to implement YourClass::detectAndDescribeSortedKeypoints().");
	}

	/** Describes the given interest point concerning the original iris image.
	 *  Each subclass of CryptDetector MUST implement this method.
	 *  @param [in] keypoint The keypoint to be described.
	 *  @param [out] description The described interest point.
	 *  @return void */
	virtual void describeKeypoint(KeyPoint *keypoint, Mat *description) {
		throw runtime_error("Don't forget to implement YourClass::describeKeypoint(KeyPoint *keypoint).");
	}

	/** Extracts iris crypts from the input image, according to the work of Chen et. al.
	 *  @return void */
	void detectCrypts();

	/** Returns a mask to the wanted crypt.
	 *  @param [in] cryptPosition The position of the crypt in the list of detected crypts.
	 *  @return A mask containing the crypt shape. */
	Mat getCryptMask(int cryptPosition);

	/** Holds the obtained crypts. */
	vector<vector<Point>> crypts;

private:
	/** Detected and selected interest points, sorted by response. */
	vector<KeyPoint> sortedKeypoints;

	/** Holds the missing interest points, that should be detected but were not.  */
	vector<KeyPoint> missingKeypoints;

	/** Holds the positions of the truly matched interest points, within the list of sorted interest points. */
	vector<int> matchedKeypointIndices;

	/** Holds the positions of the interest points matched to missing interest points in the other iris image, within the list of sorted interest points. */
	vector<int> matchedToMissingKeypointIndices;

	/** Holds the positions of the missing interest points matched to interest points in the other iris image, within the list of missing interest points. */
	vector<int> missingKeypointIndices;

	/** Holds the positions of the removed truly matched interest points. */
	set<int> removedMatchedKeypointIndices;

	/** Holds the positions of the removed interest points that were matched to missing interest points in the other iris image. */
	set<int> removedMatchedToMissingKeypointIndices;

	/** Holds the positions of the removed stub (a.k.a. missing) interest points. */
	set<int> removedMissingKeypointIndices;

	/** Stack of removed interest points, containing the point position (inside the proper list)
	 *  and the source group (0: for truly matched, 1: for matched to missing, and 2: for stub interest point). */
	list<array<int, 2>> removalHistory;

	/** Holds the number of truly matched interest points that must be rendered. */
	int keypointRenderCount;

	/** Holds the number of interest points matched to missing interest points on the other iris image that must be rendered. */
	int keypointToMissingRenderCount;

	/** Holds the number of missing interest points that must be rendered. */
	int missingKeypointRenderCount;

	/** Id of the currently selected interest point (actually, its position on the proper list).
	 *  Its value is -1, when no interest point is selected. */
	int selectedKeypointId;

	/** Group of the currently selected interest point.
	 *  It can be either -1: not applicable, 0: truly matched, 1: matched to missing, or 2: stub interest point. */
	int selectedKeypointGrp;

	/** Selects and sorts keypoints from the given ones.
	 *  @param [in] sourceKeypoints The source of keypoints that must be selected and sorted.
	 *  @param [out] selectedKeypoints The resulting selection of sorted keypoints.
	 *  @return void */
	void selectAndSortKeypoints(vector<KeyPoint> *sourceKeypoints, vector<KeyPoint> *selectedKeypoints);

	/** Input iris that must be processed. */
	Mat *inputIris;

	/** Mask of the input image. */
	Mat *inputIrisMask;

	/** Output image that will hold the processed input image. */
	Mat *outputIris;

	/** Original version of the input iris. */
	Mat *originalIris;

	/** Radius of the input iris. */
	double irisRadius;

	/** Radius of the pupil of the input iris. */
	double pupilRadius;

	/** Carthesian center of the input iris. */
	double irisX, irisY;

	/** Carthesian center of the pupil of the input iris. */
	double pupilX, pupilY;

	/** Color of the detected interest points. */
	Scalar color;
	static CryptDetector fromProtobuf(TShepiiResponse buf);
};

#endif

