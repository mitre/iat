/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, AfterViewInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-paper-canvas',
  template: `
    <canvas #canvas [width]="width" [height]="height" resize="true" (click)="onCanvasClick()"></canvas>`,
  styles: [':host { display: block; }', ':host[hidden=""] { display: none !important; }', 'canvas { text-align: left; }',
    ':host.show-boundary { border: 1px solid #dddddd; background-color: #f5f5f5; background-image: linear-gradient(to bottom,#f5f5f5 0,#e8e8e8 100%); }']
})
export class PaperCanvasComponent implements AfterViewInit {
  @Input() width;
  @Input() height;
  @ViewChild('canvas', {static: true}) canvas;
  @Output() canvasInit: EventEmitter<any> = new EventEmitter<any>(true);

  constructor() {
  }

  ngAfterViewInit() {
    this.canvasInit.emit(this.canvas);
  }

  // this method is purposely a no-op
  // listening to click events on the canvas element forces Angular's change detection to fire when using canvas tools
  onCanvasClick() {
  }
}
