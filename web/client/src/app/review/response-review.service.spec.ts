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
import { ResponseReviewService } from './response-review.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReportData } from '../types/report-data';
import { Observable } from 'rxjs';

describe('XXII. The ResponseReviewService class:', () => {

    const report_data = new ReportData();
    report_data.fileName = 'john_doe_wrapsheet.docx';
    report_data.id = '12345';

    beforeEach(() => {
        TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [ResponseReviewService]
        });
    });


    it(`a. createReport takes an object instance of the ReportData class, like ${JSON.stringify(report_data)}, and returns Observable ReportData.`, inject([ResponseReviewService], (service: ResponseReviewService) => {
        const res = service.createReport(report_data);
        expect(res).toBeInstanceOf(Observable);
    }));
});
