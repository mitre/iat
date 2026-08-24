/*****************************************************************************
* Eye Orientation CLassifier v2 (U/D + L/R)
* Author: Adam Czajka, aczajka@nd.edu, May 2016
*
* Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
* under BSD license http://opensource.org/licenses/bsd-license.php
******************************************************************************/

#pragma once

#ifndef ACII_CLIENT_INCLUDE_EOC2UTILS_H_
#define ACII_CLIENT_INCLUDE_EOC2UTILS_H_

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/ml/ml.hpp>

#include <stdexcept>
#include <iostream>
#include <string>
#include <algorithm>  // for lowercase/uppercase conversions
#include <sstream>
#include <vector>

using std::string;
using std::vector;
using std::ostringstream;
using std::transform;
using std::istringstream;
using std::runtime_error;

namespace EOC2 {

class EOC2StringUtils {
 public:
  /** Default constructor. */
  EOC2StringUtils() {
    // Do nothing
  }

  /** Default destructor. */
  ~EOC2StringUtils() {
    // Do nothing
  }


  /** Convert any basic type into string.
   * @param rData The data to convert
   * @return A string
   */
  template < typename T > string toString(const T &rData) {
    ostringstream oss;
    oss << rData;
    return oss.str();
  }

  /** Convert a string into any basic type.
   * @param rString A string
   * @return Template T
   */
  template < typename T > T fromString(const string &rString);


  /** Remove leading and trailing spaces and/or tabs.
   * @param rString A string
   * @return A string
   */
  string trim(const string &rString) {
    string s = rString.substr(0, rString.find_last_not_of("\r\n") + 1);
    int first = s.find_first_not_of(" \t");
    if (first != string::npos)
      return s.substr(first, s.find_last_not_of(" \t") - first + 1);
    else
      return "";
  }


  /** Convert BackSlashes into slashes
   * @param rString A string
   * @return A string
   */
  string convertSlashes(const string &rString) {
    string out = rString;

    for (int i = 0; i < static_cast<int>(rString.size()); i++) {
      if (rString[i] == '\\') {
        out[i] = '/';
      }
    }
    return out;
  }


  /** Convert to uppercase.
   * @param rString A string
   * @return A string
   */
  string toUpper(const string &rString) {
    string out = rString;
    transform(out.begin(), out.end(), out.begin(), ::toupper);
    return out;
  }

  /** Convert to lowercase.
   * @param rString A string
   * @return A string
   */
  string toLower(const string &rString) {
    string out = rString;
    transform(out.begin(), out.end(), out.begin(), ::tolower);
    return out;
  }

  /** Extract the filename from a full path string
   * @param rFullPath A string
   * @return A string
   */
  string extractFileName(const string &rFullPath) {
    string filename = rFullPath;
    int pos = filename.find_last_of("/");
    if (pos != string::npos) filename = filename.substr(pos);
    return filename.substr(0, filename.find_last_of("."));
  }
};  // class EOC2StringUtils



// Definition of fromString
template < typename T >
T EOC2StringUtils::fromString(const string &rString) {
    istringstream iss(rString);
    T out;
    if (!(iss >> out)) {
        throw runtime_error(
            "Cannot convert " + rString + " into basic type");
    }
    return out;
}


// Specialization of function fromString() for boolean
template < >
inline bool EOC2StringUtils::fromString<bool>(const string &rString) {
    string s = trim(toLower(rString));
    if (s == "yes" || s == "true" || s == "on" || s == "y" || s == "1")
        return true;
    else if (s == "no" || s == "false" || s == "off" || s == "n" || s == "0")
        return false;
    else
        throw runtime_error("Cannot convert " + rString + " into boolean");
}


/** Circle handler.
    * Used by the Daugman's rubber sheet method
    * @see EOC2Processings::normalize()
    */
class EOC2Circle {
 public:
  /** Default constructor. */
  EOC2Circle();

  /** Default destructor. */
  ~EOC2Circle();


  /** Overloaded constructor.
      * @param rCenter Initialization of the center
      * @param rRadius Initialization of the radius
      */
  EOC2Circle(const CvPoint &rCenter, int rRadius);
  EOC2Circle(const CvPoint2D32f &fCenter, float fRadius);


  /** Compute circle fitting by least-squares method.
      * This function is called by EOC2Processings::segment() \n
      * Reference : 
      *   http://www.dtcenter.org/met/users/docs/write_ups/circle_fit.pdf
      * @param rPoints A contour in cartesian coordinates
      * @return void
      * @see segment()
      */
  // void computeCircleFitting(const vector<CvPoint> &rPoints);
  void computeCircleFittingFloat(const vector<CvPoint> &rPoints);


  /** Draw a circle on an image.
      * @param pImage The image on which circle is to be drawn
      * @param rColor Color of the circle
      * @param thickness Circle thickness. Set to -1 to draw the disk inside the
      *   circle
      * @return void
      */
  void drawCircle(IplImage *pImage, const CvScalar &rColor = cvScalar(255),
                  int thickness = 1);


  /** Get the circle center.
      * @return The circle center
      */
  CvPoint getCenter() const;
  CvPoint2D32f getCenterFloat() const;


  /** Get the circle radius.
      * @return The circle radius
      */
  int getRadius() const;
  float getRadiusFloat() const;


  /** Set the circle center.
      * @param rCenter The circle center
      * @return void
      */
  void setCenter(const CvPoint &rCenter);
  void setCenter(const CvPoint2D32f &fCenter);


  /** Set the circle radius.
      * @param rRadius The circle radius
      * @return void
      */
  void setRadius(int rRadius);
  void setRadius(float fRadius);


  /** Set the circle center and radius.
      * @param rCenter The circle center
      * @param rRadius The circle radius
      * @return void
      */
  void setCircle(const CvPoint &rCenter, int rRadius);
  void setCircle(const CvPoint2D32f &fCenter, float fRadius);


  /** Set the circle center and radius.
      * @param rCenterX The x-coordinate of circle center
      * @param rCenterY The y-coordinate of circle center
      * @param rRadius The circle radius
      * @return void
      */
  void setCircle(int rCenterX, int rCenterY, int rRadius);
  void setCircle(float rCenterX, float rCenterY, float rRadius);


 private:
  /** The circle center. */
  CvPoint mCenter;
  CvPoint2D32f fCenter;

  /** The circle radius. */
  int mRadius;
  float fRadius;
};  // class EOC2Circle
}  // namespace EOC2


#endif  // ACII_CLIENT_INCLUDE_EOC2UTILS_H_
