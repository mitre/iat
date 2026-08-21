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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AppNotification {
    public AppNotification(){

    }

    public AppNotification(Long id, Long userId, String message, String icon, String action, String dateCreated, boolean isNew) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.icon = icon;
        this.action = action;
        this.dateCreated = dateCreated;
        this.isNew = isNew;
    }

    @Id
    @GeneratedValue
    private long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private Long userId;
    public Long getUserId(){return this.userId;}
    public void setUserId(Long userId) { this.userId = userId; }

    private String message;
    public String getMessage() { return this.message; }
    public void setMessage(String message) { this.message = message; }

    private String icon;
    public String getIcon() { return this.icon; }
    public void setIcon(String icon) { this.icon = icon; }

    private String action;
    public String getAction() { return this.action; }
    public void setAction(String action) { this.action = action; }

    private String dateCreated;
    public String getDateCreated() { return this.dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }

    private boolean isNew;
    public boolean getIsNew() { return this.isNew; }
    public void setIsNew(boolean isNew) { this.isNew = isNew; }
}
