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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateIrisSearchRequest } from '../types/create-iris-search-request';

@Injectable()
export class EbtsCreateService {

  createServiceUrl = '/api/iris/search';
  ebtsServiceUrl = '/api/iris/search/download/';

  constructor(
    private http: HttpClient
  ) { }

  createEbts( searchRequest: CreateIrisSearchRequest ): Observable<Blob> {
    //also calling this post<ArrayBuffer> doesn't work either for some reason
    return this.http.post(this.createServiceUrl, searchRequest, {
      headers: new HttpHeaders({ 'Content-Type' : 'application/json' }),
      responseType: 'blob' //this is the dumb but required by typescript
    });
  }

  downloadEBTS( searchId: string ): Observable<Blob> {
    return this.http.get(this.ebtsServiceUrl+searchId, {
      responseType: 'blob'
    });
  }

}
