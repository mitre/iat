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
import { AdminComponent } from './admin.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

describe('III. The AdminComponent Class:', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatDialogModule, MatSnackBarModule, MatTableModule],
      declarations: [AdminComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it(`a. showHidePassword takes a boolean value as a parameter (true) and makes the password invisible.`, () => {
    component.passwordVisible = true;
    component.showHidePassword();
    fixture.detectChanges();
    expect(component.passwordVisible).toEqual(false);
  });

  it(`b. showHidePassword takes a boolean value as a parameter (false) and makes the password visible.`, () => {
    component.passwordVisible = false;
    component.showHidePassword();
    fixture.detectChanges();
    expect(component.passwordVisible).toEqual(true);
  });
});
