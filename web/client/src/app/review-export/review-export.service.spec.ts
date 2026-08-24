/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { ReviewExportService } from './review-export.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('XXIV. The ReviewExportService class:', () => {
    const ids = ['12345', '67890'];
    beforeEach(() => TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
    }));

    it('a. pullReport takes an array of strings as a parameter and returns an Observable Blob.', () => {
        const service: ReviewExportService = TestBed.inject(ReviewExportService);
        const res = service.pullReport(ids);
        expect(res).toBeInstanceOf(Observable);
    });
});
