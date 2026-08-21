/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/
import { Component} from '@angular/core';
import { IrisDisplayComponent } from '../iris-display/iris-display.component';
import { PupilDilationToolService } from './pupil-dilation-tool.service';
import { AnnotationType } from '../types/annotation';
import { MatDialog } from '@angular/material/dialog';
import { ToolManagerService } from '../iris-display/tool-manager.service';
import { PdmConfirmationDialogComponent } from '../pdm-confirmation-dialog/pdm-confirmation-dialog.component';

@Component({
  selector: 'app-pupil-dilation-tool',
  templateUrl: './pupil-dilation-tool.component.html',
  styleUrls: ['./pupil-dilation-tool.component.css']
})
export class PupilDilationToolComponent{
  static icon = 'all_out';
  static id = 'Pupil Dilation';
  static type: AnnotationType = AnnotationType.PDM;
  static iconType = "material";

  userParentGroupId: string;

  alphaScore = 0.45;
  fromRedo = false;
  autoCorrect = false;
  minValue = 0.2;
  maxValue = 0.7;

  constructor(
    public irisDisplayComponent: IrisDisplayComponent,
    public pdmToolService: PupilDilationToolService,
    private toolManager: ToolManagerService,
    private matDialogService: MatDialog,
  ) {
  }

  onApply() {
    this.autoCorrectValue();
    this.irisDisplayComponent.getPdmResponses(PupilDilationToolComponent.id, PupilDilationToolComponent.type, PupilDilationToolComponent.icon, PupilDilationToolComponent.iconType, false, this.alphaScore.toString());
  }

  autoCorrectValue() {
    this.autoCorrect = true;
    // Automatically corrects any number above 0.9 to 0.9 and any number below 0.2 to 0.2.
    if (this.alphaScore > 0.9) {
      this.alphaScore = 0.9;
    } else if (this.alphaScore < 0.2) {
      this.alphaScore = 0.2
    }

    var alphaString = this.alphaScore.toString();
    if (alphaString.startsWith(".")) {
      alphaString = "0" + alphaString;
      this.alphaScore = parseFloat(alphaString);
    }
  }

  onClose() {
    this.matDialogService.open(PdmConfirmationDialogComponent).afterClosed().subscribe(selectedChoice => {
     switch (selectedChoice) {
      case 'confirm':
        this.onConfirm();
        break;
      case 'cancel':
        this.onCancel();
        break;
      case 'back':
        // Do nothing since the user wants to keep editing the image.
        break;
     }
    });
  }

  // This should keep the applied pdm image and close the tool.
  onConfirm() {
    this.irisDisplayComponent.inPdmMode = false;
    this.irisDisplayComponent.toggleToolBar(false, false);
  }

  // This should remove the applied pdm image and close the tool.
  onCancel() {
    this.irisDisplayComponent.inPdmMode = false;
    if (this.irisDisplayComponent.pdmGroupAnnotation) {
      this.toolManager.removeItem(this.irisDisplayComponent.pdmGroupAnnotation.id, false, false, false);
    }
    this.irisDisplayComponent.toggleToolBar(false, false);
  }

}
