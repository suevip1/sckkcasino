package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinoweb.job.GameRecordAsyncOper;
import com.qianyi.casinoweb.job.GameRecordJob;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(tags = "手动补单")
@RestController
@RequestMapping("/supplement")
@Slf4j
public class SupplementController {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private GameRecordJob gameRecordJob;

    @GetMapping("/wm")
    @ApiOperation("WM平台手动补单")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间,格式为:yyyyMMddHHmmss", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间,格式为:yyyyMMddHHmmss", required = true),
            @ApiImplicitParam(name = "secretkey", value = "秘钥", required = true),
    })
    public ResponseEntity wmSupplement(String secretkey, String startTime, String endTime) {
        boolean checkNull = CasinoWebUtil.checkNull(startTime, endTime);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!Constants.PLATFORM_WM_BIG.equals(secretkey)) {
            return ResponseUtil.custom("秘钥错误");
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        df.setLenient(false);//表示严格验证
        try {
            long startTimeNum = df.parse(startTime).getTime();
            long endTimeNum = df.parse(endTime).getTime();
            if (endTimeNum < startTimeNum) {
                return ResponseUtil.custom("开始时间不能大于结束时间");
            }
            long diff = new Date().getTime() - startTimeNum;
            //WM报表资料只保留60天。
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 60) {
                return ResponseUtil.custom("仅允许补单60天内的数据");
            }
        } catch (ParseException e) {
            return ResponseUtil.custom("startTime或endTime时间格式填写错误,,格式为:yyyyMMddHHmmss");
        }
        List<String> list = new ArrayList<>();
        //第三方要求拉取数据时间范围不能大于1天，大于1天，取开始时间的后一天为结束时间
        getDateTimeReport(list, startTime, endTime, df);
        return ResponseUtil.success(list);
    }

    private List<String> getDateTimeReport(List<String> list, String startTime, String endTime, SimpleDateFormat df) {
        String timeMsg = null;
        String endTimeNew = null;
        try {
            Date startDate = df.parse(startTime);
            Date endDate = df.parse(endTime);
            long startTimeNum = startDate.getTime();
            long endTimeNum = endDate.getTime();
            if ((endTimeNum - startTimeNum) > 60 * 60 * 24 * 1000) {
                Calendar after = Calendar.getInstance();
                after.setTime(startDate);
                after.add(Calendar.DAY_OF_MONTH, 1);
                Date afterDate = after.getTime();
                endTimeNew = df.format(afterDate);
            } else {
                endTimeNew = endTime;
            }
            timeMsg = startTime + "到" + endTimeNew;
            log.info("开始拉取{}的注单记录", timeMsg);
            String result = wmApi.getDateTimeReport(null, startTime, endTimeNew, 0, 1, 2, null, null);
            //远程请求异常
            if (ObjectUtils.isEmpty(result)) {
                log.error("{}游戏记录拉取失败,远程请求异常", timeMsg);
                list.add(timeMsg + "游戏记录拉取失败,远程请求异常");
            } else if ("notData".equals(result)) { //查询结果无记录
                log.info("{}时间范围无记录", timeMsg);
                list.add(timeMsg + "时间范围无记录");
            } else {
                List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
                if (!CollectionUtils.isEmpty(gameRecords)) {
                    gameRecordJob.saveAll(gameRecords);
                    log.info("{}游戏记录补单完成", timeMsg);
                    list.add(timeMsg + "游戏记录补单完成");
                }
            }
        } catch (Exception e) {
            log.error("{}游戏记录拉取异常", timeMsg);
            list.add(timeMsg + "游戏记录拉取异常");
            e.printStackTrace();
        }
        if (!endTime.equals(endTimeNew)) {
            try {
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDateTimeReport(list, endTimeNew, endTime, df);
        }
        return list;
    }
}