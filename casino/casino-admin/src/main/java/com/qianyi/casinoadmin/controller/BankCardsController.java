package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/bankcard")
@Api(tags = "平台中心")
public class BankCardsController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private UserService userService;
    /**
     * 查询所有银行列表
     * @return
     */
    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }
    /**
     * 新增银行
     * @param file 银行图标logo
     * @param bankName 银行名称
     * @param bankCode 银行编码
     * @param bankType 类型0:银行卡，1：支付宝，2：微信，3：QQ
     * @param disable 0:未禁用 1：禁用
     * @param remark 备注
     * @return
     */
    @ApiOperation("新增银行")
    @PostMapping(value = "/saveBankInfo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增银行")
    public ResponseEntity saveBankInfo(@RequestPart(value = "bankLogo银行图标") MultipartFile file, @RequestParam(value = "银行名称") String bankName,
                                        @RequestParam(value = "银行编码") String bankCode,@RequestParam(value = "类型0:银行卡，1：支付宝，2：微信，3：QQ") Integer bankType,
                                       @RequestParam(value = "0:未禁用 1：禁用") Integer disable,@RequestParam(value = "备注")String remark){
        BankInfo bankInfo = new BankInfo();
        return this.saveAndUpdate(file,bankName,bankCode,bankType,disable,remark,bankInfo);
    }
    /**
     * 修改银行信息
     * @param file 银行图标logo
     * @param bankName 银行名称
     * @param bankCode 银行编码
     * @param bankType 类型0:银行卡，1：支付宝，2：微信，3：QQ
     * @param disable 0:未禁用 1：禁用
     * @param remark 备注
     * @param id 银行id
     * @return
     */
    @ApiOperation("修改银行")
    @PostMapping(value = "/updateBankInfo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "修改银行")
    public ResponseEntity updateBankInfo(@RequestPart(value = "bankLogo银行图标") MultipartFile file, @RequestParam(value = "银行名称") String bankName,
                                       @RequestParam(value = "银行编码") String bankCode,@RequestParam(value = "0:银行卡，1：支付宝，2：微信，3：QQ") Integer bankType,
                                       @RequestParam(value = "0:未禁用 1：禁用") Integer disable,@RequestParam(value = "备注")String remark,
                                         @RequestParam(value = "银行id")  Long id){
        BankInfo bankInfo = new BankInfo();
        bankInfo.setId(id);
        return this.saveAndUpdate(file,bankName,bankCode,bankType,disable,remark,bankInfo);

    }
    private ResponseEntity saveAndUpdate(MultipartFile file,String bankName, String bankCode, Integer bankType,
                                         Integer disable,String remark,BankInfo bankInfo){
        bankInfo.setBankName(bankName);
        bankInfo.setBankCode(bankCode);
        bankInfo.setBankType(bankType);
        bankInfo.setDisable(disable);
        bankInfo.setRemark(remark);
        try {
            String fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
            bankInfo.setBankLogo(fileUrl);
            bankInfoService.saveBankInfo(bankInfo);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        return ResponseUtil.success();
    }
    /**
     * 删除银行信息
     * @param id 银行id
     * @return
     */
    @GetMapping("/deleteBankInfo")
    @ApiOperation("删除银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行id", required = true),
    })
    public ResponseEntity deleteBankInfo(Long id) {
        bankInfoService.deleteBankInfo(id);
        return ResponseUtil.success();
    }
    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    public ResponseEntity boundList(Long userId) {
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        return ResponseUtil.success(bankcardsList);
    }

    /**
     * 可以灵活添加参数，满足后续添加的需求
     *
     * @param bankAccount
     * @param realName
     * @return
     */
    @GetMapping("/peggBankCard")
    @ApiOperation("银行卡/开户名反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankAccount", value = "银行卡号", required = false),
            @ApiImplicitParam(name = "realName", value = "开户名", required = false),
    })
    public ResponseEntity peggBankCard(String bankAccount, String realName){
        if(LoginUtil.checkNull(bankAccount, realName)){
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = new Bankcards();
        bankcards.setBankAccount(bankAccount);
        bankcards.setRealName(realName);
        List<Bankcards> bankcardsList = bankcardsService.findUserBank(bankcards);
        return ResponseUtil.success(bankcardsList);
    }

    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
    public ResponseEntity bound(Long userId, String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = this.checkParamFroBound(realName, bankId, bankAccount, address);
        if (!LoginUtil.checkNull(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }

        //判断是否存在该用户
        User user = userService.findById(userId);
        if(user == null){
            return ResponseUtil.custom("不存在该会员");
        }

        if(isGreatThan6(userId)){
            return ResponseUtil.custom("已经超过6张银行卡");
        }

        Bankcards bankcards = boundCard(userId, bankId,bankAccount,address,realName);
        boolean isSuccess= bankcardsService.boundCard(bankcards)==null?true:false;
        return ResponseUtil.success(isSuccess);
    }

    private String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                      String address) {
        if(LoginUtil.checkNull(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空！";
        }
        if (LoginUtil.checkNull(address)) {
            return "开户地址不能为空！";
        }
        if (LoginUtil.checkNull(bankAccount)) {
            return "银行账号不能为空！";
        }
        if (bankAccount.length() > 20 || bankAccount.length() < 16) {
            return "长度只能在16~20位！";
        }
        return null;
    }

    private boolean isGreatThan6(Long userId) {
        int count = bankcardsService.countByUserId(userId);
        return count>=Constants.MAX_BANK_NUM;
    }

    @PostMapping("/disable")
    @ApiOperation("禁用/启用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),

    })
    public ResponseEntity disable(Long userId, String bankId){
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(userId);
        bankcards.setBankId(bankId);
        //查询银行卡
        List<Bankcards> bankcardsList = bankcardsService.findUserBank(bankcards);
        if(bankcardsList == null || bankcardsList.size()<= 0){
            return ResponseUtil.custom("用户未绑定银行卡");
        }
        Bankcards bank = bankcardsList.get(0);
        if(bank.getDisable() == Constants.BANK_CLOSE){
            bank.setDisable(Constants.BANK_OPEN);
        }else{
            bank.setDisable(Constants.BANK_CLOSE);
        }
        boolean isSuccess= bankcardsService.boundCard(bank)==null?true:false;
        return ResponseUtil.success(isSuccess);
    }


    private Bankcards boundCard(Long userId, String bankId, String bankAccount, String address, String realName){
        Bankcards firstBankcard = bankcardsService.findBankCardsInByUserId(userId);
        Date now = new Date();
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(userId);
        bankcards.setBankId(bankId);
        bankcards.setBankAccount(bankAccount);
        bankcards.setAddress(address);
        bankcards.setRealName(getRealName(firstBankcard,realName));
        bankcards.setUpdateTime(now);
        bankcards.setCreateTime(now);
        bankcards.setDisable(Constants.BANK_OPEN);
        bankcards.setDefaultCard(isFirstCard(firstBankcard));
        return bankcards;
    }

    private String getRealName(Bankcards bankcards, String realName){
        return bankcards==null?realName:bankcards.getRealName();
    }

    private Integer isFirstCard(Bankcards bankcards){
        return bankcards==null?0:1;
    }




}
