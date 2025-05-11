package com.notificationapi.controller;

import com.notification.common.dto.GcmTokenRequest;
import com.notification.common.dto.MPResponse;
import com.notification.common.dto.NotificationSendRequest;
import com.notification.constants.RequestURI;
import com.notificationbackend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @RequestMapping(method = RequestMethod.POST, value = RequestURI.UPDATE_USER_GCM_TOKEN)
    @ResponseBody
    ResponseEntity<MPResponse> updateUserGcmToken(@Valid @RequestBody GcmTokenRequest request) throws Exception {
        MPResponse response = notificationService.updateUserGcmToken(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestURI.SEND_NOTIFICATION)
    @ResponseBody
    ResponseEntity<MPResponse> sendNotification(@Valid @RequestBody NotificationSendRequest request) throws Exception {
        MPResponse response = notificationService.sendNotification(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
