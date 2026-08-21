/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LogonInfo {

  loginUrl = '/api/logon/info';

  constructor(private http: HttpClient) {
  }

  getIsSecure(): Observable<boolean> {
    return this.http.get<boolean>(this.loginUrl);
  }
}
