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

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      <button class="btn btn-primary btn-xs" type="button" id="crop_btn" (click)="onClick()"
              [disabled]="isImageTooSmall()">{{cropped ? "Show Rectangle" : "Crop Image"}}</button>
    </ng-container>
  `
})
export class CropTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Crop';
  static icon = 'fa-crop';
  static id = 'Crop';
  static type: IrisToolType = IrisToolType.IMAGE;
  rect: any;
  previousPoint: any;
  cropped = false;

  rectangleColor = 'black';
  rectangleLineWidth = 2;
  minHeightWidth = 25;

  annotation: Annotation;
  topLeft: any;
  width: number;
  height: number;
  boundsLeft: number;
  boundsTop: number;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.createRectangle();
    this.toolManager.createClone();
    this.annotation = new Annotation(CropTool.id, AnnotationType.CROP, CropTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        fromPointX: undefined,
        fromPointY: undefined,
        width: undefined,
        height: undefined,
        boundsLeft: this.toolManager.iris.bounds.left,
        boundsTop: this.toolManager.iris.bounds.top
      }
    };
  }

  createRectangle() {
    const boxWidth = this.toolManager.iris.bounds.width / 2;
    const boxHeight = this.toolManager.iris.bounds.height / 2;

    const newFromX = this.toolManager.iris.bounds.center.x - boxWidth / 2;
    const newFromY = this.toolManager.iris.bounds.center.y - boxHeight / 2;

    const lineWidth = Math.max(this.toolManager.iris.width, this.toolManager.iris.height);
    this.rectangleLineWidth = (lineWidth / 500) + 1;
    const from = new Point(newFromX, newFromY);
    const to = new Point(newFromX + boxWidth, newFromY + boxHeight);

    this.rect = this.coreFunctions.drawRect(
      from,
      to,
      this.rectangleColor,
      this.rectangleLineWidth
    );
    this.rect.rotation = -1 * this.toolManager._project.view.rotation;
    this.cropped = false;
  }

  onClick() {
    if (!this.cropped) {
      const bottomLeft = this.rect.segments[0].point;
      this.topLeft = this.rect.segments[1].point;
      const topRight = this.rect.segments[2].point;

      this.width = this.topLeft.getDistance(topRight);
      this.height = this.topLeft.getDistance(bottomLeft);

      if (this.width < this.minHeightWidth || this.height < this.minHeightWidth) {
        window.alert('Crop box is too small. It must be at least 25 pixels x 25 pixels large.');
        return;
      }

      this.boundsLeft = this.toolManager.iris.bounds.left;
      this.boundsTop = this.toolManager.iris.bounds.top;

      this.completeCrop();
    } else {
      this.createRectangle();
    }
    this.toolManager.setIrisModified(true);
  }

  completeCrop() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const subRaster = this.coreFunctions.getSubRaster(this.topLeft.x,
      this.topLeft.y,
      this.width,
      this.height,
      this.toolManager.iris,
      this.toolManager.iris.bounds.left,
      this.toolManager.iris.bounds.top
    );
    this.annotation.value = {
      points: undefined,
      data: {
        fromPointX: this.topLeft.x,
        fromPointY: this.topLeft.y,
        width: this.width,
        height: this.height,
        boundsLeft: this.boundsLeft,
        boundsTop: this.boundsTop
      }
    };
    this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);

    //get rid of the rotation and basically restart
    const tempMatrix = (<any>this.toolManager._project.view).matrix;
    tempMatrix.append(tempMatrix.clone().invert());

    this.toolManager._iris.remove();
    this.toolManager._iris = subRaster;
    this.toolManager._iris.name = 'iris-raster';
    this.toolManager._iris.size = new paper.Size(this.width, this.height);
    this.toolManager._iris.position = this._layer.view.center;

    this.removeRectangle();

    this.toolChange$.next('Crop');
    super.complete();

    this.cropped = true;
    this.toolManager.setIrisModified(true);
  }

  isImageTooSmall() {
    const width = this.toolManager.iris.width;
    const height = this.toolManager.iris.height;

    if (width < this.minHeightWidth || height < this.minHeightWidth) {
      return true;
    }

    return false;
  }

  rectState = 'none';
  fromPoint: InstanceType<typeof Point>;
  toPoint: InstanceType<typeof Point>;

  onMouseDown(evt) {
    if (evt.event.shiftKey || evt.event.altKey || this.cropped) {
      return false;
    }

    const pt = evt.point;

    const bottomLeft = this.rect.segments[0].point;
    const topLeft = this.rect.segments[1].point;
    const topRight = this.rect.segments[2].point;
    const bottomRight = this.rect.segments[3].point;


    if (topLeft.isClose(pt, 10)) {
      this.rectState = 'resizing';
      this.toPoint = pt;
      this.fromPoint = bottomRight;
    } else if (topRight.isClose(pt, 10)) {
      this.rectState = 'resizing';
      this.toPoint = pt;
      this.fromPoint = bottomLeft;
    } else if (bottomRight.isClose(pt, 10)) {
      this.rectState = 'resizing';
      this.toPoint = pt;
      this.fromPoint = topLeft;
    } else if (bottomLeft.isClose(pt, 10)) {
      this.rectState = 'resizing';
      this.toPoint = pt;
      this.fromPoint = topRight;
    } else if (this.rect.contains(pt)) {
      this.rectState = 'moving';
      this.previousPoint = pt;
      document.body.style.cursor = 'grabbing';
    } else {
      document.body.style.cursor = 'default';
    }
  }

  onMouseUp(evt) {
    if (this.cropped) {
      return;
    }

    this.rectState = 'none';
  }

  onMouseMove(evt) {
    const pt = evt.point;
    const bottomLeft = this.rect.segments[0].point;
    const topLeft = this.rect.segments[1].point;
    const topRight = this.rect.segments[2].point;
    const bottomRight = this.rect.segments[3].point;

    if (topLeft.isClose(pt, 10) || bottomRight.isClose(pt, 10)) {
      document.body.style.cursor = 'nwse-resize';
    } else if (topRight.isClose(pt, 10) || bottomLeft.isClose(pt, 10)) {
      document.body.style.cursor = 'nesw-resize';
    } else if (this.rect.contains(pt)) {
      document.body.style.cursor = 'grab';
    } else{
      document.body.style.cursor = 'default';
    }
  }

  drawRect(from: InstanceType<typeof Point>, to: InstanceType<typeof Point>) {
    if (this.rect != undefined) {
      this.rect.remove();
    }

    from = from.transform((<any>this.toolManager._project.view).matrix);
    to = to.transform((<any>this.toolManager._project.view).matrix);
    this.rect = this.coreFunctions.drawRect(
      from,
      to,
      this.rectangleColor,
      this.rectangleLineWidth
    );

    const tempMatrix = (<any>this.toolManager._project.view).matrix.clone().invert();
    this.rect.transform(tempMatrix);

  }

  onMouseDrag(evt) {
    if (this.cropped) {
      return;
    }

    if (this.rectState === 'moving') {
      const newXPosition = this.rect.position.x + evt.point.x - this.previousPoint.x;
      const newYPosition = this.rect.position.y + evt.point.y - this.previousPoint.y;

      this.rect.position = [newXPosition, newYPosition];
      this.previousPoint = evt.point;
    } else if (this.rectState === 'resizing') {
      this.drawRect(this.fromPoint, evt.point);
    }
  }

  checkPointDistance(fromPoint, toPoint) {
    let move = true;
    const xDifference = fromPoint.x - toPoint.x;
    const yDifference = fromPoint.y - toPoint.y;
    if (Math.abs(xDifference) < 2) {
      move = false;
    }

    if (Math.abs(yDifference) < 2) {
      move = false;
    }

    return move;
  }

  removeRectangle() {
    if (this.rect != null) {
      this.rect.remove();
      this.rect = null;
    }
  }

  _destroy() {
    this.removeRectangle();
    super._destroy();
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.topLeft = {
      x: annotation.value.data['fromPointX'],
      y: annotation.value.data['fromPointY']
    };
    this.width = annotation.value.data['width'];
    this.height = annotation.value.data['height'];
    this.boundsLeft = annotation.value.data['boundsLeft'];
    this.boundsTop = annotation.value.data['boundsTop'];
    this.annotation.id = annotation.id;

    this.completeCrop();
  }
}
