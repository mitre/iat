/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';

interface LetContext<T> {
  myLet: T;
}

@Directive({
  selector: '[myLet]'
})

export class LetDirective<T> {
  private _context: LetContext<T> = {myLet: null};

  constructor(_viewContainer: ViewContainerRef,
              _templateRef: TemplateRef<LetContext<T>>) {
    _viewContainer.createEmbeddedView(_templateRef, this._context);
  }

  @Input()
  set myLet(value: T) {
    this._context.myLet = value;
  }
}
