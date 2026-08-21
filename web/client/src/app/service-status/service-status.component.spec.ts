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
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ServiceStatusComponent } from './service-status.component';

describe('XXVII. The ServiceStatusComponent class:', () => {
  let component: ServiceStatusComponent;
  let fixture: ComponentFixture<ServiceStatusComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatButtonModule, MatMenuModule, MatSnackBarModule, MatTableModule, MatTooltipModule],
      declarations: [ServiceStatusComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('a. setInterval uses a callback function to check the service status of the component. Otherwise it times out.', () => {
    expect(component).toBeTruthy();
  });
});
