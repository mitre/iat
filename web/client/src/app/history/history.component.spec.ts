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
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { HistoryComponent } from './history.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DatePipe } from '@angular/common';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('XIV. The HistoryComponent class:', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;
  const epoch = Math.floor(new Date().getTime() / 1000);
  const DEBUG = false;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ MatSnackBarModule, MatSortModule, MatTableModule, RouterTestingModule, HttpClientTestingModule, BrowserAnimationsModule, NoopAnimationsModule ],
      declarations: [ HistoryComponent ],
      providers: [DatePipe],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('a. getDateString takes an epoch date as a paremeter and returns that date as a string (local time).', () => {
    const dtString = component.getDateString(epoch);
    expect(dtString).toBeInstanceOf(String);

    if (DEBUG) {
        console.log(`UNIT TEST XIV(a): getDateString converted the epoch ${epoch} to local datetime string ${dtString}.`);
    }

  });

  it('b. getDateFromString takes a date as a string (local time) as a paremeter and returns a shortened version of the date.', () => {
    const dtString = component.getDateString(epoch);
    const stringToEpoch = component.getDateFromString(dtString);
    expect(stringToEpoch).toBeInstanceOf(String);

    if (DEBUG) {
        console.log(`UNIT TEST XIV(b): getDateFromString converted the long-form datetime string ${dtString} to this: ${stringToEpoch}.`);
    }
  });
});
