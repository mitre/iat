/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { uuid } from 'short-uuid';

export class AppNotification {
  id: string;
  message: string;
  icon: string;
  action: string;
  userId: number;
  dateCreated: Date;
  isNew: boolean;

  constructor(
    message: string,
    icon: string,
    action: string,
    userId: number
  ) {
    this.id = uuid();
    this.message = message;
    this.icon = icon;
    this.action = action;
    this.userId = userId;
    this.dateCreated = new Date();
    this.isNew = true;
  }
}
