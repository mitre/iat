/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, OnInit, OnChanges } from '@angular/core';
import { ProfileService } from '../profile/profile.service';
import { Profile } from '../profile/profile';
import { UntypedFormGroup } from '@angular/forms';
import { ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import * as cloneDeep from 'lodash/cloneDeep';
import { AdminService } from '../admin/admin.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  templateUrl: 'user.component.html',
  styles: [`
    #profile-tab .form-control, #password-tab .form-control {
      width: 200px;
    }

    .mat-mdc-form-field {
      padding-bottom: 6px;
    }

    .container {
      max-width: 760px;
    }

  `]
})
export class UserComponent implements OnInit, OnChanges {
  initialProfile: Profile;
  currentProfile: Profile = this.profileService.createEmptyProfile();
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  passwordVisible = false;

  canEdit = false;
  numHistory = [5, 10, 15, 20];
  privs: any = [];
  formGroup: UntypedFormGroup;

  @ViewChild('editForm', {static: true})
  private editForm: NgForm;

  //passwordForm
  @ViewChild('passwordForm', {static: true})
  private passwordForm: NgForm;

  constructor(private profileService: ProfileService,
              private adminService: AdminService,
              private matSnackbar: MatSnackBar) {
    this.profileService.currentProfile$
      .subscribe((profile) => {
        this.currentProfile = profile;
      });
    this.profileService.getUserPrivileges().subscribe(privs => {
      this.privs = Object.keys(privs);
    });
  }

  ngOnInit() {
    this.profileService.getProfile().subscribe(prfs => {
      // Do nothing. simply a session alive check
    });
    this.currentProfile = this.profileService.getCurrentProfile();
    this.initialProfile = cloneDeep(this.currentProfile);
  }

  ngOnChanges(changes) {
  }

  saveProfile() {
    if (this.canEdit) {
      this.profileService.updateProfile(this.currentProfile);

    } else {
      if (this.initialProfile.id == null) {
        this.initialProfile = cloneDeep(this.currentProfile);
      }
    }

    this.canEdit = !this.canEdit;
  }

  isProfileValid() {
    let temp = true;

    Object.keys(this.editForm.controls).forEach((key: string) => {
      const abstractControl = this.editForm.controls[key];

      if (abstractControl.invalid) {
        temp = false;
      }
    });

    return temp;
  }

  resetProfile() {
    this.currentProfile = cloneDeep(this.initialProfile);
  }

  updatePassword(passwordFormGroup: NgForm) {
    // Check to see if the currentPassword input is correct.
    this.adminService.checkPassword(this.currentProfile.name, this.currentPassword).subscribe({
      next: (resp) => {
        // If currentPassword input is correct, try to update the user in the next handler
        // If it's wrong, it does NOT update the user and notifies the user that the currentPassword input was wrong
        this.adminService.updatePassword(this.currentProfile.name, this.currentPassword, this.newPassword).subscribe({
          next: (resp) => {
            //reset the create modal
            this.currentPassword = '';
            this.newPassword = '';
            this.confirmPassword = '';
            this.matSnackbar.open('User password updated', 'Dismiss');
          },
          error: (err) => {
            this.matSnackbar.open('User password was not Updated!', 'Dismiss');
          }
        });
      },
        error: (err) => {
          passwordFormGroup.form.get('currentPasswordInput').setErrors({badPassword: true});
          this.matSnackbar.open('Password incorrect; user was not updated!');
        }
    });
  }

  showHidePassword() {
    this.passwordVisible = !this.passwordVisible;
  }
}
