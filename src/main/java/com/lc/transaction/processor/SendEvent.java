package com.lc.transaction.processor;

import com.lc.transaction.common.redis.model.TransactionTask;
import com.lc.transaction.common.redis.repository.TransactionTaskRepository;
import com.lc.transaction.common.Topcs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 发送事件
 *
 * @author liucheng
 * @create 2018-05-11 17:57
 **/
@Component
@EnableAsync
public class SendEvent {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private TransactionTaskRepository transactionTaskRepository;
    @Value("${spring.application.name}")
    private String sponsor;
    @Value("${server.port}")
    private String port;


    /**
     * 发送消息
     *
     * @param top     top发送地址
     * @param content 发送内容
     */
    @Async
    public void send(String top, String content) {
        kafkaTemplate.send(top, content);
    }

    /**
     * 发送事务消息
     *
     * @param content 要处理的事件主题
     * @param expects 要参与的人名称
     */
    @Async
    public void send(String content, Set<String> expects) {
        TransactionTask transactionTask = transactionTaskRepository.save(TransactionTask.builder()
                .sponsor(sponsor + port)
                .content(content)
                .expects(expects)
                .expectNum(expects.size())
                .build());
        send(Topcs.TRANSACTION, transactionTask.getId().toString());
    }
}