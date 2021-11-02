package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.DownloadStationService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("downloadStation")
@Api(tags = "应用版本控制")
public class DownloadStationController {

    @Autowired
    private DownloadStationService downloadStationService;
    @Autowired
    private PlatformConfigService platformConfigService;

    private final static Integer ANDROID_TERMINAL=1;
    private final static Integer IOS_TERMINAL=2;

    @GetMapping("getIosNewestVersion")
    @ApiOperation("ios最新版本检查")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "versionNumber", value = "版本号", required = true),
    })
    public ResponseEntity<List<DownloadStation>> getIosNewestVersion(String versionNumber) {
        boolean checkNull = CommonUtil.checkNull(versionNumber);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        List<DownloadStation> list = downloadStationService.findByterminalTypeAndVersionNumberGreaterThan(IOS_TERMINAL, versionNumber);
        return ResponseUtil.success(list);
    }

    @GetMapping("getAndroidNewestVersion")
    @ApiOperation("Android最新版本检查")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "versionNumber", value = "版本号", required = true),
    })
    public ResponseEntity<List<DownloadStation>> getAndroidNewestVersion(String versionNumber) {
        boolean checkNull = CommonUtil.checkNull(versionNumber);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        List<DownloadStation> list = downloadStationService.findByterminalTypeAndVersionNumberGreaterThan(ANDROID_TERMINAL, versionNumber);
        return ResponseUtil.success(list);
    }

    @GetMapping("getIosDownloadUrl")
    @ApiOperation("获取ios最新下载链接")
    @NoAuthentication
    public ResponseEntity<String> getIosDownloadUrl() {
        DownloadStation downloadStation = downloadStationService.getNewestVersion(IOS_TERMINAL);
        if (downloadStation == null) {
            return ResponseUtil.success();
        }
        return ResponseUtil.success(downloadStation.getDownloadUrl());
    }

    @GetMapping("getAndroidDownloadUrl")
    @ApiOperation("获取Android最新下载链接")
    @NoAuthentication
    public ResponseEntity<String> getAndroidDownloadUrl() {
        DownloadStation downloadStation = downloadStationService.getNewestVersion(ANDROID_TERMINAL);
        if (downloadStation == null) {
            return ResponseUtil.success();
        }
        return ResponseUtil.success(downloadStation.getDownloadUrl());
    }

    @GetMapping("getWebUrl")
    @ApiOperation("网页版链接")
    @NoAuthentication
    public ResponseEntity getWebUrl() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getDomainNameConfiguration())) {
            return ResponseUtil.custom("网站域名未配置,请联系客服");
        }
        String domain = platformConfig.getDomainNameConfiguration();
        return ResponseUtil.success(domain);
    }
}
