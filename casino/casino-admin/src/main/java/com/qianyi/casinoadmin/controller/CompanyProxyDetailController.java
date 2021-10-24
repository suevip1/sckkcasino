package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyProxyDetailService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PageUtil;
import com.qianyi.casinocore.vo.CompanyProxyDetailVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private CompanyProxyDetailService companyProxyDetailService;

    @Autowired
    private ProxyUserService proxyUserService;
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetailVo> find(Integer pageSize, Integer pageCode, Integer proxyRole, Integer tag, String userName,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        List<CompanyProxyDetailVo> companyProxyDetailVos = new LinkedList<>();
        if (!LoginUtil.checkNull(userName)){
            if (LoginUtil.checkNull(tag)){
                return ResponseUtil.custom("参数不合法");
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_1){//包含下级
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    companyProxyDetail.setFirstProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.allCompanyProxyDetail(companyProxyDetails,companyProxyDetailVos);
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    companyProxyDetail.setSecondProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.secondCompanyProxyDetail(companyProxyDetails,companyProxyDetailVos);
                }else {
                    companyProxyDetail.setThirdProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.thirdCompanyProxyDetail(companyProxyDetails,companyProxyDetailVos);
                }
            }else {
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    companyProxyDetail.setFirstProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.assemble(companyProxyDetails,companyProxyDetailVos,byUserName.getId(),CommonConst.NUMBER_1);
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    companyProxyDetail.setSecondProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.assemble(companyProxyDetails,companyProxyDetailVos,byUserName.getId(),CommonConst.NUMBER_2);
                }else {
                    companyProxyDetail.setThirdProxy(byUserName.getId());
                    List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
                    this.assemble(companyProxyDetails,companyProxyDetailVos,byUserName.getId(),CommonConst.NUMBER_3);
                }
            }
        }else {
            List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
            this.allCompanyProxyDetail(companyProxyDetails,companyProxyDetailVos);
        }
        PageVo pageVO = new PageVo(pageCode,pageSize);
        PageResultVO<CompanyProxyDetailVo> pageResultVO;
        if (!LoginUtil.checkNull(proxyRole)){
            List<CompanyProxyDetailVo> companyProxyDetailVoList = companyProxyDetailVos.stream().filter(item -> item.getProxyRole().equals(proxyRole)).collect(Collectors.toList());
            pageResultVO = (PageResultVO<CompanyProxyDetailVo>) PageUtil.handlePageResult(companyProxyDetailVoList, pageVO);
        }else {
            pageResultVO = (PageResultVO<CompanyProxyDetailVo>) PageUtil.handlePageResult(companyProxyDetailVos, pageVO);
        }
        List<CompanyProxyDetailVo> content = (List<CompanyProxyDetailVo>) pageResultVO.getContent();
        if (content.size() > CommonConst.NUMBER_0){
            List<Long> proxyUserIds = content.stream().map(CompanyProxyDetailVo::getProxyUserId).collect(Collectors.toList());
            List<ProxyUser> proxyUser = proxyUserService.findProxyUser(proxyUserIds);
            if(proxyUser != null){
                content.stream().forEach(proxyDetail ->{
                    proxyUser.stream().forEach(proxy->{
                        if (proxy.getId().equals(proxyDetail.getProxyUserId())){
                            proxyDetail.setUserName(proxy.getUserName());
                            proxyDetail.setNickName(proxy.getNickName());
                        }
                    });
                });
            }
        }
        return ResponseUtil.success(pageResultVO);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前id", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetail> findDailyDetails(Long id,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byUserName = proxyUserService.findById(id);
        List<CompanyProxyDetail> companyProxyDetailList = new LinkedList<>();
        if (LoginUtil.checkNull(byUserName)){
            return ResponseUtil.success(companyProxyDetailList);
        }
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
            companyProxyDetail.setFirstProxy(byUserName.getId());
            List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
            this.assemble(companyProxyDetails,companyProxyDetailList);
        }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
            companyProxyDetail.setSecondProxy(byUserName.getId());
            List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
            this.assemble(companyProxyDetails,companyProxyDetailList);
        }else {
            companyProxyDetail.setThirdProxy(byUserName.getId());
            companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate);
        }

        return ResponseUtil.success(companyProxyDetailList);
    }
    private void thirdCompanyProxyDetail(List<CompanyProxyDetail> companyProxyDetails,List<CompanyProxyDetailVo> companyProxyDetailVos){
        Map<Long, List<CompanyProxyDetail>> thirdMap = companyProxyDetails.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getThirdProxy));
        thirdMap.forEach((thirdKey, thirdValue)->{
            List<CompanyProxyDetail> thirdList = thirdValue;
            this.assemble(thirdList,companyProxyDetailVos,thirdKey,CommonConst.NUMBER_3);
        });
    }
    private void secondCompanyProxyDetail(List<CompanyProxyDetail> companyProxyDetails,List<CompanyProxyDetailVo> companyProxyDetailVos){
        Map<Long, List<CompanyProxyDetail>> secondMap = companyProxyDetails.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getSecondProxy));
        secondMap.forEach((secondKey, secondValue)->{
            List<CompanyProxyDetail> secondList = secondValue;
            this.assemble(secondList,companyProxyDetailVos,secondKey,CommonConst.NUMBER_2);
            this.thirdCompanyProxyDetail(secondList,companyProxyDetailVos);
        });
    }
    private void allCompanyProxyDetail(List<CompanyProxyDetail> companyProxyDetails,List<CompanyProxyDetailVo> companyProxyDetailVos){
        Map<Long, List<CompanyProxyDetail>> firstMap = companyProxyDetails.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getFirstProxy));
        firstMap.forEach((firstKey, firstValue) -> {
            List<CompanyProxyDetail> firstList = firstValue;
            this.assemble(firstList,companyProxyDetailVos,firstKey,CommonConst.NUMBER_1);
            this.secondCompanyProxyDetail(firstList,companyProxyDetailVos);
        });
    }
    private void assemble(List<CompanyProxyDetail> list,List<CompanyProxyDetailVo> companyProxyDetailVos,Long proxyId,Integer proxyRole){
        CompanyProxyDetailVo vo = new CompanyProxyDetailVo();
        BigDecimal groupBetAmount = list.stream().map(CompanyProxyDetail::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal groupTotalprofit = list.stream().map(CompanyProxyDetail::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = list.stream().map(CompanyProxyDetail::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGroupBetAmount(groupBetAmount);
        vo.setGroupTotalprofit(groupTotalprofit);
        vo.setProfitAmount(profitAmount);
        vo.setProxyUserId(proxyId);
        vo.setProxyRole(proxyRole);
        companyProxyDetailVos.add(vo);
    }
    private void assemble(List<CompanyProxyDetail> list,List<CompanyProxyDetail> companyProxyDetailList){
        Map<String, List<CompanyProxyDetail>> collect = list.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getStaticsTimes));
        collect.forEach((firstKey, firstValue) -> {
            List<CompanyProxyDetail> firstList = firstValue;
            CompanyProxyDetail vo = new CompanyProxyDetail();
            BigDecimal groupBetAmount = firstList.stream().map(CompanyProxyDetail::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal groupTotalprofit = firstList.stream().map(CompanyProxyDetail::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal profitAmount = firstList.stream().map(CompanyProxyDetail::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer playerNum = firstList.stream().mapToInt(CompanyProxyDetail::getPlayerNum).sum();
            vo.setStaticsTimes(firstKey);
            CompanyProxyDetail companyProxyDetail = firstList.get(CommonConst.NUMBER_0);
            vo.setBenefitRate(companyProxyDetail.getBenefitRate());
            vo.setProfitRate(companyProxyDetail.getProfitRate());
            vo.setProfitLevel(companyProxyDetail.getProfitLevel());
            vo.setPlayerNum(playerNum);
            vo.setGroupBetAmount(groupBetAmount);
            vo.setGroupTotalprofit(groupTotalprofit);
            vo.setProfitAmount(profitAmount);
            companyProxyDetailList.add(vo);
        });
    }
}
