package com.qianyi.casinoadmin.controller;

import cn.hutool.core.date.DateUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.util.ExcelUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PersonReportTotalVo;
import com.qianyi.casinocore.vo.PersonReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "报表中心")
@Slf4j
@RestController
@RequestMapping("report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    // @NoAuthorization
    @ApiOperation("查询个人报表")
    @GetMapping("/queryPersonReport")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "time", value = "1 北京时间 2 美东时间", required = false),
        @ApiImplicitParam(name = "tag", value = "1：投注笔数 2：投注金额 3：有效投注 4：洗码发放 5：用户输赢金额", required = false),})
    public ResponseEntity<PersonReportVo> queryPersonReport(Integer pageSize, Integer pageCode, String userName,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime, String platform, Integer sort, Integer time,
        Integer tag) {
        if (LoginUtil.checkNull(startTime, endTime, pageSize, pageCode)) {
            return ResponseUtil.custom("参数不合法");
        }
        String orderTimeStart = "'" + DateUtil.formatDate(startTime) + "'";
        String orderTimeEnd = "'" + DateUtil.formatDate(endTime) + "'";
        if (LoginUtil.checkNull(time)) {
            // 向后偏移12小时
            if (LoginUtil.checkNull(platform) || platform.equals("WM")) {
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        } else {
            if (time == CommonConst.NUMBER_2) {
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        }
        String startTimeStr = DateUtil.formatDateTime(startTime);
        String endTimeStr = DateUtil.formatDateTime(endTime);
        if (StringUtils.hasLength(userName)) {
            User user = userService.findByAccount(userName);
            if (user != null) {
                List<PersonReportVo> reportResult = userService.findMap(platform, startTimeStr, endTimeStr,
                    user.getId(), orderTimeStart, orderTimeEnd, "");
                PageResultVO<PersonReportVo> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
                return ResponseUtil.success(getMap(mapPageResultVO));
            }
            List<PersonReportVo> emptyResult = new ArrayList<>();
            return ResponseUtil.success(combinePage(emptyResult, 0, pageCode, pageSize));
        }
        int page = (pageCode - 1) * pageSize;
        List<PersonReportVo> reportResult = null;
        try {
            String statement = getOrderByStatement(tag, sort);
            reportResult = userService.findMap(platform, startTimeStr, endTimeStr, page, pageSize, statement,
                orderTimeStart, orderTimeEnd, "");
        } catch (Exception e) {
            return ResponseUtil.custom("查询失败");
        }
        int totalElement = reportService.queryTotalElement(startTimeStr, endTimeStr);
        PageResultVO<PersonReportVo> mapPageResultVO = combinePage(reportResult, totalElement, pageCode, pageSize);
        return ResponseUtil.success(getMap(mapPageResultVO));
    }

    // 获取 order by 语句
    private String getOrderByStatement(Integer tag, Integer sort) {
        if (LoginUtil.checkNull(tag)) {
            return "";
        }
        String str = "ORDER BY {0} ";
        switch (tag) {
            case 1:
                str = MessageFormat.format(str, "num");
                break;
            case 2:
                str = MessageFormat.format(str, "bet_amount");
                break;
            case 3:
                str = MessageFormat.format(str, "validbet");
                break;
            case 4:
                str = MessageFormat.format(str, "wash_amount");
                break;
            case 5:
                str = MessageFormat.format(str, "win_loss");
                break;
            default:
                throw new BusinessException("参数不合法");
        }
        if (LoginUtil.checkNull(sort) || sort == CommonConst.NUMBER_1) {
            str = str + "ASC";
        } else {
            str = str + "DESC";
        }
        return str;
    }

    private PageResultVO<PersonReportVo> getMap(PageResultVO<PersonReportVo> mapPageResultVO) {
        List<PersonReportVo> content = (List<PersonReportVo>)mapPageResultVO.getContent();
        if (!LoginUtil.checkNull(content) && content.size() > CommonConst.NUMBER_0) {
            for (PersonReportVo item : content) {
                if (item.getThirdProxy() == null) {
                    continue;
                }
                ProxyUser third_proxy = proxyUserService.findById(Long.parseLong(item.getThirdProxy()));
                item.setThirdProxyName(third_proxy == null ? "" : third_proxy.getUserName());
            }
        }
        return mapPageResultVO;
    }

    private List<PersonReportVo> getMap(List<PersonReportVo> content) {
        if (!LoginUtil.checkNull(content) && content.size() > CommonConst.NUMBER_0) {
            Set<String> ids = content.stream().filter(item -> StringUtils.hasLength(item.getThirdProxy()))
                .map(PersonReportVo::getThirdProxy).collect(Collectors.toSet());
            List<String> list = new ArrayList<>();
            list.addAll(ids);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUsers(list);
            Map<Long, ProxyUser> map = proxyUsers.stream().collect(Collectors.toMap(ProxyUser::getId, (p) -> p));
            for (PersonReportVo item : content) {
                if (!StringUtils.hasLength(item.getThirdProxy())) {
                    continue;
                }
                ProxyUser proxyUser = map.get(Long.valueOf(item.getThirdProxy()));
                item.setThirdProxyName( proxyUser == null ? "" : proxyUser.getUserName());
            }
        }
        return content;
    }

    private PageResultVO<PersonReportVo> combinePage(List<PersonReportVo> reportResult, int totalElement, int page,
        int num) {
        PageResultVO<PersonReportVo> pageResult =
            new PageResultVO<>(page, num, Long.parseLong(totalElement + ""), reportResult);
        return pageResult;
    }

    @NoAuthorization
    @ApiOperation("导出个人报表")
    @GetMapping("/exportReport")
    @ApiImplicitParams({@ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "time", value = "1 北京时间 2 美东时间", required = false),
        @ApiImplicitParam(name = "tag", value = "1：投注笔数 2：投注金额 3：有效投注 4：洗码发放 5：用户输赢金额", required = false),})
    public void exportReport(String userName, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime, String platform, Integer sort, Integer time,
        Integer tag, HttpServletRequest request, HttpServletResponse response) {
        String orderTimeStart = "'" + DateUtil.formatDate(startTime) + "'";
        String orderTimeEnd = "'" + DateUtil.formatDate(endTime) + "'";
        if (LoginUtil.checkNull(time)) {
            // 向后偏移12小时
            if (LoginUtil.checkNull(platform) || platform.equals("WM")) {
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        } else {
            if (time == CommonConst.NUMBER_2) {
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        }

        String startTimeStr = DateUtil.formatDateTime(startTime);
        String endTimeStr = DateUtil.formatDateTime(endTime);
        List<PersonReportVo> list = new ArrayList<>();
        if (StringUtils.hasLength(userName)) {
            User user = userService.findByAccount(userName);
            if (user != null) {
                List<PersonReportVo> reportResult = userService.findMap(platform, startTimeStr, endTimeStr,
                    user.getId(), orderTimeStart, orderTimeEnd, "");
                list = getMap(reportResult);
            }
        } else {
            List<PersonReportVo> reportResult = null;
            try {
                String statement = getOrderByStatement(tag, sort);
                reportResult = userService.findMapExport(platform, startTimeStr, endTimeStr, statement, orderTimeStart,
                    orderTimeEnd, "");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询会员报表失败");
            }
            list = getMap(reportResult);

            if(Objects.nonNull(list)){
                Map<String, Object> result =
                    userService.findMap(platform, startTimeStr, endTimeStr, orderTimeStart, orderTimeEnd, "");
                PersonReportVo item = DTOUtil.toDTO(result, PersonReportVo.class);
                item.setAccount("总计");
                list.add(item);
            }
        }
        String[] title =
            {"会员账号", "基层代理", "投注笔数", "投注金额", "有效投注", "贡献代理抽点", "用户输赢", "平台盈亏结算(毛利1)", "累计人人贷佣金", "提款手续费", "总结算(毛利2)"};
        // excel文件名
        String fileName = "会员总报表" + System.currentTimeMillis() + ".xls";
        // sheet名
        String sheetName = "会员总报表";
        String[][] content = new String[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            content[i] = new String[title.length];
            PersonReportVo obj = list.get(i);
            content[i][0] = obj.getAccount();
            content[i][1] = obj.getThirdProxyName();
            content[i][2] = obj.getNum().toString();
            content[i][3] = obj.getBetAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][4] = obj.getValidbet().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][5] = obj.getAllWater().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][6] = obj.getWinLoss().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][7] = obj.getAvgBenefit().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][8] = obj.getAllProfitAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][9] = obj.getServiceCharge().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            content[i][10] = obj.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        // 创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);
        // 响应到客户端
        try {
            this.setResponseHeader(response, fileName);
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送响应流方法

    public void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            fileName = new String(fileName.getBytes("utf-8"),"ISO-8859-1");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("content-disposition",
                "attachment;filename=" +
                    new String(fileName.getBytes("utf-8"),"ISO-8859-1" )+ ".xls");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // @NoAuthorization
    @ApiOperation("查询个人报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({@ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "time", value = "1 北京时间 2 美东时间", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),})
    public ResponseEntity<PersonReportTotalVo> queryTotal(String userName, String platform, Integer time,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (LoginUtil.checkNull(startDate, endDate)) {
            return ResponseUtil.custom("参数不合法");
        }

        String orderTimeStart = "'" + DateUtil.formatDate(startDate) + "'";
        String orderTimeEnd = "'" + DateUtil.formatDate(endDate) + "'";

        if (LoginUtil.checkNull(time)) {
            // 向后偏移12小时
            if (LoginUtil.checkNull(platform) || platform.equals("WM")) {
                startDate = DateUtil.offsetHour(startDate, 12);
                endDate = DateUtil.offsetHour(endDate, 12);
            }
        } else {
            if (time == CommonConst.NUMBER_2) {
                startDate = DateUtil.offsetHour(startDate, 12);
                endDate = DateUtil.offsetHour(endDate, 12);
            }
        }

        String startTime = DateUtil.formatDateTime(startDate);
        String endTime = DateUtil.formatDateTime(endDate);

        Long userId = null;
        PersonReportTotalVo itemObject = null;
        if (StringUtils.hasLength(userName)) {
            User user = userService.findByAccount(userName);
            if (user != null) {
                userId = user.getId();
                List<PersonReportVo> maps =
                    userService.findMap(platform, startTime, endTime, userId, orderTimeStart, orderTimeEnd, "");
                itemObject = DTOUtil.toDTO(maps.get(0), PersonReportTotalVo.class);
            }
        } else {
            Map<String, Object> result =
                userService.findMap(platform, startTime, endTime, orderTimeStart, orderTimeEnd, "");
            itemObject = DTOUtil.toDTO(result, PersonReportTotalVo.class);
        }
        return ResponseUtil.success(itemObject);
    }

    /*private HistoryTotal getHistoryItem(Map<String,Object> result){
        HistoryTotal historyTotal = new HistoryTotal();
        historyTotal.setAll_profit_amount(new BigDecimal(result.get("all_profit_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setService_charge(new BigDecimal(result.get("service_charge").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setTotal_amount(new BigDecimal(result.get("total_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setNum(Integer.parseInt(result.get("num").toString()));
        historyTotal.setBet_amount(new BigDecimal(result.get("bet_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWin_loss(new BigDecimal(result.get("win_loss").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setValidbet(new BigDecimal(result.get("validbet").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWash_amount(new BigDecimal(result.get("wash_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setAvg_benefit(new BigDecimal(result.get("avg_benefit").toString()).setScale(2, RoundingMode.HALF_UP));
        return historyTotal;
    }*/
}
