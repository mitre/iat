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
import org.mitre.iwp.buffers.IrisAnnotation.*;
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.model.IrisAnnotationResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.mitre.iwp.web.service.IrisAnnotationResponseRepository;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "irisAnnotationService", havingValue = "true")
public class IrisAnnotationReceiver {
    private static final Logger log = LoggerFactory.getLogger(IrisAnnotationReceiver.class);
    private final IrisAnnotationResponseRepository irisAnnotationResponseRepository;

    @Autowired
    public IrisAnnotationReceiver(
            IwpWebProperties iwpWebProperties,
            IrisAnnotationResponseRepository irisAnnotationResponseRepository){
        this.irisAnnotationResponseRepository = irisAnnotationResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getIrisAnnotationQueueResponse()}")
    public void processMessage(Object object) {
        try {
            BytesMessage bMessage = (BytesMessage) object;

            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);
            IrisAnnotationReply serviceResponse = IrisAnnotationReply.parseFrom(bytes);
            Long responseImageId = Long.parseLong(serviceResponse.getId());
            
            log.info("Iris Annotation Response for Image Id: {}", responseImageId);
            IrisAnnotationResponse irisAnnotationResponse = this.irisAnnotationResponseRepository.findByImageId(responseImageId);

            if(irisAnnotationResponse == null){
                irisAnnotationResponse = new IrisAnnotationResponse();
            }

            irisAnnotationResponse.setServiceResponse(bytes);
            irisAnnotationResponse.setStatus(ServiceCallStatus.Received);

            this.irisAnnotationResponseRepository.save(irisAnnotationResponse);
        } catch (JMSException | InvalidProtocolBufferException jmsException) {
            log.debug(jmsException.getMessage());
        }
    }
}
