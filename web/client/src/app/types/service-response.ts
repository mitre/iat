/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

export enum ServiceStatus {
  NotSent = 'NotSent',
  Pending = 'Pending',
  Error = 'Error',
  Received = 'Received'
}

export class ServiceResponse {
  // imageId is the modified id number
  // imageId and ebtsImageId can be the same number it's not a resubmitted image.
  imageId = '';
  error = '';
  status: ServiceStatus;
  ebtsImageId = '';
  isResubmit = false;

  constructor(
    imageId: string,
    error: string,
    status: ServiceStatus,
    ebtsImageId: string,
    isResubmit: boolean) {
    this.imageId = imageId;
    this.error = error;
    this.status = status;
    this.ebtsImageId = ebtsImageId;
    this.isResubmit = isResubmit;
  }
}
