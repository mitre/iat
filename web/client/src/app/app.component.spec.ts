/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { Profile, UserSetting } from './profile/profile';
import { ProfileService } from './profile/profile.service';
import { ServiceStatusService } from './service-status/service-status.service';
import { NotificationsService } from './notifications/notifications.service';
import { LogonInfo } from './types/logonInfo';
import { MatDialog } from '@angular/material/dialog';
import { BehaviorSubject, of } from 'rxjs';

describe('VI. The AppComponent Class: ', () => {
    let fixture: ComponentFixture<AppComponent>;
    let app: AppComponent;
    let profileService: jasmine.SpyObj<ProfileService>;
    let serviceStatusService: jasmine.SpyObj<ServiceStatusService>;

    const profile: Profile = {
        id: '12345',
        name: 'John Doe',
        destinationAgencyIdentifier: '67890',
        originatingAgencyIdentifier: '101112',
        attentionIndicator: 'Attention!',
        sourceAgency: 'MITRE',
        isReportExclude: false,
        fullName: 'John D Doe',
        email: 'jdoe@iwp.org',
        phoneNumber: '555-5555',
        title: 'Duke',
        department: 'Sporting Goods',
        address: '123 Street St, A City, A State',
        userSetting: new UserSetting(),
        permissions: 5
    };

    beforeEach(waitForAsync(() => {
        profileService = jasmine.createSpyObj<ProfileService>(
            'ProfileService',
            ['getProfile', 'getUserPrivileges', 'setCurrentProfile'],
            {
                username$: new BehaviorSubject('jdoe'),
                currentProfile$: new BehaviorSubject(profile)
            }
        );
        profileService.getProfile.and.returnValue(of(profile));
        profileService.getUserPrivileges.and.returnValue(of({CREATE_SEARCH: 'CREATE_SEARCH'}));

        serviceStatusService = jasmine.createSpyObj<ServiceStatusService>(
            'ServiceStatusService',
            ['updateServiceVersions']
        );

        TestBed.configureTestingModule({
        declarations: [
            AppComponent
        ],
        imports: [ RouterTestingModule ],
        providers: [
            {provide: ProfileService, useValue: profileService},
            {provide: ServiceStatusService, useValue: serviceStatusService},
            {provide: NotificationsService, useValue: {}},
            {provide: LogonInfo, useValue: {getIsSecure: () => of(false)}},
            {provide: MatDialog, useValue: {}}
        ],
        schemas: [ NO_ERRORS_SCHEMA ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppComponent);
        app = fixture.componentInstance;
    });

    it('a. Creates the app.', () => {
        expect(app).toBeTruthy();
    });

    it('b. Has \'Iris Workstation\' as its title.', () => {
        expect(app.title).toEqual('Iris Workstation');
    });

    it('c. Renders its title in a span tag', () => {
        fixture.detectChanges();
        const compiled = fixture.debugElement.nativeElement;
        expect(compiled.querySelector('span').textContent).toContain('Iris Analysis Toolkit');
    });

    it(`d. profileSelected takes a profile like ${JSON.stringify(profile)} as a parameter and returns a void type.`, () => {
        const res = app.profileSelected(profile);
        expect(res).toBeUndefined();
        expect(profileService.setCurrentProfile).toHaveBeenCalledWith(profile);
    });
});
