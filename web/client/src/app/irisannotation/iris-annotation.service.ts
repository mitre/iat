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
import { HttpClient } from '@angular/common/http';
import { Observable, timer, Subject, Subscription } from 'rxjs';
import { switchMap, share, retry, takeUntil } from 'rxjs/operators';
import { IrisAnnotationResponse } from './iris-annotation-response';
import { ServiceStatus } from '../types/service-response';

@Injectable({
  providedIn: 'root'
})
export class IrisAnnotationService implements OnDestroy {
  private imageAnnotationUrl = '/api/service/annotation/';
  private imageAnnotationStatusUrl = '/api/service/annotation/status/';
  private annotationSnapshotUrl = '/api/service/annotation/snapshot/'
  private annotationResponseStatus$: Observable<ServiceStatus>;
  public annotationResponse$ = new Subject<IrisAnnotationResponse>();
  private stopAnnotationPolling$ = new Subject();
  private _annotationStatusSubscription: Subscription[] = [];

  constructor(
    private http: HttpClient) {
  }

  getIrisAnnotationsStatus(ebtsImageId: string, modifiedImageId: string): Observable<ServiceStatus> {
    return this.http.get<ServiceStatus>(this.imageAnnotationStatusUrl + ebtsImageId + '/' + modifiedImageId);
  }

  getIrisAnnotationsResponse(imageId: string, isResubmit: boolean): Observable<IrisAnnotationResponse> {
    return this.http.get<IrisAnnotationResponse>(this.imageAnnotationUrl + imageId + '/' + isResubmit);
  }

  startAnnotationStatusPolling(ebtsImageId: string, modifiedImageId: string): void {
    this.annotationResponseStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
      switchMap(() =>
        this.getIrisAnnotationsStatus(ebtsImageId, modifiedImageId)
      ),
      retry(4),
      share(),
      takeUntil(this.stopAnnotationPolling$)
    );
  }

  stopAnnotationStatusPolling(imageId?: string): void {
    if (imageId) {
      this._annotationStatusSubscription[imageId].unsubscribe();
    } else {
      this.stopAnnotationPolling$.next(true);
      this.stopAnnotationPolling$.complete();
      this._annotationStatusSubscription.forEach(subscription => {
        subscription.unsubscribe();
      });
    }
  }

  ngOnDestroy() {
    this.stopAnnotationStatusPolling();
  }

  getAnnotationSnapshot(imageData: string, ebtsImageId: string): Observable<string> {
    return this.http.post<string>(this.annotationSnapshotUrl + ebtsImageId, imageData);
  }

  requestIrisAnnotationResponse(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    let imageId = ebtsImageId;
    if (modifiedImageId != ebtsImageId) {
      imageId = modifiedImageId;
    } else {
      modifiedImageId = '-1';
    }
    
    this.startAnnotationStatusPolling(ebtsImageId, modifiedImageId);
    const serviceStatus = ServiceStatus;

    this._annotationStatusSubscription[imageId] = this.annotationResponseStatus$.subscribe(
      data => {
        switch (data) {
          case serviceStatus.Error: {
            console.log('Iris Annotation ERROR');
            window.alert('Iris Annotation error');
            break;
          }
          case serviceStatus.Received: {
            this.getIrisAnnotationsResponse(imageId, isResubmit).subscribe(
              annotationResponse => {
                if (annotationResponse.status == ServiceStatus.Received) {
                  annotationResponse.isResubmit = isResubmit;
                  this.annotationResponse$.next(annotationResponse);
                  this.stopAnnotationStatusPolling(imageId);
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
            console.log('Iris Annotation: No Status Yet');
            break;
        }
    });
  }
}
