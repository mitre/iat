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
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  IterableDiffer,
  IterableDiffers,
  OnDestroy,
  OnInit,
  Output,
  TemplateRef,
  ViewChild
 } from '@angular/core';
import { ContentService } from '../utils/content.service';
import { ToolManagerService, UnrollStatus } from './tool-manager.service';
import { IrisToolRegistry } from './tools/tool.registry';
import { take } from 'rxjs/operators';
import { BehaviorSubject, Observable, Subject, Subscription } from 'rxjs';
import { Path, Point, Raster } from 'paper';
import { CircleTool, PointTool, PolygonTool } from './tools/tool-components';
import { Annotation, AnnotationType, ANNO_ICON_MAPPING } from '../types/annotation';
import { LinkedMap } from '../types/linked-map';
import { State, StateType } from '../types/state';
import { TshepiiService } from '../tshepii/tshepii.service';
import { IwpPoint } from '../tshepii/iwp-point';
import { TshepiiResponse } from '../tshepii/tshepii-response';
import { MapUtils } from '../utils/map-utils';
import { ProfileService } from '../profile/profile.service';
import { MatDrawer } from '@angular/material/sidenav';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AnnotationComponent } from '../annotation-dialog/annotation.component';
import { AnnotationHistoryComponent } from '../annotation-history-dialog/annotation-history.component';
import { IrisToolType } from './tools/tool.base';
import { IrisAnnotationService } from '../irisannotation/iris-annotation.service';
import { ServiceStatus } from '../types/service-response';
import { MatDialog } from '@angular/material/dialog';
import { IwpServiceStatus, ServiceStatusNameType, ServiceStatusStatusType } from '../types/service-status';
import { ServiceStatusService } from '../service-status/service-status.service';
import { AciiService } from '../acii/acii.service';
import { CreateContentService } from '../create/create.service';
import { EbtsImageData } from '../types/image-data';
import { ReviewContentService } from '../review/review.service';
import { HistoryService } from '../history/history.service';
import { BiqtContactService } from '../biqt-contact/biqt-contact.service';
import { PupilDilationToolService } from '../pupil-dilation-tool/pupil-dilation-tool.service';
import { DeformedImageData } from '../pupil-dilation-tool/pupil-dilation-response';
import { DualPDMService } from '../dual-pdm/dual-pdm.service';

//https://www.npmjs.com/package/angular4-color-picker

interface canvasConfig {
  width: number;
  height: number;

}

@Component({
  selector: 'app-iris-display',
  templateUrl: 'iris-display.component.html',
  styleUrls: ['./iris-display.component.css'],
  providers: [
    ToolManagerService // Creates an isolated instance of ToolManagerService
  ],
})
export class IrisDisplayComponent implements OnInit, OnDestroy {

  @Input() iris: HTMLImageElement;    // TypeScript doesn't like "Image"
  @Input() unrollImagePath = '';
  @Input() unrollRingData: object;
  @Input() displayDrawColor = '#127bdc';
  @Input() displayFillColor = '#333333';
  // These are the original annotations associated with the image. You should NEVER empty this array since this holds the annotations from the last saved point. If you want to reset the image, set the actualAnnotations to this array, and if you want to clear the image, set the actualAnnotations to empty.
  @Input('annotations') originalAnnotations: Array<Annotation>;
  @Input() tshepiiHidden: boolean;
  @Input() showOriginal: BehaviorSubject<boolean>;
  @Input() showUnrolled: BehaviorSubject<boolean>;
  @Input() isVertical: BehaviorSubject<boolean>;
  @Input() saved: Subject<any>;
  @Input() pastStates: State[];
  @Input() currState: State;
  @Input() isReview = false;
  @Input() sourceName: string;
  @Input() currentImageData: EbtsImageData;
  @Input() loadedCandidateImage: any;
  @Input() loadedProbeImage: any;

  @Output('resetIris') resetEmitter = new EventEmitter<string>(true);
  @Output('clear') clearEmitter = new EventEmitter<string>(true);
  @Output() displayInit: EventEmitter<any> = new EventEmitter<any>(true);
  @Output('irismousemove') mouseMove: EventEmitter<any> = new EventEmitter<any>(true);
  @Output('irisAnnotationsRequest') irisAnnotationRequest: EventEmitter<any> = new EventEmitter<any>(true);
  @Output('updateUnroll') updateUnrollEmitter: EventEmitter<any> = new EventEmitter<any>(true);

  @ViewChild('annotationButton', {static: true}) annotationButton: ElementRef;
  @ViewChild('originalCanvas') originalCanvas: ElementRef;
  @ViewChild('unrollCanvas') unrollCanvas: ElementRef;
  @ViewChild('drawer') drawer: MatDrawer;
  @ViewChild('annotationComp') annotationComp: AnnotationComponent;
  @ViewChild('historyComp') historyComp: AnnotationHistoryComponent;
  @ViewChild('historyButton', {static: true}) historyButton: ElementRef;
  @ViewChild('irisAnnotationButton', {static: true}) irisAnnotationButton: ElementRef;

  tshepiiAnnotations = [];
  annotationStartX = 0;
  annotationStartY = 0;

  historyStartX = 0;
  historyStartY = 0;
  pdmModifiedId = undefined;
  allServicesErrorOrDisabled = false;
  private _irisAnnotationServiceSubscription: Subscription[] = [];

  id: string;
  config: any;
  canvas: canvasConfig = {       // Canvas Configuration
    width: 0,
    height: 0
  };

  rectCanvas: canvasConfig = {       // Canvas Configuration
    width: 0,
    height: 0,
  };

  rectHeight = 80;
  Unroll = UnrollStatus;        // binding to use in template logic

  tools: any[] = [];
  // Need this to display the name correctly in the template
  allServicesButTshepii: IwpServiceStatus[] = [];
  tshepiiAndAnnotationServiceOnly: IwpServiceStatus[] = [];

  serviceSelected: IwpServiceStatus[] = [];
  imageTools: any[] = [];
  annotationTools: any[] = [];
  iconMap: Record<string, string>;
  activeTool: string;

  inPdmMode: boolean = false;
  private _pdmResponseSubscription: Subscription;
  public pdmMapper: Map<String, DeformedImageData> = new Map<String, DeformedImageData>();

  private _DualPDMResponseSubscription: Subscription;


  // Transparent image pixel to use as ghost image on draggable
  TRANSPARENT_IMG_SRC = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

  differ: IterableDiffer<any>;
  private _manualAnnotationSubscription: Subscription;
  public toolChange$: Subject<string> = new Subject<string>();
  // Used to know which automated annotation response to use when pulling the automated annotations.
  currentAutomatedAnnotationId: string;

  public static COUNT = 0;

  private currUser;
  private isVerticalValue = false;
  private fromShowOriginalClick: boolean = false;

  public isToolBarActive = false;
  public haveAnnotationsReturned = false;
  public havePdmResultsReturned = false;
  public pdmResubmit = false;

  public pdmGroupAnnotation: Annotation = undefined;

  _tshepiiResponse: TshepiiResponse;
  _biqtResponseSubscription: Subscription;
  _contactResponseSubscription: Subscription;
  _aciiResponseSubscription: Subscription;
  _automatatedAnnotationResponseSubscription: Subscription;
  isLoadedReview: boolean;

  _showAnnotations = false;

  get showAnnotations() {
    return this._showAnnotations;
  }

  set showAnnotations(s: boolean) {
    this._showAnnotations = s;
  }

  get serviceStatusStatusType(): typeof ServiceStatusStatusType {
    return ServiceStatusStatusType;
  }

  get serviceStatusNameType(): typeof ServiceStatusNameType {
    return ServiceStatusNameType;
  }

  _showHistory = false;
  get showHistory() {
    return this._showHistory;
  }

  set showHistory(s: boolean) {
    this._showHistory = s;
  }

  get annotations(): LinkedMap<Annotation> {
    return this.toolManager.annotations;
  }

  constructor(private contentSvc: ContentService,
              public toolManager: ToolManagerService,
              private toolRegistry: IrisToolRegistry,
              private _differs: IterableDiffers,
              private _cdr: ChangeDetectorRef,
              private profileService: ProfileService, public modalService: MatDialog,
              private aciiService: AciiService,
              private annotationService: IrisAnnotationService,
              private biqtContactService: BiqtContactService,
              private tshepiiService: TshepiiService,
              private dualPDMService: DualPDMService,
              public createService: CreateContentService,
              public reviewService: ReviewContentService,
              public historyService: HistoryService,
              private serviceStatusService: ServiceStatusService,
              private irisAnnotationService: IrisAnnotationService, 
              private pdmToolService: PupilDilationToolService,
            ) {
    this.id = '' + IrisDisplayComponent.COUNT++;
    this.tshepiiHidden = true;
    const trackByFunc = (idx, item) => item.id;
    this.differ = this._differs.find([]).create(trackByFunc);
  }

  ngOnInit() {
    this.pdmModifiedId = this.currentImageData.ebtsImageId;
    this.currentAutomatedAnnotationId = this.currentImageData.ebtsImageId;
    this.currUser = this.profileService.getCurrentProfile();
    this.config = this.contentSvc.config;

    // Since create page doesn't' allow automated annotations, we only need to check the status of the annotation response on a review.
    if (this.isReview) {
      this.checkAnnotationsResponseStatus(this.currentImageData.ebtsImageId, false, this.currentAutomatedAnnotationId);
    }

    this.checkPdmResponseStatus(this.currentImageData.ebtsImageId, false, this.pdmModifiedId);
    if (this.isVertical) {
      this.isVertical.subscribe(v => this.isVerticalValue = v);
    }
    this.canvas.height = this.iris.height > this.config.maxCanvasHeight ? this.config.maxCanvasHeight : this.iris.height;

    this.rectCanvas.width = this.config.rectWidth || this.iris.width;
    this.rectCanvas.height = this.config.rectHeight || this.rectHeight;

    this.tools = this.config.tools || [];
    for (const t of this.tools) {
      if (t.type === IrisToolType.ANNOTATION.toString()) {
        this.annotationTools.push(t);
      } else if (t.type === IrisToolType.IMAGE.toString()) {
        this.imageTools.push(t);
      }
    }

    this.iconMap = this.toolRegistry.iconMap;

    this.activeTool = this.toolManager.activeTool;
    this.toolManager.sourceName = this.sourceName;

    this._manualAnnotationSubscription = this.toolManager.manualAnnotation.subscribe(i => {
      // sometimes manually triggering the change detection to fire is required
      this._cdr.detectChanges();        // fires change detection to detect the changes made by the ngDoCheck call
    });

    this.toolManager.toolChange$.subscribe((toolName) => {
      this.toolChange$.next(toolName);
    });

    this.saved.subscribe((newAnnotations) => {
      const arr = [];
      for (const anno of newAnnotations) {
        arr.push(this.parseAnnotation(anno));
      }

      this.originalAnnotations = arr;
      this.toolManager.originalAnnotations = this.originalAnnotations;
    });

  }

  toggleToolBar(val: boolean, fromDualPDM: boolean = false) {
    this.isToolBarActive = val;
    this.deactivateTool(fromDualPDM);
  }

  getIrisModified(): boolean {
    return this.toolManager.getIrisModified();
  }

  ngOnDestroy() {
    this._manualAnnotationSubscription.unsubscribe();   // prevents memory leaks
    this.stopAnnotationStatusPolling();
  }

  activateTool(name: string) {
    //this is to account for when the user doesn't wish to finish the unroll and
    //move onto another tool

    this.deactivateTool();

    this.activeTool = name;
    this.toolManager.disableTools = false;
  }

  deactivateTool(fromDualPDM: boolean = false) {
    if (this.activeTool != undefined) {
      this.toolManager.tempToolLayer.removeChildren();
    }
    this.toolManager.disableTools = true;
    delete this.activeTool;

    this.inPdmMode = false;
    // We don't want to deactivate it if we are just entering the dual pdm mode.
    if (!fromDualPDM) {
      this.reviewService._showDualPDMComparison = false;
    }
  }

  // Need this to set the variable fromShowOriginalClick to true so that the onCanvasInit knows whether to redraw the annotations or not.
  onShowOriginalClicked(event) {
    this.showOriginal.next(event.source.checked);
    this.fromShowOriginalClick = true;
  }

  onCanvasInit(canvas) {
    this.isLoadedReview = this.historyService.hasSavedReview || this.historyService.hasIrisSearchRequest;

    this.toolManager.initManager(canvas, this.iris, this.isVertical, this.fromShowOriginalClick);

    this.displayInit.emit();

    canvas.nativeElement.onmousemove = (event) => {
      this.mouseMove.emit(new Point(event.offsetX, event.offsetY));
    };

    // Apply the annotations from the last saved point
    if (this.originalAnnotations && this.originalAnnotations.length > 0) {

      for (let i = 0; i < this.originalAnnotations.length; i++) {
        if (!this.originalAnnotations[i].id) {
          this.originalAnnotations[i].reassignId();
        }
        const anno = this.parseAnnotation(this.originalAnnotations[i]);

        if (this.isLoadedReview && anno.text == 'Iris Annotation Service') {
          this.toolManager.pullAnnoButtonClicked = true;
        }

        if (anno.text == "Individual Pupil Dilation Service") {
          const child = anno.children.getFirst();
          this.applyPdmImageData(child.value.data["alphaScore"]);
        } else if (anno.text == "Dual Pupil Dilation Service") {
          const child = anno.children.getFirst();
          this.applyDualPdmImageData();
        } else {
          const [item, label, usr] = this.toolManager.drawAndLabelAnno(anno);
        }
        this.originalAnnotations[i] = anno;
      }
      
      this.toolManager.originalAnnotations = this.originalAnnotations.slice();

      // If the image is being reloaded from clicking on the Original button, don't reset the image since this will overwrite the toolManager._original image, rendering the clear useless. This is also needed for the annotation history list.
      if (!this.fromShowOriginalClick) {
        this.toolManager.reset(false);
      }

      if (this.pastStates && this.pastStates.length) {
        this.toolManager.states.past = this.pastStates;
        this.pastStates.every(state => {
          if (state.annotation.parentId == undefined) {
            if (state.annotation.text != 'Iris Annotation Service' && state.annotation.text != "Individual Pupil Dilation Service" && state.annotation.text != "Dual Pupil Dilation Service") {
              this.toolManager.userParentGroupId = state.annotation.id;
            } else {
              this.toolManager.userParentGroupId = undefined;
            }
            return true;
          } else {
            return false;
          }
        });
      } else {
        if (!this.isLoadedReview) {
          // Basically a workaround to existing annotations.
          // We can safely iterate through `originalAnnotations` without
          // recursion because grouping wasn't available before
          for (const anno of this.originalAnnotations) {
            const state = new State(anno, StateType.ADD);
            this.toolManager.states.past.push(state);
          }
          this.pastStates = this.toolManager.states.past;
          this.pastStates.every(state => {
            if (state.annotation.parentId == undefined) {
              if (state.annotation.text != 'Iris Annotation Service' && state.annotation.text != "Individual Pupil Dilation Service" && state.annotation.text != "Dual Pupil Dilation Service") {
                this.toolManager.userParentGroupId = state.annotation.id;
              }
              return true;
            } else {
              return false;
            }
          });
        }
      }

      if (this.currState && this.currState.annotation) {
        this.toolManager.states.curr = this.currState;
      }
    }

    if (this.unrollRingData != undefined && this.unrollRingData['innerCenterX'] != undefined) {
      this.toolManager.unrollStatus = UnrollStatus.INITIALIZING;

      // give the outerRing a chance to draw before performing the expensive operation of unrolling the Iris
      setTimeout(() => {
        const innerPoint = new Point(Number(this.unrollRingData['innerCenterX']), Number(this.unrollRingData['innerCenterY']));
        const outerPoint = new Point(Number(this.unrollRingData['outerCenterX']), Number(this.unrollRingData['outerCenterY']));

        const innerRing = new Path.Circle(innerPoint, Number(this.unrollRingData['innerRadius']));
        const outerRing = new Path.Circle(outerPoint, Number(this.unrollRingData['outerRadius']));

        innerRing['center'] = innerPoint;
        innerRing['radius'] = Number(this.unrollRingData['innerRadius']);
        innerRing.name = 'inner';

        outerRing['center'] = outerPoint;
        outerRing['radius'] = Number(this.unrollRingData['outerRadius']);
        outerRing.name = 'outer';

        this.toolManager.unrollImagePath = this.unrollImagePath;

        this.toolManager.drawUnrolledIris(innerRing, outerRing);
      }, 100);
    }

    // If this method is being called from clicking show/hide Original or from applying a pdm, we need to redraw the annotations and set fromShowOriginalClick back to false.
    if (this.fromShowOriginalClick) {
      this.toolManager.redrawAnnotations();
      this.fromShowOriginalClick = false;
    }
  }

  onRectCanvasInit(rectCanvas) {
    this.toolManager.bindRectCanvas(rectCanvas, this.isVertical);
  }

  reset_clear(isReset: boolean, isClear: boolean) {
    this.currentAutomatedAnnotationId = this.currentImageData.ebtsImageId;
    this.pdmModifiedId = this.currentImageData.ebtsImageId;
    if (isReset && !isClear) {
      this.toolManager.pullAnnoButtonClicked = false;
      if (this.isLoadedReview) {
        this.toolManager.selectedAnnotation = undefined;
        if (!!this.toolManager.originalAnnotations && this.toolManager.originalAnnotations.length < 0) {
          for (const annotation of this.toolManager.originalAnnotations) {
            if (annotation.text == 'Iris Annotation Service') {
              this.toolManager.pullAnnoButtonClicked = true;
              break;
            }
          }
        }
      }
      this.toolManager._pdmOriginal = null;
      this.toolManager._dualPdmOriginal = null;
      var pdmAnno = this.toolManager.reset(true);
      if (pdmAnno) {
        const child = pdmAnno.children.getFirst();
        this.applyPdmImageData(child.value.data["alphaScore"]);
      }
    } else if (!isReset && isClear) {
      this.toolManager.pullAnnoButtonClicked = false;
      this.toolManager._pdmOriginal = null;
      this.toolManager._dualPdmOriginal = null;

      this.toolManager.clear(true);
    }

    this.toolManager.hasUnrolled.pipe(
      take(1)
    ).subscribe(unrolled => {
      if (unrolled) {
        this.updateUnroll();
      }
    });

    if (!!this.toolManager.componentRef) {
      this.toolManager.componentRef.resetData();
    }
  }

  get hasCanvas(): boolean {
    return this.toolManager.initialized;
  }

  get hasUnrolled(): boolean {
    return this.toolManager.hasUnrolled.getValue();
  }

  exportPNG() {
    if (!this.toolManager.initialized) {
      return null;
    }
    const ret = new Raster(this.toolManager._original);
    ret.remove();

    return ret.toDataURL().split(',')[1];
  }

  setTshepii(tshepiiResponse: TshepiiResponse, colorString: string) {
    this._tshepiiResponse = tshepiiResponse;
    this.tshepiiAnnotations = [];
    this.drawTshepii(colorString);
  }

  drawTshepii(colorString: string) {
    const croppedCenter = new Point(this._tshepiiResponse.cropCenter.x, this._tshepiiResponse.cropCenter.y);
    const originalCenter = new Point(this._tshepiiResponse.originalIrisCenter.x, this._tshepiiResponse.originalIrisCenter.y);

    const vectorX = croppedCenter.x - originalCenter.x;
    const vectorY = croppedCenter.y - originalCenter.y;

    const ratio = (this._tshepiiResponse.originalIrisRadius / this._tshepiiResponse.cropRadius);

    const group = [];
    let index = 0;
    for (const crypt of this._tshepiiResponse.cryptList) {
      const anno = this.createPolygonFromTshepii(crypt['pointList'], vectorX, vectorY, originalCenter, ratio, colorString);
      if (anno) {
        this.tshepiiAnnotations.push(anno);
        this.toolManager.addItem(anno, false);
      }

      group.push({id: anno.id, index: index++});
    }

    this.toolManager.groupItems({no_parent: group}, false, 'Tshepii');

    // Unroll Image
    const innerCenter = new Point(this._tshepiiResponse.originalPupilCenter.x, this._tshepiiResponse.originalPupilCenter.y);
    const innerRing = new Path.Circle(
      innerCenter,
      this._tshepiiResponse.originalPupilRadius
    );

    innerRing['center'] = innerCenter;
    innerRing['radius'] = this._tshepiiResponse.originalPupilRadius;
    innerRing.name = 'inner';

    //set the outer ring
    const outerCenter = new Point(this._tshepiiResponse.originalIrisCenter.x, this._tshepiiResponse.originalIrisCenter.y);
    let outerRing = new Path.Circle(
      outerCenter,
      this._tshepiiResponse.originalIrisRadius
    );

    outerRing = outerRing;
    outerRing['center'] = outerCenter;
    outerRing['radius'] = this._tshepiiResponse.originalIrisRadius;
    outerRing.name = 'outer';

    this.toolManager.drawUnrolledIris(innerRing, outerRing);
    this.toolManager.redrawAnnotations();

  }

  toggleAnnotations(event: Event) {
    event.preventDefault();
    this.showHistory = !this.showHistory;
    if (this.showHistory) {
      this.drawer.open();
      this.toolManager.resizeWidth();
    } else {
      this.drawer.close();
      setTimeout(() => this.toolManager.resizeWidth(), 1200);
    }
  }

  get annotationStatus(): boolean {
    return this.haveAnnotationsReturned;
  }

  get pdmStatus(): boolean {
    return this.havePdmResultsReturned;
  }

  stopAnnotationStatusPolling() {
    this.irisAnnotationService.stopAnnotationStatusPolling();
    this._irisAnnotationServiceSubscription.forEach(subscription => {
      subscription.unsubscribe();
    })
  }

  checkAnnotationsResponseStatus(ebtsImageId: string, isResubmit: boolean,  modifiedImageId?: string): void {
    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.annotation)) {
      return;
    }

    this.irisAnnotationService.requestIrisAnnotationResponse(ebtsImageId, isResubmit, modifiedImageId);

    const pendingTimer = this.createService.setPendingTimer(isResubmit, ServiceStatusNameType.annotation);

    this._automatatedAnnotationResponseSubscription = this.irisAnnotationService.annotationResponse$.subscribe(
      annotationResponse => {
        this.createService.clearPendingTimer(annotationResponse.status, pendingTimer);
        
        if (annotationResponse.status == ServiceStatus.Received) {
          this.haveAnnotationsReturned = true;
          this.toolManager.pullAnnoButtonClicked = false;
      
          // Need to remove the old Automated Annotations if there are any.
          this.removeAutoAnnos();
        
        }

        // If it's resubmit, we should show the annotations right away.
        if (annotationResponse.isResubmit) {
          this.toolManager.addIrisAnnotationResponse(annotationResponse);
        }

        this._automatatedAnnotationResponseSubscription.unsubscribe();
        this.createService.showSnackBar(annotationResponse.isResubmit, false, ServiceStatusNameType.annotation);
      }
    );
  }

  toggleComparison(annoVisible: boolean, mouseVisible: boolean, tempVisible: boolean, tshepiiVisible: boolean) {
    this.toolManager.userGroup.visible = annoVisible;
    this.toolManager.labelLayer.visible = annoVisible;
    this.toolManager.mouseOverLayer.visible = mouseVisible;
    this.toolManager.tempToolLayer.visible = tempVisible;
    this.toolManager.generatedGroup.visible = tshepiiVisible;
  }

  adjustPointToCenter(pointToAdjust: IwpPoint, vectorX: number, vectorY: number, originalCenter: IwpPoint, ratio: number): IwpPoint {
    const tempPoint = new IwpPoint(pointToAdjust.x - vectorX, pointToAdjust.y - vectorY);

    if (ratio == 1) {
      return tempPoint;
    }

    const newVectorX = (tempPoint.x - originalCenter.x) * (1 - ratio);
    const newVectorY = (tempPoint.y - originalCenter.y) * (1 - ratio);

    const returnPoint = new IwpPoint(tempPoint.x - newVectorX, tempPoint.y - newVectorY);

    return returnPoint;
  }

  createPolygonFromTshepii(pointList: IwpPoint[], vectorX: number, vectorY: number, originalCenter: IwpPoint, ratio: number, colorString: string): Annotation {
    if (pointList.length > 1) {
      const annotation = new Annotation('Polygon', AnnotationType.POLYGON, PolygonTool.icon);
      annotation.isSystemGenerated = true;
      const points = [];
      for (const point of pointList) {
        const adjustPoint = this.adjustPointToCenter(point, vectorX, vectorY, originalCenter, ratio);
        points.push({x: adjustPoint.x, y: adjustPoint.y});
      }

      points.push(this.adjustPointToCenter(pointList[0], vectorX, vectorY, originalCenter, ratio));
      
      annotation.value = {
        points,
        data: {
          strokeColor: colorString,
          fillColor: colorString,
          strokeWidth: 1,
          fillAlpha: 0.1
        }
      };

      return annotation;
    }

    return undefined;
  }

  createPointFromTshepii(point: IwpPoint, radius: number): Annotation {
    const paperPoint = new Point(point.x, point.y);
    const paperPointArray = Point[1];
    paperPointArray.push(paperPoint);

    const annotation = new Annotation('Point', AnnotationType.POINT, PointTool.icon);
    annotation.isSystemGenerated = true;
    annotation.value = {
      points: paperPointArray,
      data: {
        radius,
        strokeColor: this.toolManager.drawColor,
        strokeWidth: 1,
        fillColor: this.toolManager.fillColor
      }
    };

    return annotation;
  }

  createCircleFromTshepii(point: IwpPoint, radius: number): Annotation {
    if (radius > 0) {
      const annotation = new Annotation('Circle', AnnotationType.CIRCLE, CircleTool.icon);
      annotation.isSystemGenerated = true;
      annotation.value = {
        points: [new Point(point.x, point.y)],
        data: {
          radius,
          strokeColor: this.toolManager.drawColor,
          strokeWidth: this.toolManager.strokeWidth
        }
      };

      return annotation;
    }

    return undefined;
  }

  exportFlattenedPNG(isSaveCreateImage: boolean, specificToolManager?: ToolManagerService) {
    // If there isn't a specific ToolManager we should use, we default to the current ToolManager (this.toolManager)
    let toolManager;
    if (!!specificToolManager) {
      toolManager = specificToolManager;
    } else {
      toolManager = this.toolManager;
    }

    if (!toolManager.initialized) {
      return null;
    }

    this.toggleComparison(true, false, false, false);
    toolManager.showGhostCursor = false;

    // Flatten layers
    const imageLayer = toolManager.project.layers[0].clone({insert: false, deep: true});

    for (let i = 1; i < toolManager.project.layers.length; i++) {
      const subLayer = toolManager.project.layers[i];
      if (subLayer.name == 'annotation') {
        const subChildren = subLayer.children.length;

        for (let child = 0; child < subChildren; child++) {
          imageLayer.addChild(subLayer.children[child].clone({insert: false, deep: true}));
        }
      break;
      }
    }

    const ret = imageLayer.rasterize();
    const url = ret.toDataURL().split(',')[1];

    ret.remove();

    this.toggleComparison(true, true, true, true);
    toolManager.showGhostCursor = true;

    return url;
  }

  exportUnrollPNG() {
    if (!this.toolManager.initialized) {
      return null;
    }

    if (this.toolManager.unrolledRaster == null) {
      return '';
    }

    const imageLayer = this.toolManager._rect.layers['unrolled-items'].clone({insert: false, deep: true});

    for (const subLayer of this.toolManager._rect.layers) {
      if (!(subLayer.name == 'ghost-cursor')) {
        const subChildren = subLayer.children.length;

        for (let child = 0; child < subChildren; child++) {
          const childAnno = subLayer.children[child].clone({insert: false, deep: true});
          imageLayer.addChild(childAnno);
        }
      }
    }

    const ret = imageLayer.rasterize(this.toolManager._rect.view.resolution);

    const url = ret.toDataURL().split(',')[1];
    ret.remove();

    return url;

    //return this.toolManager.unrolledRaster.toDataURL().split(',')[1];
  }

  exportAnnotations() {
    if (!this.toolManager.initialized) {
      return null;
    }

    const arr = [];
    for (const anno of this.annotations) {
      arr.push(this.exportAnnotation(anno));
    }

    return arr;
  }

  exportPastStates() {
    if (!this.toolManager.initialized ||
      this.toolManager.states.past.length <= 0) {
      return null;
    }

    // PROBLEM: state.children is an Object instead of LinkedMap
    const arr = [];
    for (const state of this.toolManager.states.past) {
      arr.push(this.exportState(state));
    }

    return arr;
  }

  exportCurrState() {
    if (!this.toolManager.initialized ||
      !this.toolManager.states.curr) {
      return null;
    }

    const copy = this.exportState(this.toolManager.states.curr);
    return copy;
  }

  changeStrokeWidth(width) {
    this.toolManager.strokeWidth = width;
  }

  //activate the drawing canvas when you enter the dom element
  @HostListener('mouseenter', ['$event'])
  onMouseEnter(e) {
    if (this.toolManager.initialized) {
      this.toolManager.project.activate();
    }
  }


  @HostListener('mousewheel', ['$event'])
  onMousewheel(event) {
    if (this.toolManager.initialized && this.toolManager.canZoomImage()) {
      this.toolManager.zoom(event);
      return false;
    }
    return true;
  }

  @HostListener('DOMMouseScroll', ['$event'])
  onMouseWheelFirefox(event: any) {
    if (this.toolManager.initialized && this.toolManager.canZoomImage()) {
      this.toolManager.zoom(event);
      return false;
    }
    return true;
  }

  @HostListener('onmousewheel', ['$event'])
  onMouseWheelIE(event: any) {
    if (this.toolManager.initialized && this.toolManager.canZoomImage()) {
      this.toolManager.zoom(event);
      return false;
    }
    return true;
  }

  @HostListener('dragover', ['$event'])
  onDragOver(event: any) {
    event.preventDefault();
    return false;
  }

  showAnnotationColor(isShow: boolean) {
    this.toolManager.drawAnnotationLabels = isShow;
    this.toolManager.redrawAnnotations();
  }

  updateUnroll() {
    this.updateUnrollEmitter.emit();
    this.toolManager.unrollStatus = this.Unroll.INITIALIZING;
    setTimeout(() => {
      this.toolManager.drawUnrolledIris(this.toolManager.unrollInnerRing, this.toolManager.unrollOuterRing);
      this.toolManager.unrollStatus = this.Unroll.RECT_DRAWN;
    }, 100);
  }

  private expandOriginalIndicator;
  private expandOriginalStartY = 0;
  private expandOriginalDown = false;

  private expandUnrolledIndicator;
  private expandUnrolledStartY = 0;
  private expandUnrolledDown = false;

  onExpandOriginalDrag(event) {
    event.preventDefault();
    if (this.expandOriginalDown) {
      const yChange = event.clientY - this.expandOriginalStartY;

      this.toolManager.adjustCanvasHeight(yChange);
      this.expandOriginalStartY = event.clientY;
    }
  }

  onExpandOriginalDragStart(event) {
    this.expandOriginalIndicator = event.srcElement;
    this.expandOriginalIndicator.style.color = '#dddddd';
    this.expandOriginalStartY = event.clientY;
    this.expandOriginalDown = true;

    const img = new Image();
    img.src = this.TRANSPARENT_IMG_SRC;
    event.dataTransfer.setDragImage(img, 0, 0);
  }

  onExpandOriginalEnd(event) {
    event.preventDefault();
    this.expandOriginalDown = false;

    if (this.expandOriginalIndicator) {
      this.expandOriginalIndicator.style.color = 'black';
    }
  }

  onExpandUnrolledDrag(event) {
    event.preventDefault();
    if (this.expandUnrolledDown) {
      const yChange = event.clientY - this.expandUnrolledStartY;

      this.toolManager.adjustRectHeight(yChange);
      this.expandUnrolledStartY = event.clientY;
    }
  }

  onExpandUnrolledDragStart(event) {
    this.expandUnrolledIndicator = event.srcElement;
    this.expandUnrolledIndicator.style.color = '#dddddd';
    this.expandUnrolledStartY = event.clientY;
    this.expandUnrolledDown = true;

    const img = new Image();
    img.src = this.TRANSPARENT_IMG_SRC;
    event.dataTransfer.setDragImage(img, 0, 0);
  }

  onExpandUnrolledEnd(event) {
    event.preventDefault();
    this.expandUnrolledDown = false;

    if (this.expandUnrolledIndicator) {
      this.expandUnrolledIndicator.style.color = 'black';
    }
  }

  undo() {
    this.toolManager.undoAnnotation();
  }

  redo() {
    this.toolManager.redoAnnotation();
  }

  /*
   * A little sketchy way to recursively "clone" annotations.
   * Basically, the problem is that we can't pass LinkedMap to
   * the server because `JSON.stringify()` is unable to serialize
   * circular references (caused by prev/next pointers).
   * We *could* use `flatted` to serialize circular references, but
   * deserializing it on the server side with Spring is a little
   * more complicated than I thought...
   * So, we are trying to recursively convert the LinkedMap to
   * a plain old Array and store it into a dummy attribute, `tempChildren`
   * and null-out the `children` attribute.
   * But we need to deep clone the annotations because we don't actually
   * want `children` to be null.
   */

  private exportAnnotation(anno: Annotation) {
    // Iterate thru attributes of `Annotation`
    const copy = Object.keys(anno)
      .reduce((obj, key) => {
        const arr = [];

        // If we encounter the `children` field, convert it to an Array.
        // Also, recursively clone all children Annotations
        if (key === 'children') {
          const children = anno['children'].toList();

          for (const child of children) {
            arr.push(this.exportAnnotation(child));
          }
          return {
            ...obj,
            tempChildren: arr
          };
        } else if (key === 'tempChildren') {
          // Because we set `tempChildren` when we encounter `children`,
          // just skip setting the field.
          return {
            ...obj
          };
        }

        // Set the current key and value in the clone.
        return {
          ...obj,
          [key]: anno[key]
        };
      }, {});
    return copy;
  }

  private exportState(state: State) {
    const self = this;
    const _export = function myself(st: State): any {
      const copy = Object.keys(st)
        .reduce((obj, key) => {
          if (key === 'annotation') {
            return {
              ...obj,
              annotation: self.exportAnnotation(st.annotation)
            };
          }
          return {
            ...obj,
            [key]: st[key]
          };
        }, {});
      return copy;
    };

    let json;
    switch (state.type) {
      case StateType.BULK_REMOVE:
        json = _export(state);
        Object.keys(json.data['revertTo']).forEach(key => {
          const children = json.data['revertTo'][key];
          for (const child of children) {
            child['annotation'] = this.exportAnnotation(child['annotation']);
          }
        });
        return json;
      case StateType.RESET:
      case StateType.CLEAR:
        // state.data['reverTo'] contains annotation array
        json = _export(state);
        for (let i = 0; i < json.data['revertTo'].length; i++) {
          const anno = json.data['revertTo'][i];
          json.data['revertTo'][i] = this.exportAnnotation(anno);
        }
        return json;
      case StateType.ADD:
      case StateType.REMOVE:
      case StateType.GROUP_ONLY:
      case StateType.GROUP_ADD:
        return _export(state);
      default:
        return null;
    }
  }

  private parseAnnotation(json: any) {
    const anno = MapUtils.deserialize(Annotation, json);

    const arr = [];
    if (anno.tempChildren) {
      for (const child of anno.tempChildren) {
        arr.push({
          key: child.id,
          value: this.parseAnnotation(child)
        });
      }
    }

    if (arr.length > 0) {
      anno.children = new LinkedMap<Annotation>(arr);
    }
    anno.tempChildren = null;

    // This is here because annoIcon isn't saved to the db,
    // therefore when a past review/create is pulled up,
    // the icons aren't populated
    if (!anno.annoIcon) {
      anno.annoIcon = ANNO_ICON_MAPPING[anno.type];
    }

    return anno;
  }

  pullAnnotations(modifiedImageId?: string) {
    // Automatically assume that imageId is the ebtsImageId
    let imageId = this.currentImageData.ebtsImageId;
    // But if there is a modifiedImageId coming in, we should set the imageId to that instead.
    if (!!modifiedImageId) {
      imageId = modifiedImageId;
    } 
    
    this.irisAnnotationService.getIrisAnnotationsResponse(imageId, false).subscribe(
    irisAnnotations => {
      this.toolManager.addIrisAnnotationResponse(irisAnnotations);
      }
    );
    
  }

  resubmitButtonClicked(modal: TemplateRef<any>) {
    this.tshepiiAndAnnotationServiceOnly = [];
    this.allServicesButTshepii = [];
    this.serviceSelected = [];
    this.modalService.open(modal);
    this.getListOfServices();
  }

  getListOfServices(): void {
    let services = [];
    this.allServicesButTshepii = [];
    this.tshepiiAndAnnotationServiceOnly = [];
    let errorOrDisabledServices = 0;
    
    services = this.serviceStatusService.getNonWebServices();
    services.forEach(service => {
      if (service.status == this.serviceStatusStatusType.Error || service.status == this.serviceStatusStatusType.Disabled) {
        errorOrDisabledServices++;
      }
      if (service.name != this.serviceStatusNameType.tshepii && service.name != this.serviceStatusNameType.annotation) {
        this.allServicesButTshepii.push(service);
      } else {
        this.tshepiiAndAnnotationServiceOnly.push(service);
      }
    });
    if (errorOrDisabledServices == 5) {
      this.allServicesErrorOrDisabled = true;
    }
  }

  addServiceToSelected(service: IwpServiceStatus): void {
    if (this.isReview || service.name != ServiceStatusNameType.tshepii) {
      if (service.status == this.serviceStatusStatusType.Online) {
        const index = this.serviceSelected.findIndex(serviceAdded => serviceAdded.name == service.name);
        if (index != -1) {
          this.serviceSelected.splice(index, 1);
        } else {
          this.serviceSelected.push(service);
        }
      }
    }
  }

  resubmitImage(isReview: boolean): void {
    if (!isReview) {
      this.resubmitCreateImage();
    } else {
      this.resubmitReviewImage();
    }
    this.modalService.closeAll();
  }

  resubmitCreateImage(): void {
    this.serviceSelected.forEach(service => {
      let imageData = this.exportFlattenedPNG(false);

      switch (service.name) {
        case ServiceStatusNameType.acii:
          this.aciiService.getAciiSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;
            if (modifiedImageId != "-1") {
              this.createService.getAutoEyeLabel(this.currentImageData.ebtsImageId, true, modifiedImageId);
            }
          })
          break;

        case ServiceStatusNameType.biqt:
          this.biqtContactService.getBiqtContactSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;
            if (modifiedImageId != "-1") {
              this.createService.getIrisImageQualityAndContact(this.currentImageData.ebtsImageId, true, modifiedImageId);
            }
          });
          break;
        
        case ServiceStatusNameType.pdm:
          this.pdmToolService.getPdmSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;
            if (modifiedImageId != "-1") {
              this.pdmModifiedId = modifiedImageId
              this.pdmResubmit = true;
              this.triggerPdmMode(true);
            }
          });
          break;
      }
    });
  }

  resubmitReviewImage(): void {
    this.serviceSelected.forEach(service => {
      let imageData = this.exportFlattenedPNG(false);

      switch (service.name) {
        case ServiceStatusNameType.acii:
          this.aciiService.getAciiSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;

            if (modifiedImageId != "-1") {
              this.reviewService.getEyeLabel(this.currentImageData.ebtsImageId, true, modifiedImageId);
            }
          })
          break;
  
        case ServiceStatusNameType.annotation:
          this.toolManager.pullAnnoButtonClicked = true;
          this.annotationService.getAnnotationSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;

            if (modifiedImageId != "-1") {
              this.currentAutomatedAnnotationId = modifiedImageId;
              this.checkAnnotationsResponseStatus(this.currentImageData.ebtsImageId, true, this.currentAutomatedAnnotationId);
            }
          });
          break;

        case ServiceStatusNameType.biqt:
          this.biqtContactService.getBiqtContactSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;
            if (modifiedImageId != "-1") {
              this.reviewService.getIrisImageQualityAndContact(this.currentImageData.ebtsImageId, true, modifiedImageId);
            }
          });
          break;

        case ServiceStatusNameType.pdm:
          this.pdmToolService.getPdmSnapshot(imageData, this.currentImageData.ebtsImageId).subscribe((imageId) => {
            let modifiedImageId = imageId;

            if (modifiedImageId != "-1") {
              this.pdmModifiedId = modifiedImageId
              this.pdmResubmit = true;
              this.triggerPdmMode(true);
            }
          });
          break;
  
        case ServiceStatusNameType.tshepii:
          // Check to make sure that there is a probe and candidate image selected
          if (this.isReview && this.loadedProbeImage.image != null && this.loadedCandidateImage.image != null) {
            // Get both the probe and candidate's toolManager
            const reviewSvc = (<ReviewContentService>this.contentSvc);
            const probeInstance = reviewSvc.cache(this.loadedProbeImage.activeUuid).instance;
            const candidateInstance = reviewSvc.cache(this.loadedCandidateImage.activeUuid).instance;

            // Get the annotations that are currently applied to the probe and candidate's images
            let annotations = reviewSvc.getAnnotationsFromCache();
            // Remove the Tshepii parts of the annotations
            this.removeTshepii(annotations[this.loadedProbeImage.activeUuid], annotations[this.loadedCandidateImage.activeUuid], probeInstance, candidateInstance);
            // We need to pull the annotations again now that we removed the Tshepii parts.
            annotations = reviewSvc.getAnnotationsFromCache();

            // Get the image data for both probe and candidate
            let tempProbeData = this.exportFlattenedPNG(false, probeInstance.toolManager)
            let tempCandidateData = this.exportFlattenedPNG(false, candidateInstance.toolManager)
            // Submit the images with their new annotations to get their Tshepii Responses.
            this.tshepiiService.getTshepiiSnapshot(tempProbeData, tempCandidateData, this.loadedProbeImage.ebtsImageId, this.loadedCandidateImage.ebtsImageId).subscribe((imageId) => {
              let modifiedProbeId = imageId[0];
              let modifiedCandidateId = imageId[1];
              if (modifiedProbeId != "-1" && modifiedCandidateId != "-1") {
                this.reviewService.setComparisonMode(!this.reviewService._showTshepiiComparison, this.loadedProbeImage, this.loadedCandidateImage, this.contentSvc);
                if (this.reviewService._showTshepiiComparison){
                  const reviewSvc = (<ReviewContentService>this.contentSvc);
                  let annotations = reviewSvc.getAnnotationsFromCache();
                  // Get the tshepii comparison response
                  this.reviewService.getTshepii(this.loadedProbeImage, this.loadedCandidateImage, this.contentSvc, true, annotations, modifiedProbeId, modifiedCandidateId);
                }
              }
            });
          }
          break;
        }
      });
    }

  removeTshepii(probeAnnos: object, candidateAnnos: object, probeInstance: any, candidateInstance: any): void {
    // Remove the annotations from the viewable image
    this.removeImageTshepiiAnnotations(probeAnnos, probeInstance);
    this.removeImageTshepiiAnnotations(candidateAnnos, candidateInstance);

    // Remove the annotations from the review instance that gets sent back to the tshepii service
    this.removeInstanceTshepiiAnnotations(probeInstance);
    this.removeInstanceTshepiiAnnotations(candidateInstance);
  }

  removeImageTshepiiAnnotations(annos: object, instance: any):void {
    for (let i in annos) {
      let anno = annos[i];
      if (anno.text == "Tshepii") {
          instance.toolManager.removeTshepiiItems();
          instance.toolManager.removeTshepiiHighlights();
          instance.toolManager.removeItem(anno.id, true);
      }
    }
  }

  removeInstanceTshepiiAnnotations(instance: any): void {
    instance.tshepiiAnnotations.forEach(anno => {
      instance.toolManager.removeItem(anno.id, true);
    })
  }

  removeAutoAnnos(): void {
    // Check if there are any automated annotations in the first place
    if (this.annotations.size == 0) {
      return;
    } else {
      for (let anno of this.annotations) {
        if (anno.text == "Iris Annotation Service") {
          this.toolManager.removeItem(anno.id, false);
        }
      }
    }

  }

  cancelResubmission(): void {
    this.modalService.closeAll();
  }

  triggerPdmMode(isResubmit: boolean=false): void {
    this.pdmGroupAnnotation = undefined;
    if (!isResubmit) {
      // Disable everything while in this mode
      this.toggleToolBar(true, false)
      this.inPdmMode = true;
    }
    
    if (isResubmit) {
      this.havePdmResultsReturned = false
    }

    if (this.serviceStatusService.isServiceDisabled(ServiceStatusNameType.pdm)) {
      return;
    }

    this.checkPdmResponseStatus(this.currentImageData.ebtsImageId, isResubmit, this.pdmModifiedId);
  }

  // Preps getting the responses for both the individual pdm service and dual pdm service. 
  getPdmResponses(pdmId: string, pdmType: AnnotationType, pdmIcon: string, iconType: string, isDualPdm: boolean, alphaScore?: string) {
    // Creating an annotation label for the history
    var currentAppliedPdm = new Annotation(pdmId, pdmType, pdmIcon, iconType);

    var groupText = "Dual Pupil Dilation Service";
    if (!isDualPdm && !!alphaScore) {
      groupText = "Individual Pupil Dilation Service";
    }

    // Check to see if there is already a deformed image applies to the canvas and remove it
    var removedPdm = this.removeAppliedPdm(currentAppliedPdm, groupText);
    let isPdmGroup = removedPdm[0];
    let beforeId = removedPdm[1];
    currentAppliedPdm = removedPdm[2];
    let pdmParentGroup = removedPdm[3];

    if (!isDualPdm && !!alphaScore) {
      this.applyPdmImageData(alphaScore);
    } else {
      this.applyDualPdmImageData();
    }
    
    currentAppliedPdm.isSystemGenerated = true;

    if (!isDualPdm && !!alphaScore) {
      currentAppliedPdm.value = {
        points: [],
        data: {
          alphaScore: alphaScore
        }
      };
    } else {
      currentAppliedPdm.value = {
      points: [],
      data: {}
    };
    }

    // Creating pdm's own group under the history tab
    if (isPdmGroup) {
      this.toolManager.checkingUserCreatedAnnotations(currentAppliedPdm, !currentAppliedPdm.isSystemGenerated, null, beforeId, pdmParentGroup.id, null, this.pdmResubmit);
      
    } else if (!this.toolManager.annotationExists(currentAppliedPdm.id)) {
      const pdmGroup = [];
      pdmGroup.push({id: currentAppliedPdm.id, index: 0});
      this.toolManager.addItem(currentAppliedPdm, !currentAppliedPdm.isSystemGenerated);
      pdmParentGroup = this.toolManager.groupItems({no_parent:pdmGroup}, false, groupText);
    }

    this.pdmGroupAnnotation = pdmParentGroup;

  }

  removeAppliedPdm(currentAppliedPdm: Annotation, groupText: string): [boolean, string, Annotation, Annotation] {
    var isPdmGroup = false;
    var beforeId: string = null;
    var pdmParentGroup: Annotation = null;
    const currentGroups = this.toolManager.getAnnotations(AnnotationType.GROUP);
    currentGroups.forEach(group => {
      if (group.text == groupText) {
        isPdmGroup = true;
        pdmParentGroup = group;
        if (group.children.size > 0) {
          currentAppliedPdm = group.children.getFirst();
          beforeId = this.toolManager.preparingRemoval(currentAppliedPdm)[1];
          this.toolManager.removeItem(currentAppliedPdm.id, false, false, true)
        }
      }
    });
    return [isPdmGroup, beforeId, currentAppliedPdm, pdmParentGroup];
  }

  applyPdmImageData(alphaScore: string) {
    this._pdmResponseSubscription = this.pdmToolService.getPdmResponse(this.pdmModifiedId, alphaScore).subscribe(async deformedImageBytes => {
      var deformedImageResults = await this.pdmToolService.convertImageBytesToImage(deformedImageBytes);
      this.setIrisData(deformedImageResults.img);
      this._pdmResponseSubscription.unsubscribe();
    });
  }

  applyDualPdmImageData() {
    this._DualPDMResponseSubscription = this.dualPDMService.getDualPDMResponseImage(this.pdmModifiedId).subscribe(
        async (dualPDMResponseImage) => {
            try {
                const results = await this.dualPDMService.prepImage(dualPDMResponseImage);
                this.setIrisData(results.img, true);
                this._DualPDMResponseSubscription.unsubscribe();
            } catch (error) {
                console.error("Error preparing and displaying image:", error);
            }
        },
        (error) => {
            console.error("Error fetching Dual PDM Response Image:", error);
        });
  }


  setIrisData(img: HTMLImageElement, setDualPdmOriginal: boolean = false) {
    this.iris = img;
    // If we are just displaying a dual pdm image, don't set the _pdmOriginal. Normally, we should set it though, so the default will be true.
    if (!setDualPdmOriginal) {
      this.toolManager._pdmOriginal = this.iris;
    } else {
      this.toolManager._dualPdmOriginal = this.iris;
    }

    // need to reload the canvas since it has new image data
    // If it's a modified image, we only want to redraw the pdm since it compressed the image already.
    if (this.currentImageData.ebtsImageId != this.pdmModifiedId) {
      this.toolManager.redrawAnnotations(true);
    } else {
      this.toolManager.redrawAnnotations(false);
    }
  }

  checkPdmResponseStatus(ebtsImageId: string, isResubmit: boolean, modifiedImageId?: string): void{
    let imageId = ebtsImageId;
    if (!!modifiedImageId) {
      imageId = modifiedImageId;
    } else {
      modifiedImageId = '-1';
    }

    this.pdmToolService.startPdmResponsePolling(ebtsImageId, modifiedImageId);
    const serviceStatus = ServiceStatus;
    this.pdmToolService._pdmStatusSubscription[imageId] =this.pdmToolService.pdmResponseStatus$.subscribe(
      data => {
        switch (data) {
          case serviceStatus.Error: {
            this.pdmToolService.stopPdmResponsePolling(imageId);
            break;
          }
          // If the status is receieved for both images, we can submit an alpha score to get the corresponding PDM Mapper
          case serviceStatus.Received: {
            this.pdmToolService.stopPdmResponsePolling(imageId);
            this.setPdmRelatedChecks(ebtsImageId);
            this.createService.showSnackBar(isResubmit, false, ServiceStatusNameType.pdm);
            break;
          }
          case serviceStatus.Pending: {
            break;
          }
          case serviceStatus.NotSent: {
            break;
          }
          default:
            console.log('Pdm: No Status Yet');
            this.pdmToolService.stopPdmResponsePolling(imageId);
            break;
        }
      });
  }

  // Sets the booleans logic for all things related to pdm
  setPdmRelatedChecks(imageId: string): void {
    this.havePdmResultsReturned = true;
    this.pdmResubmit = false;

    const currProbe = this.reviewService.probe;
    const currCandidate = this.reviewService.candidate;

    if (currProbe.ebtsImageId == imageId) {
      this.reviewService.probe.hasPdmResultsReturned = true;
    } else if (currCandidate.ebtsImageId == imageId) {
      this.reviewService.candidate.hasPdmResultsReturned = true;
    }
  }

}
