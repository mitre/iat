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

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class UserRole {
  public UserRole(){

  }

  public UserRole(Long id, String name, String description, Collection<UserAuthentication> users, Collection<Privilege> privileges){
    this.id = id;
    this.name = name;
    this.description = description;
    this.users = users;
    this.privileges = privileges;
  }

  @Id
  @GeneratedValue
  private Long id;
  public Long getId() { return this.id; }
  public void setId(Long id) { this.id = id; }

  private String name;
  public String getName() { return this.name; }
  public void setName(String name) { this.name = name; }

  private String description;
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  @ManyToMany
  private Collection<UserAuthentication> users = new ArrayList<>();
  public Collection<UserAuthentication> getUsers() {return users;}
  public void setUsers(Collection<UserAuthentication> users) {this.users = users;}
  public void addUser(UserAuthentication user) { this.users.add(user); }

  @ManyToMany
  private Collection<Privilege> privileges = new ArrayList<>();
  public Collection<Privilege> getPrivileges() {return privileges;}
  public void setPrivileges(Collection<Privilege> privileges) { this.privileges = privileges; }
  public void addPrivilege(Privilege privilege) { this.privileges.add(privilege); }
  public void removePrivilege(long privId) {
    this.privileges.removeIf(n-> (n.getId() == privId));
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UserRole role = (UserRole) obj;
      return this.equals(role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, users, privileges);
  }

  @Override
  public String toString() {
    return "Role [name=" + name + "]" + "[id=" + id + "]";
  }

  public List<String> getPrivilegeList(){
    List<String> list = new ArrayList<>();
    for (Privilege p : privileges) {
      list.add(p.getName());
    }
    return list;
  }

}
