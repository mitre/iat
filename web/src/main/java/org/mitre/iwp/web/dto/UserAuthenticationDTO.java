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

import java.util.List;

public class UserAuthenticationDTO {
    public UserAuthenticationDTO() {}

    public UserAuthenticationDTO(Long id, String username, String password, List<String> roles, ProfileDTO profile, boolean isNewUser){
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.profile = profile;
        this.isNewUser = isNewUser;
    }

    private Long id;
    String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public boolean getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(boolean isNewUser) {
        this.isNewUser = isNewUser;
    }

    String password;
    List<String> roles;
    ProfileDTO profile;
    boolean isNewUser;
}
