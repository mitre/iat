/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Profile } from '../profile/profile';
import { User } from '../user/user';
import { AdminFilterPipe } from './admin-filter.pipe';

describe('II. The AdminFilterPipe Class:', () => {
  const pipe = new AdminFilterPipe();
  const profile = new Profile();

  const user1 = new User(-1, 'jdoe', profile);
  const user2 = new User(1, 'jsmith', profile);

  const userArray = [user1, user2];
  const jsonArray = [JSON.stringify(user1), JSON.stringify(user2)];

  const filter = 'jsmith';
  const transformer = pipe.transform(userArray, filter);
  const results = transformer[0].username;

  it(`a. Creates an array of User: ${jsonArray}.`, () => {
    expect(transformer).toBeInstanceOf(Array);
  });

  it(`b. Filters the User array by username "${filter}."`, () => {
    expect(results).toEqual(filter);
  });

  it (`c. Returns the ${transformer.length} element that contains the username ${filter}: ${jsonArray[1]}.`, () => {
    expect(transformer.length).toEqual(1);
  });
});
