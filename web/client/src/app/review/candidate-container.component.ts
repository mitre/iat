/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { 
  Component, Directive, ViewContainerRef, Output, EventEmitter,
  ComponentFactoryResolver, ViewChild, Input, OnChanges, SimpleChanges
 } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { ReviewContentService } from './review.service';
import { IrisDisplayComponent } from '../iris-display/iris-display.component';
import { ContentService } from '../utils/content.service';
import { EbtsImageData } from '../types/image-data';
import { Annotation } from '../types/annotation';
import { State } from '../types/state';


@Directive({
  selector: '[appViewCandidate]'
})
export class ViewCandidateDirective {
  constructor(public viewContainerRef: ViewContainerRef) {
  }
}

@Component({
  selector: 'app-candidate-container',
  template: `
    <ng-template appViewCandidate></ng-template>`
})
export class CandidateContainerComponent implements OnChanges {

  @ViewChild(ViewCandidateDirective, {static: true}) viewCandidate: ViewCandidateDirective;
  @Input() uuid: string;
  @Input() iris: HTMLImageElement;
  @Input() currentImageData: EbtsImageData;
  @Input() unrollImagePath = '';
  @Input() unrollRingData: object;
  @Input() displayDrawColor: string;
  @Input() displayFillColor: string;
  @Input('annotations') originalAnnotations: Annotation[];
  @Input() showOriginal: BehaviorSubject<boolean>;
  @Input() showUnrolled: BehaviorSubject<boolean>;
  @Input() isVertical: BehaviorSubject<boolean>;
  @Input() saved: Subject<any>;
  @Input() pastStates: State[];
  @Input() currState: State;
  @Input() isReview = false;
  @Input() sourceName: string;
  @Input() loadedCandidateImage: any;
  @Input() loadedProbeImage: any;
  @Output('irismousemove') mouseMove: EventEmitter<any> = new EventEmitter<any>(true);
  @Output() resetEmitter = new EventEmitter<string>(true);
  @Output() cacheEmitter = new EventEmitter<string>(true);
  @Output('updateUnroll') updateUnrollEmitter: EventEmitter<string> = new EventEmitter<string>(true);

  private _lastUuid = '';
  private reviewSvc: ReviewContentService;

  constructor(private contentSvc: ContentService,
              private componentFactoryResolver: ComponentFactoryResolver) {
    this.reviewSvc = (<ReviewContentService>this.contentSvc);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.uuid != undefined) {
      const uuid = changes.uuid.currentValue;

      if (uuid !== this._lastUuid) {
        this.onCandidateChange(uuid);
        this.cacheEmitter.emit(uuid);
        this._lastUuid = uuid;
      }
    }
  }

  updateUnroll() {
    this.updateUnrollEmitter.emit(this.uuid);
  }

  resetCurrentDisplay() {
    // remove the current View instance of the corresponding image/uuid pair
    const vcf = this.viewCandidate.viewContainerRef;
    vcf.remove();

    this.createIrisDisplayComponent(this._lastUuid, true);
    this.resetEmitter.emit(this.uuid);
  }

  clearCurrentDisplayAnnotations() {// remove the current View instance of the corresponding image/uuid pair
    const vcf = this.viewCandidate.viewContainerRef;
    vcf.remove();

    this.createIrisDisplayComponent(this._lastUuid, false);
  }

  createIrisDisplayComponent(uuid, loadOriginalAnnotations: boolean) {
    const vcf = this.viewCandidate.viewContainerRef;

    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(IrisDisplayComponent);

    const componentRef = vcf.createComponent(componentFactory);

    // Bind the iris image to the [iris] @Input of the IrisDisplayComponent
    (<IrisDisplayComponent>componentRef.instance).iris = this.iris;
    (<IrisDisplayComponent>componentRef.instance).tshepiiHidden = false;
    (<IrisDisplayComponent>componentRef.instance).pdmModifiedId = this.currentImageData.ebtsImageId;
    (<IrisDisplayComponent>componentRef.instance).currentAutomatedAnnotationId = this.currentImageData.ebtsImageId;
    (<IrisDisplayComponent>componentRef.instance).sourceName = this.sourceName;


    if (loadOriginalAnnotations) {
      (<IrisDisplayComponent>componentRef.instance).originalAnnotations = this.originalAnnotations;
    }

    // Set probeId, candidateId, and current image data
    (<IrisDisplayComponent>componentRef.instance).currentImageData = this.currentImageData;
    (<IrisDisplayComponent>componentRef.instance).loadedProbeImage = this.loadedProbeImage;
    (<IrisDisplayComponent>componentRef.instance).loadedCandidateImage = this.loadedCandidateImage;

    // Set draw and fill colors for candidate components
    (<IrisDisplayComponent>componentRef.instance).displayDrawColor = this.displayDrawColor;
    (<IrisDisplayComponent>componentRef.instance).displayFillColor = this.displayFillColor;

    // Set unroll info
    (<IrisDisplayComponent>componentRef.instance).unrollImagePath = this.unrollImagePath;
    (<IrisDisplayComponent>componentRef.instance).unrollRingData = this.unrollRingData;

    // Set view info
    (<IrisDisplayComponent>componentRef.instance).showOriginal = this.showOriginal;
    (<IrisDisplayComponent>componentRef.instance).showUnrolled = this.showUnrolled;
    (<IrisDisplayComponent>componentRef.instance).isReview = this.isReview;


    // Set orientation
    (<IrisDisplayComponent>componentRef.instance).isVertical = this.isVertical;
    (<IrisDisplayComponent>componentRef.instance).saved = this.saved;

    (<IrisDisplayComponent>componentRef.instance).pastStates = this.pastStates;
    (<IrisDisplayComponent>componentRef.instance).currState = this.currState;

    // Set tshepii annotations
    if (this.reviewSvc.checkAnnotationsForTshepii) {
      this.currentImageData.annotations.forEach((anno: Annotation) => {
        if (anno.text === "Tshepii") {
          let tshepiiAnnotations = []
          anno.tempChildren.forEach((child: Annotation) => {
            tshepiiAnnotations.push(child);
          });
          (<IrisDisplayComponent>componentRef.instance).tshepiiAnnotations = tshepiiAnnotations;
        }
      });
    }

    // Listen for the (reset) @Output EventEmitter of the IrisDisplayComponent
    (<IrisDisplayComponent>componentRef.instance).resetEmitter.subscribe(evt => this.resetCurrentDisplay());
    (<IrisDisplayComponent>componentRef.instance).clearEmitter.subscribe(evt => this.clearCurrentDisplayAnnotations());
    (<IrisDisplayComponent>componentRef.instance).updateUnrollEmitter.subscribe(evt => this.updateUnroll());

    (<IrisDisplayComponent>componentRef.instance).mouseMove.subscribe(evt => this.mouseMove.emit(evt));

    this.reviewSvc.cache(uuid, componentRef);
  }

  onCandidateChange(uuid) {
    const vcf = this.viewCandidate.viewContainerRef;
    vcf.detach();

    if (!this.reviewSvc.hasCached(uuid)) {
      this.createIrisDisplayComponent(uuid, true);
    } else {
      vcf.insert(this.reviewSvc.cache(uuid).hostView);
    }
  }
}
