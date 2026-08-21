/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { inject, TestBed } from '@angular/core/testing';
import { MaxDirective } from './max.directive';

describe('XXIX. The MaxDirective Class:', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [MaxDirective]
        });
    });

    it('a. maxValidator takes zero parameters and returns an object instance of the ValidatorFn interface (i.e., a function).', inject([MaxDirective], (service: MaxDirective) => {
        const res = service.maxValidator();
        expect(res).toBeInstanceOf(Function);
    }));
});
