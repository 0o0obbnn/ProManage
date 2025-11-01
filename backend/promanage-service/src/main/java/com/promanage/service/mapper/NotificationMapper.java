package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.Notification;

/** 通知Mapper接口 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

  /** 根据用户ID查找未读通知数量 */
  int countUnreadByUserId(@Param("userId") Long userId);

  /** 根据用户ID查找通知列表 */
  List<Notification> findByUserId(@Param("userId") Long userId);

  /** 根据用户ID和类型查找通知列表 */
  List<Notification> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

  /** 标记通知为已读 */
  int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

  /** 批量标记通知为已读 */
  int markAsReadBatch(@Param("ids") List<Long> ids, @Param("userId") Long userId);

  /** 标记用户所有通知为已读 */
  int markAllAsRead(@Param("userId") Long userId);

  /** 删除通知 */
  int deleteNotification(@Param("id") Long id, @Param("userId") Long userId);

  /** 批量删除通知 */
  int deleteNotificationBatch(@Param("ids") List<Long> ids, @Param("userId") Long userId);

  /** 根据相关数据查找通知 */
  List<Notification> findByRelatedData(
      @Param("relatedId") Long relatedId, @Param("relatedType") String relatedType);
}
