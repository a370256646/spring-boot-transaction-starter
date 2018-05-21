package com.lc.transaction.common.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 单个任务执行结果，存储到数据库
 *
 * @author liucheng
 * @create 2018-05-16 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionTaskInvoke implements Serializable {

    private Long id;
    /**
     * distributionTask的id
     */
    private Long distributionTaskId;
    /**
     * TransactionTask的id
     */
    private Long taskId;
    /**
     * 所有者
     */
    private String possessor;
    /**
     * 执行结果 成功/失败
     */
    private Boolean succeed;

    /**
     * 执行结果
     */
    private String text;
    /**
     * 创建时间
     */
    private Date createAt;
}
