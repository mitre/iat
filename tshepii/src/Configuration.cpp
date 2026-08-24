// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

/** @file Implementation of Configuration class. */

#include "Configuration.hpp"

/* General configuration. */
const string Configuration::FILE_SEPARATOR = "\\"; // Unix-like systems: change to "/"
const int Configuration::PROCESSED_IRIS_IMAGE_COUNT = 15;
const int Configuration::SCORE_COUNT = 11;

/* OSIRIS configuration. */
const double Configuration::OSIRIS_THRESHOLD = 0.4461;
int Configuration::OSIRIS_MIN_IRIS_DIAMETER = 160;
int Configuration::OSIRIS_MIN_PUPIL_DIAMETER = 48;
int Configuration::OSIRIS_MAX_IRIS_DIAMETER = 380;
int Configuration::OSIRIS_MAX_PUPIL_DIAMETER = 160;
int Configuration::OSIRIS_NORM_IMAGE_COLS = 512;
int Configuration::OSIRIS_NORM_IMAGE_ROWS = 64;

/* BSIF configuration. */
const double Configuration::BSIF_THRESHOLD = 0.4216;
const int Configuration::BSIF_NORM_IMAGE_COLS = 256;
const int Configuration::BSIF_NORM_IMAGE_ROWS = 32;

/* Iris content pre-processing. */
const int Configuration::IRIS_MASK_BLUR_SIZE = 5;
const int Configuration::IRIS_CLAHE_TILES = 16;
const double Configuration::IRIS_CLAHE_CLIP = 2.0;

/* Interest point configuration. */
// All configurations
const int Configuration::KP_LINE_WIDTH = 1;
const int Configuration::KP_MIN_SIZE = 9;
const int Configuration::KP_MAX_SIZE = 76;
const double Configuration::KP_COINCIDENCE_TOLERANCE = 0.5;
const int Configuration::KP_MATCHER_KNN_K = 50;
const double Configuration::KP_MATCHER_NORM_L2_QUALITY_RATIO = 0.8;
const double Configuration::KP_MATCHER_NORM_HAMMING_QUALITY_RATIO = 0.5;
const double Configuration::KP_MATCHER_DEVIATION_TOLERANCE = 1.5;
const double Configuration::KP_MATCHER_POLAR_RADIUS_TOLERANCE = 0.075;
const double Configuration::KP_MATCHER_POLAR_ANGLE_TOLERANCE = 0.2;

// interest point detectors
// Crypts
const double Configuration::CRYPT_THRESHOLD = 0.6389;
const int Configuration::CRYPTS_LINE_WIDTH = 1;
const int Configuration::CRYPTS_SMOOTH_GAUSS_SIZE = 5;                // original value
const int Configuration::CRYPTS_SMOOTH_GAUSS_SIGMA = 3;               // original value
const int Configuration::CRYPTS_MIN_ITERATIVE_STR_EL_SIZE = 7;        // original value
const int Configuration::CRYPTS_INITIAL_ITERATIVE_STR_EL_SIZE = 17;   // original value
const double Configuration::CRYPTS_MIN_CRYPT_AREA = 49.0;             // 7 x 7
const double Configuration::CRYPTS_MAX_CRYP_AREA_LB = 652.0;          // 384 * 384 * <145> / (512 * 64)
const double Configuration::CRYPTS_MIN_CRYP_AREA_UB = 1426.0;         // 384 * 384 * <317> / (512 * 64)
const double Configuration::CRYPTS_CUT_STD = 0.06;                    // original value
const double Configuration::CRYPTS_MATCH_COINCIDENCE_TOLERANCE = 0.5; // 50% of crypt coincidence
const double Configuration::CRYPTS_MATCH_DIST_ALPHA = 0.5;            // coincidence and area complementary weights
const Scalar Configuration::CRYPTS_COLOR(255, 255, 0);

// TSHEPII
const double Configuration::TSHEPII_MATCH_THRESHOLD = 0.0105;
const double Configuration::TSHEPII_MISMATCH_THRESHOLD = 0.0022;
const double Configuration::TSHEPII_BORDER_AVOIDANCE = 1.5;
const double Configuration::TSHEPII_OVERLAP_TOLERANCE = 1.0;
const double Configuration::TSHEPII_KP_THRESHOLD = 1.0;
const int Configuration::TSHEPII_CELL_DIVISION = 4;
const double Configuration::TSHEPII_BIN_THRESHOLD = 1.0;
const string Configuration::TSHEPII_FILTER_LIST_FILEPATH = "tshepii_filter_list.txt";
const string Configuration::TSHEPII_ZND_LIST_FILEPATH = "tshepii_zn_list.txt";
const Scalar Configuration::TSHEPII_COLOR(255, 0, 0);

// SURF
const double Configuration::SURF_MATCH_THRESHOLD = 0.2358;
const double Configuration::SURF_MISMATCH_THRESHOLD = 0.3361;
const double Configuration::SURF_HESSIAN_THRESH = 1.0;
const Scalar Configuration::SURF_COLOR(0, 130, 255);

// SIFT
const double Configuration::SIFT_MATCH_THRESHOLD = 0.3932;
const double Configuration::SIFT_MISMATCH_THRESHOLD = 0.5156;
const int Configuration::SIFT_KP_COUNT = 0;             // > zero means all possible interest points
const int Configuration::SIFT_OCTAVE_LAYER_COUNT = 3;   // > 3 is the lieterature suggested value
const double Configuration::SIFT_CONTRAST_THRESH = 0.0; // > the lesser this value, the more detected interest points
const double Configuration::SIFT_EDGE_THRESH = 1000.0;  // > the more this value, the more detected interest points
const Scalar Configuration::SIFT_COLOR(255, 0, 255);

// MSER
const double Configuration::MSER_MATCH_THRESHOLD = 0.2796;
const double Configuration::MSER_MISMATCH_THRESHOLD = 0.1377;
const int Configuration::MSER_DELTA = 3; // > the lesser this value, the more the detected regions
const int Configuration::MSER_MIN_AREA = Configuration::KP_MIN_SIZE * Configuration::KP_MIN_SIZE;
const int Configuration::MSER_MAX_AREA = Configuration::KP_MAX_SIZE * Configuration::KP_MAX_SIZE;
const double Configuration::MSER_MAX_VARIATION = 0.25; // > the more this value, the more the detected regions
const Scalar Configuration::MSER_COLOR(0, 255, 255);

/* Annotation configuration. */
const int Configuration::ANNOT_CTRL_PNT_SIZE = 3;
const int Configuration::ANNOT_CTRL_PNT_SIZE_SELECTED = 5;
const int Configuration::ANNOT_LINE_WIDTH = 1;
const int Configuration::ANNOT_LINE_WIDTH_SELECTED = 2;
const int Configuration::ANNOT_CTRL_PNT_DIST_TOLERANCE = 4;
const int Configuration::ANNOT_SELECTION_TOLERANCE = 5;
const int Configuration::ANNOT_CTRL_PNT_MIN_COUNT = 5;

/* Software  configuration. */
// main window
const string Configuration::MAIN_WINDOW_TITLE = "TSHEPII";
const int Configuration::MAIN_WINDOW_WIDTH = 1300;
const int Configuration::MAIN_WINDOW_HEIGHT = 800;
const Scalar Configuration::MAIN_WINDOW_BACKGROUND_COLOR(0, 0, 0);

// main menu
const int Configuration::MAIN_MENU_X = 0;
const int Configuration::MAIN_MENU_Y = 0;
const int Configuration::MAIN_MENU_HEIGHT = 36;

const string Configuration::MAIN_MENU_LOAD_IRIS_LABEL = "Load irises";
const int Configuration::MAIN_MENU_LOAD_IRIS_WIDTH = 100;

const string Configuration::MAIN_MENU_LOAD_EXAMINATION_LABEL = "Load examination";
const int Configuration::MAIN_MENU_LOAD_EXAMINATION_WIDTH = 150;

const string Configuration::MAIN_MENU_SAVE_EXAMINATION_LABEL = "Save examination";
const int Configuration::MAIN_MENU_SAVE_EXAMINATION_WIDTH = 150;

const string Configuration::MAIN_MENU_SAVE_REPORT_LABEL = "Save report";
const int Configuration::MAIN_MENU_SAVE_REPORT_WIDTH = 120;

const string Configuration::MAIN_MENU_QUIT_PROGRAM_LABEL = "Quit program";
const int Configuration::MAIN_MENU_QUIT_PROGRAM_WIDTH = 120;
const string Configuration::MAIN_MENU_QUIT_PROGRAM_QUESTION = "Quit program?";

// modal messages
const int Configuration::MODAL_WIDTH = 300;
const int Configuration::MODAL_HEIGHT = 120;
const int Configuration::MODAL_BUTTON_HEIGHT = 40;
const string Configuration::MODAL_BUTTON_LABEL = "OK";

// yes/no dialog
const int Configuration::DIALOG_WIDTH = 300;
const int Configuration::DIALOG_HEIGHT = 120;
const int Configuration::DIALOG_BUTTON_HEIGHT = 40;
const string Configuration::DIALOG_YES_LABEL = "OK";
const string Configuration::DIALOG_NO_LABEL = "Cancel";

// progress bar
const string Configuration::PROGRESS_TITLE = "Please wait";
const int Configuration::PROGRESS_WIDTH = 300;
const int Configuration::PROGRESS_HEIGHT = 120;
const int Configuration::PROGRESS_BAR_HEIGHT = 40;
const int Configuration::PROGRESS_BAR_BORDER = 10;
const Scalar Configuration::PROGRESS_BAR_COLOR(255, 255, 255);

// iris image loading
const char *Configuration::LOAD_IRIS_EXTENSIONS = "png,jpg,bmp,tif,tiff"; // valid extensions
const string Configuration::LOAD_IRIS_WARN_MSG_1 = "Please select two irises.";
const string Configuration::LOAD_IRIS_WARN_MSG_2 = "Could not open iris";
const string Configuration::LOAD_IRIS_WARN_MSG_3 = "Loading irises...";
const string Configuration::LOAD_IRIS_WARN_MSG_4 = "Could not compute OSIRIS score.";
const string Configuration::LOAD_IRIS_WARN_MSG_5 = "Could not compute BSIF score.";

// examination loading
const string Configuration::SAVE_EXAM_WARN_MSG_1 = "Successfully saved current examination.";
const string Configuration::SAVE_EXAM_WARN_MSG_2 = "Could not save current examination.";
const string Configuration::LOAD_EXAM_WARN_MSG_1 = "Could not read stored examination.";

// report saving
const string Configuration::SAVE_REPORT_WARN_MSG_1 = "Successfully saved report.";

// iris windows
const int Configuration::IRIS_WIDTH = 384;
const int Configuration::IRIS_HEIGHT = 384;
const int Configuration::IRIS_RADIUS_OFFSET = 10;
const int Configuration::IRIS_1_X = 245;
const int Configuration::IRIS_1_Y = 36;
const int Configuration::IRIS_2_X = 671;
const int Configuration::IRIS_2_Y = 36;

// iris image processing windows
const double Configuration::IRIS_BRIGHT_MIN = -1.0;
const double Configuration::IRIS_BRIGHT_MAX = 1.0;

const double Configuration::IRIS_CONTRAST_MIN = 0.0;
const double Configuration::IRIS_CONTRAST_MAX = 2.0;

const int Configuration::IRIS_SHARP_MIN = 0;
const int Configuration::IRIS_SHARP_MAX = 10;

const string Configuration::IRIS_BRIGHT_TITLE = "Brightness";
const string Configuration::IRIS_CONTRAST_TITLE = "Contrast";
const string Configuration::IRIS_SHARP_TITLE = "Sharpening";

const int Configuration::IRIS_1_CTRL_WINDOW_X = 10;
const int Configuration::IRIS_1_CTRL_WINDOW_Y = 66;
const int Configuration::IRIS_1_CTRL_WINDOW_WIDTH = 225;
const int Configuration::IRIS_1_CTRL_WINDOW_HEIGHT = 355;

const int Configuration::IRIS_1_BRIGHT_TITLE_X = 15;
const int Configuration::IRIS_1_BRIGHT_TITLE_Y = 100;
const int Configuration::IRIS_1_BRIGHT_TRACK_X = 15;
const int Configuration::IRIS_1_BRIGHT_TRACK_Y = 120;
const int Configuration::IRIS_1_BRIGHT_TRACK_WIDTH = 215;

const int Configuration::IRIS_1_CONTRAST_TITLE_X = 15;
const int Configuration::IRIS_1_CONTRAST_TITLE_Y = 190;
const int Configuration::IRIS_1_CONTRAST_TRACK_X = 15;
const int Configuration::IRIS_1_CONTRAST_TRACK_Y = 210;
const int Configuration::IRIS_1_CONTRAST_TRACK_WIDTH = 215;

const int Configuration::IRIS_1_SHARP_TITLE_X = 15;
const int Configuration::IRIS_1_SHARP_TITLE_Y = 280;
const int Configuration::IRIS_1_SHARP_TRACK_X = 15;
const int Configuration::IRIS_1_SHARP_TRACK_Y = 300;
const int Configuration::IRIS_1_SHARP_TRACK_WIDTH = 215;

const string Configuration::IRIS_1_SEGMENT_BUTTON_LABEL = "Segment iris";
const int Configuration::IRIS_1_SEGMENT_BUTTON_X = 25;
const int Configuration::IRIS_1_SEGMENT_BUTTON_Y = 370;
const int Configuration::IRIS_1_SEGMENT_BUTTON_WIDTH = 195;
const int Configuration::IRIS_1_SEGMENT_BUTTON_HEIGHT = 30;

const int Configuration::IRIS_2_CTRL_WINDOW_X = 1065;
const int Configuration::IRIS_2_CTRL_WINDOW_Y = 66;
const int Configuration::IRIS_2_CTRL_WINDOW_WIDTH = 225;
const int Configuration::IRIS_2_CTRL_WINDOW_HEIGHT = 355;

const int Configuration::IRIS_2_BRIGHT_TITLE_X = 1070;
const int Configuration::IRIS_2_BRIGHT_TITLE_Y = 100;
const int Configuration::IRIS_2_BRIGHT_TRACK_X = 1070;
const int Configuration::IRIS_2_BRIGHT_TRACK_Y = 120;
const int Configuration::IRIS_2_BRIGHT_TRACK_WIDTH = 215;

const int Configuration::IRIS_2_CONTRAST_TITLE_X = 1070;
const int Configuration::IRIS_2_CONTRAST_TITLE_Y = 190;
const int Configuration::IRIS_2_CONTRAST_TRACK_X = 1070;
const int Configuration::IRIS_2_CONTRAST_TRACK_Y = 210;
const int Configuration::IRIS_2_CONTRAST_TRACK_WIDTH = 215;

const int Configuration::IRIS_2_SHARP_TITLE_X = 1070;
const int Configuration::IRIS_2_SHARP_TITLE_Y = 280;
const int Configuration::IRIS_2_SHARP_TRACK_X = 1070;
const int Configuration::IRIS_2_SHARP_TRACK_Y = 300;
const int Configuration::IRIS_2_SHARP_TRACK_WIDTH = 215;

const string Configuration::IRIS_2_SEGMENT_BUTTON_LABEL = "Segment iris";
const int Configuration::IRIS_2_SEGMENT_BUTTON_X = 1080;
const int Configuration::IRIS_2_SEGMENT_BUTTON_Y = 370;
const int Configuration::IRIS_2_SEGMENT_BUTTON_WIDTH = 195;
const int Configuration::IRIS_2_SEGMENT_BUTTON_HEIGHT = 30;

// iris zooming
const double Configuration::IRIS_ZOOM_MIN = 1.0;
const double Configuration::IRIS_ZOOM_MAX = 3.0;

const int Configuration::IRIS_1_ZOOM_CTRL_WINDOW_X = 245;
const int Configuration::IRIS_1_ZOOM_CTRL_WINDOW_Y = 430;
const int Configuration::IRIS_1_ZOOM_CTRL_WINDOW_WIDTH = 374;
const int Configuration::IRIS_1_ZOOM_CTRL_WINDOW_HEIGHT = 60;

const int Configuration::IRIS_2_ZOOM_CTRL_WINDOW_X = 681;
const int Configuration::IRIS_2_ZOOM_CTRL_WINDOW_Y = 430;
const int Configuration::IRIS_2_ZOOM_CTRL_WINDOW_WIDTH = 374;
const int Configuration::IRIS_2_ZOOM_CTRL_WINDOW_HEIGHT = 60;

const int Configuration::IRIS_CONNECT_BTN_X = 620;
const int Configuration::IRIS_CONNECT_BTN_Y = 430;
const int Configuration::IRIS_CONNECT_BTN_WIDTH = 60;
const int Configuration::IRIS_CONNECT_BTN_HEIGHT = 60;
const string Configuration::IRIS_CONNECT_BTN_LABEL = "1x";

// human-interpretable and annotation windows: common features
const int Configuration::COLOR_FLAG_SIZE = 15;
const int Configuration::COLOR_FLAG_PADDING = 5;

// non-human-interpretable output window
const string Configuration::NHI_OUTPUT_WINDOW_TITLE = "Non-Human-Interpretable Features";
const int Configuration::NHI_OUTPUT_WINDOW_X = 10;
const int Configuration::NHI_OUTPUT_WINDOW_Y = 710;
const int Configuration::NHI_OUTPUT_WINDOW_WIDTH = 850;
const int Configuration::NHI_OUTPUT_WINDOW_HEIGHT = 80;

const double Configuration::NHI_OUTPUT_SCORE_SIZE_1 = 0.6;
const double Configuration::NHI_OUTPUT_SCORE_SIZE_2 = 0.3;
const unsigned int Configuration::NHI_OUTPUT_SCORE_COLOR = 0xffffff;

const string Configuration::NHI_OUTPUT_OSIRIS_LABEL_1 = "Gabor Filters";
const string Configuration::NHI_OUTPUT_OSIRIS_LABEL_2 = "thr:";
const int Configuration::NHI_OUTPUT_OSIRIS_LABEL_1_X = 30;
const int Configuration::NHI_OUTPUT_OSIRIS_LABEL_1_Y = 750;
const int Configuration::NHI_OUTPUT_OSIRIS_LABEL_2_X = 185;
const int Configuration::NHI_OUTPUT_OSIRIS_LABEL_2_Y = 767;
const int Configuration::NHI_OUTPUT_OSIRIS_SCORE_X = 180;
const int Configuration::NHI_OUTPUT_OSIRIS_SCORE_Y = 743;
const int Configuration::NHI_OUTPUT_OSIRIS_CLASS_X = 280;
const int Configuration::NHI_OUTPUT_OSIRIS_CLASS_Y = 750;

const string Configuration::NHI_OUTPUT_BSIF_LABEL_1 = "BSIF Filters";
const string Configuration::NHI_OUTPUT_BSIF_LABEL_2 = "thr:";
const int Configuration::NHI_OUTPUT_BSIF_LABEL_1_X = 485;
const int Configuration::NHI_OUTPUT_BSIF_LABEL_1_Y = 750;
const int Configuration::NHI_OUTPUT_BSIF_LABEL_2_X = 625;
const int Configuration::NHI_OUTPUT_BSIF_LABEL_2_Y = 767;
const int Configuration::NHI_OUTPUT_BSIF_SCORE_X = 620;
const int Configuration::NHI_OUTPUT_BSIF_SCORE_Y = 743;
const int Configuration::NHI_OUTPUT_BSIF_CLASS_X = 720;
const int Configuration::NHI_OUTPUT_BSIF_CLASS_Y = 750;

// manual annotation window
const string Configuration::ANNOT_CTRL_WINDOW_TITLE = "Manual Annotation";
const int Configuration::ANNOT_CTRL_WINDOW_X = 870;
const int Configuration::ANNOT_CTRL_WINDOW_Y = 500;
const int Configuration::ANNOT_CTRL_WINDOW_WIDTH = 420;
const int Configuration::ANNOT_CTRL_WINDOW_HEIGHT = 200;

const string Configuration::ANNOT_TYPE_CHKBOX_GROUP_LABEL = "Annotate...";
const int Configuration::ANNOT_TYPE_CHKBOX_GROUP_X = 890;
const int Configuration::ANNOT_TYPE_CHKBOX_GROUP_Y = 540;

const string Configuration::ANNOT_TYPE_CHKBOX_MATCH_LABEL = "Matching Regions";
const int Configuration::ANNOT_TYPE_CHKBOX_MATCH_X = 890;
const int Configuration::ANNOT_TYPE_CHKBOX_MATCH_Y = 560;
const Scalar Configuration::ANNOT_TYPE_MATCH_COLOR(0, 255, 0);

const string Configuration::ANNOT_TYPE_CHKBOX_NON_MATCH_LABEL = "Non-Matching Regions";
const int Configuration::ANNOT_TYPE_CHKBOX_NON_MATCH_X = 1080;
const int Configuration::ANNOT_TYPE_CHKBOX_NON_MATCH_Y = 560;
const Scalar Configuration::ANNOT_TYPE_NON_MATCH_COLOR(0, 0, 255);

const string Configuration::ANNOT_SHOW_MATCH_LABEL = "Show Matching Regions";
const int Configuration::ANNOT_SHOW_MATCH_X = 890;
const int Configuration::ANNOT_SHOW_MATCH_Y = 620;

const string Configuration::ANNOT_SHOW_NON_MATCH_LABEL = "Show Non-Matching Regions";
const int Configuration::ANNOT_SHOW_NON_MATCH_X = 890;
const int Configuration::ANNOT_SHOW_NON_MATCH_Y = 640;

// human-interpretable feature window
const int Configuration::HI_INITIAL_MATCH_COUNT = 5;
const int Configuration::HI_INITIAL_NON_MATCH_COUNT = 1;
const double Configuration::HI_SCORE_LABEL_SIZE = 0.3;

const string Configuration::HI_FEAT_CTRL_WINDOW_TITLE = "Human-Interpretable Features";
const int Configuration::HI_FEAT_CTRL_WINDOW_X = 10;
const int Configuration::HI_FEAT_CTRL_WINDOW_Y = 500;
const int Configuration::HI_FEAT_CTRL_WINDOW_WIDTH = 850;
const int Configuration::HI_FEAT_CTRL_WINDOW_HEIGHT = 200;
const string Configuration::HI_FEAT_CHKBOX_MATCH_LABEL = "Show Matched";
const string Configuration::HI_FEAT_CHKBOX_NON_MATCH_LABEL = "Show Unmatched";

const string Configuration::HI_FEAT_TSHEPII_LABEL = "TSHEPII";
const int Configuration::HI_FEAT_TSHEPII_LABEL_X = 30;
const int Configuration::HI_FEAT_TSHEPII_LABEL_Y = 540;
const int Configuration::HI_FEAT_TSHEPII_CLASS_X = 30;
const int Configuration::HI_FEAT_TSHEPII_CLASS_Y = 562;
const int Configuration::HI_FEAT_TSHEPII_CHKBOX_MATCH_X = 110;
const int Configuration::HI_FEAT_TSHEPII_CHKBOX_MATCH_Y = 540;
const int Configuration::HI_FEAT_TSHEPII_CHKBOX_NON_MATCH_X = 110;
const int Configuration::HI_FEAT_TSHEPII_CHKBOX_NON_MATCH_Y = 560;
const int Configuration::HI_FEAT_TSHEPII_COUNT_MATCH_X = 255;
const int Configuration::HI_FEAT_TSHEPII_COUNT_MATCH_Y = 540;
const int Configuration::HI_FEAT_TSHEPII_COUNT_NON_MATCH_X = 255;
const int Configuration::HI_FEAT_TSHEPII_COUNT_NON_MATCH_Y = 560;
const int Configuration::HI_FEAT_TSHEPII_MAX_MATCH_X = 355;
const int Configuration::HI_FEAT_TSHEPII_MAX_MATCH_Y = 545;
const int Configuration::HI_FEAT_TSHEPII_MAX_NON_MATCH_X = 355;
const int Configuration::HI_FEAT_TSHEPII_MAX_NON_MATCH_Y = 565;
const Scalar Configuration::HI_FEAT_TSHEPII_COLOR(255, 0, 0);

const string Configuration::HI_FEAT_SURF_LABEL = "SURF";
const int Configuration::HI_FEAT_SURF_LABEL_X = 30;
const int Configuration::HI_FEAT_SURF_LABEL_Y = 600;
const int Configuration::HI_FEAT_SURF_CLASS_X = 30;
const int Configuration::HI_FEAT_SURF_CLASS_Y = 622;
const int Configuration::HI_FEAT_SURF_CHKBOX_MATCH_X = 110;
const int Configuration::HI_FEAT_SURF_CHKBOX_MATCH_Y = 600;
const int Configuration::HI_FEAT_SURF_CHKBOX_NON_MATCH_X = 110;
const int Configuration::HI_FEAT_SURF_CHKBOX_NON_MATCH_Y = 620;
const int Configuration::HI_FEAT_SURF_COUNT_MATCH_X = 255;
const int Configuration::HI_FEAT_SURF_COUNT_MATCH_Y = 600;
const int Configuration::HI_FEAT_SURF_COUNT_NON_MATCH_X = 255;
const int Configuration::HI_FEAT_SURF_COUNT_NON_MATCH_Y = 620;
const int Configuration::HI_FEAT_SURF_MAX_MATCH_X = 355;
const int Configuration::HI_FEAT_SURF_MAX_MATCH_Y = 605;
const int Configuration::HI_FEAT_SURF_MAX_NON_MATCH_X = 355;
const int Configuration::HI_FEAT_SURF_MAX_NON_MATCH_Y = 625;
const Scalar Configuration::HI_FEAT_SURF_COLOR(0, 130, 255);

const string Configuration::HI_FEAT_SIFT_LABEL = "SIFT";
const int Configuration::HI_FEAT_SIFT_LABEL_X = 460;
const int Configuration::HI_FEAT_SIFT_LABEL_Y = 600;
const int Configuration::HI_FEAT_SIFT_CLASS_X = 460;
const int Configuration::HI_FEAT_SIFT_CLASS_Y = 622;
const int Configuration::HI_FEAT_SIFT_CHKBOX_MATCH_X = 530;
const int Configuration::HI_FEAT_SIFT_CHKBOX_MATCH_Y = 600;
const int Configuration::HI_FEAT_SIFT_CHKBOX_NON_MATCH_X = 530;
const int Configuration::HI_FEAT_SIFT_CHKBOX_NON_MATCH_Y = 620;
const int Configuration::HI_FEAT_SIFT_COUNT_MATCH_X = 675;
const int Configuration::HI_FEAT_SIFT_COUNT_MATCH_Y = 600;
const int Configuration::HI_FEAT_SIFT_COUNT_NON_MATCH_X = 675;
const int Configuration::HI_FEAT_SIFT_COUNT_NON_MATCH_Y = 620;
const int Configuration::HI_FEAT_SIFT_MAX_MATCH_X = 775;
const int Configuration::HI_FEAT_SIFT_MAX_MATCH_Y = 605;
const int Configuration::HI_FEAT_SIFT_MAX_NON_MATCH_X = 775;
const int Configuration::HI_FEAT_SIFT_MAX_NON_MATCH_Y = 625;
const Scalar Configuration::HI_FEAT_SIFT_COLOR(255, 0, 255);

const string Configuration::HI_FEAT_MSER_LABEL = "MSER";
const int Configuration::HI_FEAT_MSER_LABEL_X = 460;
const int Configuration::HI_FEAT_MSER_LABEL_Y = 540;
const int Configuration::HI_FEAT_MSER_CLASS_X = 460;
const int Configuration::HI_FEAT_MSER_CLASS_Y = 562;
const int Configuration::HI_FEAT_MSER_CHKBOX_MATCH_X = 530;
const int Configuration::HI_FEAT_MSER_CHKBOX_MATCH_Y = 540;
const int Configuration::HI_FEAT_MSER_CHKBOX_NON_MATCH_X = 530;
const int Configuration::HI_FEAT_MSER_CHKBOX_NON_MATCH_Y = 560;
const int Configuration::HI_FEAT_MSER_COUNT_MATCH_X = 675;
const int Configuration::HI_FEAT_MSER_COUNT_MATCH_Y = 540;
const int Configuration::HI_FEAT_MSER_COUNT_NON_MATCH_X = 675;
const int Configuration::HI_FEAT_MSER_COUNT_NON_MATCH_Y = 560;
const int Configuration::HI_FEAT_MSER_MAX_MATCH_X = 775;
const int Configuration::HI_FEAT_MSER_MAX_MATCH_Y = 545;
const int Configuration::HI_FEAT_MSER_MAX_NON_MATCH_X = 775;
const int Configuration::HI_FEAT_MSER_MAX_NON_MATCH_Y = 565;
const Scalar Configuration::HI_FEAT_MSER_COLOR(0, 255, 255);

const string Configuration::HI_FEAT_CRYPTS_LABEL = "Crypts";
const int Configuration::HI_FEAT_CRYPTS_LABEL_X = 30;
const int Configuration::HI_FEAT_CRYPTS_LABEL_Y = 660;
const int Configuration::HI_FEAT_CRYPTS_CLASS_X = 30;
const int Configuration::HI_FEAT_CRYPTS_CLASS_Y = 682;
const int Configuration::HI_FEAT_CRYPTS_CHKBOX_MATCH_X = 110;
const int Configuration::HI_FEAT_CRYPTS_CHKBOX_MATCH_Y = 660;
const int Configuration::HI_FEAT_CRYPTS_COUNT_MATCH_X = 255;
const int Configuration::HI_FEAT_CRYPTS_COUNT_MATCH_Y = 660;
const int Configuration::HI_FEAT_CRYPTS_MAX_MATCH_X = 355;
const int Configuration::HI_FEAT_CRYPTS_MAX_MATCH_Y = 660;
const Scalar Configuration::HI_FEAT_CRYPTS_COLOR(255, 255, 0);

const int Configuration::HI_UNDO_REMOVAL_BUTTON_X = 675;
const int Configuration::HI_UNDO_REMOVAL_BUTTON_Y = 660;
const int Configuration::HI_UNDO_REMOVAL_BUTTON_WIDTH = 160;
const int Configuration::HI_UNDO_REMOVAL_BUTTON_HEIGHT = 30;
const string Configuration::HI_UNDO_REMOVAL_BUTTON_LABEL = "Undo last removal";

// score window
const string Configuration::SCORE_WINDOW_TITLE = "Global match score";
const int Configuration::SCORE_WINDOW_X = 870;
const int Configuration::SCORE_WINDOW_Y = 710;
const int Configuration::SCORE_WINDOW_WIDTH = 420;
const int Configuration::SCORE_WINDOW_HEIGHT = 80;
const double Configuration::SCORE_LABEL_SIZE = 0.6;
const int Configuration::SCORE_LABEL_X = 950;
const int Configuration::SCORE_LABEL_Y = 750;
const double Configuration::SCORE_VALUE_SIZE = 0.6;
const int Configuration::SCORE_VALUE_X = 1150;
const int Configuration::SCORE_VALUE_Y = 750;
const unsigned int Configuration::SCORE_VALUE_COLOR = 0xffffff;
const string Configuration::SCORE_GENUINE_LABEL = "genuine";
const unsigned int Configuration::SCORE_GENUINE_COLOR = 0xffffff;
const string Configuration::SCORE_IMPOSTOR_LABEL = "impostor";
const unsigned int Configuration::SCORE_IMPOSTOR_COLOR = 0xffffff;

// remove-annotation popup window
const string Configuration::ANNOT_REMOVAL_POPUP_TITLE = "Annotation";
const int Configuration::ANNOT_REMOVAL_POPUP_WIDTH = 100;
const int Configuration::ANNOT_REMOVAL_POPUP_HEIGHT = 60;
const string Configuration::ANNOT_REMOVAL_POPUP_REMOVE_OP = "Remove";
const string Configuration::ANNOT_REMOVAL_POPUP_CANCEL_OP = "Cancel";

// remove-keypoint popup window
const string Configuration::KP_REMOVAL_POPUP_TITLE = "Keypoint";
const int Configuration::KP_REMOVAL_POPUP_WIDTH = 100;
const int Configuration::KP_REMOVAL_POPUP_HEIGHT = 60;
const string Configuration::KP_REMOVAL_POPUP_REMOVE_OP = "Remove";
const string Configuration::KP_REMOVAL_POPUP_CANCEL_OP = "Cancel";

// report content
const string Configuration::REPORT_TITLE = "TSHEPII Report";

const string Configuration::REPORT_HEADER_1 = "Examined Irises";
const string Configuration::REPORT_TABLE_1_HEADER_1 = "Iris 1";
const string Configuration::REPORT_TABLE_1_HEADER_2 = "Iris 2";

const string Configuration::REPORT_HEADER_2 = "Annotations";
const string Configuration::REPORT_TABLE_2_HEADER_1 = "Matched (#)";
const string Configuration::REPORT_TABLE_2_HEADER_2 = "Unmatched (#)";

const string Configuration::REPORT_HEADER_3 = "TSHEPII Descriptor Matches";
const string Configuration::REPORT_TABLE_3_HEADER_1 = "Matched (#)";
const string Configuration::REPORT_TABLE_3_HEADER_2 = "Unmatched (#)";

const string Configuration::REPORT_HEADER_4 = "SURF Matches";
const string Configuration::REPORT_TABLE_4_HEADER_1 = "Matched (#)";
const string Configuration::REPORT_TABLE_4_HEADER_2 = "Unmatched (#)";

const string Configuration::REPORT_HEADER_5 = "SIFT Matches";
const string Configuration::REPORT_TABLE_5_HEADER_1 = "Matched (#)";
const string Configuration::REPORT_TABLE_5_HEADER_2 = "Unmatched (#)";

const string Configuration::REPORT_HEADER_6 = "MSER Matches";
const string Configuration::REPORT_TABLE_6_HEADER_1 = "Matched (#)";
const string Configuration::REPORT_TABLE_6_HEADER_2 = "Unmatched (#)";

const string Configuration::REPORT_HEADER_7 = "Crypt Matches";
const string Configuration::REPORT_TABLE_7_HEADER_1 = "Matched (#)";

const string Configuration::REPORT_HEADER_8 = "Scores";
const string Configuration::REPORT_TABLE_8_HEADER_1 = "Method";
const string Configuration::REPORT_TABLE_8_HEADER_2 = "Threshold";
const string Configuration::REPORT_TABLE_8_HEADER_3 = "Score";
const string Configuration::REPORT_TABLE_8_HEADER_4 = "Genuine?";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_01 = "Gabor Filters";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_02 = "BSIF Filters";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_03 = "TSHEPII Matched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_04 = "TSHEPII Unmatched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_05 = "SURF Matched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_06 = "SURF Unmatched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_07 = "SIFT Matched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_08 = "SIFT Unmatched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_09 = "MSER Matched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_10 = "MSER Unmatched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_11 = "Crypt Matched";
const string Configuration::REPORT_TABLE_8_COLUMN_1_ROW_12 = "Combined";

const string Configuration::REPORT_GENUINE_LABEL = "Yes";
const string Configuration::REPORT_IMPOSTOR_LABEL = "No";

// manual iris segmentation window
const string Configuration::SEGMENT_WINDOW_TITLE = "Segment";
const int Configuration::SEGMENT_WINDOW_BOTTOM_HEIGHT = 60;
const int Configuration::SEGMENT_WINDOW_HORIZ_PADDING = 5;
const int Configuration::SEGMENT_WINDOW_VERTC_PADDING = 25;
const int Configuration::SEGMENT_IRIS_HEIGHT = 500;
const string Configuration::SEGMENT_MESSAGE_1 = "Select 3 or more points over the pupil border.";
const string Configuration::SEGMENT_MESSAGE_2 = "Select 3 or more points over the iris border.";
const string Configuration::SEGMENT_MESSAGE_3 = "Draw on the border of region to remove.";
const string Configuration::SEGMENT_BUTTON_LABEL_1 = "Next step";
const string Configuration::SEGMENT_BUTTON_LABEL_2 = "Restart step";
const string Configuration::SEGMENT_BUTTON_LABEL_3 = "Cancel segmentation";
const string Configuration::SEGMENT_BUTTON_LABEL_4 = "Previous step";
const int Configuration::SEGMENT_POINT_SIZE = 3;
const int Configuration::SEGMENT_LINE_WIDTH = 1;
const int Configuration::SEGMENT_CTRL_PNT_DIST_TOLERANCE = 7;
const Scalar Configuration::SEGMENT_PUPIL_COLOR(0, 255, 255);
const Scalar Configuration::SEGMENT_IRIS_COLOR(255, 0, 0);
const Scalar Configuration::SEGMENT_REGION_COLOR(0, 0, 255);
const Scalar Configuration::SEGMENT_CLOSED_REGION_COLOR(0, 255, 0);
