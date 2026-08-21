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
import { Path, Color, Point } from 'paper';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      Hold the button to draw
    </ng-container>
  `
})
export class PencilTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Pencil';
  static icon = 'fa-pencil-alt';
  static id = 'Pencil';
  static type: IrisToolType = IrisToolType.ANNOTATION;
  path: InstanceType<typeof Path>;
  smoothing: true;
  data = {
    type: 'pencil',
    points: []
  };

  annotation: Annotation;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
  }

  onMouseDown(evt) {
    this.annotation = new Annotation(PencilTool.id, AnnotationType.PENCIL, PencilTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        strokeColor: this.toolManager.drawColor,
        strokeWidth: this.toolManager.strokeWidth
      }
    };

    if (evt.event.shiftKey || evt.event.altKey) {
return false;
}

    if (evt.target.name == 'unrolled-raster') {
      return false;
    }

    this.path = new Path();
    this.path.strokeColor = new Color(this.toolManager.tempDrawColor);
    this.path.strokeWidth = this.toolManager.strokeWidth;
    this.path.add(evt.point);
  }

  onMouseDrag(evt) {
    if (evt.target.name == 'unrolled-raster') {
      return;
    }
    this.path.add(evt.point);
  }

  onMouseUp(event) {
    if (event.event.shiftKey || event.event.altKey) {
      return;
    }

    if (event.target.name == 'unrolled-raster') {
      return;
    }

    if (this.smoothing) {//reduce the points
      this.path.simplify();
    }

    const points = [];
    for (const seg of this.path.segments) {
      points.push({
        x: seg.point.x,
        y: seg.point.y
      });
    }
    this.annotation.value = {
      points,
      data: {
        strokeColor: this.toolManager.drawColor,
        strokeWidth: this.toolManager.strokeWidth
      }
    };

    this.completePencil(this.annotation);
  }

  completePencil(annotation: Annotation) {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const pencil = this.coreFunctions.drawPath(annotation.value.points, annotation.value.data['strokeColor'], annotation.value.data['strokeWidth']);
    pencil.data.annotation = annotation;

    const text = '(' + Math.floor(annotation.value.points[0]['x']) + ', ' + Math.floor(annotation.value.points[0]['y']) + ')';
    let label;

    if (this.toolManager.drawAnnotationLabels) {
      label = this.coreFunctions.drawAnnotationText(
        new Point(annotation.value.points[0]),
        text,
        this.toolManager.annotationColor,
        this.toolManager.annotationTextSize
      );
    }

    this.toolManager.checkingUserCreatedAnnotations(annotation, true, pencil, null, this.userParentGroupId, label, this.fromRedo);

    if (!this.fromRedo) {
      this.data.points.push({drawObj: this.path.segments, drawShape: 'pencil'});
      this._finalItem = this.path;

      this.removePath();
    }

    this.complete();
    this.toolChange$.next('Pencil');
    this.toolManager.setIrisModified(true);
  }

  removePath() {
    if (this.path != null) {
      this.path.remove();
      this.path = null;
    }
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;

    this.completePencil(annotation);
  }
}
