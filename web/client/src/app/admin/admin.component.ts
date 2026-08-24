/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { ChangeDetectorRef, Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { User } from '../user/user';
import { NgForm } from '@angular/forms';
import { AdminService } from './admin.service';
import { UserRole } from './user-role';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { ProfileService } from '../profile/profile.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {

  users: User[] = [];

  // Objects for Material Tables
  usersData!: MatTableDataSource<User>;
  usersColumns = ['userName', 'actions'];
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  userRoles: UserRole[] = [];
  userToCreateOrEdit: User = this.adminService.createEmptyUser();
  editing = true;
  newUser = false;
  passwordVisible = false;
  loggedInUsername: string;
  originalRoles: string[] = [];

  hasRoleChanged = false;

  confirmNewPassword = '';
  newPassword = '';
  currentPassword = '';

  p = 1;

  @ViewChild('addProfileModal', {static: true})
  addProfileModal: ElementRef;

  @ViewChild('createForm', {static: true})
  createForm: NgForm;

  @ViewChild('removeConfirmModal', {static: true})
  removeConfirmModal: ElementRef;

  constructor(private adminService: AdminService, public modalService: MatDialog, private matSnackBar: MatSnackBar, public profileService: ProfileService) {
    this.profileService.username$
      .subscribe(name => {
        this.loggedInUsername = name;
      });
  }

  ngOnInit() {

    this.loadProfiles();

    this.adminService.getAllRoles().subscribe(roles => {
      this.userRoles = roles;
    });
  }

  isLoggedInUser(user: User): boolean {
    return user.username === this.loggedInUsername;
  }

  applyFilter($event: KeyboardEvent) {
    var value = ($event.target as HTMLTextAreaElement).value;
    this.usersData.filter = value.trim().toLowerCase();
  }

  loadProfiles() {
    this.adminService.getAllUsers().subscribe(users => {
      this.users = users;
      this.usersData = new MatTableDataSource<User>(users);
      this.usersData.paginator = this.paginator;
    });
  }

  closeProfile(form: NgForm) {
    this.loadProfiles();
    form.resetForm();
    this.originalRoles = [];
    this.modalService.closeAll();
  }

  updateRoles(isPasswordUpdated: boolean) {
    this.userToCreateOrEdit.password = this.newPassword;
    this.adminService.addUpdateUser(this.userToCreateOrEdit, false).subscribe({
      next: () => {
        //reset the addProfile modal
        this.userToCreateOrEdit = this.adminService.createEmptyUser();
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmNewPassword = '';
        this.modalService.closeAll();
        this.loadProfiles();
        if (isPasswordUpdated) {
          this.matSnackBar.open('User roles and password updated', 'Dismiss');
        } else {
          this.matSnackBar.open('User roles updated', 'Dismiss');
        }
      },
      error: (err) => {
        if (isPasswordUpdated) {
          this.matSnackBar.open('User password updated but roles were NOT updated', 'Dismiss');
        } else {
          this.matSnackBar.open('User roles were not updated!', 'Dismiss');
        }
      }
    });
  }

  updateUser(passwordFormGroup: NgForm) {
    // Check to see if the currentPassword input is correct.
    this.adminService.checkPassword(this.userToCreateOrEdit.username, this.currentPassword).subscribe({
      next: (resp) => {
        // If currentPassword input is correct, 
        // checks to see if the password is getting changed
        // else it just updates the roles in the next handler
        if (this.newPassword != null && this.newPassword.length > 0) {
          this.adminService.updatePassword(this.userToCreateOrEdit.username, this.currentPassword, this.newPassword).subscribe({
            next: (resp) => {
              // Checks to see if the roles are being changed. If it is, it will call updateRoles()
              // If not, it means they only wanted to change the password and will show the popup
              if (this.hasRoleChanged) {
                this.updateRoles(true);
              } else {
                this.matSnackBar.open('User password updated');
                this.modalService.closeAll();
              }
            },
            // If it couldn't update the password, it doesn't try to update the roles.
            error: (err) => {
              this.matSnackBar.open('User password and roles were NOT updated!');
            }
          });
        } else {
          this.updateRoles(false);
        }
      },
      // If currentPassword input is wrong, it does NOT update the user and notifies the user that the currentPassword input was wrong
      error: (err) => {
        passwordFormGroup.form.get('currentPasswordInput').setErrors({badPassword: true});
        this.matSnackBar.open('Password incorrect; user was not updated!');
      }
    });
  }

  saveProfile(passwordFormGroup: NgForm) {
    // Check if it's updating a user or creating a user
    // If it's a previously created user, it goes into the IF statement and updates the user
    // If it is a new user, it goes into the ELSE statement and creates the user
    if (this.editing == true) {
      this.updateUser(passwordFormGroup)
    } else {
      // Set the userToCreateOrEdit's password to the typed in password
      this.userToCreateOrEdit.password = this.newPassword;
      this.adminService.addUpdateUser(this.userToCreateOrEdit, true)
        .subscribe({
          next: () => {
            // reset the create modal
            this.userToCreateOrEdit = this.adminService.createEmptyUser();
            this.currentPassword = '';
            this.newPassword = '';
            this.confirmNewPassword = '';
            this.modalService.closeAll();
            this.loadProfiles();
            this.matSnackBar.open('User created', 'Dismiss');
          },
          error: (err) => {
            this.matSnackBar.open('User was NOT created!');
          }
      });
    }
  }

  removeProfile(user: User) {
    this.adminService.removeUser(user).subscribe(response => {
      this.userToCreateOrEdit = this.adminService.createEmptyUser();
      this.loadProfiles();
      this.modalService.closeAll();
      this.matSnackBar.open('User removed', 'Dismiss');
    });
  }

  profileEditClicked(user: User, modal: TemplateRef<any>) {
    this.passwordVisible = false;
    this.newUser = false;
    this.editing = true;
    this.userToCreateOrEdit = user;
    this.userToCreateOrEdit.roles.forEach(role => {
      this.originalRoles.push(role);
    })
    this.hasRoleChanged = false;
    this.modalService.open(modal);
  }

  newProfileClicked(modal: TemplateRef<any>) {
    this.passwordVisible = false;
    this.newUser = true;
    this.editing = false;
    this.hasRoleChanged = false;
    this.userToCreateOrEdit = this.adminService.createEmptyUser();
    this.modalService.open(modal);
  }

  removeProfileClicked(user: User, modal: TemplateRef<any>) {
    this.userToCreateOrEdit = user;
    this.modalService.open(modal);
  }

  setRole(userRole: UserRole, event: any) {
    let newUserRoles: string[] = [];
    if (event.srcElement.checked) {
      newUserRoles.push(userRole.name)
    } else {
      const currentIndex: number = this.userToCreateOrEdit.roles.indexOf(userRole.name);
      const newIndex: number = newUserRoles.indexOf(userRole.name)
      if (currentIndex !== -1) {
        this.userToCreateOrEdit.roles.splice(currentIndex, 1);
      }
      if (newIndex !== -1) {
        newUserRoles.splice(newIndex,1)
      }
    }
    this.userToCreateOrEdit.roles = this.userToCreateOrEdit.roles.concat(newUserRoles)
    this.hasRoleChanged = !this.checkRoles();
  }

  checkRoles() {
    let jsonCurrent = JSON.stringify(this.userToCreateOrEdit.roles.sort());
    let jsonOriginal = JSON.stringify(this.originalRoles.sort());
    return jsonCurrent === jsonOriginal;
  }

  showHidePassword() {
    this.passwordVisible = !this.passwordVisible; 
  }

  userHasRole(role: UserRole) {
    return this.userToCreateOrEdit.roles.indexOf(role.name) >= 0;
  }

}
