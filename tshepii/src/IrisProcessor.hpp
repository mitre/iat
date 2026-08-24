/*************************************************************************
* TSHEPII: Tool for Supporting Human Examination of Postmortem Iris Images
* Version : 1.0
* Date : April 2018
* Authors : Daniel Moreira, Adam Czajka, University of Notre Dame, USA
*************************************************************************/

#ifndef IRIS_PROCESSOR_H
#define IRIS_PROCESSOR_H

#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

/** Implements a processor of iris images. */
class IrisProcessor {
public:
	/** Constructor of iris processors.
	 *  @param [in] inputIris The iris to be processed.
	 *  @param [in] inputIrisMask A mask to be taken into account when processing the given iris.
	 *  @param [in] outputIris The result of processing the given iris.
	 *  @param [in] originalIris The original version of the input iris image, without any previous processing.
	 */
	IrisProcessor(Mat *inputIris, Mat *inputIrisMask, Mat *outputIris, Mat *originalIris) {
		this->inputIris = inputIris;
		this->inputIrisMask = inputIrisMask;
		this->outputIris = outputIris;
		this->originalIris = originalIris;
	}

	/** Destructor. */
	~IrisProcessor() {
		this->inputIris = NULL;
		this->inputIrisMask = NULL;
		this->outputIris = NULL;
		this->originalIris = NULL;
	}

	/** Returns a pointer to the input iris mask.
	 *  @return The input iris mask. */
	Mat* getInputIrisMask() {
		return this->inputIrisMask;
	}

	/** Updates the output iris image, after processing the input one.
	 *  Each subclass of IrisProcessor MUST implement this method.
	 *  @return void */
	virtual void updateOutput() {
		throw runtime_error("Don't forget to implement YourClass::updateOutput().");
	}

	/** Returns the image to be added to the report document.
	 *  @return The image to be added to the report document. */
	virtual Mat getReportImage() {
		throw runtime_error("Don't forget to implement YourClass::getReportImage().");
	}

protected:
	/** Input iris that must be processed. */
	Mat *inputIris;

	/** Mask of the input image. */
	Mat *inputIrisMask;

	/** Output image that will hold the processed input image. */
	Mat *outputIris;

	/** Original version of the input iris. */
	Mat *originalIris;
};

#endif