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
      <button class="btn btn-primary" type="button" id="mirrot_btn" (click)="onClick()">Flip</button>
    </ng-container>
  `
})
export class MirrorTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Mirror';
  static icon = 'fa-exchange-alt';
  static id = 'Mirror';
  static type: IrisToolType = IrisToolType.IMAGE;
  annotation: Annotation;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(MirrorTool.id, AnnotationType.MIRROR, MirrorTool.icon);
  }

  onClick(inputPosition?, annotationId?) {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    let position;
    if (!!inputPosition) {
      position = inputPosition;
    }

    const mirrorAnnotations = this.toolManager.getAnnotations(AnnotationType.MIRROR);
    if (mirrorAnnotations.length != 0) {
      this.annotation = mirrorAnnotations[0];
      position = this.annotation.value.data['adjustment'];
    } else {
      position = 'Normal';
      this.annotation = new Annotation(MirrorTool.id, AnnotationType.MIRROR, MirrorTool.icon);
    }

    if (position === 'Flipped') {
      position = 'Normal';
    } else {
      position = 'Flipped';
    }

    this.annotation.value = {
      points: undefined,
      data: {
        adjustment: position
      }
    };

    this.coreFunctions.mirrorImage(this.toolManager.iris);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    this.toolChange$.next('Mirror');
    this.toolManager.setIrisModified(true);
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    const position = annotation.value.data['adjustment'];

    this.onClick(position, annotation.id);
  }

}
