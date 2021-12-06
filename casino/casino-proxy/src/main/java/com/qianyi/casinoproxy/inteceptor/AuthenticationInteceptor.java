package com.qianyi.casinoproxy.inteceptor;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected boolean hasBan() {
       Long authId= CasinoProxyUtil.getAuthId();
        ProxyUser proxyUser = proxyUserService.findById(authId);
        boolean flag= proxyUser.checkUser(proxyUser);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, HttpServletResponse response) {
        String token = CasinoProxyUtil.getToken();

        if(JjwtUtil.check(token, "casino-proxy")){
            return true;
        }
        return false;
    }

    /**
     * 多设备登录校验，后面登录的会踢掉前面登录的
     * @return
     */
    @Override
    protected boolean multiDeviceCheck() {
        Long authId = CasinoProxyUtil.getAuthId();
        String token = CasinoProxyUtil.getToken();
        String key = Constants.REDIS_TOKEN + "proxy:"+ authId;
        Object redisToken = redisUtil.get(key);
        if (ObjectUtils.isEmpty(redisToken)) {
            return true;
        }
        JjwtUtil.Token redisToken1 = (JjwtUtil.Token) redisToken;
        //新旧token有一个匹配得上说明就是最新token
        if (token.equals(redisToken1.getOldToken())||token.equals(redisToken1.getNewToken())) {
            return true;
        }
        return false;
    }
}
