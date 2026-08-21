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

import com.google.gson.annotations.Expose;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class ServiceResponse {
    @Id
    @GeneratedValue
    private Long imageId;
    public Long getImageId() { return this.imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }

    public void setError(String error) {
        this.error = error;
    }
    public String getError() {
        return error;
    }

    public void setStatus(ServiceCallStatus status) { this.status = status; }
    public ServiceCallStatus getStatus() { return this.status; }

    public void setEbtsImageId(Long ebtsImageId) { this.ebtsImageId = ebtsImageId; }
    public Long getEbtsImageId() { return this.ebtsImageId; }

    @Expose
    String error;
    @Expose
    String imagePath;
    @Expose
    ServiceCallStatus status;
    @Expose
    Long ebtsImageId;
}
