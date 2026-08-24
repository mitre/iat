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
import { Observable } from 'rxjs';
import { Profile } from './profile';
import { ProfileService } from './profile.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('XXI. The ProfileService class:', () => {
  const profile: Profile = new Profile();


  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProfileService]
    });
  });

  it('a. getProfile takes zero parameters and returns an Observable Profile.', inject([ProfileService], (service: ProfileService) => {
    const res = service.getProfile();
    expect(res).toBeInstanceOf(Observable);
  }));

  it('b. updateProfile takes an object instance of the Profile class as a parameter and returns a void type.', inject([ProfileService], (service: ProfileService) => {
    const res = service.updateProfile(profile);
    expect(res).toBeUndefined();
  }));

  it('c. createEmptyProfile takes an zero parameters and returns an empty object instance of the Profile class.', inject([ProfileService], (service: ProfileService) => {
    const res = service.createEmptyProfile();
    expect(res).toBeInstanceOf(Object);
  }));

  it('d. getCurrentProfile takes zero parameters and returns a void type.', inject([ProfileService], (service: ProfileService) => {
    const res = service.getCurrentProfile();
    expect(res).toBeInstanceOf(Object);
  }));

  it('e. setCurrentProfile takes an object instance of the Profile class as a parameter and returns a void type.', inject([ProfileService], (service: ProfileService) => {
    const res = service.setCurrentProfile(profile);
    expect(res).toBeUndefined();
  }));

  it('f. getUserPrivileges takes zero parameters and returns an Obervable Object.', inject([ProfileService], (service: ProfileService) => {
    const res = service.getUserPrivileges();
    expect(res).toBeInstanceOf(Object);
  }));

  it('g. getUserList takes zero parameters and returns and Observable Object.', inject( [ProfileService], (service: ProfileService) => {
    const res = service.getUserList();
    expect(res).toBeInstanceOf(Object);
  }));
});
