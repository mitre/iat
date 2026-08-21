/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.jms;

import org.mitre.iwp.buffers.Pdm;
import org.mitre.iwp.web.model.DualPDMCompareResponse;
import org.mitre.iwp.web.model.DualPDMResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.mitre.iwp.web.service.DualPDMCompareResponseRepository;
import org.mitre.iwp.web.service.DualPDMResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "dualPDMService", havingValue = "true")
public class DualPDMReceiver {
    private static final Logger log = LoggerFactory.getLogger(DualPDMReceiver.class);
    private final DualPDMResponseRepository dualPDMResponseRepository;
    private final DualPDMCompareResponseRepository dualPDMCompareResponseRepository;

    @Autowired
    public DualPDMReceiver(
        DualPDMResponseRepository dualPDMResponseRepository,
        DualPDMCompareResponseRepository dualPDMCompareResponseRepository) {
        this.dualPDMResponseRepository = dualPDMResponseRepository;
        this.dualPDMCompareResponseRepository = dualPDMCompareResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getDualPDMQueueResponse()}")
    public void processMessage(BytesMessage bMessage) {
        try {
            log.info("Received Response from Iwp-pdm DUAL PDM");

            Long probeId = bMessage.getLongProperty("probeId");
            Long candidateId = bMessage.getLongProperty("candidateId");

            // Parse the incoming message
            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);


            // Attempt to parse the message as DualPDMComparison
            Pdm.DualPDMComparison comparison = this.tryParseComparison(bytes);
            if (comparison == null) {
                log.error("Failed to parse DualPDMComparison message.");
                return;
            }

            // Extract probe and candidate images
            byte[] probeBytes = comparison.getProbe().toByteArray();
            byte[] candidateBytes = comparison.getCandidate().toByteArray();

            log.info("DualPDM Response for Probe Id: {} and Candidate Id: {}", probeId, candidateId);

            DualPDMResponse probeResponse = dualPDMResponseRepository.findByImageId(probeId);
            if (probeResponse == null) {
                probeResponse = new DualPDMResponse();
            }
            probeResponse.setImageBytes(probeBytes);
            probeResponse.setStatus(ServiceCallStatus.Received);
            dualPDMResponseRepository.save(probeResponse);

            DualPDMResponse candidateResponse = dualPDMResponseRepository.findByImageId(candidateId);
            if (candidateResponse == null) {
                candidateResponse = new DualPDMResponse();
            }
            candidateResponse.setImageBytes(candidateBytes);
            candidateResponse.setStatus(ServiceCallStatus.Received);
            dualPDMResponseRepository.save(candidateResponse);

            // Save the combined comparison response
            DualPDMCompareResponse compareResponse = dualPDMCompareResponseRepository.findByProbeIdAndCandidateId(probeId, candidateId);
            if (compareResponse == null) {
                compareResponse = new DualPDMCompareResponse();
                compareResponse.setProbeId(probeId);
                compareResponse.setCandidateId(candidateId);
            }
            compareResponse.setServiceResponse(bytes);
            compareResponse.setStatus(ServiceCallStatus.Received);
            dualPDMCompareResponseRepository.save(compareResponse);

        } catch (JMSException ex) {
            log.error("Error processing DualPDM message: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Attempt to parse the incoming bytes as a DualPDMComparison message.
     */
    private Pdm.DualPDMComparison tryParseComparison(byte[] bytes) {
        try {
            return Pdm.DualPDMComparison.parseFrom(bytes);
        } catch (InvalidProtocolBufferException ex) {
            log.error("Failed to parse DualPDMComparison message: {}", ex.getMessage());
            return null;
        }
    }
}
