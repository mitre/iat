/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.data;

import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Type17 {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private int length;
    public int getLength() { return length; }
    public void setLength( int length ) { this.length = length; } 

    private String imageDesignationCharacter;
    public String getImageDesignationCharacter() { return this.imageDesignationCharacter; }
    public void setImageDesignationCharacter(String imageDesignationCharacter) { this.imageDesignationCharacter = imageDesignationCharacter; }

    private int eyeLabel;
    public int getEyeLabel() { return this.eyeLabel; }
    public void setEyeLabel(int eyeLabel) { this.eyeLabel = eyeLabel; }

    private String sourceAgency;
    public String getSourceAgency() { return this.sourceAgency; }
    public void setSourceAgency(String sourceAgency) { this.sourceAgency = sourceAgency; }

    private String irisCaptureDate;
    public String getIrisCaptureDate() { return this.irisCaptureDate; }
    public void setIrisCaptureDate(String irisCaptureDate) { this.irisCaptureDate = irisCaptureDate; }

    private int horizontalLineLength;
    public int getHorizontalLineLength() { return this.horizontalLineLength; }
    public void setHorizontalLineLength(int horizontalLineLength) { this.horizontalLineLength = horizontalLineLength; }

    private int verticalLineLength;
    public int getVerticalLineLength() { return this.verticalLineLength; }
    public void setVerticalLineLength(int verticalLineLength) { this.verticalLineLength = verticalLineLength; }

    private int scaleUnits;
    public int getScaleUnits() { return this.scaleUnits; }
    public void setScaleUnits(int scaleUnits) { this.scaleUnits = scaleUnits; }

    private int transmittedHorizontalPixelScale;
    public int getTransmittedHorizontalPixelScale() { return this.transmittedHorizontalPixelScale; }
    public void setTransmittedHorizontalPixelScale(int transmittedHorizontalPixelScale) { this.transmittedHorizontalPixelScale = transmittedHorizontalPixelScale; }

    private int transmittedVerticalPixelScale;
    public int getTransmittedVerticalPixelScale() { return this.transmittedVerticalPixelScale; }
    public void setTransmittedVerticalPixelScale(int transmittedVerticalPixelScale) { this.transmittedVerticalPixelScale = transmittedVerticalPixelScale; }

    private String compressionAlgorithm;
    public String getCompressionAlgorithm() { return this.compressionAlgorithm; }
    public void setCompressionAlgorithm(String compressionAlgorithm) { this.compressionAlgorithm = compressionAlgorithm; }

    private int bitsPerPixel;
    public int getBitsPerPixel() { return this.bitsPerPixel; }
    public void setBitsPerPixel(int bitsPerPixel) { this.bitsPerPixel = bitsPerPixel; }

    private String colorSpace;
    public String getColorSpace() { return this.colorSpace; }
    public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }

    private int rotationAngleOfEye;
    public int getRotationAngleOfEye() { return this.rotationAngleOfEye; }
    public void setRotationAngleOfEye(int rotationAngleOfEye) { this.rotationAngleOfEye = rotationAngleOfEye; }

    private String rotationUncertainty;
    public String getRotationUncertainty() { return this.rotationUncertainty; }
    public void setRotationUncertainty(String rotationUncertainty) { this.rotationUncertainty = rotationUncertainty; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ImagePropertyCodeField imagePropertyCodeField;
    public ImagePropertyCodeField getImagePropertyCodeField() { return this.imagePropertyCodeField; }
    public void setImagePropertyCodeField(ImagePropertyCodeField imagePropertyCodeField) { this.imagePropertyCodeField = imagePropertyCodeField; }

    private String deviceUniqueIdentifier;
    public String getDeviceUniqueIdentifier() { return this.deviceUniqueIdentifier; }
    public void setDeviceUniqueIdentifier(String deviceUniqueIdentifier) { this.deviceUniqueIdentifier = deviceUniqueIdentifier; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MakeModelSerialNumberField makeModelSerialNumberField;
    public MakeModelSerialNumberField getMakeModelSerialNumberField() { return this.makeModelSerialNumberField; }
    public void setMakeModelSerialNumberField(MakeModelSerialNumberField makeModelSerialNumberField) { this.makeModelSerialNumberField = makeModelSerialNumberField; }

    private String eyeColor;
    public String getEyeColor() { return this.eyeColor; }
    public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }

    private String comment;
    public String getComment() { return this.comment; }
    public void setComment(String comment) { this.comment = comment; }

    private String effectiveAcquisitionSpectrum;
    public String getEffectiveAcquisitionSpectrum() { return this.effectiveAcquisitionSpectrum; }
    public void setEffectiveAcquisitionSpectrum(String effectiveAcquisitionSpectrum) { this.effectiveAcquisitionSpectrum = effectiveAcquisitionSpectrum; }

    private String damagedOrMissingEye;
    public String getDamagedOrMissingEye() { return this.damagedOrMissingEye; }
    public void setDamagedOrMissingEye(String damagedOrMissingEye) { this.damagedOrMissingEye = damagedOrMissingEye; }

    private int subjectAcquisitionProfileIris;
    public int getSubjectAcquisitionProfileIris() { return this.subjectAcquisitionProfileIris; }
    public void setSubjectAcquisitionProfileIris(int subjectAcquisitionProfileIris) { this.subjectAcquisitionProfileIris = subjectAcquisitionProfileIris; }

    private int irisStorageFormat;
    public int getIrisStorageFormat() { return this.irisStorageFormat; }
    public void setIrisStorageFormat(int irisStorageFormat) { this.irisStorageFormat = irisStorageFormat; }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnotationInformationField> annotationInformationFieldList = new ArrayList<>();
    public List<AnnotationInformationField> getAnnotationInformationFieldList() { return this.annotationInformationFieldList; }
    public void setAnnotationInformationFieldList( List<AnnotationInformationField> annotationInformationFieldList ) {
      this.annotationInformationFieldList = annotationInformationFieldList;
    }

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] data; //17.999
    public byte[] getData() { return data; }
    public void setData( byte[] data ) { this.data = data; }

    private long ebtsImageId;
    public long getEbtsImageId() { return this.ebtsImageId; }
    public void setEbtsImageId( long ebtsImageId ) { this.ebtsImageId = ebtsImageId; }

    private static final String imageNoneMessage = "image/none";

    public Type17() {
      // Empty constructor to satisfy JPA requirement
  }

    public static Type17 buildFromEbtsRecord(LogicalRecord logicalRecord) {
        Type17 type17 = new Type17();

        type17.length = EbtsConversionHelper.getIntFromField(logicalRecord.getField(1), Integer.MAX_VALUE);
        type17.imageDesignationCharacter = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2));
        type17.eyeLabel = EbtsConversionHelper.getIntFromField(logicalRecord.getField(3), Integer.MAX_VALUE);
        type17.sourceAgency = EbtsConversionHelper.getStringFromField(logicalRecord.getField(4));
        type17.irisCaptureDate = EbtsConversionHelper.getStringFromField(logicalRecord.getField(5));
        type17.horizontalLineLength = EbtsConversionHelper.getIntFromField(logicalRecord.getField(6), Integer.MAX_VALUE);
        type17.verticalLineLength = EbtsConversionHelper.getIntFromField(logicalRecord.getField(7), Integer.MAX_VALUE);
        type17.scaleUnits = EbtsConversionHelper.getIntFromField(logicalRecord.getField(8), Integer.MAX_VALUE);
        type17.transmittedHorizontalPixelScale = EbtsConversionHelper.getIntFromField(logicalRecord.getField(9), Integer.MAX_VALUE);
        type17.transmittedVerticalPixelScale = EbtsConversionHelper.getIntFromField(logicalRecord.getField(10), Integer.MAX_VALUE);
        type17.compressionAlgorithm = EbtsConversionHelper.getStringFromField(logicalRecord.getField(11));
        type17.bitsPerPixel = EbtsConversionHelper.getIntFromField(logicalRecord.getField(12), Integer.MAX_VALUE);
        type17.colorSpace = EbtsConversionHelper.getStringFromField(logicalRecord.getField(13));
        type17.rotationAngleOfEye = EbtsConversionHelper.getIntFromField(logicalRecord.getField(14), Integer.MAX_VALUE);
        type17.rotationUncertainty = EbtsConversionHelper.getStringFromField(logicalRecord.getField(15));
        type17.imagePropertyCodeField = ImagePropertyCodeField.buildFromEbtsField(logicalRecord.getField(16));
        type17.deviceUniqueIdentifier = EbtsConversionHelper.getStringFromField(logicalRecord.getField(17));
        type17.makeModelSerialNumberField = MakeModelSerialNumberField.buildFromEbtsField(logicalRecord.getField(19));
        type17.eyeColor = EbtsConversionHelper.getStringFromField(logicalRecord.getField(20));
        type17.comment = EbtsConversionHelper.getStringFromField(logicalRecord.getField(21));
        type17.damagedOrMissingEye = EbtsConversionHelper.getStringFromField(logicalRecord.getField(28));
        type17.effectiveAcquisitionSpectrum = EbtsConversionHelper.getStringFromField(logicalRecord.getField(25));
        type17.subjectAcquisitionProfileIris = EbtsConversionHelper.getIntFromField(logicalRecord.getField(31), Integer.MAX_VALUE);
        type17.irisStorageFormat = EbtsConversionHelper.getIntFromField(logicalRecord.getField(32), Integer.MAX_VALUE);

        org.mitre.jet.ebts.field.Field field = logicalRecord.getField(902); // ann
        if(field != null){
            for(Occurrence occurrence : field.getOccurrences()) {
                type17.annotationInformationFieldList.add(AnnotationInformationField.buildFromEbtsOccurence(occurrence));
            }
        }

        type17.data = logicalRecord.getImageData();

        return type17;
    }

    public String toString() {
        return String.format("%s#<id='%s'", 
                this.getClass().getSimpleName(), id);
    }

    public static LogicalRecord buildEbtsLogicalRecord(Type17 t17) {
        LogicalRecord logicalRecord = new GenericRecord(17);

        logicalRecord.setField(3, EbtsConversionHelper.getFieldFromInt(t17.getEyeLabel()));
        logicalRecord.setField(4, EbtsConversionHelper.getFieldFromString(t17.getSourceAgency()));
        logicalRecord.setField(5, EbtsConversionHelper.getFieldFromString(t17.getIrisCaptureDate()));
        logicalRecord.setField(6, EbtsConversionHelper.getFieldFromInt(t17.getHorizontalLineLength()));
        logicalRecord.setField(7, EbtsConversionHelper.getFieldFromInt(t17.getVerticalLineLength()));
        logicalRecord.setField(8, EbtsConversionHelper.getFieldFromInt(t17.getScaleUnits()));
        logicalRecord.setField(9, EbtsConversionHelper.getFieldFromInt(t17.getTransmittedHorizontalPixelScale()));
        logicalRecord.setField(10, EbtsConversionHelper.getFieldFromInt(t17.getTransmittedVerticalPixelScale()));
        logicalRecord.setField(11, EbtsConversionHelper.getFieldFromString(t17.getCompressionAlgorithm()));
        logicalRecord.setField(12, EbtsConversionHelper.getFieldFromInt(t17.getBitsPerPixel()));
        logicalRecord.setField(13, EbtsConversionHelper.getFieldFromString(t17.getColorSpace()));
        logicalRecord.setField(14, EbtsConversionHelper.getFieldFromInt(t17.getRotationAngleOfEye()));
        logicalRecord.setField(15, EbtsConversionHelper.getFieldFromString(t17.getRotationUncertainty()));
        logicalRecord.setField(17, EbtsConversionHelper.getFieldFromString(t17.getDeviceUniqueIdentifier()));
        logicalRecord.setField(20, EbtsConversionHelper.getFieldFromString(t17.getEyeColor()));
        logicalRecord.setField(21, EbtsConversionHelper.getFieldFromString(t17.getComment()));
        logicalRecord.setField(25, EbtsConversionHelper.getFieldFromString(t17.getEffectiveAcquisitionSpectrum()));
        logicalRecord.setField(28, EbtsConversionHelper.getFieldFromString(t17.getDamagedOrMissingEye()));
        logicalRecord.setField(31, EbtsConversionHelper.getFieldFromInt(t17.getSubjectAcquisitionProfileIris()));
        logicalRecord.setField(32, EbtsConversionHelper.getFieldFromInt(t17.getIrisStorageFormat()));

        // Create 'Image Property Code' Occurrence - 1 Occurrence
        org.mitre.jet.ebts.field.Field tempField = EbtsConversionHelper.getEmptyField();
        tempField.getOccurrences().add(ImagePropertyCodeField.buildEbtsOccurrence(t17.getImagePropertyCodeField()));
        logicalRecord.setField(16, tempField);

        // Create 'Make/Model/Serial Number' Occurrence - 1 Occurrence
        tempField = EbtsConversionHelper.getEmptyField();
        tempField.getOccurrences().add(MakeModelSerialNumberField.buildEbtsOccurrence(t17.getMakeModelSerialNumberField()));
        logicalRecord.setField(19, tempField);

        tempField = EbtsConversionHelper.getEmptyField();
        for(AnnotationInformationField ann : t17.getAnnotationInformationFieldList()) {
            tempField.getOccurrences().add(AnnotationInformationField.buildEbtsOccurrence(ann));
        }
        logicalRecord.setField(902, tempField);

        // Get Image Data, if available

        logicalRecord.setImageData(t17.getData());

        return logicalRecord;
    }

    public static String getContentType(String compressionAlgorithm) {
      if( compressionAlgorithm.equalsIgnoreCase("png") ) {
        return "image/png";
      } else if( compressionAlgorithm.equalsIgnoreCase("none") ) {
        return "image/raw";
      } else if( compressionAlgorithm.equalsIgnoreCase("jp2") ) {
        return "image/jp2";
      } else if( compressionAlgorithm.equalsIgnoreCase("jp2l") ) {
        return "image/jp2l";
      } else if( compressionAlgorithm.equalsIgnoreCase("jpeg") || compressionAlgorithm.equalsIgnoreCase("jpegb") || compressionAlgorithm.equalsIgnoreCase("jpegl")) {
        return "image/jpeg";
      } 

      return imageNoneMessage;
    }

    public static String getBinaryContentType(String compressionAlgorithm){

        if(compressionAlgorithm.equals("0")){
            return imageNoneMessage;
        } else if (compressionAlgorithm.equals("1")){
            return "image/WSQ20";
        }else if (compressionAlgorithm.equals("2")){
            return "image/JPEGB";
        }else if (compressionAlgorithm.equals("3")){
            return "image/JPEGL";
        }else if (compressionAlgorithm.equals("4")){
            return "image/JP2";
        }else if (compressionAlgorithm.equals("5")){
            return "image/JP2L";
        }

        return imageNoneMessage;
    }
}
