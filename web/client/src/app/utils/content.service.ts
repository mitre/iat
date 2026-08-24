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

@Injectable()
export class ContentService {

  _data = {};
  protected _config: any = {};

  constructor() {
  }

  getContent(key) {
    return this._data[key];
  }

  setContent(key, value) {
    this._data[key] = value;
  }

  get config() {
    return this._config;
  };

  configure(config) {
    this._config = Object.assign(config, this._config);
  }

  getToolConfig(toolName) {
    return this._config.tools.find((x) => x.name == toolName);
  }
}
