package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.ProxyReportVo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/proxyReport")
@Api(tags = "人人代管理")
public class ProxyReportController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    public final static  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public final static  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @ApiOperation("查询人人代报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyReportVo> find(String userName, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(userName,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        User byAccount = userService.findByAccount(userName);
        if (LoginUtil.checkNull(byAccount)){
            return ResponseUtil.custom("找不到该会员");
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        List<ProxyReportVo> list = new LinkedList();
        Map<Long,BigDecimal> map = new HashMap<>();
        Map<Long,Integer> userMap = new HashMap<>();
        this.assemble(map,userMap,byAccount,byAccount.getId(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_0,CommonConst.NUMBER_0);
        List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, byAccount.getId());
        if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
            firstUsers.forEach(f ->{
                this.assemble(map,userMap,f,byAccount.getId(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_1,CommonConst.NUMBER_1);
            });
            List<User> secondPid = userService.findByStateAndSecondPid(Constants.open, byAccount.getId());
            if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                secondPid.forEach(s ->{
                    this.assemble(map,userMap,s,byAccount.getId(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_2,CommonConst.NUMBER_2);
                });
                List<User> thirdPid = userService.findByStateAndThirdPid(Constants.open, byAccount.getId());
                if (!LoginUtil.checkNull(thirdPid) && thirdPid.size() > CommonConst.NUMBER_0){
                    thirdPid.forEach(t ->{
                        this.assemble(map,userMap,t,byAccount.getId(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_3,CommonConst.NUMBER_3);
                    });
                }
            }
        }
        if (list.size() > CommonConst.NUMBER_0){
            List<Long> userIds = list.stream().map(ProxyReportVo::getFirstPid).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            list.stream().forEach(proxyReportVo ->{
                proxyReportVo.setAllPerformance(map.get(proxyReportVo.getUserId()).subtract(proxyReportVo.getPerformance()));
                proxyReportVo.setAllGroupNum(userMap.get(proxyReportVo.getUserId()));
                userList.stream().forEach(user->{
                    if (user.getId().equals(proxyReportVo.getFirstPid())){
                        proxyReportVo.setFirstPidAccount(user.getAccount());
                    }
                });
            });
            userIds.clear();
            userList.clear();
        }
        map.clear();
        userMap.clear();
        return this.getData(list);
    }
    @ApiOperation("下级明细")
    @GetMapping("/findDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
            @ApiImplicitParam(name = "tier", value = "当前层级 层级 0 当前 1 一级 2 二级 3 三级", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer tier){
        if (LoginUtil.checkNull(id,startDate,endDate,tier)){
            return ResponseUtil.custom("参数必填");
        }
        List<ProxyReportVo> list = new LinkedList();
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success(list);
        }
        Long userId = null;
        if (tier == CommonConst.NUMBER_1){
            userId = user.getFirstPid();
        }else if (tier == CommonConst.NUMBER_2){
            userId = user.getSecondPid();
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        Map<Long,BigDecimal> map = new HashMap<>();
        Map<Long,Integer> userMap = new HashMap<>();
        this.assemble(map,userMap,user,userId,startTime,endTime,list,startDate,endDate,tier,CommonConst.NUMBER_0);
        List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, user.getId());
        if (tier == CommonConst.NUMBER_1){
            if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                firstUsers.forEach(f ->{
                    this.assemble(map,userMap,f,user.getFirstPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_2,CommonConst.NUMBER_1);
                });
                List<User> secondPid = userService.findByStateAndSecondPid(Constants.open, user.getId());
                if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                    secondPid.forEach(s ->{
                        this.assemble(map,userMap,s,user.getFirstPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_3,CommonConst.NUMBER_2);
                    });
                }
            }
        }else if(tier == CommonConst.NUMBER_2){
            if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                firstUsers.forEach(f ->{
                    this.assemble(map,userMap,f,user.getSecondPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_3,CommonConst.NUMBER_1);
                });
            }
        }else {
            return ResponseUtil.custom("参数不合法");
        }
        if (list.size() > CommonConst.NUMBER_0){
            list.stream().forEach(proxyReportVo ->{
               proxyReportVo.setAllPerformance(map.get(proxyReportVo.getUserId()).subtract(proxyReportVo.getPerformance()));
               proxyReportVo.setAllGroupNum(userMap.get(proxyReportVo.getUserId()));
            });
         }
        map.clear();
        userMap.clear();
        return this.getData(list);
    }
    private ResponseEntity<ProxyReportVo> getData(List<ProxyReportVo> list){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setPerformance(list.stream().map(ProxyReportVo::getPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setContribution(list.stream().map(ProxyReportVo::getContribution).reduce(BigDecimal.ZERO, BigDecimal::add));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", list);
        jsonObject.put("sum", proxyReportVo);
        return ResponseUtil.success(jsonObject);
    }
    private ResponseEntity<ProxyReportVo> getDataDetail(List<ProxyReportVo> list){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setPerformance(list.stream().map(ProxyReportVo::getPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setContribution(list.stream().map(ProxyReportVo::getContribution).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setAllPerformance(list.stream().map(ProxyReportVo::getAllPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", list);
        jsonObject.put("sum", proxyReportVo);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDayDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
            @ApiImplicitParam(name = "tier", value = "当前层级 层级 0 当前 1 一级 2 二级 3 三级", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDayDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer tier){
        if (LoginUtil.checkNull(id,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        List<String> dateLists = this.findDates("D", startDate, endDate);
        List<ProxyReportVo> list = new LinkedList();
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success(list);
        }
        dateLists.forEach(date ->{
            List<BigDecimal> allPerformances = new ArrayList<>();
            ProxyReportVo proxyReportVo = null;
            try {
                if (tier == CommonConst.NUMBER_0){
                    proxyReportVo = this.assemble(id,id,date);
                    List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                        List<User> secondPid = userService.findByStateAndSecondPid(Constants.open,id);
                        if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                            secondPid.forEach(s ->{
                                this.assemble(s.getId(),date,allPerformances);
                            });
                            List<User> thirdPid = userService.findByStateAndThirdPid(Constants.open,id);
                            if (!LoginUtil.checkNull(thirdPid) && thirdPid.size() > CommonConst.NUMBER_0){
                                thirdPid.forEach(t ->{
                                    this.assemble(t.getId(),date,allPerformances);
                                });
                            }
                        }
                    }
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else if (tier == CommonConst.NUMBER_1){
                    proxyReportVo = this.assemble(id,user.getFirstPid(),date);
                    List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                        List<User> secondPid = userService.findByStateAndSecondPid(Constants.open,id);
                        if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                            secondPid.forEach(s ->{
                                this.assemble(s.getId(),date,allPerformances);
                            });
                        }
                    }
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else if (tier == CommonConst.NUMBER_2){
                    proxyReportVo = this.assemble(id,user.getSecondPid(),date);
                    List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                    }
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else {
                    proxyReportVo = this.assemble(id,user.getThirdPid(),date);
                }
                list.add(proxyReportVo);
            }catch (ParseException e){
                log.error("每日结算细节统计失败{}",e);
            }
        });
        return this.getDataDetail(list);
    }
    private void  assemble(Long userId,String date,List<BigDecimal> allPerformances){
        String startTime = date+start;
        String endTime  = date+end;
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(userId, startTime+start, endTime+end);
        allPerformances.add((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
    }
    private ProxyReportVo assemble(Long id,Long userId,String date) throws ParseException {
        String startTime = date+start;
        String endTime  = date+end;
        Date startDate = formatter.parse(startTime);
        Date endDate = formatter.parse(endTime);
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setStaticsTimes(date);
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(id, startTime, endTime);
        proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
//        proxyReportVo.setAllPerformance(proxyReportVo.getPerformance());
        List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(id,userId,startDate, endDate);
        if (shareProfitChanges == null || shareProfitChanges.size() == CommonConst.NUMBER_0){
            proxyReportVo.setCommission("0");
        }else {
            BigDecimal commission = shareProfitChanges.get(CommonConst.NUMBER_0).getProfitRate();
            commission = commission.setScale(CommonConst.NUMBER_3, RoundingMode.HALF_UP);
            proxyReportVo.setCommission(commission + "%");
        }
        BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        contribution = contribution.setScale(CommonConst.NUMBER_4, RoundingMode.HALF_UP);
        proxyReportVo.setContribution(contribution);
        return proxyReportVo;
    }

    private void assemble(Map<Long,BigDecimal> map,Map<Long,Integer> userMap,User user,Long userId,String startTime,String endTime,List<ProxyReportVo> list,
                          Date startDate,Date endDate,Integer tag,Integer level){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setTier(tag);
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(user.getId(), startTime+start, endTime+end);
        proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
        this.compute(map,user,level,proxyReportVo.getPerformance(),userMap);
        if (tag == CommonConst.NUMBER_0){
            proxyReportVo.setContribution(BigDecimal.ZERO);
        }else {
            List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(user.getId(),userId, startDate, endDate);
            BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            contribution = contribution.setScale(CommonConst.NUMBER_4, RoundingMode.HALF_UP);
            proxyReportVo.setContribution(contribution);
        }
        proxyReportVo.setUserId(user.getId());
        proxyReportVo.setAccount(user.getAccount());
        proxyReportVo.setFirstPid(user.getFirstPid());
        list.add(proxyReportVo);
    }

    private void compute(Map<Long,BigDecimal> map,User user,Integer tag,BigDecimal performance,Map<Long,Integer> userMap){
        if (tag == CommonConst.NUMBER_0){
            map.put(user.getId(),performance);
            userMap.put(user.getId(),CommonConst.NUMBER_0);
        }else if(tag == CommonConst.NUMBER_1){
            map.put(user.getId(),performance);
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid())));
            userMap.put(user.getId(),CommonConst.NUMBER_0);
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid()) + CommonConst.NUMBER_1);
        }else if(tag == CommonConst.NUMBER_2){
            map.put(user.getId(),performance);
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid())));
            map.put(user.getSecondPid(),performance.add(map.get(user.getSecondPid())));
            userMap.put(user.getId(),CommonConst.NUMBER_0);
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid()) + CommonConst.NUMBER_1);
            userMap.put(user.getSecondPid(),userMap.get(user.getSecondPid()) + CommonConst.NUMBER_1);
        }else {
            map.put(user.getId(),performance);
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid())));
            map.put(user.getSecondPid(),performance.add(map.get(user.getSecondPid())));
            map.put(user.getThirdPid(),performance.add(map.get(user.getThirdPid())));
            userMap.put(user.getId(),CommonConst.NUMBER_0);
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid()) + CommonConst.NUMBER_1);
            userMap.put(user.getSecondPid(),userMap.get(user.getSecondPid()) + CommonConst.NUMBER_1);
            userMap.put(user.getThirdPid(),userMap.get(user.getThirdPid()) + CommonConst.NUMBER_1);
        }

    }
    public static List<String> findDates(String dateType, Date dBegin, Date dEnd){
        List<String> listDate = new ArrayList<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (calEnd.after(calBegin)) {
            if (calEnd.after(calBegin))
                listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            else
                listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calEnd.getTime()));
            switch (dateType) {
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);break;
                case "N":
                    calBegin.add(Calendar.SECOND, 1);break;
            }
        }
        return listDate;
    }
}
