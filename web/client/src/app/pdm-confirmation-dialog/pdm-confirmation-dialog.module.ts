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

// Import any shared components, directives, or pipes here
import { PdmConfirmationDialogComponent } from './pdm-confirmation-dialog.component';

@NgModule({
  declarations: [
    PdmConfirmationDialogComponent, // Add shared components here
  ],
  imports: [
    CommonModule // Always import CommonModule in feature modules
  ],
  exports: [
    PdmConfirmationDialogComponent, // Export so other modules can use it
    CommonModule    // Optionally export CommonModule for convenience
  ]
})
export class PdmConfirmationDialogModule { }