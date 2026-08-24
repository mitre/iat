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

import org.mitre.iwp.web.model.ContactType;
import org.mitre.iwp.web.model.EyeLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDataDTO {
    public ImageDataDTO(){}

    public ImageDataDTO(Long id, Long ebtsImageId, Long unrollImageId, Map<String, String> unrollRingData,
                        Long rasterizeEbtsImageId, EyeLabel eyeLabel, ContactType contactType,
                        List<AnnotationDTO> annotations, String aciiHorizontal, boolean reported,
                        String imageNotes, String dme, String imageData, String unrollImageData,
                        byte[] imageBytes, byte[] unrollImageBytes,
                        int recordType, int recordIndex, int qualityScore) {
        this.id = id;
        this.ebtsImageId = ebtsImageId;
        this.unrollImageId = unrollImageId;
        this.unrollRingData = unrollRingData;
        this.rasterizeEbtsImageId = rasterizeEbtsImageId;
        this.eyeLabel = eyeLabel;
        this.contactType = contactType;
        this.annotations = annotations;
        this.aciiHorizontal = aciiHorizontal;
        this.reported = reported;
        this.imageNotes = imageNotes;
        this.dme = dme;
        this.imageData = imageData;
        this.unrollImageData = unrollImageData;
        this.imageBytes = imageBytes;
        this.unrollImageBytes = unrollImageBytes;
        this.recordType = recordType;
        this.recordIndex = recordIndex;
        this.qualityScore = qualityScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEbtsImageId() {
        return ebtsImageId;
    }

    public void setEbtsImageId(Long ebtsImageId) {
        this.ebtsImageId = ebtsImageId;
    }

    public Long getUnrollImageId() {
        return unrollImageId;
    }

    public void setUnrollImageId(Long unrollImageId) {
        this.unrollImageId = unrollImageId;
    }

    public Map<String, String> getUnrollRingData() {
        return unrollRingData;
    }

    public void setUnrollRingData(Map<String, String> unrollRingData) {
        this.unrollRingData = unrollRingData;
    }

    public Long getRasterizeEbtsImageId() {
        return rasterizeEbtsImageId;
    }

    public void setRasterizeEbtsImageId(Long rasterizeEbtsImageId) {
        this.rasterizeEbtsImageId = rasterizeEbtsImageId;
    }

    public EyeLabel getEyeLabel() {
        return eyeLabel;
    }

    public void setEyeLabel(EyeLabel eyeLabel) {
        this.eyeLabel = eyeLabel;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
    }

    public List<AnnotationDTO> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationDTO> annotations) {
        this.annotations = annotations;
    }

    public String getAciiHorizontal() {
        return aciiHorizontal;
    }

    public void setAciiHorizontal(String aciiHorizontal) {
        this.aciiHorizontal = aciiHorizontal;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public String getImageNotes() {
        return imageNotes;
    }

    public void setImageNotes(String imageNotes) {
        this.imageNotes = imageNotes;
    }

    public String getDme() {
        return dme;
    }

    public void setDme(String dme) {
        this.dme = dme;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getUnrollImageData() {
        return unrollImageData;
    }

    public void setUnrollImageData(String unrollImageData) {
        this.unrollImageData = unrollImageData;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public byte[] getUnrollImageBytes() {
        return unrollImageBytes;
    }

    public void setUnrollImageBytes(byte[] unrollImageBytes) {
        this.unrollImageBytes = unrollImageBytes;
    }

    public int getRecordType() { return this.recordType; }
    public void setRecordType(int recordType) { this.recordType = recordType; }

    public int getRecordIndex() { return this.recordIndex; }
    public void setRecordIndex(int recordIndex) { this.recordIndex = recordIndex; }

    public int getQualityScore() { return this.qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }

    Long id;
    Long ebtsImageId;
    Long unrollImageId;
    Map<String, String> unrollRingData = new HashMap<>();
    Long rasterizeEbtsImageId;
    EyeLabel eyeLabel;
    ContactType contactType;
    List<AnnotationDTO> annotations = new ArrayList<>();
    String aciiHorizontal;
    boolean reported = false;
    String imageNotes = "";
    String dme = "";
    String imageData;
    String unrollImageData;
    byte[] imageBytes;
    byte[] unrollImageBytes = new byte[0];
    private int recordIndex;
    private int recordType;
    private int qualityScore;
}
