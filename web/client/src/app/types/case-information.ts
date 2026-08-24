/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Person } from './person';
import { JsonProperty } from '../utils/decorators/json-property';

export class CaseInformation {
  @JsonProperty({clazz: Person})
  conductedBy: Person;

  @JsonProperty({clazz: Person})
  requestedBy: Person;

  comparisonDate: number;
  receivedDate: number;


  constructor() {
    this.conductedBy = undefined;
    this.requestedBy = undefined;
    this.comparisonDate = new Date().getMilliseconds();
    this.receivedDate = new Date().getMilliseconds();
  }
}
