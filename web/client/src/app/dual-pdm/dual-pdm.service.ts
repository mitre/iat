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
import { DualPDMCompareResponse, DualDeformedImage } from './dual-pdm-compare-response';
import { Annotation } from '../types/annotation';


@Injectable()
export class DualPDMService implements OnDestroy {
    private dualPDMURL = 'api/service/dualpdm/';
    private stopResponsePolling$ = new Subject();
    private stopComparePolling$ = new Subject();
    public dualpdmResponseStatus$: Observable<ServiceStatus>
    public dualPDMCompareStatus$: Observable<ServiceStatus>
    _dualPDMCompareSubscription: Subscription[] = [];
    public dualPdmGroupAnnotation: Annotation = undefined;

    constructor(
        private http: HttpClient
      ) {}

    getDualPdmResponseStatus(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string = '-1', candidateModifiedImageId: string = '-1'): Observable<ServiceStatus> {
        return this.http.get<ServiceStatus>(this.dualPDMURL + 'status/' + probeEbtsImageId + '/' + candidateEbtsImageId + '/' + probeModifiedImageId + '/' + candidateModifiedImageId);
    }
    
    // Polls until both Probe and Candidate images are saved in DualPDMResponseRepository
    startDualPdmResponseStatusPolling(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId?: string, candidateModifiedImageId?: string): void {
        this.dualpdmResponseStatus$ = timer(1, 5000).pipe(
            switchMap(() =>
                this.getDualPdmResponseStatus(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId)
            ),
                retry(4),
                share(),
                takeUntil(this.stopResponsePolling$)
        );
    }

    stopDualPDMResponseStatusPolling(): void{
        this.stopResponsePolling$.next(true);
        this.stopResponsePolling$.complete();
    }

    // Retreives the combination of Probe and Candidate Images
    getDualPDMComparison(probeId: string, candidateId: string, isResubmit: boolean): Observable<DualPDMCompareResponse> {
        return this.http.get<DualPDMCompareResponse>(this.dualPDMURL + 'compare/' + probeId + '/' + candidateId + '/' + isResubmit);
    }
    
    getDualPdmCompareResponseStatus(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId: string = '-1', candidateModifiedImageId: string = '-1'): Observable<ServiceStatus> {
        return this.http.get<ServiceStatus>(this.dualPDMURL + 'compare/status/' + probeEbtsImageId + '/' + candidateEbtsImageId + '/' + probeModifiedImageId + '/' + candidateModifiedImageId);
    }

    // Polls until the combination of Probe and Candidate images are saved in DualPDMCompareResponseRepository
    startDualPDMCompareStatusPolling(probeEbtsImageId: string, candidateEbtsImageId: string, probeModifiedImageId?: string, candidateModifiedImageId?: string): void {
        this.dualPDMCompareStatus$ = timer(1, 5000).pipe(
            switchMap(() =>
                this.getDualPdmCompareResponseStatus(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId)
            ),
            retry(4),
            share(),
            takeUntil(this.stopComparePolling$)
        );
    }

    stopDualPDMCompareStatusPolling(imageId?: string): void {
        if (imageId){
            this._dualPDMCompareSubscription[imageId].unsubscribe();
        } else {
            this.stopComparePolling$.next(true);
            this.stopComparePolling$.complete();
            this._dualPDMCompareSubscription.forEach(subscription => {
                subscription.unsubscribe();
            });
        }
    }

    // Retreives projected image (i.e. Probe ONTO Candidate, Candidate ONTO Probe)
    getDualPDMResponseImage(imageId: string): Observable<DualDeformedImage> {
        return this.http.get<DualDeformedImage>('/api/service/dualpdm/response/' + imageId);
    }

    // Prepares to display byte array through HTML canvas and image elements
    async prepImage(dualPDMResponseImage: DualDeformedImage): Promise<{dualDeformedImage: DualDeformedImage, canvas: HTMLCanvasElement, img: HTMLImageElement}> {
        var paddedImageData = dualPDMResponseImage.imageBytes;
        var imageData;

        const imageArray = new Uint8Array(paddedImageData);
        const blob = new Blob([imageArray], { type: 'image/png' });

        const img = await new Promise<HTMLImageElement>((resolve, reject) => {
            const image = new Image();
            image.onload = () => resolve(image);
            image.onerror = (err) => reject(err);
            image.src = URL.createObjectURL(blob);
        });

        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');

        if (!ctx) {
            throw new Error("Can't get 2D context");
        }
        ctx.drawImage(img, 0, 0);

        // Get raw pixel data (RGBA values)
        imageData = ctx.getImageData(0, 0, img.width, img.height);

        var dualDeformedImage = new DualDeformedImage(imageData);
        return {dualDeformedImage, canvas, img};
    }

  ngOnDestroy() {
    this.stopDualPDMResponseStatusPolling();
  }
}