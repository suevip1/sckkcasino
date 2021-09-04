package com.qianyi.casinoadmin.controller;

import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.service.SysUserLoginLogService;
import com.qianyi.casinocore.utils.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.SysUserLoginLog;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.moduleauthenticator.GoogleAuthUtil;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.ExpiringMapUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 用户谷歌验证登录
 */
@Api(tags = "认证中心")
@RestController
@RequestMapping("login")
public class LoginController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    private Producer captchaProducer;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserLoginLogService sysUserLoginLogService;

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "captchaCode", value = "验证码代号", required = true),
            @ApiImplicitParam(name = "captchaText", value = "验证码文本", required = true),
    })
    @PostMapping("loginA")
    public ResponseEntity loginA(
            String userName,
            String password,
            String captchaCode,
            String captchaText) {
        if (ObjectUtils.isEmpty(userName) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(captchaCode) || ObjectUtils.isEmpty(captchaText)) {
            return ResponseUtil.parameterNotNull();
        }

        SysUser user = sysUserService.findByUserName(userName);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = user.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }

        String token = JjwtUtil.generic(user.getId() + "");
        return ResponseUtil.success(token);
    }

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
    })
    @PostMapping("loginB")
    public ResponseEntity loginB(String userName, String password, Integer code) {
        if (ObjectUtils.isEmpty(userName) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
            return ResponseUtil.parameterNotNull();
        }

        boolean length = SysUser.checkLength(userName, password);
        if (!length) {
            return ResponseUtil.custom("帐号,密码长度3-15位");
        }

        SysUser user = sysUserService.findByUserName(userName);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = SysUser.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }

        String token = JjwtUtil.generic(user.getId() + "");

//        if(Constants.open != user.getGaStatus()){//谷歌验证关闭
//            return ResponseUtil.success(token);
//        }

        String secret = user.getGaKey();
        if (LoginUtil.checkNull(secret)) {
            return ResponseUtil.custom("请先绑定谷歌身份验证器");
        }
        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
        if (!checkCode) {
            return ResponseUtil.googleAuthNoPass();
        }


        //记录登陆日志
        String ip = IpUtil.getIp(LoginUtil.getRequest());
        SysUserLoginLog sysUserLoginLog = new SysUserLoginLog(ip, user.getUserName(), user.getId(), "admin");
        sysUserLoginLogService.saveSyncLog(sysUserLoginLog);

        return ResponseUtil.success(token);
    }

    @ApiOperation("谷歌图形验证码")
    @ApiImplicitParam(name = "code", value = "code前端可随机数或者时间戮，以降低冲突的次数", required = true)
    @GetMapping("captcha")
    @NoAuthentication
    public void captcha(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (LoginUtil.checkNull(code)) {
            return;
        }
        //生产验证码字符串并保存到session中
        String createText = captchaProducer.createText();

        String key = LoginUtil.getCaptchaKey(request, code);
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


    @GetMapping("google/auth/bind")
    @NoAuthentication
    @ApiOperation("绑定谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "返回二维码地址")
    })
    public ResponseEntity bindGoogleAuth(String userName, String password) {
        if (LoginUtil.checkNull(userName) || LoginUtil.checkNull(password)) {
            return ResponseUtil.parameterNotNull();
        }

        SysUser user = sysUserService.findByUserName(userName);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = SysUser.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }

        String secret = user.getGaKey();

        if (LoginUtil.checkNull(secret)) {
            secret = GoogleAuthUtil.generateSecretKey();
            sysUserService.setSecretById(user.getId(), secret);
        }

        String qrcode = GoogleAuthUtil.getQcode(userName, secret);
        return ResponseUtil.success(qrcode);

    }


    @GetMapping("getJwtToken")
    @ApiOperation("开发者通过此令牌调试接口。不可用于正式请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
    })
    @NoAuthentication
    public ResponseEntity getJwtToken(String account) {
        SysUser user = sysUserService.findByUserName(account);
        if (user == null) {
            return ResponseUtil.fail();
        }
        String jwt = JjwtUtil.generic(user.getId()+"");
        return ResponseUtil.success(jwt);
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    public ResponseEntity refreshJwtToken(String token) {
        String refreshToken = JjwtUtil.refreshToken(token);
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }

        return ResponseUtil.success(refreshToken);
    }
}
