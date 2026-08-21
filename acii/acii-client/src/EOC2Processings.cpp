/*****************************************************************************
 * Eye Orientation CLassifier v2 (U/D + L/R)
 * Author: Adam Czajka, aczajka@nd.edu, May 2016
 *
 * Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
 * under BSD license http://opensource.org/licenses/bsd-license.php
 ******************************************************************************/


#include <opencv/cv.h>
#include <opencv2/core/core.hpp>
#include <log4cxx/logger.h>
#include <string>
#include <algorithm>
#include <vector>
#include "EOC2Utils.h"
#include "EOC2Processings.h"

using std::invalid_argument;
using std::max;
using std::min;
using std::string;
using std::vector;

const extern log4cxx::LoggerPtr logger;

namespace EOC2 {

  EOC2Processings::EOC2Processings() {
    // Do nothing
  }

  EOC2Processings::~EOC2Processings() {
    // Do nothing
  }

  void EOC2Processings::segment(const IplImage * pSrc,
                                IplImage * pMask,
                                EOC2Circle & rPupil,
                                EOC2Circle & rIris,
                                vector<float> & rThetaCoarsePupil,
                                vector<float> & rThetaCoarseIris,
                                vector<CvPoint> & rCoarsePupilContour,
                                vector<CvPoint> & rCoarseIrisContour,
                                int minIrisDiameter,
                                int minPupilDiameter,
                                int maxIrisDiameter,
                                int maxPupilDiameter) {
    //////////////////
    // Check arguments

    // String functions
    EOC2StringUtils str;

    // Temporary int to check sizes of pupil and iris
    int check_size = 0;

    // Default value for maxIrisDiameter if user did not specify it
    if (maxIrisDiameter == 0) {
      maxIrisDiameter = min(pSrc->height, pSrc->width);
    } else if (maxIrisDiameter > (check_size =
          static_cast<int>(floor(static_cast<float>(min(pSrc->height, pSrc->width)))))) {
      // Change maxIrisDiameter if it is too big relative to image sizes
      LOG4CXX_WARN(logger,
          "Warning in function segment : maxIrisDiameter = " <<
          maxIrisDiameter <<
          " is replaced by " << check_size <<
          " because image size is " << pSrc->width << "x" << pSrc->height);

      maxIrisDiameter = check_size;
    }

    // Default value for maxPupilDiameter if user did not specify it
    if (maxPupilDiameter == 0) {
      maxPupilDiameter =
          static_cast<int>(OSI_MAX_RATIO_PUPIL_IRIS * maxIrisDiameter);
    } else if (maxPupilDiameter > (check_size =
          static_cast<int>(OSI_MAX_RATIO_PUPIL_IRIS * maxIrisDiameter))) {
      // Change maxPupilDiameter if it is too big relative to maxIrisDiameter
      // and OSI_MAX_RATIO_PUPIL_IRIS
      LOG4CXX_WARN(logger,
          "Warning in function segment : maxPupilDiameter = " <<
          maxPupilDiameter <<
          " is replaced by " << check_size <<
          " because maxIrisDiameter = " << maxIrisDiameter <<
          " and ratio pupil/iris is generally lower than " <<
          OSI_MAX_RATIO_PUPIL_IRIS);
      maxPupilDiameter = check_size;
    }

    // Change minIrisDiameter if it is too small relative to OSI_SMALLEST_IRIS
    if (minIrisDiameter < (check_size = OSI_SMALLEST_IRIS)) {
      LOG4CXX_WARN(logger,
          "Warning in function segment : minIrisDiameter = " <<
          minIrisDiameter <<
          " is replaced by " << check_size <<
          " which is the smallest size for detecting iris");
      minIrisDiameter = check_size;
    }

    // Change minPupilDiameter if it is too small relative to minIrisDiameter
    // and OSI_MIN_RATIO_PUPIL_IRIS
    if (minPupilDiameter < (check_size =
          static_cast<int>(minIrisDiameter*OSI_MIN_RATIO_PUPIL_IRIS))) {
      LOG4CXX_WARN(logger,
          "Warning in function segment : minPupilDiameter = " <<
          minPupilDiameter <<
          " is replaced by " << check_size <<
          " because minIrisDiameter = " << minIrisDiameter <<
          " and ratio pupil/iris is generally upper than " <<
          OSI_MIN_RATIO_PUPIL_IRIS);
      minIrisDiameter = check_size;
    }

    // Check that minIrisDiameter < maxIrisDiameter
    if (minIrisDiameter > maxIrisDiameter) {
      throw invalid_argument("Error in function segment : minIrisDiameter = " +
          str.toString(minIrisDiameter) +
          " should be lower than maxIrisDiameter = " +
          str.toString(maxIrisDiameter));
    }

    // Make size odds
    minIrisDiameter += (minIrisDiameter % 2) ? 0 : -1;
    maxIrisDiameter += (maxIrisDiameter % 2) ? 0 : +1;
    minPupilDiameter += (minPupilDiameter % 2) ? 0 : -1;
    maxPupilDiameter += (maxPupilDiameter % 2) ? 0 : +1;

    ///////////////////
    // Start processing

    // Locate the pupil
    detectPupil(pSrc, rPupil, minPupilDiameter, maxPupilDiameter);

    // Fill the holes in an area surrounding pupil
    IplImage * clone_src = cvCloneImage(pSrc);
    cvSetImageROI(clone_src, cvRect(
          static_cast<int>(
              rPupil.getCenter().x - 3.0 / 4.0 * maxIrisDiameter / 2.0),
          static_cast<int>(
              rPupil.getCenter().y - 3.0 / 4.0 * maxIrisDiameter / 2.0),
          static_cast<int>(3.0 / 4.0 * maxIrisDiameter),
          static_cast<int>(3.0 / 4.0 * maxIrisDiameter)));
    fillWhiteHoles(clone_src, clone_src);
    cvResetImageROI(clone_src);

    // Will contain samples of angles, in radians
    vector<float> theta;
    float theta_step = 0;

    /////////////////////////
    // Pupil Accurate Contour

    theta.clear();
    theta_step = static_cast<float>(360.0 / OSI_PI / rPupil.getRadius());
    for (float t = 0; t < 360; t += theta_step) {
      theta.push_back(t*OSI_PI/180);
    }
    vector<CvPoint> pupil_accurate_contour = findContour(clone_src,
        rPupil.getCenter(),
        theta,
        rPupil.getRadius()-20,
        rPupil.getRadius()+20);

    // Circle fitting on accurate contour
    rPupil.computeCircleFittingFloat(pupil_accurate_contour);

    ///////////////////////
    // Pupil Coarse Contour

    theta.clear();
    theta_step = static_cast<float>(360.0 / OSI_PI / rPupil.getRadius() * 2);
    for (float t = 0; t < 360; t += theta_step) {
      if (t > 45 && t < 135) t += theta_step;
      theta.push_back(t*OSI_PI/180);
    }
    vector<CvPoint> pupil_coarse_contour = findContour(clone_src,
        rPupil.getCenter(),
        theta,
        rPupil.getRadius()-20,
        rPupil.getRadius()+20);

    rThetaCoarsePupil = theta;
    rCoarsePupilContour = pupil_coarse_contour;

    // Circle fitting on coarse contour
    rPupil.computeCircleFittingFloat(pupil_coarse_contour);

    ////////////////
    // Mask of pupil

    IplImage * mask_pupil = cvCloneImage(pSrc);
    cvZero(mask_pupil);
    drawContour(mask_pupil, pupil_accurate_contour, cvScalar(255), -1);

    //////////////////////
    // Iris Coarse Contour

    theta.clear();
    int min_radius = max<int>(
        static_cast<int>(rPupil.getRadius() / OSI_MAX_RATIO_PUPIL_IRIS),
        static_cast<int>(minIrisDiameter / 2));
    int max_radius = min<int>(
        static_cast<int>(rPupil.getRadius() / OSI_MIN_RATIO_PUPIL_IRIS),
        static_cast<int>(3 * maxIrisDiameter / 4));
    theta_step = static_cast<float>(360.0 / OSI_PI / min_radius);

    for (float t = 0; t < 360; t += theta_step) {
      if (t < 180 || (t > 225 && t < 315)) t += 2*theta_step;
      theta.push_back(t*OSI_PI/180);
    }

    vector<CvPoint> iris_coarse_contour = findContour(clone_src,
        rPupil.getCenter(),
        theta,
        min_radius,
        max_radius);

    rThetaCoarseIris = theta;
    rCoarseIrisContour = iris_coarse_contour;

    // Circle fitting on coarse contour
    rIris.computeCircleFittingFloat(iris_coarse_contour);

    // Mask of iris
    ///////////////

    IplImage * mask_iris = cvCloneImage(mask_pupil);
    cvZero(mask_iris);
    drawContour(mask_iris, iris_coarse_contour, cvScalar(255), -1);

    ////////////////////////
    // Iris Accurate Contour

    // For iris accurate contour, limit the search of contour inside a mask

    // Dilate mask of iris by a disk-shape element
    IplImage * mask_iris2 = cvCloneImage(mask_iris);
    IplConvKernel * struct_element =
        cvCreateStructuringElementEx(21, 21, 10, 10, CV_SHAPE_ELLIPSE);
    cvDilate(mask_iris2, mask_iris2, struct_element);
    cvReleaseStructuringElement(&struct_element);

    // Dilate the mask of pupil by a horizontal line-shape element
    IplImage * mask_pupil2 = cvCloneImage(mask_pupil);
    struct_element = cvCreateStructuringElementEx(21, 21, 10, 1, CV_SHAPE_RECT);
    cvDilate(mask_pupil2, mask_pupil2, struct_element);
    cvReleaseStructuringElement(&struct_element);
    cvXor(mask_iris2, mask_pupil2, mask_iris2);

    theta.clear();
    theta_step = static_cast<float>(360.0 / OSI_PI / rIris.getRadius());
    for (float t = 0; t < 360; t += theta_step) {
      theta.push_back(t*OSI_PI/180);
    }
    vector<CvPoint> iris_accurate_contour = findContour(clone_src,
        rPupil.getCenter(),
        theta,
        rIris.getRadius()-50,
        rIris.getRadius()+20,
        mask_iris2);

    // Release memory
    cvReleaseImage(&mask_pupil2);
    cvReleaseImage(&mask_iris2);

    //////////////////////////////////////////
    // Mask of iris based on accurate contours

    cvZero(mask_iris);
    drawContour(mask_iris, iris_accurate_contour, cvScalar(255), -1);
    cvXor(mask_iris, mask_pupil, mask_iris);

    /////////////////////////////////////////
    // Refine the mask by removing some noise

    // Build a safe area = avoid occlusions
    IplImage * safe_area = cvCloneImage(mask_iris);
    cvRectangle(safe_area,
        cvPoint(0, 0),
        cvPoint(safe_area->width - 1, rPupil.getCenter().y),
        cvScalar(0), -1);
    cvRectangle(safe_area,
        cvPoint(0, rPupil.getCenter().y + rPupil.getRadius()),
        cvPoint(safe_area->width - 1, safe_area->height - 1),
        cvScalar(0), -1);
    struct_element =
        cvCreateStructuringElementEx(11, 11, 5, 5, CV_SHAPE_ELLIPSE);
    cvErode(safe_area, safe_area, struct_element);
    cvReleaseStructuringElement(&struct_element);

    // Compute the mean and the variance of iris texture inside safe area
    double iris_mean = cvMean(pSrc, safe_area);
    IplImage * variance = cvCreateImage(cvGetSize(pSrc), IPL_DEPTH_32F, 1);
    cvConvert(pSrc, variance);
    cvSubS(variance, cvScalar(iris_mean), variance, safe_area);
    cvMul(variance, variance, variance);
    double iris_variance = sqrt(cvMean(variance, safe_area));
    cvReleaseImage(&variance);
    cvReleaseImage(&safe_area);

    // Build mask of noise : |I-mean| > 2.35 * variance
    IplImage * mask_noise = cvCloneImage(pSrc);
    cvAbsDiffS(pSrc, mask_noise, cvScalar(iris_mean));
    cvThreshold(mask_noise, mask_noise, 2.35 * iris_variance, 255,
        CV_THRESH_BINARY);
    cvAnd(mask_iris, mask_noise, mask_noise);

    // Fusion with accurate contours
    IplImage * accurate_contours = cvCloneImage(mask_iris);
    struct_element = cvCreateStructuringElementEx(3, 3, 1, 1, CV_SHAPE_ELLIPSE);
    cvMorphologyEx(accurate_contours, accurate_contours, accurate_contours,
        struct_element, CV_MOP_GRADIENT);
    cvReleaseStructuringElement(&struct_element);
    reconstructMarkerByMask(accurate_contours, mask_noise, mask_noise);
    cvReleaseImage(&accurate_contours);
    cvXor(mask_iris, mask_noise, pMask);

    // Release memory
    cvReleaseImage(&mask_noise);
    cvReleaseImage(&mask_pupil);
    cvReleaseImage(&mask_iris);
    cvReleaseImage(&clone_src);  // AC added
  }  // end of function




  void EOC2Processings::normalize(const IplImage * pSrc ,
      IplImage * pDst ,
      const EOC2Circle & rPupil ,
      const EOC2Circle & rIris) {
    // Local variables
    CvPoint point_pupil , point_iris;
    int x , y;
    float theta , radius;

    // Set to zeros all pixels
    cvZero(pDst);

    // Loop on columns of normalized src
    for (int j = 0; j < pDst->width; j++) {
      // One column correspond to an angle teta
      theta = static_cast<float>(j) / pDst->width * 2 * OSI_PI;

      // Coordinates relative to both centers : iris and pupil
      point_pupil = convertPolarToCartesian(rPupil.getCenter(),
          rPupil.getRadius(), theta);
      point_iris = convertPolarToCartesian(rIris.getCenter(),
          rIris.getRadius(), theta);

      // Loop on lines of normalized src
      for (int i = 0; i < pDst->height; i++) {
        // The radial parameter
        radius = static_cast<float>(i) / pDst->height;

        // Coordinates relative to both radii : iris and pupil
        x = static_cast<int>((1-radius) *
            point_pupil.x + radius * point_iris.x);
        y = static_cast<int>((1-radius) *
            point_pupil.y + radius * point_iris.y);

        // Do not exceed src size
        if (x >= 0 && x < pSrc->width && y >= 0 && y < pSrc->height) {
          ((uchar*)(pDst->imageData+i*pDst->widthStep))[j] =
              ((uchar*)(pSrc->imageData+y*pSrc->widthStep))[x];
        }
      }
    }
  }


  void EOC2Processings::normalizeFromContour(const IplImage * pSrc ,
      IplImage * pDst ,
      const EOC2Circle & rPupil ,
      const EOC2Circle & rIris ,
      const vector<float> rThetaCoarsePupil ,
      const vector<float> rThetaCoarseIris ,
      const vector<CvPoint> & rPupilCoarseContour ,
      const vector<CvPoint> & rIrisCoarseContour) {
    // Local variables
    CvPoint point_pupil , point_iris;
    int x , y;
    float theta , radius;

    // Set to zeros all pixels
    cvZero(pDst);

    // Loop on columns of normalized src
    for (int j = 0; j < pDst->width; j++) {
      // One column correspond to an angle teta
      theta = static_cast<float>(j) / pDst->width * 2 * OSI_PI;

      // Interpolate pupil and iris radii from coarse contours
      point_pupil = interpolate(rPupilCoarseContour, rThetaCoarsePupil, theta);
      point_iris = interpolate(rIrisCoarseContour, rThetaCoarseIris, theta);

      // Loop on lines of normalized src
      for (int i = 0; i < pDst->height; i++) {
        // The radial parameter
        radius = static_cast<float>(i) / pDst->height;

        // Coordinates relative to both radii : iris and pupil
        x = static_cast<int>((1-radius) *
            point_pupil.x + radius * point_iris.x);
        y = static_cast<int>((1-radius) *
            point_pupil.y + radius * point_iris.y);

        // Do not exceed src size
        if (x >= 0 && x < pSrc->width && y >= 0 && y < pSrc->height) {
          ((uchar*)(pDst->imageData+i*pDst->widthStep))[j] =
              ((uchar*)(pSrc->imageData+y*pSrc->widthStep))[x];
        }
      }
    }
  }

  CvPoint EOC2Processings::interpolate(const vector<CvPoint> coarseContour ,
      const vector<float> coarseTheta ,
      const float theta) {
    float interpolation;
    int i1 , i2;

    if (theta < coarseTheta[0]) {
      i1 = coarseTheta.size() - 1;
      i2 = 0;
      interpolation = (theta - (coarseTheta[i1]-2*OSI_PI)) /
          (coarseTheta[i2] - (coarseTheta[i1]-2*OSI_PI));
    } else if (theta >= coarseTheta[coarseTheta.size()-1]) {
      i1 = coarseTheta.size() - 1;
      i2 = 0;
      interpolation = (theta - coarseTheta[i1]) /
          (coarseTheta[i2]+2*OSI_PI - coarseTheta[i1]);
    } else {
      int i = 0;
      while (coarseTheta[i+1] <= theta) i++;
      i1 = i;
      i2 = i+1;
      interpolation = (theta - coarseTheta[i1]) /
          (coarseTheta[i2] - coarseTheta[i1]);
    }

    float x = (1-interpolation) * coarseContour[i1].x +
        interpolation * coarseContour[i2].x;
    float y = (1-interpolation) * coarseContour[i1].y +
        interpolation * coarseContour[i2].y;

    return cvPoint(static_cast<int>(x), static_cast<int>(y));
  }

  // Convert polar coordinates into cartesian coordinates
  CvPoint EOC2Processings::convertPolarToCartesian(const CvPoint &rCenter,
                                                   int rRadius,
                                                   float rTheta) {
    int x = static_cast<int>(rCenter.x + rRadius * cos(rTheta));
    int y = static_cast<int>(rCenter.y - rRadius * sin(rTheta));
    return cvPoint(x, y);
  }


  // Add left and right borders on an unwrapped image
  IplImage * EOC2Processings::addBorders(const IplImage * pImage ,
      int width) {
    // Result image
    IplImage * result = cvCreateImage(cvSize(pImage->width + 2 * width,
                                             pImage->height),
                                             pImage->depth,
                                             pImage->nChannels);

    // Copy the image in the center
    cvCopyMakeBorder(pImage, result, cvPoint(width, 0), IPL_BORDER_REPLICATE,
        cvScalarAll(0));

    // Create the borders left and right assuming wrapping
    for (int i = 0; i < pImage->height; i++) {
      for (int j = 0; j < width; j++) {
        ((uchar *)(
            result->imageData + i*result->widthStep))[j] =
            ((uchar *)(
              pImage->imageData + i*pImage->widthStep))[pImage->width-width+j];

        ((uchar *)(
            result->imageData + i*result->widthStep))[result->width-width+j] =
            ((uchar *)
                (pImage->imageData + i*pImage->widthStep))[j];
      }
    }

    return result;
  }


  // Detect and locate a pupil inside an eye image
  void EOC2Processings::detectPupil(const IplImage * pSrc,
                                     EOC2Circle & rPupil,
                                     int minPupilDiameter,
                                     int maxPupilDiameter) {
    //////////////////
    // Check arguments

    // String functions
    EOC2StringUtils str;

    // Default value for maxPupilDiameter, if user did not specify it
    if (maxPupilDiameter == 0) {
      maxPupilDiameter = static_cast<int>(
          min(pSrc->height, pSrc->width) * OSI_MAX_RATIO_PUPIL_IRIS);
    } else if (maxPupilDiameter >
        min(pSrc->height, pSrc->width) * OSI_MAX_RATIO_PUPIL_IRIS) {
      // Change maxPupilDiameter if it is too big relative to the image size and
      // the ratio pupil/iris
      int newmaxPupilDiameter =
          static_cast<int>(floor(
              min(pSrc->height, pSrc->width) * OSI_MAX_RATIO_PUPIL_IRIS));
      LOG4CXX_WARN(logger,
          "Warning in function detectPupil : maxPupilDiameter = " <<
          maxPupilDiameter <<
          " is replaced by " << newmaxPupilDiameter <<
          " because image size is " << pSrc->width << "x" << pSrc->height <<
          " and ratio pupil/iris is generally lower than " <<
          OSI_MAX_RATIO_PUPIL_IRIS);
      maxPupilDiameter = newmaxPupilDiameter;
    }

    // Change minPupilDiameter if it is too small relative to OSI_SMALLEST_PUPIL
    if (minPupilDiameter < OSI_SMALLEST_PUPIL) {
      LOG4CXX_WARN(logger,
          "Warning in function detectPupil : minPupilDiameter = " <<
          minPupilDiameter <<
          " is replaced by " << OSI_SMALLEST_PUPIL <<
          " which is the smallest size for detecting pupil");
      minPupilDiameter = OSI_SMALLEST_PUPIL;
    }

    // Check that minPupilDiameter < maxPupilDiameter
    if (minPupilDiameter >= maxPupilDiameter) {
      throw invalid_argument(
          "Error in function detectPupil : minPupilDiameter = " +
          str.toString(minPupilDiameter) +
          " should be lower than maxPupilDiameter = " +
          str.toString(maxPupilDiameter));
    }

    ///////////////////
    // Start processing

    // Resize image (downsample)
    float scale = static_cast<float>(OSI_SMALLEST_PUPIL) / minPupilDiameter;
    IplImage * resized = cvCreateImage(cvSize(
                                  static_cast<int>(pSrc->width*scale),
                                  static_cast<int>(pSrc->height*scale)),
                                  pSrc->depth, 1);
    cvResize(pSrc, resized);

    // Rescale sizes
    maxPupilDiameter = static_cast<int>(maxPupilDiameter * scale);
    minPupilDiameter = static_cast<int>(minPupilDiameter * scale);

    // Make sizes odd
    maxPupilDiameter += (maxPupilDiameter % 2) ? 0 : +1;
    minPupilDiameter += (minPupilDiameter % 2) ? 0 : -1;

    // Fill holes
    IplImage * filled = cvCreateImage(cvGetSize(resized), resized->depth, 1);
    fillWhiteHoles(resized, filled);

    // Gradients in horizontal direction
    IplImage * gh = cvCreateImage(cvGetSize(filled), IPL_DEPTH_32F, 1);
    cvSobel(filled, gh, 1, 0);

    // Gradients in vertical direction
    IplImage * gv = cvCreateImage(cvGetSize(filled), IPL_DEPTH_32F, 1);
    cvSobel(filled, gv, 0, 1);

    // Normalize gradients
    IplImage * gh2 = cvCreateImage(cvGetSize(filled), IPL_DEPTH_32F, 1);
    cvMul(gh, gh, gh2);
    IplImage * gv2 = cvCreateImage(cvGetSize(filled), IPL_DEPTH_32F, 1);
    cvMul(gv, gv, gv2);
    IplImage * gn = cvCreateImage(cvGetSize(filled), IPL_DEPTH_32F, 1);
    cvAdd(gh2, gv2, gn);
    cvPow(gn, gn, 0.5);
    cvDiv(gh, gn, gh);
    cvDiv(gv, gn, gv);

    // Create the filters fh and fv
    int filter_size = maxPupilDiameter;
    filter_size += (filter_size % 2) ? 0 : -1;
    CvMat * fh = cvCreateMat(filter_size, filter_size, CV_32FC1);
    CvMat * fv = cvCreateMat(filter_size, filter_size, CV_32FC1);

    for (int i = 0; i < fh->rows; i++) {
      float x = static_cast<float>(i) - (filter_size-1)/2;

      for (int j = 0; j < fh->cols; j++) {
        float y = static_cast<float>(j) - (filter_size-1)/2;
        if (x != 0 || y != 0) {
          (fh->data.fl)[i*fh->cols+j] = y / sqrt(x*x+y*y);
          (fv->data.fl)[i*fv->cols+j] = x / sqrt(x*x+y*y);
        } else {
          (fh->data.fl)[i*fh->cols+j] = 0;
          (fv->data.fl)[i*fv->cols+j] = 0;
        }
      }
    }

    // Create the mask
    CvMat * mask = cvCreateMat(filter_size, filter_size, CV_8UC1);

    // Temporary matrix for masking the filter
    CvMat * temp_filter = cvCreateMat(filter_size, filter_size, CV_32FC1);

    double old_max_val = 0;

    // Multi resolution of radius
    for (int r = (OSI_SMALLEST_PUPIL-1)/2; r < (maxPupilDiameter-1)/2; r++) {
      // Centred ring with radius = r and width = 2
      cvZero(mask);
      cvCircle(mask, cvPoint((filter_size-1)/2, (filter_size-1)/2),
               r, cvScalar(1), 2);

      // Fh * Gh
      cvZero(temp_filter);
      cvCopy(fh, temp_filter, mask);
      cvFilter2D(gh, gh2, temp_filter);

      // Fv * Gv
      cvZero(temp_filter);
      cvCopy(fv, temp_filter, mask);
      cvFilter2D(gv, gv2, temp_filter);

      // Fh*Gh + Fv*Gv
      cvAdd(gh2, gv2, gn);
      cvScale(gn, gn, 1.0/cvSum(mask).val[0]);

      // Sum in the disk-shaped neighbourhood
      cvZero(mask);
      cvCircle(mask, cvPoint((filter_size-1)/2, (filter_size-1)/2),
                             r, cvScalar(1), -1);
      cvFilter2D(filled, gh2, mask);
      cvScale(gh2, gh2, -1.0/cvSum(mask).val[0]/255.0, 1);

      // Add the two features : contour + darkness
      cvAdd(gn, gh2, gn);

      // Find the maximum in feature image
      double max_val;
      CvPoint max_loc;
      cvMinMaxLoc(gn, 0, &max_val, 0, &max_loc);

      if (max_val > old_max_val) {
        old_max_val = max_val;
        rPupil.setCircle(max_loc, r);
      }
    }

    // Rescale circle
    int x = static_cast<int>(
        (static_cast<float>(rPupil.getCenter().x * (pSrc->width-1))) /
        (filled->width-1) +
        static_cast<float>((1.0/scale)-1)/2);
    int y = static_cast<int>(
        (static_cast<float>(
          rPupil.getCenter().y * (pSrc->height-1))) / (filled->height-1) +
        static_cast<float>((1.0/scale)-1)/2);
    int r = static_cast<int>(rPupil.getRadius() / scale);
    rPupil.setCircle(x, y, r);

    // Release memory
    cvReleaseImage(&resized);
    cvReleaseImage(&filled);
    cvReleaseImage(&gh);
    cvReleaseImage(&gv);
    cvReleaseImage(&gh2);
    cvReleaseImage(&gv2);
    cvReleaseImage(&gn);
    cvReleaseMat(&fh);
    cvReleaseMat(&fv);
    cvReleaseMat(&mask);
    cvReleaseMat(&temp_filter);
  }  // end of function

  // Morphological reconstruction
  void EOC2Processings::reconstructMarkerByMask(const IplImage * pMarker,
                                                const IplImage * pMask,
                                                IplImage * pDst) {
    // Temporary image that will inform about marker evolution
    IplImage * difference = cvCloneImage(pMask);

    // :WARN: if user calls f(x, y, y) instead of f(x, y, z), the mask MUST
    // be cloned before processing
    IplImage * mask = cvCloneImage(pMask);

    // Copy the marker
    cvCopy(pMarker, pDst);

    // Structuring element for morphological operation
    IplConvKernel * structuring_element =
        cvCreateStructuringElementEx(3, 3, 1, 1, CV_SHAPE_ELLIPSE);

    // Will stop when marker does not change anymore
    while (cvSum(difference).val[0]) {
      // Remind marker before processing, in order to
      // compare with the marker after processing
      cvCopy(pDst, difference);

      // Dilate the marker
      cvDilate(pDst, pDst, structuring_element);

      // Keep the minimum between marker and mask
      cvMin(pDst, mask, pDst);

      // Evolution of the marker
      cvAbsDiff(pDst, difference, difference);
    }

    // Release memory
    cvReleaseImage(&mask);
    cvReleaseImage(&difference);
    cvReleaseStructuringElement(&structuring_element);
  }  // end of function

  // Fill the white holes surrounded by dark pixels, such as specular reflection
  // inside pupil area
  void EOC2Processings::fillWhiteHoles(const IplImage * pSrc,
                                       IplImage * pDst) {
    int width , height;
    if (pSrc->roi) {
      width = pSrc->roi->width;
      height = pSrc->roi->height;
    } else {
      width = pSrc->width;
      height = pSrc->height;
    }

    // Mask for reconstruction : pSrc + borders=0
    IplImage * mask = cvCreateImage(cvSize(width+2, height+2), pSrc->depth, 1);
    cvZero(mask);
    cvSetImageROI(mask, cvRect(1, 1, width, height));
    cvCopy(pSrc, mask);
    cvResetImageROI(mask);

    // Marker for reconstruction : all=0 + borders=255
    IplImage * marker = cvCloneImage(mask);
    cvZero(marker);
    cvRectangle(marker, cvPoint(1, 1), cvPoint(width+1, height+1),
        cvScalar(255));

    // Temporary result of reconstruction
    IplImage * result = cvCloneImage(mask);

    // Morphological reconstruction
    reconstructMarkerByMask(marker, mask, result);

    // Remove borders
    cvSetImageROI(result, cvRect(1, 1, width, height));
    cvCopy(result, pDst);

    // Release memory
    cvReleaseImage(&marker);
    cvReleaseImage(&mask);
    cvReleaseImage(&result);
  }  // end of function

  // Rescale between 0 and 255, and show image
  void EOC2Processings::showImage(const IplImage *pImage,
                                  int delay,
                                  const string &rWindowName) {
    IplImage * show;

    if (pImage->nChannels == 1) {
      // Rescale between 0 and 255 by computing : (X-min)/(max-min)
      double min_val , max_val;
      cvMinMaxLoc(pImage, &min_val, &max_val);
      IplImage * scaled = cvCloneImage(pImage);
      cvScale(pImage, scaled, 255/(max_val-min_val),
          -min_val/(max_val-min_val));

      // Convert into 8-bit
      show = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);
      cvConvert(scaled, show);

      // Release memory
      cvReleaseImage(&scaled);
    } else {
      show = cvCloneImage(pImage);
    }

    // Show image
    cvShowImage(rWindowName.c_str(), show);
    cvWaitKey(delay);

    // Release image
    cvReleaseImage(&show);
  }

  // Unwrap a ring into a rectangular band
  IplImage * EOC2Processings::unwrapRing(const IplImage *pSrc,
                                         const CvPoint &rCenter,
                                         int minRadius,
                                         int maxRadius,
                                         const vector<float> &rTheta) {
    // Result image
    IplImage * result = cvCreateImage(cvSize(
                      rTheta.size(), maxRadius-minRadius+1), pSrc->depth, 1);
    cvZero(result);

    // Loop on columns of normalized image
    for (int j = 0; j < result->width; j++) {
      // Loop on lines of normalized image
      for (int i = 0; i < result->height; i++) {
        CvPoint point = convertPolarToCartesian(rCenter,
                                                minRadius+i, rTheta[j]);

        // Do not exceed image size
        if (point.x >= 0 && point.x < pSrc->width &&
              point.y >= 0 && point.y < pSrc->height) {
          ((uchar *)(result->imageData+i*result->widthStep))[j] =
              ((uchar *)(pSrc->imageData+point.y*pSrc->widthStep))[point.x];
        }
      }
    }
    return result;
  }

  // Smooth the image by anisotropic smoothing (Gross & Brajovic, 2003)
  void EOC2Processings::processAnisotropicSmoothing(const IplImage * pSrc ,
                                                    IplImage * pDst ,
                                                    int iterations ,
                                                    float lambda) {
    // Temporary float images
    IplImage * tfs = cvCreateImage(cvGetSize(pSrc), IPL_DEPTH_32F, 1);
    cvConvert(pSrc, tfs);
    IplImage * tfd = cvCreateImage(cvGetSize(pSrc), IPL_DEPTH_32F, 1);
    cvConvert(pSrc, tfd);

    // Make borders dark
    cvRectangle(tfd, cvPoint(0, 0), cvPoint(tfd->width-1,
        tfd->height-1), cvScalar(0));

    // Weber coefficients
    float rhon , rhos , rhoe , rhow;

    // Store pixel values
    float tfsc , tfsn , tfss , tfse , tfsw , tfdn , tfds , tfde , tfdw;

    // Loop on iterations
    for (int k = 0; k < iterations; k++) {
      // Odd pixels
      for (int i = 1; i < tfs->height-1; i++) {
        for (int j = 2-i%2; j < tfs->width-1; j = j + 2) {
          // Get pixels in neighbourhood of original image
          tfsc = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j];
          tfsn = (reinterpret_cast<float*>(
              tfs->imageData + (i-1) * tfs->widthStep))[j];
          tfss = (reinterpret_cast<float*>(
              tfs->imageData + (i+1) * tfs->widthStep))[j];
          tfse = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j-1];
          tfsw = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j+1];

          // Get pixels in neighbourhood of light image
          tfdn = (reinterpret_cast<float*>(
              tfd->imageData + (i-1) * tfd->widthStep))[j];
          tfds = (reinterpret_cast<float*>(
             tfd->imageData + (i+1) * tfd->widthStep))[j];
          tfde = (reinterpret_cast<float*>(
              tfd->imageData + i * tfd->widthStep))[j-1];
          tfdw = (reinterpret_cast<float*>(
              tfd->imageData + i * tfd->widthStep))[j+1];

          // Compute weber coefficients
          rhon = min(tfsn, tfsc) / max<float>(1.0, abs(tfsn-tfsc));
          rhos = min(tfss, tfsc) / max<float>(1.0, abs(tfss-tfsc));
          rhoe = min(tfse, tfsc) / max<float>(1.0, abs(tfse-tfsc));
          rhow = min(tfsw, tfsc) / max<float>(1.0, abs(tfsw-tfsc));

          // Compute LightImage(i, j)
          (reinterpret_cast<float*>(tfd->imageData+i*tfd->widthStep))[j] =
              ((tfsc + lambda *
              (rhon * tfdn + rhos * tfds + rhoe * tfde + rhow * tfdw)) /
              (1 + lambda * (rhon + rhos + rhoe + rhow)));
        }
      }

      cvCopy(tfd, tfs);

      // Even pixels
      for (int i = 1; i < tfs->height-1; i++) {
        for (int j = 1+i%2; j < tfs->width-1; j = j + 2) {
          // Get pixels in neighbourhood of original image
          tfsc = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j];
          tfsn = (reinterpret_cast<float*>(
              tfs->imageData + (i-1) * tfs->widthStep))[j];
          tfss = (reinterpret_cast<float*>(
              tfs->imageData + (i+1) * tfs->widthStep))[j];
          tfse = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j-1];
          tfsw = (reinterpret_cast<float*>(
              tfs->imageData + i * tfs->widthStep))[j+1];

          // Get pixels in neighbourhood of light image
          tfdn = (reinterpret_cast<float*>(
              tfd->imageData + (i-1) * tfd->widthStep))[j];
          tfds = (reinterpret_cast<float*>(
              tfd->imageData + (i+1) * tfd->widthStep))[j];
          tfde = (reinterpret_cast<float*>(
              tfd->imageData + i * tfd->widthStep))[j-1];
          tfdw = (reinterpret_cast<float*>(
              tfd->imageData + i * tfd->widthStep))[j+1];

          // Compute weber coefficients
          rhon = min(tfsn, tfsc) / max<float>(1.0, abs(tfsn-tfsc));
          rhos = min(tfss, tfsc) / max<float>(1.0, abs(tfss-tfsc));
          rhoe = min(tfse, tfsc) / max<float>(1.0, abs(tfse-tfsc));
          rhow = min(tfsw, tfsc) / max<float>(1.0, abs(tfsw-tfsc));

          // Compute LightImage(i, j)
          (reinterpret_cast<float*>(tfd->imageData+i*tfd->widthStep))[j] =
              ((tfsc + lambda *
              (rhon * tfdn + rhos * tfds + rhoe * tfde + rhow * tfdw))
              / (1 + lambda * (rhon + rhos + rhoe + rhow)));
        }
      }

      cvCopy(tfd, tfs);
      cvConvert(tfd, pDst);
    }  // end of iterations k

    // Borders of image
    for (int i = 0; i < tfd->height; i++) {
      ((uchar*)(pDst->imageData+i*pDst->widthStep))[0] =
        ((uchar*)(pDst->imageData+i*pDst->widthStep))[1];
      ((uchar*)(pDst->imageData+i*pDst->widthStep))[pDst->width-1] =
        ((uchar*)(pDst->imageData+i*pDst->widthStep))[pDst->width-2];
    }
    for (int j = 0; j < tfd->width; j++) {
      ((uchar*)(pDst->imageData))[j] =
        ((uchar*)(pDst->imageData+pDst->widthStep))[j];
      ((uchar*)(pDst->imageData+(pDst->height-1)*pDst->widthStep))[j] =
        ((uchar*)(pDst->imageData+(pDst->height-2)*pDst->widthStep))[j];
    }

    // Release memory
    cvReleaseImage(&tfs);
    cvReleaseImage(&tfd);
  }  // end of function

  // Compute vertical gradients using Sobel operator
  void EOC2Processings::computeVerticalGradients(const IplImage * pSrc,
                                                 IplImage * pDst) {
    // Float values for Sobel
    IplImage * result_sobel = cvCreateImage(cvGetSize(pSrc), IPL_DEPTH_32F, 1);

    // Sobel filter in vertical direction
    cvSobel(pSrc, result_sobel, 0, 1);

    // Remove negative edges, ie from white (top) to black (bottom)
    cvThreshold(result_sobel, result_sobel, 0, 0, CV_THRESH_TOZERO);

    // Convert into 8-bit
    double min , max;
    cvMinMaxLoc(result_sobel, &min, &max);
    cvConvertScale(result_sobel, pDst, 255/(max-min), -255*min/(max-min));

    // Release memory
    cvReleaseImage(&result_sobel);
  }  // end of function

  // Run viterbi algorithm on gradient (or probability) image and find optimal
  // path
  void EOC2Processings::runViterbi(const IplImage *pSrc,
                                   vector<int> &rOptimalPath) {
    // Initialize the output
    rOptimalPath.clear();
    rOptimalPath.resize(pSrc->width);

    // Initialize cost matrix to zero
    IplImage * cost = cvCreateImage(cvGetSize(pSrc), IPL_DEPTH_32F, 1);
    cvZero(cost);

    // Forward process : build the cost matrix
    for (int w = 0; w < pSrc->width; w++) {
      for (int h = 0; h < pSrc->height; h++) {
        // First column is same as source image
        if (w == 0) {
          (reinterpret_cast<float*>(cost->imageData+h*cost->widthStep))[w] =
            ((uchar*)(pSrc->imageData+h*pSrc->widthStep))[w];
         } else {
          // First line
          if (h == 0) {
            (reinterpret_cast<float*>(
                cost->imageData + h * cost->widthStep))[w] =
                max<float>((reinterpret_cast<float*>(
                    cost->imageData+(h) * cost->widthStep))[w - 1],
                    (reinterpret_cast<float*>(
                        cost->imageData+(h+1) * cost->widthStep))[w - 1]) +
                        (reinterpret_cast<uchar*>(
                            pSrc->imageData + h * pSrc->widthStep))[w];
          } else if (h == pSrc->height - 1) {
          // Last line
            (reinterpret_cast<float*>(
                cost->imageData + h * cost->widthStep))[w] =
                    max<float>((reinterpret_cast<float*>(
                        cost->imageData + h * cost->widthStep))[w-1],
                        (reinterpret_cast<float*>(
                            cost->imageData + (h - 1)*cost->widthStep))[w-1]) +
                            (reinterpret_cast<uchar*>(
                                pSrc->imageData + h * pSrc->widthStep))[w];
          } else {
          // Middle lines
            (reinterpret_cast<float*>(
                cost->imageData+h*cost->widthStep))[w] =
                max<float>((reinterpret_cast<float*>(
                    cost->imageData+h*cost->widthStep))[w-1],
                max<float>((reinterpret_cast<float*>(
                    cost->imageData+(h+1)*cost->widthStep))[w-1],
            (reinterpret_cast<float*>(
                cost->imageData+(h-1)*cost->widthStep))[w-1])) +
                (reinterpret_cast<uchar*>(
                    pSrc->imageData+h*pSrc->widthStep))[w];
          }
        }
      }
    }

    // Get the maximum in last column of cost matrix
    cvSetImageROI(cost, cvRect(cost->width-1, 0, 1, cost->height));
    CvPoint max_loc;
    cvMinMaxLoc(cost, 0, 0, 0, &max_loc);
    int h = max_loc.y;
    int h0 = h;
    cvResetImageROI(cost);

    // Store the point in output vector
    rOptimalPath[rOptimalPath.size()-1] = h0;

    float h1 , h2 , h3;

    // Backward process
    for (int w = rOptimalPath.size() - 2; w >= 0; w--) {
      // Constraint to close the contour
      if (h - h0 > w) {
        h--;
      } else if (h0 - h > w) {
        h++;
      } else {
        // When no need to constraint : use the cost matrix
        // h1 is the value above line h
        h1 = (h == 0) ? 0 : (reinterpret_cast<float*>(cost->imageData +
            (h - 1) * cost->widthStep))[w];

        // h2 is the value at line h
        h2 = (reinterpret_cast<float*>(cost->imageData +
            h * cost->widthStep))[w];

        // h3 is the value below line h
        h3 = (h == cost->height - 1) ? 0 :
            (reinterpret_cast<float*>(cost->imageData +
                (h + 1) * cost->widthStep))[w];

        // h1 is the maximum => h decreases
        if (h1 > h2 && h1 > h3)
          h--;

        // h3 is the maximum => h increases
        else if (h3 > h2 && h3 > h1)
          h++;
      }

      // Store the point in output contour
      rOptimalPath[w] = h;
    }

    // Release memory
    cvReleaseImage(&cost);
  }  // end of function

  // Find a contour in image using Viterbi algorithm and anisotropic smoothing
  vector<CvPoint> EOC2Processings::findContour(const IplImage *pSrc,
                                               const CvPoint &rCenter,
                                               const vector<float> &rTheta,
                                               int minRadius,
                                               int maxRadius,
                                               const IplImage *pMask) {
    // Output
    vector<CvPoint> contour;
    contour.resize(rTheta.size());

    // Unwrap the image
    IplImage * unwrapped = unwrapRing(pSrc, rCenter, minRadius, maxRadius,
                                      rTheta);

    // Smooth image
    processAnisotropicSmoothing(unwrapped, unwrapped, 100, 1);

    // Extract the gradients
    computeVerticalGradients(unwrapped, unwrapped);

    // Take into account the mask
    if (pMask) {
      IplImage * mask_unwrapped = unwrapRing(pMask, rCenter, minRadius,
                                             maxRadius, rTheta);
      IplImage * temp = cvCloneImage(unwrapped);
      cvZero(unwrapped);
      cvCopy(temp, unwrapped, mask_unwrapped);
      cvReleaseImage(&temp);
      cvReleaseImage(&mask_unwrapped);
    }

    // Find optimal path in unwrapped image
    vector<int> optimalPath;
    runViterbi(unwrapped, optimalPath);
    for (int i = 0; i < static_cast<int>(optimalPath.size()); i++) {
      contour[i] = convertPolarToCartesian(rCenter,
                                           minRadius+optimalPath[i],
                                           rTheta[i]);
    }

    // Release memory
    cvReleaseImage(&unwrapped);

    return contour;
  }  // end of function

  // Draw a contour (vector of CvPoint) on an image
  void EOC2Processings::drawContour(IplImage * pImage,
                                    const vector<CvPoint> &rContour,
                                    const CvScalar &rColor,
                                    int thickness) {
    // Draw INSIDE the contour if thickness is negative
    if (thickness < 0) {
      CvPoint * points = new CvPoint[rContour.size()];
      for (int i = 0; i < static_cast<int>(rContour.size()); i++) {
        points[i].x = rContour[i].x;
        points[i].y = rContour[i].y;
      }
      cvFillConvexPoly(pImage, points, rContour.size(), rColor);
      delete [] points;
      // Else draw the contour
    } else {
      // Draw the contour on binary mask
      IplImage * mask = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);
      cvZero(mask);
      for (int i = 0; i < static_cast<int>(rContour.size()); i++) {
        // Do not exceed image sizes
        int x = min(max(0, rContour[i].x), pImage->width);
        int y = min(max(0, rContour[i].y), pImage->height);

        // Plot the point on image
        ((uchar *)(mask->imageData + y*mask->widthStep))[x] = 255;
      }

      // Dilate mask if user specified thickness
      if (thickness > 1) {
        IplConvKernel * se = cvCreateStructuringElementEx(3, 3, 1, 1,
            CV_SHAPE_ELLIPSE);
        cvDilate(mask, mask, se, thickness-1);
        cvReleaseStructuringElement(&se);
      }

      // Color rgb
      cvSet(pImage, rColor, mask);

      // Release memory
      cvReleaseImage(&mask);
    }
  }  // end of function
}  // end of namespace


