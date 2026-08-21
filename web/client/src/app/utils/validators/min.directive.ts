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
  selector: '[appMin]',
  providers: [{provide: NG_VALIDATORS, useExisting: MinDirective, multi: true}]
})
export class MinDirective implements Validator {
  @Input('appMin') min: number;

  validate(control: AbstractControl): ValidationErrors | null {
    return this.min ? this.minValidator()(control) : null;
  }

  minValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const invalid = this.min > control.value;
      return invalid ? {invalidMin: {value: control.value}} : null;
    };
  }

  constructor() {
  }

}
