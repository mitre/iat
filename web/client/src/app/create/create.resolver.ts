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
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';


import { ProfileService } from '../profile/profile.service';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class CreateResolver implements Resolve<Promise<any>> {

  configFileUrl = './iris_create_config.json';

  constructor(private http: HttpClient, private profileService: ProfileService) {
  }

  resolve(route: ActivatedRouteSnapshot,
          state: RouterStateSnapshot): Promise<any> {
    this.profileService.getProfile().subscribe(prfs => {
      // Do nothing. simply a session alive check
    });
    return this.http.get(this.configFileUrl).toPromise().then(r => r);
  }
}
