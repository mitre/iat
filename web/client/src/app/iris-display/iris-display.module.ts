/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UtilsModule } from '../utils/utils.module';

import { IrisDisplayComponent } from './iris-display.component';
import { IrisToolComponent, ToolHostDirective } from './tools/iris-tool.component';
import { IrisDrawingTool } from './tools/tool.base';
import { IrisToolRegistry } from './tools/tool.registry';
import { AnnotationHistoryComponent } from '../annotation-history-dialog/annotation-history.component';
import { AnnotationComponent } from '../annotation-dialog/annotation.component';

// Angular Material
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';

import { 
  AdaptiveContrastTool,
  BrightnessTool, ChannelTool, CircleTool, ColorLevelTool,
  ContrastTool,
  CropTool,
  MirrorTool,
  PencilTool,
  PointTool, PolygonTool, EyelidTool,
  RotateTool, ScaleTool, SharpenTool, UnrollTool, ZoomTool
 } from './tools/tool-components';
import { FindIrisTool } from './tools/tool-components/findiris.tool';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { PupilDilationToolComponent } from '../pupil-dilation-tool/pupil-dilation-tool.component';
import { DualPDMComponent } from '../dual-pdm/dual-pdm.components';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PdmConfirmationDialogModule } from '../pdm-confirmation-dialog/pdm-confirmation-dialog.module';


/**
 * Feature NgModule for IrisDisplayComponent (and supporting services, components, and pipes)
 */

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        UtilsModule,
        MatButtonToggleModule,
        MatSlideToggleModule,
        MatSidenavModule,
        MatIconModule,
        MatToolbarModule,
        MatTabsModule,
        MatButtonModule,
        MatDividerModule,
        MatCheckboxModule,
        MatTableModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        MatCardModule,
        MatTooltipModule,
        PdmConfirmationDialogModule
    ],
  declarations: [IrisDisplayComponent, IrisToolComponent, ToolHostDirective, IrisDrawingTool,
    AnnotationHistoryComponent, AnnotationComponent,
    BrightnessTool, ContrastTool, PointTool, CropTool, MirrorTool, PencilTool, RotateTool, ScaleTool, ColorLevelTool, ChannelTool,
    ZoomTool, PolygonTool, EyelidTool, CircleTool, SharpenTool, UnrollTool, AdaptiveContrastTool, FindIrisTool, PupilDilationToolComponent, DualPDMComponent
  ],
  exports: [IrisDisplayComponent],
  providers: [IrisToolRegistry]
})
export class IrisDisplayModule {
}
