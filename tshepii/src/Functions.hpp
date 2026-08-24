/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef FUNCTIONS_H
#define FUNCTIONS_H

#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

/** Implements a facade/singleton containing auxiliary functions to the TSHEPII tool. */
class Functions {
public:
	/** Splits a given string, according to the given delimiter chars.
	 *  @param [in] s The string to be split.
	 *  @param [in] delimiter The delimiter char used as separator of splits.
	 *  @param [out] tokens A list containing the resulting split parts of the string. */
	static void split(string *s, string delimiters, vector<string> *tokens);

	/** Returns current date/time, format is "YYYY-MM-DD   HH:mm:ss".
	 *  @return A string with the current date-time. */
	static string getCurrentDateTime();

	/** Helps to sort lists of keypoint matches.
	 *  @param [in] match1 One of the matches to be compared.
	 *  @param [in] match2 The other one of the matches to be compared.
	 *  @return TRUE if match1 comes first than match2, in an eventual list, FALSE otherwise. */
	static bool compareMatches(DMatch match1, DMatch match2);

	/** Helps to sort lists of keypoints.
	 *  @param [in] kp1 One of the keypoints to be compared.
	 *  @param [in] kp2 The other one of the keypoints to be compared.
	 *  @return TRUE if kp1 comes first than kp2, in an eventual list, FALSE otherwise. */
	static bool compareKeypoints(KeyPoint kp1, KeyPoint kp2);

	/** Computes the mean and standard deviation of the given data.
	 *  @param [in] data The data whose mean and standard deviation are wanted.
	 *  @param [out] mean The computed mean.
	 *  @param [out] std  The computed standard deviation.
	 *  @return void */
	static void computeMeanStd(vector<double> *data, double *mean, double *std);
};

#endif
