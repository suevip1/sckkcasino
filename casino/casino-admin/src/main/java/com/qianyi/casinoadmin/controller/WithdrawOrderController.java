package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.WithdrawOrderVo;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提现记录表
 */
@Slf4j
@RestController
@RequestMapping("/withdraw")
@Api(tags = "资金中心")
public class WithdrawOrderController {

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("提现列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false),
            @ApiImplicitParam(name = "no", value = "订单号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
            @ApiImplicitParam(name = "account", value = "用户账号", required = false),
            @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员", required = false),
    })
    @GetMapping("/withdrawList")
    public ResponseEntity<WithdrawOrderVo> withdrawList(Integer pageSize,Integer pageCode, Integer status, String account,
                                                        String no, String bankId,Integer type){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            withdrawOrder.setUserId(user.getId());
        }
        withdrawOrder.setStatus(status);
        withdrawOrder.setNo(no);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setType(type);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder);
        PageResultVO<WithdrawOrderVo> pageResultVO = new PageResultVO(withdrawOrderPage);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<WithdrawOrderVo> withdrawOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(WithdrawOrder::getUserId).collect(Collectors.toList());
            List<String> collect = content.stream().map(WithdrawOrder::getBankId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<Bankcards> all = bankcardsService.findAll(collect);
//            List<String> updateBys = content.stream().map(WithdrawOrder::getUpdateBy).collect(Collectors.toList());
//            List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            Map<Long, Bankcards> bankcardMap = all.stream().collect(Collectors.toMap(Bankcards::getId, a -> a, (k1, k2) -> k1));
            if(userList != null){
                content.stream().forEach(withdraw ->{
                    WithdrawOrderVo withdrawOrderVo = new WithdrawOrderVo(withdraw);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(withdraw.getUserId())){
                            withdrawOrderVo.setAccount(user.getAccount());
                            try {
                                this.setBankcards(bankcardMap.get(Long.valueOf(withdrawOrderVo.getBankId())),withdrawOrderVo);
                            }catch (Exception ex){
                                log.info("bankId类型转换错误{}",withdrawOrderVo.getBankId());
                            }
                        }
                    });
//                    sysUsers.stream().forEach(sysUser->{
//                        if (withdraw.getStatus() != CommonConst.NUMBER_0 && sysUser.getId().toString().equals(withdraw.getUpdateBy() == null?"":withdraw.getUpdateBy())){
//                            withdrawOrderVo.setUpdateBy(sysUser.getUserName());
//                        }
//                    });
                    withdrawOrderVoList.add(withdrawOrderVo);
                });
            }
            pageResultVO.setContent(withdrawOrderVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setBankcards(Bankcards bankcards,WithdrawOrderVo withdrawOrderVo){
        if (LoginUtil.checkNull(bankcards)){
            return ;
        }
        withdrawOrderVo.setBankNo(bankcards.getBankAccount());
        withdrawOrderVo.setAccountName(bankcards.getRealName());
    }
    @ApiOperation("提现审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "审核状态，1：通过，2：拒绝", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("saveWithdraw")
    public ResponseEntity saveWithdraw(Long id, Integer status,String remark){
        if (LoginUtil.checkNull(id,status)){
            return ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
            return ResponseUtil.custom("参数不合法");
        }
//        WithdrawOrder byId = withdrawOrderService.findById(id);
//        if (LoginUtil.checkNull(byId)){
//            return ResponseUtil.custom("订单不存在");
//        }
//        if (byId.getThirdProxy() != null && byId.getThirdProxy() >= CommonConst.LONG_1){
//            return ResponseUtil.custom("代理提现订单不能处理");
//        }
        Long userId = LoginUtil.getLoginUserId();
        SysUser sysUser = sysUserService.findById(userId);
        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();
        return withdrawBusiness.updateWithdrawAndUser(id,status,lastModifier,remark);
    }
}
