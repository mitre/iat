/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { PersonService } from '../person/person.service';
import { ProfileService } from '../profile/profile.service';
import { Profile } from '../profile/profile';
import { Person } from '../types/person';
import { CaseInformation } from '../types/case-information';

@Component({
  selector: 'app-report-details',
  templateUrl: 'report-details.component.html',
  styles: [`
 `]
})
export class ReportDetailsComponent implements OnInit{
  persons: Person[] = [];
  currentProfile: Profile;
  selected: any;
  noSelectionRequestPerson = new Person();
  isNameRequired = false;
  isAutoSelected = false;
  initDate = new Date();
  comparisonDate = new Date();
  receivedDate = new Date();

  @Input() caseInformation: CaseInformation;
  @ViewChild(NgForm, {static: true}) reportForm;
  @ViewChild(NgForm, {static: true}) requestForm;


  constructor(
    public personService: PersonService,
    private profileService: ProfileService
  ) {
    this.personService.getPersons().subscribe(persons => {
      this.persons = persons;
    });

    this.noSelectionRequestPerson.name = '';
    this.noSelectionRequestPerson.email = '';
    this.noSelectionRequestPerson.phone = '';
    this.noSelectionRequestPerson.title = '';
    this.noSelectionRequestPerson.department = '';
    this.noSelectionRequestPerson.address = '';
    this.noSelectionRequestPerson.id = '';

    this.currentProfile = this.profileService.getCurrentProfile();
  }

  ngOnInit() {
    this.selected = 'No Selection';
    this.caseInformation.requestedBy = this.noSelectionRequestPerson;

    const conductedByPerson = new Person();
    conductedByPerson.name = this.currentProfile.fullName;
    conductedByPerson.title = this.currentProfile.title;
    conductedByPerson.department = this.currentProfile.department;
    conductedByPerson.address = this.currentProfile.address;
    conductedByPerson.email = this.currentProfile.email;
    conductedByPerson.phone = this.currentProfile.phoneNumber;
    conductedByPerson.id = this.currentProfile.id;

    this.caseInformation.conductedBy = conductedByPerson;
  }

  setComparisonFormField($event) {
    this.caseInformation.comparisonDate = $event.getTime();
  }

  setReceivedFormField($event) {
    this.caseInformation.receivedDate = $event.getTime();
  }

  conductedBySelected(selectedPerson: Person) {
    this.caseInformation.conductedBy = selectedPerson;
  }

  requestedBySelected(selectedPerson: Person) {
    if (selectedPerson != this.noSelectionRequestPerson) {
      this.isAutoSelected = true;
      this.isNameRequired = false;
    } else {
      this.isAutoSelected = false;
    }
    this.caseInformation.requestedBy = selectedPerson;
  }

  checkRequesterFields() {
    if (this.caseInformation.requestedBy == this.noSelectionRequestPerson) {
      if (this.caseInformation.requestedBy.name != '') {
        this.caseInformation.requestedBy.id = '0';
        this.isNameRequired = false;
      } else {
        if (this.caseInformation.requestedBy.email != '' || this.caseInformation.requestedBy.phone != '' || this.caseInformation.requestedBy.title != '' || this.caseInformation.requestedBy.department != '' || this.caseInformation.requestedBy.address != '') {
          this.isNameRequired = true;
        } else {
          this.isNameRequired = false;
        }
      }
    }
  }

}
