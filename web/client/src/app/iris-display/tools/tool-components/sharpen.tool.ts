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
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      Sharpen: <strong>{{ sharpen }}</strong>
      <div class="slider-wrapper">
        <label class="pull-left">1</label>
        <input type="range" class="custom-range" min="1" max="20" [(ngModel)]="sharpen" (ngModelChange)="apply()">
        <label class="text-right">20</label>
      </div>
    </ng-container>
  `,
  styles: ['div.slider-wrapper { display: flex }', 'button.apply-btn { margin-top: -8px; margin-left: 8px; }']
})
export class SharpenTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Sharpen';             // name cannot be static, conflicts with Function.name
  static icon = 'fa-sliders-h';
  static id = 'Sharpen';
  static type: IrisToolType = IrisToolType.IMAGE;
  sharpen = 1;

  annotation: Annotation;
  fromRedo = false;

  _updating = false;

  protected _layerKey = 'iris';

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(SharpenTool.id, AnnotationType.SHARPEN, SharpenTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        adjustment: undefined
      }
    };
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
    this.annotation.value = {
      points: undefined,
      data: {
        adjustment: this.sharpen
      }
    };

    const imageData = this.coreFunctions.sharpen(this.toolManager._irisClone.getImageData(null), this.sharpen);
    this.toolManager._iris.setImageData(imageData);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    this._updating = false;
    this.toolChange$.next('Sharpen');
    this.toolManager.setIrisModified(true);
  }

  resetData() {
    this.sharpen = 1;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.sharpen = annotation.value.data['adjustment'];
    this.annotation.id = annotation.id;

    this.apply();
  }

}
