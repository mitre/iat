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
import org.mitre.iwp.buffers.Pdm;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

@Entity
public class DualPDMCompareResponse extends ServiceResponse {
    private static final Logger log = LoggerFactory.getLogger(DualPDMCompareResponse.class);

    private Long probeId;
    private Long candidateId;
    private Long candidateEbtsImageId;

    private byte[] probe;
    private byte[] candidate;

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] serviceResponse;
    public void setServiceResponse(byte[] response) { this.serviceResponse = response; }
    public byte[] getServiceResponse() { return this.serviceResponse; }

    // Probe - Getter / Setters
    public byte[] getProbe() { return probe; }
    public void setProbe(byte[] probe) { this.probe = probe; }

    // Candidate - Getter / Setters
    public byte[] getCandidate() { return candidate; }
    public void setCandidate(byte[] candidate) { this.candidate = candidate; }

    // Ids - Getter / Setters
    public Long getProbeId() { return probeId; }
    public void setProbeId(Long probeId) { this.probeId = probeId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getCandidateEbtsImageId() { return candidateEbtsImageId; }
    public void setCandidateEbtsImageId(Long candidateEbtsImageId) { this.candidateEbtsImageId = candidateEbtsImageId; }


    public boolean buildFromServiceResponse() {

        if(serviceResponse.length == 0){
            log.info("Dual PDM Compare Response ServiceResponse length is 0");
            return false;
        }

        try {
            Pdm.DualPDMComparison comparison = Pdm.DualPDMComparison.parseFrom(serviceResponse);
            DualPDMCompareResponse response = new DualPDMCompareResponse();
            response.setProbe(comparison.getProbe().toByteArray());
            response.setCandidate(comparison.getCandidate().toByteArray());
            return true;

        } catch (InvalidProtocolBufferException e) {
            log.info(e.getMessage());
            return false;
        }
    }
}
