/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { TestBed, inject } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NotificationsService } from './notifications.service';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppNotification } from '../types/notification';
import { Observable } from 'rxjs';

describe('XIX. The NotificationsService class:', () => {
    const userId = '12345';
    const message = 'Hello World';
    const icon = 'myIcon';
    const action = 'modify';
    const numId = 12345;
    const appNote = new AppNotification(message, icon, action, numId);

  beforeEach(() => TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatSnackBarModule, RouterTestingModule]
    })
      .compileComponents()
  );

  it(`a. pollForNewNotifications takes a string like '${userId}' as a parameter and returns a void type.`, inject([NotificationsService], (service: NotificationsService) => {
    spyOn(service, 'pollForNewNotifications');
    const res = service.pollForNewNotifications(userId);
    expect(res).toBeUndefined();
  }));

  it(`b. checkIfNoteWasNotified takes an object instance of the AppNotification class like '${JSON.stringify(appNote)}' as a parameter and returns a void type.`, inject([NotificationsService], (service: NotificationsService) => {
    spyOn(service, 'checkIfNoteWasNotified');
    const res = service.checkIfNoteWasNotified(appNote);
    expect(res).toBeUndefined();
  }));

  it(`c. postNotification takes an object instance of the AppNotification class like '${JSON.stringify(appNote)}' as a parameter and returns an Observable AppNotification.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.postNotification(appNote);
    expect(res).toBeInstanceOf(Observable);

  }));

  it(`d. addNewNotification takes an object instance of the AppNotification class like '${JSON.stringify(appNote)}' and a boolean value as parameters and returns a void type.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.addNewNotification(appNote, true);
    expect(res).toBeUndefined();
  }));

  it(`e. getAllNotificationsForUser takes a string like '${userId}' as a parameter and returns an Observable array of AppNotifications.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.getAllNotificationsForUser(userId);
    expect(res).toBeInstanceOf(Observable);
  }));

  it(`f. getNewUserNotificationsForUser takes a string like '${userId}' as a parameter and returns an Observable array of AppNotifications.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.getNewUserNotificationsForUser(userId);
    expect(res).toBeInstanceOf(Observable);
  }));

  it(`g. markSeen takes an object instance of the AppNotification class like '${JSON.stringify(appNote)}' as a parameter and returns an Observable string.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.markSeen(appNote);
    expect(res).toBeInstanceOf(Observable);
  }));

  it(`h. markAllSeen takes zero parameters and returns a void type.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.markAllSeen();
    expect(res).toBeUndefined();
  }));

  it(`i. deleteNote takes an object instance of the AppNotification class like '${JSON.stringify(appNote)}' as a parameter and returns an Observable string.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.deleteNote(appNote);
    expect(res).toBeInstanceOf(Observable);
  }));

  it(`j. deleteAllNotes takes zero parameters and returns a void type.`, inject([NotificationsService], (service: NotificationsService) => {
    const res = service.deleteAllNotes();
    expect(res).toBeUndefined();
  }));
});
