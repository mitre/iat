/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/
import { Component, OnInit } from '@angular/core';
import { IrisDisplayComponent } from '../iris-display/iris-display.component';
import { AnnotationType } from '../types/annotation';
import { ToolManagerService } from '../iris-display/tool-manager.service';
import { ReviewContentService } from '../review/review.service';

@Component({
  selector: 'app-dual-pdm',
  templateUrl: './dual-pdm.component.html',
  styleUrls: ['./dual-pdm.component.css']
})
export class DualPDMComponent implements OnInit {

  static icon = 'all_out';
  static id = 'Dual Pupil Dilation Comparison';
  static type: AnnotationType = AnnotationType.DUAL_PDM;
  static iconType = "material";

  onSetButtonClicked = false;

  constructor(private irisDisplayComponent: IrisDisplayComponent, private toolManager: ToolManagerService, public reviewService: ReviewContentService) {}

  ngOnInit(): void {
    // Initialize component logic if needed
  }

  onSet() {
    this.irisDisplayComponent.getPdmResponses(DualPDMComponent.id, DualPDMComponent.type, DualPDMComponent.icon, DualPDMComponent.iconType, true);
    this.onSetButtonClicked = true;
  }

  onUnset() {
    this.toolManager.removeItem(this.irisDisplayComponent.pdmGroupAnnotation.id, false, false, false);
    this.onSetButtonClicked = false;
  }
}
