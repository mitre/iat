/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Annotation } from './annotation';
import { JsonProperty } from '../utils/decorators/json-property';
import { ComponentRef } from '@angular/core';

export enum StateType {
  ADD,
  REMOVE,
  BULK_REMOVE,
  RESET,
  CLEAR,
  GROUP_ONLY,
  GROUP_ADD
}

export class State {
  @JsonProperty({clazz: Annotation})
  annotation: Annotation;
  data: any; // This feels like a hack
  type: StateType;
  compRef: ComponentRef<any>;

  constructor();
  constructor(annotation: Annotation, type: StateType);
  constructor(annotation?: Annotation, type?: StateType) {
    this.annotation = annotation;
    this.data = undefined;
    this.type = type;
  }
}
