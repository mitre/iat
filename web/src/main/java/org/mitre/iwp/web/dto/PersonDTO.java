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

import java.util.Collection;
import java.util.ArrayList;

public class PersonDTO {
    public PersonDTO(){}

    public PersonDTO(Long id, String name, String title, String department, String address, String email, String phone, Collection<CaseInformationDTO> cases) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.department = department;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.cases = cases;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Collection<CaseInformationDTO> getCases() {
        return cases;
    }

    public void setCases(Collection<CaseInformationDTO> cases) {
        this.cases = cases;
    }

    Long id;
    String name;
    String title;
    String department;
    String address;
    String email;
    String phone;
    Collection<CaseInformationDTO> cases = new ArrayList<>();

}
