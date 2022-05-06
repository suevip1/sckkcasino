package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.GameRecordTotalVo;
import com.qianyi.casinocore.vo.GameRecordVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("gameRecord")
@Api(tags = "报表中心")
public class GameRecordController {
    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;
    /**
     * 分页查询三方游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("分页查询三方游戏注单")
    @GetMapping("/findGameRecordPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    public ResponseEntity<GameRecordVo> findGameRecordPage(Integer pageSize, Integer pageCode, String user, String betId,
                                                         String gname,Integer gid,String account,Integer tag,
                                                           @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                           @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecord game = new GameRecord();
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        game.setUserId(userId);
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        game.setUser(user);
        Page<GameRecord> gameRecordPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,startTime,endTime,null,null);
            }else {
                gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordVo> pageResultVO =new PageResultVO(gameRecordPage);
        List<GameRecord> content = gameRecordPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecord::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordVo gameRecordVo = new GameRecordVo(gameRecord);
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            gameRecordVo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(gameRecordVo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

//    @ApiOperation("分页查询三方游戏注单总计")
//    @GetMapping("/findGameRecordTotal")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
//            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
//            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
//            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
//            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
//            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
//            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
//            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
//    })
//    @NoAuthorization
//    public ResponseEntity<GameRecordTotalVo> findGameRecordTotal(String user, String betId,
//                                                                 String gname, Integer gid, String account, Integer tag,
//                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
//                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
//        GameRecord game = new GameRecord();
//        Long userId = null;
//        if (!LoginUtil.checkNull(account)){
//            User byAccount = userService.findByAccount(account);
//            if (LoginUtil.checkNull(byAccount)){
//                return ResponseUtil.custom("用户不存在");
//            }
//            userId = byAccount.getId();
//        }
//        game.setUserId(userId);
//        game.setBetId(betId);
//        game.setGname(gname);
//        game.setGid(gid);
//        game.setUser(user);
//        List<GameRecord> gameRecordList;
//        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
//            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
//            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
//            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
//                gameRecordList = gameRecordService.findGameRecordList(game,startTime,endTime,null,null);
//            }else {
//                gameRecordList = gameRecordService.findGameRecordList(game,null,null,startTime,endTime);
//            }
//        }else {
//            gameRecordList = gameRecordService.findGameRecordList(game,null,null,null,null);
//        }
//        BigDecimal bet=BigDecimal.ZERO;
//        BigDecimal validbet=BigDecimal.ZERO;
//        BigDecimal winLoss=BigDecimal.ZERO;
//        if(gameRecordList != null && gameRecordList.size() > 0){
//            List<Map<BigDecimal, Object>> list = new ArrayList<>();
//            gameRecordList.stream().forEach(info ->{
//                list.add(new HashMap(1) {{
//                    put("validbet", new BigDecimal(info.getValidbet()));
//                    put("bet", new BigDecimal(info.getBet()));
//                    put("winLoss", new BigDecimal(info.getWinLoss()));
//                }});
//            });
//             bet = list.stream().map(item -> { { return new BigDecimal(item.get("bet").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
//            validbet = list.stream().map(item -> { { return new BigDecimal(item.get("validbet").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
//            winLoss = list.stream().map(item -> { { return new BigDecimal(item.get("winLoss").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
//        }
//        GameRecordTotalVo gameRecordTotalVo=new GameRecordTotalVo();
//        gameRecordTotalVo.setBet(bet);
//        gameRecordTotalVo.setValidbet(validbet);
//        gameRecordTotalVo.setWinLoss(winLoss);
//        return ResponseUtil.success(gameRecordTotalVo);
//    }

    /**
     * 统计三方游戏注单
     *findRecordRecordSum
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("统计三方游戏注单")
    @GetMapping("/findGameRecordTotal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordTotalVo> findRecordRecordSum(String user,String betId,String gname,Integer gid,String account,Integer tag,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecord game = new GameRecord();
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            game.setUserId(byAccount.getId());
        }
        game.setUser(user);
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        GameRecord recordRecordSum;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                recordRecordSum = gameRecordService.findRecordRecordSum(game, startTime, endTime, null, null);
            }else {
                recordRecordSum = gameRecordService.findRecordRecordSum(game,null,null,startTime,endTime);
            }
        }else {
            recordRecordSum = gameRecordService.findRecordRecordSum(game,null,null,null,null);
        }
        GameRecordTotalVo gameRecordTotalVo = new GameRecordTotalVo();
        if (!LoginUtil.checkNull(recordRecordSum)){
            if (StringUtils.hasText(recordRecordSum.getBet())){
                gameRecordTotalVo.setBet(new BigDecimal(recordRecordSum.getBet()));
            }
            if (StringUtils.hasText(recordRecordSum.getValidbet())){
                gameRecordTotalVo.setValidbet(new BigDecimal(recordRecordSum.getValidbet()));
            }
            if (StringUtils.hasText(recordRecordSum.getBet())){
                gameRecordTotalVo.setWinLoss(new BigDecimal(recordRecordSum.getWinLoss()));
            }
        }
        return ResponseUtil.success(recordRecordSum);
    }
}
