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


export class ContactClassifierResponse extends ServiceResponse {
  detectionCode: string;
  processTimeMS: string;

  constructor(detectionCode: string,
              processTimeMS: string,
              imageId: string,
              error: string,
              status: ServiceStatus,
              ebtsImageId: string,
              isResubmit: boolean
  ) {
    super(imageId, error, status, ebtsImageId, isResubmit);
    this.detectionCode = detectionCode;
    this.processTimeMS = processTimeMS;
  }
}
