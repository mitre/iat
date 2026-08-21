/*****************************************************************************
* Eye Orientation CLassifier v2 (U/D + L/R)
* Author: Adam Czajka, aczajka@nd.edu, May 2016
*
* Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
* under BSD license http://opensource.org/licenses/bsd-license.php
******************************************************************************/

#ifndef ACII_CLIENT_INCLUDE_EOC2EYE_H_
#define ACII_CLIENT_INCLUDE_EOC2EYE_H_

#ifdef EOC2_DEBUG
#include <iostream>
#endif
#include <string>
#include <vector>
#include "EOC2Utils.h"
#include "Iwp.pb.h"

using std::string;
using std::vector;
using org::mitre::iwp::buffers::ImageServiceQuery;

namespace EOC2 {
/** Eye handler.
 * Allows to process one eye, that is calculate
 * L/R and U/D features. This class is the link between the manager
 * and the processing functions.
 * @see EOC2Processings
 * @see EOC2Manager
 */
class EOC2Eye {
 public :
  /** Default constructor.
   * Initialize all pointers of image to 0.
   * Initialize pupil and iris circles to (0,0,0).
   */
  EOC2Eye(int image_type = ImageServiceQuery::PNG,
      int image_width = 0, int image_height = 0);

  /** Default destructor.
   * Release all images (free memory).
   */
  ~EOC2Eye();

  /** Load the original image corresponding to the eye.
   * @param rFilename Complete path of the image
   * @return void
   * @see loadImage()
   */
  void loadOriginalImage(const string &rFilename);

  /**
   * Loads the eye image from a raw pixel format (8 bpp 3 channel)
   *
   * @param rFilename The local path to the file
   */
  void loadPixmap(const string &rFilename);

  /** Initialize the mask.
   * Create the mask and set all pixels to 255.
   * This function is used when user did not provide the mask
   * or did not ask to compute the mask and yet mask is required to go on.
   * @return void
   */
  void initMask();


  /** Segment the eye.
   * Initialize the mask if they have not been created yet.\n
   * Call the function EOC2Processings::segment().\n
   * Draw the results of segmentation on the private
   * attribute mpSegmentedImage (color image).
   * @param minIrisDiameter The minimum diameter for segmenting the iris
   * @param minPupilDiameter The minimum diameter for segmenting the pupil
   * @param maxIrisDiameter The maximum diameter for segmenting the iris
   * @param maxPupilDiameter The maximum diameter for segmenting the pupil
   * @return void
   * @see EOC2Processings::segment()
   */
  void segment(int minIrisDiameter, int minPupilDiameter, int maxIrisDiameter,
        int maxPupilDiameter);


  /** Normalize image and mask.
   * If the mask is not already initialized, the function does intialize it
   * to 255. Uses the Daugman's rubber-sheet method.
   * @param widthOfNormalizedIris Width of normalized image
   * @param heightOfNormalizedIris Height of normalized image
   * @return void
   * @see EOC2Processings::normalize()
   */
  void normalize(int widthOfNormalizedIris, int heightOfNormalizedIris);


  /** Calculates the image features required for upright / upside-down
   * classification.
   * @return void
   */
  void featuresUD();


  /** Calculates the image features required for left / right classification.
   * @return void
   */
  void featuresLR();


#ifdef EOC2_DEBUG
  /** Prints features calulated for upright / upside-down classification.
   * @return void
   */
  void printFeaturesUD(int &iFileNo, const string &rFilename,
      const string &label);


  /** Prints the feature header for upright / upside-down classification.
   * @return void
   */
  void printFeaturesUDHeader();


  /** Prints features calulated for left / right classification.
      * @return void
      */
  void printFeaturesLR(int &iFileNo, const string &rFilename,
      const string &label);


  /** Prints the feature header for left / right classification.
      * @return void
      */
  void printFeaturesLRHeader();
#endif


  /** Returns the number of features used in left / right classification.
      * @return int
      */
  int getNumberOfLRfeatures();


  /** Returns the number of features used in upright / upside-down classification.
      * @return int
      */
  int getNumberOfUDfeatures();


  /** Image features for upright / upside-down classification. */
  struct _stFeaturesUD {
      float fVerticalPupilShift;
      float fEyelidCoverage;
      float fEyelidValue;
      float fEyelidFiltered;
      float fEyelidFilteredCosVar;
      float fEyelidFilteredSinVar;
  } stFeaturesUD;


  /** Image features for left / right classification. */
  struct _stFeaturesLR {
      float fEyelidValue;
      float fHorizontalPupilShift;
  } stFeaturesLR;


  /** Flag indicating that the iris image was correctly processed. */
  bool bProcessedOK;


#ifdef EOC2_DEBUG
  /** Save debug data (not used in the final version):
      * - segmented color image
      * - binary mask
      * - normalized image
      * - normalized mask
      * - contour parameters
      * @param rFilename Complete path of the image
      * @return void
      * @see saveImage()
      */
          void saveSegmentedImage ( const string &rFilename ) ;
          void saveMask ( const string &rFilename ) ;
          void saveNormalizedImage ( const string &rFilename ) ;
          void saveNormalizedMask ( const string &rFilename ) ;
          void saveParameters ( const string &rFilename ) ;
#endif

 private :

  /** The format of the image being loaded */
  int imageType;

  /** The height & width (for pixmap images) */
  int imageWidth;
  int imageHeight;

  /** The original image corresponding to the eye (input only). */
  IplImage *mpOriginalImage;

#ifdef EOC2_DEBUG
  /** The segmented image (color) corresponding to the eye (output only). */
  IplImage * mpSegmentedImage ;
#endif

  /** The mask corresponding to the eye (input and/or output). */
  IplImage * mpMask;

  /** The normalized image corresponding to the eye (input and/or output). */
  IplImage * mpNormalizedImage;

  /** The normalized mask corresponding to the eye (input and/or output). */
  IplImage * mpNormalizedMask;

  /** The pupil circle corresponding to the eye (input and/or output). */
  EOC2Circle mPupil;

  /** The iris circle corresponding to the eye (input and/or output). */
  EOC2Circle mIris;

  /** The pupil coarse contour corresponding to the eye. */
  vector<CvPoint> mCoarsePupilContour;

  /** The iris coarse contour corresponding to the eye. */
  vector<CvPoint> mCoarseIrisContour;

  /** The theta sampling for pupil coarse contour. */
  vector<float> mThetaCoarsePupil;

  /** The theta sampling for iris coarse contour. */
  vector<float> mThetaCoarseIris;

  /** Number of features used in upright / upside-down recognition. */
  int iNumberOfUDfeatures;

  /** Number of features used in left / right recognition. */
  int iNumberOfLRfeatures;

  /** Generic function to save the image-like attributes of the eye.
      * @param rFilename The complete path of the image
      * @param ppImage A pointer of pointer on the image
      * @return void
      */
  void loadImage(const string &rFilename, IplImage ** ppImage);

#ifdef EOC2_DEBUG
  /** Generic function to load the image-like attributes of the eye.
      * Needed only when saving the debug data (not used in the final version)
      * @param rFilename The complete path of the image
      * @param pImage A pointer on the image
      * @return void
      */
  void saveImage(const string &rFilename, const IplImage *pImage);
#endif
};  // class EOC2Eye
}  // namespace EOC2

#endif  // ACII_CLIENT_INCLUDE_EOC2EYE_H_
