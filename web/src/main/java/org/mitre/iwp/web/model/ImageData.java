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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class ImageData {
    private static final Logger log = LoggerFactory.getLogger(ImageData.class);

    public ImageData() {
    }

    public ImageData(Long ebtsImageId) {
        this.ebtsImageId = ebtsImageId;
    }

    public ImageData(Long id, Long ebtsImageId, Long unrollImageId, Map<String, String> unrollRingData,
            Long rasterizeEbtsImageId, EyeLabel eyeLabel, ContactType contactType,
            List<Annotation> annotations, String aciiHorizontal, boolean reported,
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
        this.iData = imageData;
        this.unrollImageData = unrollImageData;
        this.imageBytes = imageBytes;
        this.unrollImageBytes = unrollImageBytes;
        this.recordType = recordType;
        this.recordIndex = recordIndex;
        this.qualityScore = qualityScore;
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

    private Long ebtsImageId;

    public Long getEbtsImageId() {
        return this.ebtsImageId;
    }

    public void setEbtsImageId(Long ebtsImageId) {
        this.ebtsImageId = ebtsImageId;
    }

    private Long unrollImageId;

    public Long getUnrollImageId() {
        return this.unrollImageId;
    }

    public void setUnrollImageId(Long unrollImageId) {
        this.unrollImageId = unrollImageId;
    }

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "unroll_ring_map", joinColumns = { @JoinColumn(name = "unroll_id") })
    private Map<String, String> unrollRingData = new HashMap<>();

    public Map<String, String> getUnrollRingData() {
        return this.unrollRingData;
    }

    public void setUnrollRingData(Map<String, String> unrollRingData) {
        this.unrollRingData = unrollRingData;
    }

    private Long rasterizeEbtsImageId;

    public Long getRasterizeEbtsImageId() {
        return this.rasterizeEbtsImageId;
    }

    public void setRasterizeEbtsImageId(Long rasterEbtsImageId) {
        this.rasterizeEbtsImageId = rasterEbtsImageId;
    }

    @Enumerated(value = EnumType.STRING)
    private EyeLabel eyeLabel = EyeLabel.UNDEFINED;

    public EyeLabel getEyeLabel() {
        return this.eyeLabel;
    }

    public void setEyeLabel(EyeLabel label) {
        this.eyeLabel = label;
    }

    @Enumerated(value = EnumType.STRING)
    private ContactType contactType = ContactType.NONE;

    public ContactType getContactType() {
        return this.contactType;
    }

    public void setContactType(ContactType type) {
        this.contactType = type;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Annotation> annotations = new ArrayList<>();

    public List<Annotation> getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    private String aciiHorizontal;

    public String getAciiHorizontal() {
        return this.aciiHorizontal;
    }

    public void setAciiHorizontal(String response) {
        this.aciiHorizontal = response;
    }

    private boolean reported = false;

    public boolean isReported() {
        return this.reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    private String imageNotes = "";

    public String getImageNotes() {
        return this.imageNotes;
    }

    public void setImageNotes(String imageNotes) {
        this.imageNotes = imageNotes;
    }

    private String dme = "";

    public String getDme() {
        return this.dme;
    }

    public void setDme(String dme) {
        this.dme = dme;
    }

    @Transient
    @Lob
    private String iData;

    public String getImageData() {
        return this.iData;
    }

    public void setImageData(String imageData) {
        this.iData = imageData;
    }

    @Transient
    private String unrollImageData;

    public String getUnrollImageData() {
        return this.unrollImageData;
    }

    public void setUnrollImageData(String unrollImageData) {
        this.unrollImageData = unrollImageData;
    }

    @Transient
    private byte[] imageBytes;

    public byte[] getImageBytes() {
        return this.imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    @Transient
    private byte[] unrollImageBytes = new byte[0];

    public byte[] getUnrollImageBytes() {
        return this.unrollImageBytes;
    }

    public void setUnrollImageBytes(byte[] unrollImageBytes) {
        this.unrollImageBytes = unrollImageBytes;
    }

    private int recordType;

    public int getRecordType() {
        return this.recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    private int recordIndex;

    public int getRecordIndex() {
        return this.recordIndex;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    private int qualityScore;

    public int getQualityScore() {
        return this.qualityScore;
    }

    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLineString = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLineString);

        // determine fields declared in this class only (no fields of superclass)
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();

        // print field names paired with their values
        for (java.lang.reflect.Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                // requires access to private field:
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
                log.error(ex.getMessage(), ex);
            }
            result.append(newLineString);
        }
        result.append("}");

        return result.toString();
    }
}
