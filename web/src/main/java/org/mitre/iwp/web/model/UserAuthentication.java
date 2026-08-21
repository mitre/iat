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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UserAuthentication {
  public UserAuthentication(){

  }

  public UserAuthentication(Long id, String username, String password, List<String> roles, Profile profile, boolean isNewUser){
    this.id = id;
    this.username = username;
    this.password = password;
    this.roles = roles;
    this.profile = profile;
	  this.isNewUser = isNewUser;
  }

  @Id
  @GeneratedValue
  private Long id;
  public Long getId() {
    return this.id;
  }
  public void setId(Long id) {
    this.id = id;
  }

  private String username;
  public String getUsername() {
    return this.username;
  }
  public void setUsername(String userName) {
    this.username = userName;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;
  public String getPassword() {
    return this.password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> roles = new ArrayList<>();
  public List<String> getRoles() {
    return this.roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  @OneToOne(cascade = CascadeType.ALL)
  private Profile profile;
  public Profile getProfile() {
    return this.profile;
  }
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  private boolean isNewUser;
  public boolean getIsNewUser() {
    return this.isNewUser;
  }
  public void setIsNewUser(boolean isNewUser) {
    this.isNewUser = isNewUser;
  }
}
