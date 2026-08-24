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
import com.google.protobuf.InvalidProtocolBufferException;
import org.mitre.iwp.buffers.IrisAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class IrisAnnotationResponse extends ServiceResponse {
    private static final Logger log = LoggerFactory.getLogger(IrisAnnotationResponse.class);

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] serviceResponse;
    public void setServiceResponse(byte[] response) { this.serviceResponse = response; }
    public byte[] getServiceResponse() { return this.serviceResponse; }


    @Transient
    @Expose
    private String response;

    @Transient
    @Expose
    private String responseCode;

    @Transient
    @Expose
    private String responseMessage;

    @Transient
    @Expose
    private String responseTimeMs;

    @Transient
    @Expose
    private List<Polygon> polygonList = new ArrayList<>();
    public void setPolygonList(List<Polygon> polygonList) { 
        this.polygonList = polygonList;
    }
    public List<Polygon> getPolygonList() { 
        return this.polygonList; 
    }

    public boolean buildIrisAnnotationReply(){
        if(this.serviceResponse.length == 0){
            log.info("Service Response is empty");
            return false;
        }

        try {
            IrisAnnotation.IrisAnnotationReply irisAnnotationReply = IrisAnnotation.IrisAnnotationReply.parseFrom(this.serviceResponse);

            this.responseCode = irisAnnotationReply.getResponseCode();
            this.responseTimeMs = irisAnnotationReply.getResponseTimeMS();
            this.response = irisAnnotationReply.getResponse();
            this.responseCode = irisAnnotationReply.getResponseCode();

            if(this.getEbtsImageId() == null || this.getEbtsImageId() == 0){
                this.setEbtsImageId(Long.parseLong(irisAnnotationReply.getId()));
            }

            for(IrisAnnotation.Annotation annotation : irisAnnotationReply.getResultsList()){
                for(IrisAnnotation.Polygon annoPoly : annotation.getPolygonsList()) {
                    Polygon poly = new Polygon();
                    poly.setCode(annotation.getId());
                    
                    for(IrisAnnotation.Point point: annoPoly.getOutline().getPointsList()){
                        poly.getOutline().add(new Point(point.getX(), point.getY()));
                    }

                    this.polygonList.add(poly);
                }
            }

        } catch (InvalidProtocolBufferException e) {
            this.setError(e.getMessage());
            log.info(e.getMessage());
            return false;
        }

        return true;
    }
}
