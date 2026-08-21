/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.dto;

import org.mitre.iwp.web.model.CaseInformation;
import org.mitre.iwp.web.model.Person;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class EbtsMapper {
    public CaseInformationDTO toCaseInformationDTO(CaseInformation caseInformation){
        if(caseInformation == null){
            return new CaseInformationDTO();
        }

        return new CaseInformationDTO(caseInformation.getId(), caseInformation.getComparisonDate(),
                caseInformation.getReceivedDate(), caseInformation.getRequestedBy());
    }

    public CaseInformation toCaseInformation(CaseInformationDTO caseInformation){
        if(caseInformation == null){
            return new CaseInformation();
        }

        return new CaseInformation(caseInformation.getId(), caseInformation.getComparisonDate(),
                caseInformation.getReceivedDate(), caseInformation.getRequestedBy());
    }

    public PersonDTO toPersonDTO(Person person){
        if(person == null){
            return new PersonDTO();
        }

        Collection<CaseInformationDTO> cases = new ArrayList<>();
        for(CaseInformation caseInformation : person.getCases()){
            cases.add(this.toCaseInformationDTO(caseInformation));
        }
        return new PersonDTO(person.getId(), person.getName(), person.getTitle(), person.getDepartment(), person.getAddress(), person.getEmail(),
                person.getPhone(), cases);
    }

    public Person toPerson(PersonDTO person){
        if(person == null){
            return new Person();
        }

        Collection<CaseInformation> cases = new ArrayList<>();
        for(CaseInformationDTO caseInformation : person.getCases()){
            cases.add(this.toCaseInformation(caseInformation));
        }
        return new Person(person.getId(), person.getName(), person.getTitle(), person.getDepartment(), person.getAddress(), person.getEmail(),
                person.getPhone(), cases);
    }
}
