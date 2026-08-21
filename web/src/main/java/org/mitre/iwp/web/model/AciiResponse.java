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

@Entity
public class AciiResponse extends ServiceResponse {
    public void setVertical(String vertical) {
        this.vertical = vertical;
    }
    public String getVertical() {
        return this.vertical;
    }

    public void setHorizontal(String horizontal) {
        this.horizontal = horizontal;
    }
    public String getHorizontal() { return this.horizontal; }

    String vertical;
    String horizontal;
}
