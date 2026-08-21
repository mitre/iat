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

@Component({
  template: `
    <ng-container *ngIf="_isActive">
      <button class="btn btn-primary btn-xs" type="button" id="zoomin_btn" (click)="onZoomIn()">Zoom In</button>
      <button class="btn btn-primary btn-xs" type="button" id="zoomout_btn" (click)="onZoomOut()">Zoom out</button>
      <button class="btn btn-secondary btn-xs" type="button" id="resetzoom_btn" (click)="resetZoom()">Reset Zoom</button>
    </ng-container>
  `
})
export class ZoomTool extends IrisDrawingTool implements RequiredMembers {
  name = 'Zoom';
  static icon = 'fa-search';
  static id = 'Zoom';
  static type: IrisToolType = IrisToolType.IMAGE;
  default_zoom_factor = 1.05;
  startingZoom = 1.0; // Define the starting zoom level
  data = {
    zoom: 0
  };

  constructor(protected toolManager: ToolManagerService) {
    super(toolManager);
  }

  onZoomIn() {
    this.toolManager._project.view.zoom = parseFloat((this.toolManager._project.view.zoom * this.default_zoom_factor).toFixed(2));
    this.data.zoom = this.toolManager._project.view.zoom;
  }

  onZoomOut() {
    this.toolManager._project.view.zoom = parseFloat((this.toolManager._project.view.zoom / this.default_zoom_factor).toFixed(2));
    this.data.zoom = this.toolManager._project.view.zoom;
  }

  resetZoom() {
    // Reset the zoom level to the starting zoom
    this.toolManager._project.view.zoom = this.startingZoom;
    this.data.zoom = this.toolManager._project.view.zoom;
  }

}