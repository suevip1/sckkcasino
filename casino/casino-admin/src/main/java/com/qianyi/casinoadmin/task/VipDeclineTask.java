package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.business.UserLevelBusiness;
import com.qianyi.casinocore.model.UserLevelDecline;
import com.qianyi.casinocore.repository.UserLevelDeclineRepository;
import com.qianyi.casinocore.service.UserLevelService;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Transactional
public class VipDeclineTask {

    @Autowired
    private UserLevelBusiness userLevelBusiness;
    @Autowired
    private UserLevelDeclineRepository userLevelDeclineRepository;
    @Autowired
    private UserLevelService userLevelService;

//    @Scheduled(fixedRate = 6000 * 10 * 6)
    public void begin() {
        log.info(" Vip 升级开始 start=============================================》");
        try {
            Date startTime = new Date();
            Date endTime = new Date();
            startTime = cn.hutool.core.date.DateUtil.offsetMinute(startTime, -300);
            endTime = cn.hutool.core.date.DateUtil.offsetMinute(endTime, -1);
            List<Map<String, Object>> idList = userLevelService.findLastRiseUser(startTime, endTime);
            for (Map<String, Object> map : idList) {
                Long userId = Long.parseLong(map.get("userId").toString());
                Long id = Long.parseLong(map.get("id").toString());
                userLevelBusiness.processUserKeepLevel(userId);
                int row = queryToday(userId);
                if (row < 1) {
                    UserLevelDecline userLevelDecline = new UserLevelDecline();
                    userLevelDecline.setTodayDeclineStatus(1);
                    userLevelDecline.setUserId(userId);
                    userLevelDeclineRepository.save(userLevelDecline);
                }
            }
            log.info(" Vip 升级开始 结束end=============================================》");
        } catch (Exception ex) {
            log.error(" Vip 升级开始 失败=============================================》", ex);
        }
    }


    private int queryToday(Long userId) {
        String startTime = DateUtil.getStartTime(0);
        String endTime = DateUtil.getEndTime(0);
        return userLevelDeclineRepository.queryBonusAmount(userId, startTime, endTime);
    }

}