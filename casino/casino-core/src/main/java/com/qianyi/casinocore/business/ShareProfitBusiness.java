package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinocore.vo.ShareProfitVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ShareProfitBusiness {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;


    public void procerssShareProfit(ShareProfitMqVo shareProfitMqVo){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        GameRecord record = gameRecordService.findGameRecordById(shareProfitMqVo.getGameRecordId());
        List<ShareProfitBO> shareProfitBOList = shareProfitOperator(platformConfig,record);
        processShareProfitList(shareProfitBOList);
    }

    private List<ShareProfitBO> shareProfitOperator(PlatformConfig platformConfig, GameRecord record) {
        User user = userService.findById(record.getUserId());
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(user.getFirstPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getFirstPid(),new BigDecimal(record.getValidbet()),platformConfig.getFirstCommission(),getUserIsFirstBet(user),record.getBetTime(),true));
        if(user.getSecondPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getSecondPid(),new BigDecimal(record.getValidbet()),platformConfig.getSecondCommission(),getUserIsFirstBet(user),record.getBetTime(),false));
        if(user.getThirdPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getThirdPid(),new BigDecimal(record.getValidbet()),platformConfig.getThirdCommission(),getUserIsFirstBet(user),record.getBetTime(),false));
        return shareProfitBOList;
    }

    private ShareProfitBO getShareProfitBO(Long userId,BigDecimal betAmount,BigDecimal commission,Boolean isFirst,String betTime,boolean direct){
        ShareProfitBO shareProfitBO = new ShareProfitBO();
        shareProfitBO.setUserId(userId);
        shareProfitBO.setBetAmount(betAmount);
        shareProfitBO.setProfitAmount(betAmount.multiply(commission));
        shareProfitBO.setFirst(isFirst);
        shareProfitBO.setBetTime(betTime);
        shareProfitBO.setDirect(direct);
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(User user){
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no)
            return true;
        return false;
    }

    @Transactional
    protected void processShareProfitList(List<ShareProfitBO> shareProfitBOList){
        shareProfitBOList.forEach(item->processItem(item));
    }

    private void processItem(ShareProfitBO shareProfitBO){
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(shareProfitBO.getUserId());
        //进行分润
        userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
        userMoneyService.save(userMoney);
        //进行日报表处理
        proxyDayReportBusiness.processReport(shareProfitBO);
        //进行总报表处理
        proxyReportBusiness.processReport(shareProfitBO);
    }


    @Transactional
    public void shareProfit(PlatformConfig platformConfig, GameRecord record) {
        log.info("开始三级分润={}", record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
//        Long userId = record.getUserId();
        if (platformConfig == null) {
            updateShareProfitStatus(record);
            return;
        }
        User user = userService.findById(record.getUserId());
        if (user == null) {
            updateShareProfitStatus(record);
            return;
        }
        Long firstPid = user.getFirstPid();
        if (firstPid == null) {
            updateShareProfitStatus(record);
            return;
        }
        UserMoney firstUser = userMoneyService.findUserByUserIdUseLock(firstPid);
        if (firstUser == null) {
            updateShareProfitStatus(record);
            return;
        }
        BigDecimal firstRate = platformConfig.getFirstCommission() == null ? BigDecimal.ZERO : platformConfig.getFirstCommission();
        BigDecimal secondRate = platformConfig.getSecondCommission() == null ? BigDecimal.ZERO : platformConfig.getSecondCommission();
        BigDecimal thirdRate = platformConfig.getThirdCommission() == null ? BigDecimal.ZERO : platformConfig.getThirdCommission();
        ShareProfitVo shareProfitVo = new ShareProfitVo();
        //查询当前用户是否是首次下注
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no) {
            shareProfitVo.setIsFirst(true);
            user.setIsFirstBet(Constants.yes);
            userService.save(user);
        }
        //一级分润
        BigDecimal firstMoney = validbet.multiply(firstRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        firstMoney = firstMoney == null ? BigDecimal.ZERO : firstMoney;
        shareProfitVo.setFirstUserId(firstPid);
        shareProfitVo.setFirstMoney(firstMoney);
        if (firstMoney.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyService.addShareProfit(firstPid, firstMoney);
        }
        //二级分润
        Long secondPid = user.getSecondPid();
        UserMoney secondUser = null;
        if (secondPid != null) {
            secondUser = userMoneyService.findUserByUserIdUseLock(secondPid);
        }
        if (secondUser != null) {
            BigDecimal secondMoney = validbet.multiply(secondRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            secondMoney = secondMoney == null ? BigDecimal.ZERO : secondMoney;
            shareProfitVo.setSecondUserId(secondPid);
            shareProfitVo.setSecondMoney(secondMoney);
            if (secondMoney.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.addShareProfit(secondPid, secondMoney);
            }
        }
        //三级分润
        Long thirdPid = user.getThirdPid();
        UserMoney thirdUser = null;
        if (thirdPid != null) {
            thirdUser = userMoneyService.findUserByUserIdUseLock(thirdPid);
        }
        if (thirdUser != null) {
            BigDecimal thirdMoney = validbet.multiply(thirdRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            thirdMoney = thirdMoney == null ? BigDecimal.ZERO : thirdMoney;
            shareProfitVo.setThirdUserId(thirdPid);
            shareProfitVo.setThirdMoney(thirdMoney);
            if (thirdMoney.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.addShareProfit(thirdPid, thirdMoney);
            }
        }
        updateShareProfitStatus(record);
//        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.SHAREPROFIT_DIRECT, shareProfitVo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("分润消息发送成功={}", shareProfitVo);
    }

    /**
     * 更新分润状态
     *
     * @param record
     */
    @Transactional
    public void updateShareProfitStatus(GameRecord record) {
        record.setShareProfitStatus(Constants.yes);
        gameRecordService.save(record);
    }
}