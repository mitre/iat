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
import { Path, Point, Color } from 'paper';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      <div *ngIf="stagesComplete == 0">
        Click to draw points for the upper eyelid. Shift-click to continue.
        <button class="btn btn-danger btsn-xs" type="button" autocomplete="off" (click)="completeCurrentAnnotation()">
          Next
        </button>
      </div>
      <div *ngIf="stagesComplete == 1">
        Click to draw points for the lower eyelid. Shift-click to continue.
        <button class="btn btn-danger btsn-xs" type="button" autocomplete="off" (click)="completeCurrentAnnotation()">
          Next
        </button>
      </div>
      <div *ngIf="stagesComplete >=2">
        Completed Eyelid Annotations.
        Press 'Redo Eyelid Annotations' to re-annotate the eyelids.
        <button class="btn btn-danger btsn-xs" type="button" autocomplete="off" (click)="resetEyelidAnnotations()">Redo
          Eyelid Annotations
        </button>
      </div>
    </ng-container>
  `
})
export class EyelidTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Eyelid';
  static icon = 'fa-smile-beam';
  static id = 'Eyelid';
  static type: IrisToolType = IrisToolType.ANNOTATION;
  path: InstanceType<typeof Path>;
  smoothing: true;
  cur_point: InstanceType<typeof Point>;
  move_point: InstanceType<typeof Point>;
  move_pointAdded: boolean;
  point_radius = 4;
  stagesComplete = 0;
  circles = [];
  protected canvas: HTMLCanvasElement;

  annotation: Annotation;
  points = [];
  fromRedo = false;

  data = {
    polygons: []
  };

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(EyelidTool.id, AnnotationType.EYELID, EyelidTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        strokeColor: this.config['drawColor'] || this.toolManager.drawColor,
        strokeWidth: this.toolManager.strokeWidth,
        eyelidType: undefined
      }
    };

    const eyelidAnnotations = this.toolManager.getAnnotations(AnnotationType.EYELID);
    this.stagesComplete = eyelidAnnotations.length;
    this.canvas = this.toolManager._project.view.element;
    this.canvas.addEventListener('mouseleave', this.onMouseLeave.bind(this));
  }

  onMouseMove(event) {
    if (event.event.shiftKey || event.event.altKey || !this.isEventInCanvas(event)
      || event.target.name == 'unrolled-raster' || this.stagesComplete >= 2) {
      return;
    }

    if (this.move_point == null) {
      this.move_point = new Point(0, 0);
    }

    this.move_point.x = event.point.x;
    this.move_point.y = event.point.y;

    if (this.path != null) {
      if (this.move_pointAdded) {
        this.path.segments.pop();
      }

      this.path.add(this.move_point);
      this.move_pointAdded = true;

      const distance = event.point.getDistance(this.path.segments[0].point);
      if (distance < 5) {
        this.path.strokeColor = new Color(this.toolManager.drawColor);
      } else {
        this.path.strokeColor = new Color(this.toolManager.tempDrawColor);
      }
    }
  }

  onMouseLeave(event) {
    if (this.path != null) {
      if (this.move_pointAdded) {
        this.path.segments.pop();
        this.move_pointAdded = false;

      }
    }
  }

  onMouseUp(event) {
    if (event.event.shiftKey) {
      if (this.move_pointAdded) {
        this.path.segments.pop();
        this.move_pointAdded = false;
      }
      this.completeCurrentAnnotation();
      return;
    }

    if (event.event.altKey || event.target.name == 'unrolled-raster'
      || !this.isEventInCanvas(event) || this.stagesComplete >= 2) {
      return;
    }

    this.cur_point = event.point;

    if (this.path == null) {
      this.path = new Path();
      this.toolManager.tempToolLayer.addChild(this.path);
    }

    this.path.strokeColor = new Color(this.toolManager.tempDrawColor);
    this.path.strokeWidth = this.toolManager.strokeWidth;

    if (this.move_pointAdded) {
      this.path.segments.pop();
      this.move_pointAdded = false;
    }

    this.path.add(this.cur_point);

    this.drawCircle(this.cur_point);
    this.toolChange$.next('Eyelid');
  }

  isEventInCanvas(event) {
    return true;
  }

  completeCurrentAnnotation() {
    this.annotation = new Annotation(EyelidTool.id, AnnotationType.EYELID, EyelidTool.icon);
    this.points = [];
    for (const seg of this.path.segments) {
      this.points.push({x: seg.point.x, y: seg.point.y});
    }
    this.annotation.value = {
      points: this.points,
      data: {
        strokeColor: this.config['drawColor'] || this.toolManager.drawColor,
        strokeWidth: this.toolManager.strokeWidth,
        eyelidType: this.stagesComplete == 0 ? 'Upper' : 'Lower'
      }
    };

    this.completeEyelid();
  }

  completeEyelid() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const polygon = this.coreFunctions.drawPolygon(this.annotation.value.points, this.annotation.value.data['strokeColor'],
      this.annotation.value.data['strokeWidth'], this.annotation.value.data['fillColor'], this.annotation.value.data['fillAlpha'], false);
    polygon.data.annotation = this.annotation;
    polygon.closed = false;

    const text = '(' + Math.floor(this.points[0]['x']) + ', ' + Math.floor(this.points[0]['y']) + ')';
    let label;

    if (this.toolManager.drawAnnotationLabels) {
      label = this.coreFunctions.drawAnnotationText(
        new Point(this.points[0]),
        text,
        this.toolManager.annotationColor,
        this.toolManager.annotationTextSize
      );
    }

    this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, polygon, null, this.userParentGroupId, label, this.fromRedo);

    this.removePath();
    this.path = null;
    this.removeCircles();
    this.removePath();
    this.toolChange$.next('Eyelid');
    this.toolManager.setIrisModified(true);
    this.stagesComplete++;

    if (this.stagesComplete >= 2) {
      this.completeAnnotating();
    }
  }

  completeAnnotating() {
    this.toolChange$.next('Eyelid');
    this.toolManager.setIrisModified(true);
    this.complete();
  }

  resetEyelidAnnotations() {
    this.toolManager.removeAnnotations(AnnotationType.EYELID);
    this.stagesComplete = 0;
    this.removeCircles();
    this.removePath();
    this.toolChange$.next('Eyelid');
    this.toolManager.setIrisModified(true);
  }


  drawCircle(point) {
    const zoom = this.toolManager._iris.view.zoom;
    let rad = this.point_radius / zoom;
    if (rad < 1) {
      rad = 1;
    } else if (rad > this.point_radius) {
      rad = this.point_radius;
    }

    const myCircle = new Path.Circle({
      center: this.cur_point,
      radius: rad
    });
    myCircle.strokeColor = new Color(this.toolManager.tempDrawColor);
    myCircle.fillColor = myCircle.strokeColor;
    this.toolManager.tempToolLayer.addChild(myCircle);
    this.circles.push(myCircle);
  }

  removeCircles() {
    for (const item of this.circles) {
      item.remove();
    }
    this.circles = [];
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
    this.points = annotation.value.points;
    this.annotation = annotation;

    this.completeEyelid();
  }

}
