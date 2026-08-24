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
import { TshepiiCompareResponse } from './tshepii-compare-response';

// Tshepii service is different than the other services. To get the Tshepii Comparison results, you need 2 images, and each image needs to have a Tshepii Response before it can get the Tshepii Comparison Response. 
// When you upload an image into the probe/candidate canvas, it automatically gets sent to get the individual Tshepii Response.
@Injectable()
export class TshepiiService implements OnDestroy {
  private tshepiiSnapshotUrl = '/api/service/tshepii/snapshot/';
  private tshepiiServiceStatusUrl = '/api/service/tshepii/status/';
  private tshepiiCompareServiceUrl = '/api/service/tshepii/compare/';
  public tshepiiResponseStatus$: Observable<ServiceStatus>;
  public tshepiiCompareStatus$: Observable<ServiceStatus>;
  private stopResponsePolling$ = new Subject();
  private stopComparePolling$ = new Subject();
  _tshepiiCompareSubscription: Subscription[] = [];

  constructor(
    private http: HttpClient
  ) {
  }

  getTshepiiSnapshot(probeData: string, candidateData: string, probeEbtsImageId: string, candidateEbtsImageId: string): Observable<string> {
    let data = [probeData, candidateData]
    return this.http.post<string>(this.tshepiiSnapshotUrl + probeEbtsImageId + "/" + candidateEbtsImageId, data);
  }
  
  getTshepiiResponseStatus(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string, candidateModifiedImageId: string): Observable<ServiceStatus> {
      return this.http.get<ServiceStatus>(this.tshepiiServiceStatusUrl + probeEbtsImageId + '/' + candidateEbtsImageId + '/' + probeModifiedImageId + '/' + candidateModifiedImageId);
  }
  
  // Usage of this function should poll this endpoint until it returns 'error', 'not sent', or 'Received'
  // When we use this on the review page we should keep the tshepii button disabled until we've gotten a response of 'Received'
  // From the review component start calling this function after the user has uploaded their report (or at least two images?)
  startTshepiiResponseStatusPolling(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string, candidateModifiedImageId: string): void {
    this.tshepiiResponseStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
      switchMap(() =>
        this.getTshepiiResponseStatus(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId)
      ),
      retry(4),
      share(),
      takeUntil(this.stopResponsePolling$)
    );
  }

  stopTshepiiResponseStatusPolling(): void {
    this.stopResponsePolling$.next(true);
    this.stopResponsePolling$.complete();
  }

  getTshepiiComparison(probeImageId: string, candidateImageId: string, isResubmit: boolean): Observable<TshepiiCompareResponse> {
    return this.http.get<TshepiiCompareResponse>(this.tshepiiCompareServiceUrl + probeImageId + '/' + candidateImageId + '/' + isResubmit
    );
  }

  getTshepiiCompareStatus(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string, candidateModifiedImageId: string): Observable<ServiceStatus> {
      return this.http.get<ServiceStatus>(this.tshepiiCompareServiceUrl + 'status/' + probeEbtsImageId + '/' + candidateEbtsImageId + '/' + probeModifiedImageId + '/' + candidateModifiedImageId);
  }

  startTshepiiCompareStatusPolling(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string, candidateModifiedImageId: string): void {
    this.tshepiiCompareStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
      switchMap(() =>
        this.getTshepiiCompareStatus(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId)
      ),
      retry(4),
      share(),
      takeUntil(this.stopComparePolling$)
    );
  }

  stopTshepiiCompareStatusPolling(imageId?: string): void {
    if (imageId) {
      this._tshepiiCompareSubscription[imageId].unsubscribe();
    } else {
      this.stopComparePolling$.next(true);
      this.stopComparePolling$.complete();
      this._tshepiiCompareSubscription.forEach(subscription => {
        subscription.unsubscribe();
      });
    }
  }

  ngOnDestroy() {
    this.stopTshepiiResponseStatusPolling();
    this.stopTshepiiCompareStatusPolling();
  }

}
