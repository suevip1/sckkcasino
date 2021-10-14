package com.qianyi.casinoreport.consumer;

import com.qianyi.casinoreport.business.RechargeRecordBussiness;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.CHARGEORDER_QUEUE)
@Component
public class ChargeOrderConsumer {

    @Autowired
    private RechargeRecordBussiness rechargeRecordBussiness;

    @RabbitHandler
    public void process(RechargeRecordVo rechargeRecordVo, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",rechargeRecordVo);
        rechargeRecordBussiness.procerssShareProfit(rechargeRecordVo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
