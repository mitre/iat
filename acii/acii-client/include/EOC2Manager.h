/*****************************************************************************
* Eye Orientation CLassifier v2(U/D + L/R)
* Author: Adam Czajka, aczajka@nd.edu, May 2016
*
* Code in this file is partially based on Open Source for Iris(OSIRIS) v. 4.1
* under BSD license http://opensource.org/licenses/bsd-license.php
******************************************************************************/


#ifndef ACII_CLIENT_INCLUDE_EOC2MANAGER_H_
#define ACII_CLIENT_INCLUDE_EOC2MANAGER_H_

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/core/core.hpp>
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/logger.h>
#include <log4cxx/logmanager.h>
#include <log4cxx/rollingfileappender.h>
#include <log4cxx/simplelayout.h>

#include <iostream>
#include <vector>
#include <string>
#include <map>

#include "EOC2Eye.h"

using std::map;
using std::string;

namespace EOC2 {

/** Overall manager.
 * This class manages all the files, configuration, saving
 * and loading options. It uses EOC2Eye to execute technical processings.
 * @see EOC2Eye
 */
class EOC2Manager {
 public:
  /** Default constructor.
   * Associate lines of configuration file to the attributes of the class.
   * Initialize all parameters to default values.
   * @see initConfiguration()
   */
  EOC2Manager(bool keep_class_in_mem = true);

  /** Default destructor.
   * Release matrix containing the application points.\n
   */
  ~EOC2Manager();

  /** Load configuration from a text file.
   * @param rFilename The relative path to the filename(relative to the
   *   executable)
   * @return void
   * @see showConfiguration()
   */
  void loadConfiguration(const string &rFilename = "conf.ini");

  /** Show configuration in prompt command.
   * @see initConfiguration()
   * @see loadConfiguration()
   */
  void showConfiguration();

  /** Check configuration provided in conf.ini file .
   * @return int: 1 configuration correct, 0 otherwise
   * @see initConfiguration()
   * @see loadConfiguration()
   */
  int checkConfiguration();

  /** Run the software according to the configuration.
   * Build the eye lists and process them as requested by the configuration file
   * @see processUD()
   */
  void run(string image_path, int &up_down, int &left_right, int image_type,
      int image_width = 0, int image_height = 0);


 private:
  log4cxx::LoggerPtr logger;

  // If true, keeps classification data structures in memory,
  // else loads each processing call
  bool keep_class_in_mem;

  // Classification data-structures
  CvSVM* SVM_UD;
  cv::Mat* colSTD_UD;
  cv::Mat* colMEAN_UD;
  CvSVM* SVM_LR;
  cv::Mat* colSTD_LR;
  cv::Mat* colMEAN_LR;

  // Classification file paths
  string mClassifierFileNameUD;
  string mClassifierFileNameLR;
  string mFeatureNormFileNameUD;
  string mFeatureNormFileNameLR;

  // Parameters
  int mMinPupilDiameter;
  int mMaxPupilDiameter;
  int mMinIrisDiameter;
  int mMaxIrisDiameter;
  int mWidthOfNormalizedIris;
  int mHeightOfNormalizedIris;
  float mRBFgamma;

  // Maps to associate a string(conf file) to a variable(not the value of the
  // variable !)
  map<string, bool*> mMapBool;
  map<string, int*> mMapInt;
  map<string, string*> mMapString;
  map<string, float*> mMapFloat;

  void loadClassifier_UD();
  void loadClassifier_LR();
  void cleanupClassifier_UD();
  void cleanupClassifier_LR();
  void cleanupClassifier();

  /** Loads classification data structures from disk. Read from
   * mClassifierFileNameUD, mClassifierFileNameLR, mFeatureNormFileNameUD and
   * mFeatureNormFileNameUD as loaded from
   * config file in loadConfiguration(). Called after loadConfiguration();
   * @see loadConfiguration()
   */
  void loadClassifier();

  /** Initialize all configuration options to default values.
   * Default values are :
   * - For all directory/textfile paths : ""
   * - Minimum and maximum diameter for the pupil : 21 - 91 pixels
   * - Minimum and maximum diameter for the iris : 99 - 399 pixels
   * - All commands of processing are set to false => nothing is going to be
   *     executed
   * @see loadConfiguration()
   * @see showConfiguration()
   */
  void initConfiguration();

  /** Calculate the image features required for upright / upside-down classification.
   *
   * @param rName The eye name
   * @param rEye The eye to be processed
   * @return void
   * @see EOC2Eye
   */
  void processUD(const string &rFileName, EOC2Eye &rEye);

  /** Use of the trained SVM classifier for recognition of upright / upside-down
   * orientation. Returns 0 if upright, 1 if upside-down, -1 on error
   *
   * @param vListOfTestingImages List of images used in training
   * @return int
   */
  int recognitionUD(string image_path, int image_type, int image_width = 0,
      int image_height = 0);


  /** Calculate the image features required for left / right classification.
   * @param rName The eye name
   * @param rEye The eye to be processed
   * @return void
   * @see EOC2Eye
   */
  void processLR(const string &rFileName, EOC2Eye &rEye);


  /** Use of the trained SVM classifier for left / right classification.
   * Returns 0 if left, 1 if right, -1 on error
   * @param vListOfTestingImages List of images used in training
   * @return int
   */
  int recognitionLR(string image_path, int image_type, int image_width = 0,
      int image_height = 0);


#ifdef EOC2_DEBUG
  // Private variables needed if debug data is saved
  // Suffix for filenames
  string mSuffixSegmentedImages;
  string mSuffixParameters;
  string mSuffixMasks;
  string mSuffixNormalizedImages;
  string mSuffixNormalizedMasks;
  // Outputs
  string mOutputDirSegmentedImages;
  string mOutputDirParameters;
  string mOutputDirMasks;
  string mOutputDirNormalizedImages;
  string mOutputDirNormalizedMasks;
#endif
};  // class EOC2Manager
}  // namespace EOC2

#endif  // ACII_CLIENT_INCLUDE_EOC2MANAGER_H_

