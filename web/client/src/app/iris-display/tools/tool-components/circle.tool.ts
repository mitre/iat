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
import { IrisDrawingTool, IrisToolType, RequiredMembers } from '../tool.base';
import { ToolManagerService } from '../../tool-manager.service';
import { Point } from 'paper';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">

      <span class="pull-left" style="padding-right: 10px; width:270px;">Click and drag to create a circle.</span>
      <div class="btn-group" data-toggle="buttons">
        <button class="btn btn-primary btn-sm active" type="button" id="createCircleBtn" (click)="pushCircle()"
                [disabled]="tempRing == null">Complete
        </button>
      </div>
    </ng-container>
  `
})
export class CircleTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Circle';
  static icon = 'fa-circle';
  static id = 'Circle';
  static type: IrisToolType = IrisToolType.ANNOTATION;
  tempRing: any;
  startPoint: InstanceType<typeof Point>;
  movePoint: InstanceType<typeof Point>;
  movingCircle = false;
  resizingCircle = false;
  circleCreated = false;
  newStartSet = false;
  previousPoint: any;
  editCircleWidth = 4;
  drawCircleWidth = 2;
  circleHasSize = false;
  transparentColor = 'rgba(0, 0, 0, 0)';
  annotation: Annotation;
  fromRedo = false;
  newCenterX: any;
  newCenterY: any;
  radius: any;

  tempAnno: Annotation;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
  }

  onMouseUp(event) {
    if (event.event.shiftKey) {
      return;
    }

    if (event.target.name == 'unrolled-raster') {
      return;
    }

    if (this.circleHasSize) {
      this.movingCircle = false;
      this.resizingCircle = false;
      this.newStartSet = false;
      this.circleCreated = true;
    }
  }

  onMouseDown(event) {
    if (event.target.name == 'unrolled-raster') {
      return;
    }

    if (!this.circleCreated) {
      this.startPoint = new Point(event.point.x, event.point.y);
    } else {
      if (this.tempRing.hitTest(event.point)) {
        this.resizingCircle = true;
        this.movingCircle = false;
      } else if (this.tempRing.contains(event.point)) {
        this.movingCircle = true;
        this.previousPoint = event.point;
      } else {
        this.movingCircle = false;
      }
    }
  }

  onMouseDrag(event) {
    if (event.target.name == 'unrolled-raster') {
      return;
    }

    if (this.movingCircle) {
      // Create points for start point mod
      const newStartXPosition = this.startPoint.x + event.point.x - this.previousPoint.x;
      const newStartYPosition = this.startPoint.y + event.point.y - this.previousPoint.y;

      this.startPoint = new Point(newStartXPosition, newStartYPosition);

      // Create points for circle center
      const newXPosition = this.tempRing.center.x + event.point.x - this.previousPoint.x;
      const newYPosition = this.tempRing.center.y + event.point.y - this.previousPoint.y;

      const radius = this.tempRing.radius;

      // Create new circle
      const ring = this.coreFunctions.drawCircle(new Point(newXPosition, newYPosition), radius, this.tempRing.strokeColor, this.transparentColor, this.editCircleWidth);
      ring['center'] = new Point(newXPosition, newYPosition);
      ring['radius'] = radius;

      ring.name = 'temp';
      this.removeTemporaryRing();
      this.tempRing = ring;
      this.toolManager.tempToolLayer.addChild(this.tempRing);
      this.previousPoint = event.point;
    } else if (!this.circleCreated) {
      this.circleHasSize = true;
      this.movePoint = new Point(event.point.x, event.point.y);
      this.removeTemporaryRing();
      this.tempRing = this.drawRing(this.toolManager.tempDrawColor, this.editCircleWidth);
      this.toolManager.tempToolLayer.addChild(this.tempRing);
    } else if (this.resizingCircle) {
      // Need to find opposite point on the circle
      // Get distance/difference to center
      // Double and set start point to opposite side point
      if (!this.newStartSet) {
        const xDiff = this.tempRing.center.x - event.point.x;
        const yDiff = this.tempRing.center.y - event.point.y;

        const newStartX = event.point.x + (2 * xDiff);
        const newStartY = event.point.y + (2 * yDiff);

        this.startPoint = new Point(newStartX, newStartY);
        this.newStartSet = true;
      }

      this.movePoint = event.point;
      this.removeTemporaryRing();
      this.tempRing = this.drawRing(this.toolManager.tempDrawColor, this.editCircleWidth);
      this.toolManager.tempToolLayer.addChild(this.tempRing);
    }
  }

  drawRing(color, width) {
    const distance = this.startPoint.getDistance(this.movePoint, false) / 2;
    const halfPointX = (this.startPoint.x + this.movePoint.x) / 2;
    const halfPointY = (this.startPoint.y + this.movePoint.y) / 2;
    const midPoint = new Point(halfPointX, halfPointY);

    const ring = this.coreFunctions.drawCircle(midPoint, distance, color, this.transparentColor, width);

    ring['center'] = midPoint;
    ring['radius'] = distance;


    return ring;
  }

  removeTemporaryRing() {
    if (this.tempRing != null) {
      this.tempRing.remove();
      this.tempRing = null;
    }
  }

  pushCircle() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    if (!this.fromRedo) {
      this.annotation = new Annotation(CircleTool.id, AnnotationType.CIRCLE, CircleTool.icon);
      this.newCenterX = this.tempRing.center.x;
      this.newCenterY = this.tempRing.center.y;
      this.radius = this.tempRing.radius;
    }

    const center = new Point(this.newCenterX, this.newCenterY);

    const ring = this.coreFunctions.drawCircle(center, this.radius, this.toolManager.drawColor, this.transparentColor, this.toolManager.strokeWidth);

    ring['center'] = center;
    ring['radius'] = this.radius;
    const points = [];
    points.push({
      x: this.newCenterX,
      y: this.newCenterY
    });
    if (this.tempAnno) {
      this.annotation.value = this.tempAnno.value;
    } else {
      this.annotation.value = {
        points,
        data: {
          radius: this.radius,
          strokeColor: this.toolManager.drawColor,
          strokeWidth: ring.strokeWidth
        }
      };
    }

    ring.data.annotation = this.annotation;

    const text = '(' + Math.floor(this.annotation.value.points[0].x) + ', ' + Math.floor(this.annotation.value.points[0].y) + ')';
    let label;

    if (this.toolManager.drawAnnotationLabels) {
      label = this.coreFunctions.drawAnnotationText(
        new Point(this.annotation.value.points[0].x, this.annotation.value.points[0].y - this.annotation.value.data['radius']),
        text,
        this.toolManager.annotationColor,
        this.toolManager.annotationTextSize
      );
    }
    this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, ring, null, this.userParentGroupId, label, this.fromRedo);

    this.removeTemporaryRing();
    this.complete();
    this.toolChange$.next('Circle');
    this.toolManager.setIrisModified(true);

    // reset circle stuff
    this.movingCircle = false;
    this.resizingCircle = false;
    this.circleCreated = false;
    this.circleHasSize = false;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.annotation = annotation;
    this.newCenterX = annotation.value.points[0].x;
    this.newCenterY = annotation.value.points[0].y;
    this.radius = annotation.value.data['radius'];
    this.annotation.id = annotation.id;

    this.pushCircle();
  }
}
