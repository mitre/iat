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
import { Router } from '@angular/router';
import { ProfileService } from './profile/profile.service';
import { Profile } from './profile/profile';
import { ServiceStatusService } from './service-status/service-status.service';
import { NotificationsService } from './notifications/notifications.service';
import { VersionInfo } from './types/versionInfo';
import { MatDialog } from '@angular/material/dialog';
import { setTheme } from 'ngx-bootstrap/utils';
import { LogonInfo } from './types/logonInfo';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {

  currentProfileName = '';
  serviceItemColumns = ['service', 'status'];
  title = 'Iris Workstation';
  public username: string;
  numNotifications = 0;
  privs: any = {};
  data: VersionInfo = new VersionInfo();
  isSecure: boolean;

  constructor(
    public router: Router,
    public profileService: ProfileService,
    public serviceStatusService: ServiceStatusService,
    public notificationService: NotificationsService,
    public logonInfo: LogonInfo,
    public dialog: MatDialog,
  ) {
    // Manually set the bootstrap theme to bs4
    setTheme('bs4');
    this.profileService.username$
      .subscribe(name => {
        this.username = name;
      });

    this.logonInfo.getIsSecure().subscribe(resp => {
      this.isSecure = resp;
    })

    this.profileService.currentProfile$.subscribe((profile) => {
      this.currentProfileName = profile.name;
    });

    this.profileService.getProfile()
      .subscribe(prfs => {
        this.currentProfileName = prfs.name;
      });

    this.profileService.getUserPrivileges().subscribe(privs => {
      this.privs = privs;
    });
  }

  ngOnInit() {
    this.serviceStatusService.updateServiceVersions();
  }

  profileSelected(profile: Profile) {
    this.profileService.setCurrentProfile(profile);
  }

}
