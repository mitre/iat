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
import { IrisDrawingTool, IrisToolType, RequiredMembers } from '../tool.base';
import { ToolManagerService } from '../../tool-manager.service';
import { ColorChannel } from '../../core/core-functions';
import { Annotation, AnnotationType } from '../../../types/annotation';

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="radio" name="colors" id="red-{{idSuffix}}" (click)="onClick('Red')">
        <label class="form-check-label" for="red-{{idSuffix}}">Red</label>
      </div>
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="radio" name="colors" id="green-{{idSuffix}}" (click)="onClick('Green')">
        <label class="form-check-label" for="green-{{idSuffix}}">Green</label>
      </div>
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="radio" name="colors" id="blue-{{idSuffix}}" (click)="onClick('Blue')">
        <label class="form-check-label" for="blue-{{idSuffix}}">Blue</label>
      </div>
    </ng-container>
  `
})
export class ChannelTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Channel';
  static icon = 'fa-chart-bar';
  static id = 'Channel';
  static type: IrisToolType = IrisToolType.IMAGE;
  annotation: Annotation = new Annotation(ChannelTool.id, AnnotationType.CHANNEL, ChannelTool.icon);
  idSuffix: string;
  beforeId: string;
  parentAnno: Annotation;
  isOldAnnot: boolean;
  fromRedo = false;

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onToolInit() {
    this.isOldAnnot = false;
    const channelAnnotations = this.toolManager.getAnnotations(AnnotationType.CHANNEL);

    if (channelAnnotations.length != 0) {
      this.isOldAnnot = true;
      this.annotation = channelAnnotations[0];

      [this.parentAnno, this.beforeId] = this.toolManager.preparingRemoval(this.annotation);

      this.toolManager.removeItem(channelAnnotations[0].id, true);
    }
    this.toolManager.createClone();
    this.idSuffix = this.toolManager.sourceName;

    if (channelAnnotations.length != 0) {
      this.onClick(this.annotation.value.data['channel']);
    }
  }

  onClick(color) {
    if (this.fromRedo) {
      this.toolManager.createClone();
    }
    this.userParentGroupId = this.toolManager.userParentGroupId;
    const channelAnnotations = this.toolManager.getAnnotations(AnnotationType.CHANNEL);
    if (channelAnnotations.length != 0) {
      this.annotation = channelAnnotations[0];
    } else {
      this.annotation = new Annotation(ChannelTool.id, AnnotationType.CHANNEL, ChannelTool.icon);
    }

    const origData = this.toolManager._irisClone.getImageData(null);
    let colorChannel = ColorChannel.BLUE;
    if (color == 'Red') {
      colorChannel = ColorChannel.RED;
    } else if (color == 'Green') {
      colorChannel = ColorChannel.GREEN;
    } else if (color == 'Blue') {
      colorChannel = ColorChannel.BLUE;
    }

    this.annotation.value = {
      points: undefined,
      data: {channel: color}
    };

    const imageData = this.coreFunctions.changeColors(colorChannel, origData);
    this.toolManager._iris.setImageData(imageData);


    if (!this.toolManager.annotationExists(this.annotation.id) && this.isOldAnnot) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, this.beforeId, this.parentAnno.id, null, this.fromRedo);
    } else if (!this.toolManager.annotationExists(this.annotation.id)) {
      this.toolManager.checkingUserCreatedAnnotations(this.annotation, true, null, null, this.userParentGroupId, null, this.fromRedo);
    }

    this.toolChange$.next('Channel');
    this.toolManager.setIrisModified(true);
  }

  reAddData(annotation) {
    super.reAddData(annotation);

    this.fromRedo = true;
    const color = annotation.value.data['channel'];

    this.onClick(color);
  }
}
