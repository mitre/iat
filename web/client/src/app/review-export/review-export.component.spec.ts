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
import { ReviewExportComponent } from './review-export.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ProfileService } from '../profile/profile.service';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';

describe('XXIII. The ReviewExportComponent class:', () => {
    const epoch = 1661187660;
    const selectAll = false;
    const num = '5';
    const size = 15;
    let component: ReviewExportComponent;
    let fixture: ComponentFixture<ReviewExportComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
        imports: [
            BrowserAnimationsModule,
            FormsModule,
            HttpClientTestingModule,
            RouterTestingModule,
            MatCheckboxModule,
            MatDatepickerModule,
            MatDialogModule,
            MatFormFieldModule,
            MatInputModule,
            MatNativeDateModule,
            MatSortModule,
            MatPaginatorModule,
            MatTableModule
        ],
        declarations: [ReviewExportComponent],
        providers: [ProfileService, MatDialogModule, MatPaginatorModule],
        schemas: [CUSTOM_ELEMENTS_SCHEMA]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ReviewExportComponent);
        component = fixture.componentInstance;

        fixture.detectChanges();
    });

    it(`a. getDateStringFromEpoch takes an epoch date as a parameter, like ${epoch}, and returns that epoch as a string.`, () => {
        const res = component.getDateStringFromEpoch(epoch);
        expect(res).toBeInstanceOf(String);
    });

    it(`b. toggleSelectAllText takes boolean as a parameter and returns the changed selectAll status and text (technically a void type).`, () => {
        const res = component.toggleSelectAllText(selectAll);
        expect(res).toBeUndefined();
    });

    it(`c. pad takes a number to pad (e.g., ${num}) and a padding size (e.g., ${size}) as parameters, and then pads that number with ${size-1} leading zeros, i.e., '000000000000005'.`, () => {
        const res = component.pad(num,size);
        expect(res).toBeInstanceOf(String);
    });
});
