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
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { EbtsCreateService } from './ebts-create.service';
import { CreateIrisSearchRequest } from '../types/create-iris-search-request';
import { EbtsMessageRequest } from '../types/ebts-message-request';

describe('XI. The EbtsCreateService class:', () => {
  const searchRequest: CreateIrisSearchRequest = {
    id:'12345',
    message : new EbtsMessageRequest(),
    userId : '67890',
    creationDateEpoch : 0,
    isFinished : false,
    filePath: ''
  };
  const expectedResponse: Blob = new Blob();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EbtsCreateService],
      imports: [HttpClientModule, HttpClientTestingModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    });
  });
  it('a. createEbts takes an object instance of the CreateIrisSearchRequest class and returns an Observable Blob.', inject( [EbtsCreateService, HttpClient, HttpTestingController], ( service: EbtsCreateService, http: HttpClient, httpMock: HttpTestingController ) => {
    service.createEbts(searchRequest)
      .subscribe( (resp: any) => {
        expect(resp).toBe(expectedResponse);
      });

    const search = httpMock.expectOne(service.createServiceUrl);
    search.flush(expectedResponse);
  }));

  it('b. downloadEBTS takes a searchId and returns an Observable Blob.', inject( [EbtsCreateService, HttpClient, HttpTestingController], ( service: EbtsCreateService, http: HttpClient, httpMock: HttpTestingController ) => {
    const searchId = '12345';
    service.downloadEBTS(searchId)
      .subscribe( (resp: any) => {
        expect(resp).toBe(expectedResponse);
      });

    const search = httpMock.expectOne(service.ebtsServiceUrl+searchId);
    search.flush(expectedResponse);
  }));
});
