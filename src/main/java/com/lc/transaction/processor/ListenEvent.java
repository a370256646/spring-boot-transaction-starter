package com.lc.transaction.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.transaction.common.Topcs;
import com.lc.transaction.common.mysql.model.DistributionTaskInvoke;
import com.lc.transaction.common.redis.CompleteStatusEnum;
import com.lc.transaction.common.redis.model.DistributionTask;
import com.lc.transaction.common.redis.model.TransactionTask;
import com.lc.transaction.common.redis.repository.DistributionTaskRepository;
import com.lc.transaction.common.redis.repository.TransactionTaskRepository;
import com.lc.transaction.reflex.BeanInvoke;
import com.lc.transaction.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 消息监听
 *
 * @author liucheng
 * @create 2018-05-11 17:32
 **/
@Slf4j
@Component
@EnableAsync
public class ListenEvent {
    @Autowired
    private TransactionTaskRepository transactionTaskRepository;
    @Autowired
    private DistributionTaskRepository distributionTaskRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ConfigService configService;

    @Value("${spring.application.name}")
    private String sponsor;
    @Value("${server.port}")
    private String port;


    @KafkaListener(topics = Topcs.TRANSACTION)
    public void handle(String msg, Acknowledgment ack) {
        log.info("收到kafka消息：" + msg);
        try {
            if (handleListener(Long.valueOf(msg))) {
                ack.acknowledge();
                log.info("kafka消息处理完成：" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("kafka消息处理失败：" + msg, e);
        }
    }


    /**
     * 消息处理器
     *
     * @param taskId 任务id
     * @return 返回是否消费掉消息
     */
    private boolean handleListener(Long taskId) {
        String possessor = sponsor + port;
        Optional<TransactionTask> optional = transactionTaskRepository.findById(taskId);
        if (!optional.isPresent()) {
            log.error("当前任务id无法查询到任务taskId:" + taskId);
            return false;
        }
        TransactionTask transactionTask = optional.get();
        if (!transactionTask.getExpects().contains(sponsor)) {
            log.info("当前服务不是该任务的参与者，默认消费掉 taskId:" + taskId);
            return true;
        }
        //加锁
        DistributionTask prepareLock = DistributionTask.builder()
                .id(System.currentTimeMillis())
                .taskId(transactionTask.getId())
                .possessor(possessor)
                .completeStatus(CompleteStatusEnum.NOT_STARTED.key)
                .createAt(new Date())
                .build();
        distributionTaskRepository.save(prepareLock);
        //检测锁是否生效
        List<DistributionTask> distributionList = distributionTaskRepository.findByTaskIdAndPossessorOrderByCreateAtAsc(
                transactionTask.getId(), possessor);
        if (null == distributionList || distributionList.isEmpty()) {
            log.error("加锁失败:taskId:" + taskId + " sponsor:" + possessor);
            return false;
        }
        DistributionTask lock = distributionList.get(0);
        //比较生效的锁
        for (int i = 0; i < distributionList.size(); i++) {
            if (i != 0) {
                //删除锁
                distributionTaskRepository.delete(distributionList.get(i));
            }
        }
        if (!lock.getId().equals(prepareLock.getId()) && !lock.getPossessor().equals(possessor)) {
            //当前锁没生效
            log.info("当前服务没有抢占到任务锁" + lock.getId() + "，默认消费掉 taskId:" + taskId + " prepareLockId:" + prepareLock.getId());
            return true;
        }
        //当前开始执行事务机制
        boolean fag;
        try {
            Class clz = configService.getClass(transactionTask.getContent());
            if (clz == null) {
                log.error("找不到处理实例 taskId:" + taskId + " content:" + transactionTask.getContent());
                fag = false;
            } else {
                DistributionTaskInvoke rlt = BeanInvoke.invoke(clz, transactionTask.getContent());
                fag = rlt.getSucceed();
            }
            if (fag) {
                //保存到数据库
                log.info("成功 taskId:" + taskId);
                fag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            fag = false;
        }
        if (!fag) {
            log.error("当前任务执行事务失败 taskId:" + taskId);
            lock.setCompleteStatus(CompleteStatusEnum.FAILURE.key);
            distributionTaskRepository.save(lock);
        }
        return fag;
    }

}
