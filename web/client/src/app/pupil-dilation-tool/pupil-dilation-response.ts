/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/

import { ServiceResponse, ServiceStatus } from '../types/service-response';


export class PupilDilationResponse extends ServiceResponse {
    responseStatus: ServiceStatus;
    alphaBytes: DeformedImageBytes;
    alphaImageData: DeformedImageData;

    constructor(
        imageId: string,
        error: string,
        modeStatus: ServiceStatus,
        responseStatus: ServiceStatus,
        ebtsImageId: string,
        // alphaScoreMapper connects alphaScore (string) to a DeformedImage (DeformedImage)
        alphaBytes: DeformedImageBytes,
        isResubmit: boolean
    ) {
        super(imageId, error, modeStatus, ebtsImageId, isResubmit);
        this.responseStatus = responseStatus;
        this.alphaBytes = alphaBytes;
    }
}

export class DeformedImageBytes {
    
    deformedId: number;
    imageData: number[];
    // Score is the Confidence Score
    score: number;
    imageType: string;
    width: number;
    height: number;

    constructor(
        deformedId: number,
        imageData: number[],
        score: number,
        imageType: string,
        width: number,
        height: number
    ){
        this.deformedId = deformedId;
        this.imageData = imageData;
        this.score = score;
        this.imageType = imageType;
        this.width = width;
        this.height = height;
    }
}

export class DeformedImageData {
    
    imageData: ImageData;
    // Score is the Confidence Score
    score: number;
    imageType: string;
    width: number;
    height: number;

    constructor(
        imageData: ImageData,
        score: number,
        imageType: string,
        width: number,
        height: number
    ){
        this.imageData = imageData;
        this.score = score;
        this.imageType = imageType;
        this.width = width;
        this.height = height;
    }

}
