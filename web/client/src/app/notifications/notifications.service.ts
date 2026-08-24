/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { AppNotification } from '../types/notification';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, throwError as observableThrowError } from 'rxjs';
import { startWith, switchMap, catchError } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { interval } from 'rxjs/internal/observable/interval';

// https://stackoverflow.com/questions/36395252/how-to-get-data-from-observable-in-angular2
@Injectable({
  providedIn: 'root'
})

export class NotificationsService {
  notificationsArray: AppNotification[];
  notifiedIdSet = new Set();
  private addNoteUrl = '/api/notification/add';
  private getAllNoteUrl = '/api/notification/all/';
  private getNewNoteUrl = '/api/notification/new/';
  private markNoteReadUrl = '/api/notification/markRead/';
  private deleteNoteUrl = '/api/notification/delete/';
  private notificationPollingInterval = 15000; // (5000 = 5s)

  constructor(
    private matSnackBar: MatSnackBar,
    private http: HttpClient
  ) {
    this.initializeUserNotifications();
  }

  // Only to be used for initial pull of user notifications upon service load, intentionally does /not/ trigger matSnackBar
  private initializeUserNotifications(): void {
    this.notificationsArray = [];
    this.notifiedIdSet.clear();
  }

  pollForNewNotifications(userId: string): void {
    interval(this.notificationPollingInterval)
      .pipe(
        startWith(0),
        switchMap(() => this.getNewUserNotificationsForUser(userId))
      ).subscribe(newNotes => {
      newNotes.forEach(note => {
        this.checkIfNoteWasNotified(note);
      });
    });
  }

  checkIfNoteWasNotified(noteToCheck: AppNotification) {
    if (this.notifiedIdSet.has(noteToCheck.id)) {
      //do nothing
    } else {
      this.addNewNotification(noteToCheck, false);
    }
  }

  postNotification(notifcationToAdd: AppNotification): Observable<AppNotification> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.http.post<AppNotification>(this.addNoteUrl, notifcationToAdd,
      {headers})
      .pipe(catchError(this.handleError));
  }

  addNewNotification(noteToAdd: AppNotification, isInitilization: boolean): void {
    // Add notification to the current array
    this.notificationsArray.unshift(noteToAdd);
    this.notifiedIdSet.add(noteToAdd.id);
    // Trigger popup
    if (!isInitilization) {
      const snackbarRef = this.matSnackBar.open(noteToAdd.message, 'Go');
      // Bind popup button to action
      snackbarRef.onAction().subscribe(() => {
        noteToAdd.isNew = false;
        window.open(noteToAdd.action);
      });
    }
  }

  getAllNotificationsForUser(userId: string): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.getAllNoteUrl + userId)
      .pipe(catchError(this.handleError));
  }

  getNewUserNotificationsForUser(userId: string): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.getNewNoteUrl + userId)
      .pipe(catchError(this.handleError));
  }

  // Mark a single notification as 'seen', updates server accordingly
  // hit server endpoint to ensure the isNew flag gets updated centrally
  markSeen(note: AppNotification): Observable<string> {
    note.isNew = false;
    return this.http.get(this.markNoteReadUrl + note.id, {responseType: 'text'})
      .pipe(catchError(this.handleError));
  }


  // Marks all notifications for this user as 'seen', updates server accordingly
  markAllSeen(): void {
    this.notificationsArray.forEach(note => note.isNew = false);
    // hit server endpoint to update isNew flag for all of this user's notifications
    this.notificationsArray.forEach(
      note => this.markSeen(note).subscribe(
        noteSeen => {
        })
    );
  }

  deleteNote(noteToDelete: AppNotification): Observable<string> {
    noteToDelete.isNew = false;
    this.notificationsArray = this.notificationsArray.filter(note => note.id !== noteToDelete.id);
    return this.http.get(this.deleteNoteUrl + noteToDelete.id, {responseType: 'text'}).pipe(catchError(this.handleError));
  }

  deleteAllNotes(): void {
    this.notificationsArray.forEach(
      note => this.deleteNote(note).subscribe(
        noteToDelete => {
        })
    );
  }

  private handleError(error: any) {
    const errMsg = (error.message) ? error.message : error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    console.log(error);
    return observableThrowError(errMsg);
  }

}
