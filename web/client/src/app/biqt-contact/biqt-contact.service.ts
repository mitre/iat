/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable, OnDestroy } from '@angular/core';
import { Observable, timer, Subject, Subscription } from 'rxjs';
import { switchMap, share, retry, takeUntil } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { ServiceStatus } from '../types/service-response';
import { BiqtResponse } from './biqtResponse';
import { ContactClassifierResponse } from './contact-classifier-response';

@Injectable()
export class BiqtContactService implements OnDestroy {
  public contactResponse$ = new Subject<ContactClassifierResponse>();
  public biqtResponse$ = new Subject<BiqtResponse>();
  private biqtContactRequestStatus$: Observable<ServiceStatus[]>;
  private stopBiqtPolling$ = new Subject();
  private stopContactPolling$ = new Subject();
  private stopBiqtContactPolling$ = new Subject();
  private _biqtStatusSubscription: Subscription[] = [];
  private _contactStatusSubscription: Subscription[] = [];
  private biqtContactServiceUrl = '/api/service/biqtContact/';
  private biqtContactServiceStatusUrl = '/api/service/biqtContact/status/';
  private biqtContactSnapshotUrl = '/api/service/biqtContact/snapshot/';
  
  constructor(
    private http: HttpClient
  ) {
  }

  getBiqtContactStatus(ebtsImageId: string, modifiedImageId: string): Observable<ServiceStatus[]> {
    return this.http.get<ServiceStatus[]>(this.biqtContactServiceStatusUrl + ebtsImageId + '/' + modifiedImageId);
  }

  getBiqtResponse(imageId: string, isResubmit: boolean): Observable<BiqtResponse> {
    return this.http.get<BiqtResponse>(this.biqtContactServiceUrl + 'biqt/' + imageId + '/' + isResubmit);
  }

  getContactResponse(imageId: string, isResubmit: boolean): Observable<ContactClassifierResponse> {
    return this.http.get<ContactClassifierResponse>(this.biqtContactServiceUrl + 'contact/' + imageId + '/' + isResubmit);
  }

  getBiqtContactSnapshot(imageData: string, ebtsImageId: string): Observable<string> {
    return this.http.post<string>(this.biqtContactSnapshotUrl + ebtsImageId, imageData);
  }

  startBiqtContactPolling(ebtsImageId: string, modifiedImageId: string): void {
    this.biqtContactRequestStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
      switchMap(() =>
        this.getBiqtContactStatus(ebtsImageId, modifiedImageId)
      ),
      retry(4),
      share(),
      takeUntil(this.stopBiqtContactPolling$)
    );
  }

  stopBiqtStatusPolling(imageId?: string): void {
    this.stopBiqtPolling$.next(true);
    this.stopBiqtPolling$.complete();
    if (imageId) {
      this._biqtStatusSubscription[imageId].unsubscribe();
    } else {
      this._biqtStatusSubscription.forEach(subscription => {
        subscription.unsubscribe();
      });
    }
  }

  stopContactStatusPolling(imageId?: string): void {
    this.stopContactPolling$.next(true);
    this.stopContactPolling$.complete();
    if (imageId) {
      this._contactStatusSubscription[imageId].unsubscribe();
    } else {
      this._contactStatusSubscription.forEach(subscription => {
        subscription.unsubscribe();
      });
    }
  }

  requestBiqtContactResponse(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    let imageId = ebtsImageId;
    if (!!modifiedImageId) {
      imageId = modifiedImageId;
    } else {
      modifiedImageId = '-1';
    }
    this.startBiqtContactPolling(ebtsImageId, modifiedImageId);
    let biqtIsReceived = this.requestBiqtResponse(imageId, isResubmit);
    let contactIsReceived = this.requestContactResponse(imageId, isResubmit);
    if (biqtIsReceived && contactIsReceived) {
      this.stopBiqtContactPolling$.next(true);
      this.stopBiqtContactPolling$.complete();
    }
  }

  requestBiqtResponse(imageId: string, isResubmit: boolean): boolean {
    let isReceived = false;

    this._biqtStatusSubscription[imageId] = this.biqtContactRequestStatus$.subscribe(
      data => {
        let status = data[0];
        switch (status) {
          case ServiceStatus.Error: {
            window.alert('BIQT error');
            break;
          }
          case ServiceStatus.Received: {
            this.getBiqtResponse(imageId, isResubmit).subscribe(
              biqtResponse => {
                if (biqtResponse.status == ServiceStatus.Received) {
                  biqtResponse.isResubmit = isResubmit;
                  this.biqtResponse$.next(biqtResponse);
                  this.stopBiqtStatusPolling(imageId);
                  isReceived = true;
                }
              });
            break;
          }
          case ServiceStatus.Pending: {
            break;
          }
          case ServiceStatus.NotSent: {
            break;
          }
          default:
            console.log('BIQT: No Status Yet');
            break;
        }
    });
    return isReceived;
  }

  requestContactResponse(imageId: string, isResubmit: boolean): boolean {
    let isReceived = false;
    this._contactStatusSubscription[imageId] = this.biqtContactRequestStatus$.subscribe(
      data => {
        let status = data[1];
        switch (status) {
          case ServiceStatus.Error: {
            window.alert('Contact error');
            break;
          }
          case ServiceStatus.Received: {
            this.getContactResponse(imageId, isResubmit).subscribe(
              contactResponse => {
                if (contactResponse.status == ServiceStatus.Received) {
                  contactResponse.isResubmit = isResubmit;
                  this.contactResponse$.next(contactResponse);
                  this.stopContactStatusPolling(imageId);
                  isReceived = true;
                }
              });
            break;
          }
          case ServiceStatus.Pending: {
            break;
          }
          case ServiceStatus.NotSent: {
            break;
          }
          default:
            console.log('Contact: No Status Yet');
            break;
        }
    });
    return isReceived;
  }

  ngOnDestroy() {
    this.stopBiqtStatusPolling();
    this.stopContactStatusPolling();
  }


}
