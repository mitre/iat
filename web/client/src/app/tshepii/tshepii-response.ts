/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { IwpPoint } from './iwp-point';
import { ServiceResponse, ServiceStatus } from '../types/service-response';


export class TshepiiResponse extends ServiceResponse {
  cropCenter: IwpPoint;
  originalIrisCenter: IwpPoint;
  originalPupilCenter: IwpPoint;
  cropRadius: number;
  originalIrisRadius: number;
  originalPupilRadius: number;
  keyPoints: Array<IwpPoint>;
  matchIndex: Array<number>;
  cryptList: Array<Array<IwpPoint>>;

  constructor(cropRadius: number,
              cropCenter: IwpPoint,
              originalIrisRadius: number,
              originalIrisCenter: IwpPoint,
              originalPupilRadius: number,
              originalPupilCenter: IwpPoint,
              imageId: string,
              error: string,
              status: ServiceStatus,
              ebtsImageId: string,
              isResubmit: boolean
  ) {
    super(imageId, error, status, ebtsImageId, isResubmit);
    this.cropCenter = cropCenter;
    this.originalIrisCenter = originalIrisCenter;
    this.originalPupilCenter = originalPupilCenter;

    this.cropRadius = cropRadius;
    this.originalIrisRadius = originalIrisRadius;
    this.originalPupilRadius = originalPupilRadius;

    this.keyPoints = new Array<IwpPoint>();
    this.cryptList = new Array<Array<IwpPoint>>();
    this.matchIndex = new Array<number>();
  }
}
