/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Type, Injectable } from '@angular/core';
import * as TOOL_COMPONENTS from './tool-components';

@Injectable()
export class IrisToolRegistry {
  _registry: any = {};

  constructor() {
    Object.keys(TOOL_COMPONENTS).forEach(tool => {
      const t = TOOL_COMPONENTS[tool];
      this._registry[t.id] = {id: t.id, name: t.id, component: t};
    });
  }

  register(name: string, component: Type<any>) {
  }

  getComponent(name) {
    return this._registry[name];
  }

  get iconMap() {
    return Object.keys(this._registry).reduce((map, key) => {
      map[key] = this._registry[key].component.icon;
      return map;
    }, {});
  }
}
