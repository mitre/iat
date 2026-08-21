/*****************************************************************************
 * Eye Orientation CLassifier v2 (U/D + L/R)
 * Author: Adam Czajka, aczajka@nd.edu, May 2016
 *
 * Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
 * under BSD license http://opensource.org/licenses/bsd-license.php
 ******************************************************************************/

#include "EOC2Manager.h"
#include "EOC2Utils.h"
#include <fstream>
#include <iterator>
#include <stdexcept>
#include <string>

using cv::FileStorage;
using cv::Mat;
using std::exception;
using std::ifstream;
using std::runtime_error;
using std::string;

namespace EOC2 {
  // Default constructor
  EOC2Manager::EOC2Manager(bool keep_class_in_mem) {
    logger = log4cxx::Logger::getLogger("acii.eyeclassifier");

    this->keep_class_in_mem = keep_class_in_mem;

    SVM_UD = NULL;
    colSTD_UD = NULL;
    colMEAN_UD = NULL;
    SVM_LR = NULL;
    colSTD_LR = NULL;
    colMEAN_LR = NULL;

    // Associate lines of configuration file to the attributes

    mMapString["XML filename for upright / upside-down classifier"] =
      &mClassifierFileNameUD;
    mMapString["XML filename for left / right classifier"] =
      &mClassifierFileNameLR;
    mMapString["XML filename for upright / upside-down feature normalization "
      "coefficients"] = &mFeatureNormFileNameUD;
    mMapString["XML filename for left / right feature normalization "
      "coefficients"] = &mFeatureNormFileNameLR;

    mMapFloat["RBF gamma used in training"] = &mRBFgamma;

    // Iris segmentation section
    mMapInt["Minimum diameter for pupil"] = &mMinPupilDiameter;
    mMapInt["Maximum diameter for pupil"] = &mMaxPupilDiameter;
    mMapInt["Minimum diameter for iris"] = &mMinIrisDiameter;
    mMapInt["Maximum diameter for iris"] = &mMaxIrisDiameter;

    // Initialize all parameters
    initConfiguration();

#ifdef EOC2_DEBUG
    // Operations needed if debug data is saved (not used in the final version):
    mMapString["Suffix for segmented images"] = &mSuffixSegmentedImages;
    mMapString["Suffix for parameters"] = &mSuffixParameters;
    mMapString["Suffix for masks of iris"] = &mSuffixMasks;
    mMapString["Suffix for normalized images"] = &mSuffixNormalizedImages;
    mMapString["Suffix for normalized masks"] = &mSuffixNormalizedMasks;

    mMapString["Save segmented images"] = &mOutputDirSegmentedImages;
    mMapString["Save contours parameters"] = &mOutputDirParameters;
    mMapString["Save masks of iris"] = &mOutputDirMasks;
    mMapString["Save normalized images"] = &mOutputDirNormalizedImages;
    mMapString["Save normalized masks"] = &mOutputDirNormalizedMasks;
#endif
  }

  EOC2Manager::~EOC2Manager() {
    cleanupClassifier();
  }

  // Initialize all configuration parameters
  void EOC2Manager::initConfiguration() {
    mClassifierFileNameUD = "";
    mClassifierFileNameLR = "";
    mFeatureNormFileNameUD = "";
    mFeatureNormFileNameLR = "";

    // Parameters
    mMinPupilDiameter = 21;
    mMaxPupilDiameter = 91;
    mMinIrisDiameter = 99;
    mMaxIrisDiameter = 399;
    mWidthOfNormalizedIris = 512;
    mHeightOfNormalizedIris = 64;
    mRBFgamma = 0.5;

#ifdef EOC2_DEBUG
    // Initialization needed if debug data is saved
    // (not used in the final version):
    // Outputs
    mOutputDirSegmentedImages = "";
    mOutputDirParameters = "";
    mOutputDirMasks = "";
    mOutputDirNormalizedImages = "";
    mOutputDirNormalizedMasks = "";

    // Suffix for filenames
    mSuffixSegmentedImages = "_segm.bmp";
    mSuffixParameters = "_para.txt";
    mSuffixMasks = "_mask.bmp";
    mSuffixNormalizedImages = "_imno.bmp";
    mSuffixNormalizedMasks = "_mano.bmp";
#endif
  }

  void EOC2Manager::loadClassifier_UD() {
    EOC2Eye eyeGlobal;
    FileStorage fs; 
    SVM_UD = new CvSVM();
    colSTD_UD = new Mat(1, eyeGlobal.getNumberOfUDfeatures(), CV_64FC1);
    colMEAN_UD = new Mat(1, eyeGlobal.getNumberOfUDfeatures(), CV_64FC1);

    ///////////////////////////////////////////////////////
    // Loading the trained SVM classifier
    LOG4CXX_DEBUG(logger, "Loading trained UP/DOWN SVM classifier from " <<
        mClassifierFileNameUD);

    // Check if file exists
    fs.open(mClassifierFileNameUD, FileStorage::READ);
    if (!fs.isOpened()) {
      LOG4CXX_ERROR(logger, "UP/DOWN SVM classifier file not found");
      throw runtime_error("UP/DOWN SVM classifier file not found");
    }
    fs.release();

    // We have the trained SVM; let's load it
    SVM_UD->load(mClassifierFileNameUD.c_str());
    LOG4CXX_DEBUG(logger, "Finished loading UP/DOWN SVM classifier");

    ///////////////////////////////////////////////////////
    // Loading feature normalization coefficients
    LOG4CXX_DEBUG(logger, "Loading UP/DOWN feature normalization coefficients "
        "from " << mFeatureNormFileNameUD);

    // Check if file exists
    fs.open(mFeatureNormFileNameUD, FileStorage::READ);
    if (!fs.isOpened()) {
      LOG4CXX_ERROR(logger,
          "UP/DOWN feature normalization coefficients file not found");
      throw runtime_error(
          "UP/DOWN feature normalization coefficients file not found");
    }
    fs["MEAN"] >> *colMEAN_UD;
    fs["STD"] >> *colSTD_UD;
    fs.release();
    LOG4CXX_DEBUG(logger,
      "Finished loading UP/DOWN feature normalization coefficients");

    // Log feature normalization coefficients (if needed)
    for (int i = 0; i < eyeGlobal.getNumberOfUDfeatures(); ++i) {
        LOG4CXX_TRACE(logger, "UP/DOWN mean vector value " <<
        i << ": " << colMEAN_UD->at<double>(i));
    }
    for (int i = 0; i < eyeGlobal.getNumberOfUDfeatures(); ++i) {
        LOG4CXX_TRACE(logger, "UP/DOWN std vector value " << i
        << ": " << colSTD_UD->at<double>(i));
    }
  }

  // Loads classification data structures from disk
  void EOC2Manager::loadClassifier_LR() {
    EOC2Eye eyeGlobal;
    FileStorage fs;

    SVM_LR = new CvSVM();
    colSTD_LR = new Mat(1, eyeGlobal.getNumberOfUDfeatures(), CV_64FC1);
    colMEAN_LR = new Mat(1, eyeGlobal.getNumberOfUDfeatures(), CV_64FC1);

    ///////////////////////////////////////////////////////
    // Loading the trained SVM classifier
    LOG4CXX_DEBUG(logger, "Loading trained LEFT/RIGHT SVM classifier from " <<
        mClassifierFileNameLR);

    // Check if file exists
    fs.open(mClassifierFileNameLR, FileStorage::READ);
    if (!fs.isOpened()) {
      LOG4CXX_ERROR(logger, "LEFT/RIGHT SVM classifier file not found");
      throw runtime_error("LEFT/RIGHT SVM classifier file not found");
    }
    fs.release();

    // We have the trained SVM; let's load it
    SVM_LR->load(mClassifierFileNameLR.c_str());
    LOG4CXX_DEBUG(logger, "Finished loading LEFT/RIGHT SVM classifier");

    ///////////////////////////////////////////////////////
    // Loading feature normalization coefficients
    LOG4CXX_DEBUG(logger, "Loading LEFT/RIGHT feature normalization "
        "coefficients from " << mFeatureNormFileNameLR);

    // Check if file exists
    fs.open(mFeatureNormFileNameLR, FileStorage::READ);
    if (!fs.isOpened()) {
      LOG4CXX_ERROR(logger,
          "LEFT/RIGHT feature normalization coefficients file not found");
      throw runtime_error(
          "LEFT/RIGHT feature normalization coefficients file not found");
    }
    fs["MEAN"] >> *colMEAN_LR;
    fs["STD"] >> *colSTD_LR;
    fs.release();
    LOG4CXX_DEBUG(logger,
        "Finished loading LEFT/RIGHT feature normalization coefficients");


    // Log feature normalization coefficients (if needed)
    for (int i = 0; i < eyeGlobal.getNumberOfUDfeatures(); ++i) {
      LOG4CXX_TRACE(logger, "LEFT/RIGHT mean vector value " <<
          i << ": " << colMEAN_LR->at<double>(i));
    }
    for (int i = 0; i < eyeGlobal.getNumberOfUDfeatures(); ++i) {
      LOG4CXX_TRACE(logger, "LEFT/RIGHT std vector value " <<
          i << ": " << colSTD_LR->at<double>(i));
    }
  }

  void EOC2Manager::loadClassifier() {
    EOC2Manager::loadClassifier_UD();
    EOC2Manager::loadClassifier_LR();
  }

  void EOC2Manager::cleanupClassifier_UD() {
    delete SVM_UD;
    delete colSTD_UD;
    delete colMEAN_UD;
    LOG4CXX_DEBUG(logger, "Removing UP / DOWN classifier from memory");
  }

  void EOC2Manager::cleanupClassifier_LR() {
    delete SVM_LR;
    delete colSTD_LR;
    delete colMEAN_LR;
    LOG4CXX_DEBUG(logger, "Removing LEFT / RIGHT classifier from memory");
  }

  void EOC2Manager::cleanupClassifier() {
    cleanupClassifier_UD();
    cleanupClassifier_LR();
  }


  /*
   * Loads the configuration from a textfile (ini)
   *
   * @param rFilename The configuration file path
   */
  void EOC2Manager::loadConfiguration(const string &rFilename) {
    // Open the file
    ifstream file(rFilename.c_str(), ifstream::in);

    if (!file.good()) {
      LOG4CXX_ERROR(logger, "Cannot read configuration file ");
      throw runtime_error("Cannot read configuration file " + rFilename);
    }

    // Some string functions
    EOC2StringUtils osu;

    // Loop on lines
    while (file.good() && !file.eof()) {
      // Get the new line
      string line;
      getline(file, line);

      // Filter out comments
      if (!line.empty()) {
        int pos = line.find('#');
        if (pos != string::npos) {
          line = line.substr(0, pos);
        }
      }

      // Split line into key and value
      if (!line.empty()) {
        int pos = line.find("=");

        if (pos != string::npos) {
          // Trim key and value
          string key = osu.trim(line.substr(0, pos));
          string value = osu.trim(line.substr(pos + 1));

          if (!key.empty() && !value.empty()) {
            // Option is type bool
            if (mMapBool.find(key) != mMapBool.end()) {
              *mMapBool[key] = osu.fromString<bool>(value);

            // Option is type int
            } else if (mMapInt.find(key) != mMapInt.end()) {
              *mMapInt[key] = osu.fromString<int>(value);

            // Option is type float
            } else if (mMapFloat.find(key) != mMapFloat.end()) {
              *mMapFloat[key] = osu.fromString<float>(value);

            // Option is type string
            } else if (mMapString.find(key) != mMapString.end()) {
              *mMapString[key] = osu.convertSlashes(value);

            // Option is not stored in any mMap
            } else {
              LOG4CXX_ERROR(logger,
                  "Unknown option in configuration file : " << line);
            }
          }
        }
      }
    }
    if (keep_class_in_mem) {
      EOC2Manager::loadClassifier();
    }
  }

  // Show the configuration in prompt command
  void EOC2Manager::showConfiguration() {
    LOG4CXX_INFO(logger,
        "Trained left / right classifier read from: " << mClassifierFileNameLR);
    LOG4CXX_INFO(logger, "Feature normalization coefficients for left / right "
        "classification read from: " << mFeatureNormFileNameLR);

    LOG4CXX_INFO(logger, "Trained upright / upside-down classifier read from: "
        << mClassifierFileNameUD);
    LOG4CXX_INFO(logger, "Feature normalization coefficients for upright / "
        "upside-down classification read from: " << mFeatureNormFileNameUD);

    LOG4CXX_INFO(logger, "Pupil diameter range: (" << mMinPupilDiameter << ", "
        << mMaxPupilDiameter << ")");
    LOG4CXX_INFO(logger, "Iris diameter range: (" << mMinIrisDiameter << ", "
      << mMaxIrisDiameter << ")");
  }  // end of function

  // Calculate the image features required for upright / upside-down
  // classification.
  void EOC2Manager::processUD(const string &rFileName, EOC2Eye &rEye) {
    // Strings handle
    EOC2StringUtils osu;

    // Load the image
    rEye.loadOriginalImage(rFileName);


    // Segment the iris
    rEye.segment(mMinIrisDiameter, mMinPupilDiameter, mMaxIrisDiameter,
        mMaxPupilDiameter);

    // Normalize the iris image (Cartesian -> polar)
    rEye.normalize(mWidthOfNormalizedIris, mHeightOfNormalizedIris);

    // Extract features required for upright / upside-down classification
    rEye.featuresUD();

    // Since we are here (no exceptions thrown) the processing is succesful
    rEye.bProcessedOK = true;

#ifdef EOC2_DEBUG
    // Save debug data - segmentation image, parameters, mask, normalized image
    // and mask (not used in the final version)

    string short_name = rFileName.substr(0, rFileName.rfind("."));

    if (mOutputDirSegmentedImages != "") {
      rEye.saveSegmentedImage(mOutputDirSegmentedImages + short_name +
          mSuffixSegmentedImages);
    }
    if ( mOutputDirParameters != "" ) {
      rEye.saveParameters(mOutputDirParameters + short_name +
          mSuffixParameters);
    }
    if ( mOutputDirMasks != "" ) {
      rEye.saveMask(mOutputDirMasks + short_name + mSuffixMasks);
    }
    if ( mOutputDirNormalizedImages != "" ) {
      rEye.saveNormalizedImage(mOutputDirNormalizedImages + short_name +
          mSuffixNormalizedImages);
    }
    if ( mOutputDirNormalizedMasks != "" ) {
      rEye.saveNormalizedMask(mOutputDirNormalizedMasks + short_name +
          mSuffixNormalizedMasks);
    }
#endif
  }  // end of function

  /**
   * Calculates the image features required for left / right classification.
   *
   * @param rFileName The local path to the image file
   * @param rEye The Eye object associated with the image
   */
  void EOC2Manager::processLR(const string &rFileName, EOC2Eye &rEye) {
    // Strings handle
    EOC2StringUtils osu;

    // Load the image
    rEye.loadOriginalImage(rFileName);


    // Segment the iris
    rEye.segment(mMinIrisDiameter, mMinPupilDiameter, mMaxIrisDiameter,
        mMaxPupilDiameter);

    // Normalize the iris image (Cartesian -> polar)
    rEye.normalize(mWidthOfNormalizedIris, mHeightOfNormalizedIris);

    // Extract features required for upright / upside-down classification
    rEye.featuresLR();

    // Since we are here (no exceptions thrown) the processing is succesful
    rEye.bProcessedOK = true;

#ifdef EOC2_DEBUG
    // Save debug data - segmentation image, parameters, mask, normalized image
    // and mask (not used in the final version)

    string short_name = rFileName.substr(0, rFileName.rfind("."));

    if (mOutputDirSegmentedImages != "") {
      rEye.saveSegmentedImage(mOutputDirSegmentedImages + short_name +
          mSuffixSegmentedImages);
    }
    if (mOutputDirParameters != "") {
      rEye.saveParameters(mOutputDirParameters + short_name +
          mSuffixParameters);
    }
    if (mOutputDirMasks != "") {
      rEye.saveMask(mOutputDirMasks + short_name + mSuffixMasks);
    }
    if (mOutputDirNormalizedImages != "") {
      rEye.saveNormalizedImage(mOutputDirNormalizedImages + short_name +
          mSuffixNormalizedImages);
    }
    if (mOutputDirNormalizedMasks != "") {
      rEye.saveNormalizedMask(mOutputDirNormalizedMasks + short_name +
          mSuffixNormalizedMasks);
    }
#endif
  }  // end of function

  /**
   * Determines whether the eye in the image is upright or upside-down.
   * 0 = Upright, 1 = Upside down, -1 = Unable to determine.
   *
   * @param image_path The local path to the image file
   */
  int EOC2Manager::recognitionUD(std::string image_path, int image_type, 
      int image_width, int image_height) {
    EOC2Eye eyeGlobal;

    if (!keep_class_in_mem) {
      loadClassifier_UD();
    }

    // Class prediction and presentation of the result
    Mat testingData(1, eyeGlobal.getNumberOfUDfeatures(), CV_32FC1);
#ifdef EOC2_DEBUG
    eyeGlobal.printFeaturesUDHeader();
#endif

    int recogOrientation = -1;

    EOC2Eye eye(image_type, image_width, image_height);

    try {
      LOG4CXX_DEBUG(logger, "Processing image...");
      processUD(image_path, eye);
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, "Error while processing image: " << e.what() <<
          ". " << image_path << "\t\t" << "UNDETERMINED");
    }
    if (eye.bProcessedOK) {
      testingData.at<float>(0) = (eye.stFeaturesUD.fEyelidCoverage -
          static_cast<float>(colMEAN_UD->at<double>(0))) /
          static_cast<float>(colSTD_UD->at<double>(0));
      testingData.at<float>(1) = (eye.stFeaturesUD.fEyelidValue -
          static_cast<float>(colMEAN_UD->at<double>(1))) /
          static_cast<float>(colSTD_UD->at<double>(1));
      testingData.at<float>(2) = (eye.stFeaturesUD.fVerticalPupilShift -
          static_cast<float>(colMEAN_UD->at<double>(2))) /
          static_cast<float>(colSTD_UD->at<double>(2));
      testingData.at<float>(3) = (eye.stFeaturesUD.fEyelidFiltered -
          static_cast<float>(colMEAN_UD->at<double>(3))) /
          static_cast<float>(colSTD_UD->at<double>(3));
      testingData.at<float>(4) = (eye.stFeaturesUD.fEyelidFilteredCosVar -
          static_cast<float>(colMEAN_UD->at<double>(4))) /
          static_cast<float>(colSTD_UD->at<double>(4));
      testingData.at<float>(5) = (eye.stFeaturesUD.fEyelidFilteredSinVar -
          static_cast<float>(colMEAN_UD->at<double>(5))) /
          static_cast<float>(colSTD_UD->at<double>(5));

      if (SVM_UD->predict(testingData) > 0) {
#ifdef EOC2_DEBUG
        eye.printFeaturesUD(i, image_path, "UPRIGHT");
#endif
        recogOrientation = 0;
        LOG4CXX_DEBUG(logger, "Detected UPRIGHT EYE");
      } else {
#ifdef EOC2_DEBUG
        eye.printFeaturesUD(i, image_path, "UP/DOWN");
#endif
        recogOrientation = 1;
        LOG4CXX_DEBUG(logger, "Detected UPSIDE DOWN EYE");
      }
    }

    if (!keep_class_in_mem) {
      cleanupClassifier_UD();
    }

    return recogOrientation;
  }

  /**
   * Determines whether the image is of the left or right eye. 0 = Left eye, 
   *   1 = Right eye, -1 = Unable to determine.
   *
   * @param image_path The local path to the image file
   */
  int EOC2Manager::recognitionLR(std::string image_path, int image_type,
      int image_width, int image_height) {
    EOC2Eye eyeGlobal;
    if (!keep_class_in_mem) {
      loadClassifier_LR();
    }

    // Class prediction and presentation of the result
    Mat testingData(1, eyeGlobal.getNumberOfLRfeatures(), CV_32FC1);
#ifdef EOC2_DEBUG
    eyeGlobal.printFeaturesLRHeader();
#endif

    int recogOrientation = -1;

    EOC2Eye eye(image_type, image_width, image_height);

    try {
      LOG4CXX_DEBUG(logger, "Processing image...");
      processLR(image_path, eye);
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, "Error while processing image: " << e.what() <<
          ". " << image_path << "\t\t" << "UNDETERMINED");
    }
    if (eye.bProcessedOK) {
      testingData.at<float>(0) = (eye.stFeaturesLR.fEyelidValue -
          static_cast<float>(colMEAN_LR->at<double>(0))) /
          static_cast<float>(colSTD_LR->at<double>(0));
      testingData.at<float>(1) = (eye.stFeaturesLR.fHorizontalPupilShift -
      static_cast<float>(colMEAN_LR->at<double>(1))) /
      static_cast<float>(colSTD_LR->at<double>(1));

      if (SVM_LR->predict(testingData) > 0) {
#ifdef EOC2_DEBUG
        eye.printFeaturesLR(i, image_path, "LEFT");
#endif
        recogOrientation = 0;
        LOG4CXX_DEBUG(logger, "Detected LEFT EYE");
      } else {
#ifdef EOC2_DEBUG
        eye.printFeaturesLR(i, image_path, "RIGHT");
#endif
        recogOrientation = 1;
        LOG4CXX_DEBUG(logger, "Detected RIGHT EYE");
      }
    }

    if (!keep_class_in_mem) {
      cleanupClassifier_LR();
    }
    return recogOrientation;
  }



  /**
   * Run the software according to the configuration.
   *
   * @param image_path The local path to the image file
   * @param up_down An integer reference to be set to the determined image
   *   orientation, upright or upside down. 0 = Eye is upright, 1 = Eye is
   *   upside-down, -1 = Unable to determine.
   * @param left_right An integer reference to be set to the determined
   *   left/right eye classification. 0 = Left eye, 1 = Right eye,
   *   -1 = Unable to determine
   */
  void EOC2Manager::run(std::string image_path, int &up_down, int &left_right, 
      int image_type, int image_width, int image_height) {
    up_down = -1;
    left_right = -1;

    try {
      LOG4CXX_INFO(logger, "Starting UP / DOWN classification...");
      // Run the recognition
      up_down = recognitionUD(image_path, image_type, image_width,
          image_height);
      LOG4CXX_INFO(logger, "Finshed UP / DOWN classification");
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, "Error occured duing UP / DOWN classification: "
          << e.what());
    }

    try {
      LOG4CXX_INFO(logger, "Starting LEFT / RIGHT classification...");
      // Run the recognition
      left_right = recognitionLR(image_path, image_type, image_width,
          image_height);
      LOG4CXX_INFO(logger, "Finshed LEFT / RIGHT classification");
    } catch (exception &e) {
      LOG4CXX_ERROR(logger, "Error occured duing LEFT / RIGHT classification: "
          << e.what());
    }
  }  // end of function
}  // namespace EOC2
