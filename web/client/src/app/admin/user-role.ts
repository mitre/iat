/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Privilege } from './privilege';

export class UserRole {
  id: number;
  name: string;
  description: string;
  privileges: Privilege[] = [];

  constructor(id, name, description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}
