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
      Draw points for the polygon. Select first point to complete.
    </ng-container>
  `
})
export class PolygonTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Polygon';
  static icon = 'fa-cube';
  static id = 'Polygon';
  static type: IrisToolType = IrisToolType.ANNOTATION;
  path: InstanceType<typeof Path>;
  smoothing: true;
  cur_point: InstanceType<typeof Point>;
  move_point: InstanceType<typeof Point>;
  move_pointAdded: boolean;
  point_radius = 4;
  circles = [];
  data = {
    polygons: []
  };

  annotation: Annotation;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
    this.toolManager.createClone();
  }

  onMouseMove(event) {
    this.annotation = new Annotation(PolygonTool.id, AnnotationType.POLYGON, PolygonTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        strokeColor: this.config['drawColor'] || this.toolManager.drawColor,
        fillColor: this.config['fillColor'] || this.toolManager.fillColor,
        strokeWidth: this.toolManager.strokeWidth,
        fillAlpha: this.data['fillAlpha'] || this.defaults['fillAlpha']
      }
    };

    if (event.event.shiftKey || event.event.altKey) {
      return;
    }

    if (event.target.name == 'unrolled-raster') {
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

  onMouseUp(event) {
    if (event.event.shiftKey || event.event.altKey) {
      return;
    }

    if (event.target.name == 'unrolled-raster') {
      return;
    }

    this.cur_point = event.point;

    if (this.path == null) {
      this.path = new Path();
      this.toolManager.tempToolLayer.addChild(this.path);
    }

    this.path.strokeColor = new Color(this.toolManager.tempDrawColor);
    this.path.strokeWidth = this.toolManager.strokeWidth;
    if ((this.config['fillColor'] && this.config['fillColor'].length > 0) || this.toolManager.fillColor) {
      this.path.fillColor = new Color(this.config['fillColor'] || this.toolManager.fillColor);
      this.path.fillColor.alpha = this.data['fillAlpha'] || this.defaults['fillAlpha'];
    }

    if (this.move_pointAdded) {
      this.path.segments.pop();
      this.move_pointAdded = false;
    }

    //check for complete
    if (this.path.segments.length > 1) {
      const distance = event.point.getDistance(this.path.segments[0].point);
      if (distance < 5) {
        this.cur_point = this.path.segments[0].point;
        this.path.add(this.cur_point);
        this.path.closed = true;

        const points = [];
        for (const seg of this.path.segments) {
          points.push({x: seg.point.x, y: seg.point.y});
        }
        this.annotation.value = {
          points,
          data: {
            strokeColor: this.config['drawColor'] || this.toolManager.drawColor,
            fillColor: this.config['fillColor'] || this.toolManager.fillColor,
            strokeWidth: this.toolManager.strokeWidth,
            fillAlpha: this.data['fillAlpha'] || this.defaults['fillAlpha']
          }
        };

        this.completePolygon(points);
        return;
      }
    }

    this.path.add(this.cur_point);

    this.drawCircle(this.cur_point);
    this.toolChange$.next('Polygon');
  }

  completePolygon(points) {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const polygon = this.coreFunctions.drawPolygon(this.annotation.value.points, this.annotation.value.data['strokeColor'], this.annotation.value.data['strokeWidth'], this.annotation.value.data['fillColor'], this.annotation.value.data['fillAlpha']);
    polygon.data.annotation = this.annotation;

    const text = '(' + Math.floor(points[0]['x']) + ', ' + Math.floor(points[0]['y']) + ')';
    let label;

    if (this.toolManager.drawAnnotationLabels) {
      label = this.coreFunctions.drawAnnotationText(
        new Point(points[0]),
        text,
        this.toolManager.annotationColor,
        this.toolManager.annotationTextSize
      );
    }

    this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, polygon, null, this.userParentGroupId, label, this.fromRedo);

    this.removePath();
    this.complete();
    this.path = null;
    this.removeCircles();
    this.removePath();
    this.toolChange$.next('Polygon');
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
    this.annotation = annotation;

    this.completePolygon(annotation.value.points);
  }

}
