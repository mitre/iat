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


import { ContentService } from '../utils/content.service';
import { EyeLabel } from '../types/eye-label';
import { AciiService } from '../acii/acii.service';
import { Subscription } from 'rxjs';
import { ContactType } from '../types/contact-type';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ServiceStatusNameType } from '../types/service-status';
import { ServiceStatus } from '../types/service-response';
import { BiqtContactService } from '../biqt-contact/biqt-contact.service';


@Injectable()
export class CreateContentService extends ContentService {
  protected _config: object = {
    imageURL: '/api/image/',
    url: '/api/iris/create',
    acceptedFiles: 'image/png, image/jp2, image/jp2l, image/bmp, image/jpeg, image/jpg, image/tiff, .tiff',
    maxFiles: 1,
    headers: {'X-XSRF-TOKEN': document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1')}
  };
  configFileUrl = './iris_create_config.json';
  colors: any[] = [];
  irisImageQuality = -1;
  eyeLabel: EyeLabel = EyeLabel[''];
  contactType: ContactType = ContactType[''];

  initIrisImageQuality = -1;
  initExtension = 0;
  initNumCandidate = 50;
  initDai = 'TempDAI12';
  initOri = 'TempORI12';
  initAtt = 'Attention';
  initEyeLabel: EyeLabel = EyeLabel[''];
  initContactType: ContactType = ContactType[''];
  initSourceAgency = 'SRCAGENCY';
  initRoe = 0;
  initRou = 0;


  _biqtResponseSubscription: Subscription;
  _contactResponseSubscription: Subscription;
  _aciiResponseSubscription: Subscription;

  constructor(
    private aciiService: AciiService,
    private biqtContactService: BiqtContactService,
    private matSnackBar: MatSnackBar,
  ) {
    super();
  }

  getAutoEyeLabel(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    this.aciiService.requestAciiResponse(ebtsImageId, isResubmit, modifiedImageId);
    const pendingTimer = this.setPendingTimer(isResubmit, ServiceStatusNameType.acii);
    this._aciiResponseSubscription = this.aciiService.aciiResponse$.subscribe(
      aciiResponse => {
        this.clearPendingTimer(aciiResponse.status, pendingTimer);
        const orientation = aciiResponse.horizontal;
        this.setAutoEyeLabel(orientation);
        this.showSnackBar(aciiResponse.isResubmit, false, ServiceStatusNameType.acii);
        this._aciiResponseSubscription.unsubscribe();
      }
    );
  }

  setAutoEyeLabel(orientation): void {
    let response;
    switch (orientation) {
      case 'Undefined':
      case '0':
        response = EyeLabel.Undefined;
        break;
      case 'Right':
      case '1':
        response = EyeLabel.Right;
        break;
      case 'Left':
      case '2':
        response = EyeLabel.Left;
        break;
      case '':
      case '3':
      default:
        response = EyeLabel[''];
        break;
    }
    this.eyeLabel = response;
  }

  getIrisImageQualityAndContact(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    this.biqtContactService.requestBiqtContactResponse(ebtsImageId, isResubmit, modifiedImageId);
    const biqtPendingTimer = this.setPendingTimer(isResubmit, ServiceStatusNameType.biqt);
    const contactPendingTimer = this.setPendingTimer(isResubmit, ServiceStatusNameType.contactdetection);
    this._biqtResponseSubscription = this.biqtContactService.biqtResponse$.subscribe(
      response => {
        this.clearPendingTimer(response.status, biqtPendingTimer);
        if (response.quality >= 0) {
            this.irisImageQuality = response.quality;
          this.showSnackBar(response.isResubmit, false, ServiceStatusNameType.biqt);
        }

        this._biqtResponseSubscription.unsubscribe();
      });
    
    this._contactResponseSubscription = this.biqtContactService.contactResponse$.subscribe(
      response => {
        this.clearPendingTimer(response.status, contactPendingTimer);
        this.contactType = this.getContactType(response.detectionCode)
        this.showSnackBar(response.isResubmit, false, ServiceStatusNameType.contactdetection);
        this._contactResponseSubscription.unsubscribe();
        
      }
    )
  }

  setPendingTimer(isResubmit: boolean, serviceName: ServiceStatusNameType): any {
    let timeInterval = 8000;
    // Needs to set each service to a different timer so they don't overlap
    switch(serviceName) {
      case ServiceStatusNameType.acii: {
        break;
      }
      case ServiceStatusNameType.biqt: {
        timeInterval += 500;
        break;
      }
      case ServiceStatusNameType.contactdetection: {
        timeInterval += 800;
        break;
      }
      case ServiceStatusNameType.tshepii: {
        timeInterval += 1100;
        break;
      }
    }

    const timer = setInterval(() => this.showSnackBar(isResubmit, true, serviceName), timeInterval);
    return timer;
  }

  clearPendingTimer(responseStatus: ServiceStatus, pendingTimer: number): void {
    if (responseStatus == ServiceStatus.Received) {
      clearInterval(pendingTimer);
    }
  }

  showSnackBar(isResubmit: boolean, isPendingStill: boolean, serviceName: ServiceStatusNameType, customMessage?: string) {
    if (!!customMessage) {
      this.matSnackBar.open(customMessage, 'Dismiss');
    }
    if (isResubmit && !isPendingStill) {
      this.matSnackBar.open('Resubmitting image finished for ' + serviceName, 'Dismiss');
    }
  }

  getContactType(contactType: string): ContactType {
    switch (contactType) {
      case '0': 
        return ContactType.None;
      case '1':
        return ContactType.Cosmetic;
      case '2':
        return  ContactType.Clear;
      case '3':
      default:
        return ContactType[''];
    }
  }
}
