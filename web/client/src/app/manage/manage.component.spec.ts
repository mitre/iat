/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ManageComponent } from './manage.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { Person } from '../types/person';
import { FormsModule } from '@angular/forms';

describe('XVII. The ManageComponent class:', () => {
  let component: ManageComponent;
  let fixture: ComponentFixture<ManageComponent>;
  const pers = new Person();

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule, MatDialogModule, MatSnackBarModule, MatTableModule],
      declarations: [ManageComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('a. getAllPersons takes zero parameters and returns a void type.', () => {
    expect(component.getAllPersons()).toBeUndefined();
  });

  it('b. createNewPerson takes zero parameters and returns a void type.', () => {
    expect(component.createNewPerson()).toBeUndefined();
  });

  it('c. closePerson takes zero parameters and returns a void type.', () => {
    expect(component.closePerson()).toBeUndefined();
  });

  it('d. savePerson takes zero parameters and returns a void type.', () => {
    expect(component.savePerson()).toBeUndefined();
  });

  it('e. removePerson takes an object instance of the Person class as a parameter and returns a void type.', () => {
    expect(component.removePerson(pers)).toBeUndefined();
  });

});
