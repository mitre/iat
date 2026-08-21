/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.model;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.ArrayList;

@Entity
public class Person {
    public Person(){

    }

    public Person(Long id, String name, String title, String department, String address, String email, String phone, Collection<CaseInformation> cases) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.department = department;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.cases = cases;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String title;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    private String department;
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    private String address;
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    private String phone;
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Collection<CaseInformation> cases = new ArrayList<CaseInformation>();

    public Collection<CaseInformation> getCases() {
        return new ArrayList<CaseInformation>(this.cases);
    }

    public void addCaseInformation(CaseInformation caseInformation) {
        if (this.cases.contains(caseInformation)) return;

        this.cases.add(caseInformation);
    }

    public void removeCaseInformation(CaseInformation caseInformation) {
        if (!this.cases.contains(caseInformation)) return;

        this.cases.remove(caseInformation);
    }

    public boolean isPopulated() {
        boolean populated;

        populated = StringUtils.isNotBlank(this.name);
        populated |= StringUtils.isNotBlank(this.title);
        populated |= StringUtils.isNotBlank(this.department);
        populated |= StringUtils.isNotBlank(this.address);
        populated |= StringUtils.isNotBlank(this.email);
        populated |= StringUtils.isNotBlank(this.phone);

        return populated;
    }
}
