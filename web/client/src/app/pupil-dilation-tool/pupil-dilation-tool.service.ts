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
import { DeformedImageBytes, DeformedImageData } from './pupil-dilation-response';

@Injectable()
export class PupilDilationToolService implements OnDestroy {
    private pdmResponseUrl = '/api/service/pdm/';
    public pdmResponseStatus$: Observable<ServiceStatus>;
    private stopResponsePolling$ = new Subject();
    public _pdmStatusSubscription: Subscription[] = [];

    constructor(
        private http: HttpClient,
    ) {
    }

    //   Pdm Response Methods:
    getPdmStatus(ebtsImageId: string, modifiedImageId: string): Observable<ServiceStatus> {
        return this.http.get<ServiceStatus>(this.pdmResponseUrl + 'status/' + ebtsImageId + '/' + modifiedImageId);
    }

    startPdmResponsePolling(ebtsImageId: string, modifiedImageId: string): void {
        this.pdmResponseStatus$ = timer(1, 5000).pipe( //5000 = 5 second polling interval
            switchMap(() =>
                this.getPdmStatus(ebtsImageId, modifiedImageId)
            ),
            retry(4),
            share(),
            takeUntil(this.stopResponsePolling$)
        );
    }

    getPdmSnapshot(imageData: string, ebtsImageId: string): Observable<string> {
        return this.http.post<string>(this.pdmResponseUrl + 'snapshot/' + ebtsImageId, imageData);
    }

    processPdm(imageData): Observable<string> {
        return this.http.post<string>(this.pdmResponseUrl + 'process', imageData);
    }

    stopPdmResponsePolling(imageId?: string): void {
        if (imageId) {
            this._pdmStatusSubscription[imageId].unsubscribe();
        } else {
            this.stopResponsePolling$.next(true);
            this.stopResponsePolling$.complete();
            this._pdmStatusSubscription.forEach(subscription => {
                subscription.unsubscribe();
            })
        }
    }

    getPdmResponse(imageId: string, alphaScore: string): Observable<DeformedImageBytes> {
        var deformedId = imageId + "." + alphaScore;
        return this.http.get<DeformedImageBytes>(this.pdmResponseUrl + 'response/' + deformedId
        );
    }

    async convertImageBytesToImage(deformedImageBytes: DeformedImageBytes): Promise<{deformedImage: DeformedImageData, canvas: HTMLCanvasElement, img: HTMLImageElement}> {
        var paddedImageData = deformedImageBytes.imageData;
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

        var deformedImage = new DeformedImageData(imageData, deformedImageBytes.score, deformedImageBytes.imageType, deformedImageBytes.width, deformedImageBytes.height);
        return {deformedImage, canvas, img};
    }

    ngOnDestroy() {
        this.stopPdmResponsePolling();
    }


}