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
import { JsonProperty } from '../utils/decorators/json-property';

export class Probe {
  id: string;
  name: string;
  subjectIdentifier: string;
  transactionControlReference: string;
  transactionControlNumber: string;

  @JsonProperty({clazz: EbtsImageData})
  imageList: EbtsImageData[];

  constructor() {
    this.id = undefined;
    this.name = undefined;
    this.subjectIdentifier = undefined;
    this.transactionControlReference = undefined;
    this.transactionControlNumber = undefined;
    this.imageList = [];
  }
}
