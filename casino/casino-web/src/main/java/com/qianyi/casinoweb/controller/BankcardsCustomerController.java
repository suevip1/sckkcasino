package com.qianyi.casinoweb.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.BankcardsCustomer;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.BankInfoRepository;
import com.qianyi.casinocore.repository.BankcardsCustomerRepository;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsCustomerService;
import com.qianyi.casinocore.service.SysDictService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.Assert;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/customer/bank")
@Api(tags = "用户中心")
public class BankcardsCustomerController {
	
    @Autowired
    UserService userService;
    
    @Autowired
    SysDictService sysDictService;

    @Autowired
    private BankInfoService bankInfoService;
    
    @Autowired
    private BankcardsCustomerService bankcardsCustomerService;
    
    @Autowired
    private BankcardsCustomerRepository bankcardsCustomerRepository;
    
    @Autowired
    private BankInfoRepository bankInfoRepository;

    
    @GetMapping("/list")
    @ApiOperation("银行卡列表")
    @NoAuthentication
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }
    
    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @NoAuthentication
    @ResponseBody
    public ResponseEntity boundList() {
        User user = userService.findById(CasinoWebUtil.getAuthId());
    	if(StringUtils.isNullOrEmpty(user.getAccount())) {
    		BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
    		bankcardsCustomer.setAccount(user.getAccount());
    		return ResponseUtil.success(bankcardsCustomerService.findByExample(bankcardsCustomer));
    	}
    	return ResponseUtil.fail();
    }

    @PostMapping("/bound")
    @ApiOperation("用户绑定银行卡")
    @NoAuthentication
    @ApiImplicitParams({
        @ApiImplicitParam(name = "bankName", value = "银行名", required = true),
        @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
        @ApiImplicitParam(name = "bankAccount", value = "用户的银行/支付宝账号", required = true),
//        @ApiImplicitParam(name = "province", value = "省", required = true),
//        @ApiImplicitParam(name = "city", value = "市区", required = true),
        @ApiImplicitParam(name = "address", value = "支行名,开户地址", required = true),
        @ApiImplicitParam(name = "realName", value = "开户名", required = true)})
	public ResponseEntity bound(String bankName, Integer bankId, String bankAccount, String province, String city,
			String address, String realName) {
    	BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
    	bankcardsCustomer.setBankName(bankName);
    	bankcardsCustomer.setBankId(bankId);
    	bankcardsCustomer.setBankAccount(bankAccount);
    	bankcardsCustomer.setProvince(province);
    	bankcardsCustomer.setCity(city);
    	bankcardsCustomer.setAddress(address);
    	bankcardsCustomer.setRealName(realName);
    	
    	User user = userService.findById(CasinoWebUtil.getAuthId());
    	bankcardsCustomer.setAccount(user.getAccount());
    	
    	// 1.查询银行卡是否存在
		BankInfo bankInfo = new BankInfo();
		bankInfo.setId(bankcardsCustomer.getBankId());
		Boolean noBankExists = bankInfoRepository.exists(Example.of(bankInfo));
		Assert.isTrue(noBankExists, "不支持该银行，请更换银行卡");
		// 2.查询当前卡号是否存在
		BankcardsCustomer bankAccountCrad = new BankcardsCustomer();
		bankAccountCrad.setBankAccount(bankcardsCustomer.getBankAccount());
		// 当前用户是否已经绑定过，可以删除：同一张卡不同用户绑定
		bankAccountCrad.setAccount(bankcardsCustomer.getAccount());
		Boolean bankAccountExists = bankcardsCustomerRepository.exists(Example.of(bankAccountCrad));
		Assert.isTrue(!bankAccountExists, "当前银行卡已经被绑定，请换一张卡");
		
		// 3.查看已绑定的数量 不可大于最大数量
		int findByAccountCount = bankcardsCustomerRepository.findByAccountCount(bankcardsCustomer.getAccount());
		Assert.greaterOrEqual(findByAccountCount, Constants.BANK_USER_BOUND_MAX, "最多绑定" + Constants.BANK_USER_BOUND_MAX + "张银行卡，已超出限制。");
		
		// 4.必要的字段进行合法判断
		Assert.isNull(bankcardsCustomer.getAddress(), "开户地址不能为空！");
		Assert.isNull(bankcardsCustomer.getRealName(), "真实姓名不能为空！");
		Assert.isNull(bankcardsCustomer.getBankName(), "银行名不能为空！");
		Assert.isNull(bankcardsCustomer.getBankAccount(), "银行账号不能为空！");
		Assert.designatedArea(bankcardsCustomer.getBankAccount(), "长度只能在16~20位！", 16, 20);
		//TODO : 其余代码具体业务逻辑待定 例如：同一张卡的重复绑定 或者 其他相关业务的处理

		// 4.执行保存
		Date now = new Date();
		bankcardsCustomer.setUpdateTime(now);
		bankcardsCustomer.setCreateTime(now);
		
		BankcardsCustomer fristBank = new BankcardsCustomer();
		fristBank.setAccount(bankcardsCustomer.getAccount());
		bankcardsCustomer.setDefaultCard(0);
		// 如果当前用户没有绑定过卡,默认第一张卡位主卡
		boolean fristBankExists = bankInfoRepository.exists(Example.of(bankInfo));
		if(!fristBankExists) {
			bankcardsCustomer.setDefaultCard(1);
		}
    	
        Integer count = bankcardsCustomerService.bound(bankcardsCustomer);
        return ResponseUtil.success(count);
    }

	@PostMapping("/unBound")
	@ApiOperation("用户解绑银行卡")
	@NoAuthentication
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true)})
	public ResponseEntity unBound(Integer id) {
		BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
		bankcardsCustomer.setId(id);
		
		User user = userService.findById(CasinoWebUtil.getAuthId());
		bankcardsCustomer.setAccount(user.getAccount());
		Integer count = bankcardsCustomerService.unBound(bankcardsCustomer);
		return ResponseUtil.success(count);
	}
}
