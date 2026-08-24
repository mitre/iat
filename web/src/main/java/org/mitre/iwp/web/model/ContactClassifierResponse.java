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
public class ContactClassifierResponse extends ServiceResponse  {
    String detectionCode;
    public String getDetectionCode() {
        return detectionCode;
    }
    public void setDetectionCode(String detectionCode) {
        this.detectionCode = detectionCode;
    }

    String processTimeMS;
    public String getProcessTimeMS() {
        return processTimeMS;
    }
    public void setProcessTimeMS(String processTimeMS) {
        this.processTimeMS = processTimeMS;
    }
}
