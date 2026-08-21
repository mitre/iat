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
import { ModalModule } from 'ngx-bootstrap/modal';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { PaperCanvasComponent } from './paper-canvas.component';
import { LetDirective } from './let.directive';
import { MinDirective } from './validators/min.directive';
import { MaxDirective } from './validators/max.directive';
import { PasswordMatcherDirective } from './validators/password-matcher.directive';

/**
 * Feature NgModule to easily pass all Utilities (within src/app/utils) to other Feature Modules
 */

@NgModule({
  imports: [
    ModalModule.forRoot(),
    TooltipModule.forRoot(),],
  declarations: [
    PaperCanvasComponent,
    LetDirective,
    MinDirective,
    MaxDirective,
    PasswordMatcherDirective,
  ],
  exports: [
    ModalModule,
    TooltipModule,
    PaperCanvasComponent,
    LetDirective,
    MinDirective,
    MaxDirective,
    PasswordMatcherDirective,
  ]
})
export class UtilsModule {
}
