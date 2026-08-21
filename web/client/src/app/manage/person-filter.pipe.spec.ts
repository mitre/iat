/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { TestBed, inject } from '@angular/core/testing';
import { PersonFilterPipe } from './person-filter.pipe';
import { Person } from '../types/person';

describe('XVIII. The PersonFilterPipe component class: ', () => {
    let pers1: Person = new Person();
    pers1 = {id: '12345', name:'John', title:'Archduke', department:'Sporting Goods', address: '123 Street St, A City, A State', email:'john@iwp.org', phone:'555-5555'};

    let pers2: Person = new Person();
    pers2 = {id: '12345', name:'Jane', title:'King', department:'Housewares', address: '123 Street St, A City, A State', email:'jane@iwp.org', phone:'555-5556'};

    const args = 'John';
    const DEBUG = false;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [PersonFilterPipe ]
            });
    });

  it(`a. transform takes an Array of the Person class like ${[JSON.stringify(pers1), JSON.stringify(pers2)]} and any args (i.e., search terms) like '${args}' provided as parameters, and returns the matched person (please see console statement).`, inject([PersonFilterPipe], (service: PersonFilterPipe) => {
    const eighteenA = service.transform([pers1,pers2], args);
    if (DEBUG) {
        console.log('UNIT TEST XVIII(a): transform returns', eighteenA);
    }

    expect(eighteenA).toBeInstanceOf(Array);
  }));
});
