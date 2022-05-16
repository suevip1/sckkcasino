package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.VisitsDevice;
import com.qianyi.casinocore.model.VisitsDeviceAddressBook;
import com.qianyi.casinocore.service.VisitsDeviceAddressBookService;
import com.qianyi.casinocore.service.VisitsDeviceService;
import com.qianyi.casinoweb.co.VisitsDeviceAddressBookParams;
import com.qianyi.casinoweb.co.VisitsDeviceCo;
import com.qianyi.casinoweb.co.VisitsDeviceParams;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "访问设备")
@RestController
@RequestMapping("/visitsDevice")
public class VisitsDeviceController {

    @Autowired
    private VisitsDeviceService visitsDeviceService;
    @Autowired
    private VisitsDeviceAddressBookService visitsDeviceAddressBookService;

    @PostMapping("/save")
    @ApiOperation("保存请求设备信息")
    @NoAuthentication
    public ResponseEntity save(@RequestBody VisitsDeviceCo co, HttpServletRequest request) {
        VisitsDeviceParams device = co.getDevice();
        if (device == null) {
            return ResponseUtil.custom("访问设备信息为空");
        }
        String ip = IpUtil.getIp(request);
        VisitsDevice visitsDevice = visitsDeviceService.findByUdid(device.getUdid());
        if (visitsDevice != null) {
            return ResponseUtil.success();
        }
        VisitsDevice vd = new VisitsDevice();
        BeanUtils.copyProperties(device, vd);
        vd.setIp(ip);
        visitsDeviceService.save(vd);
        List<VisitsDeviceAddressBookParams> addressBook = co.getAddressBook();
        if (!CollectionUtils.isEmpty(addressBook)) {
            List<VisitsDeviceAddressBook> list = new ArrayList<>();
            for (VisitsDeviceAddressBookParams book : addressBook) {
                VisitsDeviceAddressBook vdab = new VisitsDeviceAddressBook();
                BeanUtils.copyProperties(book, vdab);
                vdab.setVisitsDeviceId(vd.getId());
                list.add(vdab);
            }
            visitsDeviceAddressBookService.saveAll(list);
        }
        return ResponseUtil.success();
    }
}
