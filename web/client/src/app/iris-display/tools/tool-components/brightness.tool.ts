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
  selector: 'brightness-tool',
  template: `
    <ng-container *ngIf="_isActive">
      Brightness: <strong>{{ brightness }}</strong>
      <div class="slider-wrapper">
        <label class="pull-left">1%</label>
        <input type="range" class="custom-range" min="1" max="100" [(ngModel)]="brightness"
               (ngModelChange)="onChange()">
        <label class="text-right">100%</label>
      </div>
    </ng-container>
  `,
  styles: ['div.slider-wrapper { display: flex }']
})
export class BrightnessTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Brightness';          // name cannot be static, conflicts with Function.name
  static icon = 'fa-sun';
  static id = 'Brightness';
  static type: IrisToolType = IrisToolType.IMAGE;
  brightness = 50;

  _updating = false;                 // reference to the active Iris Raster

  protected _layerKey = 'iris';
  annotation: Annotation;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.toolManager.createClone();
    this.annotation = new Annotation(BrightnessTool.id, AnnotationType.BRIGHTNESS, BrightnessTool.icon);
    this.annotation.value = {
      points: undefined,
      data: {adjustment: 0}
    };
  }

  onChange() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    if (this._updating) {
      return false;
    }

    this.toolChange$.next('Brightness');

    this._updating = true;

    let imageData = this.toolManager._irisClone.getImageData(null);

    // brightness range [-100, 100] (per algorithm used)
    const adjustment = (this.brightness * 2) - 100;

    //we hold onto the reference of the annotation for the duration that the user wishes to change the
    //brightness
    this.annotation.value.data['adjustment'] = adjustment;

    imageData = this.coreFunctions.adjustBrightness(imageData, adjustment);
    this.toolManager.iris.setImageData(imageData);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    this._updating = false;
    this.toolManager.setIrisModified(true);
  }

  resetData() {
    super.resetData();

    this.toolManager.userParentGroup = undefined;

    this.brightness = 50;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.brightness = (+annotation.value.data['adjustment'] + 100) / 2;
    this.annotation.id = annotation.id;

    this.onChange();
  }
}
