package com.promanage.service.strategy;

import java.util.List;

/**
 * 通知发送策略接口
 */
public interface NotificationStrategy {

    /**
     * 获取通知接收者列表
     * 
     * @param relatedId 相关数据ID
     * @param relatedType 相关数据类型
     * @param operatorId 操作者ID
     * @return 接收者ID列表
     */
    List<Long> getRecipients(Long relatedId, String relatedType, Long operatorId);

    /**
     * 生成通知标题
     * 
     * @param relatedId 相关数据ID
     * @param relatedType 相关数据类型
     * @param operatorId 操作者ID
     * @return 通知标题
     */
    String generateTitle(Long relatedId, String relatedType, Long operatorId);

    /**
     * 生成通知内容
     * 
     * @param relatedId 相关数据ID
     * @param relatedType 相关数据类型
     * @param operatorId 操作者ID
     * @return 通知内容
     */
    String generateContent(Long relatedId, String relatedType, Long operatorId);
}