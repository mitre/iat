/*****************************************************************************
 * Eye Orientation CLassifier v2 (U/D + L/R)
 * Author: Adam Czajka, aczajka@nd.edu, May 2016
 *
 * Code in this file is partially based on Open Source for Iris (OSIRIS) v. 4.1
 * under BSD license http://opensource.org/licenses/bsd-license.php
 ******************************************************************************/


#include <vector>
#include <stdexcept>
#include "EOC2Utils.h"

using std::runtime_error;
using std::vector;

namespace EOC2 {
  // Default constructor
  EOC2Circle::EOC2Circle() {
    // Do nothing
  }

  // Copying constructor
  EOC2Circle::EOC2Circle(const CvPoint &rCenter, int rRadius) {
    setCenter(rCenter);
    setRadius(rRadius);
  }

  // Default constructor
  EOC2Circle::~EOC2Circle() {
    // Do nothing
  }

  CvPoint EOC2Circle::getCenter() const {
    return mCenter;
  }

  CvPoint2D32f EOC2Circle::getCenterFloat() const {
    return fCenter;
  }


  int EOC2Circle::getRadius() const {
    return mRadius;
  }

  float EOC2Circle::getRadiusFloat() const {
    return fRadius;
  }


  void EOC2Circle::setCenter(const CvPoint & rCenter) {
    mCenter = rCenter;
  }

  void EOC2Circle::setCenter(const CvPoint2D32f & rCenter) {
    fCenter = rCenter;
  }

  void EOC2Circle::setRadius(int rRadius) {
    if (rRadius < 0) {
      throw runtime_error("Circle with negative radius : " + rRadius);
    }
    mRadius = rRadius;
  }

  void EOC2Circle::setRadius(float rRadius) {
    if (rRadius < 0) {
      throw runtime_error("Circle with negative radius : " +
          static_cast<int>(rRadius));
    }
    fRadius = rRadius;
  }


  void EOC2Circle::setCircle(const CvPoint & rCenter, int rRadius) {
    setCenter(rCenter);
    setRadius(rRadius);
  }

  void EOC2Circle::setCircle(const CvPoint2D32f & rCenter, float rRadius) {
    setCenter(rCenter);
    setRadius(rRadius);
  }

  void EOC2Circle::setCircle(int rCenterX, int rCenterY, int rRadius) {
    setCircle(cvPoint(rCenterX, rCenterY), rRadius);
  }

  void EOC2Circle::setCircle(float rCenterX, float rCenterY, float rRadius) {
    setCircle(cvPoint2D32f(rCenterX, rCenterY), rRadius);
  }

  void EOC2Circle::drawCircle(IplImage *pImage, const CvScalar &rColor,
                              int thickness ) {
    cvCircle(pImage, mCenter, mRadius, rColor, thickness);
  }



  void EOC2Circle::computeCircleFittingFloat(const vector<CvPoint> &rPoints) {
    // Compute the averages mx and my
    float mx = 0, my = 0;

    for (int p = 0; p < static_cast<int>(rPoints.size()); p++) {
      mx += static_cast<float>(rPoints[p].x);
      my += static_cast<float>(rPoints[p].y);
    }

    mx = mx / static_cast<float>(rPoints.size());
    my = my / static_cast<float>(rPoints.size());

    // Work in (u,v) space, with u = x-mx and v = y-my
    float u = 0, v = 0, suu = 0, svv = 0, suv = 0,
          suuu = 0, svvv = 0, suuv = 0, suvv = 0;

    // Build some sums
    for (int p = 0; p < static_cast<int>(rPoints.size()); p++) {
      u = static_cast<float>(rPoints[p].x) - mx;
      v = static_cast<float>(rPoints[p].y) - my;
      suu += u * u;
      svv += v * v;
      suv += u * v;
      suuu += u * u * u;
      svvv += v * v * v;
      suuv += u * u * v;
      suvv += u * v * v;
    }

    // These equations are demonstrated in paper from R.Bullock (2006)
    float uc = static_cast<float>(0.5 *
        (suv * (svvv + suuv) - svv * (suuu + suvv)) / (suv * suv - suu * svv));
    float vc = static_cast<float>(0.5 *
        (suv * (suuu + suvv) - suu * (svvv + suuv)) / (suv * suv - suu * svv));

    // Circle parameters (float)
    setCenter(cvPoint2D32f(uc + mx, vc + my));
    setRadius(static_cast<float>(sqrt(uc * uc + vc * vc +
        (suu + svv) / static_cast<float>(rPoints.size()))));

    // Circle parameters (int)
    setCenter(cvPoint(static_cast<int>(uc + mx), static_cast<int>(vc + my)));
    setRadius(static_cast<int>(sqrt(uc * uc + vc * vc +
        (suu + svv) / static_cast<float>(rPoints.size()))));
  }
}  // namespace EOC2

