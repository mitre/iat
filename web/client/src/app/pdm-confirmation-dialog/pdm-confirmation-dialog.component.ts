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
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-pdm-confirmation-dialog',
  templateUrl: './pdm-confirmation-dialog.component.html',
  styleUrl: './pdm-confirmation-dialog.component.css'
})
export class PdmConfirmationDialogComponent {
  constructor(private dialogRef: MatDialogRef<PdmConfirmationDialogComponent>) {}

  onOptionSelected(selectedChoice: string) {
    this.dialogRef.close(selectedChoice);
  }

}
