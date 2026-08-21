/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, OnInit } from '@angular/core';
import { ProfileService } from '../profile/profile.service';
import { Profile } from '../profile/profile';

@Component({
  templateUrl: 'home.component.html',
  styles: [`
    .card-grid {
      display: grid;
      grid-auto-flow: column;
      grid-gap: 15px;
    }

    .fab-icon {
      margin-top: -2px;
    }

    .title-link {
      font-size: 27px;
      cursor: pointer;
    }


  `]
})

export class HomeComponent implements OnInit {

  prefs: Profile;
  privs: any = {};


  constructor(
    private profileService: ProfileService
  ) {
    this.profileService.getUserPrivileges().subscribe(privs => {
      this.privs = privs;
    });
  }

  ngOnInit() {
    this.profileService.getProfile().subscribe(prfs => {
      this.prefs = prfs;
      this.loadUserData();
    });
  }

  loadUserData() {
   
  }

  handleTableChanges() {
    this.loadUserData();
  }

}
