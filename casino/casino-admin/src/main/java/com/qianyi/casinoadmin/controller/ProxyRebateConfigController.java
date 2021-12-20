package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/proxyRebateConfig")
@Api(tags = "代理中心")
public class ProxyRebateConfigController {
    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private ProxyUserService proxyUserService;

    @ApiOperation("查询代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前详情页面代理id", required = true),
    })
    @GetMapping("/findAll")
    public ResponseEntity findAll(Long id){
        ProxyUser proxyUser = proxyUserService.findById(id);
        if (LoginUtil.checkNull(proxyUser)){
            return ResponseUtil.custom("代理不存在");
        }
        Integer tag = CommonConst.NUMBER_1;
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findById(proxyUser.getFirstProxy());
        JSONObject jsonObject = new JSONObject();
        if (!LoginUtil.checkNull(proxyRebateConfig)){
            jsonObject.put("data", proxyRebateConfig);
            jsonObject.put("tag", tag);
            return ResponseUtil.success(jsonObject);
        }
        RebateConfig rebateConfig = rebateConfigService.findFirst();
        tag = CommonConst.NUMBER_0;
        jsonObject.put("data", rebateConfig);
        jsonObject.put("tag", tag);
        return ResponseUtil.success(jsonObject);
    }

    @NoAuthorization
    @ApiOperation("编辑代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxyRebateConfig", value = "返佣等级对象", required = false),
            @ApiImplicitParam(name = "tag", value = "0 启用全局 1 启用个人", required = true),
            @ApiImplicitParam(name = "proxyUserId", value = "当前详情页面代理id", required = true),
    })
    @PostMapping("/updateProxyRebate")
    public ResponseEntity updateRegisterSwitch(ProxyRebateConfig proxyRebateConfig,Long proxyUserId,Integer tag){
        if (DateUtil.verifyTime()){
            return ResponseUtil.custom("0点到1点不能修改该配置");
        }
        if (LoginUtil.checkNull(tag,proxyUserId)){
            return ResponseUtil.custom("参数必填");
        }
        ProxyUser proxyUser = proxyUserService.findById(proxyUserId);
        if (LoginUtil.checkNull(proxyUser)){
            return ResponseUtil.custom("代理不存在");
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_1){
            return ResponseUtil.custom("只能设置总代");
        }
        ProxyRebateConfig byId = proxyRebateConfigService.findById(proxyUser.getId());
        if (tag == CommonConst.NUMBER_0){
            if (!LoginUtil.checkNull(byId)){
                proxyRebateConfigService.delete(byId.getProxyUserId(),byId);
            }
            return ResponseUtil.success();
        }else if (tag == CommonConst.NUMBER_1){
            if (LoginUtil.checkNull(proxyRebateConfig)){
                return ResponseUtil.custom("参数不合法");
            }
            if (this.check(proxyRebateConfig)){
                return ResponseUtil.custom("参数必填");
            }
            if (this.verify(proxyRebateConfig)){
                return ResponseUtil.custom("返佣不能大于30块");
            }
            if (this.verifySize(proxyRebateConfig)){
                return ResponseUtil.custom("低级别值不能大于高级别");
            }
            if (this.verifyAmountLineSize(proxyRebateConfig)){
                return ResponseUtil.custom("低级别值不能大于高级别");
            }
            if (!LoginUtil.checkNull(byId)){
                proxyRebateConfig.setId(byId.getId());
            }
            proxyRebateConfigService.save(proxyRebateConfig);
            return ResponseUtil.success();
        }
        return ResponseUtil.custom("参数不合法");
    }
    private Boolean check(ProxyRebateConfig proxyRebateConfig){
        if (LoginUtil.checkNull(proxyRebateConfig.getFirstMoney()) || LoginUtil.checkNull(proxyRebateConfig.getFirstProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getSecondMoney()) || LoginUtil.checkNull(proxyRebateConfig.getSecondProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getThirdMoney()) || LoginUtil.checkNull(proxyRebateConfig.getThirdProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getFourMoney()) || LoginUtil.checkNull(proxyRebateConfig.getFourProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getFiveMoney()) || LoginUtil.checkNull(proxyRebateConfig.getFiveProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getSixMoney()) || LoginUtil.checkNull(proxyRebateConfig.getSixProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getSevenMoney()) || LoginUtil.checkNull(proxyRebateConfig.getSevenProfit())){
            return true;
        }
        if (LoginUtil.checkNull(proxyRebateConfig.getEightMoney()) || LoginUtil.checkNull(proxyRebateConfig.getEightProfit())){
            return true;
        }
        return false;
    }
    private Boolean verify(ProxyRebateConfig proxyRebateConfig){
        if (!LoginUtil.checkNull(proxyRebateConfig.getFirstProfit()) && proxyRebateConfig.getFirstProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getSecondProfit()) && proxyRebateConfig.getSecondProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getThirdProfit()) && proxyRebateConfig.getThirdProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getFourProfit()) && proxyRebateConfig.getFourProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getFiveProfit()) && proxyRebateConfig.getFiveProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getSixProfit()) && proxyRebateConfig.getSixProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getSevenProfit()) && proxyRebateConfig.getSevenProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(proxyRebateConfig.getEightProfit()) && proxyRebateConfig.getEightProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        return false;
    }
    private Boolean verifySize(ProxyRebateConfig rebateConfig){
        if (rebateConfig.getFirstProfit().compareTo(rebateConfig.getSecondProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFirstMoney() >= rebateConfig.getSecondMoney() ){
            return true;
        }
        if (rebateConfig.getSecondProfit().compareTo(rebateConfig.getThirdProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSecondMoney() >= rebateConfig.getThirdMoney() ){
            return true;
        }
        if (rebateConfig.getThirdProfit().compareTo(rebateConfig.getFourProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getThirdMoney() >= rebateConfig.getFourMoney() ){
            return true;
        }
        if (rebateConfig.getFourProfit().compareTo(rebateConfig.getFiveProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFourMoney() >= rebateConfig.getFiveMoney() ){
            return true;
        }
        if (rebateConfig.getFiveProfit().compareTo(rebateConfig.getSixProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFiveMoney() >= rebateConfig.getSixMoney() ){
            return true;
        }
        if (rebateConfig.getSixProfit().compareTo(rebateConfig.getSevenProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSixMoney() >= rebateConfig.getSevenMoney() ){
            return true;
        }
        if (rebateConfig.getSevenProfit().compareTo(rebateConfig.getEightProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSevenMoney() >= rebateConfig.getEightMoney() ){
            return true;
        }
        return false;
    }


    private Boolean verifyAmountLineSize(ProxyRebateConfig rebateConfig){
        if (rebateConfig.getFirstProfit().compareTo(rebateConfig.getSecondProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getFirstAmountLine().compareTo(rebateConfig.getSecondAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getSecondProfit().compareTo(rebateConfig.getThirdProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getSecondAmountLine().compareTo(rebateConfig.getThirdAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getThirdProfit().compareTo(rebateConfig.getFourProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getThirdAmountLine().compareTo(rebateConfig.getFourAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getFourProfit().compareTo(rebateConfig.getFiveProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getFourAmountLine().compareTo(rebateConfig.getFiveAmountLine())>=CommonConst.NUMBER_0 ){
            return true;
        }
        if (rebateConfig.getFiveProfit().compareTo(rebateConfig.getSixProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getFiveAmountLine().compareTo(rebateConfig.getSixAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getSixProfit().compareTo(rebateConfig.getSevenProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getSixAmountLine().compareTo(rebateConfig.getSevenAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getSevenProfit().compareTo(rebateConfig.getEightProfit()) >= CommonConst.NUMBER_0 ||
                rebateConfig.getSevenAmountLine().compareTo(rebateConfig.getEightAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        return false;
    }
}
