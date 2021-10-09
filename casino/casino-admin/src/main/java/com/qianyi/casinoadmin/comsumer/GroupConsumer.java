package com.qianyi.casinoadmin.comsumer;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE)
@Component
public class GroupConsumer {
    @RabbitHandler
    public void process(User user, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",user);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
