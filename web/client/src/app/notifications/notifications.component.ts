/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, OnInit, AfterViewInit } from '@angular/core';
import { AppNotification } from '../types/notification';
import { NotificationsService } from './notifications.service';
import { Router } from '@angular/router';
import { ProfileService } from '../profile/profile.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
  selected: any = undefined;
  notifications: AppNotification[];
  messageIndexForTesting = 0;
  currentUserId = 'null';

  constructor(public noteService: NotificationsService,
              private router: Router,
              private profileService: ProfileService,
  ) {
  }

  ngOnInit() {
    this.getCurrentUser();
    this.getNotificationServiceArray();
  }

  getCurrentUser(): void {
    this.profileService.currentProfile$.subscribe((profile) => {
      this.currentUserId = profile.id;
      this.getAllNotificationsForUser();
    });
  }

  getNotificationServiceArray(): void {
    this.notifications = this.noteService.notificationsArray;
  }

  getAllNotificationsForUser(): void {
    this.noteService.getAllNotificationsForUser(this.currentUserId).subscribe(
      notificationResponse => {
        notificationResponse.forEach(note => {
          if (!this.notifications.find(currNote => currNote.id == note.id)){
            this.noteService.addNewNotification(note, true);
          }
        });
      });
    this.noteService.pollForNewNotifications(this.currentUserId);
  }

  get newNotificationCount(): number {
    this.getNotificationServiceArray();
    return this.notifications.filter(note => note.isNew).length;
  }

  clickNotification(note: AppNotification): void {
    note.isNew = false;
    this.resetButtonToggle();
    if (note.action.includes('download')) {
      window.open(note.action);
    } else {
      this.router.navigateByUrl(note.action).then(res => {
      });
    }

    this.noteService.markSeen(note)
      .subscribe(
        noteSeen => {
        });
  }

  clearNotifications(): void {
    this.noteService.markAllSeen();
  }

  deleteNotification(noteToDelete: AppNotification): void {
    this.resetButtonToggle();
    this.noteService.deleteNote(noteToDelete).subscribe(
      noteDeleted => {
        this.getNotificationServiceArray();
      });
  }

  resetButtonToggle(): void {
    this.selected = null;
  }

  deleteAllNotifications(): void {
    this.noteService.deleteAllNotes();
  }
  
  addNotification() {
    this.postNotification({
      message: this.messageIndexForTesting.toString(),
      action: '/',
      dateCreated: new Date(),
      icon: 'notifications',
      isNew: true,
      id: '999',
      userId: 1
    });
    this.messageIndexForTesting += 1;
  }

  postNotification(noteToAdd: AppNotification) {
    this.noteService.postNotification(noteToAdd).subscribe(data => {
      noteToAdd.id = data.toString();
    });
  }
}
