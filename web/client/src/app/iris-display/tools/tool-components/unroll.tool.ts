/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component } from '@angular/core';
import { IrisDrawingTool, RequiredMembers, IrisToolType } from '../tool.base';
import { ToolManagerService, UnrollStatus } from '../../tool-manager.service';
import { Path, Raster, Point } from 'paper';
import { FindIrisService } from '../../../find-iris/find-iris.service';
import Color = paper.Color;


enum CircleState {
  RESIZING,
  MOVING,
  REST
}


@Component({
  template: `
    <ng-container *ngIf="_isActive">
      <span class="pull-left"
            style="padding-right: 10px; width:270px;">Move/resize the circle to cover the {{ iris.showCircle ? 'iris' : 'pupil' }}</span>
      <div class="btn-group" data-toggle="buttons">
        <label for="pupil-{{idSuffix}}" class="btn btn-success mb-0 btn-sm active" (click)="showPupilCircle()">
          <input id="pupil-{{idSuffix}}" type="radio" autocomplete="off" checked> Pupil
        </label>
        <label for="iris-{{idSuffix}}" class="btn btn-danger mb-0 btn-sm" (click)="showIrisCircle()">
          <input id="iris-{{idSuffix}}" type="radio" autocomplete="off"> Iris
        </label>
      </div>
      <button style="padding-left: 10px;" *ngIf="pupil.circle.visible && iris.circle.visible"
              class="btn btn-primary btn-sm" type="button" autocomplete="off" (click)="drawUnrollImage()">Done
      </button>
    </ng-container>
  `
})
export class UnrollTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Unroll';
  static icon = 'fa-map';
  static id = 'Unroll';
  static type: IrisToolType = IrisToolType.IMAGE;
  pointRadius = 5;
  requiredPointCount = 3;
  innerRing: any;
  outerRing: any;
  points: any[] = [];
  _irisRaster: InstanceType<typeof Raster>;
  idSuffix: string;

  pupil: {
    circle: InstanceType<typeof Path>;
    state: CircleState;
    showCircle: boolean;
  } = {circle: null, state: CircleState.REST, showCircle: false};

  iris: {
    circle: InstanceType<typeof Path>;
    state: CircleState;
    showCircle: boolean;
  } = {circle: null, state: CircleState.REST, showCircle: false};

  data = {};

  constructor(
    protected findIrisService: FindIrisService,
    protected toolManager: ToolManagerService
  ) {
    super(toolManager);

    this._irisRaster = this.toolManager.iris;
  }

  onToolInit() {
    this.idSuffix = this.toolManager.sourceName;
    this.pointRadius = this.config['pointRadius'] || 5;
    this.requiredPointCount = this.config['requiredPointCount'] || 3;

    this.findIrisService.initialize(this.toolManager);
    const pupilCenter = new Point(this.findIrisService.pupilCenterX, this.findIrisService.pupilCenterY);

    pupilCenter.x += this.toolManager.iris.bounds.topLeft.x;
    pupilCenter.y += this.toolManager.iris.bounds.topLeft.y;

    this.pupil.circle = new Path.Circle(
      pupilCenter,
      this.findIrisService.pupilRadius
    );
    this.pupil.circle.strokeColor = new Color('lime');
    this.pupil.circle.strokeWidth = 2;
    this.pupil.state = CircleState.REST;
    this.pupil.circle.visible = true;
    this.pupil.showCircle = true;

    const irisCenter = new Point(this.findIrisService.IrisCenterX, this.findIrisService.IrisCenterY);
    irisCenter.x += this.toolManager.iris.bounds.topLeft.x;
    irisCenter.y += this.toolManager.iris.bounds.topLeft.y;

    this.iris.circle = new Path.Circle(
      irisCenter,
      this.findIrisService.IrisRadius
    );
    this.iris.circle.strokeColor = new Color('red');
    this.iris.circle.strokeWidth = 2;
    this.iris.state = CircleState.REST;
    this.iris.circle.visible = false;
    this.iris.showCircle = false;

    this.toolManager.tempToolLayer.addChild(this.iris.circle);
    this.toolManager.tempToolLayer.addChild(this.pupil.circle);
  }

  onMouseDown(event) {
    if (event.event.shiftKey) {
      return;
    }

    if (this.pupil.showCircle) {
      if (this.pupil.circle.hitTest(event.point, {curves: true, tolerance: 10})) {
        //the user is trying to resize the circle
        this.pupil.state = CircleState.RESIZING;
      } else if (this.pupil.circle.contains(event.point)) {
        //the user wishes to move the circle
        this.pupil.state = CircleState.MOVING;
      }
    } else if (this.iris.showCircle) {
      if (this.iris.circle.hitTest(event.point, {curves: true, tolerance: 10})) {
        //the user is trying to resize the circle
        this.iris.state = CircleState.RESIZING;
      } else if (this.iris.circle.contains(event.point)) {
        //the user wishes to move the circle
        this.iris.state = CircleState.MOVING;
      }
    }
  }

  onMouseDrag(event) {
    if (this.pupil.circle.visible == false && this.iris.circle.visible == false) {
      return;
    }

    if (this.pupil.showCircle) {
      if (this.pupil.state == CircleState.RESIZING) {
        //get the distance from the mouse point and the center
        const newRadius = this.pupil.circle.bounds.center.getDistance(event.point);
        const oldCenter = this.pupil.circle.bounds.center.clone();
        this.pupil.circle.bounds.width = newRadius * 2;
        this.pupil.circle.bounds.height = newRadius * 2;
        this.pupil.circle.bounds.center = oldCenter;
      } else if (this.pupil.state == CircleState.MOVING) {
        this.pupil.circle.bounds.center.x += event.delta.x;
        this.pupil.circle.bounds.center.y += event.delta.y;
      }
    } else if (this.iris.showCircle) {
      if (this.iris.state == CircleState.RESIZING) {
        //get the distance from the mouse point and the center
        const newRadius = this.iris.circle.bounds.center.getDistance(event.point);
        const oldCenter = this.iris.circle.bounds.center.clone();
        this.iris.circle.bounds.width = newRadius * 2;
        this.iris.circle.bounds.height = newRadius * 2;
        this.iris.circle.bounds.center = oldCenter;
      } else if (this.iris.state == CircleState.MOVING) {
        this.iris.circle.bounds.center.x += event.delta.x;
        this.iris.circle.bounds.center.y += event.delta.y;
      }
    }
  }

  checkInnerOuterRings() {
    // If Inner Ring is larger than outer ring, switch positions
    const innerRadius = this.innerRing.radius;
    const outerRadius = this.outerRing.radius;

    if (innerRadius > outerRadius) {
      const tempRing = this.innerRing;
      this.innerRing = this.outerRing;
      this.outerRing = tempRing;
    }
  }

  removeRings() {
    this.pupil.circle.visible = false;
    this.iris.circle.visible = false;
  }

  showIrisCircle() {
    if (!this.iris.circle.visible) {
      this.iris.circle.visible = true;
    }
    this.iris.showCircle = true;
    this.pupil.showCircle = false;
  }

  showPupilCircle() {
    if (!this.pupil.circle.visible) {
      this.pupil.circle.visible = true;
    }
    this.iris.showCircle = false;
    this.pupil.showCircle = true;
  }

  drawUnrollImage() {
    //set the inner ring
    this.innerRing = this.pupil.circle;
    this.innerRing['center'] = this.pupil.circle.bounds.center;
    this.innerRing['radius'] = this.pupil.circle.bounds.width / 2;
    this.innerRing.name = 'inner';

    //set the outer ring
    this.outerRing = this.iris.circle;
    this.outerRing['center'] = this.iris.circle.bounds.center;
    this.outerRing['radius'] = this.iris.circle.bounds.width / 2;
    this.outerRing.name = 'outer';


    if (this.innerRing.getIntersections(this.outerRing).length > 0) {
      window.alert('Unroll circles may not overlap. Please move/resize the circles');
    } else {
      this.checkInnerOuterRings();

      if (this.outerRing.center.x + this.outerRing.radius > this.toolManager._iris.bounds.bottomRight.x
        || this.outerRing.center.x - this.outerRing.radius < this.toolManager._iris.bounds.topLeft.x) {
        window.alert('The outer ring must be contained within the image');
      } else if (this.outerRing.center.y + this.outerRing.radius > this.toolManager._iris.bounds.bottomRight.y
        || this.outerRing.center.y - this.outerRing.radius < this.toolManager._iris.bounds.topLeft.y) {
        window.alert('The outer ring must be contained within the image');
      } else if (this.outerRing.contains(this.innerRing.center)) {
        this.toolManager.unrollStatus = UnrollStatus.INITIALIZING;

        // give the outerRing a chance to draw before performing the expensive operation of unrolling the Iris
        setTimeout(() => {
          this.toolManager.drawUnrolledIris(this.innerRing, this.outerRing);
          this.removeRings();
        }, 100);

        // Needs to turn of the tool
        this._destroy();

      } else {
        window.alert('Unroll circles may not overlap. Please move/resize the circles');
      }
    }//end of else

    this.toolManager.setIrisModified(true);
  }
}
