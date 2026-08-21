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
import { Size } from 'paper';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      Scale: <strong>{{ scale }}</strong> - Image Dimensions: (W: <strong>{{width}}</strong>,
      H:<strong>{{height}}</strong>)
      <div class="slider-wrapper">
        <label class="pull-left">.1</label>
        <input type="range" class="custom-range ml-1 mr-1" min=".1" max="5" step=".1" [(ngModel)]="scale"
               (ngModelChange)="apply()">
        <label class="text-right">5</label>
      </div>
    </ng-container>
  `,
  styles: ['div.slider-wrapper { display: flex }']
})
export class ScaleTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Scale';
  static icon = 'fa-object-ungroup';
  static id = 'Scale';
  static type: IrisToolType = IrisToolType.IMAGE;
  scale = 1;
  height = 1;
  width = 1;
  annotation: Annotation;

  _updating = false;
  _originalHeight: number;
  _originalWidth: number;
  fromRedo = false;

  protected _layerKey = 'iris';

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this._originalWidth = this.toolManager._irisClone.width;
    this._originalHeight = this.toolManager._irisClone.height;
    this.width = this.toolManager._irisClone.width;
    this.height = this.toolManager._irisClone.height;
    this.annotation = new Annotation(ScaleTool.id, AnnotationType.RESIZE, ScaleTool.icon);
  }

  apply() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    if (this._updating) {
      return false;
    }

    this._updating = true;

    // reset iris data to original values
    this.toolManager._iris.size = new Size(this._originalWidth, this._originalHeight);
    this.toolManager.iris.setImageData(this.toolManager._irisClone.getImageData(null));
    // check if the new width or new height is less than 1
    if ((this._originalWidth * this.scale) < 1) {
      this.width = 1;
    }
    if ((this._originalHeight * this.scale) < 1) {
      this.height = 1;
    }

    if ((this._originalHeight * this.scale) > 1 && (this._originalWidth * this.scale) > 1) {
      // resize to new values
      if (!this.fromRedo) {
        this.width = this._originalWidth * this.scale;
        this.height = this._originalHeight * this.scale;
      }
    }

    this.coreFunctions.resize(this.toolManager._iris, this.width, this.height);

    this.annotation.value = {
      points: undefined,
      data: {
        width: this.width,
        height: this.height
      }
    };

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    // update view information
    this._layer.view.update();

    this._updating = false;
    this.toolChange$.next('Scale');
    this.toolManager.setIrisModified(true);

  }

  resetData() {
    super.resetData();

    this.toolManager.createClone();
    this._originalWidth = this.toolManager._irisClone.width;
    this._originalHeight = this.toolManager._irisClone.height;

    this.width = this._originalWidth;
    this.height = this._originalHeight;
    this.width = this._originalWidth;

    this.scale = 1;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.height = annotation.value.data['height'];
    this.width = annotation.value.data['width'];
    this.annotation.id = annotation.id;

    this.apply();
  }
}
