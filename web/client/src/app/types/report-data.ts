/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Probe } from './probe';
import { Candidate } from './candidate';
import { CaseInformation } from './case-information';
import { JsonProperty } from '../utils/decorators/json-property';
import { Profile } from '../profile/profile';

export enum ResponseType {
  SRB = 1,
  ERRB = 2,
  XML = 3,
  Image = 4,
  Unknown = 0
}

export class ReportData {
  id: string;
  fileName: string;
  allowNullRequester: boolean;

  @JsonProperty({clazz: Probe})
  probe: Probe;

  @JsonProperty({clazz: Candidate})
  candidates: Candidate[];

  includeExclude: boolean;
  responseType: ResponseType;

  @JsonProperty({clazz: CaseInformation})
  caseInformation: CaseInformation;

  userId: string;
  isFinished: boolean;
  errorMessages: string[];
  invalidFields: string[];

  workedBy: UserAuthentication;

  constructor() {
    this.id = '';
    this.fileName = '';
    this.probe = new Probe();
    this.candidates = [];
    this.includeExclude = false;
    this.responseType = ResponseType.Unknown;
    this.caseInformation = new CaseInformation();
    this.userId = '';
    this.isFinished = false;
    this.errorMessages = Array<string>();
    this.invalidFields = Array<string>();
    this.workedBy = new UserAuthentication();
    this.allowNullRequester = false;
  }
}

export class UserAuthentication {
  profile: Profile;
}



