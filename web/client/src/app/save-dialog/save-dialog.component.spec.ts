/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { SaveDialogComponent } from './save-dialog.component';
import { BsModalService } from 'ngx-bootstrap/modal';

describe('XXVI. The SaveDialogComponent class:', () => {
  let component: SaveDialogComponent;
  let fixture: ComponentFixture<SaveDialogComponent>;
  let modalService: jasmine.SpyObj<BsModalService>;

  beforeEach(waitForAsync(() => {
    modalService = jasmine.createSpyObj<BsModalService>('BsModalService', ['show']);
    modalService.show.and.returnValue({hide: jasmine.createSpy('hide')} as any);

    TestBed.configureTestingModule({
      declarations: [SaveDialogComponent],
      providers: [
        {provide: BsModalService, useValue: modalService}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('a. show takes zero parameters and returns a void type, but prints the modalRef to console.', () => {
    const res = component.show();
    expect(res).toBeUndefined();
    expect(modalService.show).toHaveBeenCalledWith(component.saveModal);
  });

});
