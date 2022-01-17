package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(4)
public class SqlInitialize implements CommandLineRunner {
    @Autowired
    private SysPermissionService sysPermissionService;

    @Override
    public void run(String... args) throws Exception {
        SysPermission sysPermission1 = sysPermissionService.findByName("历史盈亏报表");
        if (!LoginUtil.checkNull(sysPermission1)){
            sysPermission1.setName("会员总报表");
            sysPermissionService.save(sysPermission1);
        }
    }
}
