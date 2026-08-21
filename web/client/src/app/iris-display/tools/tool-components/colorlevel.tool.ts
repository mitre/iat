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
      <div class="d-inline-block">
        <div class="d-inline-block">
          <div class="form-inline">
            <label for="red-{{idSuffix}}" class="d-block">Red:<input id="red-{{idSuffix}}" class="small ml-1"
                                                                     type="text" size="3" [(ngModel)]="colorLevel.red"
                                                                     (ngModelChange)="changeColors()"></label>
          </div>
          <div class="d-inline-flex p-1">
            <span>-255</span>
            <input id="red-{{idSuffix}}" type="range" class="custom-range" min="-255" max="255"
                   [(ngModel)]="colorLevel.red" (ngModelChange)="changeColors()">
            <span>255</span>
          </div>
        </div>
        <div class="d-inline-block">
          <div class="form-inline">
            <label for="green-{{idSuffix}}">Green:<input id="green-{{idSuffix}}" class="small ml-1" type="text" size="3"
                                                         [(ngModel)]="colorLevel.green"
                                                         (ngModelChange)="changeColors()"></label>
          </div>
          <div class="d-inline-flex ml-auto p-1">
            <span>-255</span>
            <input id="green-{{idSuffix}}" type="range" class="custom-range" min="-255" max="255"
                   [(ngModel)]="colorLevel.green" (ngModelChange)="changeColors()">
            <span>255</span>
          </div>
        </div>
        <div class="d-inline-block">
          <div class="form-inline">
            <label for="blue-{{idSuffix}}">Blue:<input id="blue-{{idSuffix}}" class="small ml-1" type="text" size="3"
                                                       [(ngModel)]="colorLevel.blue"
                                                       (ngModelChange)="changeColors()"></label>
          </div>
          <div class="d-inline-flex ml-auto p-1">
            <span>-255</span>
            <input id="blue-{{idSuffix}}" type="range" class="custom-range" min="-255" max="255"
                   [(ngModel)]="colorLevel.blue" (ngModelChange)="changeColors()">
            <span>255</span>
          </div>
        </div>
      </div>
      <button class="btn btn-primary btn-xs ml-2" type="button" id="clear_btn-{{idSuffix}}" (click)="onClear()">Clear
      </button>
    </ng-container>
  `, styles: []
})
export class ColorLevelTool extends IrisDrawingTool implements RequiredMembers {
  name = 'ColorLevel';
  static icon = 'fa-chart-pie';
  static id = 'ColorLevel';
  static type: IrisToolType = IrisToolType.IMAGE;
  colorLevel = {red: 0, green: 0, blue: 0};
  annotation: Annotation;
  idSuffix: string;
  fromRedo = false;


  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.idSuffix = this.toolManager.sourceName;
    this.toolManager.createClone();
    this.annotation = new Annotation(ColorLevelTool.id, AnnotationType.COLOR_LEVEL, ColorLevelTool.icon);
  }

  onClear() {
    this.colorLevel.red = 0;
    this.colorLevel.green = 0;
    this.colorLevel.blue = 0;
    this.changeColors();
    this.toolManager.setIrisModified(false);
  }

  changeColors() {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const imageData = this.coreFunctions.addColor({
      red: this.colorLevel.red,
      green: this.colorLevel.green,
      blue: this.colorLevel.blue
    }, this.toolManager._irisClone.getImageData(null));

    this.annotation.value = {
      points: undefined,
      data: {
        red: this.colorLevel.red,
        green: this.colorLevel.green,
        blue: this.colorLevel.blue
      }
    };

    this.toolManager.iris.setImageData(imageData);

    if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    this.toolManager.setIrisModified(true);
  }

  resetData() {
    super.resetData();

    this.colorLevel.red = 0;
    this.colorLevel.green = 0;
    this.colorLevel.blue = 0;
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    this.colorLevel.red = annotation.value.data['red'];
    this.colorLevel.green = annotation.value.data['green'];
    this.colorLevel.blue = annotation.value.data['blue'];
    this.annotation.id = annotation.id;

    this.changeColors();
  }

}
