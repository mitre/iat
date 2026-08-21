/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Pipe, PipeTransform } from '@angular/core';
import { User } from '../user/user';

@Pipe({
  name: 'adminFilter'
})
export class AdminFilterPipe implements PipeTransform {
  transform(user: User[], args?: any): any {
    const sortedUsers = user.sort((p1, p2) => {
      const name1 = p1.username.toLowerCase();
      const name2 = p2.username.toLowerCase();
      if(name1 > name2) {
 return 1;
}
      if(name1 < name2) {
 return -1;
}
      return 0;
    });

    if (args == ''){
      return sortedUsers;
    }


    return sortedUsers.filter(usr => usr.username.toLowerCase().indexOf(args.toLowerCase()) !== -1);
  }
}
