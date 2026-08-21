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
import { ToolManagerService } from '../../tool-manager.service';
import { Point } from 'paper';
import { Annotation, AnnotationType } from '../../../types/annotation';
import { CoreFunctions } from '../../core/core-functions';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
    </ng-container>
  `
})
export class PointTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Point';
  static icon = 'fa-map-pin';
  static id = 'Point';
  static type: IrisToolType = IrisToolType.ANNOTATION;
  annotationType = AnnotationType.POINT;
  pointType = 'circle';
  radius = 3;
  strokeColor = '#127bdc';
  fillColor = '#333333';
  coreFunctions: CoreFunctions = new CoreFunctions();

  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
    this.toolManager.createClone();
  }

  onMouseDown(evt) {
    if (evt.event.shiftKey || evt.event.altKey) {
      return false;
    }

    if (evt.target.name == 'unrolled-raster') {
      return;
    }

    this.completePoint(evt.point);
  }

  completePoint(eventPoint, annotationId?) {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const point = this.coreFunctions.drawCircle(eventPoint, this.radius, this.toolManager.drawColor, this.toolManager.fillColor);
    const annotation = new Annotation(PointTool.id, AnnotationType.POINT, PointTool.icon);

    const points = [];
    points.push({
      x: point.position.x,
      y: point.position.y
    });
    annotation.value = {
      points,
      data: {
        radius: this.radius,
        strokeColor: this.toolManager.drawColor,
        strokeWidth: 1,
        fillColor: this.toolManager.fillColor
      }
    };
    if (!!annotationId) {
      annotation.id = annotationId;
    }


    const text = '(' + Math.floor(annotation.value.points[0].x) + ', ' + Math.floor(annotation.value.points[0].y) + ')';
    let label;

    if (this.toolManager.drawAnnotationLabels) {
      label = this.coreFunctions.drawAnnotationText(
        new Point(annotation.value.points[0].x, annotation.value.points[0].y),
        text,
        this.toolManager.annotationColor,
        this.toolManager.annotationTextSize
      );
    }

    this.toolManager.checkingUserCreatedAnnotations(annotation, true, point, null, this.userParentGroupId, label, this.fromRedo);

    this.toolChange$.next('Pencil');
    this.toolManager.setIrisModified(true);
    this._finalItem = point;
    this.complete();
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;

    this.completePoint(annotation.value.points[0], annotation.id);
  }
}
