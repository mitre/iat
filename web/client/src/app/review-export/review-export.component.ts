/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableDataSource } from '@angular/material/table';
import { ReportData } from '../types/report-data';
import { HistoryService } from '../history/history.service';
import { ProfileService } from '../profile/profile.service';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { ReviewExportService } from './review-export.service';

@Component({
  selector: 'app-review-export',
  templateUrl: './review-export.component.html',
  styleUrls: ['./review-export.component.css'],
  providers: [DatePipe, MatSort]
})

export class ReviewExportComponent implements OnInit {
  reviewRequests: Array<ReportData> = [];
  idList: Array<string> = [];
  reviewRequestData: MatTableDataSource<ReportData>;
  originalReviewRequestData: MatTableDataSource<ReportData>;
  reviewStart = 0;
  allSelected = false;
  reviewLength = 0;
  stepSize = 20;

  reviewSortColumn = 'caseInformation.comparisonDate';
  reviewRequestColumns = ['selected', 'comparisonDate', 'transactionControlNumber', 'responseType',
    'candidateCount', 'requestedBy', 'reviewedBy', 'isFinished'];
  caretScaling = 'caret-descend';

  // @ViewChild is a reference that injects the
  // child component into a given parent component.
  @ViewChild('reviewSort') reviewRequestSort: MatSort;
  @ViewChild('reviewPaginator', {static: true}) reviewPaginator: MatPaginator;

  startDateVal: Date;
  endDateVal: Date;

  constructor(
    private historyService: HistoryService,
    public profileService: ProfileService,
    private reviewService: ReviewExportService) {
  }

  ngOnInit() {
    this.historyService.getReviewCount().subscribe((len) => {
      this.reviewLength = len;
    });

    const lastMonth = new Date();
    lastMonth.setDate(lastMonth.getDate() - 30);
    this.startDateVal = lastMonth;
    this.endDateVal = new Date();

    const dir = this.caretScaling == 'caret-descend' ? 'desc' : 'asc';

    this.historyService.getAllReviewList(this.reviewStart, this.stepSize, this.reviewSortColumn, dir)
      .subscribe((requests) => {

        this.reviewRequests = requests;
        this.reviewRequestData = new MatTableDataSource<ReportData>(requests);
        this.reviewRequestData.sortingDataAccessor = (item, property) => {
          let value;

          switch (property) {
            case 'comparisonDate':
              value = item.caseInformation.comparisonDate;
              break;
            case 'transactionControlNumber':
              value = item.probe.transactionControlNumber;
              break;
            case 'candidateCount':
              value = item.candidates.length;
              break;
            case 'requestedBy':
              value = item.caseInformation.requestedBy.name;
              break;
            case 'reviewedBy':
              value = item.workedBy.profile.name;
              break;
            default:
              value = item[property];
          }

          return value;
        };
        this.reviewRequestData.paginator = this.reviewPaginator;
        this.reviewRequestData.sort = this.reviewRequestSort;
      });
  }

  applyDateFilter() {
    this.idList = [];
    this.toggleSelectAllText(false);

    this.reviewRequestData = new MatTableDataSource<ReportData>(this.reviewRequests);
    this.reviewRequestData.data = this.reviewRequestData.data.filter(e =>
      // Add a day in milliseconds to the end date to make the filter include the entire selected day
       e.caseInformation.comparisonDate > this.startDateVal.valueOf() && e.caseInformation.comparisonDate < (this.endDateVal.valueOf() + 8.64e+7)
    );
  }

  getDateStringFromEpoch(epochDate: number): string {
    const d = new Date(0);
    d.setUTCSeconds(epochDate);
    return d.toLocaleString();
  }

  addToList(i: string, $event) {
    if (!$event.checked) {
      this.toggleSelectAllText(false);
      this.idList.splice(this.idList.indexOf(i), 1);
    } else {
      this.idList.push(i);
    }
  }

  toggleSelectAllText(selectAll: boolean) {
    if (selectAll) {
      this.allSelected = true;
    } else {
      this.allSelected = false;
    }
  }

  toggleAll($event) {
    this.idList = [];

    if ($event.checked) {
      for (const temp of this.reviewRequestData.data) {
        this.idList.push(temp.id);
      }
    }

    this.toggleSelectAllText($event.checked);
  }

  createCSV() {
    if (this.idList.length > 0) {
      this.reviewService.pullReport(this.idList).subscribe(data => {
          const blob = new Blob([data], {type: 'text/csv'});
          //create the link element
          const a = document.createElement('a');
          document.body.appendChild(a);

          //set the fields
          const objectUrl = URL.createObjectURL(data);
          a.href = objectUrl;
          const date = new Date();
          a.download = this.pad(date.getMonth() + 1, 2) + '.' + this.pad(date.getDate(), 2) + '.' + date.getFullYear() + '-'
            + this.pad(date.getHours(), 2) + '.' + this.pad(date.getMinutes(), 2) + '.' + this.pad(date.getSeconds(), 2) + '.csv';
          a.click();
          window.URL.revokeObjectURL(objectUrl);
        },
        error => {
          console.log(error);
        });
    }
  }

  pad(num, size): string {
    let s = num + '';
    while (s.length < size) {
      s = '0' + s;
    }
    return s;
  }
}
