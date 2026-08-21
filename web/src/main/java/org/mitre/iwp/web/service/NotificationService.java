/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.service;

import org.apache.commons.collections4.IterableUtils;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.AppNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final AppNotificationRepository repository;

    private static final String idEmptyMessage = "UserNotification Id is empty";
    private static final String notificationNotFoundMessage = "No notification found with id '%s'";

    public NotificationService(AppNotificationRepository repository) {
        this.repository = repository;
    }

    public List<AppNotification> getNotificationsForUser(long userId) {
        return repository.findByUserId(userId);
    }

    public Long getNotificationCountForUser(long userId) {
        return repository.countByUserId(userId);
    }

    /***
     *
     * @param id
     * @return individual notification
     */
    public AppNotification getNotification(long id) {
        Optional<AppNotification> userOption = repository.findById(id);
        if(userOption.isPresent()) {
            return userOption.get();
        }

        return null;
    }

    /***
     * Adds a notification to the database
     * 
     * @param userId
     * @param message
     * @return
     * @throws InvalidMessageException
     */
    public Long createUserNotification(long userId, String message) throws InvalidMessageException {
        AppNotification appNotification = new AppNotification();
        appNotification.setUserId(userId);
        appNotification.setMessage(message);
        appNotification.setIsNew(true);
        appNotification.setDateCreated(Instant.now().toString());
        appNotification.setIcon("notifications");
        return this.createUserNotification(appNotification);
    }

    /***
     * Adds a notification to the database
     * 
     * @param notification
     * @return
     * @throws InvalidMessageException
     */
    public Long createUserNotification(AppNotification notification) throws InvalidMessageException {
        if (notification.getUserId() == null) {
            throw new InvalidMessageException(String.format("UserNotification %s missing user ", notification.getId()));
        }

        repository.save(notification);
        return notification.getId();
    }

    /***
     * Gets a list of all the notifications in the system
     * 
     * @return
     */
    public List<AppNotification> listUserNotifications() {
        return IterableUtils.toList(repository.findAll());
    }

    /***
     * Updates the notification
     * 
     * @param notification
     * @return
     * @throws InvalidMessageException
     */
    public void updateUserNotification(AppNotification notification) throws InvalidMessageException {
        // Ensure notification id exists in system
        if (notification.getId() == -1) {
            throw new InvalidMessageException(idEmptyMessage);
        }

        repository.save(notification);
    }

    /***
     *
     * @param notificationId
     * @param status         NotificationStatus
     * @return
     * @throws InvalidMessageException
     */
    public void setStatusUserNotification(long notificationId, String status) throws InvalidMessageException {
        // Ensure notification id exists in system
        if (notificationId == -1) {
            throw new InvalidMessageException(idEmptyMessage);
        }
        AppNotification not = repository.findById(notificationId)
                .orElseThrow(() -> new InvalidMessageException(String.format(notificationNotFoundMessage, notificationId)));

        not.setIsNew(false);
        repository.save(not);
    }

    /***
     * Deletes a notification
     * 
     * @param notificationId
     * @return
     * @throws InvalidMessageException
     */
    public boolean deleteUserNotification(long notificationId) throws InvalidMessageException {
        logger.debug("Delete UserNotification id:{} ", notificationId);
        // Ensure notification id exists in system
        if (notificationId == -1) {
            throw new InvalidMessageException(idEmptyMessage);
        }

        if (repository.existsById(notificationId)) {
            repository.deleteById(notificationId);
            return true;
        }

        throw new InvalidMessageException(String.format(notificationNotFoundMessage, notificationId));
    }

}
