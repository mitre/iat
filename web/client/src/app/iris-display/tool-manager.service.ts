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
import { ProfileService } from '../profile/profile.service';
import { Color, Group, Item, Layer, Path, Point, PointText, Project, Raster, Rectangle, Size } from 'paper';
import { Paper } from '../utils/paper.service';
import { IrisDrawingTool } from './tools/tool.base';
import { BehaviorSubject, Subject } from 'rxjs';
import { Annotation, AnnotationType } from '../types/annotation';
import { LinkedMap } from '../types/linked-map';
import { State, StateType } from '../types/state';
import { CoreFunctions } from './core/core-functions';

import { IrisAnnotationResponse } from '../irisannotation/iris-annotation-response';

/**
 * Tool Manager CANNOT inject the ToolRegistryService, it will cause a circular dependency
 */

export enum UnrollStatus {
  UNINITIALIZED,
  INITIALIZING,
  RECT_DRAWN
}

@Injectable()
export class ToolManagerService {
  initialized = false;
  irisModified = false;
  tempToolIrisModification = false;

  _project: InstanceType<typeof Project>;
  _rect: InstanceType<typeof Project>;
  _iris: InstanceType<typeof Raster>;
  _irisClone: InstanceType<typeof Raster>;
  _original: HTMLImageElement;
  _active: string;
  _componentRef: IrisDrawingTool;
  _pdmOriginal: HTMLImageElement;
  _dualPdmOriginal: HTMLImageElement;
  irisImageLayer: InstanceType<typeof Layer>;
  sourceName: string;

  unrollInnerRing: any;
  unrollOuterRing: any;
  unrollImagePath = '';

  hasSaveButtonSelected = false;

  pullAnnoButtonClicked = false;

  public drawAnnotationLabels = false;

  // Hierarchy:
  //
  // - ghostCursorLayer
  // - mouseOverLayer
  // - tempToolLayer
  // - labelLayer
  // - annotationLayer
  //     + userGroup (user-added annotations)
  //     + generatedGroup (tshepii-generated annotations)
  //         - highlightGroup
  //         - itemsGroup
  //
  ghostCursorLayer: InstanceType<typeof Layer>;
  mouseOverLayer: InstanceType<typeof Layer>;
  tempToolLayer: InstanceType<typeof Layer>;
  labelLayer: InstanceType<typeof Layer>;
  annotationLayer: InstanceType<typeof Layer>;
  userGroup: InstanceType<typeof Group>;
  generatedGroup: InstanceType<typeof Group>;

  tempFillColor: InstanceType<typeof Color>;
  tempStrokeWidth: number;

  // This is needed for the reset. These will be the last saved point. So if you load from a saved history, set this as the annotations from the loaded review.
  originalAnnotations: Array<Annotation>;

  // These are the annotations that are applied to the image at the moment. If you clear the image, set this to empty. If you want to reset the image, set this to the original annotations.
  actualAnnotations = new LinkedMap<Annotation>();
  itemsMap = {}; // Store every single Item (paper.js)
  // according to their Annotation.
  // key: Annotation.id
  // value: { annotation, item }
  // item may be null if not a drawn annotation

  states = {
    past: [],   // to undo
    curr: null, // last added
    future: [], // to redo
  };

  selectedAnnotation: Annotation = undefined;
  hidden = new Set(); // hidden annos
  previouslyFiltered = new Set();

  coreFunctions = new CoreFunctions();

  private _manualAnnotate = new Subject<number>();
  public manualAnnotation = this._manualAnnotate.asObservable();
  public annotationColor = '#ff0000';
  annotationTextSize = 12;

  unrolledRaster: InstanceType<typeof Raster>;
  private _unrollStatus: UnrollStatus = UnrollStatus.UNINITIALIZED;
  private hasUnrolledSubject = new BehaviorSubject<boolean>(false);
  private showOriginalSubject = new BehaviorSubject<boolean>(true);

  unroll: any = {};
  userAnnoGroup: any = [];
  _userParentGroupId: string;
  userParentGroup: any = [];
  previousParentGroupId: string;

  minHeight = 0;
  maxHeight = 2000;
  private currUser;

  get hasUnrolled(): BehaviorSubject<boolean> {
    return this.hasUnrolledSubject;
  }

  get hasOriginal(): BehaviorSubject<boolean> {
    return this.showOriginalSubject;
  }

  _unrollGhostCursor: InstanceType<typeof Path>; // this is for the unroll raster
  ghostCursor: InstanceType<typeof Path>; // this is the one for the main image

  setUnrollCursorVisible(visible: boolean) {
    if (this._unrollGhostCursor) {
      this._unrollGhostCursor.visible = visible;
    }
  }

  set userParentGroupId(id: string) {
    this._userParentGroupId = id;
  }

  get userParentGroupId(): string {
    return this._userParentGroupId;
  }


  set showGhostCursor(show: boolean) {
    if (this.ghostCursor) {
      this.ghostCursorLayer.visible = show;
      this.ghostCursorLayer.bringToFront();
    }
  }

  get showGhostCursor(): boolean {
    if (this.ghostCursor) {
      return this.ghostCursor.visible;
    }
    return false;
  }

  set ghostCursorPosition(pos: InstanceType<typeof Point>) {
    this.ghostCursor.bounds.center = pos;
  }

  get ghostCursorPosition(): InstanceType<typeof Point> {
    return this.ghostCursor.bounds.center;
  }

  // Variable to keep track if the mouse is within the window
  private isInWindow = false;
  private isInUnrollWindow = false;

  public disableTools = false;

  /*
   * Clone the current iris image and store it in `_irisClone`
   * without inserting.
   */
  createClone() {
    this._irisClone = <InstanceType<typeof Raster>>(this.iris.clone({insert: false}));
  }

  /*
   * Check if the canvas is zoomable.
   */
  canZoomImage() {
    return this.isInUnrollWindow || this.isInWindow;
  }

  public drawColor = '#127bdc';
  public fillColor = '#333333';
  public strokeWidth = 2;
  tempDrawColor = '#0f0';
  highlightDrawColor = '#FFEE00';

  // Event called when a tool makes a change
  public toolChange$: Subject<string> = new Subject<string>();

  get componentRef(): IrisDrawingTool {
    return this._componentRef;
  };

  setCompRef(ref: any) {
    this._componentRef = (<IrisDrawingTool>ref.instance);
    this._componentRef.toolChange$.subscribe((toolName) => {
      this.toolChange$.next(toolName);
    });
  }

  get activeTool(): string {
    return this._active;
  }

  set activeTool(tool: string) {
    this._active = tool;
  }

  get project() {
    return this._project;
  }

  get iris() {
    return this._iris;
  }

  get unrollStatus(): UnrollStatus {
    return this._unrollStatus;
  }

  set unrollStatus(s: UnrollStatus) {
    this._unrollStatus = s;
  }

  get annotations(): LinkedMap<Annotation> {
    if (!this.initialized) {
      return new LinkedMap<Annotation>();
    }
    return this.actualAnnotations;
  }

  constructor(public contentSvc: ContentService,
              public profileService: ProfileService,
              private paperSvc: Paper
            ) {
    this.currUser = this.profileService.getCurrentProfile();
    const settings = this.currUser.userSetting;

    this.strokeWidth = settings.strokeWidth;
    this.drawColor = settings.drawColor;
    this.fillColor = settings.fillColor;
  }

  public createIrisRaster(iris: HTMLImageElement) {
    this.irisImageLayer.activate();
    // when this raster is added to the view it will go through the view's transformation
    // matrix, but it will still be at the origin (0, 0) which is the top left of the view
    // so we have to translate down by half the size.  Don't add a new point as part of the
    // constructor because that throws all the transformation off--just translate it.
    const raster = new Raster(iris);
    raster.translate(new Point(iris.width / 2, iris.height / 2));
    raster.name = 'iris-raster';

    return raster;
  };

  setIrisModified(modified: boolean) {
    this.tempToolIrisModification = modified;
  }

  getIrisModified() {
    return this.irisModified || this.tempToolIrisModification;
  }

  resizeWidth() {
    // resize the canvas element to match the size of the parent element
    const rect = (<HTMLElement>this._project.view.element.parentNode).getBoundingClientRect();
    const h = this._project.view.viewSize.height;
    this._project.view.viewSize = new Size(rect.width, h);
  }

  /*
   * Initialize the ToolManager.
   *
   * Things that are initialized:
   *   - The main canvas
   *   - Layers
   *   - Mouse event bound to the canvases
   *   - Loaded iris image
   */
  initManager(canvas, iris, isVertical, fromShowOriginalClick = false) {
    // addCanvas calls activate()
    this._project = this.paperSvc.addCanvas(canvas.nativeElement, null, isVertical, true);

    // resize the canvas element to match the size of the parent element
    const rect = (<HTMLElement>this._project.view.element.parentNode).getBoundingClientRect();
    this._project.view.viewSize = new Size(rect.width, rect.height);

    // create iris layer and raster instance
    this.irisImageLayer = new Layer({name: 'iris'});
    this._project.addLayer(this.irisImageLayer);

    // Keep a copy for reset
    if (!fromShowOriginalClick) {
      this._original = iris;
    }

    this.bindMouseEvents(this._project.view);

    // add a class with displays a border to the parent element (canvas elements don't support borders)
    this._project.view.element.parentElement.classList.add('show-boundary');

    this.initialized = true;

    // Initialize ghostCursorLayer
    this.ghostCursorLayer = new Layer({name: 'ghost-cursor'});
    this.ghostCursor = <InstanceType<typeof Path>>this.ghostCursorLayer.addChild(new Path.Circle({
      center: new Point(0, 0),
      radius: 5,
      strokeColor: '#000',
      fillColor: '#ff0'
    }));
    this.ghostCursorLayer.bringToFront();
    this.ghostCursorLayer.visible = false;

    this.mouseOverLayer = new Layer({name: 'mouse-over'});
    this.tempToolLayer = new Layer({name: 'temp-tool'});
    this.labelLayer = new Layer({name: 'label-layer'});

    this.annotationLayer = new Layer({name: 'annotation'});
    this.userGroup = new Group({name: 'user'});
    this.generatedGroup = new Group({name: 'generated'});
    this.generatedGroup.addChild(new Group({name: 'highlight'}));
    this.generatedGroup.addChild(new Group({name: 'item'}));
    this.annotationLayer.addChildren([this.userGroup, this.generatedGroup]);

    this.project.addLayer(this.annotationLayer);
    this.project.addLayer(this.labelLayer);
    this.project.addLayer(this.ghostCursorLayer);
    this.project.addLayer(this.mouseOverLayer);
    this.project.addLayer(this.tempToolLayer);

    this._iris = this.createIrisRaster(iris);
  }

  /*
   * Adjust the height of the main canvas.
   */
  adjustCanvasHeight(add) {
    const rect = this._project.view.viewSize;

    if ((rect.height + add) != rect.height &&
      (rect.height + add) > this.minHeight &&
      (rect.height + add) < this.maxHeight) {
      this._project.view.viewSize.height = rect.height + add;
    }
  }

  /*
   * Adjust the height of the unroll canvas.
   */
  adjustRectHeight(add) {
    const rect = this._rect.view.viewSize;

    if ((rect.height + add) != rect.height &&
      (rect.height + add) > this.minHeight &&
      (rect.height + add) < this.maxHeight) {
      this._rect.view.viewSize.height = rect.height + add;
    }
  }

  /*
   * Initialize and set up the unroll canvas.
   */
  bindRectCanvas(rectCanvas, isVertical) {
    // addCanvas calls activate()
    this._rect = this.paperSvc.addCanvas(rectCanvas.nativeElement, null, isVertical);
    this._rect.view.element.parentElement.classList.add('show-boundary');

    this._rect.addLayer(new Layer({name: 'unrolled-iris'}));
    this._rect.addLayer(new Layer({name: 'unrolled-items'}));
  }

  lookupTable = {};

  drawUnrolledIris(inner: InstanceType<typeof Path>, outer: InstanceType<typeof Path>) {
    this.annotationLayer.activate();
    this.unrollInnerRing = inner;
    this.unrollOuterRing = outer;

    const pixel_data = this._iris.getImageData(null).data;

    for (let x = 0; x < this._iris.width; x++) {
      this.lookupTable[x] = {};
      for (let y = 0; y < this._iris.height; y++) {
        this.lookupTable[x][y] = [-1, -1];
      }
    }

    //put in 2d lookup array for performance
    const img_data = {};
    for (let y = 0; y < this._iris.height; y++) {
      img_data[y] = {};
      for (let x = 0; x < this._iris.width; x++) {
        const index = (y * this._iris.width + x) * 4;
        img_data[y][x] = [pixel_data[index], pixel_data[index + 1], pixel_data[index + 2]];
      }
    }

    const size = this._project.layers['iris'].bounds;
    const dx = this._iris.width / size.width; // relative to the img in the raster, not the current view
    const dy = this._iris.height / size.height;
    const px = inner['center'].x * dx;
    const py = inner['center'].y * dy;
    const pr = inner['radius'];
    const lx = outer['center'].x * dx;
    const ly = outer['center'].y * dy;
    const lr = outer['radius'];

    // get the rectangle canvas
    this._rect.activate();
    this._rect.layers['unrolled-iris'].activate(); // draw specific layer
    this._rect.layers['unrolled-iris'].clear(); // Appears to prevent unusual sizing issues with unroll raster

    const power = 1.5;
    const rate = 80;

    for (let degree = 0; degree <= 360; degree++) {
      let idx = 0;
      let idy = 0;
      const radians = (Math.PI / 180) * degree;
      const ix = px + pr * Math.cos(radians); // inner
      const iy = py + pr * Math.sin(radians);
      const ox = lx + lr * Math.cos(radians); // outer
      const oy = ly + lr * Math.sin(radians);

      for (let r = 0; r <= rate; r++) {
        idx = Math.trunc(ix + Math.pow(r, power) * (ox - ix) / (Math.pow((rate - 1), power)));
        idy = Math.trunc(iy + Math.pow(r, power) * (oy - iy) / (Math.pow((rate - 1), power)));
        this.lookupTable[idx][idy] = [degree, r];
        try {
          if (this._iris.bounds.topLeft.x != 0) {
            idx = idx - this._iris.bounds.topLeft.x;
          }

          if (this._iris.bounds.topLeft.y != 0) {
            idy = idy - this._iris.bounds.topLeft.y;
          }

          const d = img_data[Math.floor(idy)][Math.floor(idx)]; // get from lookup, getImageData is way too slow
          const rgb = 'rgb(' + d[0] + ', ' + d[1] + ', ' + d[2] + ')';
          const rectangle = new Rectangle(new Point(degree, r), new Size(1, 1));
          const path = new Path.Rectangle(rectangle);
          path.fillColor = new Color(rgb);
        } catch (e) {
          console.log(e);
          console.log('idx:', idx, 'idy:', idy);
          console.log('e:' + degree + ',' + r);
        }
      }
    }

    // Store variable and make some calculations for the mouse event conversions
    this.unroll.inner = inner;
    this.unroll.outer = outer;

    // take the midpoint of the two disjoint ring center points in an attempt
    // to normalize things
    this.unroll.mid = new Point(
      (outer['center'].x + inner['center'].x) / 2,
      (outer['center'].y + inner['center'].y) / 2
    );
    this.unroll.width = this._rect.layers['unrolled-iris'].bounds.width;


    if (this.unrollImagePath != '') {
      const unrolledImage = new Image();
      unrolledImage.src = this.unrollImagePath;
      unrolledImage.crossOrigin = 'anonymous';
      unrolledImage.onload = (evt) => {
        this.replaceUnrollWithRaster(unrolledImage);

        // Check/Add Existing Annotation Text
        this.updateUnrollAnnotations();
      };
    } else {
      this.replaceUnrollWithRaster(undefined);

      // Check/Add Existing Annotation Text
      this.updateUnrollAnnotations();
    }

    this.project.activate();
  }

  replaceUnrollWithRaster(imageElement: HTMLImageElement) {
    const unrollLayer = this._rect.layers['unrolled-iris'];
    unrollLayer.activate();

    // Converts this layer, where each pixel is drawn as a 1x1 Rectangle,
    // to an image
    if (imageElement == undefined) {
      const raster = unrollLayer.rasterize(null, false);
      raster.name = 'unrolled-raster';

      // Remove all the 1x1 Rectangles (to be replace by the raster image)
      this._rect.activeLayer.removeChildren();

      // Scale the project viewport to be the same size of the unrolled
      // iris (currently 360 x rate [80])

      // Add the "image-ized" version of the unrolled iris this layer,
      // dramatically improving performance
      this._rect.activeLayer.addChild(raster);

      this.unrolledRaster = raster;

    } else {

      // Remove all the 1x1 Rectangles (to be replace by the raster image)
      this._rect.activeLayer.removeChildren();

      const posX = imageElement.width / 2;
      const posY = imageElement.height / 2;
      const raster = new Raster(imageElement);
      raster.name = 'unrolled-raster';

      // Scale the project viewport to be the same size of the unrolled
      // iris (currently 360 x rate [80])

      // Add the "image-ized" version of the unrolled iris this layer,
      // dramatically improving performance
      this._rect.activeLayer.addChild(raster);
      // Setting raster.position directly, as setting position with new Raster(imageElement, new Point(posX, posY)); created Raster with undefined position
      raster.position = new Point(posX, posY);
      this.unrolledRaster = raster;
      this.unrollImagePath = '';
    }

    // store every "mousedown" + shiftKey events
    // this event is ALWAYS fired before a "mousedrag" event
    let lastDownEvt = null;

    // generic method to pass mouse events to the active IrisToolComponent
    const evtHandler = (method) => (evt) => this._componentRef[method](evt);

    const dragUnrollOnShift = (evt) => {
      if (evt.point) {
        // compare event point to reference point
        // (where the "mousedown" event was fired)
        evt.point.x -= lastDownEvt.point.x;
        evt.point.y -= lastDownEvt.point.y;   // written this way because TypeScript
        // doesn't like Point - Point
        // (despite Paper.js allowing it)

        this.unrolledRaster.view.translate(evt.point);

        evt.preventDefault();
        evt.stopPropagation();
      }
    };

    const lastAltDrag = null;

    // specific handler for "mousedrag" to determine whether the event is
    // 'special' or not (shiftKey || altKey)
    const toolOrSpecialEvent = (evt) => {
      if (evt.event.shiftKey && this.isInUnrollWindow) {
        return dragUnrollOnShift(evt);
      }

      if (!this._componentRef) {
        lastDownEvt = evt;

        evt.preventDefault();
        evt.stopPropagation();
        return false;
      }

      return evtHandler('onMouseDrag')(evt);
    };

    const onMouseDown = (evt) => {
      if (evt.event.shiftKey || evt.event.altKey || !this._componentRef) {
        lastDownEvt = evt;

        evt.preventDefault();
        evt.stopPropagation();
        return false;
      }
      return evtHandler('onMouseDown')(evt);
    };

    ////////////////////////////////////////////////////////////////
    this.unrolledRaster.view.on({
      mousedown: (evt) => onMouseDown(evt),
      mousedrag: (evt) => toolOrSpecialEvent(evt),
      mouseleave: (evt) => {
        // need to note whether or not the mouse cursor is in the window
        // for zooming
        this.isInUnrollWindow = false;
      },

      mouseenter: (evt) => {
        this.isInWindow = false;
        this.isInUnrollWindow = true;
      }
    });

    ////////////////////////////////////////////////////////////////

    // adds a "ghost cursor" layer and indicator, to track mouse
    // movements made within the iris
    this.createGhostCursor();

    this.unrollStatus = UnrollStatus.RECT_DRAWN;

    this.hasUnrolledSubject.next(true);

    //go back to origin canvas
    this._project.activate();

    // programmatically clicks the canvas element (exposed by paper.js)
    // this is a shortcut to firing Angular's change detection
    // and hiding the loading display
    this._project.view.element.click();
  }

  /*
   * Check if the given annotation is a drawn annotation.
   * If a group is passed in, it's considered 'drawn' if and only if
   * it contains solely drawn annotations.
   *
   * Checking `isDrawnAnnotation()` and removing if `true` implies that
   * the entire group will be removed, not just leaf annotations.
   *
   */
  isDrawnAnnotation(annotation: Annotation) {
    if (!annotation.hasChildren()) {
      if (annotation.type == AnnotationType.POINT ||
        annotation.type == AnnotationType.CIRCLE ||
        annotation.type == AnnotationType.POLYGON ||
        annotation.type == AnnotationType.PENCIL ||
        annotation.type == AnnotationType.GROUP) {
        return true;
      }
      return false;
    }

    // First get the first child, see if it's a drawn annotation.
    // Then, iterate through the rest of the children and `&&`
    // the boolean to get the overall boolean value.
    //
    // First accessing the first child and calling `continue` on it
    // in the loop is a stupid workaround for `LinkedMap`, since
    // accessing by index is O(n).
    const firstChild = annotation.getFirstChild();
    let drawn = this.isDrawnAnnotation(firstChild);
    for (const child of annotation.children) {
      if (child === firstChild) {
        continue;
      }
      drawn = drawn && this.isDrawnAnnotation(child);
    }
    return drawn;
  }

  getAnnotations(atype: AnnotationType) {
    const annotations: Annotation[] = [];
    Object.keys(this.itemsMap).forEach(key => {
      const obj = this.itemsMap[key];
      if (obj['annotation'].type == atype) {
        annotations.push(obj['annotation']);
      }
    });
    return annotations;
  }

  removeAnnotations(atype: AnnotationType) {
    const annotations: Annotation[] = this.getAnnotations(atype);
    for (const a of annotations) {
      this.removeItem(a.id, false);
      this.removeAnnotation(a, true, true);
    }
  }

  checkingUserCreatedAnnotations(anno: Annotation, user: boolean, item?: InstanceType<typeof Item>, beforeId?: string, parentId?: string, label?: InstanceType<typeof Item>, fromRedo?: boolean) {
    this.hasSaveButtonSelected = false;
    var groupName = 'User Created Annotations';
    if (anno.type == AnnotationType.PDM) {
      groupName = 'Individual Pupil Dilation Service';
    } else if (anno.type == AnnotationType.DUAL_PDM) {
      groupName = 'Dual Pupil Dilation Service';
    }
    if (!user || (this.selectedAnnotation !== undefined && this.selectedAnnotation.text == 'Iris Annotation Service')) {
      this.selectedAnnotation = undefined;
    }
    // Check if it's a user created annotation
    if (user && !parentId) {
      this.addItem(anno, user, item, beforeId, this.userParentGroupId, label, fromRedo);
      this.userAnnoGroup.push({id: anno.id, annotation: anno});
      if (fromRedo) {
        this.userParentGroup = this.groupItems({no_parent: this.userAnnoGroup}, user, groupName, anno.parentId, fromRedo, true);
        this.previousParentGroupId = undefined;
      } else {
        this.userParentGroup = this.groupItems({no_parent: this.userAnnoGroup}, user, groupName, null, fromRedo, true);
      }
      this.userParentGroupId = this.userParentGroup.id;
      // Switching the current grouping annotation with the iris draw tool annotation from the past
      const groupAnnotation = this.states.curr;
      const firstAnno = this.states.past[this.states.past.length - 2];
      this.states.past[this.states.past.length - 2] = groupAnnotation;
      //  this set the last item in past to be the first annotation (the actual annotation that is initiates adding a group)
      // Changing it to just pop that item off since we are not adding the current annotation until AFTER the user decides to make another annotation.
      this.states.past.pop();
      this.states.curr = firstAnno;
    } else {
      this.addItem(anno, user, item, beforeId, parentId, label, fromRedo);
    }
  }

  addItem(annotation: Annotation,
          user: boolean,
          item?: InstanceType<typeof Item>,
          beforeId?: string,
          parentId?: string,
          label?: InstanceType<typeof Item>,
          fromRedo: boolean = false) {
    this.userGroup.bringToFront();
    // Getting the parent Id to add the anno to if there is a selected Annotation
    if (!annotation.isSystemGenerated && this.selectedAnnotation !== undefined) {
      parentId = this.selectedAnnotation.id;
      if (this.selectedAnnotation.type != AnnotationType.GROUP) {
        parentId = this.selectedAnnotation.parentId;
      }
    } 
    
    if (parentId) {
      this.addAnnoToParent(annotation, item, beforeId, parentId);
    } else {
      if (beforeId) {
        this.actualAnnotations.insertBefore(beforeId, annotation.id, annotation);
      } else {
        this.actualAnnotations.insert(annotation.id, annotation);
      }

      if (item) {
        this.userGroup.addChild(item);
      }
    }

    if (label) {
      this.labelLayer.addChild(label);
    }

    this.itemsMap[annotation.id] = {
      annotation,
      item,
      label
    };

    this.propagateItemToUnroll(annotation);

    if (user) {
      if (this.states.curr) {
        this.states.past.push(this.states.curr);
      }

      const data = {
        parentId,
        beforeId
      };

      this.states.curr = new State(annotation, StateType.ADD);
      this.states.curr.data = data;
      if (this.states.curr.annotation.type == AnnotationType.GROUP) {
        this.states.curr.compRef = undefined;
      } else {
        this.states.curr.compRef = this.componentRef;
      }
      
      if (!fromRedo) {
        this.states.future.length = 0;
      }
    }
    
  }

  addAnnoToParent(annotation: Annotation,
                  item?: InstanceType<typeof Item>,
                  beforeId?: string,
                  parentId?: string) {
    const parent = this.itemsMap[parentId];

    const parentItem = parent['item'];
    const parentAnno = parent['annotation'];

    // If `beforeId` is specified, add the new item right after it.
    // If not, just push to the end of the list
    if (beforeId) {
      parentAnno.addChildBefore(beforeId, annotation);
    } else {
      parentAnno.addChild(annotation);
    }

    // Add `item` to paper.js Group only if `!!item`
    if (item) {
      parentItem.addChild(item);
    }
  }

  /*
   * Update the unroll canvas with newly added/removed annotations.
   *
   * This should only be called if you want to update the *entire*
   * unroll canvas (i.e., if the iris raster was modified)
   */
  updateUnrollAnnotations() {
    this.removeExistingAnnotationsFromUnroll();
    this.addExistingAnnotationsToUnroll();
  }

  /*
   * Undo the most recently added annotation.
   * If no annotation was added, nothing is done.
   */
  undoAnnotation() {
        // Don't do anything if no annotations were ever added
    if (!this.states.curr) {
      return;
    }

    this.states.future.push(this.states.curr);

    const state = this.states.curr;
    const anno = state.annotation;
    const data = state.data;
    this._componentRef = state.compRef;
    this.selectedAnnotation = undefined;

    const user = !anno.isSystemGenerated;

    switch (this.states.curr.type) {
      case StateType.ADD:
        this.removeItem(anno.id, user, true);
        // Check this method
        if (!!this.componentRef) {
          this.componentRef.resetData();
        }
        break;

      case StateType.REMOVE:
        const [item, label, usr] = this.drawAndLabelAnno(anno, true);

        this.addItem(anno, user, item, data.beforeId, data.parentId, label);
        break;

      case StateType.BULK_REMOVE:
        Object.keys(data['revertTo']).forEach(parentId => {
          for (const obj of data['revertTo'][parentId]) {
            const anno = obj['annotation'];
            const [item, label, usr] = this.drawAndLabelAnno(anno);
            parentId = parentId === 'no_parent' ? null : parentId;
            this.addItem(anno, user, item, obj['beforeId'], parentId, label);
          }
        });
        break;

      case StateType.CLEAR:
      case StateType.RESET:
        this._clear();

        for (const currAnno of data['revertTo']) {
          const [item, label, usr] = this.drawAndLabelAnno(currAnno, true);
          this.addItem(currAnno, user, item, null, null, label);
        }
        break;

      case StateType.GROUP_ONLY:
        this.ungroupItems(state.data['revertTo'],
          anno,
          user);
        break;

      case StateType.GROUP_ADD:
      default:
        break;
    }

    this.states.curr = this.states.past.pop();

    // This ensures that the save modal is prompted when navigating away
    this.setIrisModified(true);
    this.hasSaveButtonSelected = false;
  }

  /*
   * Redo the most recently undone annotation.
   * If no annotation was undone, nothing is done.
   */
  redoAnnotation() {
    if (!this.states.future.length) {
      return;
    }

    // Set new current to last element in future
    const currentAnno = this.states.future.pop();

    this._componentRef = currentAnno.compRef;

    const state = currentAnno;
    const anno = state.annotation;
    let data;
    if (!!state.data) {
      data = state.data;
    } else {
      data = [];
    }

    const user = !anno.isSystemGenerated;

    switch (state.type) {
      case StateType.ADD:
        // `state.data` looks like: { parentid, beforeId }
        this.componentRef.reAddData(anno);
        break;

      case StateType.REMOVE:
        this.removeItem(anno.id, user);
        break;

      case StateType.BULK_REMOVE:
        this.deleteAllAnnotations(user);
        break;

      case StateType.CLEAR:
        this.clear();
        break;
      case StateType.RESET:
        this.reset();
        break;

      case StateType.GROUP_ONLY:
        this.itemsMap[this.states.curr.annotation.id] = {
          annotation: this.states.curr.annotation,
          item: null,
          label: null
        };
        this.groupItems(data['revertTo'], user, anno.text, anno.id,true);
        break;

      case StateType.GROUP_ADD:
      default:
        break;
    }

    // This ensures that the save modal is prompted when navigating away
    this.setIrisModified(true);
    this.hasSaveButtonSelected = false;
  }

  /*
   * "Draw" the annotation provided by `anno`.
   * Note that the drawn annotation (paper.js) is placed in `annotationLayer`
   * If `insert` flag is `true`, the drawn annotation is
   * inserted into the global `itemsMap`.
   *
   * Returns the drawn paper.js Item.
   */
  drawItem(annotation: Annotation, insert?: boolean) {
    this.annotationLayer.activate();

    const self = this;
    const _drawItem = function myself(anno: Annotation) {
      let i;

      if (anno.type != AnnotationType.GROUP) {
        i = self.coreFunctions.drawAnnotation(anno, self._iris);
      } else {
        i = new Group();
        for (const child of anno.children) {
          const subitem = myself(child);
          if (subitem) {
            i.addChild(subitem);
          }
        }
      }

      if (i) {
        i.data.annotation = anno;
      }

      if (insert) {
        self.itemsMap[anno.id] = {
          annotation: anno,
          item: i
        };

        self.userGroup.addChild(i);
      }

      // We can return empty paper.js Groups because
      // there can be empty parent annotations
      return i;
    };


    const item = _drawItem(annotation);
    if (item) {
      item.data.annotation = annotation;
    }


    return item;
  }

  /*
   * Given an annotation, draw its label.
   * Only applies for drawn annotations.
   */
  drawLabel(annotation: Annotation, insert?: boolean) {
    if (!this.drawAnnotationLabels) {
      return null;
    }

    this.labelLayer.activate();

    const self = this;
    const _drawAnnotationText = function myself(anno: Annotation) {
      if (anno.type != AnnotationType.GROUP && anno.value) {
        let tempPoint = new Point(0, 0);
        let pointDefined = false;

        if (anno.value.points) {

          if (anno.value.points.length > 1) {
            const points = new Array<InstanceType<typeof Point>>(anno.value.points.length);
            if (points.length) {
              tempPoint = points[0][0];
              pointDefined = true;
            }
          } else if (anno.value.points.length == 1) { // Probably a Point or Circle
            if (anno.type != AnnotationType.POINT &&
              anno.value.data['radius']) {
              tempPoint = new Point(anno.value.points[0].x, anno.value.points[0].y - anno.value.data['radius']);
            } else {
              tempPoint = new Point(anno.value.points[0].x, anno.value.points[0].y);
            }
            pointDefined = true;
          }
        }

        let label;
        if (pointDefined) {
          const text = '(' + Math.floor(tempPoint.x) + ', ' + Math.floor(tempPoint.y) + ')';
          label = self.coreFunctions.drawAnnotationText(tempPoint,
            text,
            self.annotationColor,
            self.annotationTextSize);
        }
        return label;
      } else if (anno.type == AnnotationType.GROUP) {
        let group;
        if (anno.hasChildren()) {
          group = new Group();
          for (const child of anno.children) {
            const label = myself(child);
            if (label) {
              group.addChild(label);

              if (insert) {
                self.itemsMap[child.id]['label'] = label;
              }
            }
          }
        }
        return group;
      }
      return null;
    };

    return _drawAnnotationText(annotation);
  }


  /*
   * Propagate the given annotation's label to the unroll canvas,
   * if initialized.
   */
  propagatePointTextItemToUnroll(annotation: Annotation) {
    if (this.unrollStatus == UnrollStatus.UNINITIALIZED ||
      !this.drawAnnotationLabels) {
      return;
    }

    const layer = this._rect.layers['unrolled-items'];

    const self = this;
    const _propagatePointTextItemToUnroll = function myself(anno: Annotation) {
      if (anno.type == AnnotationType.POINT && anno.value) {
        let loc = new Point(anno.value.points[0].x, anno.value.points[0].y);
        loc = self.mainToUnroll(loc);

        if (loc.x != -1 && loc.y != -1) {
          const pointText = layer.addChild(new PointText(loc));
          const text = (<InstanceType<typeof PointText>>pointText);
          text.justification = 'center';
          text.fillColor = new Color(self.annotationColor);
          text.content = '(' + loc.x + ', ' + loc.y + ')';
          text.bringToFront();
        }
      } else if (anno.hasChildren()) {
        for (const child of anno.children) {
          myself(child);
        }
      }
    };
    this._rect.activate();
    _propagatePointTextItemToUnroll(annotation);
    this._project.activate();
  }

  redrawAnnotations(redrawPdmOnly: boolean = false) {
    if (this._project) {
      this.resetIris();
      this.clearLayers();

      for (const anno of this.actualAnnotations) {
        if (redrawPdmOnly && anno.text != "Individual Pupil Dilation Service") {
          continue;
        } 
        const [item, label, usr] = this.drawAndLabelAnno(anno, true);
      }

      this.updateUnrollAnnotations();
      this.ghostCursorLayer.bringToFront();
    }
  }

  /*
   * Triggered when the mouse is over an annotation in annotation
   * dialog. Highlight the annotation.
   */
  annotationMouseOn(id: string) {
    // Mouseleave has inconsistent firing. Verify that windows are "off"
    this.isInUnrollWindow = false;
    this.isInWindow = false;

    this.mouseOverLayer.bringToFront();
    this.mouseOverLayer.removeChildren();
    this.mouseOverLayer.activate();

    const self = this;
    const _annotationMouseOn = function myself(anno: Annotation) {
      if (!anno.hasChildren()) {
        if (!(anno.value && anno.value.data['strokeColor'])) {
          return;
        }

        switch (+anno.type) {
          case AnnotationType.POINT:
          case AnnotationType.CIRCLE:
            const point = new Point(anno.value.points[0].x, anno.value.points[0].y);
            self.mouseOverLayer.addChild(
              self.coreFunctions.drawCircle(point,
                anno.value.data['radius'],
                self.highlightDrawColor,
                anno.value.data['fillColor'],
                Number(anno.value.data['strokeWidth']) + 2));
            break;
          case AnnotationType.POLYGON:
            self.mouseOverLayer.addChild(
              self.coreFunctions.drawPolygon(anno.value.points,
                self.highlightDrawColor,
                Number(anno.value.data['strokeWidth']) + 2,
                anno.value.data['fillColor'],
                anno.value.data['fillAlpha']));
            break;
          case AnnotationType.EYELID:
            self.mouseOverLayer.addChild(
              self.coreFunctions.drawPolygon(anno.value.points,
                self.highlightDrawColor,
                Number(anno.value.data['strokeWidth']) + 2,
                anno.value.data['fillColor'],
                anno.value.data['fillAlpha'],
                false));
            break;
          case AnnotationType.PENCIL:
            self.mouseOverLayer.addChild(
              self.coreFunctions.drawPath(anno.value.points,
                self.highlightDrawColor,
                Number(anno.value.data['strokeWidth']) + 2));
            break;
          default:
            break;
        }

        return;
      }

      for (const child of anno.children) {
        myself(child);
      }
    };

    const annotation = this.itemsMap[id]['annotation'];
    _annotationMouseOn(annotation);
  }

  /*
   * Update the annotation label text size and redraw the
   * annotations (which also redraws the labels).
   */
  updateAnnotationTextSize(textSize: number) {
    this.annotationTextSize = textSize;
    this.redrawAnnotations();
  }

  /*
   * Triggered when mouse leaves the annotation in annotation
   * dialog. Un-highlight the annotation.
   */
  annotationMouseOff(index) {
    this.mouseOverLayer.removeChildren();
  }

  preparingRemoval(annotation: Annotation): [Annotation, string] {
    var parentAnno: Annotation;
    var beforeId: string;
    const allAnno = this.getAnnotations(AnnotationType.GROUP);
      allAnno.forEach(anno => {
        var childrenAnnotations = anno.children['map'];
        if (!!childrenAnnotations[annotation.id]) {
          parentAnno = anno;
          if (!!anno.children.getNext(annotation.id)) {
            beforeId = anno.children.getNext(annotation.id).id;
          } else {
            beforeId = null;
          }
        }
      });
    return [parentAnno, beforeId];
  }

  /*
   * Remove an annotation specified by `id`, recursively.
   */
  removeItem(id: string, user?: boolean, fromUndo: boolean = false, reapplyPdm = false) {
    const obj = this.itemsMap[id];

    if (!obj) { // Nothing to remove
      return;
    }

    const item = obj['item'];
    const annotation = obj['annotation'];
    const label = obj['label'];

    if (!user) {
      user = !annotation.isSystemGenerated
    }

    const removed = new Set();

    const self = this;
    const _removeItem = function myself(anno: Annotation, reapplyPdm) {
      var needToResetPdmOriginal = false;
      var needToResetDualPdmOriginal = false;
      const l = self.itemsMap[anno.id]['label'];
      const i = self.itemsMap[anno.id]['item'];

      // If they're drawn annotations, remove them from
      // the canvas. But notice that we're not recursively
      // removing the annotations from its parents, since
      // we want to keep the reference in case of an undo.
      if (l) {
        l.remove();
      }
      if (i) {
        i.remove();
      }

      if (anno.type == AnnotationType.GROUP) {
        for (const child of anno.children) {
          var needToReset = myself(child, reapplyPdm);
          needToResetPdmOriginal = needToReset.needToResetPdmOriginal;
          needToResetDualPdmOriginal = needToReset.needToResetDualPdmOriginal;
        }
      }

      // We need to reset the _pdmOriginal/_dualPdmOriginal since there is no way to remove either pdms from the _iris
      if (anno.type == AnnotationType.PDM && !reapplyPdm) {
        needToResetPdmOriginal = true;
      }
      if (anno.type == AnnotationType.DUAL_PDM) {
        needToResetDualPdmOriginal = true;

      }

      delete self.itemsMap[anno.id];
      removed.add(anno.id);
      return {needToResetPdmOriginal, needToResetDualPdmOriginal};
    };

    let parentAnno;
    let annoAfter;

    // Save `annoAfter` to save it in `state.data['revertTo']`
    // Also remove the current annotation from its parent.
    if (annotation.parentId) {
      parentAnno = this.itemsMap[annotation.parentId]['annotation'];
      if (fromUndo && parentAnno.children.size == 1) {
        annoAfter = parentAnno.getChildAfter(annotation.id);
        parentAnno.removeChild(annotation.id);
        this.removeItem(parentAnno.id, parentAnno.user, fromUndo, reapplyPdm);
        // Get rid of the parent group since readding an annotation will automatically regenerate one.
        this.states.past.pop();
        this.userAnnoGroup = [];
      } else {
        annoAfter = parentAnno.getChildAfter(annotation.id);
        parentAnno.removeChild(annotation.id);
      }
    } else {
      annoAfter = this.actualAnnotations.getNext(annotation.id);

      this.actualAnnotations.remove(annotation.id);

      // If we're deleting the userParentGroup then remove reference to it
      if (annotation.id === this.userParentGroupId) {
        this.previousParentGroupId = this.userParentGroupId;
        this.userParentGroupId = undefined;
        this.userAnnoGroup = [];
      }
    }

    // Remove items from the map and the parent list
    if (this.itemsMap[annotation.id]) {
      const needToReset = _removeItem(annotation, reapplyPdm);
      if (needToReset.needToResetPdmOriginal) {
        this._pdmOriginal = null;
      }
      if (needToReset.needToResetDualPdmOriginal) {
        this._dualPdmOriginal = null;
      }
    }
    if (!this.isDrawnAnnotation(annotation) && !reapplyPdm) {
      // We need to reset the iris, since there's no way to
      // undo non-drawn annotations (ex. crop)
      this.resetIris();
      Object.keys(this.itemsMap).forEach(key => {
        const anno = this.itemsMap[key]['annotation'];

        // Skip groups to avoid duplication. Skip removed
        // annotations and drawn annotations since we only
        // want to re-apply filtered annotations
        if (anno.type == AnnotationType.GROUP ||
          removed.has(key) ||
          this.isDrawnAnnotation(anno)) {
          return;
        }

        this.coreFunctions.drawAnnotation(anno, self._iris);
      });
    }

    this.updateUnrollAnnotations();

    this.mouseOverLayer.removeChildren();
    this.tempToolLayer.removeChildren();

    if (user) {
      if (!fromUndo) {
        if (this.states.curr) {
          this.states.past.push(this.states.curr);
        }

        const data = {
          parentId: parentAnno ? parentAnno.id : null,
          beforeId: annoAfter ? annoAfter.id : null
        };
        this.states.curr = new State(annotation, StateType.REMOVE);
        this.states.curr.data = data;
        this.states.future.length = 0;
      }
    }
  }

  showHideAnnotations(showAnnotations) {
    this.userGroup.visible = showAnnotations;
    this.userGroup.bringToFront();
    this.ghostCursorLayer.bringToFront();
  }

  /*
   * Remove all drawn annotations such as Polygon, Point, Pencil,
   * and Circle, recursively.
   *
   * If a group only contains drawn annotations, remove the entire group.
   */
  deleteAllAnnotations(user?: boolean) {
    const data = {};
    data['revertTo'] = {}; // In the format { parentId: [ {anno, index} ] }

    const self = this;
    const _deleteDrawnAnnotations = function myself(anno: Annotation,
                                                    index: number) {
      if (self.isDrawnAnnotation(anno)) {
        const item = self.itemsMap[anno.id]['item'];
        const label = self.itemsMap[anno.id]['label'];

        delete self.itemsMap[anno.id];
        item.remove();
        if (label) {
          label.remove();
        }

        let parentId = anno.parentId;
        let next;

        if (parentId) {
          const parent = self.itemsMap[parentId]['annotation'];
          next = parent.getChildAfter(anno.id);
          parent.removeChild(anno.id);
        } else {
          parentId = 'no_parent';

          next = self.actualAnnotations.getNext(anno.id);
          self.actualAnnotations.remove(anno.id);
        }

        if (!data['revertTo'][parentId]) {
          data['revertTo'][parentId] = [];
        }

        data['revertTo'][parentId].push({
          annotation: anno,
          beforeId: next ? next.id : null
        });
      } else if (anno.hasChildren()) {
        let i = 0;
        for (const child of anno.children) {
          myself(child, i);
          i++;
        }
      }
    };

    let i = 0;
    for (const anno of this.actualAnnotations) {
      _deleteDrawnAnnotations(anno, i);
      i++;
    }

    if (user) {
      if (this.states.curr) {
        this.states.past.push(this.states.curr);
      }

      const newAnno = new Annotation('Bulk Removal', AnnotationType.UNKNOWN, 'window-close-o');
      this.states.curr = new State(newAnno, StateType.BULK_REMOVE);
      this.states.curr.data = data;
      this.states.future.length = 0;
    }
  }

  /*
   *
   * Takes two annotations, recursively finds common parent.
   * Returns the annotation whose common parent's children has
   * the least index.
   *
   * Example) Suppose we have the following structure:
   *
   * Group_A
   *  - Group_I
   *    + Anno_1
   *  - Group_II
   *    + Anno_2
   *
   * When we call func(Anno_1, Anno_2), the function will iterate
   * til `Group_A` (the common grandparent) and return `Anno_1`
   * because `Anno_1` ultimately comes before `Anno_2` since `Group_I`
   * comes before `Group_II` under the common (grand)parent `Group_A`
   *
   */
  private getAnnotationWithLeastIndex(anno1: Annotation, anno2: Annotation) {
    if (!anno1 || !anno2) {
      return anno1 || anno2;
    }
    if (anno1.parentId == anno2.parentId) { // Found common parent
      if (!anno1.parentId) {
        return this.actualAnnotations.compareIndices(anno1.id, anno2.id) < 0 ?
          anno1 : anno2;
      } else {
        const commonParent = this.itemsMap[anno1.parentId]['annotation'];
        return commonParent.children.compareChildIndices(anno1.id, anno2.id) < 0 ?
          anno1 : anno2;
      }
    }

    const obj1 = this.itemsMap[anno1.parentId];
    const obj2 = this.itemsMap[anno2.parentId];
    return this.getAnnotationWithLeastIndex(obj1 ? obj1['annotation'] : null,
      obj2 ? obj2['annotation'] : null);
  }

  /*
   * Ungroup the group `anno`.
   * `obj` should be in the form of { parentId: [{ id, index }] }
   * that was stored in state.data['revertTo'] where state.type === StateType.GROUP.
   */
  ungroupItems(obj: any,
               anno: Annotation,
               user: boolean) {
    // Nothing to ungroup
    if (!obj ||
      (Object.keys(obj).length === 0 && obj.constructor === Object)) {
      return;
    }

    // First, remove the group
    const item = this.itemsMap[anno.id]['item'];
    const label = this.itemsMap[anno.id]['label'];

    if (anno.parentId) {
      const parent = this.itemsMap[anno.parentId]['annotation'];
      parent.removeChild(anno.id);
    } else {
      this.actualAnnotations.remove(anno.id);
    }

    delete this.itemsMap[anno.id];

    if (item) {
      item.remove();
    }
    if (label) {
      label.remove();
    }


    // Add items back into the project
    Object.keys(obj).forEach(parentId => {
      const arr = obj[parentId];

      for (const info of arr) {
        const index = info['index'];
        const child = this.itemsMap[info['id']];

        if (!child) {
          continue;
        }
        const childItem = child['item'];
        const childAnno = child['annotation'];

        if (parentId === 'no_parent') {
          this.actualAnnotations.insert(childAnno.id, childAnno, index);
          this.userGroup.insertChild(index, childItem);
          childAnno.parentId = null;
        } else {
          const parent = this.itemsMap[parentId];
          parent['annotation'].addChild(childAnno, index);
          parent['item'].insertChild(index, childItem);
        }
      }
    });
  }

  /*
   * Group annotations stored in `obj`.
   * `obj` should be in the form of { parentId: [{ id, index }] }.
   * If `existingId` is specified, when a new group Annotation is created,
   * set its id as `existingId`.
   */
  groupItems(obj: any,
             user: boolean,
             groupName?: string,
             existingId?: string,
             fromRedo?: boolean,
             fromChecking: boolean = false): Annotation {
    // No group to make
    if (!obj || (Object.keys(obj).length === 0 && obj.constructor === Object)) {
      return;
    }

    let deepestParent;
    let deepestParentDepth = 0;

    const items = [];
    const labels = [];
    const annotations = [];

    let sibling;

    // First, iterate thru all the selected annotations and
    // find the deepest parent to add the new group to.
    //
    // Note that obj is in the form of
    // { parentId: [ { id, index }, ...  ], ... }
    Object.keys(obj).forEach(parentId => {
      let parent;

      if (parentId !== 'no_parent') {
        parent = this.itemsMap[parentId]['annotation'];
      }

      const depth = this.getAnnotationDepth(parent);

      if (depth > deepestParentDepth) {
        deepestParent = parent;
        deepestParentDepth = depth;
      } else if (depth == deepestParentDepth) {
        deepestParent = this.getAnnotationWithLeastIndex(deepestParent, parent);
      }
    });

    // Set the new group's sibling that will come after in order
    // TODO: Look into -> throw error several times
    if (!deepestParent) {
      sibling = this.itemsMap[obj['no_parent'][0].id]['annotation'];
    } else {
      sibling = this.itemsMap[obj[deepestParent.id][0].id]['annotation'];
    }

    // Now, remove all items except the deepst parent's
    // least-index child
    Object.keys(obj).forEach(parentId => {
      const children = obj[parentId];
      let parent;

      if (parentId !== 'no_parent') {
        parent = this.itemsMap[parentId]['annotation'];
      }

      for (const child of children) {
        annotations.push(this.itemsMap[child.id]['annotation']);
        items.push(this.itemsMap[child.id]['item']);
        labels.push(this.itemsMap[child.id]['label']);

        // Skip removing `sibling` because it'll be used
        // as a reference when inserting the new group
        if (child.id === sibling.id) {
          continue;
        }

        if (parent) {
          parent.removeChild(child.id);
        } else {
          this.actualAnnotations.remove(child.id);
        }
      }
    });

    // Create the new group and a new annotation
    const group = new Group();
    let newAnno;
    if (groupName) {
      newAnno = new Annotation(groupName, AnnotationType.GROUP, 'fa-folder-o');
    } else {
      newAnno = new Annotation('Group', AnnotationType.GROUP, 'fa-folder-o');
    }
    newAnno.isSystemGenerated = !user;
    group.data.annotation = newAnno;

    const labelGroup = new Group(labels);

    if (existingId) {
      newAnno.id = existingId;
    } else if (this.previousParentGroupId) {
      newAnno.id = this.previousParentGroupId;
    }

    const beforeId = sibling.id;
    const deepestParentId = deepestParent ? deepestParent.id : null;

    this.addItem(newAnno, user, group, beforeId, deepestParentId, labelGroup, fromRedo);

    // Now remove sibling that was used as an anchor
    if (deepestParent) {
      deepestParent.removeChild(sibling.id);
    } else {
      this.actualAnnotations.remove(sibling.id);
    }

    // Setting the parent id to any of the annotations that don't have a parent id
    for (const anno of annotations) {
      if (!anno.parentId) {
        anno.parentId = newAnno.id;
      }
    }

    newAnno.appendChildren(annotations);
    group.addChildren(items);

    if (user) {
      if (fromChecking) {
        if (this.states.curr) {
          this.states.past.push(this.states.curr);
        }
      }
      const data = {
        revertTo: obj
      };
      this.states.curr = new State(newAnno, StateType.GROUP_ONLY);
      this.states.curr.data = data;
      this.states.curr.compRef = undefined;
      if (!fromRedo) {
        this.states.future.length = 0;
      }
    }

    if (!user === undefined) {
      annotations.forEach(annotation => {
        if (!annotation.isSystemGenerated) {
          newAnno.isSystemGenerated = false;
          return newAnno;
        } else {
          newAnno.isSystemGenerated = true;
        }
      });
    }

    return newAnno;
  }

  public temp_data: ImageData;
  initTool(name: string, layerKey?: string) {

    this.temp_data = this._iris.getImageData(null);
    this.irisModified = this.irisModified || this.tempToolIrisModification;
    this._project.activate();
    this.annotationLayer.activate();
    return this.annotationLayer;
  }

  zoom(evt) {
    if (this.isInWindow) {
      const delta = Math.max(-1, Math.min(1, (evt.wheelDelta || -evt.detail)));
      const factor = 1.05;        // zoom factor
      if (delta < 0) {
        this._project.view.zoom /= factor;
        this.ghostCursor.bounds.width *= factor;
        this.ghostCursor.bounds.height *= factor;
      } else if (delta > 0) {
        this._project.view.zoom *= factor;
        this.ghostCursor.bounds.width /= factor;
        this.ghostCursor.bounds.height /= factor;
      }
    } else if (this.isInUnrollWindow) {
      const delta = Math.max(-1, Math.min(1, (evt.wheelDelta || -evt.detail)));
      const factor = 1.05;        // zoom factor
      if (delta < 0) {
        this.unrolledRaster.view.zoom /= factor;
      } else if (delta > 0) {
        this.unrolledRaster.view.zoom *= factor;
      }
    }
  }

  bindMouseEvents(layer: any) {
    const self = this;

    /**
     * Dev Note:
     * Purposely did not use Paper.js MouseEvent.delta for implementing Move/Zoom functionality
     * It caused odd behavior when the mouse was moved at slower speeds
     */

      // generic method to pass mouse events to the active IrisToolComponent
    const evtHandler = (method) => (evt) => {
        self.convertMainEventToUnroll(evt);

        if (self._componentRef) {
          if (self.disableTools) {
            return;
          }
          // evt.target is always the view, because the event
          // listeners are mounted on the view object

          return self._componentRef[method](evt);
        }
      };

    // store every "mousedown" + shiftKey events
    // this event is ALWAYS fired before a "mousedrag" event
    let lastDownEvt = null;

    // specific handler for "mousedrag" + shiftKey
    // (assumes "mousedown" fired previously)
    const dragIrisOnShift = (evt) => {
      if (evt.point) {
        // compare event point to reference point
        // (where the "mousedown" event was fired)
        evt.point.x -= lastDownEvt.point.x;
        evt.point.y -= lastDownEvt.point.y;   // written this way because
        // TypeScript doesn't like Point -
        // Point (despite Paper.js allowing it)

        this._project.view.translate(evt.point);

        evt.preventDefault();
        evt.stopPropagation();
      }
    };

    const lastAltDrag = null;

    // specific handler for "mousedrag" to determine
    // whether the event is 'special' or not (shiftKey || altKey)
    const toolOrSpecialEvent = (evt) => {
      if (evt.event.shiftKey && this.isInWindow) {
        return dragIrisOnShift(evt);
      }

      return evtHandler('onMouseDrag')(evt);
    };

    // specific handler for "mousedown", stores last "mousedown" + shiftKey
    // event
    const onMouseDown = (evt) => {
      if (evt.event.shiftKey || evt.event.altKey) {
        lastDownEvt = evt;

        evt.preventDefault();
        evt.stopPropagation();
        return false;
      }

      return evtHandler('onMouseDown')(evt);
    };

    // apply these mouse event handlers to the active layer
    layer.on({
      mouseup: (evt) => evtHandler('onMouseUp')(evt),
      mousedown: (evt) => onMouseDown(evt),
      mousedrag: (evt) => toolOrSpecialEvent(evt),
      mousemove: (evt) => evtHandler('onMouseMove')(evt),
      mouseleave: (evt) => {
        //need to note whether or not the mouse cursor is in the window
        //since mouse drag gets called when you drag the mouse outside
        //of the window and additionally for zooming
        this.isInWindow = false;
      },

      // Used to set canvas context (when tools are not switched)
      mouseenter: (evt) => {
        this.isInUnrollWindow = false;
        this.isInWindow = true;
        this._project.activate();
      }
    });
  }

  removeExistingAnnotationsFromUnroll() {
    if (this.unrollStatus == UnrollStatus.UNINITIALIZED) {
      return;
    }
    if (!!this._rect) {
      const layer = this._rect.layers['unrolled-items'];
      layer.removeChildren();
    }
  }

  addExistingAnnotationsToUnroll() {
    for (const anno of this.actualAnnotations) {
      this.propagateItemToUnroll(anno);
      this.propagatePointTextItemToUnroll(anno);
    }
  }

  /*
   * Propagate an annotation to the unroll canvas, if initialized.
   * Basically only propagates Point annotations.
   * Should be called if adding a single annotation to the unroll
   * canvas.
   */
  propagateItemToUnroll(annotation: Annotation) {
    if (this.unrollStatus == UnrollStatus.UNINITIALIZED || !annotation) {
      return;
    }
    
    const self = this;
    const _propagateAnnotationToUnroll = function myself(anno: Annotation) {
      // Only propagate Points
      if (anno.type == AnnotationType.POINT) {
        // Copy cosmetic settings from the original Item
        const config = {
          strokeWidth: anno.value.data['strokeWidth'],
          strokeColor: anno.value.data['strokeColor'],
          fillColor: anno.value.data['fillColor'],
          pointRadius: anno.value.data['radius'] || null
        };

        // x values of all converted points are a function of the degree of the angle
        // therefore crossing the "boundary" results in (n).x - (n-1).x ~= rect.width
        // Compare each point - (point-1) against a threshold of 85% the width of the rect
        // to determine when a "boundary jump" takes place within a single Item or Path
        //const boundaryJumpThersold = this._rect.view.bounds.width * 0.85;
        const centerPoint = new Point(anno.value.points[0].x, anno.value.points[0].y);

        const tempCenter = self.mainToUnroll(centerPoint);
        anno.value.data['unrollX'] = tempCenter.x;
        anno.value.data['unrollY'] = tempCenter.y;

        let circle;
        if (tempCenter.x != -1 && tempCenter.y != -1) {
          circle = new Path.Circle(tempCenter, config.pointRadius);
          circle.fillColor = config.fillColor;
          circle.strokeWidth = config.strokeWidth;
          circle.strokeColor = config.strokeColor;
        }
        return circle;
      } else if (anno.type == AnnotationType.GROUP) {
        const group = new Group();
        for (const child of anno.children) {
          const drawn = myself(child);
          if (drawn) {
            group.addChild(drawn);
          }
        }
        if (group.hasChildren()) {
          return group;
        }
      }
      return null;
    };

    if (!!this._rect) {
      this._rect.activate();
      const layer = this._rect.layers['unrolled-items'];

      // convert the single iris Item into a Group of Items to be drawn on the unrolled canvas
      // (this prevents erroneous lines connecting two on separate sides of the boundary)
      const itemGroup = _propagateAnnotationToUnroll(annotation);
      if (itemGroup) {
        layer.addChild(itemGroup);
      }
    }
    // Reactivate main canvas
    this._project.activate();
  }

  /*
   * Converts a main canvas mouse event to an unroll event.
   */
  convertMainEventToUnroll(evt) {
    if (this.unrollStatus === UnrollStatus.RECT_DRAWN) {
      if (this.unroll.outer.contains(evt.point) && !this.unroll.inner.contains(evt.point)) {
        this._unrollGhostCursor.bounds.center = this.mainToUnroll(evt.point);
      }
    }
  }

  /*
   * Given a Point in the main canvas, create a Point to place
   * in the unroll canvas.
   */
  mainToUnroll(point: InstanceType<typeof Point>): InstanceType<typeof Point> {
    // returns: [-1, -1] for points not in the unroll
    const ret = point.clone();
    ret.x = -1;
    ret.y = -1;

    if (!this.unroll.outer.contains(point) || this.unroll.inner.contains(point)) {
      return ret;
    }

    ret.x = Math.round(point.x);
    ret.y = Math.round(point.y);

    let modPoint = this.lookupTable[ret.x][ret.y];
    if (this.lookupTable[ret.x][ret.y][0] == -1
      && this.lookupTable[ret.x][ret.y][1] == -1) {

      for (let x = 1; x < Math.min(this._iris.width, this._iris.height) - 1; x++) {
        modPoint = this.lookupTable[ret.x + x][ret.y];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x + x][ret.y + x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x + x][ret.y - x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x][ret.y - x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x][ret.y + x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x - x][ret.y - x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x - x][ret.y];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

        modPoint = this.lookupTable[ret.x - x][ret.y + x];
        if (modPoint[0] != -1 && modPoint[1] != -1) {
          break;
        }

      }
    }

    ret.x = modPoint[0];
    ret.y = modPoint[1];
    return ret;
  }

  /*
   * Reset the annotations to revert back to the last saved point.
   */
  reset(user?: boolean) {
    if (user) {
      const data = {
        revertTo: this.actualAnnotations.toList()
      };
      if (this.states.curr) {
        this.states.past.push(this.states.curr);
      }

      const anno = new Annotation('Reset', AnnotationType.UNKNOWN, null);

      this.states.curr = new State(anno, StateType.RESET);
      this.states.curr.data = data;
      this.states.future.length = 0;
    }

    this._clear();
    var pdmAnno;
    if (this.originalAnnotations && this.originalAnnotations.length) {
      for (let i = 0; i < this.originalAnnotations.length; i++) {
        const anno = this.originalAnnotations[i];
        // Both PDM annotations need to be added a different way.
        if (anno.text == "Individual Pupil Dilation Service" || anno.text == "Dual Pupil Dilation Service") {
          pdmAnno = anno;
        }
        const [item, label, usr] = this.drawAndLabelAnno(anno);
        this.addItem(anno, usr, item, null, null, label);

        // Replace with deep clone
        this.originalAnnotations[i] = Annotation.clone(anno);
      }
      return pdmAnno;
    }


    this.updateUnrollAnnotations();
    if (this.originalAnnotations && this.originalAnnotations.length) {
      for (const item of this.originalAnnotations) {
        if ((item.parentId == null || item.parentId == "") && item.children != null && item.text !== 'Iris Annotation Service' && item.text !== 'Individual Pupil Dilation Service' && item.text != "Dual Pupil Dilation Service") {
          this.userParentGroupId = item.id;
          break;
        } else {
          this.userParentGroupId = undefined;
        }
      }
    } else {
      this.userParentGroupId = undefined;
    }
    this.userGroup = new Group({name: 'user'});
    this.userAnnoGroup = [];
    this.userParentGroup = [];
    this.selectedAnnotation = undefined;
  }

  /*
   * Clear the iris and the annotation list.
   */
  private _clear() {
    this.actualAnnotations.clearAll();
    this.itemsMap = {};
    this.hidden.clear();

    this.resetIris();
    this.clearLayers();

    this.annotationLayer.activate();

    this.states = {
      past: [],   // to undo
      curr: null, // last added
      future: [], // to redo
    };
    this.previousParentGroupId = undefined;

  }

  /*
   * Remove all annotations and clear the canvas.
   */
  clear(user?: boolean) {
    if (user) {
      const data = {
        revertTo: this.actualAnnotations.toList(),
      };

      if (this.states.curr) {
        this.states.past.push(this.states.curr);
      }

      const anno = new Annotation('Clear', AnnotationType.UNKNOWN, null);

      this.states.curr = new State(anno, StateType.CLEAR);
      this.states.curr.data = data;
      this.states.future.length = 0;
    }

    this._clear();
    this.createClone();
    this.updateUnrollAnnotations();
    this.previousParentGroupId = this.userParentGroupId;
    this.userParentGroupId = undefined;
    this.selectedAnnotation = undefined;
    this.userAnnoGroup = [];

  }

  /*
   * Clear all the paper.js layers.
   */
  clearLayers() {
    this.project.activate();

    this.mouseOverLayer.removeChildren();
    this.tempToolLayer.removeChildren();
    this.labelLayer.removeChildren();

    this.annotationLayer.removeChildren();
    this.userGroup.removeChildren();
    this.generatedGroup.children['highlight'].removeChildren();
    this.generatedGroup.children['item'].removeChildren();
    this.annotationLayer.addChild(this.userGroup);
    this.annotationLayer.addChild(this.generatedGroup);
  }

  /*
   * Reset the raster and clear the layers.
   */
  resetIris() {
    // reset to original rotation
    this.coreFunctions.rotate(this.iris, -this._project.view.rotation);

    this.irisModified = false;
    this.tempToolIrisModification = false;

    this.irisImageLayer.removeChildren();
    // We need to check if there is a PDM applied when we are deleting a single annotation.
    if (this._pdmOriginal) {
      this._iris = this.createIrisRaster(this._pdmOriginal);
    } else if (this._dualPdmOriginal){
      this._iris = this.createIrisRaster(this._dualPdmOriginal);
    } else {
      this._iris = this.createIrisRaster(this._original);
    }
    
    this.irisImageLayer.addChild(this._iris);
  }

  addTshepiiPoly(anno: Annotation, annotationColor: string): InstanceType<typeof Path> {
    const points = anno.value.points;

    const path = new Path();
    path.strokeColor = new Color(annotationColor);
    path.strokeColor.alpha = this.currUser.userSetting.tshepiiStrokeOpacity / 100;
    path.strokeWidth = 1;

    path.fillColor = new Color(annotationColor);
    path.fillColor.alpha = .1;

    for (const point of points) {
      path.add(point);
    }//end of for loop

    path.closed = true;

    this.generatedGroup.children['item'].addChild(path);

    return path;
  }

  addTshepiiHighlight(anno: Annotation, annotationColor: string) {
    const points = anno.value.points;

    const path = new Path();
    path.strokeColor = new Color(annotationColor);
    path.strokeColor.alpha = 0.0;
    path.strokeWidth = 2;
    path.fillColor = new Color(annotationColor);
    path.fillColor.alpha = .2;

    for (const point of points) {
      path.add(point);
    }//end of for loop

    path.closed = true;

    this.generatedGroup.children['highlight'].addChild(path);
  }

  removeTshepiiItems() {
    this.generatedGroup.children['item'].removeChildren();
  }

  removeTshepiiHighlights() {
    this.generatedGroup.children['highlight'].removeChildren();
  }

  showHideAnnotation(initialId: string) {
    const hide = !this.hidden.has(initialId);
    const initialObj = this.itemsMap[initialId];
    const initalAnno = this.itemsMap[initialId]['annotation'];
    // If the whole group is being hidden, it marks which annotations were previously hidden.
    if (initalAnno.type == AnnotationType.GROUP && hide) {
      this.previouslyFiltered = new Set(this.hidden);
    }

    const self = this;
    const _hideAnnotation = function myself(anno: Annotation) {
      if (hide) { // Hide
        this.hidden.add(anno.id);
      }
      if (anno.type != AnnotationType.GROUP) {
        if (initialObj['item']) {
          initialObj['item'].visible = !hide;
          if (initialObj['label']) {
            initialObj['label'].visible = !hide;
          }
        }

      } else { // This is a group
        for (const child of anno.children) {
          _hideAnnotation(child);
        }
      }
    }.bind(this);

    // Check to see if the itemId is in the hidden set.
    // Deletes the id from the set and draws the annotation if:
    // 1) id is NOT in hidden set and it's a hide, or 2) if id IS in hidden and it's a show
    const _checkHidden = function _checkHidden(anno: Annotation) {
      if ((this.hidden.has(anno.id) && !hide && anno.id == initialId) ||
        (!this.hidden.has(anno.id) && hide)) {
        // Apply non-hidden annotations to image
        this.hidden.delete(anno.id);
        this.coreFunctions.drawAnnotation(anno, this._iris);
      }
    }.bind(this);

    _hideAnnotation(initalAnno);

    if (this.hidden.size) {
      // If we're hiding annotations,
      // reset the entire iris
      if (hide) {
        this.resetIris();
      }

      Object.keys(this.itemsMap).forEach(itemId => {
        const itemAnno = this.itemsMap[itemId]['annotation'];

        // Skips itemAnno if it's of Type GROUP
        if (itemAnno.type === AnnotationType.GROUP) {
          return;
        }

        // If it's hide, it checks to see if the itemAnno is in the hidden set
        if (hide) {
          _checkHidden(itemAnno);
        } else { // Show
          if (initalAnno.type !== AnnotationType.GROUP) {
            _checkHidden(itemAnno);
          } else { // This is for when initial annotation is of Type GROUP
            // Checks to see if the annotation was previously hidden before the whole group was hidden
            // Deletes the id from hidden set and draws the annotation if:
            // 1) Id IS in previouslyFiltered set and it's a hide, or 2) if the id is NOT in previouslyFiltered set and it's a show
            if ((this.previouslyFiltered.has(itemId) && hide) ||
              (!this.previouslyFiltered.has(itemId) && !hide)) {
              // Apply hidden filters to the iris
              this.hidden.delete(itemId);
              this.coreFunctions.drawAnnotation(itemAnno, self._iris);
            }
            // Removes the group id from the hidden set
            this.hidden.delete(initialId);
          }
        }
      });
    }
  }

  /*
   * Check if an annotation specified by `id` exists.
   */
  annotationExists(id: string) {
    return !!this.itemsMap[id];
  }

  public revertToState(index: number) {
    if (!this.states.past.length) {
      return;
    }

    while (this.states.past.length > index) {
      this.undoAnnotation();
    }
  }

  private getAnnotationDepth(annotation: Annotation) {
    const self = this;
    const _getAnnotationDepth = function myself(anno: Annotation) {
      if (!anno || !anno.parentId) {
        return 0;
      }
      return 1 + myself(self.itemsMap[anno.parentId]);
    };

    return _getAnnotationDepth(annotation);
  }

  public removeAnnotation(anno: Annotation,
                           recursive?: boolean,
                           removeFromParent?: boolean) {
    delete this.itemsMap[anno.id];

    if (removeFromParent) {
      if (anno.parentId) {
        const parent = this.itemsMap[anno.parentId];
        parent['annotation'].removeChild(anno.id);
      } else {
        this.actualAnnotations.remove(anno.id);
      }
    }

    if (recursive && anno.hasChildren()) {
      for (const child of anno.children) {
        this.removeAnnotation(child, true, removeFromParent);
      }
    }
  }

  private createGhostCursor() {
    if (this._unrollGhostCursor == undefined) {
      const cursorLayer = this._rect.addLayer(new Layer({name: 'ghost-cursor'}));
      this._unrollGhostCursor = <InstanceType<typeof Path>>cursorLayer.addChild(new Path.Circle({
        center: new Point(0, 0),
        radius: 5,
        strokeColor: '#000',
        fillColor: '#ff0',
      }));
      cursorLayer.bringToFront();
    }
  }

  addIrisAnnotationResponse(irisResponse: IrisAnnotationResponse) {
    this.pullAnnoButtonClicked = true;
    const group = [];
    let index = 0;
    for (const poly of irisResponse.polygonList) {
      if (poly.outline.length > 0) {
        const annotation = new Annotation(poly.name, AnnotationType.POLYGON, 'fa-cube');
        annotation.isSystemGenerated = true;
        const points = [];
        for (const seg of poly.outline) {
          points.push({x: seg.x, y: seg.y});
        }
        annotation.value = {
          points,
          data: {
            strokeColor: poly.color,
            fillColor: poly.color,
            strokeWidth: this.strokeWidth,
            fillAlpha: 0.2
          }
        };

        const polygon = this.coreFunctions.drawPolygon(annotation.value.points, annotation.value.data['strokeColor'], annotation.value.data['strokeWidth'], annotation.value.data['fillColor'], annotation.value.data['fillAlpha']);
        polygon.data.dannotation = annotation;
        const text = '(' + Math.floor(points[0]['x']) + ', ' + Math.floor(points[0]['y']) + ')';
        let label;

        if (this.drawAnnotationLabels) {
          label = this.coreFunctions.drawAnnotationText(
            new Point(points[0]),
            text,
            this.annotationColor,
            this.annotationTextSize
          );
        }

        group.push({id: annotation.id, index: index++});
        this.addItem(annotation, !annotation.isSystemGenerated, polygon, null, null, label);
      } else {
        console.log('Poly: ' + poly.name + ' is empty');
      }
    }

    this.groupItems({no_parent:group}, false, 'Iris Annotation Service');
    this.redrawAnnotations();
  }

  // Unused but keeping in case we need to figure out if the annotation is in past states
  currAnnoInPast() {
    return this.states.past.some(pastState => {
      return pastState.annotation.id == this.states.curr.annotation.id;
    })
  }

  selectedAnnotationChanged($event: Annotation) {
    this.selectedAnnotation = $event;
  }

  drawAndLabelAnno(anno: Annotation, insert?: boolean) {
    const item = this.drawItem(anno, insert);
    const label = this.drawLabel(anno, insert);
    const usr = !anno.isSystemGenerated;

    return [item, label, usr];
  }
}
