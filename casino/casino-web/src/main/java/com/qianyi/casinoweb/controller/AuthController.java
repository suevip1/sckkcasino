package com.qianyi.casinoweb.controller;

import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.IpBlackService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.InviteCodeUtil;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.moduleauthenticator.WangyiDunAuthUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.ExpiringMapUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Api(tags = "认证中心")
@RestController
@RequestMapping("auth")
@Slf4j
public class AuthController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    Producer captchaProducer;

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    IpBlackService ipBlackService;

    @Autowired
    @Qualifier("loginLogJob")
    AsyncService asyncService;

    @PostMapping("register")
    @ApiOperation("用户注册")
    @NoAuthentication
    @Transactional
    //1分钟3次
    @RequestLimit(limit = 3, timeout = 60)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),
            @ApiImplicitParam(name = "validate", value = "网易易顿", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "邀请码", required = true),
    })
    public ResponseEntity register(String account, String password, String phone,
                                   HttpServletRequest request, String validate,String inviteCode) {
        boolean checkNull = CommonUtil.checkNull(account, password, phone, validate,inviteCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }


        //验证码校验
//        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
//        if (!captcha) {
//            return ResponseUtil.custom("验证码错误");
//        }
        boolean wangyidun = WangyiDunAuthUtil.verify(validate);
        if (!wangyidun) {
            return ResponseUtil.custom("验证码错误");
        }


        //卫语句校验
        boolean checkAccountLength = User.checkAccountLength(account);
        if (!checkAccountLength) {
            return ResponseUtil.custom("用户名长度6-15位,由字母，数字，下划线组成");
        }

        boolean checkPasswordLength = User.checkPasswordLength(password);
        if (!checkPasswordLength) {
            return ResponseUtil.custom("密码长度6-15位,由字母，数字，下划线组成");
        }

        String ip = IpUtil.getIp(request);
        //查询ip注册账号限制
        if (!ObjectUtils.isEmpty(ip)) {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            Integer timeLimit = null;
            if (platformConfig != null) {
                timeLimit = platformConfig.getIpMaxNum() == null ? 5 : platformConfig.getIpMaxNum();
            }
            Integer count = userService.countByIp(ip);
            if (count != null && count > timeLimit) {
                return ResponseUtil.custom("当前IP注册帐号数量超过上限");
            }
        }

        User user = userService.findByAccount(account);
        if (user != null && !CommonUtil.checkNull(user.getPassword())) {
            return ResponseUtil.custom("该帐号已存在");
        }

        user = new User();
        User byInviteCode = userService.findByInviteCode(inviteCode);
        if (byInviteCode == null) {
            return ResponseUtil.custom("邀请码不存在");
        }
        user.setFirstPid(byInviteCode.getId());
        user.setSecondPid(byInviteCode.getFirstPid());
        user.setThirdPid(byInviteCode.getSecondPid());
        user.setAccount(account);
        user.setPassword(CasinoWebUtil.bcrypt(password));
        user.setPhone(phone);
        user.setState(Constants.open);
        user.setRegisterIp(ip);
        //生成邀请码
        user.setInviteCode(createInviteCode());
        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney = new UserMoney();
        userMoney.setUserId(save.getId());
        userMoneyService.save(userMoney);
        //记录注册日志
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        vo.setRemark(Constants.CASINO_WEB);
        vo.setType(2);
        asyncService.executeAsync(vo);
        //推送MQ
        rabbitTemplate.convertAndSend(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.ADDUSERTOTEAM_DIRECT, save, new CorrelationData(UUID.randomUUID().toString()));
        log.info("团队新增成员消息发送成功={}", save);
        return ResponseUtil.success();
    }

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "validate", value = "网易易顿", required = true),
            @ApiImplicitParam(name = "deviceId", value = "设备ID,移动端必传", required = false),
    })
    @PostMapping("loginA")
    public ResponseEntity loginA(
            String account,
            String password,
            String validate,String deviceId) {
        if (CasinoWebUtil.checkNull(account, password, validate)) {
            return ResponseUtil.parameterNotNull();
        }

//        //验证码校验
//        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
//        if (!captcha) {
//            return ResponseUtil.custom("验证码错误");
//        }

        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        String bcryptPassword = user.getPassword();
        boolean bcrypt = CasinoWebUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        //常用设备优先校验
        boolean verifyFlag = false;
        if (!ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId) && user.getDeviceId().equals(deviceId)) {
            verifyFlag = true;
        }
        if (!verifyFlag) {
            //验证码校验
            boolean wangyidun = WangyiDunAuthUtil.verify(validate);
            if (!wangyidun) {
                return ResponseUtil.custom("验证码错误");
            }
        }
        if (ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId)) {
            user.setDeviceId(deviceId);
            userService.save(user);
        }
        //记录登陆日志
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        vo.setRemark(Constants.CASINO_WEB);
        vo.setType(1);
        asyncService.executeAsync(vo);

        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String token = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), token);
        return ResponseUtil.success(token);
    }

//    @NoAuthentication
//    @ApiOperation("帐密登陆.谷歌身份验证器")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "帐号", required = true),
//            @ApiImplicitParam(name = "password", value = "密码", required = true),
//            @ApiImplicitParam(name = "code", value = "验证码", required = true),
//    })
//    @PostMapping("loginB")
//    public ResponseEntity loginB(String account, String password, Integer code) {
//        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        boolean length = User.checkLength(account, password);
//        if (!length) {
//            return ResponseUtil.custom("帐号,密码长度3-15位");
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("该帐号不可操作");
//        }
//
//        String secret = user.getSecret();
//        if (PayUtil.checkNull(secret)) {
//            return ResponseUtil.custom("请先绑定谷歌身份验证器");
//        }
//        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
//        if (!checkCode) {
//            return ResponseUtil.googleAuthNoPass();
//        }
//
//        String token = JjwtUtil.generic(user.getId() + "");
//
//        //记录登陆日志
//        String ip = IpUtil.getIp(PayUtil.getRequest());
//        new Thread(new LoginLogJob(ip, user.getAccount(), user.getId(), "admin")).start();
//
//        return ResponseUtil.success(token);
//    }


    @ApiOperation("谷歌图形验证码")
    @ApiImplicitParam(name = "code", value = "code前端可随机数或者时间戮，以降低冲突的次数", required = true)
    @GetMapping("captcha")
    @NoAuthentication
    public void captcha(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (CommonUtil.checkNull(code)) {
            return;
        }
        //生产验证码字符串并保存到session中
        String createText = captchaProducer.createText();

        String key = CasinoWebUtil.getCaptchaKey(request, code);
        ExpiringMapUtil.putMap(key, createText);

        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ServletOutputStream responseOutputStream = response.getOutputStream();

        //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
        BufferedImage challenge = captchaProducer.createImage(createText);
        ImageIO.write(challenge, "jpg", jpegOutputStream);
        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
//
//    @GetMapping("google/auth/bind")
//    @NoAuthentication
//    @ApiOperation("绑定谷歌身份验证器")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "帐号", required = true),
//            @ApiImplicitParam(name = "password", value = "密码", required = true)
//    })
//    @ApiResponses({
//            @ApiResponse(code = 0, message = "返回二维码地址")
//    })
//    public ResponseEntity bindGoogleAuth(String account, String password) {
//        if (PayUtil.checkNull(account) || PayUtil.checkNull(password)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("该帐号不可操作");
//        }
//
//        String secret = user.getSecret();
//
//        if (PayUtil.checkNull(secret)) {
//            secret = GoogleAuthUtil.generateSecretKey();
//            userService.setSecretById(user.getId(), secret);
//        }
//
//        String qrcode = GoogleAuthUtil.getQcode(account, secret);
//        return ResponseUtil.success(qrcode);
//
//    }

    @GetMapping("getJwtToken")
    @ApiOperation("开发者通过此令牌调试接口。不可用于正式请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "固定值。", required = true),
    })
    @NoAuthentication
    public ResponseEntity getJwtToken(String token) {
        User user = userService.findByAccount(token);
        if (user == null) {
            return ResponseUtil.custom("账号不存在");
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String jwt = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), jwt);
        return ResponseUtil.success(jwt);
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    public ResponseEntity refreshJwtToken(String token) {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId(token);
        if (authId == null) {
            return ResponseUtil.authenticationNopass();
        }
        User user = userService.findById(authId);
        String refreshToken = JjwtUtil.refreshToken(token, user.getPassword(),Constants.CASINO_WEB);
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }
        setUserTokenToRedis(authId, refreshToken);
        return ResponseUtil.success(refreshToken);
    }

    @GetMapping("checkInviteCode")
    @ApiOperation("校验邀请码")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "inviteCode", value = "邀请码", required = true),
    })
    public ResponseEntity checkInviteCode(String inviteCode) {
        boolean checkNull = CommonUtil.checkNull(inviteCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        User user = userService.findByInviteCode(inviteCode);
        if (user == null) {
            String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
            IpBlack ipBlack = new IpBlack();
            ipBlack.setIp(ip);
            ipBlack.setStatus(Constants.no);
            ipBlack.setRemark("邀请码填写错误，封IP");
            ipBlackService.save(ipBlack);
            return ResponseUtil.custom("邀请码填写错误,ip被封");
        }
        return ResponseUtil.success();
    }

    private void setUserTokenToRedis(Long userId, String token) {
        try {
            redisTemplate.opsForValue().set("token:" + userId, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createInviteCode() {
        User user = null;
        String inviteCode = null;
        do {
            inviteCode = InviteCodeUtil.randomCode6();
            user = userService.findByInviteCode(inviteCode);
        } while (user != null);
        return inviteCode;
    }
}
