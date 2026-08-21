/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { TestBed, inject } from '@angular/core/testing';
import { HistoryService } from './history.service';
import { Observable } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CreateIrisSearchRequest } from '../types/create-iris-search-request';

describe(`XV. The HistoryService component class:`, () => {
  const start = 10;
  const limit = 17;
  const column = 'foo';
  const dir = 'bar';
  const id = 'spam';
  const iris = new CreateIrisSearchRequest();
  const reviewId = 'John Doe';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HistoryService]
    });
  });

  it(`a. getIrisSearchCreateList takes two numbers like ${start} and ${limit} and two strings like '${column}' and '${dir}' as parameters and returns an Observable Array of type CreateIrisSearchRequest.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenA = service.getIrisSearchCreateList(start, limit, column, dir);
        expect(fifteenA).toBeInstanceOf(Observable);
  }));

  it(`b. getAllIrisSearchCreateList takes zero parameters and also returns an Observable Array of type CreateIrisSearchRequest.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenB = service.getAllIrisSearchCreateList();
    expect(fifteenB).toBeInstanceOf(Observable);
  }));

  it(`c. getEbtsMessageFile takes a string like '${id}' as a parameter and returns an Observable of type Blob.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenC = service.getEbtsMessageFile(id);
    expect(fifteenC).toBeInstanceOf(Observable);
  }));

  it(`d. saveIrisSearchRequest takes an instance of the CreateIrisSearchRequest class like '${JSON.stringify(iris, undefined, 4)} as a parameter and returns an Observable of type CreateIrisSearchRequest.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenD = service.saveIrisSearchRequest(iris);
    expect(fifteenD).toBeInstanceOf(Observable);
  }));

  it(`e. getAllReviewList takes two numbers like ${start} and ${limit} and two strings like '${column}' and '${dir}' as parameters and returns an Observable Array of type ReportData.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenE = service.getAllReviewList(start, limit, column, dir);
    expect(fifteenE).toBeInstanceOf(Observable);
  }));

  it(`f. getReview takes a string like '${reviewId}' as a parameter and returns an Observable of type ReportData.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenF = service.getReview(reviewId);
    expect(fifteenF).toBeInstanceOf(Observable);
  }));

  it(`g. getSearch takes a string like '${reviewId}' as a parameter and returns an Observable of type CreateIrisSearchRequest.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenG = service.getSearch(reviewId);
    expect(fifteenG).toBeInstanceOf(Observable);
  }));

  it(`h. getSearchCount returns an Observable of type number.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenH = service.getSearchCount();
    expect(fifteenH).toBeInstanceOf(Observable);
  }));

  it(`i. getReviewCount returns an Observable of type number.`, inject([HistoryService], (service: HistoryService) => {
    const fifteenI = service.getReviewCount();
    expect(fifteenI).toBeInstanceOf(Observable);
  }));

});
