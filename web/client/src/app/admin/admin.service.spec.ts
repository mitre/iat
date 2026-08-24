/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed, inject } from '@angular/core/testing';
import { User } from '../user/user';
import { AdminService } from './admin.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

describe('IV. The AdminService Class:', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [ HttpClientTestingModule ],
    providers: [ AdminService ],
    schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
  }));

  const user = new User(
    1,
    'test',
    {
      id:1,
      name:'test',
      destinationAgencyIdentifier:'TempDAI12',
      originatingAgencyIdentifier:'TempORI12',
      attentionIndicator:'Attention',
      sourceAgency:'SRCAGENCY',
      isReportExclude:true,
      fullName:'FULL NAME',
      email:'EMAIL',
      phoneNumber:null,
      title:'TITLE',
      department:'DEPARTMENT',
      address:'ADDRESS',
      userSetting:{
        id:1,
        strokeWidth:2,
        drawColor:'#127bdc',
        fillColor:'#333333',
        numHistory:20,
        tshepiiStrokeOpacity:0
      }
    }
  );

  it('a. getAllUsers returns an Observable Array of Users.', inject([AdminService], (service: AdminService) => {
    const userArr = new Array(user);

    spyOn(service, 'getAllUsers').and.returnValue(of(userArr));
    const res = service.getAllUsers();

    res.subscribe(
      (value) => {
        expect(value).toBeInstanceOf(Array);
      }
    );
  }));
});
