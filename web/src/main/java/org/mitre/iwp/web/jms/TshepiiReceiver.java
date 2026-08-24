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

import com.google.protobuf.*;
import org.mitre.iwp.buffers.Tshepii;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.service.TshepiiCompareResponseRepository;
import org.mitre.iwp.web.service.TshepiiResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "tshepiiService", havingValue = "true")
public class TshepiiReceiver {
    private static final Logger log = LoggerFactory.getLogger(TshepiiReceiver.class);
    private final TshepiiResponseRepository tshepiiResponseRepository;
    private final TshepiiCompareResponseRepository tshepiiCompareResponseRepository;

    @Autowired
    public TshepiiReceiver(
            TshepiiResponseRepository tshepiiResponseRepository,
            TshepiiCompareResponseRepository tshepiiCompareResponseRepository) {
        this.tshepiiResponseRepository = tshepiiResponseRepository;
        this.tshepiiCompareResponseRepository = tshepiiCompareResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getTshepiiQueueResponse()}")
    public void processMessage(Object object) {
        try {
            BytesMessage bMessage = (BytesMessage) object;

            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);
            Tshepii.TShepiiCompareResponse compareResponse = this.tryParseComparison(bytes);

            if(compareResponse == null){
                Tshepii.TShepiiResponse serviceResponse = Tshepii.TShepiiResponse.parseFrom(bytes);
                Long responseImageId = Long.parseLong(serviceResponse.getImageId());

                log.info("Tshepii Response for Image Id: {}", responseImageId);
                TshepiiResponse tshepiiResponse = this.tshepiiResponseRepository.findByImageId(responseImageId);

                if (tshepiiResponse == null) {
                    tshepiiResponse = new TshepiiResponse();
                }

                tshepiiResponse.setServiceResponse(bytes);
                tshepiiResponse.setStatus(ServiceCallStatus.Received);

                this.tshepiiResponseRepository.save(tshepiiResponse);
            } else {
                Long probeImageId = Long.parseLong(compareResponse.getCryptResponse().getImageId());
                Long candImageId = Long.parseLong(compareResponse.getCryptResponse2().getImageId());

                log.info("Tshepii Compare Response for Probe Id: {} and Candidate Id: {}",
                        probeImageId, candImageId);
                TshepiiCompareResponse tShepiiCompareResponse = this.tshepiiCompareResponseRepository.findByImageIdAndCandidateImageId(probeImageId, candImageId);

                if(tShepiiCompareResponse == null){
                    tShepiiCompareResponse = new TshepiiCompareResponse();
                }

                tShepiiCompareResponse.setServiceResponse(bytes);
                tShepiiCompareResponse.setStatus(ServiceCallStatus.Received);

                this.tshepiiCompareResponseRepository.save(tShepiiCompareResponse);
            }
        } catch (JMSException | InvalidProtocolBufferException jmsException) {
            log.info(jmsException.getMessage());
        }
    }

    public Tshepii.TShepiiCompareResponse tryParseComparison(byte[] bytes){
        try {
            Tshepii.TShepiiCompareResponse response = Tshepii.TShepiiCompareResponse.parseFrom(bytes);
            if(response.hasCryptResponse() && response.hasCryptResponse2()) {
                return response;
            } else {
                if(response.hasCryptResponse()){
                    log.info("Missing CryptResponse2");
                } else {
                    log.info("Missing CryptResponse");
                }
            }
        } catch(InvalidProtocolBufferException exc){
            log.info("Failed to parse Compare Response");
        }
        return null;
    }


}
