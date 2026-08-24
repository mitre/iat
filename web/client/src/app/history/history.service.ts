/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable, EventEmitter, Output, Directive } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { CreateIrisSearchRequest } from '../types/create-iris-search-request';
import { ReportData } from '../types/report-data';
import { State } from '../types/state';
import { MapUtils } from '../utils/map-utils';

@Directive()
@Injectable({providedIn: 'root'})
export class HistoryService {
  getSearchRequestsUrl = '/api/iris/search/list';
  getEbtsMessageFileUrl = '/api/iris/search/download';
  saveSearchRequestUrl = '/api/iris/search/save';
  getSearchUrl = '/api/iris/search/find/';
  getReviewUrl = '/api/iris/report/find/';
  getReviewListUrl = '/api/iris/report/list';
  saveReviewUrl = '/api/iris/report/save';
  getSearchCountUrl = '/api/iris/search/count';
  getReviewCountUrl = '/api/iris/report/count';
  ebtsURL = '/api/iris/search/ebts';

  // need to notify the navbar of updates
  // This is the child component
  @Output() modifiedNotifications: EventEmitter<boolean> = new EventEmitter();

  notificationsModified(str) {
    this.modifiedNotifications.emit(str);
  }

  _savedIrisSearchRequest: CreateIrisSearchRequest;
  _hasIrisSearchRequest = false;

  get hasIrisSearchRequest() {
    return this._hasIrisSearchRequest;
  }

  set savedIrisSearchRequest(val: CreateIrisSearchRequest) {
    this._savedIrisSearchRequest = val;
    this._hasIrisSearchRequest = true;
  }

  get savedIrisSearchRequest() {
    return this._savedIrisSearchRequest;
  }


  _savedReview: ReportData;
  _hasSavedReview = false;

  get hasSavedReview() {
    return this._hasSavedReview;
  }

  set savedReview(val: ReportData) {
    this._savedReview = val;
    this._hasSavedReview = true;
  }

  get savedReview() {
    return this._savedReview;
  }

  constructor(
    private http: HttpClient
  ) {
  }

  getIrisSearchCreateList(start: number, limit: number, column?: string, dir?: string): Observable<Array<CreateIrisSearchRequest>> {
    let creates;
    if (!dir) {
dir = 'desc';
}
    if (!column) {
      creates = this.http.get<Array<CreateIrisSearchRequest>>(this.getSearchRequestsUrl, {
        params: new HttpParams()
          .set('start', start.toString())
          .set('limit', limit.toString())
          .set('direction', dir)
      });
    } else {
      creates = this.http.get<Array<CreateIrisSearchRequest>>(this.getSearchRequestsUrl, {
        params: new HttpParams()
          .set('start', start.toString())
          .set('limit', limit.toString())
          .set('sort', column)
          .set('direction', dir)
      });
    }

    return creates.pipe(map((data: CreateIrisSearchRequest[]) => data.map(val => MapUtils.deserialize(CreateIrisSearchRequest, val))));
  }

  getAllIrisSearchCreateList(): Observable<Array<CreateIrisSearchRequest>> {
    return this.http.get<Array<CreateIrisSearchRequest>>(this.getSearchRequestsUrl).pipe(
        map(
            (data: CreateIrisSearchRequest[]) => data.map(
                val => MapUtils.deserialize(CreateIrisSearchRequest, val)!
            )
        )
    );
  }

  getEbtsMessageFile(id: string): Observable<Blob> {
    return this.http.get<Blob>(this.getEbtsMessageFileUrl, {
      params: new HttpParams().set('id', id)
    });
  }

  saveIrisSearchRequest(createIrisSearchRequest: CreateIrisSearchRequest): Observable<CreateIrisSearchRequest> {
    return this.http.post<CreateIrisSearchRequest>(this.saveSearchRequestUrl, createIrisSearchRequest);
  }

  getAllReviewList(start?: number, limit?: number, column?: string, dir?: string): Observable<Array<ReportData>> {
    const tempStart = start || 0;
    let reviews;

    if( !column ) {
      column = 'caseInformation.comparisonDate';
    }
    if( !dir ) {
      dir = 'desc';
    }

    if( limit ) {
      reviews = this.http.get<Array<ReportData>>( this.getReviewListUrl, {
        params: new HttpParams()
          .set('start', start!.toString())
          .set('limit', limit.toString())
          .set('direction', dir)
          .set('column', column)
      });

    } else {
      reviews = this.http.get<Array<ReportData>>(this.getReviewListUrl, {
        params: new HttpParams()
        .set('start', start!.toString())
        .set('direction', dir)
        .set('column', column)
      });
    }

    return reviews.pipe(map((data: ReportData[]) => data.map(val => MapUtils.deserialize(ReportData, val))));
  }

  getReview(reviewId): Observable<ReportData> {
    return this.http.get<ReportData>(this.getReviewUrl + reviewId).pipe(map(
      res => MapUtils.deserialize(ReportData, res)!));
  }

  getSearch(searchId): Observable<CreateIrisSearchRequest> {
    return this.http.get<CreateIrisSearchRequest>(this.getSearchUrl + searchId).pipe(map(
      search => MapUtils.deserialize(CreateIrisSearchRequest, search)!));
  }

  saveReview(reportData: ReportData): Observable<ReportData> {
    // Remove pastStates/currState to get rid of cyclic JSON:

    for (const image of reportData.probe.imageList) {
      image.pastStates = new Array<State>();
      image.currState = new State();
    }

    for (const candidate of reportData.candidates) {
      for (const cimage of candidate.imageList) {
        cimage.pastStates = new Array<State>();
        cimage.currState = new State();
      }
    }

    return this.http.post<ReportData>(this.saveReviewUrl, reportData);
  }

  getSearchCount(): Observable<number> {
    return this.http.get<number>(this.getSearchCountUrl);
  }

  getReviewCount(): Observable<number> {
    return this.http.get<number>(this.getReviewCountUrl);
  }

  clearSavedReview() {
    this.savedReview = new ReportData();
    this._hasSavedReview = false;
  }

  clearIrisSearchRequest() {
    this.savedIrisSearchRequest = new CreateIrisSearchRequest();
    this._hasIrisSearchRequest = false;
  }
}
