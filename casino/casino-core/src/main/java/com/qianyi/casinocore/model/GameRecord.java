package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name ="game_record",uniqueConstraints={@UniqueConstraint(columnNames={"betId"})})
@ApiModel("游戏记录")
public class GameRecord extends BaseEntity{

    /**
     * 账号
     */
    private String user;

    /**
     * 注单号
     */
    private String betId;

    /**
     * 下注時間
     */
    private String betTime;

    /**
     * 下注前金额
     */
    private String beforeCash;

    /**
     * 下注金额
     */
    @ApiModelProperty(value = "下注金额")
    private String bet;

    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    private String validbet;

    /**
     * 退水金额
     */
    @ApiModelProperty(value = "退水金额")
    private String water;

    /**
     * 下注结果
     */
    private String result;

    /**
     * 下注代碼
     */
    private String betCode;

    /**
     * 下注内容
     */
    private String betResult;

    /**
     * 下注退水金额
     */
    @ApiModelProperty(value = "下注退水金额")
    private String waterbet;

    /**
     * 输赢金额
     */
    @ApiModelProperty(value = "输赢金额")
    private String winLoss;

    /**
     * ip
     */
    private String ip;

    /**
     * 游戏类别编号
     */
    private Integer gid;

    /**
     * 场次编号
     */
    private String eventAndRound;

    /**
     * 子场次编号
     */
    private String eventChildAndSubround;

    /**
     * 桌台编号
     */
    private String tableId;

    /**
     * 	牌型ex:庄:♦3♦3 闲:♥9♣10
     */
    private String gameResult;

    /**
     * 游戏名称ex:百家乐
     */
    private String gname;

    /**
     * 0:一般, 1:免佣
     */
    private Integer commission;

    /**
     * Y:有重对, N:非重对
     */
    private String reset;

    /**
     * 结算时间
     */
    private String settime;

    /**
     * 电子游戏代码
     */
    private String slotGameId;

    public GameRecord(BigDecimal bet,BigDecimal validbet,BigDecimal water,BigDecimal waterbet,BigDecimal winLoss){
        this.bet = bet.toString();
        this.validbet = validbet.toString();
        this.water = water.toString();
        this.waterbet = waterbet.toString();
        this.winLoss = winLoss.toString();
    }
    public GameRecord(){

    }
}
