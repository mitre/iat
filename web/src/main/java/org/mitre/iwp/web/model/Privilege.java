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

import com.google.common.base.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Privilege {
  @Id
  @GeneratedValue
  private Long id;
  public Long getId() { return this.id; }
  public void setId(Long id) { this.id = id; }

  public Privilege(){

  }

  public Privilege(String name, String description){
    this.name = name;
    this.description = description;
  }

  public Privilege(Long id, String name, String description) {
    this(name, description);
    this.id = id;
  }

  private String name;
  public String getName() { return this.name; }
  public void setName(String name) { this.name = name; }

  private String description;
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

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
    final Privilege priv = (Privilege) obj;
      return this.equals(priv);
  }

  @Override
  public int hashCode(){
    return Objects.hashCode(id, name, description);
  }

  @Override
  public String toString() {
    return "Privilege [name=" + name + "]" + "[id=" + id + "]";
  }



}
