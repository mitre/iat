/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Annotation, AnnotationType } from '../../types/annotation';
import { Path, Point, Size, Rectangle, Raster, Color, PointText } from 'paper';

let globalRotationValue = 0;
export enum ColorChannel {
  RED,
  GREEN,
  BLUE
}

export class ColorChannelAddition {
  red: number;
  green: number;
  blue: number;
}

export class CoreFunctions {

  drawCircle(point: InstanceType<typeof Point>, radius: number, strokeColor: string, fillColor: string, strokeWidth?: number): InstanceType<typeof Path> {

    if (fillColor == undefined) {
      fillColor = 'rgba(0, 0, 0, 0)';
    }

    const shape = new Path.Circle({
      center: point,
      radius
    });
    shape.strokeColor = new Color(strokeColor);
    shape.fillColor = new Color(fillColor);

    if (strokeWidth) {
      shape.strokeWidth = strokeWidth;
    }

    return shape;
  }

  drawSquare(point: InstanceType<typeof Point>, radius: number, strokeColor: string, fillColor: string): InstanceType<typeof Path> {
    const shape = new Path.Rectangle(point, new Size(radius, radius));
    shape.strokeColor = new Color(strokeColor);
    shape.fillColor = new Color(fillColor);

    return shape;
  }

  clahe(imageData: ImageData, tileDivisor: number, clipFactor: number): ImageData {

    const imageWidth = imageData.width;
    const imageHeight = imageData.height;
    const kernelSize = {x: Math.floor(imageWidth / tileDivisor), y: Math.floor(imageHeight / tileDivisor)};
    const clipLimit = Math.floor(clipFactor * kernelSize.x * kernelSize.y);
    const middleKernelSizeX = Math.floor(kernelSize.x / 2);
    const middleKernelSizeY = Math.floor(kernelSize.y / 2);
    const numberOfTilesX = Math.floor(imageWidth / kernelSize.x);
    const numberOfTilesY = Math.floor(imageHeight / kernelSize.y);

    const grayImage = this.grayScaleImage(imageData);

    const getImagePosition = (x, y) => x + y * imageWidth;

    const hist = new Array<Array<number>>();

    for (let y = 0; y < imageHeight; y += kernelSize.y) {
      for (let x = 0; x < imageWidth; x += kernelSize.x) {
        const temp = new Array<number>(256);
        for (let t = 0; t < temp.length; t++) {
          temp[t] = 0;
        }

        for (let j = 0; j < kernelSize.y; j++) {
          for (let i = 0; i < kernelSize.x; i++) {
            const intensity = grayImage[getImagePosition(x + i, y + j)];
            temp[intensity] += 1;
          }
        }//end of through kernel loop

        hist.push(temp);
      }//end of for x
    }//end of for y

    //clipping the amount on the histograms
    for (const h of hist) {
      let excess = 0;
      //check each intensity to make sure it doesn't go past the limit
      for (let i = 0; i < 256; i++) {
        if (h[i] > clipLimit) {
          excess += h[i] - clipLimit;
          h[i] = clipLimit;
        }
      }//end of for

      if (excess > 0) {
        const additionalAmt = excess / 256;
        for (let i = 0; i < 256; i++) {
          h[i] += additionalAmt;
        }
      }

    }

    const cdf = new Array<Array<number>>();
    for (const h of hist) {
      const temp = new Array<number>(h.length);
      temp[0] = h[0] / (kernelSize.x * kernelSize.y);

      for (let t = 1; t < h.length; t++) {
        temp[t] = temp[t - 1] + (h[t] / (kernelSize.x * kernelSize.y));
      }//end of for

      cdf.push(temp);
    }//end of for

    //try to do bileear interpolation
    //https://en.wikipedia.org/wiki/Bilinear_interpolation <---came from here
    //for each image point you are trying to find four tile center points that
    //surround the point with point1 being top left, point2 being top right,
    //point3 bottom right, and point4 bottom left.  You then take a bilinear interpolation
    //of those points to figure out the intensity of the image point.  To paraphrase you
    //are using the surrounding tile's histograms to come up with the intensity of
    //a point which is an approximation of taking a point's histogram and cdf and
    //using that to get the intensity of a point which would be to computationally
    //intensive to use.
    for (let y = middleKernelSizeY; y < imageHeight - (middleKernelSizeY); y++) {
      for (let x = middleKernelSizeX; x < imageWidth - (middleKernelSizeX); x++) {
        //this is the tile that it is in
        const cy = Math.floor(y / kernelSize.y);
        const cx = Math.floor(x / kernelSize.x);

        //check if it is the middle point in the tile
        const tempx = x % kernelSize.x;
        const tempy = y % kernelSize.y;
        if (tempx == middleKernelSizeX && tempy == middleKernelSizeY) {
          const intense = grayImage[getImagePosition(x, y)];
          grayImage[getImagePosition(x, y)] = cdf[Math.floor(cx + (cy * (numberOfTilesX)))][intense] * 255;
          continue;
        }

        const point1 = {x: 0, y: 0, cx: 0, cy: 0};
        const point2 = {x: 0, y: 0, cx: 0, cy: 0};
        const point3 = {x: 0, y: 0, cx: 0, cy: 0};
        const point4 = {x: 0, y: 0, cx: 0, cy: 0};

        //get the four closest middle tile points
        if (tempx < middleKernelSizeX && tempy < middleKernelSizeY) {
          //left top quadrant of tile
          point1.x = (cx - 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point1.y = (cy - 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point1.cx = cx - 1;
          point1.cy = cy - 1;

          point2.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point2.y = (cy - 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy - 1;

          point3.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point3.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point3.cx = cx;
          point3.cy = cy;

          point4.x = (cx - 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point4.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point4.cx = cx - 1;
          point4.cy = cy;

        } else if (tempx >= middleKernelSizeX && tempy < middleKernelSizeY) {
          //right top quadrant of tile
          point1.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point1.y = (cy - 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy - 1;

          point2.x = (cx + 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point2.y = (cy - 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point2.cx = cx + 1;
          point2.cy = cy - 1;

          point3.x = (cx + 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point3.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point3.cx = cx + 1;
          point3.cy = cy;

          point4.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point4.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point4.cx = cx;
          point4.cy = cy;

        } else if (tempx >= middleKernelSizeX && tempy >= middleKernelSizeY) {
          //right bottom quadrant of tile
          point1.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point1.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy;

          point2.x = (cx + 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point2.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point2.cx = cx + 1;
          point2.cy = cy;

          point3.x = (cx + 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point3.y = (cy + 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point3.cx = cx + 1;
          point3.cy = cy + 1;

          point4.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point4.y = (cy + 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point4.cx = cx;
          point4.cy = cy + 1;

        } else if (tempx < middleKernelSizeX && tempy >= middleKernelSizeY) {
          //left bottom quadrant of tile
          point1.x = (cx - 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point1.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point1.cx = cx - 1;
          point1.cy = cy;

          point2.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point2.y = (cy) * kernelSize.y + Math.floor(middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy;

          point3.x = (cx) * kernelSize.x + Math.floor(middleKernelSizeX);
          point3.y = (cy + 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point3.cx = cx;
          point3.cy = cy + 1;

          point4.x = (cx - 1) * kernelSize.x + Math.floor(middleKernelSizeX);
          point4.y = (cy + 1) * kernelSize.y + Math.floor(middleKernelSizeY);
          point4.cx = cx - 1;
          point4.cy = cy + 1;

        }

        const intensity = grayImage[getImagePosition(x, y)];
        const fq11 = cdf[Math.floor((point1.cx) + ((point1.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq21 = cdf[Math.floor((point2.cx) + ((point2.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq12 = cdf[Math.floor((point4.cx) + ((point4.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq22 = cdf[Math.floor((point3.cx) + ((point3.cy) * (numberOfTilesX)))][intensity] * 255;

        const fxy1 = ((point2.x - x) / (point2.x - point1.x)) * fq11 + ((x - point1.x) / (point2.x - point1.x)) * fq21;
        const fxy2 = ((point3.x - x) / (point3.x - point4.x)) * fq12 + ((x - point4.x) / (point3.x - point4.x)) * fq22;

        const fxy = ((point3.y - y) / (point3.y - point1.y)) * fxy1 + ((y - point1.y) / (point3.y - point1.y)) * fxy2;

        grayImage[getImagePosition(x, y)] = fxy;

      }
    }

    //do the transfrom for the four corners
    //the transformation function is the histogram equalization function for the tile
    //which is intensity = cdf(intensity) * maximum gray level
    for (let y = 0; y < middleKernelSizeY; y++) {
      for (let x = 0; x < middleKernelSizeX; x++) {
        //upper left
        let intensity = grayImage[getImagePosition(x, y)];
        grayImage[getImagePosition(x, y)] = cdf[0][intensity] * 255;

        //upper right
        const upperRightcx = Math.floor(numberOfTilesX - 1);
        const upperRightcy = 0;
        intensity = grayImage[getImagePosition(x + upperRightcx * kernelSize.x + (middleKernelSizeX), y)];
        grayImage[getImagePosition(x + upperRightcx * kernelSize.x + (middleKernelSizeX), y)] = cdf[upperRightcx + (upperRightcy * (numberOfTilesX))][intensity] * 255;

        //bottom left
        const lowerLeftcx = 0;
        const lowerLeftcy = Math.floor(numberOfTilesY - 1);
        intensity = grayImage[getImagePosition(x, y + lowerLeftcy * kernelSize.y)];
        grayImage[getImagePosition(x, y + lowerLeftcy * kernelSize.y + (middleKernelSizeY))] = cdf[lowerLeftcx + (lowerLeftcy * (numberOfTilesX))][intensity] * 255;

        //bottom right
        const lowerRightcx = Math.floor(numberOfTilesX - 1);
        const lowerRightcy = Math.floor(numberOfTilesY - 1);
        intensity = grayImage[getImagePosition(x + lowerRightcx * kernelSize.x + (middleKernelSizeX), y + lowerRightcy * kernelSize.y + (middleKernelSizeY))];
        grayImage[getImagePosition(x + lowerRightcx * kernelSize.x + (middleKernelSizeX), y + lowerRightcy * kernelSize.y + (middleKernelSizeY))] = cdf[lowerRightcx + (lowerRightcy * (numberOfTilesX))][intensity] * 255;
      }
    }

    //do the bars on the side and use linear interpolation
    //Comes from https://en.wikipedia.org/wiki/Linear_interpolation
    //y = (y0 * (x1 - x) + y1 * (x - x0) ) / (x1 - x0) = y0 * (x1-x)/(x1-x0) + y1 * (x-x0)/(x1-x0)
    //much like above in the bilinear interpolation
    //you don't have to worry about the other variable for the point (x,y) since they are the same
    //for this example what you are worried about is between (x, f(x, y)) with y being a constant
    //below for the right left bar x is the constant and y is the variable i.e. (y, f(x,y)) is what
    //you are worried about
    //top bar
    for (let y = 0; y < middleKernelSizeY; y++) {
      for (let x = middleKernelSizeX; x < imageWidth - (middleKernelSizeX); x++) {
        const cx = Math.floor(x / kernelSize.x);

        const point1 = {x: 0, y: 0, cx: 0, cy: 0};
        const point2 = {x: 0, y: 0, cx: 0, cy: 0};
        if (x % kernelSize.x < (middleKernelSizeX)) {
          //on the left side of the current tile half i.e. upper left
          point1.x = (cx - 1) * kernelSize.x + (middleKernelSizeX);
          point1.y = middleKernelSizeY;
          point1.cx = cx - 1;
          point1.cy = 0;

          point2.x = (cx) * kernelSize.x + (middleKernelSizeX);
          point2.y = middleKernelSizeY;
          point2.cx = cx;
          point2.cy = 0;
        } else {
          //on the right side of the current tile half i.e. upper right
          point1.x = (cx) * kernelSize.x + (middleKernelSizeX);
          point1.y = middleKernelSizeY;
          point1.cx = cx;
          point1.cy = 0;

          point2.x = (cx + 1) * kernelSize.x + (middleKernelSizeX);
          point2.y = middleKernelSizeY;
          point2.cx = cx + 1;
          point2.cy = 0;
        }

        const intensity = grayImage[getImagePosition(x, y)];
        const fq11 = cdf[Math.floor((point1.cx) + ((point1.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq21 = cdf[Math.floor((point2.cx) + ((point2.cy) * (numberOfTilesX)))][intensity] * 255;
        const fxy1 = ((point2.x - x) / (point2.x - point1.x)) * fq11 + ((x - point1.x) / (point2.x - point1.x)) * fq21;
        grayImage[getImagePosition(x, y)] = fxy1;
      }
    }

    //bottom bar
    for (let y = imageHeight - (middleKernelSizeY); y < imageHeight; y++) {
      for (let x = middleKernelSizeX; x < imageWidth - (middleKernelSizeX); x++) {
        const cx = Math.floor(x / kernelSize.x);
        const cy = Math.floor(numberOfTilesY - 1);

        const point1 = {x: 0, y: 0, cx: 0, cy: 0};
        const point2 = {x: 0, y: 0, cx: 0, cy: 0};
        if (x % kernelSize.x < (middleKernelSizeX)) {
          //on the left side of the current tile half i.e. upper left
          point1.x = (cx - 1) * kernelSize.x + (middleKernelSizeX);
          point1.y = middleKernelSizeY;
          point1.cx = cx - 1;
          point1.cy = cy;

          point2.x = (cx) * kernelSize.x + (middleKernelSizeX);
          point2.y = middleKernelSizeY;
          point2.cx = cx;
          point2.cy = cy;
        } else {
          //on the right side of the current tile half i.e. upper right
          point1.x = (cx) * kernelSize.x + (middleKernelSizeX);
          point1.y = middleKernelSizeY;
          point1.cx = cx;
          point1.cy = cy;

          point2.x = (cx + 1) * kernelSize.x + (middleKernelSizeX);
          point2.y = middleKernelSizeY;
          point2.cx = cx + 1;
          point2.cy = cy;
        }

        const intensity = grayImage[getImagePosition(x, y)];
        const fq11 = cdf[Math.floor((point1.cx) + ((point1.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq21 = cdf[Math.floor((point2.cx) + ((point2.cy) * (numberOfTilesX)))][intensity] * 255;
        const fxy1 = ((point2.x - x) / (point2.x - point1.x)) * fq11 + ((x - point1.x) / (point2.x - point1.x)) * fq21;
        grayImage[getImagePosition(x, y)] = fxy1;
      }
    }

    //left bar
    for (let y = middleKernelSizeY; y < imageHeight - (middleKernelSizeY); y++) {
      for (let x = 0; x < (middleKernelSizeX); x++) {
        const cx = 0;
        const cy = Math.floor(y / kernelSize.y);

        const point1 = {x: 0, y: 0, cx: 0, cy: 0};
        const point2 = {x: 0, y: 0, cx: 0, cy: 0};
        if (y % kernelSize.y < (middleKernelSizeY)) {
          //the point is top left
          point1.x = middleKernelSizeX;
          point1.y = (cy - 1) * kernelSize.y + (middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy - 1;

          point2.x = middleKernelSizeX;
          point2.y = (cy) * kernelSize.y + (middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy;
        } else {
          //the point is bottom left
          point1.x = middleKernelSizeX;
          point1.y = (cy) * kernelSize.y + (middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy;

          point2.x = middleKernelSizeX;
          point2.y = (cy + 1) * kernelSize.y + (middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy + 1;
        }

        const intensity = grayImage[getImagePosition(x, y)];
        const fq11 = cdf[Math.floor((point1.cx) + ((point1.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq21 = cdf[Math.floor((point2.cx) + ((point2.cy) * (numberOfTilesX)))][intensity] * 255;
        const fxy1 = ((point2.y - y) / (point2.y - point1.y)) * fq11 + ((y - point1.y) / (point2.y - point1.y)) * fq21;
        grayImage[getImagePosition(x, y)] = fxy1;
      }
    }

    //right bar
    for (let y = middleKernelSizeY; y < imageHeight - (middleKernelSizeY); y++) {
      for (let x = imageWidth - (middleKernelSizeX); x < imageWidth; x++) {
        const cx = Math.floor((numberOfTilesX) - 1);
        const cy = Math.floor(y / kernelSize.y);

        const point1 = {x: 0, y: 0, cx: 0, cy: 0};
        const point2 = {x: 0, y: 0, cx: 0, cy: 0};
        if (y % kernelSize.y < (middleKernelSizeY)) {
          //the point is top left
          point1.x = middleKernelSizeX;
          point1.y = (cy - 1) * kernelSize.y + (middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy - 1;

          point2.x = middleKernelSizeX;
          point2.y = (cy) * kernelSize.y + (middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy;
        } else {
          //the point is bottom left
          point1.x = middleKernelSizeX;
          point1.y = (cy) * kernelSize.y + (middleKernelSizeY);
          point1.cx = cx;
          point1.cy = cy;

          point2.x = middleKernelSizeX;
          point2.y = (cy + 1) * kernelSize.y + (middleKernelSizeY);
          point2.cx = cx;
          point2.cy = cy + 1;
        }

        const intensity = grayImage[getImagePosition(x, y)];
        const fq11 = cdf[Math.floor((point1.cx) + ((point1.cy) * (numberOfTilesX)))][intensity] * 255;
        const fq21 = cdf[Math.floor((point2.cx) + ((point2.cy) * (numberOfTilesX)))][intensity] * 255;
        const fxy1 = ((point2.y - y) / (point2.y - point1.y)) * fq11 + ((y - point1.y) / (point2.y - point1.y)) * fq21;
        grayImage[getImagePosition(x, y)] = fxy1;
      }
    }

    const ret = this.grayToRgb(grayImage);
    return new ImageData(ret, imageData.width, imageData.height);
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

  //only works agains gray scale images
  getHistogram(arr: Uint8ClampedArray, start: number, end: number): Array<number> {
    const ret = new Array<number>(256);
    for (let i = 0; i < ret.length; i++) {
      ret[i] = 0;
    }

    for (let x = start; x < end; x++) {
      const intensity = arr[x];
      ret[intensity] += 1;
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

  adjustBrightness(imageData: ImageData, adjustment: number): ImageData {
    // based on https://www.html5rocks.com/en/tutorials/canvas/imagefilters/
    const d = imageData.data;

    for (let i = 0; i < d.length; i += 4) {
      d[i] = d[i] += adjustment;
      d[i + 1] = d[i + 1] += adjustment;
      d[i + 2] = d[i + 2] += adjustment;
    }
    return imageData;
  }

  changeColors(color: ColorChannel, originalData: ImageData): ImageData {
    const ret = new ImageData(originalData.data, originalData.width, originalData.height);
    const data = ret.data;
    const origData = originalData.data;

    for (let i = 0; i < data.length; i += 4) {
      switch (+color) {
        case ColorChannel.RED:
          data[i] = origData[i];
          data[i + 1] = origData[i];
          data[i + 2] = origData[i];
          break;
        case ColorChannel.GREEN:
          data[i] = origData[i + 1];
          data[i + 1] = origData[i + 1];
          data[i + 2] = origData[i + 1];
          break;
        case ColorChannel.BLUE:
          data[i] = origData[i + 2];
          data[i + 1] = origData[i + 2];
          data[i + 2] = origData[i + 2];
          break;
        default:
          data[i] = origData[i];
          data[i + 1] = origData[i + 1];
          data[i + 2] = origData[i + 2];
          break;
      }
    }
    return ret;
  }

  addColor(addition: ColorChannelAddition, originalData: ImageData): ImageData {
    const imageData = new ImageData(originalData.data, originalData.width, originalData.height);
    const data = imageData.data;
    const origData = originalData.data;

    for (let i = 0; i < data.length; i += 4) {
      // red
      data[i] = origData[i] + Number(addition.red);
      // green
      data[i + 1] = origData[i + 1] + Number(addition.green);
      // blue
      data[i + 2] = origData[i + 2] + Number(addition.blue);
    }

    return imageData;
  }

  //contrast is in the range of [-100, 100]
  changeContrast(imageData: ImageData, contrast: number): ImageData {
    // based on https://stackoverflow.com/questions/10521978/html5-canvas-image-contrast
    const d = imageData.data;
    contrast = (contrast / 100) + 1;
    const intercept = 128 * (1 - contrast);

    for (let i = 0; i < d.length; i += 4) {
      d[i] = d[i] * contrast + intercept;
      d[i + 1] = d[i + 1] * contrast + intercept;
      d[i + 2] = d[i + 2] * contrast + intercept;
    }
    return imageData;
  }

  drawRect(from: InstanceType<typeof Point>, to: InstanceType<typeof Point>, strokeColor: string, strokeWidth?: number): InstanceType<typeof Path> {
    const ret = new Path.Rectangle(from, to);
    ret.strokeColor = new Color(strokeColor);
    ret.strokeWidth = strokeWidth ? strokeWidth : 2;
    return ret;
  }

  rotatePoint(point: InstanceType<typeof Point>, angle: number, origin: InstanceType<typeof Point>): InstanceType<typeof Point> {
    const x = point.x - origin.x;
    const y = point.y - origin.y;

    let xprime = x * Math.cos(angle) - y * Math.sin(angle);
    let yprime = x * Math.sin(angle) + y * Math.cos(angle);

    xprime += origin.x;
    yprime += origin.y;

    return new Point(xprime, yprime);
  }

  getSubRaster(x: number, y: number, width: number, height: number, raster: InstanceType<typeof Raster>, left: number, top: number): InstanceType<typeof Raster> {
    let rasterRotation = raster.parent.project.view.rotation;
    const tempMatrix = (<any>raster.parent.project.view).matrix.clone();
    rasterRotation = rasterRotation * (Math.PI / 180.0);

    x = x - left;
    y = y - top;

    const tempPt = this.rotatePoint(new Point(x, y), rasterRotation, raster.bounds.center);
    x = tempPt.x;
    y = tempPt.y;


    if (rasterRotation != 0) {
      const rotation = -rasterRotation;

      const imageData = raster.getImageData(null);
      const subImageData = new ImageData(width, height);

      for (let j = y; j < y + height; j++) {
        for (let i = x; i < x + width; i++) {
          const pt = this.rotatePoint(new Point(i, j), rotation, raster.bounds.center);
          pt.x = Math.round(pt.x);
          pt.y = Math.round(pt.y);

          if ((pt.x < imageData.width && pt.y < imageData.height) && (pt.x > 0 && pt.y > 0)) {
            const index = Math.floor((pt.x + (pt.y * imageData.width)) * 4);
            const sIndex = Math.floor((i - x) + ((j - y) * subImageData.width)) * 4;

            subImageData.data[sIndex] = imageData.data[index];
            subImageData.data[sIndex + 1] = imageData.data[index + 1];
            subImageData.data[sIndex + 2] = imageData.data[index + 2];
            subImageData.data[sIndex + 3] = imageData.data[index + 3];
          } else {
            const sIndex = Math.floor((i - x) + ((j - y) * subImageData.width)) * 4;
            subImageData.data[sIndex] = 255;
            subImageData.data[sIndex + 1] = 255;
            subImageData.data[sIndex + 2] = 255;
            subImageData.data[sIndex + 3] = 0;

          }//end of else
        }
      }

      const subRaster = new Raster();
      subRaster.size = new Size(width, height);
      subRaster.setImageData(subImageData);

      return subRaster;
    } else {
      const rect = new Rectangle(x, y, width, height);
      return raster.getSubRaster(rect);
    }
  }

  mirrorImage(raster: InstanceType<typeof Raster>): void {
    raster.scale(-1, 1);
  }

  drawPath(points: Array<InstanceType<typeof Point>>, strokeColor: string, strokeWidth: number) {
    const path = new Path();
    path.strokeColor = new Color(strokeColor);
    path.strokeWidth = strokeWidth;

    for (const point of points) {
      path.add(point);
    }

    //smooth the line
    path.simplify();

    return path;
  }

  drawPolygon(points: Array<InstanceType<typeof Point>>, strokeColor: string, strokeWidth: number, fillColor: string, fillAlpha: number, closed?: boolean) {
    const path = new Path();
    path.strokeColor = new Color(strokeColor);
    path.strokeWidth = strokeWidth;

    if (closed == null || closed === true) {
      path.fillColor = new Color(fillColor);
      path.fillColor.alpha = fillAlpha;
    } else {
      path.fillColor = new Color(0, 0);
    }

    for (const point of points) {
      path.add(point);
    }//end of for loop

    if (closed != null) {
      path.closed = closed;
    } else {
      path.closed = true;
    }
    return path;
  }

  rotate(raster: InstanceType<typeof Raster>, rotation: number) {
    //raster.rotate(rotation);
    const temp = (<any>raster.parent.project.view);
    temp.rotate(rotation, raster.bounds.center);
  }

  resize(raster: InstanceType<typeof Raster>, newWidth: number, newHeight: number) {
    raster.size = new Size(newWidth, newHeight);
  }

  sharpen(sourceImage: ImageData, sharpen: number, opaque?: boolean): ImageData {
    const frac = (sharpen - 1) / -4;
    const weights = [
      0, frac, 0,
      frac, sharpen, frac,
      0, frac, 0
    ];
    const imageData = new ImageData(sourceImage.width, sourceImage.height);

    // based on https://www.html5rocks.com/en/tutorials/canvas/imagefilters/
    const src = sourceImage.data;
    const dst = imageData.data;

    const side = Math.round(Math.sqrt(weights.length));
    const halfSide = Math.floor(side / 2);

    const sw = imageData.width;
    const sh = imageData.height;
    const w = sw;
    const h = sh;

    const alphaFac = opaque ? 1 : 0;

    for (let y = 0; y < h; y++) {
      for (let x = 0; x < w; x++) {
        const sy = y;
        const sx = x;
        const dstOff = (y * w + x) * 4;

        let r = 0; let g = 0; let b = 0; let a = 0;
        for (let cy = 0; cy < side; cy++) {
          for (let cx = 0; cx < side; cx++) {
            const scy = sy + cy - halfSide;
            const scx = sx + cx - halfSide;

            if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {
              const srcOff = (scy * sw + scx) * 4;
              const wt = weights[cy * side + cx];
              r += src[srcOff] * wt;
              g += src[srcOff + 1] * wt;
              b += src[srcOff + 2] * wt;
              a += src[srcOff + 3] * wt;
            }
          }
        }

        dst[dstOff] = r;
        dst[dstOff + 1] = g;
        dst[dstOff + 2] = b;
        dst[dstOff + 3] = a + alphaFac * (255 - a);
      }
    }

    return imageData;
  }

  drawAnnotationText(point: InstanceType<typeof Point>, textLabel: string, textColor: string, textSize: number): InstanceType<typeof PointText> {
    const text = new PointText(point);               // addChild returns an Item, cast to PointText
    text.justification = 'center';
    text.fillColor = new Color(textColor);
    text.content = textLabel;
    text.fontSize = textSize;
    return text;
  }

  drawAnnotation(annotation: Annotation, raster: InstanceType<typeof Raster>): InstanceType<typeof Path> {
    let item;
    let imageData;

    //the plus sign is to get the annotations to work since typescript is a stupid language
    switch (+annotation.type) {
      case AnnotationType.POINT:
      case AnnotationType.CIRCLE:
        const point = new Point(annotation.value.points[0].x, annotation.value.points[0].y);
        item = this.drawCircle(point,
          annotation.value.data['radius'],
          annotation.value.data['strokeColor'],
          annotation.value.data['fillColor'],
          annotation.value.data['strokeWidth']);
        break;
      case AnnotationType.POLYGON:
        item = this.drawPolygon(annotation.value.points,
          annotation.value.data['strokeColor'],
          annotation.value.data['strokeWidth'],
          annotation.value.data['fillColor'],
          annotation.value.data['fillAlpha']);
        break;
      case AnnotationType.EYELID:
        item = this.drawPolygon(annotation.value.points,
          annotation.value.data['strokeColor'],
          annotation.value.data['strokeWidth'],
          annotation.value.data['fillColor'],
          annotation.value.data['fillAlpha'],
          false);
        break;
      case AnnotationType.PENCIL:
        item = this.drawPath(annotation.value.points,
          annotation.value.data['strokeColor'],
          annotation.value.data['strokeWidth']);
        break;
      case AnnotationType.CLAHE:
        imageData = this.clahe(raster.getImageData(null),
          annotation.value.data['tileDivisor'],
          annotation.value.data['clipFactor']);
        raster.setImageData(imageData);
        break;
      case AnnotationType.BRIGHTNESS:
        imageData = this.adjustBrightness(raster.getImageData(null),
          +annotation.value.data['adjustment']);
        raster.setImageData(imageData);
        break;
      case AnnotationType.CONTRAST:
        imageData = this.changeContrast(raster.getImageData(null),
          annotation.value.data['adjustment']);
        raster.setImageData(imageData);
        break;
      case AnnotationType.CHANNEL:
        let colorChannel;
        const color = annotation.value.data['channel'];
        if (color == 'Red') {
          colorChannel = ColorChannel.RED;
        } else if (color == 'Green') {
          colorChannel = ColorChannel.GREEN;
        } else if (color == 'Blue') {
          colorChannel = ColorChannel.BLUE;
        }
        imageData = this.changeColors(colorChannel, raster.getImageData(null));
        raster.setImageData(imageData);
        break;
      case AnnotationType.COLOR_LEVEL:
        imageData = this.addColor({
          red: annotation.value.data['red'],
          green: annotation.value.data['green'],
          blue: annotation.value.data['blue']
        }, raster.getImageData(null));
        raster.setImageData(imageData);
        break;
      case AnnotationType.CROP:
//      Make image normal - remove rotate
        const tempMatrix1 = (<any>raster.parent.project.view).matrix;
        tempMatrix1.append(tempMatrix1.clone().invert());

        const subraster = this.getSubRaster(annotation.value.data['fromPointX'],
          annotation.value.data['fromPointY'],
          annotation.value.data['width'],
          annotation.value.data['height'],
          raster,
          annotation.value.data['boundsLeft'],
          annotation.value.data['boundsTop']);
        //have to remove the subraster so it doesn't appear
        subraster.remove();

        raster.size = new Size(subraster.width, subraster.height);
        raster.setImageData(subraster.getImageData(null));

        //If there is rotation involved....
        this.rotate(raster, globalRotationValue);
        globalRotationValue = 0;

        break;
      case AnnotationType.ROTATE:
        globalRotationValue = annotation.value.data['adjustment'];
        this.rotate(raster, annotation.value.data['adjustment']);
        break;
      case AnnotationType.SHARPEN:
        imageData = this.sharpen(raster.getImageData(null),
          annotation.value.data['adjustment']);
        raster.setImageData(imageData);
        break;
      case AnnotationType.RESIZE:
        this.resize(raster, annotation.value.data['width'], annotation.value.data['height']);
        break;
      case AnnotationType.MIRROR:
        this.mirrorImage(raster);
        break;
      case AnnotationType.PDM:
        break;
      default:
        break;
    }

    return item;
  }
}
