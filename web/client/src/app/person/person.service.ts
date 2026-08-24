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
import { Person } from '../types/person';

import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable(
  {providedIn: 'root'}
)
export class PersonService {

  getPersonsUrl = '/api/caseinfo/persons';
  addPersonUrl = '/api/caseinfo';
  removePersonUrl = '/api/caseinfo/delete/person';

  constructor(
    private http: HttpClient
  ) {
  }

  getPersons(): Observable<Person[]> {
    return this.http.get<Person[]>(this.getPersonsUrl);
  }

  addPerson(newPerson: Person): Observable<string> {
    return this.http.post(this.addPersonUrl, newPerson, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: <'text'> 'text'
    });
  }

  removePerson(person: Person): Observable<string> {
    return this.http.post(this.removePersonUrl, person, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: <'text'> 'text'
    });
  }

  createEmptyPerson() {
    return {
      id: '',
      name: '',
      title: '',
      department: '',
      address: '',
      email: '',
      phone: ''
    };
  }
}
