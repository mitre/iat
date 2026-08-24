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
import { Person } from '../types/person';

@Pipe({
  name: 'personFilter'
})
export class PersonFilterPipe implements PipeTransform {

  transform(persons: Person[], args?: any): any  {
    // Sort List
    const sortedPersons = persons.sort((p1, p2) => {
      const name1 = p1.name.toLowerCase();
      const name2 = p2.name.toLowerCase();
      if (name1 > name2) {
        return 1;
      }
      if (name1 < name2) {
        return -1;
      }
      return 0;
    });

    if (args == '') {
return sortedPersons;
}

    return sortedPersons.filter(person => person.name.toLowerCase().indexOf(args.toLowerCase()) !== -1);
  }

}
