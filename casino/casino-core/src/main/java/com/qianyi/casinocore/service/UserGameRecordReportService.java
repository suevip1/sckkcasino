package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.UserGameRecordReport;
import com.qianyi.casinocore.repository.UserGameRecordReportRepository;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserGameRecordReportService {

    @Autowired
    private UserGameRecordReportRepository userGameRecordReportRepository;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Transactional
    public void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, BigDecimal betAmount, String platform) {
        userGameRecordReportRepository.updateKey(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            betAmount, platform);
    }

    public UserGameRecordReport save(UserGameRecordReport userGameRecordReport) {
        return userGameRecordReportRepository.save(userGameRecordReport);
    }

    public List<Map<String, Object>> sumUserRunningWater(String startTime, String endTime) {
        return userGameRecordReportRepository.sumUserRunningWater(startTime, endTime);
    }

    public BigDecimal sumUserRunningWaterByUserId(String startTime, String endTime, Long userId) {
        return userGameRecordReportRepository.sumUserRunningWaterByUserId(startTime, endTime, userId);
    }

    public Integer findTotalBetNumber(String startTime, String endTime) {
        return userGameRecordReportRepository.findTotalBetNumber(startTime, endTime);
    }

    @Transactional
    public void comparison(String dayTime) {// dayTime为一天yyyy-MM-dd
        String startTime = dayTime + start;
        String endTime = dayTime + end;
        Integer betNumber = userGameRecordReportRepository.findBetNumber(dayTime, dayTime);
        Integer totalBetNumber = this.findTotalBetNumber(startTime, endTime);
        if (betNumber.intValue() != totalBetNumber.intValue()) {
            log.error("会员报表日期{}不相等开始重新计算betNumber:{}totalBetNumber:{}", dayTime, betNumber, totalBetNumber);
            userGameRecordReportRepository.deleteByOrderTimes(dayTime);

            List<Map<String, Object>> wm = userGameRecordReportRepository.findWm(startTime, endTime);
            this.addData(wm, dayTime, Constants.PLATFORM_WM);

            List<Map<String, Object>> pg = userGameRecordReportRepository.findPg(startTime, endTime);
            this.addData(pg, dayTime, Constants.PLATFORM_PG);

            List<Map<String, Object>> sb = userGameRecordReportRepository.findSb(startTime, endTime);
            this.addData(sb, dayTime, Constants.PLATFORM_PG);

            List<Map<String, Object>> obdj = userGameRecordReportRepository.findObdj(startTime, endTime);
            this.addData(obdj, dayTime, Constants.PLATFORM_OBDJ);

            List<Map<String, Object>> obty = userGameRecordReportRepository.findObty(startTime, endTime);
            this.addData(obty, dayTime, Constants.PLATFORM_OBTY);
        }
    }

    private void addData(List<Map<String, Object>> listMap, String dayTime, String platform) {
        try {
            if (CollUtil.isNotEmpty(listMap)) {
                listMap.forEach(map -> {
                    UserGameRecordReport userGameRecordReport = new UserGameRecordReport();
                    if (platform.equals(Constants.PLATFORM_PG)) {
                        userGameRecordReport.setPlatform(map.get("vendor_code").toString());
                    } else {
                        userGameRecordReport.setPlatform(platform);
                    }
                    userGameRecordReport.setOrderTimes(dayTime);
                    userGameRecordReport.setUserId(Long.parseLong(map.get("user_id").toString()));
                    userGameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                    userGameRecordReport.setBetAmount(new BigDecimal(map.get("bet_amount").toString()));
                    userGameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                    userGameRecordReport.setWinLoss(new BigDecimal(map.get("win_loss").toString()));
                    Long userGameRecordReportId = CommonUtil.toHash(
                        dayTime + userGameRecordReport.getUserId().toString() + userGameRecordReport.getPlatform());
                    userGameRecordReport.setUserGameRecordReportId(userGameRecordReportId);
                    this.save(userGameRecordReport);
                });
                listMap.clear();
            }
        } catch (Exception ex) {
            log.error("会员报表计算失败日期{}Platform{}", dayTime, platform);
        }
    }
}
