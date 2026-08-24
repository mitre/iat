/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Profile } from '../profile/profile';

export class User {
  id = -1;
  username: string;
  password: string;
  roles: string[] = [];
  profile: Profile;
  isNewUser: boolean;

  constructor(id, username, profile) {
    this.id = id;
    this.username = username;
    this.profile = profile;
  }
}
