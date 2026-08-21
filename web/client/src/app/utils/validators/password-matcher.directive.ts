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
import {
  UntypedFormGroup,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
  ValidatorFn
 } from '@angular/forms';

@Directive({
  selector: '[appPasswordMatcher]',
  providers: [{provide: NG_VALIDATORS, useExisting: PasswordMatcherDirective, multi: true}]
})

// Input Trial
export class PasswordMatcherDirective implements Validator {
    @Input() password1: string;
    @Input() password2: string;

    validate(formGroup: UntypedFormGroup): ValidationErrors {
        return this.passwordMatcherValidator()(formGroup)!;
    }

    passwordMatcherValidator(): ValidatorFn {
        return (formGroup: UntypedFormGroup): ValidationErrors | null => {
            const newPassword = formGroup.get(this.password1);
            const confirmPassword = formGroup.get(this.password2);

            if (newPassword != null && confirmPassword != null) {
                const misMatched = newPassword.value !== confirmPassword.value;
                if (misMatched) {
                    confirmPassword.setErrors({passwordMismatch: true});
                } else {
                    confirmPassword.setErrors(null);
                }
                return misMatched ? {passwordMismatch: true} : null;
            } else {
                return null;
            }
        };
    }

    constructor() {

    }
}

