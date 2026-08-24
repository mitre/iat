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

import com.google.protobuf.InvalidProtocolBufferException;
import org.mitre.iwp.buffers.Acii.AciiServiceResponse;
import org.mitre.iwp.web.model.AciiResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.mitre.iwp.web.service.AciiResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "aciiService", havingValue = "true")
public class AciiReceiver {
    private static final Logger log = LoggerFactory.getLogger(AciiReceiver.class);
    private final AciiResponseRepository aciiResponseRepository;

    @Autowired
    public AciiReceiver(
            AciiResponseRepository aciiResponseRepository) {
        this.aciiResponseRepository = aciiResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getAciiQueueResponse()}")
    public void processMessage(Object object) {
        try {
            BytesMessage bMessage = (BytesMessage) object;

            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);
            AciiServiceResponse serviceResponse = AciiServiceResponse.parseFrom(bytes);
            Long responseImageId = Long.parseLong(serviceResponse.getImageId());

            log.info("Acii Response for Image Id: {}", responseImageId);
            AciiResponse aciiResponse = this.aciiResponseRepository.findByImageId(responseImageId);

            if(aciiResponse == null){
                aciiResponse = new AciiResponse();
            }

            aciiResponse.setHorizontal(serviceResponse.getHorizontal().toString());
            aciiResponse.setVertical(serviceResponse.getVertical().toString());
            aciiResponse.setStatus(ServiceCallStatus.Received);
            
            this.aciiResponseRepository.save(aciiResponse);
        } catch (JMSException | InvalidProtocolBufferException jmsException) {
            log.debug(jmsException.getMessage());

        }
    }
}
