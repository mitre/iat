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
import org.mitre.iwp.buffers.Tshepii;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;

@Entity
public class TshepiiCompareResponse  {
    private static final Logger log = LoggerFactory.getLogger(TshepiiCompareResponse.class);
    @Id
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

    

    private Long candidateImageId;

    public void setCandidateImageId(Long candidateImageId) { this.candidateImageId = candidateImageId; }
    public Long getCandidateImageId() { return this.candidateImageId; }

    private Long candidateEbtsImageId;
    public void setCandidateEbtsImageId(Long candidateEbtsImageId) { this.candidateEbtsImageId = candidateEbtsImageId; }
    public Long getCandidateEbtsImageId() { return this.candidateEbtsImageId; }

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] serviceResponse;
    public void setServiceResponse(byte[] response) { this.serviceResponse = response; }
    public byte[] getServiceResponse() { return this.serviceResponse; }

    @Transient
    @Expose
    private TshepiiResponse probeResponse;

    @Transient
    @Expose
    private TshepiiResponse candidateResponse;

    @Expose
    String error;
    @Expose
    String imagePath;
    @Expose
    ServiceCallStatus status;
    @Expose
    Long ebtsImageId;

    public boolean buildFromServiceResponse(){
        if(serviceResponse.length == 0){
            log.info("Tshepii Compare Response ServiceResponse length is 0");
            return false;
        }

        try {
            Tshepii.TShepiiCompareResponse serviceMessage = Tshepii.TShepiiCompareResponse.parseFrom(serviceResponse);

            this.probeResponse = new TshepiiResponse();
            this.probeResponse.buildFromCryptResponse(serviceMessage.getCryptResponse());

            this.candidateResponse = new TshepiiResponse();
            this.candidateResponse.buildFromCryptResponse(serviceMessage.getCryptResponse2());

        } catch (InvalidProtocolBufferException e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }
}
