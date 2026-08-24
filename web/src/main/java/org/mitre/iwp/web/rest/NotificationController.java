/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.rest;

import com.google.gson.Gson;

import org.mitre.iwp.web.dto.AppNotificationDTO;
import org.mitre.iwp.web.dto.UserMapper;
import org.mitre.iwp.web.model.AppNotification;
import org.mitre.iwp.web.service.AppNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final AppNotificationRepository appNotificationRepository;
    private final UserMapper userMapper;

    public NotificationController(AppNotificationRepository appNotificationRepository,
                                  UserMapper userMapper){
        this.appNotificationRepository = appNotificationRepository;
        this.userMapper = userMapper;
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/notification/all/{userId}")
    public ResponseEntity<String> findAllNotificationsForUser(@PathVariable("userId") long userId, Principal principal){
        logger.debug("/api/notification/all/{}", userId);
        Gson gson = new Gson();
        try {
            List<AppNotification> notList = this.appNotificationRepository.findByUserId(userId);
            return new ResponseEntity<>(gson.toJson(notList), HttpStatus.OK);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(gson.toJson(e.getMessage()));
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/notification/new/{userId}")
    public ResponseEntity<String> findAllNewNotificationsForUser(@PathVariable("userId") long userId, Principal principal){
        logger.debug("/api/notification/all/{}", userId);
        Gson gson = new Gson();
        try {
            List<AppNotification> notList = this.appNotificationRepository.findByUserIdAndIsNew(userId, true);
            return new ResponseEntity<>(gson.toJson(notList), HttpStatus.OK);
        } catch(Exception e){
            return ResponseEntity.badRequest().body(gson.toJson(e.getMessage()));
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/notification/markRead/{notificationId}")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable("notificationId") long notificationId, Principal principal){
        logger.info("/api/notification/markRead/{}", notificationId);
        Optional<AppNotification> value = this.appNotificationRepository.findById(notificationId);
        if(value.isPresent()) {
            AppNotification appNotification = value.get();
            appNotification.setIsNew(false);
            this.appNotificationRepository.save(appNotification);
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }

        return new ResponseEntity<>("Id Not Found", HttpStatus.NOT_FOUND);
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @PostMapping(value = "/api/notification/add")
    public ResponseEntity<String> addNotification(@RequestBody AppNotificationDTO appNotification, Principal principal){
        logger.info("/api/notification/add");
        Gson gson = new Gson();
        AppNotification tempAppNot = this.userMapper.toAppNotification(appNotification);
        try {
            tempAppNot = this.appNotificationRepository.save(tempAppNot);
            return new ResponseEntity<>(gson.toJson(tempAppNot.getId()), HttpStatus.OK);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(gson.toJson(e.getMessage()));
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/notification/delete/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable("notificationId") long notificationId, Principal principal){
        logger.info("/api/notification/delete/{}", notificationId);
        this.appNotificationRepository.deleteById(notificationId);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
