/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { IrisDrawingTool, RequiredMembers, IrisToolType } from '../tool.base';
import { Component } from '@angular/core';
import { ToolManagerService } from '../../tool-manager.service';
import { Rectangle } from 'paper';
import { CoreFunctions } from '../../core/core-functions';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      Clip:
      <input title="Clip Limit" [(ngModel)]="clipFactor" type="number" min="0" max="1.0" step="0.01"/>

      <button class="btn btn-primary btn-sm" (click)="onApply()">Apply</button>
    </ng-container>
  `
})
export class AdaptiveContrastTool extends IrisDrawingTool implements RequiredMembers {
  name = 'AdaptiveContrast';
  static icon = 'fas fa-th';
  static id = 'AdaptiveContrast';
  static type: IrisToolType = IrisToolType.IMAGE;
  protected _layerKey = 'iris';

  userParentGroupId: string;

  coreFunctions: CoreFunctions = new CoreFunctions();

  annotation: Annotation;

  clipFactor = 0.0;
  tileDivisor = 8;
  fromRedo = false;

  constructor(
    protected toolManager: ToolManagerService
  ) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(AdaptiveContrastTool.id, AnnotationType.CLAHE, AdaptiveContrastTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        tileDivisor: this.tileDivisor,
        clipFactor: this.clipFactor
      }
    };
  }

  onApply() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    this.toolManager._iris.setImageData(this.toolManager._irisClone.getImageData(new Rectangle(0, 0, this.toolManager._iris.width, this.toolManager._iris.height)));
    this.clahe();

    this.toolManager.setIrisModified(true);
  }

  clahe() {
    this.annotation.value = {
      points: undefined,
      data: {
        tileDivisor: this.tileDivisor,
        clipFactor: this.clipFactor
      }
    };

    let imageData: ImageData = this.toolManager._irisClone.getImageData(new Rectangle(0, 0, this.toolManager._iris.width, this.toolManager._iris.height));
    imageData = this.coreFunctions.clahe(imageData, this.tileDivisor, this.clipFactor);
    this.toolManager._iris.setImageData(imageData);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }
  }

  resetData() {
    super.resetData();

    this.clipFactor = 0.0;
    this.tileDivisor = 8;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.clipFactor = annotation.value.data['clipFactor'];
    this.tileDivisor = annotation.value.data['tileDivisor'];
    this.annotation.id = annotation.id;
    this.onApply();
  }
}
