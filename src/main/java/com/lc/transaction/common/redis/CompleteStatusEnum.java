package com.lc.transaction.common.redis;

/**
 * DistributionTask 的CompleteStatus枚举
 *
 * @author liucheng
 * @create 2018-05-16 10:25
 **/
public enum CompleteStatusEnum {
    NOT_STARTED(0, "未开始"),
    FAILURE(1, "执行失败"),
    WAIT_CHECK(2, "执行完成待校验"),
    CHECK_SUCCEED(3, "校验成功"),
    CHECK_FAILURE(4, "校验失败");

    public String value;
    public Integer key;

    CompleteStatusEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getName(Integer key) {
        for (CompleteStatusEnum c : CompleteStatusEnum.values()) {
            if (c.getKey() == key) {
                return c.getValue();
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }
}
