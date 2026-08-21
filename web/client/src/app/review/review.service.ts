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
import { HttpClient } from '@angular/common/http';
import { ServiceStatus } from '../types/service-response';
import { TshepiiService } from '../tshepii/tshepii.service';
import { Observable, Subscription } from 'rxjs';
import { TshepiiCompareResponse } from '../tshepii/tshepii-compare-response';
import { TshepiiResponse } from '../tshepii/tshepii-response';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CreateContentService } from '../create/create.service';
import { ServiceStatusNameType } from '../types/service-status';
import { Annotation } from '../types/annotation';
import { ReportData, ResponseType } from '../types/report-data';
import { Probe } from '../types/probe';
import { Candidate } from '../types/candidate';
import { CaseInformation } from '../types/case-information';
import { ServiceStatusService } from '../service-status/service-status.service';
import { AciiService } from '../acii/acii.service';
import { EyeLabel } from '../types/eye-label';
import { ContactType } from '../types/contact-type';
import { BiqtContactService } from '../biqt-contact/biqt-contact.service';
import { DualPDMService } from '../dual-pdm/dual-pdm.service';

const SAMPLE_CANDIDATES =
  [{determination: 'none', rank: '1', score: 5320, file: 'img1.jpg'},
    {determination: 'none', rank: '2', score: 3210, file: ''},
    {determination: 'none', rank: '3', score: 4320, file: 'img3.jpg'}];

@Injectable()
export class ReviewContentService extends ContentService {
  configFileUrl = './iris_review_config.json';
  adjudicationUrl = '/api/iris/adjudication';
  colors: any[] = [];

  //STARTING HERE
  tshepiiStatusText = '';
  _showTshepiiComparison = false;
  _showDualPDMComparison = false;
  _tshepiiResponseSubscription: Subscription;
  _dualPDMResponseStatusSubscription: Subscription;
  _dualPDMStatusSubscription: Subscription;
  _aciiResponseSubscription: Subscription;
  _biqtResponseSubscription: Subscription;
  _contactResponseSubscription: Subscription;
  _matchMap = new Map<string, number[]>();

  currentProbeImageIndex = -1;
  currentCandidateImageIndex = 0;
  currentCandidate: Candidate = undefined;
  dualPDMReturnedResults = false;
  tshepiiReturnedResults = false; //Set this to true on initialization. We set it to false, once we hit the tshepii mode

  currentReport: ReportData = {
    id: '',
    fileName: '',
    probe: <Probe>{
      id: '',
      name: '',
      subjectIdentifier: '',
      transactionControlReference: '',
      transactionControlNumber: '',
      imageList: [],
    },
    candidates: new Array<Candidate>(),
    includeExclude: false,
    responseType: ResponseType.Unknown,
    caseInformation: new CaseInformation(),
    userId: undefined,
    isFinished: false,
    errorMessages: Array<string>(),
    invalidFields: Array<string>(),
    workedBy: undefined,
    allowNullRequester: false,
  };

  probe = {
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

  candidate = {
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

  protected _config: object = {
    imageURL: '/api/image/',
    url: '/api/iris/response', // Formerly 'upload url', refactored to play nicely with the new dropzone wrapper component
    acceptedFiles: '.sub, .ebts, .nist, .eft, .xml, image/png, image/jp2, image/jp2l, image/bmp, image/jpeg, image/jpg, image/tiff, .tiff',
    headers: {'X-XSRF-TOKEN': document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1')}
  };
  private _irisDisplayCache = {};

  private _candidateList: any[] = [];

  constructor(private http: HttpClient,
              private tshepiiService: TshepiiService,
              public matSnackBar: MatSnackBar,
              private createService: CreateContentService,
              private aciiService: AciiService,
              private biqtContactService: BiqtContactService,
              private serviceStatusService: ServiceStatusService,
              private dualPDMService: DualPDMService) {
    super();

    this._candidateList = SAMPLE_CANDIDATES;
  }

  get candidateList() {
    return this._candidateList;
  }

  cache(key: string, val?: object) {
    if (val == undefined) {
      return this._irisDisplayCache[key] || null;
    }

    this._irisDisplayCache[key] = val;
  }

  hasCached(key: string) {
    return !!this._irisDisplayCache[key];
  }

  getPNGFromCanvasCache() {
    return Object.keys(this._irisDisplayCache).reduce((acc, uuid) => {
      const instance = (this._irisDisplayCache[uuid]).instance;

      acc[uuid] = instance.exportFlattenedPNG();

      return acc;
    }, {});
  }

  getUnrollPNGFromCanvasCache() {
    return Object.keys(this._irisDisplayCache).reduce((acc, uuid) => {
      const instance = (this._irisDisplayCache[uuid]).instance;
      acc[uuid] = instance.exportUnrollPNG();

      return acc;
    }, {});
  }

  //returns an object where each key is an image id and the value
  //is a list of Annotation values
  getAnnotationsFromCache(): object {
    const ret = {};
    Object.keys(this._irisDisplayCache).forEach(id => {
      const irisDisplay = this._irisDisplayCache[id].instance;
      ret[id] = irisDisplay.exportAnnotations();
    });


    return ret;
  }

  getPastStatesFromCache() {
    const ret = {};
    Object.keys(this._irisDisplayCache).forEach(id => {
      const irisDisplay = this._irisDisplayCache[id].instance;
      ret[id] = irisDisplay.exportPastStates();
    });

    return ret;
  }

  getCurrStateFromCache() {
    const ret = {};
    Object.keys(this._irisDisplayCache).forEach(id => {
      const irisDisplay = this._irisDisplayCache[id].instance;
      ret[id] = irisDisplay.exportCurrState();
    });

    return ret;
  }

  getToolManagerFromCache(): object {
    const ret = {};
    Object.keys(this._irisDisplayCache).forEach(id => {
      const irisDisplay = this._irisDisplayCache[id].instance;
      ret[id] = irisDisplay.toolManager;
    });

    return ret;
  }

  setDualPDMComparisonMode(comparisonMode: boolean, probe: any, candidate: any, contentSvc: ContentService) {
    this._showDualPDMComparison = comparisonMode;
    const reviewSvc = (<ReviewContentService>contentSvc);
    const probeInstance = reviewSvc.cache(probe.activeUuid).instance;
    const candidateInstance = reviewSvc.cache(candidate.activeUuid).instance;

    probeInstance.toggleComparison(!comparisonMode, !comparisonMode, !comparisonMode, comparisonMode);
    candidateInstance.toggleComparison(!comparisonMode, !comparisonMode, !comparisonMode, comparisonMode);
    probeInstance.toggleToolBar(this._showDualPDMComparison, true);
    candidateInstance.toggleToolBar(this._showDualPDMComparison, true);

  }

  getDualPDM(probeEbtsImageId: string, candidateEbtsImageId: string, isResubmit: boolean, probeModifiedImageId: string, candidateModifiedImageId: string) {
    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.dualpdm)) {
      return;
    }

    this.checkDualPDMIndividualStatus(probeEbtsImageId, candidateEbtsImageId, isResubmit, probeModifiedImageId, candidateModifiedImageId);
  }

  checkDualPDMIndividualStatus(probeEbtsImageId: string, candidateEbtsImageId: string, isResubmit: boolean, probeModifiedImageId?: string, candidateModifiedImageId?: string) {

    let probeId = probeEbtsImageId;
    if (!!probeModifiedImageId) {
      probeId = probeModifiedImageId;
    } else {
      probeModifiedImageId = '-1';
    }

    let candidateId = candidateEbtsImageId;
    if (!!candidateModifiedImageId) {
      candidateId = candidateModifiedImageId;
    } else {
      candidateModifiedImageId = '-1';
    }

    this.dualPDMService.startDualPdmResponseStatusPolling(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId);
    const serviceStatus = ServiceStatus;
    
    this._dualPDMStatusSubscription = this.dualPDMService.dualpdmResponseStatus$.subscribe(
      data => {
        switch (data) {
          // Error
          case serviceStatus.Error: {
            this.dualPDMService.stopDualPDMResponseStatusPolling();
            this._dualPDMStatusSubscription.unsubscribe();
            break;
          }

          // Received
          case serviceStatus.Received: {
            this.dualPDMService.stopDualPDMResponseStatusPolling();
            this.getDualPDMReponses(probeEbtsImageId, candidateEbtsImageId, isResubmit, probeModifiedImageId, candidateModifiedImageId);
            this._dualPDMStatusSubscription.unsubscribe();
            break;
          }

          // Pending
          case serviceStatus.Pending: {
            break;
          }

          // Not Sent
          case serviceStatus.NotSent: {
            break;
          }

          // Default
          default:
            this.dualPDMService.stopDualPDMResponseStatusPolling();
            break;
        }
      });
  }

  getDualPDMReponses(probeEbtsImageId: string, candidateEbtsImageId: string, isResubmit: boolean, probeModifiedImageId?: string, candidateModifiedImageId?: string) {

    let probeId = probeEbtsImageId;
    if (!!probeModifiedImageId && probeModifiedImageId != '-1') {
      probeId = probeModifiedImageId;
    } else {
      probeModifiedImageId = '-1';
    }

    let candidateId = candidateEbtsImageId;
    if (!!candidateModifiedImageId && candidateModifiedImageId != '-1') {
      candidateId = candidateModifiedImageId;
    } else {
      candidateModifiedImageId = '-1';
    }

    this.dualPDMService.startDualPDMCompareStatusPolling(probeEbtsImageId, candidateEbtsImageId, probeModifiedImageId, candidateModifiedImageId);
    const serviceStatus = ServiceStatus;

    this._dualPDMResponseStatusSubscription = this.dualPDMService.dualPDMCompareStatus$.subscribe(
      data => {
        switch (data) {
          case serviceStatus.Error: {
            this.dualPDMService.stopDualPDMCompareStatusPolling();
            this._dualPDMResponseStatusSubscription.unsubscribe();
            break;
          }

          // Received
          case serviceStatus.Received: {
            this.dualPDMService.stopDualPDMCompareStatusPolling();
            this.dualPDMService.getDualPDMComparison(probeId, candidateId, isResubmit).subscribe(
              response => {
                this.dualPDMReturnedResults = true;
              });
            this._dualPDMResponseStatusSubscription.unsubscribe();
            break;
          }

          // Pending
          case serviceStatus.Pending: {
            break;
          }

          // Not Sent
          case serviceStatus.NotSent: {
            break;
          }

          // Default
          default:
            this.dualPDMService.stopDualPDMCompareStatusPolling();
            break;
        }
      });
  }



  getTshepii(probe: any, candidate: any, contentSvc: ContentService, isResubmit: boolean, annotations: object, modifiedProbeId?: string, modifiedCandidateId?: string) {
    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.tshepii)) {
      return;
    }

    // set the tshepiiReturnedResults to false.
    this.tshepiiReturnedResults = false;
    this.tshepiiStatusText = 'fetching TSHEPII annotations...';

    // Before we get the Tshepii Comparison Response, we need to check to make sure that the Tshepii Responses for both images came back as Received.
    if (this._showTshepiiComparison) {
      this.checkTshepiiResponseStatusAndGetComparison(contentSvc, isResubmit, annotations, probe, candidate, modifiedProbeId, modifiedCandidateId);
    }
  }

  checkTshepiiResponseStatusAndGetComparison(contentSvc: ContentService, isResubmit: boolean, annotations: object, probe: any, candidate: any, probeModifiedImageId?: string, candidateModifiedImageId?: string) {
    // Please note that this method and the getTshepiiComparisonResults method looks the same, but this one is subscribing to the TshepiiResponseStatus, where as the other method is subscribing to TshepiiCompareStatus!!
    let probeId = probe.ebtsImageId;
    let candidateId = candidate.ebtsImageId;
    if (!!probeModifiedImageId) {
      probeId = probeModifiedImageId;
    } else {
      probeModifiedImageId = '-1';
    }
    if (!!candidateModifiedImageId) {
      candidateId = candidateModifiedImageId;
    } else {
      candidateModifiedImageId = '-1';
    }

    this.tshepiiService.startTshepiiResponseStatusPolling(probe.ebtsImageId, candidate.ebtsImageId, probeModifiedImageId, candidateModifiedImageId);
    const serviceStatus = ServiceStatus;
    this._tshepiiResponseSubscription = this.tshepiiService.tshepiiResponseStatus$.subscribe(
      data => {
        switch (data) {
          case serviceStatus.Error: {
            this.tshepiiService.stopTshepiiResponseStatusPolling();
            this._tshepiiResponseSubscription.unsubscribe();
            break;
          }
          // If the status is receieved for both images, we can submit the images to get the Tshepii Comparsion Results
          case serviceStatus.Received: {
            this.tshepiiService.stopTshepiiResponseStatusPolling();
            this._tshepiiResponseSubscription.unsubscribe();
            this.tshepiiService.getTshepiiComparison(probeId, candidateId, isResubmit).subscribe(
              response => {
              }
            );
            // Now we can pull the Tshepii Comparison Results from the database
            this.getTshepiiComparisonResults(probeModifiedImageId, candidateModifiedImageId, contentSvc, isResubmit, annotations, probe, candidate);
            break;
          }
          case serviceStatus.Pending: {
            break;
          }
          case serviceStatus.NotSent: {
            break;
          }
          default:
            console.log('Tshepii: No Status Yet');
            this.tshepiiService.stopTshepiiResponseStatusPolling();
            break;
        }
      });
    }

  getTshepiiComparisonResults(probeModifiedImageId: string, candidateModifiedImageId: string, contentSvc: ContentService, isResubmit: boolean, annotations: object, probe: any, candidate: any) {
    // Again, please note that this method looks similar to checkTshepiiResponseStatusAndGetComparison method, but this method is subscribing to the TshepiiCompareStatus not TshepiiResponseStatus!!
    let probeId = probe.ebtsImageId;
    let candidateId = candidate.ebtsImageId;
    
    if (!!probeModifiedImageId && probeModifiedImageId !== '-1') {
      probeId = probeModifiedImageId;
    }
    if (!!candidateModifiedImageId && candidateModifiedImageId !== '-1') {
      candidateId = candidateModifiedImageId;
    }

    this.tshepiiService.startTshepiiCompareStatusPolling(probe.ebtsImageId, candidate.ebtsImageId, probeModifiedImageId, candidateModifiedImageId);
    const serviceStatus = ServiceStatus;
    let tshepiiCompareStatusCounter = 0;
    let subscriptionId = probeId + "+" + candidateId;
    const pendingTimer = this.createService.setPendingTimer(isResubmit, ServiceStatusNameType.tshepii);
    this.tshepiiService._tshepiiCompareSubscription[subscriptionId] = this.tshepiiService.tshepiiCompareStatus$.subscribe(
      data => {
        this.createService.clearPendingTimer(data, pendingTimer);
        switch (data) {
          case serviceStatus.Error: {
            this.tshepiiService.stopTshepiiCompareStatusPolling(subscriptionId);
            this.tshepiiStatusText = 'Error fetching TSHEPII data.';
            this.setComparisonMode(!this._showTshepiiComparison, probe, candidate, contentSvc);
            break;
          }
          // If the status of the Tshepii Comparison is received, we can post the tshepii responses
          case serviceStatus.Received: {
            this.tshepiiService.getTshepiiComparison(probeId, candidateId, isResubmit).subscribe(
              tshepiiCompareResponse => {
                this.tshepiiReturnedResults = true;
                tshepiiCompareResponse.isResubmit = isResubmit;
                this.tshepiiService.stopTshepiiCompareStatusPolling(subscriptionId);
                this.checkOverlap(tshepiiCompareResponse, probe, candidate, contentSvc, annotations);
                this.tshepiiStatusText = '';
                this.createService.showSnackBar(isResubmit, false, ServiceStatusNameType.tshepii);
              });
            break;
          }
          case serviceStatus.Pending: {
            if (tshepiiCompareStatusCounter > 3) {
              this.tshepiiStatusText = 'Waiting on TSHEPII service to return....';
            } else {
              tshepiiCompareStatusCounter++;
            }
            break;
          }
          case serviceStatus.NotSent: {
            if (tshepiiCompareStatusCounter > 3) {
              this.setComparisonMode(!this._showTshepiiComparison, probe, candidate, contentSvc);
              this.tshepiiService.stopTshepiiCompareStatusPolling(subscriptionId);
              this.createService.showSnackBar(false, false, ServiceStatusNameType.tshepii, 'TSHEPII Service Unreachable. Please try again');
            } else {
              tshepiiCompareStatusCounter++;
            }
            break;
          }
          default:
            console.log('Tshepii: No Status Yet');
            this.setComparisonMode(!this._showTshepiiComparison, probe, candidate, contentSvc);
            this.tshepiiService.stopTshepiiCompareStatusPolling(subscriptionId);
            break;
        }
      });
  }

  setComparisonMode(comparisonMode: boolean, probe: any, candidate: any, contentSvc: ContentService) {
    this._showTshepiiComparison = comparisonMode;
    try {
      const reviewSvc = (<ReviewContentService>contentSvc);
      const probeInstance = reviewSvc.cache(probe.activeUuid).instance;
      const candidateInstance = reviewSvc.cache(candidate.activeUuid).instance;

      probeInstance.toggleComparison(!comparisonMode, !comparisonMode, !comparisonMode, comparisonMode);
      candidateInstance.toggleComparison(!comparisonMode, !comparisonMode, !comparisonMode, comparisonMode);
      probeInstance.toggleToolBar(this._showTshepiiComparison);
      candidateInstance.toggleToolBar(this._showTshepiiComparison);
    } catch (e) {
    }
  }

  checkOverlap(tshepiiCompareResponse: TshepiiCompareResponse, probe: any, candidate: any, contentSvc: ContentService, annotations: object) {
    const reviewSvc = (<ReviewContentService>contentSvc);
    const probeInstance = reviewSvc.cache(probe.activeUuid).instance;
    const candidateInstance = reviewSvc.cache(candidate.activeUuid).instance;

    if (this._showTshepiiComparison) {
      let probeInitResponse: TshepiiResponse;
      let candidateInitResponse: TshepiiResponse;

      if (probeInstance._tshepiiResponse) {
        probeInitResponse = probeInstance._tshepiiResponse;
      }

      if (candidateInstance._tshepiiResponse) {
        candidateInitResponse = candidateInstance._tshepiiResponse;
      }

      const probeResponse = tshepiiCompareResponse.probeResponse;
      const candidateResponse = tshepiiCompareResponse.candidateResponse;

      if (tshepiiCompareResponse.error) {
        window.alert('TSHEPII Response error');
        this.setComparisonMode(!this._showTshepiiComparison, probe, candidate, contentSvc);
        return;
      }

      if (probeResponse.cropRadius == 0 || candidateResponse.cropRadius == 0) {
        window.alert('TSHEPII Response error');
        this.setComparisonMode(!this._showTshepiiComparison, probe, candidate, contentSvc);
        return;
      }

      if (!this.checkAnnotationsForTshepii(Array.from(annotations[probe.activeUuid]))) {
        probeInstance.setTshepii(probeResponse, '#00ff00');
      }

      if (!this.checkAnnotationsForTshepii(Array.from(annotations[candidate.activeUuid]))) {
        candidateInstance.setTshepii(candidateResponse, '#ff0000');
      }

      if (probeResponse.matchIndex.length != candidateResponse.matchIndex.length) {
        console.log('Match Index Lengths do not match');
      } else {
        const newKey = probe.activeUuid + candidate.activeUuid;
        const tempArray = [];

        for (let i = 0; i < probeResponse.matchIndex.length; i++) {
          const match = [];
          match.push(probeResponse.matchIndex[i]);
          match.push(candidateResponse.matchIndex[i]);

          tempArray.push(match);
        }
        
        this._matchMap.set(newKey, tempArray);
        this.drawTshepiiComparison(probe, candidate, contentSvc);
      }
    }
  }

  checkAnnotationsForTshepii(annotations: any): boolean {
    let returnValue = false;

    annotations.some((anno: Annotation) => {
      if (anno.text === "Tshepii") {
        returnValue = true;
      } else {
        if (anno.tempChildren.length > 0) {
          anno.tempChildren.forEach(child => {
            if (child.tempChildren.length > 0 && child.text !== "Tshepii") {
                returnValue = this.checkAnnotationsForTshepii(child);
            } else {
              if (child.text === "Tshepii") {
                returnValue = true;
              } else {
                returnValue = false;
              }
            }
          });
        } else {
          returnValue = false;
        }
      }
    });
    return returnValue;
  }

  drawTshepiiComparison(probe: any, candidate: any, contentSvc: ContentService) {
    const reviewSvc = (<ReviewContentService>contentSvc);
    const probeInstance = reviewSvc.cache(probe.activeUuid).instance;
    const candidateInstance = reviewSvc.cache(candidate.activeUuid).instance;

    probeInstance.toolManager.removeTshepiiItems();
    probeInstance.toolManager.removeTshepiiHighlights();

    candidateInstance.toolManager.removeTshepiiItems();
    candidateInstance.toolManager.removeTshepiiHighlights();

    const keyCheck = probe.activeUuid + candidate.activeUuid;

    const temp = this._matchMap.get(keyCheck);

    for (let i = 0; i < temp.length; i++) {
      if (temp.length >= i) {
        const tempArray = temp[i];
        const probeIndex = tempArray[0];
        const candidateIndex = tempArray[1];

        const probeAnno = probeInstance.tshepiiAnnotations[probeIndex];
        const candAnno = candidateInstance.tshepiiAnnotations[candidateIndex];

        if (probeAnno != undefined) {
          const probePath = probeInstance.toolManager.addTshepiiPoly(probeAnno, '#00ff00');
          probePath.matchIndex = probeIndex;

          /**********************   SAME COLOR - Mouse Over CODE ****************************/

          probePath.onMouseEnter = function(event) {
            this.fillColor = '#ff0000';
            this.fillColor.alpha = .2;
            this.bringToFront();

            for (const subArray of temp) {
              const subProbeIndex = subArray[0];
              if (this.matchIndex == subProbeIndex) {
                const subCandidateIndex = subArray[1];
                const candSubAnno = candidateInstance.tshepiiAnnotations[subCandidateIndex];
                candidateInstance.toolManager.addTshepiiHighlight(candSubAnno, '#00ff00');
              }
            }
          };

          probePath.onMouseLeave = function(event) {
            this.fillColor = '#00ff00';
            this.fillColor.alpha = .1;
            candidateInstance.toolManager.removeTshepiiHighlights();
          };

          /*******************  END SAME COLOR - Mouse Over CODE ****************************/
        }
        if (candAnno != undefined) {
          const candidatePath = candidateInstance.toolManager.addTshepiiPoly(candAnno, '#ff0000');
          candidatePath.matchIndex = candidateIndex;

          /**********************   SAME COLOR - Mouse Over CODE ****************************/

          candidatePath.onMouseEnter = function(event) {
            this.fillColor = '#00ff00';
            this.fillColor.alpha = .2;
            this.bringToFront();
  
            for (const subArray of temp) {
              const subCandidateIndex = subArray[1];
              if (this.matchIndex == subCandidateIndex) {
                const subProbeIndex = subArray[0];
                const probeSubAnno = probeInstance.tshepiiAnnotations[subProbeIndex];
                probeInstance.toolManager.addTshepiiHighlight(probeSubAnno, '#ff0000');
              }
            }
          };
  
          candidatePath.onMouseLeave = function(event) {
            this.fillColor = '#ff0000';
            this.fillColor.alpha = .1;
            probeInstance.toolManager.removeTshepiiHighlights();
          };

          /*******************  END SAME COLOR - Mouse Over CODE ****************************/
        }
      }
    }
  }

  getAdjudicationOptions(): Observable<any> {
    return this.http.get<ServiceStatus>(this.adjudicationUrl);
  }

  getIrisImageQualityAndContact(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.biqt) || this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.contactdetection)) {
      return;
    }
    
    this.biqtContactService.requestBiqtContactResponse(ebtsImageId, isResubmit, modifiedImageId);

    const biqtPendingTimer = this.createService.setPendingTimer(isResubmit, ServiceStatusNameType.biqt);
    this._biqtResponseSubscription = this.biqtContactService.biqtResponse$.subscribe(
      response => {

        this.createService.clearPendingTimer(response.status, biqtPendingTimer);
        const quality = response.quality;
        
        // If it NOT a resubmit, the biqtReponse.imageId should be the same number
        // as the current image's ebtsImageId. However if it IS a resubmit,
        // the new modified image will have a different Id, so we need to base it off
        // the original Id number.
        if (!response.isResubmit) {
          // There is a small chance that both images 
          // may have same Hash/EBTS Image Id. So we need to loop through both sets
          // of images and update all, not just probe OR candidate.
          this.checkBiqtImageId(response.imageId, quality);
        } else {
          this.checkBiqtImageId(response.ebtsImageId, quality);
        }

        this._biqtResponseSubscription.unsubscribe();
        this.createService.showSnackBar(response.isResubmit, false, ServiceStatusNameType.biqt);
      });


    const contactPendingTimer = this.createService.setPendingTimer(isResubmit, ServiceStatusNameType.contactdetection);
    this._contactResponseSubscription = this.biqtContactService.contactResponse$.subscribe(
      response => {
        let contactType;
        this.createService.clearPendingTimer(response.status, contactPendingTimer);
        switch (response.detectionCode) {
          case "0": 
            contactType = ContactType.None;
            break;
          case "1":
            contactType = ContactType.Cosmetic;
            break;
          case "2":
            contactType =  ContactType.Clear;
            break;
          case "3":
          default:
            contactType = ContactType[''];
            break;
        }

        // If it NOT a resubmit, the contactResponse.imageId should be the same number
        // as the current image's ebtsImageId. However if it IS a resubmit,
        // the new modified image will have a different Id, so we need to base it off
        // the original Id number.
        if (!isResubmit) {
          // There is a small chance that both images 
          // may have same Hash/EBTS Image Id. So we need to loop through both sets
          // of images and update all, not just probe OR candidate.
          this.checkContactImageId(response.imageId, contactType);
        } else {
          this.checkContactImageId(response.ebtsImageId, contactType);
        }
        
        this._contactResponseSubscription.unsubscribe();
        this.createService.showSnackBar(isResubmit, false, ServiceStatusNameType.contactdetection);
      });
  
  }

  checkBiqtImageId(imageId: string, quality: number): void {
    if (this.currentProbeImageIndex != -1) {
      if (this.currentReport.probe.imageList[this.currentProbeImageIndex].ebtsImageId == imageId) {
        this.currentReport.probe.imageList[this.currentProbeImageIndex].qualityScore = quality;
      }
    }

    if (this.currentCandidate) {
      if (this.currentCandidate.imageList[this.currentCandidateImageIndex].ebtsImageId == imageId) {
        this.currentCandidate.imageList[this.currentCandidateImageIndex].qualityScore = quality;
      }
    }
  }

  checkContactImageId(imageId: string, contactType: ContactType): void {
    if (this.currentProbeImageIndex != -1) {
      if (this.currentReport.probe.imageList[this.currentProbeImageIndex].ebtsImageId == imageId) {
        this.currentReport.probe.imageList[this.currentProbeImageIndex].contactType = contactType;
      }
    }

    if (this.currentCandidate) {
      if (this.currentCandidate.imageList[this.currentCandidateImageIndex].ebtsImageId == imageId) {
        this.currentCandidate.imageList[this.currentCandidateImageIndex].contactType = contactType;
      }
    }
  }

  getEyeLabel(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void {
    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.acii)) {
      return;
    }

    this.aciiService.requestAciiResponse(ebtsImageId, isResubmit, modifiedImageId);

    const pendingTimer = this.createService.setPendingTimer(isResubmit, ServiceStatusNameType.acii);
    this._aciiResponseSubscription = this.aciiService.aciiResponse$.subscribe(aciiResponse => {
      this.createService.clearPendingTimer(aciiResponse.status, pendingTimer);
      const orientation = aciiResponse.horizontal;

      // If it is NOT a resubmit, the aciiResponse.imageId should be the same number as the current image's ebtsImageId.
      // However, if it IS a resubmit, the new modified image will have a different id, so we need to base it off
      // the original Id number.
      if (!aciiResponse.isResubmit) {
        // There is a small chance that both images may have the same Hash/EBTS Image Id. So, we need to loop through
        // both sets of images and update all, not just the probe or candidate.
        this.checkAciiImageId(aciiResponse.imageId, orientation);
      } else {
        this.checkAciiImageId(aciiResponse.ebtsImageId, orientation);
      }

      this._aciiResponseSubscription.unsubscribe();
      this.createService.showSnackBar(aciiResponse.isResubmit, false, ServiceStatusNameType.acii);
    });
  }

  checkAciiImageId(imageId: string, orientation: string): void {
    if (this.currentProbeImageIndex != -1) {
      let currentProbeImage = this.currentReport.probe.imageList[this.currentProbeImageIndex];
      if (currentProbeImage.ebtsImageId == imageId){
        currentProbeImage.eyeLabel = this.setEyeLabel(orientation);
        this.probe.analysisPosition = this.setEyeLabel(orientation);
      }
    }

    if (this.currentCandidate) {
      if (this.currentCandidate.imageList[this.currentCandidateImageIndex].ebtsImageId == imageId) {
        this.currentCandidate.imageList[this.currentCandidateImageIndex].eyeLabel = this.setEyeLabel(orientation);
        this.candidate.analysisPosition = this.setEyeLabel(orientation);
      }
    }
  }

  //this is because in typescript enums can be different types
  //and they can be different types coming from a json response
  public setEyeLabel(str: string) {
    switch (str) {
      case 'Undefined':
      case '0':
        return EyeLabel.Undefined;
      case 'Right':
      case '1':
        return EyeLabel.Right;
      case 'Left':
      case '2':
        return EyeLabel.Left;
      case '':
      case '3':
      default:
        return EyeLabel[''];
    }
  }
  
}
