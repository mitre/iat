/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { EyeLabel } from './eye-label';
import { Annotation } from './annotation';
import { State } from './state';
import { JsonProperty } from '../utils/decorators/json-property';
import { ContactType } from './contact-type';

export class EbtsImageData {
  id: string;
  ebtsImageId: string;
  unrollImageId: string;
  imageData: string;
  unrollImageData: string;
  eyeLabel: EyeLabel;
  detectedContact: boolean;
  contactType: ContactType;
  dme: string;
  qualityScore = -1;
  recordIndex = -1;
  recordType = -1;

  @JsonProperty({clazz: Annotation})
  annotations: Annotation[];

  @JsonProperty({clazz: State})
  pastStates: State[];

  @JsonProperty({clazz: State})
  currState: State;

  unrollRingData: object;
  imageNotes: string;

  // This empty constructor with superficial definitions
  // are needed bacause JsonProperty.getJsonProperty won't
  // see the properties unless defined
  constructor() {
    this.id = '';
    this.ebtsImageId = '';
    this.unrollImageId = '';
    this.imageData = '';
    this.unrollImageData = '';
    this.eyeLabel = EyeLabel[''];
    this.contactType = ContactType[''];
    this.qualityScore = 0;
    this.annotations = [];
    this.pastStates = [];
    this.currState = new State();
    this.unrollRingData = {};
    this.imageNotes = '';
    this.dme = '';
  }
}
