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


export class Candidate {
  id: string;
  score: number;
  subjectIdentifier: string;
  adjudicationResults: string;
  currentImageIndex = -1;

  @JsonProperty({clazz: EbtsImageData})
  imageList: EbtsImageData[];

  note: string;

  constructor() {
    this.id = undefined;
    this.score = 0;
    this.subjectIdentifier = undefined;
    this.adjudicationResults = undefined;
    this.imageList = [];
    this.note = undefined;
  }
}
