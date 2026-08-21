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

import java.io.Serializable;

import com.google.gson.annotations.Expose;

import jakarta.persistence.*;


@Entity
public class PdmDeformedImageResponse implements Serializable {

    // This is the unique id for the deformed image. It is the imageId and alpha score combined together.
    @Id
    @Expose
    private String deformedId;
    @Lob
	@Column(columnDefinition="LONGBLOB")
    @Expose
    private byte[] imageData;
    @Expose
    private float score;
    @Expose
    private String imageType;
    @Expose
    private int width;
    @Expose
    private int height;
    @Expose
    private Long ebtsImageId;
    @Expose
    private Long imageId;

    // Getters and setters
    public String getDeformedId() {
        return deformedId;
    }

    public void setDeformedId(Long imageId, String alphaScore) {
        this.deformedId =  String.join(".", Long.toString(imageId), alphaScore);
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setEbtsImageId(Long ebtsImageId) { this.ebtsImageId = ebtsImageId; }
    public Long getEbtsImageId() { return this.ebtsImageId; }

    public void setImageId(Long imageId) { this.imageId = imageId; }
    public Long getImageId() { return this.imageId; }

    public PdmDeformedImageResponse(){
        // Empty Constructor to satisfy JPA requirement
    }
    
}
