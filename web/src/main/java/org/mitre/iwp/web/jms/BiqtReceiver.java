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
import org.mitre.iwp.buffers.Biqt;
import org.mitre.iwp.web.model.BiqtResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.mitre.iwp.web.service.BiqtResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "biqtService", havingValue = "true")
public class BiqtReceiver {
    private static final Logger log = LoggerFactory.getLogger(BiqtReceiver.class);
    private final BiqtResponseRepository biqtResponseRepository;

    @Autowired
    public BiqtReceiver(BiqtResponseRepository biqtResponseRepository) {
        this.biqtResponseRepository = biqtResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getBiqtQueueResponse()}")
    public void processMessage(Object object) {
        try {
            BytesMessage bMessage = (BytesMessage) object;

            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);
            Biqt.BiqtResponse serviceResponse = Biqt.BiqtResponse.parseFrom(bytes);
            Long responseImageId = Long.parseLong(serviceResponse.getImageId());

            log.info("Biqt Response for Image Id: {}", responseImageId);
            BiqtResponse biqtResponse = this.biqtResponseRepository.findByImageId(responseImageId);

            if(biqtResponse == null){
                biqtResponse = new BiqtResponse();
            }
            
            biqtResponse.setIrisCenterX(serviceResponse.getIrisCenterX());
            biqtResponse.setIrisCenterY(serviceResponse.getIrisCenterY());
            biqtResponse.setQuality(serviceResponse.getQuality());
            biqtResponse.setPercentVisibleIris(serviceResponse.getPercentVisibleIris());
            biqtResponse.setIrisDiameter(serviceResponse.getIrisDiameter());
            biqtResponse.setStatus(ServiceCallStatus.Received);
            
            this.biqtResponseRepository.save(biqtResponse);
        } catch (JMSException | InvalidProtocolBufferException jmsException) {
            log.debug(jmsException.getMessage());

        }
    }
}
