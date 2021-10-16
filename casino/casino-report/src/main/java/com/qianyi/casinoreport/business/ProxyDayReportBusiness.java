package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.ShareProfitBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProxyDayReportBusiness {
    @Autowired
    private ProxyDayReportService proxyDayReportService;

    /**
     * 处理分润
     * @param shareProfitBO
     */
    public ProxyDayReport processReport(ShareProfitBO shareProfitBO){

        ProxyDayReport proxyDayReport = getProxyDayReport(shareProfitBO);
        proxyDayReport.setProfitAmount(proxyDayReport.getProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyDayReport.setBetAmount(proxyDayReport.getBetAmount().add(shareProfitBO.getBetAmount()));
        return proxyDayReport;
//        proxyDayReportService.save(proxyDayReport);

    }

    private ProxyDayReport getProxyDayReport(ShareProfitBO shareProfitBO) {
        String dayTime = shareProfitBO.getBetTime();
        ProxyDayReport proxyDayReport = proxyDayReportService.findByUserIdAndDay(shareProfitBO.getUserId(),dayTime);
        if(proxyDayReport == null)
            proxyDayReport = buildProxyDayReport(shareProfitBO.getUserId(),dayTime);
        return proxyDayReport;
    }

    private ProxyDayReport buildProxyDayReport(Long userId,String dayTime) {
        ProxyDayReport proxyDayReport = new ProxyDayReport();
        proxyDayReport.setUserId(userId);
        proxyDayReport.setDayTime(dayTime);
        return proxyDayReport;
    }

    /**
     * 处理充值
     * @param rechargeProxy
     */
    public ProxyDayReport processChargeAmount(RechargeProxyBO rechargeProxy){
        log.info("process single charge amount {}",rechargeProxy);
        ProxyDayReport proxyDayReport = getProxyDayReport(rechargeProxy);
        proxyDayReport.setDeppositeAmount(proxyDayReport.getDeppositeAmount().add(rechargeProxy.getAmount()));
        return proxyDayReport;
//        proxyDayReportService.save(proxyDayReport);
    }

    private ProxyDayReport getProxyDayReport(RechargeProxyBO rechargeProxy) {
        ProxyDayReport proxyDayReport = proxyDayReportService.findByUserIdAndDay(rechargeProxy.getProxyUserId(),rechargeProxy.getDayTime());
        if(proxyDayReport == null)
            proxyDayReport = buildProxyDayReport(rechargeProxy.getProxyUserId(),rechargeProxy.getDayTime());
        return proxyDayReport;
    }

    /**
     * 处理团队用户数量
     * @param proxyUserBO
     */
    public ProxyDayReport processUser(ProxyUserBO proxyUserBO){
        ProxyDayReport proxyDayReport = getProxyDayReport(proxyUserBO.getProxyUserId(),proxyUserBO.getDayTime());
        proxyDayReport.setNewNum(proxyDayReport.getNewNum()+1);
        return proxyDayReport;
//        proxyDayReportService.save(proxyDayReport);
    }

    private ProxyDayReport getProxyDayReport(Long userId,String dayTime) {
        ProxyDayReport proxyDayReport = proxyDayReportService.findByUserIdAndDay(userId,dayTime);
        if(proxyDayReport == null)
            proxyDayReport = buildProxyDayReport(userId,dayTime);
        return proxyDayReport;
    }
}