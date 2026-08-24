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
import { Profile } from './profile';

import { Observable, Subject } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';

@Injectable(
  {providedIn: 'root'}
)
export class ProfileService {

  public currentProfile$: Subject<Profile> = new Subject<Profile>();
  public username$: Subject<string> = new Subject<string>();

  baseUrl = '/api/admin/';
  userUrl = '/api/admin/user';
  profilesUrl = '/api/admin/user/profile';
  private privUrl = '/api/admin/user/privileges';
  private usersUrl = '/api/admin/userList';


  private currentProfile: Profile = this.createEmptyProfile();
  private _username: string;
  private _privileges: object;

  constructor(private http: HttpClient) {

    this.http.get(this.userUrl, {responseType: <'text'> 'text'}).subscribe(response => {
      this._username = response;
      this.username$.next(this._username);
    });

    this.getUserPrivileges().subscribe(privs => {
      this._privileges = privs;
    });

    this.getProfile().subscribe((profile) => {
      if (!profile.userSetting) {
        const prof = this.createEmptyProfile();
        profile.userSetting = prof.userSetting;
      }
      this.setCurrentProfile(profile);
    });
  }

  get username() {
    return this._username;
  }

  set username(s: string) {
  }

  get currentPrivileges() {
    return this._privileges;
  }

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(this.profilesUrl);
  }

  updateProfile(updatedProfile: Profile) {
    this.http.post(this.profilesUrl, updatedProfile, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: <'text'> 'text',
      observe: 'response'
    })
      .subscribe(response => {
        this.getProfile()
          .subscribe((profile) => {
            this.setCurrentProfile(profile);
          });
      });
  }

  createEmptyProfile() {
    return new Profile();
  }

  getCurrentProfile() {
    return Object.assign({}, this.currentProfile);
  }

  setCurrentProfile(profile: Profile) {
    this.currentProfile = profile;
    this.currentProfile$.next(profile);
  }

  getUserPrivileges(): Observable<object> {
    return this.http.get<object>(this.privUrl + '')
      .pipe(map((response) => {
        //convert to object
        const obj = {};
        Object.keys(response).forEach(idx => {
          obj[response[idx]] = response[idx];
        });

        return obj;
      }));
  }

  getUserList(): Observable<object> {
    return this.http.get<object>(this.usersUrl);
  }

}
