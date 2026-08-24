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
import { Observable } from 'rxjs';
import { Person } from '../types/person';
import { PersonService } from './person.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('XX. The PersonService class: ', () => {
    const pers = new Person();

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [PersonService]
        });
    });

    it('a. getPersons takes zero parameters and returns an Obsevable Array of Persons.', inject([PersonService], (service: PersonService) => {
        const res = service.getPersons();
        expect(res).toBeInstanceOf(Observable);
    }));

    it(`b. addPerson takes an object instance of the Person class like ${JSON.stringify(pers)} and returns an Obsevable string.`, inject([PersonService], (service: PersonService) => {
        const res = service.addPerson(pers);
        expect(res).toBeInstanceOf(Observable);
    }));

    it(`c. removePerson takes an object instance of the Person class like ${JSON.stringify(pers)} and returns an Obsevable string.`, inject([PersonService], (service: PersonService) => {
        const res = service.removePerson(pers);
        expect(res).toBeInstanceOf(Observable);
    }));

    it(`d. createEmptyPerson takes zero parameters and returns an empty object.`, inject([PersonService], (service: PersonService) => {
        const res = service.createEmptyPerson();
        expect(res).toBeInstanceOf(Object);
    }));
});
