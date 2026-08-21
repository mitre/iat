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
import { HistoryService } from './history.service';
import { ProfileService } from '../profile/profile.service';
import { CreateIrisSearchRequest } from '../types/create-iris-search-request';
import { Router } from '@angular/router';
import { ReportData } from '../types/report-data';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { NotificationsService } from '../notifications/notifications.service';
import { AppNotification } from '../types/notification';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})

export class HistoryComponent implements OnInit {

  searchRequests: Array<CreateIrisSearchRequest> = [];
  searchRequestData: MatTableDataSource<CreateIrisSearchRequest>;
  searchRequestColumns = ['creationDateEpoch', 'cinPrefix', 'cinIdentifier', 'caseExtension',
    'destinationAgencyIdentifier', 'originatingAgencyIdentifier', 'isFinished', 'actions'];
  @ViewChild('searchSort', {static: true}) searchRequestSort: MatSort;
  @ViewChild('searchPaginator', {static: true}) searchPaginator: MatPaginator;

  reviewRequests: Array<ReportData> = [];
  reviewRequestData: MatTableDataSource<ReportData>;
  reviewRequestColumns = ['comparisonDate', 'transactionControlNumber', 'responseType',
    'candidateCount', 'requestedBy', 'isFinished', 'actions'];
  @ViewChild('reviewSort') reviewRequestSort: MatSort;
  @ViewChild('reviewPaginator', {static: true}) reviewPaginator: MatPaginator;


  userNotifications: Array<AppNotification> = [];
  userNotificationData: MatTableDataSource<AppNotification>;
  userNotificationColumns = ['receivedDate', 'message', 'status', 'action'];
  @ViewChild('notificationPaginator', {static: true}) notificationPaginator: MatPaginator;

  pageSizeOptions = [10, 25, 50];

  stepSize = 20;
  searchStart = 0;
  searchLength = 0;
  reviewStart = 0;
  reviewLength = 0;

  searchSortColumn = 'creationDateEpoch';
  reviewSortColumn = 'caseInformation.comparisonDate';
  caretScaling = 'caret-descend';

  constructor(
    private notificationService: NotificationsService,
    private historyService: HistoryService,
    public profileService: ProfileService,
    private router: Router,
    private datePipe: DatePipe
  ) {
    const settings = this.profileService.getCurrentProfile().userSetting;
    this.stepSize = settings.numHistory;
  }

  ngOnInit() {
    this.updateSearches();

    this.updateReviews();

    this.getNotificationProfile();

    this.historyService.getSearchCount().subscribe((len) => {
      this.searchLength = len;
    });

    this.historyService.getReviewCount().subscribe((len) => {
      this.reviewLength = len;
    });


  }

  continueIrisSearch(searchRequestIndex, request) {
    this.historyService.savedIrisSearchRequest = this.searchRequests[searchRequestIndex];
    this.router.navigate(['/create']);
  }

  continueReview(reviewIndex, request) {
    this.historyService.savedReview = this.reviewRequests[reviewIndex];
    this.router.navigate(['/review', request.id]);
  }

  getDateString(epochDate: number) {
    const d = new Date(0);
    d.setUTCSeconds(epochDate);
    return d.toLocaleString();
  }

  getDateFromString(dateString: string) {
    const date = new Date(dateString);

    return this.datePipe.transform(date, 'short');
  }

  private updateSearches() {
    const dir = this.caretScaling == 'caret-descend' ? 'desc' : 'asc';
    this.historyService.getIrisSearchCreateList(this.searchStart, this.stepSize, this.searchSortColumn, dir)
      .subscribe((requests) => {
        this.searchRequests = requests;
        this.searchRequestData = new MatTableDataSource<CreateIrisSearchRequest>(requests);
        this.searchRequestData.sortingDataAccessor = (item, property) => {
          let value;
          switch (property) {
            case 'isFinished':
              value = item.isFinished;
              break;
            case 'creationDateEpoch':
              value = item.creationDateEpoch;
              break;
            default:
              // Every other property is a actually a sub-property of the 'message' object
              value = item.message[property];
          }

          return value;
        };
        this.searchRequestData.paginator = this.searchPaginator;
        this.searchRequestData.sort = this.searchRequestSort;
      });
  }

  private updateReviews() {
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
            default:
              value = item[property];
          }

          return value;
        };
        this.reviewRequestData.paginator = this.reviewPaginator;
        this.reviewRequestData.sort = this.reviewRequestSort;
      });
  }

  //UserNotifications
  private getNotificationProfile() {
    let currentProfileId = this.profileService.getCurrentProfile().id;
    if (currentProfileId == null || currentProfileId == '') {
      this.profileService.currentProfile$.subscribe((profile) => {
        this.getNotifications(profile.id);
      });
    } else {
      this.getNotifications(this.profileService.getCurrentProfile().id);
    }
  }

  private getNotifications(userId: string) {
    this.notificationService.getAllNotificationsForUser(userId)
      .subscribe((requests) => {

        this.userNotifications = requests;

        this.userNotifications = this.userNotifications.sort(this.sortNotifications);
        this.userNotificationData = new MatTableDataSource<AppNotification>(this.userNotifications);
        this.userNotificationData.paginator = this.notificationPaginator;
      });
  }

  private sortNotifications(a: AppNotification, b: AppNotification) {
    return a.dateCreated > b.dateCreated ? -1 : 1;
  }

  // this is the parent behavior.
  markNotificationRead(not) {
    this.notificationService.markSeen(not)
      .subscribe((requests) => {
        not.status = status;
        this.historyService.notificationsModified('status');
      });
  }

  deleteNotification(not) {
    this.notificationService.deleteNote(not)
      .subscribe((not2) => {
        const index: number = this.userNotifications.indexOf(not);
        if (index !== -1) {
          this.userNotifications.splice(index, 1);
          this.historyService.notificationsModified('delete');
        }
      });
  }

  checkNotificationAction(note) {

    if (note.action.includes('download')) {
      window.open(note.action);
    } else {
      this.router.navigateByUrl(note.action).then(res => {
      });
    }
  }
}
