/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BehaviorSubject, Observable, Subject, Subscription } from 'rxjs';
import { CreateIrisSearchRequest } from '../types/create-iris-search-request';
import { EyeLabel } from '../types/eye-label';
import { EbtsCreateService } from './ebts-create.service';
import { ProfileService } from '../profile/profile.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ContentService } from '../utils/content.service';
import { CreateContentService } from './create.service';
import { IrisDisplayComponent } from '../iris-display/iris-display.component';
import { Profile } from '../profile/profile';
import { NgForm } from '@angular/forms';
import { ToolManagerService } from '../iris-display/tool-manager.service';
import { HistoryService } from '../history/history.service';
import { SaveDialogComponent } from '../save-dialog/save-dialog.component';
import { DialogService } from '../utils/dialog.serivce';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactType } from '../types/contact-type';
import { BiqtContactService } from '../biqt-contact/biqt-contact.service';

@Component({
  templateUrl: 'create.component.html',
  styleUrls: ['./create.component.css'],
  providers: [
    {provide: ContentService, useClass: CreateContentService},
    ToolManagerService
  ]
})

export class CreateComponent implements OnInit, OnDestroy {
  config: any;
  configFileUrl = './iris_create_config.json';
  createUploadUrl = '/api/create/upload';
  eyeLabelValues = Object.keys(EyeLabel).map(k => EyeLabel[k]).filter(v => typeof v == 'number'); //the value of the Eye Label select is a number
  eyeLabelEnum = EyeLabel; //needed for the Eye Label select element
  contactTypeValues = Object.keys(ContactType).map(k => ContactType[k]).filter(v => typeof v == 'number');
  contactTypeEnum = ContactType;

  _hasImageSelected = false;
  set hasImageSelected(s: boolean) {
    this._hasImageSelected = s;
  }

  get hasImageSelected(): boolean {
    return this._hasImageSelected;
  }

  loadedFromSave = false;
  image: HTMLImageElement;
  saved = new Subject<any>();

  @ViewChild('probeDisplay') probeDisplay: IrisDisplayComponent;
  @ViewChild('createForm', {static: true}) createForm: NgForm;
  @ViewChild('wholeForm', {static: true}) wholeForm: NgForm;
  @ViewChild('saveDialog', {static: true}) saveDialog: SaveDialogComponent;
  @ViewChild('irisCaptureDate', {static: true}) captureDate: ElementRef;

  errorMessages: string[];
  irisImageQuality = -1;
  canvasReset = false;
  saveInProgress = false;
  originalQuality = -1;
  redirectUrl = '';
  createIrisSearchRequest: CreateIrisSearchRequest = new CreateIrisSearchRequest();

  irisCaptDate: Date = new Date();
  initTcn: string = this.generateTcn();

  showOriginal: BehaviorSubject<boolean>;
  showUnrolled: BehaviorSubject<boolean>;
  _biqtResponseSubscription: Subscription;
  _contactResponseSubscription: Subscription;
  _aciiResponseSubscription: Subscription;
  okayToNavigate = false;
  userParentGroupId: string;

  constructor(
    private route: ActivatedRoute,
    private contentSvc: ContentService,
    private profileService: ProfileService,
    private irisService: EbtsCreateService,
    private biqtContactService: BiqtContactService,
    private historyService: HistoryService,
    private dialogSvc: DialogService,
    public createService: CreateContentService,
    private router: Router,
    private matSnackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.image = new Image();
    this.image.crossOrigin = 'anonymous';

    // Route Resolver for config file
    this.route.data.subscribe(data => {
      this.config = data.config;
    });

    this.contentSvc.configure(this.config);
    this.config = Object.assign(this.config, this.contentSvc.config);

    this.createIrisSearchRequest.message.numberOfCandidates = this.config.num_of_candidates_default;

    this.createIrisSearchRequest.message.transactionControlNumber = this.generateTcn();

    this.setIrisCaptureDate(this.irisCaptDate)

    //set the fields from the profile
    this.setProfileFields(this.profileService.getCurrentProfile());
    this.profileService.currentProfile$.subscribe((profile) => {
      this.setProfileFields(profile);
    });

    this.saveDialog.closed$.subscribe((closed) => {
      if (closed) {
        if (this.saveDialog.continue) {
          if (this.saveDialog.save) {
            // Save Off Search
            this.finishSearchRequest();
              this.createIrisSearchRequest.message.imageData.imageData = this.probeDisplay.exportFlattenedPNG(true);
              this.historyService.saveIrisSearchRequest(this.createIrisSearchRequest)
                .subscribe((response) => {
                  this.saved.next(this.createIrisSearchRequest.message.imageData.annotations);
                  setTimeout(() => {
                    this.matSnackBar.open('EBTS saved', 'Dismiss');
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

    if (this.historyService.hasIrisSearchRequest) {
      this.createIrisSearchRequest = this.historyService.savedIrisSearchRequest;
      this.irisCaptDate = new Date(this.createIrisSearchRequest.message.irisCaptureDate);
      // Saving the userParentGroupId to set the toolManager's userParentGroupId later
      this.historyService.savedIrisSearchRequest.message.imageData.annotations.some(annotation => {
        if (annotation.type == 15) {
          this.userParentGroupId = annotation.id;
        }
      });
      
      if (!this.createIrisSearchRequest.message.transactionControlNumber) {
        this.createIrisSearchRequest.message.transactionControlNumber = this.generateTcn();
      }
  

      this.image.onload = () => {
        this.hasImageSelected = true;
        this.loadedFromSave = true;
      };

      //get the original id of the image
      this.image.src = `${this.config.imageURL}${this.createIrisSearchRequest.message.imageData.ebtsImageId}`;
    } else {
      this.createService.irisImageQuality = this.createService.initIrisImageQuality;
    }

    this.showOriginal = new BehaviorSubject<boolean>(true);
    this.showUnrolled = new BehaviorSubject<boolean>(true);
  }


  ngOnDestroy() {
    this.historyService.clearIrisSearchRequest();
    if (this._biqtResponseSubscription) {
      this._biqtResponseSubscription.unsubscribe();
    }
    if (this._contactResponseSubscription) {
      this._contactResponseSubscription.unsubscribe();
    }
    if (this._aciiResponseSubscription) {
      this._aciiResponseSubscription.unsubscribe();
    }
  }

  setIrisCaptureDate($event) {
    if ($event != null) {
      this.createIrisSearchRequest.message.irisCaptureDate = this.getDateString($event)
    }
  }

  changeImage() {
    if (this.dialogSvc.confirmBooleam('This will discard the current image. Proceed?')) {
      this.image = new Image();
      this.image.crossOrigin = 'anonymous';
      this.hasImageSelected = false;
    }
  }

  changeCaseInformation() {
    if (this.dialogSvc.confirmBooleam('This will remove all case information. Proceed?')) {
      this.createForm.resetForm({
        imageQualityInput: this.createService.initIrisImageQuality,
        caseExtension: this.createService.initExtension,
        numberOfCandidates: this.createService.initNumCandidate,
        destinationAgencyIdentifier: this.createService.initDai,
        originatingAgencyIdentifier: this.createService.initOri,
        transactionControlNumber: this.initTcn,
        attentionIndicator: this.createService.initAtt,
        eyeLabel: this.createService.initEyeLabel,
        contactType: this.createService.initContactType,
        sourceAgency: this.createService.initSourceAgency,
        irisCaptureDate: this.irisCaptDate,
        rotationOfEye: this.createService.initRoe,
        rotationUncertainty: this.createService.initRou
      });
    }
  }

  canDeactivate(url: string) {
    if (this.okayToNavigate) {
      return true;
    } else if (this.isComponentDirty()) {
      this.redirectUrl = url;
      this.saveDialog.show();
      return false;
    } else {
      return true;
    }
  }

  isComponentDirty() {
    if (this.hasImageSelected) {
      if (this.createForm.dirty) {
        return true;
      } else {
        // Checks if any annotations were made and if the save button wasn't hit
        return this.probeDisplay.getIrisModified() && !this.probeDisplay.toolManager.hasSaveButtonSelected;
      }
    }
    return false;
  }

  private setProfileFields(profile: Profile) {
    this.createIrisSearchRequest.message.destinationAgencyIdentifier = profile.destinationAgencyIdentifier;
    this.createIrisSearchRequest.message.originatingAgencyIdentifier = profile.originatingAgencyIdentifier;
    this.createIrisSearchRequest.message.attentionIndicator = profile.attentionIndicator;
    this.createIrisSearchRequest.message.sourceAgency = profile.sourceAgency;
  }

  generateTcn(): string {
    const d = new Date();
    const hour = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(d.getHours());
    const minute = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(d.getMinutes());
    const second = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(d.getSeconds());
    const datestring = this.getDateString(d) + hour + minute + second;

    return 'IWP-' +
      datestring + '-' +
      Math.floor(Math.random() * 1000000);
  }

  handleFileSuccess($event) {
    this.createIrisSearchRequest = $event[1];

    // If user edited the information before loading in the image, use the inputted information.
    // Doesn't include iris image quality, eye label, contact type, and rotation of eye of the message property because those update with the image
    // Doesn't include captureIrisDate since it's handled by a different method
    let formControls = this.createForm.controls;

    this.createIrisSearchRequest.message.cinPrefix = formControls.cinPrefix.value;
    this.createIrisSearchRequest.message.cinIdentifier = formControls.cinIdentifier.value;
    this.createIrisSearchRequest.message.caseExtension = formControls.caseExtension.value;
    this.createIrisSearchRequest.message.numberOfCandidates = formControls.numberOfCandidates.value;
    this.createIrisSearchRequest.message.destinationAgencyIdentifier = formControls.destinationAgencyIdentifier.value;
    this.createIrisSearchRequest.message.originatingAgencyIdentifier = formControls.originatingAgencyIdentifier.value;
    this.createIrisSearchRequest.message.transactionControlNumber = formControls.transactionControlNumber.value;
    this.createIrisSearchRequest.message.attentionIndicator = formControls.attentionIndicator.value;
    this.createIrisSearchRequest.message.sourceAgency = formControls.sourceAgency.value;
    this.createIrisSearchRequest.message.rotationOfEye = formControls.rotationOfEye.value;
    this.createIrisSearchRequest.message.rotationUncertainty = formControls.rotationUncertainty.value;

    if (this.createIrisSearchRequest.message.errorMessages.length > 0) {
      console.log(this.createIrisSearchRequest.message.errorMessages);
    }
    if (this.createIrisSearchRequest.message.errorMessages.length === 0) {
      this.image.onload = () => {
        this.hasImageSelected = true;
      };

      this.image.src = `${this.config.imageURL}${this.createIrisSearchRequest.message.imageData.ebtsImageId}`;
    } else {
      this.errorMessages = []; //clear the error messages
      this.errorMessages = this.createIrisSearchRequest.message.errorMessages;
    }
  }

  checkBiqtSnapshot(){
    let imageData = this.probeDisplay.exportFlattenedPNG(false);
    this.biqtContactService.getBiqtContactSnapshot(imageData, this.probeDisplay.currentImageData.ebtsImageId).subscribe((imageId) =>{
      let modifiedImageId = imageId;
      if(modifiedImageId != "-1"){
        this.createService.getIrisImageQualityAndContact(this.probeDisplay.currentImageData.ebtsImageId, false, modifiedImageId);
      }
    })
  }

  irisDisplayInitialized(evt) {
    if (this.canvasReset) {
      this.canvasReset = false;
      this.irisImageQuality = this.originalQuality;
      this.createIrisSearchRequest.message.rotationOfEye = Math.round(this.probeDisplay.toolManager._project.view.rotation);
    } else if (this.probeDisplay) {
      if (!this.historyService.hasIrisSearchRequest) {
        this.createService.getIrisImageQualityAndContact(this.createIrisSearchRequest.message.imageData.ebtsImageId, false);
        if (this.originalQuality == -1) {
          this.originalQuality = this.createService.irisImageQuality;
        } 
        this.createService.getAutoEyeLabel(this.createIrisSearchRequest.message.imageData.ebtsImageId, false);
      } else {
        this.createService.irisImageQuality = this.createIrisSearchRequest.message.imageData.eyeLabel;
        this.createService.eyeLabel = this.createIrisSearchRequest.message.imageData.eyeLabel;
      }
    }

    this.probeDisplay.toolChange$.subscribe((toolName) => {
      this.createIrisSearchRequest.message.rotationOfEye = Math.round(this.probeDisplay.toolManager._project.view.rotation);
    });
    
    // If the create report is coming from a save, 
    if (this.historyService.hasIrisSearchRequest) {
      this.probeDisplay.toolManager.userParentGroupId = this.userParentGroupId;
    }
  }

  handleFileFailure(obj) {
    console.log('Error getting image file');
  }

  setEyeLabel(eyeLabel: EyeLabel): void {
    this.createIrisSearchRequest.message.imageData.eyeLabel = eyeLabel;
  }

  resetIris($event) {
    const imgUri = this.image.src;

    this.hasImageSelected = false;
    this.image.onload = () => {
      this.hasImageSelected = true;
    };
    this.image.src = imgUri;

    this.canvasReset = true;
  }

  getDateString(d: Date) {
    const year = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 4, useGrouping: false}).format(d.getFullYear());
    const month = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(d.getMonth() + 1);
    const day = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(d.getDate());
    const dateString = year + month + day;
    return dateString;
  }

  finishSearchRequest() {
    this.saveInProgress = true;
    this.createIrisSearchRequest.message.imageData.annotations = this.probeDisplay.exportAnnotations();
    this.createIrisSearchRequest.message.imageData.eyeLabel = this.createService.eyeLabel;
    this.createIrisSearchRequest.message.imageData.contactType = this.createService.contactType;
  }

  submitIrisSearch(evt) {
    var _saveSubscription = this.saveSearchRequest().subscribe((response) => {
      if (response) {
        this.createIrisSearchRequest.isFinished = true;

        //save the image with all the annotations added to it
        this.createIrisSearchRequest.message.imageData.imageData = this.probeDisplay.exportFlattenedPNG(true);
        const handleResponse = (ebtsFile) => { //data is a Blob object
          //create the link element
          const a = document.createElement('a');
          document.body.appendChild(a);

          //set the fields
          const objectUrl = URL.createObjectURL(ebtsFile);
          a.href = objectUrl;
          a.download = this.createIrisSearchRequest.message.transactionControlNumber + '.ebts';
          a.click();
          window.URL.revokeObjectURL(objectUrl);

          setTimeout(() => {
            this.matSnackBar.open('Generated EBTS', 'Dismiss');
            window.location.reload();
          }, 1000);
        };

        // Regardless if it's from a case, it will ither create the search request and download it, or immediately download it
        if (!this.historyService.hasIrisSearchRequest || this.historyService.savedIrisSearchRequest.filePath == null) {
          this.irisService.createEbts(this.createIrisSearchRequest)
            .subscribe(handleResponse);
        } else {
          const req = this.historyService.savedIrisSearchRequest;
          req.message = this.createIrisSearchRequest.message;
          req.message.invalidFields = [];
          this.irisService.downloadEBTS(req.id)
            .subscribe(handleResponse);
        }
        this.saveInProgress = false;
        _saveSubscription.unsubscribe();
      }
    });
  }

  saveSearchRequest(): Observable<boolean> {
    var submitResponseSubject = new Subject<boolean>();
    submitResponseSubject.next(false);
    this.saveInProgress = true;
    this.finishSearchRequest();
    this.createIrisSearchRequest.message.imageData.imageData = this.probeDisplay.exportFlattenedPNG(true);

    //update the current case with the EbtsSearch
    const updateCase = (searchId) => {

      this.createForm.control.markAsPristine();
      this.saveInProgress = false;
    };

    this.historyService.saveIrisSearchRequest(this.createIrisSearchRequest)
      .subscribe((response) => {
        this.saved.next(this.createIrisSearchRequest.message.imageData.annotations);
        updateCase(response.message.id);
        setTimeout(() => {
          this.matSnackBar.open('EBTS saved', 'Dismiss');
        }, 1000);
        submitResponseSubject.next(true)
      });
    
    this.probeDisplay.toolManager.hasSaveButtonSelected = true;
    return submitResponseSubject
  }
}
