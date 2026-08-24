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

public class UserRoleDTO {
    public UserRoleDTO(){}

    public UserRoleDTO(Long id, String name, String description, Collection<UserAuthenticationDTO> users, Collection<PrivilegeDTO> privileges){
        this.id = id;
        this.name = name;
        this.description = description;
        this.users = users;
        this.privileges = privileges;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<UserAuthenticationDTO> getUsers() {
        return users;
    }

    public void setUsers(Collection<UserAuthenticationDTO> users) {
        this.users = users;
    }

    public Collection<PrivilegeDTO> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Collection<PrivilegeDTO> privileges) {
        this.privileges = privileges;
    }

    Long id;
    String name;
    String description;
    Collection<UserAuthenticationDTO> users;
    Collection<PrivilegeDTO> privileges;
}
