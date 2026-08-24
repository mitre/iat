/*****************************************************************************
 * Eye Orientation CLassifier v2 (U/D + L/R)
 * Author: Adam Czajka, aczajka@nd.edu, May 2016
 *
 * Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
 * under BSD license http://opensource.org/licenses/bsd-license.php
 ******************************************************************************/

#include "EOC2Eye.h"
#include "EOC2Processings.h"
#include <opencv/cv.h>
#include <log4cxx/logger.h>
#include <string>
#include <algorithm>
#include <fstream>
#include <stdexcept>


using cv::BORDER_REFLECT;
using cv::INTER_CUBIC;
using cv::Mat;
using cv::Rect;
using cv::Scalar;
using cv::Scharr;
using cv::meanStdDev;
using cv::resize;
#ifdef EOC2_DEBUG
using std::cout;
#endif
using std::endl;
using std::exception;
using std::max;
using std::min;
using std::runtime_error;
using std::string;
using org::mitre::iwp::buffers::ImageServiceQuery;
#ifdef EOC2_DEBUG
using std::ofstream;
#endif

const extern log4cxx::LoggerPtr logger;

namespace EOC2 {
  EOC2Eye::EOC2Eye(int image_type, int image_width, int image_height) {
    imageType = image_type;
    imageWidth = image_width;
    imageHeight = image_height;
    mpOriginalImage = 0;
    mpMask = 0;
    mpNormalizedImage = 0;
    mpNormalizedMask = 0;
    mPupil.setCircle(0, 0, 0);
    mIris.setCircle(0, 0, 0);
    iNumberOfUDfeatures = 6;
    iNumberOfLRfeatures = 2;

    bProcessedOK = false;

    // The following variable is needed
    // if one needs to save the debug data - segmented iris image
    // (not used in the final version)
#ifdef EOC2_DEBUG
    mpSegmentedImage = 0;
#endif
  }

  // Default constructor
  EOC2Eye::~EOC2Eye() {
    cvReleaseImage(&mpOriginalImage);
    cvReleaseImage(&mpMask);
    cvReleaseImage(&mpNormalizedImage);
    cvReleaseImage(&mpNormalizedMask);

    // The following deallocation is needed
    // if one needs to save the debug data - segmented iris image
    // (not used in the final version)
#ifdef EOC2_DEBUG
    cvReleaseImage(&mpSegmentedImage);
#endif
  }


  /////////////////////////////////////////////////////////
  // Feature extraction

  void EOC2Eye::featuresUD() {
    /////////////////////////////////////////////////////////
    // Vertical and horizontal shifts

    stFeaturesUD.fVerticalPupilShift = mPupil.getCenterFloat().y -
        mIris.getCenterFloat().y;



    /////////////////////////////////////////////////////////
    // Eyelid coverage

    int w, h;
    float upperEyelidNotCovered = 0.0, lowerEyelidNotCovered = 0.0;
    int rWidthOfNormalizedIris = mpNormalizedMask->width;
    int rHeightOfNormalizedIris = mpNormalizedMask->height;

    for (w = static_cast<int>(round(rWidthOfNormalizedIris / 8));
        w < static_cast<int>(round(3 * rWidthOfNormalizedIris / 8)); ++w) {
      for (h = static_cast<int>(round(rHeightOfNormalizedIris / 2));
          h < rHeightOfNormalizedIris; ++h) {
        upperEyelidNotCovered += static_cast<float>(
            reinterpret_cast<uchar*>(mpNormalizedMask->imageData +
            h*rWidthOfNormalizedIris)[w]);
      }
    }

    for (w = static_cast<int>(round(5 * rWidthOfNormalizedIris / 8));
        w < static_cast<int>(round(7 * rWidthOfNormalizedIris / 8)); ++w) {
      for (h = static_cast<int>(round(rHeightOfNormalizedIris / 2));
          h < rHeightOfNormalizedIris; ++h) {
        lowerEyelidNotCovered += static_cast<float>(
            reinterpret_cast<uchar*>(mpNormalizedMask->imageData +
            h*rWidthOfNormalizedIris)[w]);
      }
    }

    stFeaturesUD.fEyelidCoverage =
      (lowerEyelidNotCovered - upperEyelidNotCovered) /
          ((255 * static_cast<float>(rWidthOfNormalizedIris) *
          static_cast<float>(rHeightOfNormalizedIris)) / 8);



    /////////////////////////////////////////////////////////
    // Eyelash value for upright / upside-down

    float upperEyelidValue, lowerEyelidValue, fElts;
    int rWidthOfOriginalIris = mpOriginalImage->width;
    int rHeightOfOriginalIris = mpOriginalImage->height;
    int hStart, hStop, wStart, wStop;
    float fWidthV = 1.4f;
    float fHeightV = 0.2f;

    // upper eyelid
    hStart = static_cast<int>(max<float>(0.0, mPupil.getCenterFloat().y -
        mPupil.getRadiusFloat() - roundf(fHeightV*mIris.getRadiusFloat())));
    hStop = static_cast<int>(max<float>(0.0, mPupil.getCenterFloat().y -
        mPupil.getRadiusFloat()));
    wStart = static_cast<int>(max<float>(0.0, mPupil.getCenterFloat().x -
        roundf(fWidthV*mIris.getRadiusFloat())));
    wStop = static_cast<int>(min<float>(
        static_cast<float>(rWidthOfOriginalIris),
        mPupil.getCenterFloat().x + roundf(fWidthV*mIris.getRadiusFloat())));

    fElts = 0.0;
    upperEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w) {
      for (h = hStart; h < hStop; ++h) {
        upperEyelidValue += static_cast<float>(
            reinterpret_cast<uchar*>(mpOriginalImage->imageData +
            h*rWidthOfOriginalIris)[w]);
        fElts += 1.0;
      }
    }
    if (fElts > 0) {
      upperEyelidValue /= fElts;
    } else {
      throw runtime_error("Not enough data to calculate EINV feature");
    }

    // lower eyelid
    hStart = static_cast<int>(min<float>(
        static_cast<float>(rHeightOfOriginalIris),
        mPupil.getCenterFloat().y + mPupil.getRadiusFloat()));
    hStop = static_cast<int>(min<float>(
        static_cast<float>(rHeightOfOriginalIris),
        mPupil.getCenterFloat().y + mPupil.getRadiusFloat() +
        roundf(fHeightV*mIris.getRadiusFloat())));

    fElts = 0.0;
    lowerEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w)
      for (h = hStart; h < hStop; ++h) {
        lowerEyelidValue += static_cast<float>((
        reinterpret_cast<uchar*>(mpOriginalImage->imageData +
                                 h*rWidthOfOriginalIris))[w]);
        fElts += 1.0;
      }
    if (fElts > 0)
      lowerEyelidValue /= fElts;
    else
      throw runtime_error("Not enough data to calculate EINV feature");

    stFeaturesUD.fEyelidValue = upperEyelidValue - lowerEyelidValue;



    /////////////////////////////////////////////////////////
    // Features based on fitered eyelash regions

    // global parameters and variables
    fWidthV = 1.1f;
    float fScale = 0.45f;
    Mat upperEyelashScaled, upperEyelashScaledDx, upperEyelashScaledDy,
        lowerEyelashScaled, lowerEyelashScaledDx, lowerEyelashScaledDy;

    // process upper eyelash region

    // cut appropriate portions of the eyelash areas
    hStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().y - 2 * mIris.getRadiusFloat()));
    hStop = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().y - mPupil.getRadiusFloat()));
    wStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x - roundf(fWidthV*mIris.getRadiusFloat())));
    wStop = static_cast<int>(min<float>(
        static_cast<float>(rWidthOfOriginalIris),
        mPupil.getCenterFloat().x + roundf(fWidthV*mIris.getRadiusFloat())));

    if ((wStop - wStart <= 0) || (hStop - hStart) <= 0) {
      throw runtime_error(
          "Not enough data to calculate Scharr-filter-based features");
    }

    Mat upperEyelash(mpOriginalImage, Rect(wStart, hStart,
      wStop - wStart, hStop - hStart));

    // scale selected regions
    resize(upperEyelash, upperEyelashScaled, cvSize(0, 0), fScale, fScale,
      INTER_CUBIC);

    // estimate the vertical and horizontal gradients using Scharr filters
    Scharr(upperEyelashScaled, upperEyelashScaledDx, CV_32F, 1, 0, 1.0, 0.0,
        BORDER_REFLECT);
    Scharr(upperEyelashScaled, upperEyelashScaledDy, CV_32F, 0, 1, 1.0, 0.0,
        BORDER_REFLECT);

    // Since Scharr calculates correlation (NOT convolution) we need to process
    // negative results (conv = -corr)
    upperEyelashScaledDx = -upperEyelashScaledDx;
    upperEyelashScaledDy = -upperEyelashScaledDy;


    // process lower eyelash region

    // cut appropriate portions of the eyelash areas
    hStart = static_cast<int>(min<float>(
        static_cast<float>(rHeightOfOriginalIris),
        mPupil.getCenterFloat().y + mPupil.getRadiusFloat()));
    hStop = static_cast<int>(min<float>(
        static_cast<float>(rHeightOfOriginalIris),
        mPupil.getCenterFloat().y + 2 * mIris.getRadiusFloat()));

    if ((wStop - wStart <= 0) || (hStop - hStart) <= 0) {
      throw runtime_error(
          "Not enough data to calculate Scharr-filter-based features");
    }

    Mat lowerEyelash(mpOriginalImage, Rect(wStart, hStart,
        wStop - wStart, hStop - hStart));

    // scale selected regions
    resize(lowerEyelash, lowerEyelashScaled, cvSize(0, 0), fScale, fScale,
        INTER_CUBIC);

    // estimate the vertical and horizontal gradients using Scharr filters
    // void Scharr(InputArray src, OutputArray dst, int ddepth, int dx, int dy,
    //    double scale=1, double delta=0, int borderType=BORDER_DEFAULT)
    Scharr(lowerEyelashScaled, lowerEyelashScaledDx, CV_32F, 1, 0, 1.0, 0.0,
        BORDER_REFLECT);
    Scharr(lowerEyelashScaled, lowerEyelashScaledDy, CV_32F, 0, 1, 1.0, 0.0,
        BORDER_REFLECT);

    // Since Scharr calculates correlation (NOT convolution) we need to process
    // negative results (conv = -corr)
    lowerEyelashScaledDx = -lowerEyelashScaledDx;
    lowerEyelashScaledDy = -lowerEyelashScaledDy;


    // calculate the final gradient magnitude and angles

    // upper eyelid
    CvSize sizeU = upperEyelashScaledDx.size();
    Mat fuMagMat(sizeU, CV_32F);
    Mat fuCosMat(sizeU, CV_32F), fuSinMat(sizeU, CV_32F);
    Scalar fuMagMean, fuMagStd, fuCosMean, fuCosStd, fuSinMean, fuSinStd;
    float fAngleTemp;

    for (w = 0; w < sizeU.width; ++w) {
      for (h = 0; h < sizeU.height; ++h) {
        fuMagMat.at<float>(h, w) = sqrt(upperEyelashScaledDx.at<float>(h, w) *
            upperEyelashScaledDx.at<float>(h, w) +
            upperEyelashScaledDy.at<float>(h, w) *
            upperEyelashScaledDy.at<float>(h, w));
        fAngleTemp = atan2(upperEyelashScaledDy.at<float>(h, w),
            upperEyelashScaledDx.at<float>(h, w));

        if (fAngleTemp < 0) {
          fuCosMat.at<float>(h, w) = cos(fAngleTemp + 2 * OSI_PI);
          fuSinMat.at<float>(h, w) = sin(fAngleTemp + 2 * OSI_PI);
        } else {
          fuCosMat.at<float>(h, w) = cos(fAngleTemp);
          fuSinMat.at<float>(h, w) = sin(fAngleTemp);
        }
      }
    }
    meanStdDev(fuMagMat, fuMagMean, fuMagStd);
    meanStdDev(fuCosMat, fuCosMean, fuCosStd);
    meanStdDev(fuSinMat, fuSinMean, fuSinStd);

    // lower eyelid
    CvSize sizeL = lowerEyelashScaledDx.size();
    CvSize sizeL2 = lowerEyelashScaledDy.size();

    Mat flMagMat(sizeL, CV_32F);
    Mat flCosMat(sizeL, CV_32F), flSinMat(sizeL, CV_32F);
    Scalar flMagMean, flMagStd, flCosMean, flCosStd, flSinMean, flSinStd;

    for (w = 0; w < sizeL.width; ++w) {
      for (h = 0; h < sizeL.height; ++h) {
        flMagMat.at<float>(h, w) = sqrt(
            lowerEyelashScaledDx.at<float>(h, w) *
            lowerEyelashScaledDx.at<float>(h, w) +
            lowerEyelashScaledDy.at<float>(h, w) *
            lowerEyelashScaledDy.at<float>(h, w));
        fAngleTemp = atan2(lowerEyelashScaledDy.at<float>(h, w),
            lowerEyelashScaledDx.at<float>(h, w));
        if (fAngleTemp < 0) {
          flCosMat.at<float>(h, w) = cos(fAngleTemp + 2 * OSI_PI);
          flSinMat.at<float>(h, w) = sin(fAngleTemp + 2 * OSI_PI);
        } else {
          flCosMat.at<float>(h, w) = cos(fAngleTemp);
          flSinMat.at<float>(h, w) = sin(fAngleTemp);
        }
      }
    }
    meanStdDev(flMagMat, flMagMean, flMagStd);
    meanStdDev(flCosMat, flCosMean, flCosStd);
    meanStdDev(flSinMat, flSinMean, flSinStd);

    stFeaturesUD.fEyelidFiltered =
      static_cast<float>(fuMagMean.val[0] - flMagMean.val[0]);
    stFeaturesUD.fEyelidFilteredCosVar =
        static_cast<float>(((fuCosStd.val[0]) * (fuCosStd.val[0]) -
        (flCosStd.val[0]) * (flCosStd.val[0])));
    stFeaturesUD.fEyelidFilteredSinVar =
        static_cast<float>(((fuSinStd.val[0]) * (fuSinStd.val[0]) -
        (flSinStd.val[0]) * (flSinStd.val[0])));

    // Release memory
    upperEyelash.release();
    upperEyelashScaled.release();
    upperEyelashScaledDx.release();
    upperEyelashScaledDy.release();
    lowerEyelash.release();
    lowerEyelashScaled.release();
    lowerEyelashScaledDx.release();
    lowerEyelashScaledDy.release();;
    fuMagMat.release();
    fuCosMat.release();
    fuSinMat.release();
    flMagMat.release();
    flCosMat.release();
    flSinMat.release();
  }



  void EOC2Eye::featuresLR() {
    /////////////////////////////////////////////////////////
    // Vertical and horizontal shifts

    stFeaturesLR.fHorizontalPupilShift =
        mPupil.getCenterFloat().x - mIris.getCenterFloat().x;


    /////////////////////////////////////////////////////////
    // Eyelash value for recognition of left / right

    int hStart, hStop, wStart, wStop, w, h;
    int rWidthOfOriginalIris = mpOriginalImage->width;
    int rHeightOfOriginalIris = mpOriginalImage->height;
    float leftUpperEyelidValue, leftLowerEyelidValue, rightUpperEyelidValue;
    float rightLowerEyelidValue, fElts;

    // left upper eyelash region
    hStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().y - mIris.getRadiusFloat() / 2));
    hStop = static_cast<int>(max<float>(0.0, mPupil.getCenterFloat().y));
    wStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x - mPupil.getRadiusFloat() - 2 *
        mIris.getRadiusFloat()));
    wStop = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x - mPupil.getRadiusFloat() -
        mIris.getRadiusFloat()));

    fElts = 0.0;
    leftUpperEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w) {
      for (h = hStart; h < hStop; ++h) {
        leftUpperEyelidValue += static_cast<float>(
            reinterpret_cast<uchar*>(mpOriginalImage->imageData +
                                     h*rWidthOfOriginalIris)[w]);
        fElts += 1.0;
      }
    }
    if (fElts > 0) {
      leftUpperEyelidValue /= fElts;
    } else {
      throw runtime_error("Not enough data to calculate EINH feature");
    }

    // right upper eyelash region
    wStart = static_cast<int>(min<float>(
        static_cast<float>(rWidthOfOriginalIris),
        mPupil.getCenterFloat().x + mPupil.getRadiusFloat() +
        mIris.getRadiusFloat()));
    wStop = static_cast<int>(min<float>(
        static_cast<float>(rWidthOfOriginalIris),
        mPupil.getCenterFloat().x + mPupil.getRadiusFloat() +
        2 * mIris.getRadiusFloat()));

    fElts = 0.0;
    rightUpperEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w) {
      for (h = hStart; h < hStop; ++h) {
        rightUpperEyelidValue += static_cast<float>(
            reinterpret_cast<uchar*>(mpOriginalImage->imageData +
                                     h*rWidthOfOriginalIris)[w]);
        fElts += 1.0;
      }
    }
    if (fElts > 0) {
      rightUpperEyelidValue /= fElts;
     } else {
      throw runtime_error("Not enough data to calculate EINH feature");
    }


    // left lower eyelash region
    hStart = static_cast<int>(max<float>(0.0, mPupil.getCenterFloat().y));
    hStop = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().y + mIris.getRadiusFloat() / 2));
    wStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x - mPupil.getRadiusFloat() -
        2 * mIris.getRadiusFloat()));
    wStop = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x - mPupil.getRadiusFloat() -
        mIris.getRadiusFloat()));

    fElts = 0.0;
    leftLowerEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w) {
      for (h = hStart; h < hStop; ++h) {
        leftLowerEyelidValue += static_cast<float>(
            reinterpret_cast<uchar*>(mpOriginalImage->imageData +
                                     h*rWidthOfOriginalIris)[w]);
        fElts += 1.0;
      }
    }
    if (fElts > 0) {
      leftLowerEyelidValue /= fElts;
    } else {
      throw runtime_error("Not enough data to calculate EINH feature");
    }


    // right lower eyelash region
    wStart = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x + mPupil.getRadiusFloat() +
        mIris.getRadiusFloat()));
    wStop = static_cast<int>(max<float>(0.0,
        mPupil.getCenterFloat().x + mPupil.getRadiusFloat() +
        2 * mIris.getRadiusFloat()));

    fElts = 0.0;
    rightLowerEyelidValue = 0.0;
    for (w = wStart; w < wStop; ++w) {
      for (h = hStart; h < hStop; ++h) {
        rightLowerEyelidValue += static_cast<float>(
            reinterpret_cast<uchar*>(mpOriginalImage->imageData +
                                     h*rWidthOfOriginalIris)[w]);
        fElts += 1.0;
      }
    }
    if (fElts) {
      rightLowerEyelidValue /= fElts;
    } else {
      throw runtime_error("Not enough data to calculate EINH feature");
    }

    stFeaturesLR.fEyelidValue = min<float>(leftUpperEyelidValue,
        leftLowerEyelidValue) - min<float>(rightUpperEyelidValue,
        rightLowerEyelidValue);
  }


#ifdef EOC2_DEBUG
  void EOC2Eye::printFeaturesUD(int &iFileNo, const string &rFilename,
      const string &label) {
    cout.precision(6);
    cout.setf(std::ios::fixed, std::ios::floatfield);
    cout << iFileNo << "\t" << rFilename << "\t\t"
      << label << "\t\t"
      << stFeaturesUD.fEyelidCoverage << "\t"
      << stFeaturesUD.fVerticalPupilShift << "\t"
      << stFeaturesUD.fEyelidValue << "\t"
      << stFeaturesUD.fEyelidFiltered << "\t"
      << stFeaturesUD.fEyelidFilteredSinVar << "\t"
      << stFeaturesUD.fEyelidFilteredCosVar << endl;
  }

  void EOC2Eye::printFeaturesUDHeader() {
    cout << "FileNo\tFilename\t\tLabel\t\tEOCC\t\tPIVO\t\tEINV\t\tGMAG\t\t"
        "SIND\t\tCOSD" << endl;
  }


  void EOC2Eye::printFeaturesLR(int &iFileNo, const string &rFilename,
      const string &label) {
    cout.precision(6);
    cout.setf(std::ios::fixed, std::ios::floatfield);
    cout << iFileNo << "\t" << rFilename << "\t\t"
      << label << "\t\t"
      << stFeaturesLR.fEyelidValue << "\t"
      << stFeaturesLR.fHorizontalPupilShift << endl;
  }

  void EOC2Eye::printFeaturesLRHeader() {
    cout << "FileNo\tFilename\t\tLabel\t\tPIHO\t\tEINH" << endl;
  }
#endif

  int EOC2Eye::getNumberOfLRfeatures() { return iNumberOfLRfeatures; }

  int EOC2Eye::getNumberOfUDfeatures() { return iNumberOfUDfeatures; }


  void EOC2Eye::loadImage(const string &rFilename, IplImage **ppImage) {
    try {
      if (*ppImage) {
        cvReleaseImage(ppImage);
      }

      *ppImage = cvLoadImage(rFilename.c_str(), 0);
      if (!*ppImage) {
        LOG4CXX_ERROR(logger, "Cannot load image : " << rFilename);
      }
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, e.what());
    }
  }

  void EOC2Eye::loadPixmap(const string &rFilename) {

    int channels;
    Mat imageMat;
    FILE *fp = fopen(rFilename.c_str(), "rb");
    if (!fp) {
        LOG4CXX_ERROR(logger, "Cannot load image : " << rFilename);
        return;
    }
    // Determine the number of channels
    fseek(fp, 0, SEEK_END);
    channels = ftell(fp) / imageHeight / imageWidth;
    fseek(fp, 0, SEEK_SET);
    if (channels == 3) {
      imageMat = Mat::zeros(imageHeight, imageWidth, CV_8UC3);
      fread(imageMat.data, 3, imageWidth * imageHeight, fp);
      IplImage *colorImage = new IplImage(imageMat);
      this->mpOriginalImage = cvCreateImage(cvGetSize(colorImage), IPL_DEPTH_8U,
          1);
      cvCvtColor(colorImage, this->mpOriginalImage, CV_RGB2GRAY);
      delete colorImage;
    } else {
      imageMat = Mat::zeros(imageHeight, imageWidth, CV_8UC1);
      fread(imageMat.data, 1, imageWidth * imageHeight, fp);
      // we need to create an image and clone it to prevent the underlying
      // matrix from being freed too early.
      IplImage tmpImage(imageMat);
      this->mpOriginalImage = cvCloneImage(&tmpImage);
    }
    fclose(fp);
  }

  void EOC2Eye::loadOriginalImage(const string &rFilename) {
    if (imageType == ImageServiceQuery::PIXMAP) {
      loadPixmap(rFilename);
    } else {
      loadImage(rFilename, &mpOriginalImage);
    }
  }



  void EOC2Eye::initMask() {
    if (mpMask) {
      cvReleaseImage(&mpMask);
    }
    if (!mpOriginalImage) {
      throw runtime_error(
          "Cannot initialize the mask because original image is not loaded");
    }
    mpMask = cvCreateImage(cvGetSize(mpOriginalImage), IPL_DEPTH_8U, 1);
    cvSet(mpMask, cvScalar(255));
  }



  void EOC2Eye::segment(int minIrisDiameter, int minPupilDiameter,
      int maxIrisDiameter, int maxPupilDiameter) {
    if (!mpOriginalImage) {
      throw runtime_error(
          "Cannot segment image because original image is not loaded");
    }

    // Initialize mask and segmented image
    mpMask = cvCreateImage(cvGetSize(mpOriginalImage), IPL_DEPTH_8U, 1);

    // Processing functions
    EOC2Processings op;

    // Segment the eye
    op.segment(mpOriginalImage,
               mpMask,
               mPupil,
               mIris,
               mThetaCoarsePupil,
               mThetaCoarseIris,
               mCoarsePupilContour,
               mCoarseIrisContour,
               minIrisDiameter,
               minPupilDiameter,
               maxIrisDiameter,
               maxPupilDiameter);

#ifdef EOC2_DEBUG
    // The following code is needed if one needs to save the debug data
    // and draw the iris segmentation results
       mpSegmentedImage = cvCreateImage(cvGetSize(mpOriginalImage),
          IPL_DEPTH_8U, 3);
       cvCvtColor(mpOriginalImage, mpSegmentedImage, CV_GRAY2BGR);
       IplImage * tmp = cvCloneImage(mpMask);
       cvZero(tmp);
       cvCircle(tmp, mIris.getCenter(), mIris.getRadius(), cvScalar(255), -1);
       cvCircle(tmp, mPupil.getCenter(), mPupil.getRadius(), cvScalar(0), -1);
       cvSub(tmp, mpMask, tmp);
       cvSet(mpSegmentedImage, cvScalar(0, 0, 255), tmp);
       cvReleaseImage(&tmp);
       cvCircle(mpSegmentedImage, mPupil.getCenter(), mPupil.getRadius(),
          cvScalar(0, 255, 0));
       cvCircle(mpSegmentedImage, mIris.getCenter(), mIris.getRadius(),
          cvScalar(0, 255, 0));
#endif
  }


  void EOC2Eye::normalize(int rWidthOfNormalizedIris,
                          int rHeightOfNormalizedIris) {
    // Processing functions
    EOC2Processings op;

    if (!mpOriginalImage) {
      throw runtime_error(
          "Cannot normalize image because original image is not loaded");
    }

    mpNormalizedImage = cvCreateImage(
        cvSize(rWidthOfNormalizedIris, rHeightOfNormalizedIris),
        IPL_DEPTH_8U, 1);

    if (mThetaCoarsePupil.empty() || mThetaCoarseIris.empty())
      throw runtime_error("Cannot normalize image because contours are not "
          "correctly computed/loaded");

    op.normalizeFromContour(mpOriginalImage,
                            mpNormalizedImage,
                            mPupil,
                            mIris,
                            mThetaCoarsePupil,
                            mThetaCoarseIris,
                            mCoarsePupilContour,
                            mCoarseIrisContour);

    if (!mpMask) {
      initMask();
    }

    mpNormalizedMask = cvCreateImage(cvSize(rWidthOfNormalizedIris,
        rHeightOfNormalizedIris), IPL_DEPTH_8U, 1);
    op.normalizeFromContour(mpMask,
                            mpNormalizedMask,
                            mPupil,
                            mIris,
                            mThetaCoarsePupil,
                            mThetaCoarseIris,
                            mCoarsePupilContour,
                            mCoarseIrisContour);
  }




  ///////////////////////////////////////////////////////////////////
  // Functions for saving debug data (not used in the final version)
  ///////////////////////////////////////////////////////////////////
#ifdef EOC2_DEBUG

  void EOC2Eye::saveImage(const string &rFilename, const IplImage * pImage) {
    // :TODO: no exception here, but 2 error messages
    // 1. pImage does NOT exist => "image was neither comptued nor loaded"
    // 2. cvSaveImage returns <=0 => "rFilename = invalid for saving"
    if (!pImage) {
      throw runtime_error("Cannot save image " + rFilename +
          " because this image is not built");
    }
    if (!cvSaveImage(rFilename.c_str(), pImage)) {
      LOG4CXX_ERROR(logger, "Cannot save image " << rFilename);
    }
  }

  void EOC2Eye::saveSegmentedImage(const string &rFilename) {
    saveImage(rFilename, mpSegmentedImage);
  }

  void EOC2Eye::saveMask(const string &Filename) {
    saveImage(rFilename, mpMask);
  }

  void EOC2Eye::saveNormalizedImage(const string &rFilename) {
    saveImage(rFilename, mpNormalizedImage);
  }

  void EOC2Eye::saveNormalizedMask(const string &rFilename) {
    saveImage(rFilename, mpNormalizedMask);
  }

  void EOC2Eye::saveParameters(const string &rFilename) {
    // Open the file
    ofstream file(rFilename.c_str(), ofstream::out);

    // If file is not opened
    if (!file) {
      throw runtime_error("Cannot save the parameters in " + rFilename);
    }

    try {
      file << mCoarsePupilContour.size() << endl;
      file << mCoarseIrisContour.size() << endl;
      for (int i = 0; i < static_cast<int>(mCoarsePupilContour.size()); i++) {
        file << mCoarsePupilContour[i].x << " ";
        file << mCoarsePupilContour[i].y << " ";
        file << mThetaCoarsePupil[i] << " ";
      }
      file << endl;
      for (int j = 0; j < static_cast<int>(mCoarseIrisContour.size()); j++) {
        file << mCoarseIrisContour[j].x << " ";
        file << mCoarseIrisContour[j].y << " ";
        file << mThetaCoarseIris[j] << " ";
      }
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, e.what());
      throw runtime_error("Error while saving parameters in " + rFilename);
    }

    // Close the file
    file.close();
  }
#endif


}  // namespace EOC2
