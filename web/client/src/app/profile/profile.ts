/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

export class Profile {
  id: string;
  name: string;
  destinationAgencyIdentifier: string;
  originatingAgencyIdentifier: string;
  attentionIndicator: string;
  sourceAgency: string;
  isReportExclude: boolean;

  fullName: string;
  email: string;
  phoneNumber: string;
  title: string;
  department: string;
  address: string;
  userSetting: UserSetting;
  permissions: any;

  constructor() {
    this.id= '';
    this.name= '';
    this.destinationAgencyIdentifier= '';
    this.originatingAgencyIdentifier= '';
    this.attentionIndicator= '';
    this.sourceAgency= '';
    this.isReportExclude= false;
    this.fullName= '';
    this.email= '';
    this.phoneNumber= '';
    this.title= '';
    this.department= '';
    this.address= '';
    this.permissions= {};
    this.userSetting = new UserSetting();
  }
}//end of Profile

export class UserSetting {
  strokeWidth: number;
  drawColor: string;
  fillColor: string;
  numHistory: number;
  tshepiiStrokeOpacity: number;

  constructor() {
    this.strokeWidth = 2;
    this.drawColor = '#127bdc';
    this.fillColor = '#333333';
    this.numHistory = 10;
    this.tshepiiStrokeOpacity = 0;
  }
}