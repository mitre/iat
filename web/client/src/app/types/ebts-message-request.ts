/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { EbtsImageData } from './image-data';


export class EbtsMessageRequest {
  id: string;
  cinPrefix: string;                    // cin_pre mandatory 24 character
  cinIdentifier: string;                // cin_id mandatory 24 character
  numberOfCandidates: number;           // ncr default: 20 min: 2 max: 50
  caseExtension: number;                // cix 2 to 4 byte mandatory
  dateOfSubmission: string;             // dat mandatory YYYYMMDD
  destinationAgencyIdentifier: string;  // dai mandatory
  originatingAgencyIdentifier: string;  // ori mandatory
  transactionControlNumber: string;     // tcn mandatory
  attentionIndicator: string;                   // elr mandatory
  sourceAgency: string;                 // src mandatory
  irisCaptureDate: string;              // icd mandatory YYYYMMDD
  rotationOfEye: number;                // rae optional
  rotationUncertainty: number;  // rau optional -- mandatory if rae is present
  invalidFields: any;
  errorMessages: string[];

  imageData: EbtsImageData;

  constructor() {
    this.id = '';
    this.cinPrefix = '';
    this.cinIdentifier = '';
    this.numberOfCandidates = 0;
    this.caseExtension = 0;
    this.dateOfSubmission = '';
    this.destinationAgencyIdentifier = '';
    this.originatingAgencyIdentifier = '';
    this.transactionControlNumber = '';
    this.attentionIndicator = '';
    this.sourceAgency = '';
    this.irisCaptureDate = '';
    this.rotationOfEye = 0;
    this.rotationUncertainty = 0;
    this.invalidFields = '';
    this.imageData = new EbtsImageData();
    this.errorMessages = Array<string>();
  }
}
