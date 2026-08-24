/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, Injectable, TemplateRef, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-save-dialog',
  templateUrl: './save-dialog.component.html',
  styleUrls: ['./save-dialog.component.css']
})
@Injectable({
  providedIn: 'root'
})
export class SaveDialogComponent {
  closed$: Subject<boolean> = new Subject<boolean>();
  save = false;
  continue = false;
  modalRef: BsModalRef;
  @ViewChild('appSaveModal') saveModal: TemplateRef<any>;

  constructor(private modalService: BsModalService) {
  }

  show() {
    this.modalRef = this.modalService.show(this.saveModal);
  }

  hide() {
    this.save = false;
    this.continue = false;
    this.closed$.next(true);
    this.modalRef.hide();
  }

  saveClicked() {
    this.save = true;
    this.continue = true;
    this.modalRef.hide();
    this.closed$.next(true);
  }

  continueClicked() {
    this.save = false;
    this.continue = true;
    this.closed$.next(true);
  }
}
