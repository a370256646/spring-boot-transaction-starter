package com.lc.transaction.common;

import com.lc.transaction.config.TransactionConfig;
import com.lc.transaction.processor.ListenEvent;
import com.lc.transaction.processor.SendEvent;
import com.lc.transaction.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * @author liucheng
 * @create 2018-05-11 18:30
 **/
@Configuration
@ComponentScan({"com.lc"})
@EntityScan({"com.lc.transaction.common"})
@EnableRedisRepositories("com.lc.transaction.common.redis.*")
public class TransactionAutoConfiguration {

    @Bean
    public TransactionConfig transactionConfig() {
        return new TransactionConfig();
    }

    @Bean
    public SendEvent sendEvent() {
        return new SendEvent();
    }

    @Bean
    public ListenEvent listenEvent() {
        return new ListenEvent();
    }
}
