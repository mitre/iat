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
import { AciiResponse } from './aciiResponse';


@Injectable()
export class AciiService implements OnDestroy {
  private aciiRequestStatus$!: Observable<ServiceStatus>;
  public aciiResponse$ = new Subject<AciiResponse>();
  private stopAciiPolling$ = new Subject();
  private _aciiStatusSubscription: Subscription[] = [];

  aciiServiceUrl = '/api/service/acii/';
  aciiServiceStatusUrl = '/api/service/acii/status/';
  aciiSnapshotUrl = '/api/service/acii/snapshot/'

  constructor(private http: HttpClient) {
  }

  getAciiStatus(ebtsImageId: string, modifiedImageId: string): Observable<ServiceStatus> {
    return this.http.get<ServiceStatus>(this.aciiServiceStatusUrl + ebtsImageId + '/' + modifiedImageId);
  }

  getAciiResponse(imageId: string, isResubmit: boolean): Observable<AciiResponse> {
      return this.http.get<AciiResponse>(this.aciiServiceUrl + imageId + '/' + isResubmit);
  }

  getAciiSnapshot(imageData: string, ebtsImageId: string): Observable<string> {
    return this.http.post<string>(this.aciiSnapshotUrl + ebtsImageId, imageData);
  }

  startAciiPolling(ebtsImageId: string, modifiedImageId: string): void {
    this.aciiRequestStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
      switchMap(() =>
        this.getAciiStatus(ebtsImageId, modifiedImageId)
      ),
      retry(4),
      share(),
      takeUntil(this.stopAciiPolling$)
    );
  }


  stopAciiStatusPolling(imageId?: string): void {
    if (imageId) {
      this._aciiStatusSubscription[Number(imageId)].unsubscribe();
    } else {
      this.stopAciiPolling$.next(true);
      this.stopAciiPolling$.complete();
      this._aciiStatusSubscription.forEach(subscription => {
        subscription.unsubscribe();
      });
    }
  }

  requestAciiResponse(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    let imageId = ebtsImageId;
    if (!!modifiedImageId) {
      imageId = modifiedImageId;
    } else {
      modifiedImageId = '-1';
    }
    this.startAciiPolling(ebtsImageId, modifiedImageId);
    const serviceStatus = ServiceStatus;
    let statusCounter = 0;
    this._aciiStatusSubscription[Number(imageId)] = this.aciiRequestStatus$.subscribe(
      data => {
        switch (data) {
          case serviceStatus.Error: {
            console.log('AciiStatus ERROR');
            window.alert('ACII error');
            break;
          }
          case serviceStatus.Received: {
            this.getAciiResponse(imageId, isResubmit).subscribe(
              aciiResponse => {
                if (aciiResponse.status == ServiceStatus.Received) {
                  aciiResponse.isResubmit = isResubmit;
                  this.aciiResponse$.next(aciiResponse);
                  this.stopAciiStatusPolling(imageId);
                }
              });
            break;
          }
          case serviceStatus.Pending: {
            break;
          }
          case serviceStatus.NotSent: {
            break;
          }
          default:
            console.log('ACII: No Status Yet');
            break;
        }
      });
  }

  ngOnDestroy() {
    this.stopAciiStatusPolling();
  }

}
