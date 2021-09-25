package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InteceptorController implements ErrorController {

    @NoAuthentication
    @RequestMapping("error")
    public ResponseEntity error() {
        return ResponseUtil.error();
    }

    @NoAuthentication
    @RequestMapping("authenticationNopass")
    public ResponseEntity authenticationNopass() {
        return ResponseUtil.authenticationNopass();
    }

    @RequestMapping("authenticationBan")
    public ResponseEntity authenticationBan() {
        return ResponseUtil.custom("帐号被封");
    }

    @NoAuthentication
    @RequestMapping("authenticationMultiDevice")
    public ResponseEntity authenticationMultiDevice() {
        return ResponseUtil.custom("帐号已在其他设备登录,请重新登录");
    }

    @NoAuthentication
    @RequestMapping("risk")
    public ResponseEntity risk() {
        return ResponseUtil.risk();
    }
}
