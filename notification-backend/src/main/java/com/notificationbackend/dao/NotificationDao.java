package com.notificationbackend.dao;

import com.notification.common.db.dao.CommonDao;
import com.notification.common.db.service.CommonDbService;
import com.notificationbackend.model.UserFcmTokenMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class NotificationDao {

    @Autowired
    CommonDbService commonDbService;

    @Autowired
    CommonDao commonDao;

    Logger logger = LogManager.getLogger(NotificationDao.class);

    public UserFcmTokenMapping getUserFcmTokenMappingFromUserId(String userId) throws Exception {
        String query = "select fcmt from UserFcmTokenMapping fcmt where fcmt.userId = :user_id";
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("user_id", userId);
        return commonDbService.selectEntityByCriteria(UserFcmTokenMapping.class, query, queryMap);
    }
}
