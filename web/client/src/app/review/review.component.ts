/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, ElementRef, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ProfileService } from '../profile/profile.service';
import { ReviewContentService } from './review.service';
import { ContentService } from '../utils/content.service';
import { ResponseType } from '../types/report-data';
import { ResponseReviewService } from './response-review.service';
import { Candidate } from '../types/candidate';
import { Probe } from '../types/probe';
import { EyeLabel } from '../types/eye-label';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { NgForm } from '@angular/forms';
import { EbtsImageData } from '../types/image-data';
import { DialogService } from '../utils/dialog.serivce';
import { UnrollCompareDialogComponent } from './unroll-compare/unroll-compare-dialog.component';
import { HistoryService } from '../history/history.service';
import { SaveDialogComponent } from '../save-dialog/save-dialog.component';
import { TshepiiService } from '../tshepii/tshepii.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CaseInformation } from '../types/case-information';
import { Person } from '../types/person';
import { ServiceStatus } from '../types/service-response';
import { AnnotationComponent } from '../annotation-dialog/annotation.component';
import { ContactType } from '../types/contact-type';
import { PdmConfirmationDialogComponent } from '../pdm-confirmation-dialog/pdm-confirmation-dialog.component';

enum ReviewStatus {
  FILE_LOAD,
  FULL_RESPONSE,
  RESETTING,
  ACTIVE_CANDIDATE
}

@Component({
  templateUrl: 'review.component.html',
  styleUrls: ['./review.component.css'],
  providers: [
    {provide: ContentService, useClass: ReviewContentService},
  ]
})
export class ReviewComponent implements OnInit, OnDestroy {
  config: any;
  probeConfig: any = {}; // copy of config with probe-specific tweaks

  loadedFromSave = false;
  colors: any;
  candidateStatus = ReviewStatus.FILE_LOAD;
  reviewStatus = ReviewStatus; //this is so you can reference review status within the template
  eyeLabel = EyeLabel;
  eyeLabelOptions = Object.keys(EyeLabel).splice(0, Object.keys(EyeLabel).length / 2);
  contactType = ContactType;
  contactTypeOptions = Object.keys(ContactType).splice(0, Object.keys(ContactType).length / 2);

  redirectUrl = '';
  singleImageProbeLoaded = false;
  singleImageCandidateLoaded = false;
  isCandidatePanelExpanded: boolean;
  okayToNavigate = false;

  loadedUUIDs: any[] = [];
  persons: Person[] = [];
  isSystemGeneratedAnnotationsIncluded = true;

  probeSaved = new Subject<any>();
  candidateSaved = new Subject<any>();

  @ViewChild('errorModal', {static: true}) errorModal: TemplateRef<any>;
  @ViewChild('reportFieldForm', {static: true}) reportFieldForm: NgForm;
  @ViewChild('reportDetails') reportDetails: ElementRef;
  @ViewChild('saveDialog', {static: true}) saveDialog: SaveDialogComponent;
  @ViewChild('reviewForm', {static: true}) reviewForm: NgForm;
  candidateSelectedTab = 0;
  probeSelectedTab = 0;

  canSubmit = false;
  canUnloadCandidate = false;
  canUnloadProbe = false;
  canUnloadFile = false;
  tshepiiLoading = false;
  counter = 0;
  hasSaveButtonSelected = false;
  isProbeIrisDataMissing = false;
  isCandidateIrisDataMissing = false;

  // Prevent users from loading file, unloading, and accidentally overwriting saved files on reset
  savedReviewFileReset = false;
  saveInProgress = false;

  tempImageData: EbtsImageData;

  testImageData: any;

  probeShowOrigSubject = new BehaviorSubject<boolean>(true);
  probeShowUnrolledSubject = new BehaviorSubject<boolean>(false);
  candShowOrigSubject = new BehaviorSubject<boolean>(true);
  candShowUnrolledSubject = new BehaviorSubject<boolean>(false);
  probeStatusSubject = new Subject();
  candStatusSubject = new Subject();
  // Replace these BSmodals with MatDialog!
  previewModalRef: BsModalRef;
  compareModalRef: MatDialogRef<UnrollCompareDialogComponent>;
  isGhostCursorOn = false;
  isVertical = false;
  isVerticalSubject = new BehaviorSubject<boolean>(false);
  isTshepiiCompareReady = false;
  _tshepiiResponseSubscription: Subscription;
  _contactResponseSubscription: Subscription[] = [];
  annotationComp: AnnotationComponent;
  _aciiResponseSubscription: Subscription[] = [];
  _biqtResponseSubscription: Subscription[] = [];
  adjudicationOptions: string[] = [];

  get canShowGhostCursor(): boolean {
    return this.reviewService.probe.status === 'loaded'
      && this.reviewService.currentCandidate !== null
      && this.candidateStatus == ReviewStatus.ACTIVE_CANDIDATE;
  }

  constructor(
    private route: ActivatedRoute,
    private modalService: BsModalService,
    private contentSvc: ContentService,
    private responseReviewService: ResponseReviewService,
    private profileService: ProfileService,
    private historyService: HistoryService,
    private dialogSvc: DialogService,
    private tshepiiService: TshepiiService,
    private router: Router,
    private matDialogService: MatDialog,
    private matSnackBar: MatSnackBar,
    public reviewService: ReviewContentService,
  ) {

  }

  ngOnInit() {
    // Route Resolver for config file
    this.route.data.subscribe(data => {
      this.config = data.config;
    });

    // load adjudication values
    this.reviewService.getAdjudicationOptions().subscribe(ret => {
      this.adjudicationOptions = ret;
    });

    this.contentSvc.configure(this.config);
    this.config = Object.assign(this.config, this.contentSvc.config);

    this.probeConfig = Object.assign(this.probeConfig, this.config);
    this.probeConfig.maxFiles = 1;

    this.saveDialog.closed$.subscribe((closed) => {
      if (closed) {
        if (this.saveDialog.continue) {
          if (this.saveDialog.save) {
            // Save Off Search
            this.finalizeReport();
            this.historyService.saveReview(this.reviewService.currentReport)
              .subscribe((response) => {
                this.probeSaved.next(this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].annotations);
                if (this.reviewService.currentCandidate) {
                  this.candidateSaved.next(this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex].annotations);
                }
                setTimeout(() => {
                  this.matSnackBar.open('Review saved', 'Dismiss');
                }, 1000);
                this.okayToNavigate = true;
                this.router.navigateByUrl(this.redirectUrl);
              });
          } else {
            this.okayToNavigate = true;
            this.router.navigateByUrl(this.redirectUrl);
          }
        }
        this.saveDialog.hide();
      }
    });

    //check for case req parameters
    this.route.params.subscribe(params => {
      if (params['id']) {
        if (this.historyService.hasSavedReview) {
          this.loadExistingReview(this.historyService.savedReview);
        }
      } else {
        this.isCandidatePanelExpanded = true;
      }
    });
  }

  loadExistingReview(savedReview: any) {
    this.reviewService.currentReport = savedReview;
    this.loadedFromSave = true;

    if (this.reviewService.currentReport.probe.imageList.length > 0) {
      this.reviewService.currentProbeImageIndex = 0;
      this.reviewService.probe.activeUuid = this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].id;

      if (this.loadedUUIDs.indexOf(this.reviewService.probe.activeUuid) < 0) {
        this.loadedUUIDs.push(this.reviewService.probe.activeUuid);
      }

      for (const imageData of this.reviewService.currentReport.probe.imageList) {
        imageData.eyeLabel = this.reviewService.setEyeLabel(imageData.eyeLabel.toString());
        imageData.contactType = this.getContactType(imageData.contactType.toString());
      }

      this._loadProbeImage(this.reviewService.currentReport.probe.imageList[0]);
    } else {
      this.reviewService.currentProbeImageIndex = -1;
    }

    if (this.reviewService.currentReport.candidates.length > 0) {
      for (const candidate of this.reviewService.currentReport.candidates) {
        for (const imageData of candidate.imageList) {
          imageData.eyeLabel = this.reviewService.setEyeLabel(imageData.eyeLabel.toString());
          imageData.contactType = this.getContactType(imageData.contactType.toString());
        }
      }

      this.candidateStatus = ReviewStatus.FULL_RESPONSE;
    }

    this.candidateAdjudicated({});

    if (this.reviewService.currentReport.responseType.toString() == 'SRB' || this.reviewService.currentReport.responseType.toString() == 'XML') {
      this.canUnloadFile = true;
    } else if (this.reviewService.currentReport.responseType.toString() == 'Image' || this.reviewService.currentReport.responseType.toString() == 'Unknown') {
      if (this.reviewService.currentReport.probe.imageList.length > 0) {
        this.canUnloadProbe = true;
      }

      if (this.reviewService.currentReport.candidates.length > 0) {
        this.canUnloadCandidate = true;
      }
    }
    
    if (this.reviewService.currentReport.candidates.length > 0) {
      this.selectCandidate(this.reviewService.currentReport.candidates[0]);
    }
  }

  disableSave() {
    return !(this.canUnloadProbe || this.canUnloadCandidate || this.canUnloadFile);
  }

  checkEyeLabel(userSetEyeLabel: EyeLabel, serviceSetEyeLabel: EyeLabel) {
    return userSetEyeLabel == serviceSetEyeLabel;
  }

  ngOnDestroy() {
    this.historyService.clearSavedReview();
    this.stopTshepiiResponsePolling();
    this._contactResponseSubscription.forEach(subscription => {
      subscription.unsubscribe();
    });

    this._biqtResponseSubscription.forEach(subscription => {
      subscription.unsubscribe();
    });

    this._aciiResponseSubscription.forEach(subscription => {
      subscription.unsubscribe();
    });

    this.unloadFile();
  }

  canDeactivate(url: string) {
    if (this.okayToNavigate) {
      return true;
    } else if ((this.isComponentDirty() && !this.hasSaveButtonSelected) || this.reviewForm.dirty) {
      this.redirectUrl = url;
      this.saveDialog.show();
      return false;
    } else {
      return true;
    }
  }

  isComponentDirty() {
    if (this.historyService.hasSavedReview && this.reviewForm.dirty) {
      return true;
    }

    if (this.loadedFromSave) {
      if (this.reviewForm.dirty) {
        return true;
      }

      // Check each tool manager for modified Iris Images from last save point
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      for (const item of this.loadedUUIDs) {
        if (reviewSvc.cache(item).instance.toolManager.getIrisModified()) {
          return true;
        }
      }

      return false;
    } else {

      return (this.reviewService.probe.status !== 'empty') ||
        (this.candidateStatus !== ReviewStatus.FILE_LOAD) ||
        (this.reviewService.currentCandidate !== undefined) ||
        (!!this.reportFieldForm && this.reportFieldForm.dirty);
    }
  }

  resetAnnos() {
    const keyCheck = this.reviewService.probe.activeUuid + this.reviewService.candidate.activeUuid;
    this.reviewService._matchMap.delete(keyCheck);
  }

  getTshepiiButtonTooltip(): string {
    if (!this.canRunTShepiiCompare) {
      return 'Load and select both a probe and a candidate to enable TSHEPII Mode.';
    } else {
      return this.reviewService._showTshepiiComparison ? 'Exit Tshepii Mode' : 'Enter Tshepii Mode';
    }
  }

  checkMatchMap(): void {
    this.reviewService.setComparisonMode(!this.reviewService._showTshepiiComparison, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
    if (!this.reviewService._showTshepiiComparison) {
      this.reviewService.drawTshepiiComparison(this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
      this.reviewService.tshepiiStatusText = '';
    } else {
      this.reviewService.tshepiiReturnedResults = false;
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      let annotations = reviewSvc.getAnnotationsFromCache();
      this.reviewService.getTshepii(this.reviewService.probe, this.reviewService.candidate, this.contentSvc, false, annotations);
    }
  }

  getDualPDMButtonTooltip(): string {
    if (!this.isDualPdmCompareReady()) {
      return 'Load and select both a probe and a candidate to enable Dual PDM Mode.';
    } else {
      return this.reviewService._showDualPDMComparison ? 'Save Images and Exit Dual PDM Mode' : 'Enter Dual PDM Mode';
    }
  }

  triggerDualPdmMode(): void {
    // If it's in DualPDMComparison mode (_showDualPDMComparison is true), we want to show the confirmation dialog. We don't want to change the mode view yet since the user could decide to keep working on dual pdm
    if (this.reviewService._showDualPDMComparison) {
      this.matDialogService.open(PdmConfirmationDialogComponent).afterClosed().subscribe(selectedChoice => {
      switch (selectedChoice) {
        case 'confirm':
          this.onConfirm();
          break;
        case 'cancel':
          this.onCancel();
          break;
        case 'back':
          // Do nothing since the user wants to keep editing the image.
          break;
      }
      });
    } 
    // If it's not in DualPDMComparison mode (_showDualPDMComparison is false), we want to set it put it in DualPDMComparison mode and call getDualPDM()
    else {
      this.reviewService.setDualPDMComparisonMode(!this.reviewService._showDualPDMComparison, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
      this.reviewService.getDualPDM(this.reviewService.probe.ebtsImageId, this.reviewService.candidate.ebtsImageId, false, null, null);
    }
  }

  onConfirm() {
     // Capture cached annotations and send to getDualPDM for proccessing
    this.reviewService.getDualPDM(this.reviewService.probe.ebtsImageId, this.reviewService.candidate.ebtsImageId, false, null, null);

    // Turn off the comparison mode
    this.reviewService.setDualPDMComparisonMode(!this.reviewService._showDualPDMComparison, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
  }

  onCancel() {
    const reviewSvc = (<ReviewContentService>this.contentSvc);
    let probeInstance = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;
    let candidateInstance = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;

    let probeAnnotation = probeInstance.pdmGroupAnnotation;
    let candidateAnnotation = candidateInstance.pdmGroupAnnotation;

    // for each probe and candidate, we need to remove the Dual Pdm annotation if there is one.
    if (probeAnnotation) {
      probeInstance.toolManager.removeItem(probeAnnotation.id, false, false, false);
    }
    if (candidateAnnotation) {
      candidateInstance.toolManager.removeItem(candidateAnnotation.id, false, false, false);

    }

    // Turn off the comparison mode
    this.reviewService.setDualPDMComparisonMode(!this.reviewService._showDualPDMComparison, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
  }

  changeCandidateImage() {
    if (this.reviewService.currentReport.responseType == ResponseType.Unknown) {
      this.singleImageCandidateLoaded = false;
    }

    this.stopTshepiiResponsePolling();
    if (this.dialogSvc.confirmBooleam('This will discard the loaded Candidate Image. Proceed?')) {
      // Resetting the candidate information if discarding loaded candidate image
      this.reviewService.candidate.showInfo = false;
      this.reviewService.candidate.imgLoaded = false;
      this.reviewService.candidate.ebtsImageId = '';
      this.reviewService.candidate.image = null;
      this.reviewService.candidate.unrollImagePath = '';
      this.reviewService.candidate.resetting = false;
      this.reviewService.candidate.analysisPosition = EyeLabel[''];
      this.reviewService.candidate.activeUuid = '';
      this.reviewService.candidate.unrollRingData = {};
      this.reviewService.candidate.hasPdmResultsReturned = false;

      // Resetting the review service information:
      this.reviewService.dualPDMReturnedResults = false;
      this.reviewService._showDualPDMComparison = false;
      this.reviewService._showTshepiiComparison = false;
      this.reviewService.currentReport.candidates.length = 0;
      this.reviewService.currentCandidateImageIndex = 0;
      this.reviewService.currentCandidate = undefined;

      // Resetting component information
      this.candidateStatus = ReviewStatus.FILE_LOAD;
      this.canUnloadCandidate = false;
      this.isCandidatePanelExpanded = true;
      this.isTshepiiCompareReady = false;

      // Need to reset the original and unrolled canvas
      this.candShowOrigSubject.next(true);
      this.candShowUnrolledSubject.next(false);

      this.candStatusSubject.next(false);
      this.candStatusSubject.complete();

      if (this.historyService.hasSavedReview) {
        this.savedReviewFileReset = true;
      }

      return true;
    }

    return false;
  }

  changeProbeImage() {
    if (this.reviewService.currentReport.responseType == ResponseType.Unknown) {
      this.singleImageProbeLoaded = false;
    }

    this.stopTshepiiResponsePolling();
    if (this.dialogSvc.confirmBooleam('This will discard the Probe Image. Proceed?')) {
      // Resetting the probe information if discarding probe candidate image
      this.reviewService.probe.showInfo = false;
      this.reviewService.probe.imgLoaded = false;
      this.reviewService.probe.ebtsImageId = '';
      this.reviewService.probe.status = 'empty';
      this.reviewService.probe.activeUuid = '';
      this.reviewService.probe.analysisPosition = EyeLabel[''];
      this.reviewService.probe.image = null;
      this.reviewService.probe.unrollImagePath = '';
      this.reviewService.probe.unrollRingData = {};
      this.reviewService.probe.hasPdmResultsReturned = false;

      // Resetting the review service information:
      this.reviewService.dualPDMReturnedResults = false;
      this.reviewService.currentProbeImageIndex = -1;
      this.reviewService._showDualPDMComparison = false;
      this.reviewService._showTshepiiComparison = false;

      // Resetting component information
      this.canUnloadProbe = false;
      this.isTshepiiCompareReady = false;
      
      // Need to reset the original and unrolled canvas
      this.probeShowOrigSubject.next(true);
      this.probeShowUnrolledSubject.next(false);

      this.probeStatusSubject.next(false);
      this.probeStatusSubject.complete();
      this.reviewService.currentReport.probe.imageList = [];
      if (this.historyService.hasSavedReview) {
        this.savedReviewFileReset = true;
      }

      return true;
    }
    return false;
  }

  unloadFileClicked() {
    if (this.dialogSvc.confirmBooleam('This will discard the loaded file and all Probe and Candidate Images. Proceed?')) {
      this.unloadFile();
    }
  }

  unloadFile() {
    this.reviewService.currentReport = {
      id: '',
      fileName: '',
      probe: new Probe(),
      candidates: new Array<Candidate>(),
      responseType: ResponseType.Unknown,
      includeExclude: false,
      caseInformation: new CaseInformation(),
      userId: undefined,
      isFinished: false,
      errorMessages: Array<string>(),
      invalidFields: Array<string>(),
      workedBy: undefined,
      allowNullRequester: false
    };

    this.reviewService.probe = {
      showInfo: false,
      imgLoaded: false,
      ebtsImageId: '',
      status: 'empty',
      activeUuid: '',
      analysisPosition: EyeLabel[''],
      image: null,
      unrollImagePath: '',
      unrollRingData: {},
      hasPdmResultsReturned: false
    };

    this.reviewService.candidate = {
      showInfo: false,
      imgLoaded: false,
      ebtsImageId: '',
      image: null,
      unrollImagePath: '',
      resetting: false,
      analysisPosition: EyeLabel[''],
      activeUuid: '',
      unrollRingData: {},
      hasPdmResultsReturned: false
    };

    this.reviewService.currentReport.candidates.length = 0;
    this.reviewService.currentCandidateImageIndex = 0;
    this.reviewService.currentProbeImageIndex = -1;
    this.candidateStatus = ReviewStatus.FILE_LOAD;
    this.reviewService.currentCandidate = undefined;
    this.canUnloadFile = false;
    this.isCandidatePanelExpanded = true;
    this.reviewService.dualPDMReturnedResults = false;
    this.isTshepiiCompareReady = false;
    this.reviewService.tshepiiReturnedResults = true;
    this.reviewService._showDualPDMComparison = false;
    this.reviewService._showTshepiiComparison = false;

    if (this.historyService.hasSavedReview) {
      this.savedReviewFileReset = true;
    }
  }

  openCompareModal() {
    const reviewSvc = (<ReviewContentService>this.contentSvc);

    const data = {

      probe: reviewSvc.cache(this.reviewService.probe.activeUuid).instance,
      candidate: reviewSvc.cache(this.reviewService.candidate.activeUuid).instance,

    };

    const unrollCompareConfig = {
      data
    };
    this.compareModalRef = this.matDialogService.open(UnrollCompareDialogComponent, unrollCompareConfig);
  }

  isComparable(): boolean {
    if (!!this.reviewService.probe.activeUuid && !!this.reviewService.candidate.activeUuid) {
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      if (!!reviewSvc.cache(this.reviewService.probe.activeUuid) && !!reviewSvc.cache(this.reviewService.candidate.activeUuid)) {
        const probe = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;
        const candidate = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;
        return probe.toolManager.hasUnrolled.value && candidate.toolManager.hasUnrolled.value;
      }
    }

    return false;
  }

  candidateAdjudicated(event) {
    let result = this.reviewService.currentReport.candidates.length > 0;
    for (const candidate of this.reviewService.currentReport.candidates) {
      if (candidate.adjudicationResults == undefined) {
        result = false;
      }

      //This doesn't make sense in normal languages but enums in
      //typescript can end up being strings
      if (typeof candidate.adjudicationResults == 'string') {
        const s: string = candidate.adjudicationResults;
        result = s === '' ? false : result;
      }
    }
    this.canSubmit = result;
  }

  //this is used to switch between candidates
  selectCandidate(candidate) {
    this.stopTshepiiResponsePolling();
    this.reviewService.currentCandidate = candidate;

    if(candidate.currentImageIndex != undefined && candidate.currentImageIndex != -1) {
      this.reviewService.currentCandidateImageIndex = candidate.currentImageIndex;
      this.candidateSelectedTab = candidate.currentImageIndex;
    } else {
      this.reviewService.currentCandidateImageIndex = 0;
      this.reviewService.currentCandidate.currentImageIndex = 0;
      this.candidateSelectedTab = 0;
    }

    this.isCandidateIrisDataMissing = false;
    const selectedCandidate = this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex];
    this.isCandidateIrisDataMissing = this.checkIrisDataMissing(selectedCandidate);

    let tempImageData = this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex];
    let tabCount = 0;
    this.reviewService.currentCandidate.imageList.forEach(imageData => {
      if(imageData.recordType == 17){
        tempImageData = imageData;
        return;
      }

      tabCount++;
    });

    this._loadCandidateImage(tempImageData);
    if(tempImageData.recordType == 17) {
      this.reviewService.currentCandidateImageIndex = tabCount;
      this.candidateSelectedTab = tabCount;
    }

    this.checkTshepiiResponseStatus();
    this.isCandidatePanelExpanded = false;
  }

  //called when the mouse moves on the probe image
  probeIrisDisplayMouseMove(point: any) {
    if (this.isGhostCursorOn) {
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      const probeInstance = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;
      const candidateInstance = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;

      candidateInstance.toolManager.showGhostCursor = true;
      probeInstance.toolManager.showGhostCursor = false;

      //for the event point coming in it is a general canvas onmousemove event which returns
      //x,y in that canvas's coordinate system.  paperjs has its own coordinate system that is
      //the view's matrix.  So you when you take a point from the canvas coordinate system
      //paperjs will apply a transformation to change it to its system.  By applying the inverse
      //matrix will are essentially cancelling that out.
      const temp = point.transform(probeInstance.toolManager.project.view.matrix.inverted());

      candidateInstance.toolManager.ghostCursorPosition = temp;
    }
  }

  isTshepiiDisabled(): boolean {
    if (!this.canRunTShepiiCompare || this.reviewService._showDualPDMComparison) {
      return true;
    } else {
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      const probeCache = reviewSvc.cache(this.reviewService.probe.activeUuid);
      const candidateCache = reviewSvc.cache(this.reviewService.candidate.activeUuid);

      if (probeCache == null || candidateCache == null)
      {
        return true;
      }

      if (probeCache.instance.inPdmMode || candidateCache.instance.inPdmMode) {
        return true;
      }
    }
    return false;
  }

  isDualPDMDisabled(): boolean {
    if (!this.isDualPdmCompareReady() || this.reviewService._showTshepiiComparison) {
      return true;
    } else {
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      const probeCache = reviewSvc.cache(this.reviewService.probe.activeUuid);
      const candidateCache = reviewSvc.cache(this.reviewService.candidate.activeUuid);

      if (probeCache == null || candidateCache == null)
      {
        return true;
      }

      if (probeCache.instance.inPdmMode || candidateCache.instance.inPdmMode) {
        return true;
      }
    }
    return false;
  }

  updateProbeUnroll(uuid: string) {
    if (this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].id == uuid) {
      // Reset unroll image id to force unroll update on backend
      this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].unrollImageId = undefined;
    }

    this.updateProbeStatus();
  }

  updateCandidateUnroll(uuid) {
    if (this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex].id == uuid) {
      this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex].unrollImageId = undefined;
    }
    this.updateCandidateStatus();
  }

  //called when the mouse moves on the candidate image
  candidateIrisDisplayMouseMove(point: any) {
    if (this.isGhostCursorOn) {
      const reviewSvc = (<ReviewContentService>this.contentSvc);
      const probeInstance = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;
      const candidateInstance = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;

      candidateInstance.toolManager.showGhostCursor = false;
      probeInstance.toolManager.showGhostCursor = true;

      const temp = point.transform(candidateInstance.toolManager.project.view.matrix.inverted());

      //probeInstance.toolManager.ghostCursorPosition = new Point(event.offsetX, event.offsetY);
      probeInstance.toolManager.ghostCursorPosition = temp;
    }
  }

  //this is just used to load a candidate image
  private _loadCandidateImage(imageData: EbtsImageData) {
    const uuid = imageData.id;
    this.reviewService.candidate.image = new Image();
    this.reviewService.candidate.image.crossOrigin = 'anonymous';

    this.reviewService.candidate.imgLoaded = false;

    this.reviewService.candidate.unrollImagePath = `${this.config.imageURL}${imageData.unrollImageId}`;
    this.reviewService.candidate.unrollRingData = imageData.unrollRingData;

    this.reviewService.candidate.image.src = `${this.config.imageURL}${imageData.ebtsImageId}`;
    this.reviewService.candidate.image.uuid = uuid;

    this.reviewService.candidate.ebtsImageId = imageData.ebtsImageId;

    this.reviewService.candidate.image.onload = () => {
      this.reviewService.candidate.imgLoaded = true;
      this.candidateStatus = ReviewStatus.ACTIVE_CANDIDATE;

      // this needs to be updated AFTER image is loaded,
      // so events triggered by this value changing can
      // act upon the newly loaded image (as it is already loaded)
      this.reviewService.candidate.activeUuid = uuid;

      if (this.loadedUUIDs.indexOf(uuid) < 0) {
        this.loadedUUIDs.push(uuid);
      }
    };

    if(imageData.recordType == 17 || this.reviewService.currentCandidate.imageList.length == 1){
      if (!this.historyService.hasSavedReview) {
        this.reviewService.getEyeLabel(this.reviewService.candidate.ebtsImageId, false);
        this.reviewService.getIrisImageQualityAndContact(this.reviewService.candidate.ebtsImageId, false);
      }
    }
    this.reviewService.setComparisonMode(false, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);
  }

  onLoadingError(evt) {
    alert('Error loading file...');
    console.error(evt);
  }

  private _loadProbeImage(imageData: EbtsImageData) {
    const uuid = imageData.id;
    //same thing as loading candidate image
    //so that the image will be updated
    
    const img = new Image();
    img.crossOrigin = 'anonymous';        // enable CORS

    this.reviewService.probe.unrollImagePath = `${this.config.imageURL}${imageData.unrollImageId}`;
    this.reviewService.probe.unrollRingData = imageData.unrollRingData;

    img.src = `${this.config.imageURL}${imageData.ebtsImageId}`;
    this.reviewService.probe.ebtsImageId = imageData.ebtsImageId;

    img.onload = (evt) => {

      this.reviewService.probe.image = img;
      this.reviewService.probe.imgLoaded = true;
      this.reviewService.probe.status = 'loaded';
      this.reviewService.probe.activeUuid = uuid;

      if (this.loadedUUIDs.indexOf(uuid) < 0) {
        this.loadedUUIDs.push(uuid);
      }
    };

    if(imageData.recordType == 17 || this.reviewService.currentReport.probe.imageList.length == 1) {
      // You can use the ebtsImageId since it's not a resubmitted image.
      if (!this.historyService.hasSavedReview) {
        this.reviewService.getEyeLabel(this.reviewService.probe.ebtsImageId, false);
        this.reviewService.getIrisImageQualityAndContact(this.reviewService.probe.ebtsImageId, false);
      }
    }

    this.reviewService.setComparisonMode(false, this.reviewService.probe, this.reviewService.candidate, this.contentSvc);

  }


  private getContactType(str: string) {
    switch (str) {
      case 'None':
      case '0':
        return ContactType.None;
      case 'Cosmetic':
      case '1':
        return ContactType.Cosmetic;
      case 'Clear':
      case '2':
        return ContactType.Clear;
      case '3':
      default:
        return ContactType[''];
    }
  }

  setProbeFullResponse(obj: any) {
    this.reviewService.currentProbeImageIndex = 0;
    this.reviewService.currentReport = obj;

    this.reviewService.currentReport.responseType = obj.responseType;
    switch (obj.responseType) {
      case 'SRB':
      case '1':
        this.reviewService.currentReport.responseType = ResponseType.SRB;
        break;
      case 'ERRB':
      case '2':
        this.reviewService.currentReport.responseType = ResponseType.ERRB;
        break;
      case 'XML':
      case '3':
        this.reviewService.currentReport.responseType = ResponseType.XML;
        break;
      default:
        this.reviewService.currentReport.responseType = ResponseType.Unknown;
        break;
    }

    if (this.reviewService.currentReport.responseType == ResponseType.ERRB) {
      // Show the ERRB error modal, and make sure the errorModalClose function runs if the modal is dismissed
      this.modalService.show(this.errorModal);
      this.modalService.onHide.subscribe(() => {
        this.errorModalClose();
      });
    } else {
      this.reviewService.probe.activeUuid = this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].ebtsImageId;

      if (this.loadedUUIDs.indexOf(this.reviewService.probe.activeUuid) < 0) {
        this.loadedUUIDs.push(this.reviewService.probe.activeUuid);
      }

      for (const imageData of this.reviewService.currentReport.probe.imageList) {
        imageData.eyeLabel = this.reviewService.setEyeLabel(imageData.eyeLabel.toString());
        imageData.contactType = this.getContactType(imageData.contactType.toString());
      }

      for (const candidate of this.reviewService.currentReport.candidates) {
        for (const imageData of candidate.imageList) {
          imageData.eyeLabel = this.reviewService.setEyeLabel(imageData.eyeLabel.toString());
          imageData.contactType = this.getContactType(imageData.contactType.toString());

        }
      }

      this.candidateStatus = ReviewStatus.FULL_RESPONSE;
      this._loadProbeImage(this.reviewService.currentReport.probe.imageList[0]);
    }

  }

  get canRunTShepiiCompare(): boolean {
    return this.isTshepiiCompareReady;
  }

  isDualPdmCompareReady(): boolean {
    return (this.reviewService.probe.hasPdmResultsReturned && this.reviewService.candidate.hasPdmResultsReturned);
  }

  stopTshepiiResponsePolling() {
    this.tshepiiService.stopTshepiiResponseStatusPolling();
    if (this._tshepiiResponseSubscription) {
      this._tshepiiResponseSubscription.unsubscribe();
    }
  }

  checkTshepiiResponseStatus(): void {
    this.stopTshepiiResponsePolling();
    this.isTshepiiCompareReady = false;
    if (this.reviewService.probe.ebtsImageId && this.reviewService.candidate.ebtsImageId) {
      this.tshepiiService.startTshepiiResponseStatusPolling(this.reviewService.probe.ebtsImageId, this.reviewService.candidate.ebtsImageId, '-1', '-1');
      const serviceStatus = ServiceStatus;
      this._tshepiiResponseSubscription = this.tshepiiService.tshepiiResponseStatus$.subscribe(
        data => {
          switch (data) {
            case serviceStatus.Error: {
              this.stopTshepiiResponsePolling();
              this.isTshepiiCompareReady = false;
              break;
            }
            case serviceStatus.Received: {
              this.stopTshepiiResponsePolling();
              this.isTshepiiCompareReady = true;
              break;
            }
            case serviceStatus.Pending: {
              this.isTshepiiCompareReady = false;
              break;
            }
            case serviceStatus.NotSent: {
              this.isTshepiiCompareReady = false;
              break;
            }
            default:
              console.log('Tshepii: No Status Yet');
              this.isTshepiiCompareReady = false;
              this.stopTshepiiResponsePolling();
              break;
          }
        });
    }
  }

  onCandidateLoadSuccess($event) {
        this.stopTshepiiResponsePolling();
    const tempObj = JSON.parse($event[0].xhr.response);

    if (tempObj.responseType == undefined) {

      const tempImageDataArray = Object.assign([], tempObj);
      const tempCandidate = new Candidate();
      tempImageDataArray.forEach(
        (item) =>{
          const tempImageData: EbtsImageData = item;
          tempImageData.eyeLabel = EyeLabel[''];
          tempImageData.contactType = ContactType[''];

          tempCandidate.imageList.push(tempImageData);
        }
      );

      this.reviewService.currentReport.responseType = ResponseType.Image;
      this.reviewService.currentReport.candidates.push(tempCandidate);
      this.selectCandidate(tempCandidate);
      this.canUnloadCandidate = true;
      this.singleImageCandidateLoaded = true;
    } else {
      if (this.canUnloadProbe) {
        if (!this.changeProbeImage()) {
          window.alert('Single Probe Image already loaded. Please select a single image for Candidate Canvas or unload Probe image');
          return;
        }
      }

      this.setProbeFullResponse(tempObj);
      this.setCanUnloadFile();
    }
    this.checkTshepiiResponseStatus();
    this.checkForIdenticalImages();
  }

  onProbeLoadSuccess($event) {
    this.stopTshepiiResponsePolling();
    const tempObj = JSON.parse($event[0].xhr.response);

    if (tempObj.responseType == undefined) {
      const tempImageDataArray = Object.assign([], tempObj);

      let tabGroup = 0;
      let loadImageData;
      tempImageDataArray.forEach(
        (item) =>{
          const tempImageData: EbtsImageData = item;
          tempImageData.eyeLabel = EyeLabel[''];
          tempImageData.contactType = ContactType[''];
          this.reviewService.currentReport.probe.imageList.push(tempImageData);

          if(!loadImageData || (loadImageData.recordType != 17 && tempImageData.recordType == 17)){
            loadImageData = tempImageData;
            this.probeSelectedTab = tabGroup;
          }

          tabGroup++;
        }
      );


      this.reviewService.currentProbeImageIndex = this.probeSelectedTab;

      this.reviewService.currentReport.responseType = ResponseType.Image;
      this.canUnloadProbe = true;
      this.singleImageProbeLoaded = true;
      this._loadProbeImage(loadImageData);

    } else {
      if (this.canUnloadCandidate) {
        if (!this.changeCandidateImage()) {
          window.alert('Single Candidate Image already loaded. Please select a single image for Probe Canvas or unload Candidate image');
          return;
        }
      }

      this.setProbeFullResponse(tempObj);
      this.setCanUnloadFile();
    }

    this.checkTshepiiResponseStatus();
    this.checkForIdenticalImages();
  }

  checkSaveImageLoad() {
    if (this.singleImageProbeLoaded && this.singleImageCandidateLoaded) {
      this.saveReview(true);
    }
  }

  checkForIdenticalImages() {
    const candidates = this.reviewService.currentReport.candidates;
    if (candidates.length > 0 && !!this.reviewService.probe) {
      candidates.forEach((candidate, candidateIndex) => {
        candidate.imageList.forEach((img, imgIndex) => {
          if (img.ebtsImageId === this.reviewService.probe.ebtsImageId) {
            this.matSnackBar.open(`Image ${imgIndex + 1} for Candidate ${candidateIndex + 1} is identical to the probe.`, 'Dismiss', {duration: -1});
          }
        });
      });
    }
  }

  onProbeLoad(data) {
    this.reviewService.currentReport = data;
    //make sure no matter what that the index is set to the first probe image
    this.reviewService.currentProbeImageIndex = 0;

    this._loadProbeImage(this.reviewService.currentReport.probe.imageList[0]);
    this.canUnloadProbe = true;

    this.setProbeFullResponse(data);
    this.setCanUnloadFile();
  }

  setCanUnloadFile() {
    this.canUnloadFile = true;
    this.canUnloadCandidate = false;
    this.canUnloadProbe = false;
  }

  onProbeReset($event) {
    this.reviewService.probe.status = 'resetting';
    this._loadProbeImage(this.reviewService.currentReport.probe.imageList[0]);
  }

  openPreview(previewTemplate: TemplateRef<any>) {
    this.previewModalRef = this.modalService.show(previewTemplate);
  }

  submitButtonClick(event) {
    event.preventDefault();
    this.sendReport(this.reviewService.currentReport);
  }

  sendReport(reportData) {
    this.previewModalRef.hide();
    this.saveInProgress = true;
    this.reviewService.currentReport.allowNullRequester = true;
    this.saveReview(false);

    this.responseReviewService.createReport(this.reviewService.currentReport).subscribe(
      (ret) => {
        this.reviewService.currentReport = ret;
      },
      (err) => {
        this.saveInProgress = false;
        this.matSnackBar.open('Report generation has failed', 'Dismiss', {duration: -1, verticalPosition: 'top'});
      },
      () => {
        this.saveInProgress = false;
        this.matSnackBar.open('Report sent for generation.', 'Dismiss');
      });
  }

  finalizeReport() {
    this.reviewService.currentReport.includeExclude = !this.profileService.getCurrentProfile().isReportExclude;

    for (const candidate of this.reviewService.currentReport.candidates) {
      candidate.imageList.forEach(image => {
        this.reportImageProcessing(image);
      });
    }

    //set the data for the current candidate
    if (this.reviewService.currentCandidate != undefined) {

      this.reviewService.currentCandidate.imageList.forEach(image => {
        this.reportImageProcessing(image);
      });
    }

    //get image data
    this.reviewService.currentReport.probe.imageList.forEach(image => {
      // I know this looks like duplications, but without doing that, it doesn't work.
      // Something is going wrong with redrawing the canvas and setting the image.imageData if the inital
      // image.imageData is undefined.
      if (image.imageData == undefined) {
        this.reportImageProcessing(image);
      }
      this.reportImageProcessing(image);
    });

    if (this.reviewService.currentReport.fileName == undefined || this.reviewService.currentReport.fileName == '') {
      if (this.reviewService.currentReport.probe.transactionControlNumber == undefined
        || this.reviewService.currentReport.probe.transactionControlNumber == '') {
        this.reviewService.currentReport.fileName = Math.floor(Math.random() * 10000000) + '.pdf';
      } else {
        this.reviewService.currentReport.fileName = this.reviewService.currentReport.probe.transactionControlNumber + '.pdf';
      }
    }
  }

  reportImageProcessing(image: EbtsImageData) {
    const reviewSvc = (<ReviewContentService>this.contentSvc);
    const canvasPNGData = reviewSvc.getPNGFromCanvasCache();
    const canvasUnrollPNGData = reviewSvc.getUnrollPNGFromCanvasCache();
    const annotationData = reviewSvc.getAnnotationsFromCache();
    const pastStatesData = reviewSvc.getPastStatesFromCache();
    const currStateData = reviewSvc.getCurrStateFromCache();
    const toolManagerCache = reviewSvc.getToolManagerFromCache();

    const imageToolManager = toolManagerCache[image.id];
    if (imageToolManager != undefined) {

      if (annotationData[image.id]) {
        if (this.isSystemGeneratedAnnotationsIncluded) {
          image.annotations = annotationData[image.id];
        } else {
          imageToolManager.actualAnnotations = [];
          image.annotations = [];
          annotationData[image.id].forEach(annotation => {
            if (!annotation.isSystemGenerated) {
              image.annotations.push(annotation);
              imageToolManager.actualAnnotations.push(annotation);
            }
          });
          imageToolManager.redrawAnnotations();
        }
      }

      if (imageToolManager.unrollInnerRing != undefined) {
        image.unrollRingData = {
          innerCenterX: imageToolManager.unrollInnerRing.center.x,
          innerCenterY: imageToolManager.unrollInnerRing.center.y,
          innerRadius: imageToolManager.unrollInnerRing.radius,
          outerCenterX: imageToolManager.unrollOuterRing.center.x,
          outerCenterY: imageToolManager.unrollOuterRing.center.y,
          outerRadius: imageToolManager.unrollOuterRing.radius,
        };
      }
    } 

    if (currStateData[image.id]) {
      if (this.isSystemGeneratedAnnotationsIncluded) {
        image.currState = currStateData[image.id];
      } else {
        if (!currStateData[image.id].annotation.isSystemGenerated) {
          image.currState = currStateData[image.id];
        }
      }
    }

    if (pastStatesData[image.id]) {
      if (this.isSystemGeneratedAnnotationsIncluded) {
        image.pastStates = pastStatesData[image.id];
      } else {
        image.pastStates = [];
        pastStatesData[image.id].forEach(state => {
          if (!state.annotation.isSystemGenerated) {
            image.pastStates.push(state);
          }
        });
      }
    }

    if (canvasUnrollPNGData[image.id]) {
      if (this.isSystemGeneratedAnnotationsIncluded) {
        image.unrollImageData = canvasUnrollPNGData[image.id] || image.unrollImageData;
      } else {
        if (!canvasUnrollPNGData[image.id].isSystemGenerated) {
          image.unrollImageData = canvasUnrollPNGData[image.id];
        }
      }
    }

    if (canvasPNGData[image.id]) {
      if (this.isSystemGeneratedAnnotationsIncluded) {
        image.imageData = canvasPNGData[image.id] || image.imageData;
      } else {
        image.imageData = canvasPNGData[image.id];
      }
    }
  }

  errorModalClose() {
    window.location.reload();
  }

  closeSubmit() {
    this.reportFieldForm.resetForm();
    this.reviewService.currentReport.probe.transactionControlNumber = '';
  }

  selectProbeImage(index: number) {
    this.stopTshepiiResponsePolling();
    //set the current annotations for the current image on the probe
    this.reviewService.currentProbeImageIndex = index;
    this.isProbeIrisDataMissing = false;
    const selectedProbe = this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex];
    this.isProbeIrisDataMissing = this.checkIrisDataMissing(selectedProbe);

    this._loadProbeImage(this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex]);
    this.checkTshepiiResponseStatus();
  }

  selectCandidateImage(index: number) {
    this.stopTshepiiResponsePolling();
    //get the annotations for the current image and save them
    this.reviewService.currentCandidateImageIndex = index;
    this.isCandidateIrisDataMissing = false;
    const selectedCandidate = this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex];
    this.reviewService.currentCandidate.currentImageIndex = index;
    this.isCandidateIrisDataMissing = this.checkIrisDataMissing(selectedCandidate);

    this._loadCandidateImage(this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex]);
    this.checkTshepiiResponseStatus();
  }

  showGhostCursor() {
    this.isGhostCursorOn = !this.isGhostCursorOn;
    const reviewSvc = (<ReviewContentService>this.contentSvc);
    const probeInstance = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;
    const candidateInstance = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;

    if (this.isGhostCursorOn) {
      probeInstance.toolManager.showGhostCursor = true;
      candidateInstance.toolManager.showGhostCursor = true;
    } else {
      probeInstance.toolManager.showGhostCursor = false;
      candidateInstance.toolManager.showGhostCursor = false;
    }
  }

  saveReview(showSaveSnackBar: boolean) {
    if (this.reviewService.currentReport.allowNullRequester != true) {
      this.reviewService.currentReport.allowNullRequester = false;
    }
    
    const funct = (report) => {
      this.reviewForm.control.markAsPristine();
      this.hasSaveButtonSelected = true;
      if (showSaveSnackBar) {
        setTimeout(() => {
          this.matSnackBar.open('Review saved', 'Dismiss');
        }, 1000);
      }
    };
    this._saveReview(funct);
  }

  completeReview() {
    this.reviewService.currentReport.allowNullRequester = true;
    const redir = this.redirectUrl;
    const funct = (reportId) => {
      window.location.assign(redir);
    };

    const handleResponse = (data) => { //data is a Blob object
      setInterval(() => {
        window.location.reload();
      }, 1000);
    };

    this._saveReview(funct);
    this.responseReviewService.createReport(this.reviewService.currentReport)
      .subscribe(handleResponse);
  }

  _saveReview(funct: any) {
    this.saveInProgress = true;
    this.finalizeReport();
    this.historyService.saveReview(this.reviewService.currentReport)
      .subscribe((reportData) => {
        this.reviewService.currentReport = reportData;
        this.probeSaved.next(this.reviewService.currentReport.probe.imageList[this.reviewService.currentProbeImageIndex].annotations);
        if (this.reviewService.currentCandidate) {
          this.candidateSaved.next(this.reviewService.currentCandidate.imageList[this.reviewService.currentCandidateImageIndex].annotations);
        }
       funct(this.reviewService.currentReport);

        this.saveInProgress = false;
      });
  }

  setVertical(vertical) {
    this.isVertical = vertical;
    this.isVerticalSubject.next(this.isVertical);
  }

  /*
   * Fired when the newly uploaded image's uuid gets cached into
   * ReviewContentService.
   * Subscribe to hasUnrolled to toggle Unrolled active tab automatically.
   * When the image gets discarded, manually unsubscribe thru statusSubjects
   * to prevent leak.
   *
   */
  updateProbeStatus() {
    const reviewSvc = (<ReviewContentService>this.contentSvc);
    const probeInstance = reviewSvc.cache(this.reviewService.probe.activeUuid).instance;

    // update the original canva
    probeInstance.toolManager.hasOriginal.pipe(
      takeUntil(this.probeStatusSubject)
    ).subscribe((res: boolean) => {
      this.probeShowUnrolledSubject.next(res);
    });

    // update the unrolled canva
    probeInstance.toolManager.hasUnrolled.pipe(
      takeUntil(this.probeStatusSubject)
    ).subscribe((res: boolean) => {
      this.probeShowUnrolledSubject.next(res);
    });

    
  }

  updateCandidateStatus() {
    const reviewSvc = (<ReviewContentService>this.contentSvc);
    const candidateInstance = reviewSvc.cache(this.reviewService.candidate.activeUuid).instance;

    // update the original canvas
    candidateInstance.toolManager.hasOriginal.pipe(
      takeUntil(this.candStatusSubject)
    ).subscribe((res: boolean) => {
      this.candShowOrigSubject.next(res);
    });

    // update the unrolled canvas
    candidateInstance.toolManager.hasUnrolled.pipe(
      takeUntil(this.candStatusSubject)
    ).subscribe((res: boolean) => {
      this.candShowUnrolledSubject.next(res);
    });
  }

  checkIrisDataMissing(selectedImage) {
    if (selectedImage.dme == 'UC' || selectedImage.dme == 'MA' || selectedImage.dme == null) {
      return true;
    } else if (selectedImage.ebtsImageId == '-1') {
      return true;
    } else {
      return false;
    }
  }

}
