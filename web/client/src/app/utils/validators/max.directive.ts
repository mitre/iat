/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Directive, Input } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn } from '@angular/forms';

@Directive({
  selector: '[appMax]',
  providers: [{provide: NG_VALIDATORS, useExisting: MaxDirective, multi: true}]
})
export class MaxDirective implements Validator {
  @Input('appMax') max: number;

  validate(control: AbstractControl): ValidationErrors | null {
    return this.max ? this.maxValidator()(control) : null;
  }

  maxValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const invalid = this.max < control.value;
      return invalid ? {invalidMax: {value: control.value}} : null;
    };
  }

  constructor() {
  }
}
