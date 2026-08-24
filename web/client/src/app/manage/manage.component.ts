/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { 
  Component,
  OnInit,
  ElementRef,
  TemplateRef
 } from '@angular/core';
import { PersonService } from '../person/person.service';
import { Person } from '../types/person';
import { ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styleUrls: ['./manage.component.css']
})
export class ManageComponent implements OnInit {

  persons: Person[] = [];
  personsData: MatTableDataSource<Person>;
  personsColumns = ['name', 'title', 'department', 'address', 'email', 'phone', 'actions'];
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  currentPerson: Person = this.personService.createEmptyPerson();
  createPerson: Person = this.personService.createEmptyPerson();
  errorMessage: string;
  editing = true;

  p = 1;

  @ViewChild('addPersonModal', {static: true})
  addPersonModal: ElementRef;

  @ViewChild('removeConfirmModal', {static: true})
  removeConfirmModal: ElementRef;

  @ViewChild('createForm', {static: true})
  createForm: NgForm;

  @ViewChild('currentPersonSelect', {static: true})
  currentProfileSelect: ElementRef;

  constructor(
    private personService: PersonService,
    private matDialogService: MatDialog,
    private matSnackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.getAllPersons();
  }

  applyFilter($event: KeyboardEvent): void {
    var value = ($event.target as HTMLTextAreaElement).value;
    this.personsData.filter = value.trim().toLowerCase();
  }

  getAllPersons() {
    //pull down the profiles and reset the current profile
    this.personService.getPersons()
      .subscribe(prfs => {
        this.persons = prfs;
        for (const person of this.persons) {
          if (person.name == this.currentPerson.name) {
            this.currentPerson = person;
          }
        }
        this.personsData = new MatTableDataSource<Person>(prfs);
      });
  }

  resetModalInputs() {
    this.createPerson = this.personService.createEmptyPerson();
    this.createForm.resetForm();
  }

  createNewPerson() {
    const createdPerson = this.createPerson;

    this.personService.addPerson(this.createPerson)
      .subscribe(
        response => {

          //set the current profile to the one just created
          this.currentPerson = createdPerson;

          //reset the create modal
          this.resetModalInputs();

          this.getAllPersons();
        },
        response => {
          this.errorMessage = JSON.parse(response.error).message;
          console.error(this.errorMessage);
        });
  }

  closePerson() {
    console.log('Close Person');
    this.getAllPersons();
    this.matDialogService.closeAll();
  }

  savePerson() {
    this.personService.addPerson(this.currentPerson)
      .subscribe(response => {
        this.getAllPersons();
        this.matDialogService.closeAll();
        this.matSnackBar.open('Requester saved', 'Dismiss');
      });
  }

  removePerson(person: Person) {
    this.personService.removePerson(person).subscribe(response => {
      this.currentPerson = this.personService.createEmptyPerson();
      this.getAllPersons();
      this.matDialogService.closeAll();
      this.matSnackBar.open('Requester removed', 'Dismiss');
    });
  }

  removePersonClicked(person: Person, modal: TemplateRef<any>) {
    this.currentPerson = person;
    this.matDialogService.open(modal);
  }

  personEditClicked(person: Person, modal: TemplateRef<any>) {
    this.editing = true;
    this.currentPerson = JSON.parse(JSON.stringify(person));
    this.matDialogService.open(modal);
  }

  newPersonClicked(modal: TemplateRef<any>) {
    this.editing = false;
    this.currentPerson = this.personService.createEmptyPerson();
    this.matDialogService.open(modal);
  }
}
