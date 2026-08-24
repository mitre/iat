/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable } from '@angular/core';
import { ToolManagerService } from '../iris-display/tool-manager.service';
import { Rectangle, Point } from 'paper';

class ImageArray {
  public data: Uint8ClampedArray;
  public width = 0;
  public height = 0;

  public getPixel(x, y) {
    const tempx = Math.floor(x);
    const tempy = Math.floor(y);
    return this.data[tempx + (tempy * this.width)];
  }
}

class ImageNumberArray {
  public data: Array<number>;
  public width = 0;
  public height = 0;

  public getPixel(x, y) {
    const tempx = Math.floor(x);
    const tempy = Math.floor(y);
    return this.data[tempx + (tempy * this.width)];
  }

}

@Injectable()
export class FindIrisService {

  maxsegs = 256;
  MaxR = 250;
  trigLength = this.maxsegs * this.MaxR * 2;
  trigVals = new Array<number>(this.trigLength);

  trigPos = (i, j, h) => (i * this.MaxR * 2) + (j * 2) + h;

  edgeArrayLength = 0;
  edgeValBin: ImageNumberArray = new ImageNumberArray();
  pupilEdgeBin: ImageNumberArray = new ImageNumberArray();

  vertEdge: ImageArray;

  IrisCenterX = 0;
  IrisCenterY = 0;
  IrisRadius = 0;

  pupilCenterX = 0;
  pupilCenterY = 0;
  pupilRadius = 0;
  pupilDiameter = 0;

  protected toolManager: ToolManagerService;

  constructor() {
  }

  initialize(toolManager: ToolManagerService) {
    this.toolManager = toolManager;
    this.initMathTables();
    const raster = this.toolManager._iris;
    const imageData: ImageData = this.toolManager._iris.getImageData(new Rectangle(0, 0, raster.width, raster.height));

    const ret = this.findIris(imageData);
  }

  findIris(imageData: ImageData): ImageData {
    const grayData = this.grayScaleImage(imageData);

    const dsfactor = 4;
    const dsImage = this.downScale(grayData, imageData.width, imageData.height, dsfactor);
    const edgeRawBin = this.edgeMap(dsImage.data, dsImage.width, dsImage.height, dsfactor);
    this.vertEdge = this.getVertEdges(dsImage.data, dsImage.width, dsImage.height);
    this.findIrisCenter(dsImage.width, dsImage.height, imageData, dsImage, this.vertEdge, this.edgeValBin);

    const tempDsImage = new ImageArray();
    tempDsImage.data = dsImage.data;
    tempDsImage.width = dsImage.width;
    tempDsImage.height = dsImage.height;
    this.findPupilCenter(this.edgeValBin, this.IrisCenterX, this.IrisCenterY, this.IrisRadius * 2, tempDsImage);

    const rgbData = this.grayToRgb(this.vertEdge.data);
    const data = new ImageData(rgbData, this.vertEdge.width, this.vertEdge.height);

    return data;
  }

  grayToRgb(imageData: Uint8ClampedArray): Uint8ClampedArray {
    const rgbaLength = 4;
    const ret = new Uint8ClampedArray(imageData.length * rgbaLength);

    for (let x = 0; x < imageData.length; x++) {
      const xret = x * rgbaLength;
      ret[xret] = imageData[x];
      ret[xret + 1] = imageData[x];
      ret[xret + 2] = imageData[x];
      ret[xret + 3] = 255;
    }

    return ret;
  }

  grayScaleImage(imageData: ImageData): Uint8ClampedArray {
    //the data for the image is in rgba order
    //and needs to be brought down to 8 bit grayscale
    const rgbaLength = 4;
    const ret = new Uint8ClampedArray(imageData.data.length / rgbaLength);

    for (let i = 0; i < imageData.data.length; i += 4) {
      const r = i / 4;
      ret[r] = (imageData.data[i] + imageData.data[i + 1] + imageData.data[i + 2]) / 3;
    }

    return ret;
  }

  //goes through original image and downscales the image by taking a box of
  //pixels and scaling it down to one point
  downScale(imageData: Uint8ClampedArray, imageWidth: number, imageHeight: number, downsizescale: number) {
    let origx = 0;
    let origy = 0;

    const averageboxsize = downsizescale * downsizescale;

    const dswidth = Math.floor(imageWidth / downsizescale);
    const dsheight = Math.floor(imageHeight / downsizescale);
    const ret = new Uint8ClampedArray(dswidth * dsheight);

    for (let indx = 0; indx < dswidth; indx++) {
      origx = indx * downsizescale;
      for (let indy = 0; indy < dsheight; indy++) {
        origy = indy * downsizescale;
        // for each pixel in the downsized image...
        // get the average intensity over the downsizescale*downsizescale
        // group of original pixels
        let dspoint = 0;
        for (let indxx = 0; indxx < downsizescale; indxx++) {
          for (let indyy = 0; indyy < downsizescale; indyy++) {
            let position = (indyy + origy) * imageWidth;
            position += indxx + origx;
            dspoint += imageData[position];
          }
        }

        ret[indx + (indy * dswidth)] = dspoint / averageboxsize;
      }
    }

    return {data: ret, width: dswidth, height: dsheight};
  }

  //      imageData - downscaled image
  //      imageWidth - downscaled image width
  //      imageHeight - downscaled image height
  //      downsizescale - scale used to downscale image
  edgeMap(imageData: Uint8ClampedArray, imageWidth: number, imageHeight: number, downsizescale: number) {

    this.edgeArrayLength = Math.floor(imageHeight * imageWidth);

    this.edgeValBin.data = new Array<number>(this.edgeArrayLength);
    this.edgeValBin.width = imageWidth;
    this.edgeValBin.height = imageHeight;

    this.pupilEdgeBin.data = new Array<number>(this.edgeArrayLength);
    this.pupilEdgeBin.width = imageWidth;
    this.pupilEdgeBin.height = imageHeight;

    const edgeRawBin = new Uint8ClampedArray(this.edgeArrayLength);

    for (let i = 0; i < this.edgeArrayLength; i++) {
      this.edgeValBin.data[i] = 0;
      this.pupilEdgeBin.data[i] = 0;
    }

    const imagePosition = (x, y) => x + (y * imageWidth);

    const isGreater = false;
    for (let indy = downsizescale; indy < imageHeight - 1 - downsizescale; indy++) {
      for (let kdx = 0; kdx < downsizescale; kdx++) {
        // assign 0 edge intensity values to a 4 pixel wide boundary on the
        // x axis note that this equates to a 16 pixel boundary on the
        // original image since it has been downsized by factor of 4
        this.edgeValBin.data[imagePosition(kdx, indy)] = 0;
        this.edgeValBin.data[imagePosition(imageWidth - downsizescale - 1 + kdx, indy)] = 0;

        this.pupilEdgeBin.data[imagePosition(kdx, indy)] = 0;
        this.pupilEdgeBin.data[imagePosition(imageWidth - downsizescale - 1 + kdx, indy)] = 0;

        edgeRawBin[imagePosition(kdx, indy)] = 0;
        edgeRawBin[imagePosition(imageWidth - downsizescale - 1 + kdx, indy)] = 0;
      }//end of for

      for (let indx = downsizescale; indx < imageWidth - downsizescale; indx++) {
        this.edgeValBin.data[imagePosition(indx, indy)] = 0;
        this.pupilEdgeBin.data[imagePosition(indx, indx)] = 0;

        for (let jdx = 0; jdx < downsizescale; jdx++) {
          // Takes 4 pixels to either side of the trial pixel
          // Note that when this is done on a quarter downsized image - it is
          // a 16 pixel wide average difference

          let imgpt1 = imageData[imagePosition(indx - jdx - 1, indy)];
          let imgpt2 = imageData[imagePosition(indx + jdx, indy)];

          imgpt1 = imgpt1 > 200 ? 200 : imgpt1;
          imgpt2 = imgpt2 > 200 ? 200 : imgpt2;

          this.edgeValBin.data[imagePosition(indx, indy)] += (imgpt1 - imgpt2);

        }//end of for loop

        if (this.edgeValBin.data[imagePosition(indx, indy)] < 0) {
          this.edgeValBin.data[imagePosition(indx, indy)] *= -1;
        }

        if (this.edgeValBin.data[imagePosition(indx, indy)] > 140) {
          this.edgeValBin.data[imagePosition(indx, indy)] = 140;
        }

        // Edge differences greater than 140 are likely due to
        // specularities, not iris edges
        this.edgeValBin.data[imagePosition(indx, indy)] /= 3;
        edgeRawBin[imagePosition(indx, indy)] = this.edgeValBin.data[imagePosition(indx, indy)];

      }//end of for

    }//end of for

    return {
      data: edgeRawBin,
      width: imageWidth,
      height: imageHeight
    };

  }//end of edgemap


  getVertEdges(dsImage: Uint8ClampedArray, imageWidth: number, imageHeight: number) {

    // Go horizontally across the downsized image and get average intensity
    // differences along an 8 pixel vertical edge
    // centered on the current horizontal point on each line

    const boxSize = 4;
    const vertEdge = new Uint8ClampedArray(dsImage.length);

    const imagePosition = (x, y) => x + (y * imageWidth);
    for (let idx8 = 0; idx8 < imageWidth; idx8++) {
      // Assign zero vertical edge values in the first 4 vertical lines
      for (let kdy = 0; kdy < boxSize; kdy++) {
        // assign 0 edge intensity values to a 4 pixel wide boundary on the
        // x axis note that this equates to a 16 pixel boundary on the
        // original image since it has been downsized by factor of 4

        vertEdge[imagePosition(idx8, kdy)] = 0;
        vertEdge[imagePosition(idx8, imageHeight - kdy - 1)] = 0;
      }//end of for

      for (let jdy8 = boxSize; jdy8 < imageHeight - (2 * boxSize); jdy8++) {
        let pointVal = dsImage[imagePosition(idx8, jdy8 - 2)] + dsImage[imagePosition(idx8, jdy8 - 1)];
        pointVal = pointVal - dsImage[imagePosition(idx8, jdy8 + 1)] - dsImage[imagePosition(idx8, jdy8)];

        pointVal = pointVal < 0 ? pointVal * -1 : pointVal;
        pointVal = pointVal > 255 ? 255 : pointVal;

        vertEdge[imagePosition(idx8, jdy8)] = pointVal;
        pointVal = pointVal & 0x000000ff;
        pointVal = pointVal > 20 ? 20 : pointVal;
        vertEdge[imagePosition(idx8, jdy8)] = pointVal;
      }//end of for
    }

    const ret = new ImageArray();
    ret.data = vertEdge;
    ret.width = imageWidth;
    ret.height = imageHeight;
    return ret;
  }//end of getVertEdges

  initMathTables() {
    let anglesegnum = 0.0;
    let radiuslength = 0.0;
    const halfmaxsegs = this.maxsegs / 2;
    const seg90 = this.maxsegs / 4;
    const seg180 = this.maxsegs / 2;
    let dsin = 0;
    let dcos = 0;
    const singlesegment = 6.28318530718 / this.maxsegs; //magic number?


    for (let i = 0; i < this.trigLength; i++) {
      this.trigVals[i] = 0;
    }


    for (anglesegnum = 0; anglesegnum < seg90 + 1; anglesegnum++) {
      // calculate sine and cosines
      dsin = Math.sin(singlesegment * anglesegnum);
      dcos = Math.cos(singlesegment * anglesegnum);
      for (radiuslength = 0; radiuslength < this.MaxR + 1; radiuslength++) {
        // get x axis orinals in image array for a given angle and distance
        // from center
        if (dcos >= 0) {
          this.trigVals[this.trigPos(anglesegnum, radiuslength, 0)] = Math.floor(dcos * radiuslength + 0.5);
        } else {
          this.trigVals[this.trigPos(anglesegnum, radiuslength, 0)] = Math.ceil(dcos * radiuslength - 0.5);
        }
        this.trigVals[this.trigPos(halfmaxsegs - anglesegnum, radiuslength, 0)] = -1 * this.trigVals[this.trigPos(anglesegnum, radiuslength, 0)] - 1;
        this.trigVals[this.trigPos(halfmaxsegs + anglesegnum, radiuslength, 0)] = -1 * this.trigVals[this.trigPos(anglesegnum, radiuslength, 0)] - 1;
        this.trigVals[this.trigPos(this.maxsegs - anglesegnum, radiuslength, 0)] = this.trigVals[this.trigPos(anglesegnum, radiuslength, 0)];

        // now get y axis ordinal in image array for a given angle and
        // distance from center
        if (dsin >= 0) {
          this.trigVals[this.trigPos(anglesegnum, radiuslength, 1)] = Math.floor(dsin * radiuslength + 0.5);
        } else {
          this.trigVals[this.trigPos(anglesegnum, radiuslength, 1)] = Math.ceil(dsin * radiuslength - 0.5);
        }
        this.trigVals[this.trigPos(halfmaxsegs - anglesegnum, radiuslength, 1)] = this.trigVals[this.trigPos(anglesegnum, radiuslength, 1)];
        this.trigVals[this.trigPos(halfmaxsegs + anglesegnum, radiuslength, 1)] = -1 * this.trigVals[this.trigPos(anglesegnum, radiuslength, 1)] - 1;
        this.trigVals[this.trigPos(this.maxsegs - anglesegnum, radiuslength, 1)] = -1 * this.trigVals[this.trigPos(anglesegnum, radiuslength, 1)];
      } // radius
    }
    // special cases just to keep from referencing out-of-bounds values at this.MaxR
    // since defined origin at lower left corner of (0,0)
    this.trigVals[this.trigPos(0, this.MaxR, 0)] = this.MaxR - 1;
    this.trigVals[this.trigPos(seg90, this.MaxR, 1)] = this.MaxR - 1;
    this.trigVals[this.trigPos(halfmaxsegs, this.MaxR, 0)] = -this.MaxR;
    this.trigVals[this.trigPos(this.maxsegs - seg90, this.MaxR, 1)] = -this.MaxR;
    this.trigVals[this.trigPos(this.maxsegs, this.MaxR, 0)] = this.MaxR - 1;
  }

  findIrisCenter(imageWidth: number, imageHeight: number, originalImage: ImageData, dsImage , vertEdge: any, edgeValBin: any) {

    // Version 2.2.1 change initialize darkiriscenter to zero
    let darkiriscenter = 0;
    let bestdarkiriscenter = 0;

    const iriscenterpt = new Array<InstanceType<typeof Point>>(224);
    for (let i = 0; i < 224; i++) {
      iriscenterpt[i] = new Point(0, 0);
    }

    // assume that Edgevalbin from above is used
    // remember that image coordinants are from top left to bottom right
    // MinRtrial = 35;
    // Reminder that since this is on a half-sized image...
    // correlates with 150 iris diameter min MaxRtrial = 100;  // Max trial
    // radius for 320x240 image... Max trial diameter is 400 on full-sized
    // 640x480 image LowerSegTrial = 24;   is the number of 360/256 degree
    // segments below the horizontal axis of the iris UpperSegTrial = 12;   is
    // the number of segments above the horizontal axis of the iris

    // Version 2.2.1 Change ... LowerSegTrial and UpperSegTrial are globals...
    //                          Reset these values due to conflict with Fine
    //                          Iris finding routine.
    const LowerSegTrial = 47;
    const UpperSegTrial = 40;

    let MaxSegTrial = UpperSegTrial;
    if (LowerSegTrial > MaxSegTrial) {
      MaxSegTrial = LowerSegTrial;
    }

    let bestresponse = 0;
    let RoughIrisRadius = 0;
    let RoughIriscx = 0;
    let RoughIriscy = 0;
    // set up the array of circular points close to the center to check for dark
    // areas. set up the upper right quadrant and copy to the others
    let ctr = 0;
    let rdxx = 0;
    let jdxx = 0;
    for (rdxx = 2; rdxx < 17;
         rdxx +=
           2) {
      // makes points up to 16 pixel (downsized) radius (8 samples)
      // equivalent pupil diameter is 16*2 * 4 = 128
      for (jdxx = 0; jdxx < 61;
           jdxx +=
             10) {
        // takes 10 angle segment increments up to 60 ( 7 samples )
        // creates 56 points in upper right trig quadrant (in relative
        // coordinates, not image coords)
        iriscenterpt[ctr].x = this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        iriscenterpt[ctr].y = this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        // creates additional 56 points in lower right, upper left and lower
        // left quadrants
        iriscenterpt[ctr + 56].x = this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        iriscenterpt[ctr + 56].y = -1 * this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        iriscenterpt[ctr + 112].x = -1 * this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        iriscenterpt[ctr + 112].y = this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        iriscenterpt[ctr + 168].x = -1 * this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        iriscenterpt[ctr + 168].y = -1 * this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        ctr++;
      }
    }
    let maxIrisFindRightEdgeResponse = 0;
    let maxIrisFindLeftEdgeResponse = 0;
    let maxVertEdgeResponse = 0;

    let rdx = 0;
    let idx = 0;
    let idy = 0;
    let jdx = 0;
    let MaxRelX = 0;
    let MinRelY = 0;
    let MaxRelY = 0;
    let rightedgepointsused = 0;
    let leftedgepointsused = 0;
    let trialresponse = 0;
    let leftedgeresponse = 0;
    let rightedgeresponse = 0;
    let vertedgeresponse = 0;
    let ptdarkval = 0;
    let trialx = 0;
    let trialy = 0;
    let ictr5 = 0;
    let srx = 0;
    let suy = 0;
    let slx = 0;
    let sly = 0;
    const MaxRtrial = 42;
    const MinRtrial = 28;

    const getImagePosition = (imgData, x, y) => x + (y * imgData.width);
    const counter = 0;

    for (rdx = MinRtrial; rdx < MaxRtrial; rdx++, rdx++) {
      // calculate width and height of the test area for a given rdx,jdx
      MaxRelX = rdx; // test cannot include X coordinants within the trial radius
      MinRelY = this.trigVals[this.trigPos(LowerSegTrial, rdx, 1)];
      MaxRelY = this.trigVals[this.trigPos(UpperSegTrial, rdx, 1)];
      // remember that these are opposite image coords
      for (idx = imageWidth * 3 / 10; idx < imageWidth * 7 / 10; idx++, idx++) {
        for (idy = imageHeight * 3 / 10; idy < imageHeight * 7 / 10; idy++, idy++) {
          rightedgepointsused = 0;
          leftedgepointsused = 0;
          trialresponse = 0;
          leftedgeresponse = 0;
          rightedgeresponse = 0;
          vertedgeresponse = 0;
          darkiriscenter = 0;
          ptdarkval = 0;
          trialx = 0;
          trialy = 0;

          for (ictr5 = 0; ictr5 < 224; ictr5++) {

            // calculate "Dark Center response" for the current idx,idy
            // note that the grid radius is always 5,10,15,20,25
            trialx = idx + iriscenterpt[ictr5].x;
            trialy = idy - iriscenterpt[ictr5].y;

            if (trialx > 0
              && trialx < imageWidth
              && trialy < imageHeight
              && trialy > 0) {

              ptdarkval = dsImage.data[getImagePosition(dsImage, Math.floor(trialx), Math.floor(trialy))];
              ptdarkval = (55 - ptdarkval);
              // in low contrast images to dark gray iris
              if (ptdarkval < 0) {
                ptdarkval = 0;
              }
              darkiriscenter += ptdarkval;
            }
          }//end of for

          for (jdx = 0; jdx < MaxSegTrial; jdx++) {
            // right extent
            srx = idx + this.trigVals[this.trigPos(jdx, rdx, 0)];
            // upper extent - remember lower y is upper
            suy = idy - this.trigVals[this.trigPos(jdx, rdx, 1)];
            // left extent
            slx = idx - this.trigVals[this.trigPos(jdx, rdx, 0)];
            // lower extent
            sly = idy + this.trigVals[this.trigPos(jdx, rdx, 1)];

            if ((srx + 4 < imageWidth - 1) && (srx - 2 >= 0)) {

              if ((suy >= 4) && (jdx < UpperSegTrial)) {

                rightedgeresponse += this.edgeValBin.getPixel(srx, suy);
                vertedgeresponse += this.vertEdge.getPixel(srx, suy);
                rightedgeresponse += this.edgeValBin.getPixel(srx - 1, suy);
                vertedgeresponse += this.vertEdge.getPixel(srx - 1, suy);
                rightedgeresponse += this.edgeValBin.getPixel(srx + 1, suy);
                vertedgeresponse += this.vertEdge.getPixel(srx + 1, suy);
                rightedgepointsused++;
              }

              if ((sly < imageHeight - 3) && (jdx < LowerSegTrial)) {

                rightedgeresponse += this.edgeValBin.getPixel(srx, sly);
                vertedgeresponse += this.vertEdge.getPixel(srx, sly);
                rightedgeresponse += this.edgeValBin.getPixel(srx - 1, sly);
                vertedgeresponse += this.vertEdge.getPixel(srx - 1, sly);
                rightedgeresponse += this.edgeValBin.getPixel(srx + 1, sly);
                vertedgeresponse += this.vertEdge.getPixel(srx + 1, sly);
                rightedgepointsused++;
              }
            }

            // Left Edge response
            if ((slx - 2 >= 0) && (srx + 4 < imageWidth - 1)) {
              if ((suy >= 4) && (jdx < UpperSegTrial)) {
                leftedgeresponse += this.edgeValBin.getPixel(slx, suy);
                vertedgeresponse += this.vertEdge.getPixel(slx, suy);
                leftedgeresponse += this.edgeValBin.getPixel(slx - 1, suy);
                vertedgeresponse += this.vertEdge.getPixel(slx - 1, suy);
                leftedgeresponse += this.edgeValBin.getPixel(slx + 1, suy);
                vertedgeresponse += this.vertEdge.getPixel(slx + 1, suy);
                leftedgepointsused++;
              }
              if ((sly < imageHeight - 1) &&
                (jdx < LowerSegTrial)) {
                // lower left edge response
                leftedgeresponse += this.edgeValBin.getPixel(slx, sly);
                vertedgeresponse += this.vertEdge.getPixel(slx, sly);
                leftedgeresponse += this.edgeValBin.getPixel(slx - 1, sly);
                vertedgeresponse += this.vertEdge.getPixel(slx - 1, sly);
                leftedgeresponse += this.edgeValBin.getPixel(slx + 1, sly);
                vertedgeresponse += this.vertEdge.getPixel(slx + 1, sly);
                leftedgepointsused++;
              }
            }
          } // end of loop for angle segments to be added up
          // weighting factor for darkiriscenter
          darkiriscenter = darkiriscenter * 105 / 10; // 100 used in test - works with "dark threshold" of 55
          vertedgeresponse = vertedgeresponse * 3 / 10;
          trialresponse = leftedgeresponse * 15 / 10 +
            rightedgeresponse * 15 / 10 +
            vertedgeresponse +
            darkiriscenter;
          if (trialresponse > bestresponse) {
            bestresponse = trialresponse;
            RoughIriscx = idx * 4;
            RoughIriscy = idy * 4;
            RoughIrisRadius = rdx * 4;
            this.IrisCenterX = idx * 4;
            this.IrisCenterY = idy * 4;
            this.IrisRadius = rdx * 4;
            maxIrisFindRightEdgeResponse = rightedgeresponse;
            maxIrisFindLeftEdgeResponse = leftedgeresponse;
            maxVertEdgeResponse = vertedgeresponse;
            bestdarkiriscenter = darkiriscenter;
          }
        } // end of  y loop
      }     // end of x loop
    }         // end of R loop
    // check for strong vertical edges along the iris-sclera boundary as an
    // indicator of occlusions
    this.findFineIris(originalImage, imageWidth * 4, imageHeight * 4, this.IrisCenterX, this.IrisCenterY, this.IrisRadius * 2);
  }

  findFineIris(originalImage: ImageData, imageWidth: number, imageHeight: number, roughIxc: number, roughIyc: number, roughIdiam) {

    // set the bounds of the search area based upon rough locations
    const MinRfine = roughIdiam / 2 - 4;
    let MaxRfine = roughIdiam / 2 + 4;
    if (MaxRfine > this.MaxR) {
      MaxRfine = this.MaxR;
    }
    const MinIxcfine = roughIxc - 4;
    const MaxIxcfine = roughIxc + 4;
    const MinIycfine = roughIyc - 4;
    const MaxIycfine = roughIyc + 4;
    const bestresponse = 0;
    const leftresponse = 0;
    const rightresponse = 0;
    let wf = 1;
    const TanPts = new Array<InstanceType<typeof Point>>(33);
    for (let i = 0; i < TanPts.length; i++) {
      TanPts[i] = new Point(0, 0);
    }

    let rightedgepointsused = 0;
    let leftedgepointsused = 0;
    let trialresponse = 0;
    let leftedgeresponse = 0;
    let rightedgeresponse = 0;
    const maxpointresponse = 2000;
    let srx = 0;
    let suy = 0;
    let slx = 0;
    let sly = 0;
    let idx = 0;
    let jdx = 0;
    let idy = 0;
    let rctr = 0;
    let jjdx = 0;

    // 47 works  256 / 4 = 64
    const LowerSegTrial = 40;
    // 35 works higher fails on droopy eyelids
    // cause failure lower fails on large pupils
    const UpperSegTrial = 30;

    //magic number pulled from original that is supposed to be the original value of LowerSegTrial
    const uppersegpointresponses = new Array<number>(47);
    const maxuppersegresponses = new Array<number>(47);
    for (let i = 0; i < 47; i++) {
      uppersegpointresponses[i] = 0;
      maxuppersegresponses[i] = 0;
    }

    const getImagePosition = (imgData, x, y) => x + (y * imgData.width);
    for (let rdx = MinRfine; rdx < MaxRfine; rdx++) {
      for (idx = MinIxcfine; idx < MaxIxcfine; idx++) {
        for (idy = MinIycfine; idy < MaxIycfine; idy++) {
          rightedgepointsused = 0;
          leftedgepointsused = 0;
          trialresponse = 0;
          leftedgeresponse = 0;
          rightedgeresponse = 0;

          for (jdx = 0; jdx < LowerSegTrial; jdx += 2) {

            uppersegpointresponses[jdx] = 0;
            // fix for the fact that the radial will extend an
            // additional 12-16 pixels! Right extent
            srx = idx + this.trigVals[this.trigPos(jdx, rdx + 16, 0)];
            // Upper extent - remember lower y is upper
            suy = idy - this.trigVals[this.trigPos(jdx, rdx + 16, 1)];
            // Left extent
            slx = idx - this.trigVals[this.trigPos(jdx, rdx + 16, 0)];
            // Lower extent
            sly = idy + this.trigVals[this.trigPos(jdx, rdx + 16, 1)];
            // weighting factor to give more weight to pixels
            // closest to the exact edge
            wf = 1;

            // Check to see if
            // this is correct on
            // the full size image
            if ((srx < imageWidth) && (slx >= 0)) {
              if ((suy >= 0) && (jdx < UpperSegTrial)) {
                let pointresponse = 0;
                for (rctr = 0; rctr < 16; rctr++, rctr++) {
                  wf = 4 - (rctr / 4);
                  const tempx = idx + this.trigVals[this.trigPos(jdx, rdx + rctr, 0)];
                  const tempy = idy - this.trigVals[this.trigPos(jdx, rdx + rctr, 1)];

                  const tempx2 = idx + this.trigVals[this.trigPos(jdx, rdx - rctr, 0)];
                  const tempy2 = idy - this.trigVals[this.trigPos(jdx, rdx - rctr, 1)];

                  const tempMul = originalImage.data[getImagePosition(originalImage, tempx, tempy)];
                  const tempMul2 = originalImage.data[getImagePosition(originalImage, tempx2, tempx2)];
                  pointresponse += wf * (tempMul - tempMul2);
                }

                if (pointresponse < 0) {
                  pointresponse = -pointresponse;
                }

                if (pointresponse > maxpointresponse) {
                  pointresponse = maxpointresponse;
                }

                uppersegpointresponses[jdx] = pointresponse;
                rightedgeresponse += pointresponse;
                rightedgepointsused++;
              }

              if ((sly < imageWidth) && (jdx < LowerSegTrial)) {
                let pointresponse = 0;
                for (rctr = 0; rctr < 16; rctr++) {
                  wf = 4 - (rctr / 4);
                  const tempx1 = idx + this.trigVals[this.trigPos(jdx, rdx + rctr, 0)];
                  const tempy1 = idy + this.trigVals[this.trigPos(jdx, rdx + rctr, 1)];

                  const tempx2 = idx + this.trigVals[this.trigPos(jdx, rdx - rctr, 0)];
                  const tempy2 = idy + this.trigVals[this.trigPos(jdx, rdx - rctr, 1)];

                  const tempMul = originalImage.data[getImagePosition(originalImage, tempx1, tempy1)];
                  const tempMul2 = originalImage.data[getImagePosition(originalImage, tempx2, tempx2)];

                  pointresponse += wf * (tempMul - tempMul2);
                  // watch out for X values that go beyond the
                  // image
                }

                if (pointresponse < 0) {
                  pointresponse = -pointresponse;
                }

                if (pointresponse > maxpointresponse) {
                  pointresponse = maxpointresponse;
                }

                rightedgeresponse += pointresponse;
                rightedgepointsused++;
              }
            }

            if ((slx >= 0) && (srx <= imageWidth)) {
              if ((suy >= 0) && (jdx < UpperSegTrial)) {
                let pointresponse = 0;
                for (rctr = 0; rctr < 16; rctr++) {
                  wf = 4 - (rctr / 4);
                  const tempx = idx - this.trigVals[this.trigPos(jdx, rdx + rctr, 0)];
                  const tempy = idy - this.trigVals[this.trigPos(jdx, rdx + rctr, 1)];

                  const tempx2 = idx - this.trigVals[this.trigPos(jdx, rdx - rctr, 0)];
                  const tempy2 = idy - this.trigVals[this.trigPos(jdx, rdx - rctr, 1)];

                  const tempMul = originalImage.data[getImagePosition(originalImage, tempx, tempy)];
                  const tempMul2 = originalImage.data[getImagePosition(originalImage, tempx2, tempx2)];

                  pointresponse += wf * (tempMul - tempMul2);
                }

                if (pointresponse < 0) {
                  pointresponse = -pointresponse;
                }

                if (pointresponse > maxpointresponse) {
                  pointresponse = maxpointresponse;
                }

                uppersegpointresponses[jdx] += pointresponse;
                leftedgeresponse += pointresponse;
                leftedgepointsused++;
              }

              if ((sly < imageHeight - 1) && (jdx < LowerSegTrial)) {
                // fill in logic to get the lower left response
                let pointresponse = 0;
                for (rctr = 0; rctr < 16; rctr++) {
                  wf = 4 - (rctr / 4);
                  const tempx1 = idx - this.trigVals[this.trigPos(jdx, rdx + rctr, 0)];
                  const tempy1 = idy + this.trigVals[this.trigPos(jdx, rdx + rctr, 1)];

                  const tempx2 = idx - this.trigVals[this.trigPos(jdx, rdx - rctr, 0)];
                  const tempy2 = idy + this.trigVals[this.trigPos(jdx, rdx - rctr, 1)];

                  const tempMul = originalImage.data[getImagePosition(originalImage, tempx1, tempy1)];
                  const tempMul2 = originalImage.data[getImagePosition(originalImage, tempx2, tempx2)];
                  pointresponse += wf * (tempMul - tempMul2);

                }

                if (pointresponse < 0) {
                  pointresponse = -pointresponse;
                }

                if (pointresponse > maxpointresponse) {
                  pointresponse = maxpointresponse;
                }

                leftedgeresponse += pointresponse;
                leftedgepointsused++;
              }//end of if
            }
          } // end of loop for angle segments to be added up
          trialresponse = leftedgeresponse + rightedgeresponse;

          if (trialresponse > bestresponse) {

            this.IrisCenterX = idx;
            this.IrisCenterY = idy;
            this.IrisRadius = rdx;

            // now save the upperedgepointresponses for later use
            for (jjdx = 0; jjdx < UpperSegTrial; jjdx++) {
              maxuppersegresponses[jjdx] = uppersegpointresponses[jjdx];
            }
          }
        } // end of  y loop
      }     // end of x loop
    }         // end of R loop
    // Use the best diameter to calculate diagnostic metrics:
    // Get Iris-Sclera GrayScale Difference values over a -60 to 45 deg arc
    // along the diameter Use response value trends along the arc on left and
    // right sides to determine likely eyelid locations Fill in  ISGSVals[256];
    // // 0 is 0 degrees  // 64 is 90 deg.  //128 is 180 deg //192 is 270 deg.
    const IXC = this.IrisCenterX;
    const IYC = this.IrisCenterY;
    const IRAD = this.IrisRadius;

    const ISGSVals = new Array<number>(256);
    for (let i = 0; i < ISGSVals.length; i++) {
      ISGSVals[i] = 0;
    }

    if (this.IrisRadius > this.MaxR - 16) {
      console.error('Iris Radius exceeds max allowable');
      return;
    }

    if (this.IrisRadius < 17) {
      console.error('Invalid Iris Diameter value', this.IrisRadius);
      return;
    }

    let rctr2 = 0;
    let outerpointval = 0;
    let innerpointval = 0;
    for (let jdxx = 0; jdxx < this.maxsegs; jdxx++) {
      // in this array TanPts[33] is the centered on the iris edge arc
      for (rctr = 0; rctr < 33; rctr++) {
        TanPts[rctr].x = IXC + this.trigVals[this.trigPos(jdxx, IRAD + rctr - 16, 0)];
        TanPts[rctr].y = IYC - this.trigVals[this.trigPos(jdxx, IRAD + rctr - 16, 1)];
      }
      let pointresponse = 0;
      let numradpts = 0;
      for (rctr2 = 1; rctr2 < 16; rctr2++) {
        if (TanPts[16 + rctr2].x > 0
          && TanPts[16 + rctr2].x < imageWidth
          && TanPts[16 + rctr2].y > 0
          && TanPts[16 + rctr2].y < imageHeight) {
          wf = 1;
          const tempx = TanPts[16 + rctr2].x;
          const tempy = TanPts[16 + rctr2].y;
          const tempx2 = TanPts[16 - rctr2].x;
          const tempy2 = TanPts[16 - rctr2].y;

          // weighting factor for distance from iris edge point going 16
          // pixels each direction wf = 4 - (rctr2 / 4);
          outerpointval = originalImage.data[getImagePosition(originalImage, tempx, tempy)];
          innerpointval = originalImage.data[getImagePosition(originalImage, tempx2, tempy2)];
          // Version 2.2.1 Change to limit effects of single differences)
          let responseint = outerpointval - innerpointval;
          if (responseint > 75) {
            responseint = 75;
          }
          pointresponse = pointresponse + responseint;
          numradpts++;
        } else {
          pointresponse = 0;
        }
      }
      ISGSVals[jdxx] = pointresponse;
    } // end of jdxx - intensity difference for each radial segment from 0 to
      // maxsegs at IXC, IYC, IRAD
    // Calculate adjusted Iris-Sclera GrayScale Difference using averages of
    // ISGSVals[128:138] and ISGSVals[246:0];
    let isgstotal = 0;

    // ISGSDiffMeanAvg = isgstotal / 300;   // remember that each point in
    // ISGSVals is 15 pixels on each side
    let ptsused = 0;
    for (let jctr2 = 0; jctr2 < 40; jctr2++) {
      // Each point in ISGSVals is 15 pixels on each side
      isgstotal = isgstotal + ISGSVals[255 - jctr2];
      ptsused++;
      isgstotal = isgstotal + ISGSVals[128 + jctr2];
      ptsused++;
    }

    for (let jctr3 = 0; jctr3 < 20; jctr3++) {
      // Each point in ISGSVals is 15 pixels on each side
      // Add response on 20 upper segments
      isgstotal = isgstotal + ISGSVals[jctr3];
      ptsused++;
      isgstotal = isgstotal + ISGSVals[127 - jctr3];
      ptsused++;
    }

    const ISGSDiffMeanAvg = isgstotal / (ptsused * 15); // Remember that each point in ISGSVals is 15 pixels on each side
  }


  findPupilCenter(edgeData: ImageNumberArray, irisCenterX: number, irisCenterY: number, irisDiameter: number, dsImage: ImageArray) {
    // Use same routine as Find Iris but restrict search to +/-0.1Idiam
    // inside iris region and limit radius range to 0.8 irisd. This means
    // that pupil center is restricted to within 40% of Iris diameter from
    // Iris center. Also restrict trial iris radius so that it does not get
    // within 8 pixels from iris edge
    // the division by 4 is because the edgeData and the dsImage have been
    // downscaled by 4
    const irisreduced = (irisDiameter / 4) / 10;
    let roitop = (irisCenterY - irisreduced) / 4;
    let roileft = (irisCenterX - irisreduced) / 4;
    let roibottom = (irisCenterY + irisreduced) / 4;
    let roiright = (irisCenterX + irisreduced) / 4;
    let PupIcdist = 0;
    let ipgsresponse = 0;
    let MaxPupilEdgeResponse = 0;
    let edgeresponse = 0;
    const pwf = 0;

    const counter = 0;

    const pupilcenterpt = new Array<InstanceType<typeof Point>>(224);
    for (let i = 0; i < pupilcenterpt.length; i++) {
      pupilcenterpt[i] = new Point(0, 0);
    }

    let pctr = 0;
    let rdxx = 0;
    let jdxx = 0;

    // makes points up to 45 radius = 100 pixel diameter (8 samples)
    for (rdxx = 3; rdxx < 25; rdxx += 3) {
      // this is in downsized (by factor of 4) dimensions
      // takes 10 angle segment increments up to 60 ( 7 samples )
      for (jdxx = 0; jdxx < 61; jdxx += 10) {
        // creates 56 points in upper right trig quadrant (in relative
        // coordinates, not image coords)
        pupilcenterpt[pctr].x = this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        pupilcenterpt[pctr].y = this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        // creates additional 56 points in lower right, upper left and
        // lower left quadrants
        pupilcenterpt[pctr + 56].x = this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        pupilcenterpt[pctr + 56].y = -1.0 * this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        pupilcenterpt[pctr + 112].x = -1.0 * this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        pupilcenterpt[pctr + 112].y = this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        pupilcenterpt[pctr + 168].x = -1.0 * this.trigVals[this.trigPos(jdxx, rdxx, 0)];
        pupilcenterpt[pctr + 168].y = -1.0 * this.trigVals[this.trigPos(jdxx, rdxx, 1)];
        pctr++;
      }
    }
    let ipgspointsused = 0;

    const PUpperSegTrial = 60;
    const PLowerSegTrial = 63;
    // make sure ROI is inside the image
    if (roitop < 0) {
      roitop = 0;
    }

    if (roileft < 0) {
      roileft = 0;
    }

    if (roiright > edgeData.width - 1) {
      roiright = edgeData.width - 1;
    }

    if (roibottom > edgeData.height - 1) {
      roibottom = edgeData.height - 1;
    }

    let PMaxSegTrial = PUpperSegTrial;
    if (PLowerSegTrial > PMaxSegTrial) {
      PMaxSegTrial = PLowerSegTrial;
    }

    let bestdarkpupilresponse = 0;
    let darkpupilval = 0;
    let bestresponse = 0;

    MaxPupilEdgeResponse = 0;
    edgeresponse = 0;
    let pupilCx = 0;
    let pupilCy = 0;
    let pupilD = 0;
    let pupilRad = 0;
    // for each trial radius rdx  and for angle range jdx... find the best
    // total response for the image

    const MaxPrtrial = (this.IrisRadius / 4) * 0.8; // Use 80% of IrisRadius
    let rightedgepointsused = 0;
    let leftedgepointsused = 0;
    let rdx = 0;
    let idx = 0;
    let idy = 0;
    let jdx = 0;
    let ptdarkval = 0;
    let trialx = 0;
    let trialy = 0;
    let pctr5 = 0;
    let trialresponse = 0;
    let leftedgeresponse = 0;
    let rightedgeresponse = 0;
    let srx = 0;
    let suy = 0;
    let slx = 0;
    let sly = 0;
    const MinPrtrial = 8;
    for (rdx = MinPrtrial; rdx < MaxPrtrial; rdx++) {
      // calculate width and height of the test area for a given rdx,jdx
      for (idx = roileft; idx < roiright; idx++) {
        for (idy = roitop; idy < roibottom; idy++) {
          darkpupilval = 0;
          ptdarkval = 0;
          trialx = 0;
          trialy = 0;
          for (pctr5 = 0; pctr5 < 224; pctr5++) {
            // calculate "Dark Center response" for the current idx,
            // idy note that the grid radius is always 5,10,15,20,25
            trialx = idx + pupilcenterpt[pctr5].x;
            trialy = idy - pupilcenterpt[pctr5].y;
            if (trialx > 0 && trialx < edgeData.width &&
              trialy < edgeData.height && trialy > 0) {
              ptdarkval = dsImage.getPixel(trialx, trialy);
              ptdarkval = 130 - ptdarkval;
              if (ptdarkval < 0) {
                ptdarkval = 0;
              }
              ptdarkval /= 5;
              darkpupilval = darkpupilval + ptdarkval;
            }
          }
          
          ipgsresponse = 0;
          ipgspointsused = 0;
          rightedgepointsused = 0;
          leftedgepointsused = 0;
          trialresponse = 0;
          leftedgeresponse = 0;
          rightedgeresponse = 0;

          for (jdx = 0; jdx < PMaxSegTrial; jdx++, jdx++) {
            srx = idx + this.trigVals[this.trigPos(jdx, rdx, 0)]; // Right extent
            suy = idy - this.trigVals[this.trigPos(jdx, rdx, 1)]; // Upper extent -
            // remember lower y is upper
            slx = idx - this.trigVals[this.trigPos(jdx, rdx, 0)]; // Left extent
            sly = idy + this.trigVals[this.trigPos(jdx, rdx, 1)]; // Lower extent

            if ((srx + 4 < edgeData.width - 1) && (srx - 4 >= 0)) {
              if ((suy >= 0) && (jdx < PUpperSegTrial)) {
                rightedgeresponse += 2 * edgeData.getPixel(srx, suy);
                rightedgeresponse += edgeData.getPixel(srx - 1, suy);
                rightedgeresponse += edgeData.getPixel(srx + 1, suy);
                rightedgepointsused++;
              }
              if ((sly < edgeData.height - 1) &&
                (jdx < PLowerSegTrial)) {
                rightedgeresponse += 2 * edgeData.getPixel(srx, sly);
                rightedgeresponse += edgeData.getPixel(srx - 1, sly);
                rightedgeresponse += edgeData.getPixel(srx + 1, sly);
                rightedgepointsused++;
              }
            }
            if ((slx - 4 >= 0) && (srx + 4 <= edgeData.width - 1)) {
              if ((suy >= 0) && (jdx < PUpperSegTrial)) {
                leftedgeresponse += 2 * edgeData.getPixel(slx, suy);
                leftedgeresponse += edgeData.getPixel(slx - 1, suy);
                leftedgeresponse += edgeData.getPixel(slx + 1, suy);
                leftedgepointsused++;
              }
              if ((sly < edgeData.height - 1) &&
                (jdx < PLowerSegTrial)) {
                leftedgeresponse += 2 * edgeData.getPixel(slx, sly);
                leftedgeresponse += edgeData.getPixel(slx - 1, sly);
                leftedgeresponse += edgeData.getPixel(slx + 1, sly);
                leftedgepointsused++;
              }
            }
          } // end of loop for angle segments to be added up
          if (leftedgepointsused + rightedgepointsused) {
            edgeresponse = (leftedgeresponse + rightedgeresponse) *
              100 /
              (leftedgepointsused + rightedgepointsused);
          } else {
            edgeresponse = 0;
          }
          darkpupilval *= 2; // 4
          trialresponse = edgeresponse + darkpupilval;


          if (trialresponse > bestresponse) {
            bestresponse = trialresponse;
            MaxPupilEdgeResponse = edgeresponse;
            bestdarkpupilresponse = darkpupilval;
            pupilCx = (idx - 1) * 4;
            pupilCy = (idy) * 4;
            pupilRad = (rdx) * 4;
            pupilD = rdx * 8;
          }

          {
            PupIcdist = Math.sqrt(((irisCenterY - idy) * (irisCenterY - idy) + (irisCenterX - idx) * (irisCenterX - idx)));
            if ((PupIcdist + rdx) > (irisDiameter / 2) * 65 / 100 ||
              (idy + rdx) > (irisCenterY + (irisDiameter / 2) * 65 / 100) ||
              (idy - rdx) < (idy - (irisCenterY / 2) * 65 / 100)) {
              // combination of trial radius rdx and trial pupilCx
              // (idx), pupilCy (idy) is within 85% of the edge of
              // the Iris so stop the loop
              idy = roibottom;
            }
          }//end of block
        } // end of  y loop

        if ((idx + rdx) > (irisCenterX + (irisDiameter / 2) * 65 / 100) ||
          (irisCenterX - rdx) < (idx - (irisDiameter / 2) * 65 / 100)) {
          // combination of trial radius rdx and trial pupilCx (idx),
          // pupilCy (idy) is within 85% of the edge of the Iris so
          // stop the loop
          idx = roiright;
        }
      } // end of x loop
    }     // end of R loop  ROUGH PUPIL RADIUS and Center point Done

    this.pupilCenterX = pupilCx;
    this.pupilCenterY = pupilCy;
    this.pupilRadius = pupilRad;
    this.pupilDiameter = pupilRad * 2;
  }
}