/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef CONFIG_H
#define CONFIG_H

#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

/** Singleton for loading TSHEPII configuration values. */
class Configuration {
public:
	/* General configuration. */
	static const string FILE_SEPARATOR;
	static const int PROCESSED_IRIS_IMAGE_COUNT;
	static const int SCORE_COUNT;

	/* OSIRIS configuration. */
	static const double OSIRIS_THRESHOLD;
	static int OSIRIS_MIN_IRIS_DIAMETER;
	static int OSIRIS_MIN_PUPIL_DIAMETER;
	static int OSIRIS_MAX_IRIS_DIAMETER;
	static int OSIRIS_MAX_PUPIL_DIAMETER;
	static int OSIRIS_NORM_IMAGE_COLS;
	static int OSIRIS_NORM_IMAGE_ROWS;

	/* BSIF configuration. */
	static const double BSIF_THRESHOLD;
	static const int BSIF_NORM_IMAGE_COLS;
	static const int BSIF_NORM_IMAGE_ROWS;

	/* Iris content pre-processing. */
	static const int IRIS_MASK_BLUR_SIZE;
	static const int IRIS_CLAHE_TILES;
	static const double IRIS_CLAHE_CLIP;

	/* Interest point configuration. */
	// All detectors
	static const int KP_LINE_WIDTH;
	static const int KP_MIN_SIZE;
	static const int KP_MAX_SIZE;
	static const double KP_COINCIDENCE_TOLERANCE;
	static const int KP_MATCHER_KNN_K;
	static const double KP_MATCHER_NORM_L2_QUALITY_RATIO;
	static const double KP_MATCHER_NORM_HAMMING_QUALITY_RATIO;
	static const double KP_MATCHER_DEVIATION_TOLERANCE;
	static const double KP_MATCHER_POLAR_RADIUS_TOLERANCE;
	static const double KP_MATCHER_POLAR_ANGLE_TOLERANCE;

	// Crypts
	static const double CRYPT_THRESHOLD;
	static const Scalar CRYPTS_COLOR;
	static const int CRYPTS_LINE_WIDTH;
	static const int CRYPTS_SMOOTH_GAUSS_SIZE;
	static const int CRYPTS_SMOOTH_GAUSS_SIGMA;
	static const int CRYPTS_MIN_ITERATIVE_STR_EL_SIZE;
	static const int CRYPTS_INITIAL_ITERATIVE_STR_EL_SIZE;
	static const double CRYPTS_MIN_CRYPT_AREA;
	static const double CRYPTS_MAX_CRYP_AREA_LB;
	static const double CRYPTS_MIN_CRYP_AREA_UB;
	static const double CRYPTS_CUT_STD;
	static const double CRYPTS_MATCH_COINCIDENCE_TOLERANCE;
	static const double CRYPTS_MATCH_DIST_ALPHA;

	// TSHEPII
	static const double TSHEPII_MATCH_THRESHOLD;
	static const double TSHEPII_MISMATCH_THRESHOLD;
	static const double TSHEPII_BORDER_AVOIDANCE;
	static const double TSHEPII_OVERLAP_TOLERANCE;
	static const double TSHEPII_KP_THRESHOLD;
	static const int TSHEPII_CELL_DIVISION;
	static const double TSHEPII_BIN_THRESHOLD;
	static const string TSHEPII_FILTER_LIST_FILEPATH;
	static const string TSHEPII_ZND_LIST_FILEPATH;
	static const Scalar TSHEPII_COLOR;

	// SURF
	static const double SURF_MATCH_THRESHOLD;
	static const double SURF_MISMATCH_THRESHOLD;
	static const double SURF_HESSIAN_THRESH;
	static const Scalar SURF_COLOR;

	// SIFT
	static const double SIFT_MATCH_THRESHOLD;
	static const double SIFT_MISMATCH_THRESHOLD;
	static const int SIFT_KP_COUNT;
	static const int SIFT_OCTAVE_LAYER_COUNT;
	static const double SIFT_CONTRAST_THRESH;
	static const double SIFT_EDGE_THRESH;
	static const Scalar SIFT_COLOR;

	// MSER
	static const double MSER_MATCH_THRESHOLD;
	static const double MSER_MISMATCH_THRESHOLD;
	static const int MSER_DELTA;
	static const int MSER_MIN_AREA;
	static const int MSER_MAX_AREA;
	static const double MSER_MAX_VARIATION;
	static const Scalar MSER_COLOR;

	/* Annotation configuration. */
	static const int ANNOT_CTRL_PNT_SIZE;
	static const int ANNOT_CTRL_PNT_SIZE_SELECTED;
	static const int ANNOT_CTRL_PNT_DIST_TOLERANCE;
	static const int ANNOT_LINE_WIDTH;
	static const int ANNOT_LINE_WIDTH_SELECTED;
	static const int ANNOT_SELECTION_TOLERANCE;
	static const int ANNOT_CTRL_PNT_MIN_COUNT;

	/* Software static configuration. */
	// main window
	static const string MAIN_WINDOW_TITLE;
	static const int MAIN_WINDOW_WIDTH;
	static const int MAIN_WINDOW_HEIGHT;
	static const Scalar MAIN_WINDOW_BACKGROUND_COLOR;

	// main menu
	static const int MAIN_MENU_X;
	static const int MAIN_MENU_Y;
	static const int MAIN_MENU_HEIGHT;

	static const string MAIN_MENU_LOAD_IRIS_LABEL;
	static const int MAIN_MENU_LOAD_IRIS_WIDTH;

	static const string MAIN_MENU_LOAD_EXAMINATION_LABEL;
	static const int MAIN_MENU_LOAD_EXAMINATION_WIDTH;

	static const string MAIN_MENU_SAVE_EXAMINATION_LABEL;
	static const int MAIN_MENU_SAVE_EXAMINATION_WIDTH;

	static const string MAIN_MENU_SAVE_REPORT_LABEL;
	static const int MAIN_MENU_SAVE_REPORT_WIDTH;

	static const string MAIN_MENU_QUIT_PROGRAM_LABEL;
	static const int MAIN_MENU_QUIT_PROGRAM_WIDTH;
	static const string MAIN_MENU_QUIT_PROGRAM_QUESTION;

	// modal messages
	static const int MODAL_WIDTH;
	static const int MODAL_HEIGHT;
	static const int MODAL_BUTTON_HEIGHT;
	static const string MODAL_BUTTON_LABEL;

	// yes/no dialog
	static const int DIALOG_WIDTH;
	static const int DIALOG_HEIGHT;
	static const int DIALOG_BUTTON_HEIGHT;
	static const string DIALOG_YES_LABEL;
	static const string DIALOG_NO_LABEL;

	// progress bar
	static const string PROGRESS_TITLE;
	static const int PROGRESS_WIDTH;
	static const int PROGRESS_HEIGHT;
	static const int PROGRESS_BAR_HEIGHT;
	static const int PROGRESS_BAR_BORDER;
	static const Scalar PROGRESS_BAR_COLOR;

	// iris image loading
	static const char *LOAD_IRIS_EXTENSIONS;
	static const string LOAD_IRIS_WARN_MSG_1;
	static const string LOAD_IRIS_WARN_MSG_2;
	static const string LOAD_IRIS_WARN_MSG_3;
	static const string LOAD_IRIS_WARN_MSG_4;
	static const string LOAD_IRIS_WARN_MSG_5;

	// examination saving and loading
	static const string SAVE_EXAM_WARN_MSG_1;
	static const string SAVE_EXAM_WARN_MSG_2;
	static const string LOAD_EXAM_WARN_MSG_1;

	// report saving
	static const string SAVE_REPORT_WARN_MSG_1;

	// iris windows
	static const int IRIS_WIDTH;
	static const int IRIS_HEIGHT;
	static const int IRIS_RADIUS_OFFSET;
	static const int IRIS_1_X;
	static const int IRIS_1_Y;
	static const int IRIS_2_X;
	static const int IRIS_2_Y;

	// iris image processing windows
	static const double IRIS_BRIGHT_MIN;
	static const double IRIS_BRIGHT_MAX;

	static const double IRIS_CONTRAST_MIN;
	static const double IRIS_CONTRAST_MAX;

	static const int IRIS_SHARP_MIN;
	static const int IRIS_SHARP_MAX;

	static const string IRIS_BRIGHT_TITLE;
	static const string IRIS_CONTRAST_TITLE;
	static const string IRIS_SHARP_TITLE;

	static const int IRIS_1_CTRL_WINDOW_X;
	static const int IRIS_1_CTRL_WINDOW_Y;
	static const int IRIS_1_CTRL_WINDOW_WIDTH;
	static const int IRIS_1_CTRL_WINDOW_HEIGHT;

	static const int IRIS_1_BRIGHT_TITLE_X;
	static const int IRIS_1_BRIGHT_TITLE_Y;
	static const int IRIS_1_BRIGHT_TRACK_X;
	static const int IRIS_1_BRIGHT_TRACK_Y;
	static const int IRIS_1_BRIGHT_TRACK_WIDTH;

	static const int IRIS_1_CONTRAST_TITLE_X;
	static const int IRIS_1_CONTRAST_TITLE_Y;
	static const int IRIS_1_CONTRAST_TRACK_X;
	static const int IRIS_1_CONTRAST_TRACK_Y;
	static const int IRIS_1_CONTRAST_TRACK_WIDTH;

	static const int IRIS_1_SHARP_TITLE_X;
	static const int IRIS_1_SHARP_TITLE_Y;
	static const int IRIS_1_SHARP_TRACK_X;
	static const int IRIS_1_SHARP_TRACK_Y;
	static const int IRIS_1_SHARP_TRACK_WIDTH;

	static const string IRIS_1_SEGMENT_BUTTON_LABEL;
	static const int IRIS_1_SEGMENT_BUTTON_X;
	static const int IRIS_1_SEGMENT_BUTTON_Y;
	static const int IRIS_1_SEGMENT_BUTTON_WIDTH;
	static const int IRIS_1_SEGMENT_BUTTON_HEIGHT;

	static const int IRIS_2_CTRL_WINDOW_X;
	static const int IRIS_2_CTRL_WINDOW_Y;
	static const int IRIS_2_CTRL_WINDOW_WIDTH;
	static const int IRIS_2_CTRL_WINDOW_HEIGHT;

	static const int IRIS_2_BRIGHT_TITLE_X;
	static const int IRIS_2_BRIGHT_TITLE_Y;
	static const int IRIS_2_BRIGHT_TRACK_X;
	static const int IRIS_2_BRIGHT_TRACK_Y;
	static const int IRIS_2_BRIGHT_TRACK_WIDTH;

	static const int IRIS_2_CONTRAST_TITLE_X;
	static const int IRIS_2_CONTRAST_TITLE_Y;
	static const int IRIS_2_CONTRAST_TRACK_X;
	static const int IRIS_2_CONTRAST_TRACK_Y;
	static const int IRIS_2_CONTRAST_TRACK_WIDTH;

	static const int IRIS_2_SHARP_TITLE_X;
	static const int IRIS_2_SHARP_TITLE_Y;
	static const int IRIS_2_SHARP_TRACK_X;
	static const int IRIS_2_SHARP_TRACK_Y;
	static const int IRIS_2_SHARP_TRACK_WIDTH;

	static const string IRIS_2_SEGMENT_BUTTON_LABEL;
	static const int IRIS_2_SEGMENT_BUTTON_X;
	static const int IRIS_2_SEGMENT_BUTTON_Y;
	static const int IRIS_2_SEGMENT_BUTTON_WIDTH;
	static const int IRIS_2_SEGMENT_BUTTON_HEIGHT;

	// iris zooming
	static const double IRIS_ZOOM_MIN;
	static const double IRIS_ZOOM_MAX;

	static const int IRIS_1_ZOOM_CTRL_WINDOW_X;
	static const int IRIS_1_ZOOM_CTRL_WINDOW_Y;
	static const int IRIS_1_ZOOM_CTRL_WINDOW_WIDTH;
	static const int IRIS_1_ZOOM_CTRL_WINDOW_HEIGHT;

	static const int IRIS_2_ZOOM_CTRL_WINDOW_X;
	static const int IRIS_2_ZOOM_CTRL_WINDOW_Y;
	static const int IRIS_2_ZOOM_CTRL_WINDOW_WIDTH;
	static const int IRIS_2_ZOOM_CTRL_WINDOW_HEIGHT;

	static const int IRIS_CONNECT_BTN_X;
	static const int IRIS_CONNECT_BTN_Y;
	static const int IRIS_CONNECT_BTN_WIDTH;
	static const int IRIS_CONNECT_BTN_HEIGHT;
	static const string IRIS_CONNECT_BTN_LABEL;

	// human-interpretable and annotation windows: common features
	static const int COLOR_FLAG_SIZE;
	static const int COLOR_FLAG_PADDING;

	// non-human-interpretable output window
	static const string NHI_OUTPUT_WINDOW_TITLE;
	static const int NHI_OUTPUT_WINDOW_X;
	static const int NHI_OUTPUT_WINDOW_Y;
	static const int NHI_OUTPUT_WINDOW_WIDTH;
	static const int NHI_OUTPUT_WINDOW_HEIGHT;

	static const double NHI_OUTPUT_SCORE_SIZE_1;
	static const double NHI_OUTPUT_SCORE_SIZE_2;
	static const unsigned int NHI_OUTPUT_SCORE_COLOR;

	static const string NHI_OUTPUT_OSIRIS_LABEL_1;
	static const string NHI_OUTPUT_OSIRIS_LABEL_2;
	static const int NHI_OUTPUT_OSIRIS_LABEL_1_X;
	static const int NHI_OUTPUT_OSIRIS_LABEL_1_Y;
	static const int NHI_OUTPUT_OSIRIS_LABEL_2_X;
	static const int NHI_OUTPUT_OSIRIS_LABEL_2_Y;
	static const int NHI_OUTPUT_OSIRIS_SCORE_X;
	static const int NHI_OUTPUT_OSIRIS_SCORE_Y;
	static const int NHI_OUTPUT_OSIRIS_CLASS_X;
	static const int NHI_OUTPUT_OSIRIS_CLASS_Y;

	static const string NHI_OUTPUT_BSIF_LABEL_1;
	static const string NHI_OUTPUT_BSIF_LABEL_2;
	static const int NHI_OUTPUT_BSIF_LABEL_1_X;
	static const int NHI_OUTPUT_BSIF_LABEL_1_Y;
	static const int NHI_OUTPUT_BSIF_LABEL_2_X;
	static const int NHI_OUTPUT_BSIF_LABEL_2_Y;
	static const int NHI_OUTPUT_BSIF_SCORE_X;
	static const int NHI_OUTPUT_BSIF_SCORE_Y;
	static const int NHI_OUTPUT_BSIF_CLASS_X;
	static const int NHI_OUTPUT_BSIF_CLASS_Y;

	// manual annotation window
	static const string ANNOT_CTRL_WINDOW_TITLE;
	static const int ANNOT_CTRL_WINDOW_X;
	static const int ANNOT_CTRL_WINDOW_Y;
	static const int ANNOT_CTRL_WINDOW_WIDTH;
	static const int ANNOT_CTRL_WINDOW_HEIGHT;

	static const string ANNOT_TYPE_CHKBOX_GROUP_LABEL;
	static const int ANNOT_TYPE_CHKBOX_GROUP_X;
	static const int ANNOT_TYPE_CHKBOX_GROUP_Y;

	static const string ANNOT_TYPE_CHKBOX_MATCH_LABEL;
	static const int ANNOT_TYPE_CHKBOX_MATCH_X;
	static const int ANNOT_TYPE_CHKBOX_MATCH_Y;
	static const Scalar ANNOT_TYPE_MATCH_COLOR;

	static const string ANNOT_TYPE_CHKBOX_NON_MATCH_LABEL;
	static const int ANNOT_TYPE_CHKBOX_NON_MATCH_X;
	static const int ANNOT_TYPE_CHKBOX_NON_MATCH_Y;
	static const Scalar ANNOT_TYPE_NON_MATCH_COLOR;

	static const string ANNOT_SHOW_MATCH_LABEL;
	static const int ANNOT_SHOW_MATCH_X;
	static const int ANNOT_SHOW_MATCH_Y;

	static const string ANNOT_SHOW_NON_MATCH_LABEL;
	static const int ANNOT_SHOW_NON_MATCH_X;
	static const int ANNOT_SHOW_NON_MATCH_Y;

	// human-interpretable feature window
	static const int HI_INITIAL_MATCH_COUNT;
	static const int HI_INITIAL_NON_MATCH_COUNT;
	static const double HI_SCORE_LABEL_SIZE;

	static const string HI_FEAT_CTRL_WINDOW_TITLE;
	static const int HI_FEAT_CTRL_WINDOW_X;
	static const int HI_FEAT_CTRL_WINDOW_Y;
	static const int HI_FEAT_CTRL_WINDOW_WIDTH;
	static const int HI_FEAT_CTRL_WINDOW_HEIGHT;
	static const string HI_FEAT_CHKBOX_MATCH_LABEL;
	static const string HI_FEAT_CHKBOX_NON_MATCH_LABEL;

	static const string HI_FEAT_TSHEPII_LABEL;
	static const int HI_FEAT_TSHEPII_LABEL_X;
	static const int HI_FEAT_TSHEPII_LABEL_Y;
	static const int HI_FEAT_TSHEPII_CLASS_X;
	static const int HI_FEAT_TSHEPII_CLASS_Y;
	static const int HI_FEAT_TSHEPII_CHKBOX_MATCH_X;
	static const int HI_FEAT_TSHEPII_CHKBOX_MATCH_Y;
	static const int HI_FEAT_TSHEPII_CHKBOX_NON_MATCH_X;
	static const int HI_FEAT_TSHEPII_CHKBOX_NON_MATCH_Y;
	static const int HI_FEAT_TSHEPII_COUNT_MATCH_X;
	static const int HI_FEAT_TSHEPII_COUNT_MATCH_Y;
	static const int HI_FEAT_TSHEPII_COUNT_NON_MATCH_X;
	static const int HI_FEAT_TSHEPII_COUNT_NON_MATCH_Y;
	static const int HI_FEAT_TSHEPII_MAX_MATCH_X;
	static const int HI_FEAT_TSHEPII_MAX_MATCH_Y;
	static const int HI_FEAT_TSHEPII_MAX_NON_MATCH_X;
	static const int HI_FEAT_TSHEPII_MAX_NON_MATCH_Y;
	static const Scalar HI_FEAT_TSHEPII_COLOR;

	static const string HI_FEAT_SURF_LABEL;
	static const int HI_FEAT_SURF_LABEL_X;
	static const int HI_FEAT_SURF_LABEL_Y;
	static const int HI_FEAT_SURF_CLASS_X;
	static const int HI_FEAT_SURF_CLASS_Y;
	static const int HI_FEAT_SURF_CHKBOX_MATCH_X;
	static const int HI_FEAT_SURF_CHKBOX_MATCH_Y;
	static const int HI_FEAT_SURF_CHKBOX_NON_MATCH_X;
	static const int HI_FEAT_SURF_CHKBOX_NON_MATCH_Y;
	static const int HI_FEAT_SURF_COUNT_MATCH_X;
	static const int HI_FEAT_SURF_COUNT_MATCH_Y;
	static const int HI_FEAT_SURF_COUNT_NON_MATCH_X;
	static const int HI_FEAT_SURF_COUNT_NON_MATCH_Y;
	static const int HI_FEAT_SURF_MAX_MATCH_X;
	static const int HI_FEAT_SURF_MAX_MATCH_Y;
	static const int HI_FEAT_SURF_MAX_NON_MATCH_X;
	static const int HI_FEAT_SURF_MAX_NON_MATCH_Y;
	static const Scalar HI_FEAT_SURF_COLOR;

	static const string HI_FEAT_SIFT_LABEL;
	static const int HI_FEAT_SIFT_LABEL_X;
	static const int HI_FEAT_SIFT_LABEL_Y;
	static const int HI_FEAT_SIFT_CLASS_X;
	static const int HI_FEAT_SIFT_CLASS_Y;
	static const int HI_FEAT_SIFT_CHKBOX_MATCH_X;
	static const int HI_FEAT_SIFT_CHKBOX_MATCH_Y;
	static const int HI_FEAT_SIFT_CHKBOX_NON_MATCH_X;
	static const int HI_FEAT_SIFT_CHKBOX_NON_MATCH_Y;
	static const int HI_FEAT_SIFT_COUNT_MATCH_X;
	static const int HI_FEAT_SIFT_COUNT_MATCH_Y;
	static const int HI_FEAT_SIFT_COUNT_NON_MATCH_X;
	static const int HI_FEAT_SIFT_COUNT_NON_MATCH_Y;
	static const int HI_FEAT_SIFT_MAX_MATCH_X;
	static const int HI_FEAT_SIFT_MAX_MATCH_Y;
	static const int HI_FEAT_SIFT_MAX_NON_MATCH_X;
	static const int HI_FEAT_SIFT_MAX_NON_MATCH_Y;
	static const Scalar HI_FEAT_SIFT_COLOR;

	static const string HI_FEAT_MSER_LABEL;
	static const int HI_FEAT_MSER_LABEL_X;
	static const int HI_FEAT_MSER_LABEL_Y;
	static const int HI_FEAT_MSER_CLASS_X;
	static const int HI_FEAT_MSER_CLASS_Y;
	static const int HI_FEAT_MSER_CHKBOX_MATCH_X;
	static const int HI_FEAT_MSER_CHKBOX_MATCH_Y;
	static const int HI_FEAT_MSER_CHKBOX_NON_MATCH_X;
	static const int HI_FEAT_MSER_CHKBOX_NON_MATCH_Y;
	static const int HI_FEAT_MSER_COUNT_MATCH_X;
	static const int HI_FEAT_MSER_COUNT_MATCH_Y;
	static const int HI_FEAT_MSER_COUNT_NON_MATCH_X;
	static const int HI_FEAT_MSER_COUNT_NON_MATCH_Y;
	static const int HI_FEAT_MSER_MAX_MATCH_X;
	static const int HI_FEAT_MSER_MAX_MATCH_Y;
	static const int HI_FEAT_MSER_MAX_NON_MATCH_X;
	static const int HI_FEAT_MSER_MAX_NON_MATCH_Y;
	static const Scalar HI_FEAT_MSER_COLOR;

	static const string HI_FEAT_CRYPTS_LABEL;
	static const int HI_FEAT_CRYPTS_LABEL_X;
	static const int HI_FEAT_CRYPTS_LABEL_Y;
	static const int HI_FEAT_CRYPTS_CLASS_X;
	static const int HI_FEAT_CRYPTS_CLASS_Y;
	static const int HI_FEAT_CRYPTS_CHKBOX_MATCH_X;
	static const int HI_FEAT_CRYPTS_CHKBOX_MATCH_Y;
	static const int HI_FEAT_CRYPTS_COUNT_MATCH_X;
	static const int HI_FEAT_CRYPTS_COUNT_MATCH_Y;
	static const int HI_FEAT_CRYPTS_MAX_MATCH_X;
	static const int HI_FEAT_CRYPTS_MAX_MATCH_Y;
	static const Scalar HI_FEAT_CRYPTS_COLOR;

	static const int HI_UNDO_REMOVAL_BUTTON_X;
	static const int HI_UNDO_REMOVAL_BUTTON_Y;
	static const int HI_UNDO_REMOVAL_BUTTON_WIDTH;
	static const int HI_UNDO_REMOVAL_BUTTON_HEIGHT;
	static const string HI_UNDO_REMOVAL_BUTTON_LABEL;

	// score window
	static const string SCORE_WINDOW_TITLE;
	static const int SCORE_WINDOW_X;
	static const int SCORE_WINDOW_Y;
	static const int SCORE_WINDOW_WIDTH;
	static const int SCORE_WINDOW_HEIGHT;
	static const double SCORE_LABEL_SIZE;
	static const int SCORE_LABEL_X;
	static const int SCORE_LABEL_Y;
	static const double SCORE_VALUE_SIZE;
	static const int SCORE_VALUE_X;
	static const int SCORE_VALUE_Y;
	static const unsigned int SCORE_VALUE_COLOR;
	static const string SCORE_GENUINE_LABEL;
	static const unsigned int SCORE_GENUINE_COLOR;
	static const string SCORE_IMPOSTOR_LABEL;
	static const unsigned int SCORE_IMPOSTOR_COLOR;

	// remove-annotation popup window
	static const string ANNOT_REMOVAL_POPUP_TITLE;
	static const int ANNOT_REMOVAL_POPUP_WIDTH;
	static const int ANNOT_REMOVAL_POPUP_HEIGHT;
	static const string ANNOT_REMOVAL_POPUP_REMOVE_OP;
	static const string ANNOT_REMOVAL_POPUP_CANCEL_OP;

	// remove-keypoint popup window
	static const string KP_REMOVAL_POPUP_TITLE;
	static const int KP_REMOVAL_POPUP_WIDTH;
	static const int KP_REMOVAL_POPUP_HEIGHT;
	static const string KP_REMOVAL_POPUP_REMOVE_OP;
	static const string KP_REMOVAL_POPUP_CANCEL_OP;

	// report content
	static const string REPORT_TITLE;

	static const string REPORT_HEADER_1;
	static const string REPORT_TABLE_1_HEADER_1;
	static const string REPORT_TABLE_1_HEADER_2;

	static const string REPORT_HEADER_2;
	static const string REPORT_TABLE_2_HEADER_1;
	static const string REPORT_TABLE_2_HEADER_2;

	static const string REPORT_HEADER_3;
	static const string REPORT_TABLE_3_HEADER_1;
	static const string REPORT_TABLE_3_HEADER_2;

	static const string REPORT_HEADER_4;
	static const string REPORT_TABLE_4_HEADER_1;
	static const string REPORT_TABLE_4_HEADER_2;

	static const string REPORT_HEADER_5;
	static const string REPORT_TABLE_5_HEADER_1;
	static const string REPORT_TABLE_5_HEADER_2;

	static const string REPORT_HEADER_6;
	static const string REPORT_TABLE_6_HEADER_1;
	static const string REPORT_TABLE_6_HEADER_2;

	static const string REPORT_HEADER_7;
	static const string REPORT_TABLE_7_HEADER_1;

	static const string REPORT_HEADER_8;
	static const string REPORT_TABLE_8_HEADER_1;
	static const string REPORT_TABLE_8_HEADER_2;
	static const string REPORT_TABLE_8_HEADER_3;
	static const string REPORT_TABLE_8_HEADER_4;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_01;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_02;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_03;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_04;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_05;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_06;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_07;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_08;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_09;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_10;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_11;
	static const string REPORT_TABLE_8_COLUMN_1_ROW_12;

	static const string REPORT_GENUINE_LABEL;
	static const string REPORT_IMPOSTOR_LABEL;

	// manual iris segmentation window
	static const string SEGMENT_WINDOW_TITLE;
	static const int SEGMENT_WINDOW_BOTTOM_HEIGHT;
	static const int SEGMENT_WINDOW_HORIZ_PADDING;
	static const int SEGMENT_WINDOW_VERTC_PADDING;
	static const int SEGMENT_IRIS_HEIGHT;
	static const string SEGMENT_MESSAGE_1;
	static const string SEGMENT_MESSAGE_2;
	static const string SEGMENT_MESSAGE_3;
	static const string SEGMENT_BUTTON_LABEL_1;
	static const string SEGMENT_BUTTON_LABEL_2;
	static const string SEGMENT_BUTTON_LABEL_3;
	static const string SEGMENT_BUTTON_LABEL_4;
	static const int SEGMENT_POINT_SIZE;
	static const int SEGMENT_LINE_WIDTH;
	static const int SEGMENT_CTRL_PNT_DIST_TOLERANCE;
	static const Scalar SEGMENT_PUPIL_COLOR;
	static const Scalar SEGMENT_IRIS_COLOR;
	static const Scalar SEGMENT_REGION_COLOR;
	static const Scalar SEGMENT_CLOSED_REGION_COLOR;
};

#endif