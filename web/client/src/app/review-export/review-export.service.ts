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
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReviewExportService {

  getCsvUrl = '/api/iris/report/csv/download';

  constructor(
    private http: HttpClient
  ) {
  }

  pullReport(ids: string[]): Observable<Blob> {
    return this.http.get(this.getCsvUrl, {
      params: new HttpParams().set('id', ids.join(',')),
      responseType: 'blob'
    });
  }
}
