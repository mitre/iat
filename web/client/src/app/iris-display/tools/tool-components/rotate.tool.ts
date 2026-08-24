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
      Rotation {{ -1 * rotation }}
      <br>
      <div class="slider-wrapper d-flex">
        <span>180</span>
        <input type="range" class="custom-range mr-1 ml-1" min="-180" max="180" [(ngModel)]="rotation"
               (ngModelChange)="apply()">
        <span>-180</span>
      </div>
    </ng-container>
  `
})
export class RotateTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Rotate';
  static icon = 'fa-redo';
  static id = 'Rotate';
  static type: IrisToolType = IrisToolType.IMAGE;
  rotation = 0;
  previousRotation = 0;
  annotation: Annotation;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(RotateTool.id, AnnotationType.ROTATE, RotateTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {
        adjustment: 0
      }
    };
  }

  apply() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    this.annotation.value = {
      points: undefined,
      data: {adjustment: this.rotation}
    };

    const diff = this.rotation - this.previousRotation;
    this.coreFunctions.rotate(this.toolManager.iris, diff);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
      this.previousRotation = 0;
    }

    this.previousRotation = this.rotation;

    this.toolChange$.next('Rotate');
    this.toolManager.setIrisModified(true);
  }

  resetData() {
    super.resetData();

    this.rotation = 0;
    this.previousRotation = 0;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.rotation = annotation.value.data['adjustment'];
    this.annotation.id = annotation.id;

    this.apply();
  }
}


