/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable } from '@angular/core';
import { ReportData } from '../types/report-data';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { State } from '../types/state';

@Injectable()
export class ResponseReviewService {

  reportServiceUrl = '/api/iris/report';

  constructor(
    private http: HttpClient
  ) {
  }

  createReport(reportData: ReportData): Observable<ReportData> {
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

    return this.http.post<ReportData>(this.reportServiceUrl, reportData);
  }

}
