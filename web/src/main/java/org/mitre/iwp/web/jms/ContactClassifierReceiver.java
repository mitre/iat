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
import org.mitre.iwp.buffers.ContactClassifier;
import org.mitre.iwp.web.model.ContactClassifierResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.mitre.iwp.web.service.ContactClassifierResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "contactService", havingValue = "true")
public class ContactClassifierReceiver {
    private static final Logger log = LoggerFactory.getLogger(ContactClassifierReceiver.class);
    private final ContactClassifierResponseRepository contactClassifierResponseRepository;

    @Autowired
    public ContactClassifierReceiver(ContactClassifierResponseRepository contactClassifierResponseRepository) {
        this.contactClassifierResponseRepository = contactClassifierResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getContactQueueResponse()}")
    public void processMessage(Object object) {
        try {
            BytesMessage bMessage = (BytesMessage) object;

            byte[] bytes = new byte[(int) bMessage.getBodyLength()];
            bMessage.readBytes(bytes);
            ContactClassifier.ContactClassifierResponse serviceResponse = ContactClassifier.ContactClassifierResponse.parseFrom(bytes);
            Long responseImageId = Long.parseLong(serviceResponse.getImageId());

            log.info("ContactClassifier Response for Image Id: {}", responseImageId);
            ContactClassifierResponse contactResponse = this.contactClassifierResponseRepository.findByImageId(responseImageId);

            if(contactResponse == null){
                contactResponse = new ContactClassifierResponse();
            }

            contactResponse.setDetectionCode(serviceResponse.getDetectionCode());
            contactResponse.setProcessTimeMS(serviceResponse.getProcessTimeMS());
            contactResponse.setStatus(ServiceCallStatus.Received);

            this.contactClassifierResponseRepository.save(contactResponse);
        } catch (JMSException | InvalidProtocolBufferException jmsException) {
            log.debug(jmsException.getMessage());

        }
    }
}
