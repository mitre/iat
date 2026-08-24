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
import { User } from '../user/user';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserRole } from './user-role';
import { Profile } from '../profile/profile';

@Injectable(
  {providedIn: 'root'}
)
export class AdminService {

  addUpdateUserUrl = '/api/admin/user/add/update/roles';
  removeUserUrl = '/api/admin/user/delete';
  getUsersUrl = '/api/admin/users';
  updateUserPasswordUrl = '/api/admin/user/password';
  checkPasswordUrl = '/api/admin/check/password';
  getRolesUrl = '/api/admin/roles';

  constructor(private http: HttpClient) {
  }

  createEmptyUser() {
    return {
      id: -1,
      username: '',
      roles: [],
      password: '',
      profile: new Profile(),
      isNewUser: true
    };
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.getUsersUrl);
  }

  addUpdateUser(user: User, isNewUser: boolean): Observable<string> {
    user.isNewUser = isNewUser;
    return this.http.post(this.addUpdateUserUrl, user, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'text'
    });
  }

  removeUser(user: User): Observable<string> {
    return this.http.post(this.removeUserUrl, user, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'text'
    });
  }

  checkPassword(userName: string, oldPassword: string) {
    return this.http.post(this.checkPasswordUrl, {userName, oldPassword}, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'text'
    });
  }

  updatePassword(userName: string, oldPassword: string, newPassword: string) {
    return this.http.post(this.updateUserPasswordUrl, {userName, oldPassword, newPassword}, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'text'
    });
  }

  getAllRoles(): Observable<UserRole[]> {
    return this.http.get<UserRole[]>(this.getRolesUrl);
  }
}
