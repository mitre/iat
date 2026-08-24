/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef IRIS_H
#define IRIS_H

#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

/** Implements an iris data structure for the TSHEPII application. */
class Iris {
public:
	/** Constructor.
	 *  It receives the iris image filepath as a parameter and fills the other iris attributes accordingly.
	 *  It DOES NOT pre-processes the iris content.
	 *  @param [in] filePath The filepath of the iris image. */
	Iris(string filePath);

	/**
	* Creates an iris from an image in PIXMAP format
	* 
	* @param filepath The path to the image file
	* @param width The width of the image in pixels
	* @param height The height of the image in pixels
	*/
	Iris(string filepath, int width, int height);

	/** Pre-processes the iris content with OSIRIS software.
	 *  @return void */
	void osirisPreProcess();

	/** Pre-processes the iris content based on the given manually generated data.
	 *
	 *  @param [in] contentMask Manually generated iris mask.
	 *  @param [in] stripContent Manually generated strip-normalized version of the iris.
	 *  @param [in] stripContentMask Manually generated content mask of the strip-normalized version of the iris.
	 *
	 *  @param [in] irisCenter The position of the the manually defined iris center.
	 *  @param [in] radius Manually defined iris radius.
	 *
	 *  @param [in] pupilCenter The position of the the manually defined pupil center.
	 *  @param [in] pupilRadius Manually defined pupil radius.
	 *
	 *  @return void */
	void manualPreProcess(
		Mat *contentMask, Mat *stripContent, Mat *stripContentMask,
		Point2d irisCenter, double radius,
		Point2d pupilCenter, double pupilRadius);

	/** Returns the filepath of the current iris.
	 *  @return The filepath of the current iris. */
	string getFilePath();

	/** Returns the iris ID string.
	 *  @return The ID of the iris. */
	string getId();

	/** Returns the original iris image.
	 *  @return The original iris image. */
	Mat getOriginalContent();

	/** Returns the original iris content mask.
	 *  @return The original iris content mask. */
	Mat getOriginalContentMask();

	/** Returns the original iris center.
	 *  @return The original iris center. */
	Point2d getOriginalIrisCenter();

	/** Returns the original iris radius.
	 *  @return The original iris radius, in pixels. */
	double getOriginalIrisRadius();

	/** Returns the original pupil center.
	 *  @return The original pupil center. */
	Point2d getOriginalPupilCenter();

	/** Returns the original pupil radius.
	 *  @return The original pupil radius, in pixels. */
	double getOriginalPupilRadius();

	/** Returns the strip-normalized version of the iris.
	 *  @return The strip-normalized version of the iris. */
	Mat getStripContent();

	/** Returns the mask of the strip-normalized version of the iris.
	 *  @return The mask of the strip-normalized version of the iris. */
	Mat getStripContentMask();

	/** Returns the colorful cropped version of the iris.
	 *  @return The colorful cropped version of the iris. */
	Mat getColorCrop();

	/** Returns the grayscaled cropped version of the iris.
	 *  @return The grayscaled cropped version of the iris.*/
	Mat getGrayCrop();

	/** Returns the 3-channel grayscaled cropped version of the iris.
	 *  @return The 3-channel grayscaled cropped version of the iris. */
	Mat getGray3CCrop();

	/** Returns the mask to the cropped version of the iris.
	 *  @return The mask to the cropped version of the iris. */
	Mat getCropMask();

	/** Returns the radius of the cropped iris.
	 *  @return The radius of the cropped iris, in pixels. */
	double getCropRadius();

	/** Returns the position of the cropped iris center.
	 *  @return The position of the cropped iris center. */
	Point2d getCropCenter();

	/** Returns the radius of the pupil within the cropped iris.
	 *  @return The radius of the pupil within the cropped iris, in pixels. */
	double getCropPupilRadius();

	/** Returns the position of the pupil center within the cropped iris.
	 *  @return The position of the pupil center within the cropped iris. */
	Point2d getCropPupilCenter();

private:
	/** Iris image filepath. */
	string filePath;

	/** Iris identification string. */
	string id;

	/** Iris original 3-channel content. */
	Mat originalContent;

	/** Original iris content mask. */
	Mat originalContentMask;

	/** Original iris center. */
	Point2d originalIrisCenter;

	/** Original iris radius. */
	double originalIrisRadius;

	/** Original pupil center. */
	Point2d originalPupilCenter;

	/** Original pupil radius. */
	double originalPupilRadius;

	/** Strip-normalized version of the iris. */
	Mat stripContent;

	/** Mask of the strip-normalized version of the iris. */
	Mat stripContentMask;

	/** Cropped iris 3-channel content. */
	Mat colorCrop;

	/** Cropped iris gray-scaled content. */
	Mat grayCrop;

	/** Cropped iris 3-channel gray-scaled content. */
	Mat gray3CCrop;

	/** Cropped iris mask. */
	Mat cropMask;

	/** Radius of the cropped iris. */
	double cropRadius;

	/** Position of the cropped iris center. */
	Point2d cropCenter;

	/** Radius of the pupil within the cropped iris. */
	double cropPupilRadius;

	/** Position of the pupil center within the cropped iris. */
	Point2d cropPupilCenter;

	/** Pre-processes the iris content, and sets the proper iris object attributes.
	 *
	 *  @param [in] originalMask The original iris mask, in its original resolution.
	 *  @param [in] originalStripContent The original strip-normalized version of the iris content.
	 *  @param [in] originalStripContentMask The content mask of the original strip-normalized version of the iris.
	 *
	 *  @param [in] originalCenter The original position of the center of the iris.
	 *  @param [in] originalRadius The original radius of the iris.
	 *
	 *  @param [in] originalPupilCenter The original position of the pupil center.
	 *  @param [in] originalPupilRadius The original pupil radius of the iris.
	 *
	 *  @return void */
	void preProcess(
		Mat *originalMask, Mat *originalStripContent, Mat *originalStripContentMask,
		Point2d originalCenter, double originalRadius,
		Point2d originalPupilCenter, double originalPupilRadius);
};

#endif
