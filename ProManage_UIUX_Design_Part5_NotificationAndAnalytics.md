# ProManage UI/UX 设计文档 - Part 5: 通知系统与数据分析设计

## 5. 通知系统功能设计

### 5.1 通知中心界面

```typescript
<template>
  <div class="notification-center">
    <!-- 通知中心头部 -->
    <div class="notification-header">
      <h2>通知中心</h2>
      <a-space>
        <a-button @click="handleMarkAllRead">
          <CheckOutlined /> 全部已读
        </a-button>
        <a-button @click="handleNotificationSettings">
          <SettingOutlined /> 通知设置
        </a-button>
      </a-space>
    </div>

    <!-- 通知统计 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="未读通知"
            :value="notificationStats.unread"
            :prefix="() => <BellOutlined />"
            :value-style="notificationStats.unread > 0 ? { color: '#ff4d4f' } : {}"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="今日通知"
            :value="notificationStats.today"
            :prefix="() => <ClockCircleOutlined />"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="待处理任务"
            :value="notificationStats.pendingTasks"
            :prefix="() => <ExclamationCircleOutlined />"
            :value-style="{ color: '#faad14' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 通知筛选 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-space size="large">
        <a-radio-group v-model:value="filterRead" button-style="solid">
          <a-radio-button value="all">
            全部 ({{ notificationStats.total }})
          </a-radio-button>
          <a-radio-button value="unread">
            未读 ({{ notificationStats.unread }})
          </a-radio-button>
          <a-radio-button value="read">
            已读 ({{ notificationStats.read }})
          </a-radio-button>
        </a-radio-group>

        <a-select
          v-model:value="filterType"
          style="width: 150px"
          placeholder="通知类型"
          allow-clear
        >
          <a-select-option value="all">全部类型</a-select-option>
          <a-select-option value="task_assigned">任务分配</a-select-option>
          <a-select-option value="task_mentioned">任务@我</a-select-option>
          <a-select-option value="change_request">变更请求</a-select-option>
          <a-select-option value="review_request">评审请求</a-select-option>
          <a-select-option value="approval_pending">待审批</a-select-option>
          <a-select-option value="comment_reply">评论回复</a-select-option>
          <a-select-option value="system">系统通知</a-select-option>
        </a-select>

        <a-select
          v-model:value="filterProject"
          style="width: 150px"
          placeholder="项目"
          allow-clear
          show-search
        >
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>

        <a-range-picker v-model:value="dateRange" />
      </a-space>
    </a-card>

    <!-- 通知列表 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 全部通知 -->
        <a-tab-pane key="all" tab="全部通知">
          <a-list
            :data-source="notifications"
            :loading="loading"
            :pagination="pagination"
          >
            <template #renderItem="{ item }">
              <a-list-item
                class="notification-item"
                :class="{ 'unread': !item.read }"
                @click="handleNotificationClick(item)"
              >
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge :dot="!item.read">
                      <a-avatar
                        :style="{
                          backgroundColor: getNotificationColor(item.type)
                        }"
                      >
                        <component :is="getNotificationIcon(item.type)" />
                      </a-avatar>
                    </a-badge>
                  </template>

                  <template #title>
                    <div class="notification-title">
                      <strong>{{ item.title }}</strong>
                      <a-tag
                        :color="getNotificationTypeColor(item.type)"
                        size="small"
                      >
                        {{ getNotificationTypeText(item.type) }}
                      </a-tag>
                    </div>
                  </template>

                  <template #description>
                    <div class="notification-content">
                      {{ item.content }}
                    </div>
                    <div class="notification-meta">
                      <a-space split="|" size="small">
                        <span v-if="item.projectName">
                          <ProjectOutlined />
                          {{ item.projectName }}
                        </span>
                        <span>
                          <ClockCircleOutlined />
                          {{ formatRelativeTime(item.createdAt) }}
                        </span>
                        <span v-if="item.from">
                          <UserOutlined />
                          {{ item.from.name }}
                        </span>
                      </a-space>
                    </div>
                  </template>
                </a-list-item-meta>

                <template #actions>
                  <a-space>
                    <a-button
                      type="link"
                      size="small"
                      @click.stop="handleMarkAsRead(item)"
                    >
                      <CheckOutlined /> 标记已读
                    </a-button>
                    <a-dropdown>
                      <a-button type="link" size="small" @click.stop>
                        <MoreOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleOpenRelated(item)">
                            <LinkOutlined /> 打开相关内容
                          </a-menu-item>
                          <a-menu-item @click="handleMuteNotification(item)">
                            <BellOutlined /> 静音此类通知
                          </a-menu-item>
                          <a-menu-divider />
                          <a-menu-item danger @click="handleDeleteNotification(item)">
                            <DeleteOutlined /> 删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </a-space>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>

        <!-- 待处理 -->
        <a-tab-pane key="pending">
          <template #tab>
            <a-badge :count="pendingNotifications.length" :offset="[10, 0]">
              待处理
            </a-badge>
          </template>

          <a-list
            :data-source="pendingNotifications"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar
                      :style="{
                        backgroundColor: getNotificationColor(item.type)
                      }"
                    >
                      <component :is="getNotificationIcon(item.type)" />
                    </a-avatar>
                  </template>

                  <template #title>
                    {{ item.title }}
                  </template>

                  <template #description>
                    {{ item.content }}
                  </template>
                </a-list-item-meta>

                <template #actions>
                  <a-space>
                    <a-button
                      type="primary"
                      size="small"
                      @click="handleApprove(item)"
                    >
                      批准
                    </a-button>
                    <a-button
                      size="small"
                      @click="handleReject(item)"
                    >
                      拒绝
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      @click="handleViewDetail(item)"
                    >
                      查看详情
                    </a-button>
                  </a-space>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>

        <!-- @我的 -->
        <a-tab-pane key="mentions">
          <template #tab>
            <a-badge :count="mentionNotifications.length" :offset="[10, 0]">
              @我的
            </a-badge>
          </template>

          <a-list
            :data-source="mentionNotifications"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item @click="handleNotificationClick(item)">
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :src="item.from.avatar" />
                  </template>

                  <template #title>
                    <strong>{{ item.from.name }}</strong> 在
                    <a>{{ item.relatedTitle }}</a>
                    中提到了你
                  </template>

                  <template #description>
                    <div class="mention-content">
                      {{ item.content }}
                    </div>
                    <div class="notification-meta">
                      {{ formatRelativeTime(item.createdAt) }}
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>

        <!-- 系统通知 -->
        <a-tab-pane key="system" tab="系统通知">
          <a-list
            :data-source="systemNotifications"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar
                      :style="{ backgroundColor: '#1890ff' }"
                    >
                      <BellOutlined />
                    </a-avatar>
                  </template>

                  <template #title>
                    {{ item.title }}
                  </template>

                  <template #description>
                    <div>{{ item.content }}</div>
                    <div class="notification-meta">
                      {{ formatTime(item.createdAt) }}
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 通知设置抽屉 -->
    <a-drawer
      v-model:visible="settingsVisible"
      title="通知设置"
      :width="600"
    >
      <a-form layout="vertical">
        <h3>通知渠道</h3>
        <a-form-item label="站内通知">
          <a-switch v-model:checked="settings.inApp" checked-children="开启" un-checked-children="关闭" />
          <div style="color: #8c8c8c; font-size: 12px; margin-top: 8px">
            在系统内接收通知提醒
          </div>
        </a-form-item>

        <a-form-item label="邮件通知">
          <a-switch v-model:checked="settings.email" checked-children="开启" un-checked-children="关闭" />
          <div style="color: #8c8c8c; font-size: 12px; margin-top: 8px">
            通过邮件接收重要通知
          </div>
        </a-form-item>

        <a-form-item label="移动推送">
          <a-switch v-model:checked="settings.push" checked-children="开启" un-checked-children="关闭" />
          <div style="color: #8c8c8c; font-size: 12px; margin-top: 8px">
            在移动设备上接收推送通知
          </div>
        </a-form-item>

        <a-divider />

        <h3>通知类型</h3>
        <a-form-item label="任务分配">
          <a-checkbox-group v-model:value="settings.taskAssigned">
            <a-checkbox value="inApp">站内</a-checkbox>
            <a-checkbox value="email">邮件</a-checkbox>
            <a-checkbox value="push">推送</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="任务@我">
          <a-checkbox-group v-model:value="settings.taskMentioned">
            <a-checkbox value="inApp">站内</a-checkbox>
            <a-checkbox value="email">邮件</a-checkbox>
            <a-checkbox value="push">推送</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="变更请求">
          <a-checkbox-group v-model:value="settings.changeRequest">
            <a-checkbox value="inApp">站内</a-checkbox>
            <a-checkbox value="email">邮件</a-checkbox>
            <a-checkbox value="push">推送</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="评审请求">
          <a-checkbox-group v-model:value="settings.reviewRequest">
            <a-checkbox value="inApp">站内</a-checkbox>
            <a-checkbox value="email">邮件</a-checkbox>
            <a-checkbox value="push">推送</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="评论回复">
          <a-checkbox-group v-model:value="settings.commentReply">
            <a-checkbox value="inApp">站内</a-checkbox>
            <a-checkbox value="email">邮件</a-checkbox>
            <a-checkbox value="push">推送</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-divider />

        <h3>免打扰模式</h3>
        <a-form-item label="工作日免打扰时段">
          <a-time-range-picker
            v-model:value="settings.dndWorkday"
            format="HH:mm"
          />
        </a-form-item>

        <a-form-item label="周末免打扰">
          <a-switch
            v-model:checked="settings.dndWeekend"
            checked-children="开启"
            un-checked-children="关闭"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSaveSettings">
              保存设置
            </a-button>
            <a-button @click="settingsVisible = false">
              取消
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<style scoped lang="scss">
.notification-center {
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;

    h2 {
      margin: 0;
    }
  }

  .notification-item {
    cursor: pointer;
    transition: all 0.3s;

    &.unread {
      background: #f0f9ff;
      border-left: 3px solid #1890ff;
    }

    &:hover {
      background: #fafafa;
    }

    .notification-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .notification-content {
      color: #595959;
      margin: 8px 0;
      line-height: 1.6;
    }

    .notification-meta {
      color: #8c8c8c;
      font-size: 12px;
      margin-top: 4px;
    }
  }

  .mention-content {
    background: #fafafa;
    padding: 8px 12px;
    border-radius: 4px;
    margin: 8px 0;
    border-left: 3px solid #1890ff;
  }
}
</style>
```

### 5.2 实时通知提示组件

```typescript
<template>
  <!-- 通知气泡 -->
  <div class="notification-bubble" v-if="showBubble">
    <a-badge :count="unreadCount" :overflow-count="99">
      <a-button
        type="text"
        shape="circle"
        size="large"
        @click="handleOpenNotifications"
      >
        <BellOutlined />
      </a-button>
    </a-badge>

    <!-- 通知下拉面板 -->
    <div v-if="showPanel" class="notification-panel">
      <div class="panel-header">
        <span>通知</span>
        <a-space>
          <a @click="handleMarkAllRead">全部已读</a>
          <a @click="handleViewAll">查看全部</a>
        </a-space>
      </div>

      <a-tabs v-model:activeKey="panelTab" size="small">
        <a-tab-pane key="all">
          <template #tab>
            全部 <a-badge :count="unreadCount" :offset="[8, 0]" />
          </template>

          <div class="notification-list">
            <div
              v-for="notification in recentNotifications"
              :key="notification.id"
              class="notification-item-mini"
              :class="{ 'unread': !notification.read }"
              @click="handleNotificationClick(notification)"
            >
              <a-avatar
                :style="{ backgroundColor: getNotificationColor(notification.type) }"
                :size="32"
              >
                <component :is="getNotificationIcon(notification.type)" />
              </a-avatar>
              <div class="notification-body">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-content">{{ notification.content }}</div>
                <div class="notification-time">
                  {{ formatRelativeTime(notification.createdAt) }}
                </div>
              </div>
            </div>

            <a-empty
              v-if="recentNotifications.length === 0"
              description="暂无通知"
              :image="simpleImage"
            />
          </div>
        </a-tab-pane>

        <a-tab-pane key="mentions">
          <template #tab>
            @我 <a-badge :count="mentionCount" :offset="[8, 0]" />
          </template>

          <div class="notification-list">
            <!-- @我的通知列表 -->
          </div>
        </a-tab-pane>

        <a-tab-pane key="pending">
          <template #tab>
            待处理 <a-badge :count="pendingCount" :offset="[8, 0]" />
          </template>

          <div class="notification-list">
            <!-- 待处理通知列表 -->
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>

  <!-- 桌面通知 -->
  <teleport to="body">
    <transition-group name="toast-notification">
      <div
        v-for="toast in toastNotifications"
        :key="toast.id"
        class="toast-notification"
        :class="`toast-${toast.type}`"
        @click="handleToastClick(toast)"
      >
        <div class="toast-icon">
          <component :is="getNotificationIcon(toast.type)" />
        </div>
        <div class="toast-content">
          <div class="toast-title">{{ toast.title }}</div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
        <a-button
          type="text"
          size="small"
          class="toast-close"
          @click.stop="handleCloseToast(toast)"
        >
          <CloseOutlined />
        </a-button>
      </div>
    </transition-group>
  </teleport>
</template>

<style scoped lang="scss">
.notification-bubble {
  position: relative;

  .notification-panel {
    position: absolute;
    top: calc(100% + 12px);
    right: 0;
    width: 400px;
    background: #ffffff;
    border-radius: 4px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    z-index: 1000;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      font-weight: 600;
    }

    .notification-list {
      max-height: 400px;
      overflow-y: auto;

      .notification-item-mini {
        display: flex;
        gap: 12px;
        padding: 12px 16px;
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: #f5f5f5;
        }

        &.unread {
          background: #f0f9ff;
        }

        .notification-body {
          flex: 1;
          min-width: 0;

          .notification-title {
            font-weight: 600;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .notification-content {
            color: #8c8c8c;
            font-size: 12px;
            line-height: 1.5;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }

          .notification-time {
            color: #bfbfbf;
            font-size: 11px;
            margin-top: 4px;
          }
        }
      }
    }
  }
}

.toast-notification {
  position: fixed;
  top: 24px;
  right: 24px;
  width: 360px;
  background: #ffffff;
  border-radius: 4px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 2000;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  }

  &.toast-info {
    border-left: 4px solid #1890ff;
  }

  &.toast-success {
    border-left: 4px solid #52c41a;
  }

  &.toast-warning {
    border-left: 4px solid #faad14;
  }

  &.toast-error {
    border-left: 4px solid #ff4d4f;
  }

  .toast-icon {
    font-size: 24px;
  }

  .toast-content {
    flex: 1;
    min-width: 0;

    .toast-title {
      font-weight: 600;
      margin-bottom: 4px;
    }

    .toast-message {
      color: #8c8c8c;
      font-size: 12px;
      line-height: 1.5;
    }
  }

  .toast-close {
    align-self: start;
  }
}

.toast-notification-enter-active,
.toast-notification-leave-active {
  transition: all 0.3s;
}

.toast-notification-enter-from {
  transform: translateX(400px);
  opacity: 0;
}

.toast-notification-leave-to {
  transform: translateX(400px);
  opacity: 0;
}
</style>
```

---

## 6. 数据分析功能设计

### 6.1 项目分析仪表板

```typescript
<template>
  <div class="analytics-dashboard">
    <!-- 仪表板头部 -->
    <div class="dashboard-header">
      <h2>数据分析</h2>
      <a-space>
        <a-select
          v-model:value="selectedProject"
          style="width: 200px"
          placeholder="选择项目"
        >
          <a-select-option value="all">全部项目</a-select-option>
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>

        <a-range-picker
          v-model:value="dateRange"
          :presets="datePresets"
        />

        <a-button @click="handleExportReport">
          <template #icon><ExportOutlined /></template>
          导出报告
        </a-button>

        <a-button @click="handleRefreshData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 关键指标 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="项目完成率"
            :value="kpi.completionRate"
            suffix="%"
            :prefix="() => <RiseOutlined />"
            :value-style="{ color: '#52c41a' }"
          >
            <template #suffix>
              <a-tooltip title="较上周期">
                <span style="font-size: 14px; color: #52c41a">
                  ↑ {{ kpi.completionRateDelta }}%
                </span>
              </a-tooltip>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="任务按时完成率"
            :value="kpi.onTimeRate"
            suffix="%"
            :prefix="() => <ClockCircleOutlined />"
          >
            <template #suffix>
              <span
                style="font-size: 14px"
                :style="{ color: kpi.onTimeRateDelta >= 0 ? '#52c41a' : '#ff4d4f' }"
              >
                {{ kpi.onTimeRateDelta >= 0 ? '↑' : '↓' }}
                {{ Math.abs(kpi.onTimeRateDelta) }}%
              </span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="团队协作效率"
            :value="kpi.collaborationScore"
            :prefix="() => <TeamOutlined />"
            :value-style="{ color: '#1890ff' }"
          >
            <template #footer>
              <span style="font-size: 12px; color: #8c8c8c">
                目标: 提升50%
              </span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="测试用例复用率"
            :value="kpi.testReuseRate"
            suffix="%"
            :prefix="() => <ExperimentOutlined />"
            :value-style="kpi.testReuseRate >= 70 ? { color: '#52c41a' } : { color: '#faad14' }"
          >
            <template #footer>
              <span style="font-size: 12px; color: #8c8c8c">
                目标: 70%+
              </span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <!-- 任务趋势 -->
      <a-col :xs="24" :lg="12">
        <a-card title="任务完成趋势" :bordered="false">
          <template #extra>
            <a-radio-group v-model:value="taskTrendPeriod" button-style="solid" size="small">
              <a-radio-button value="week">周</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="quarter">季度</a-radio-button>
            </a-radio-group>
          </template>

          <TaskTrendChart
            :data="taskTrendData"
            :period="taskTrendPeriod"
            :height="300"
          />
        </a-card>
      </a-col>

      <!-- 项目进度 -->
      <a-col :xs="24" :lg="12">
        <a-card title="项目进度概览" :bordered="false">
          <template #extra>
            <a-button type="link" @click="handleViewProjectDetails">
              查看详情 →
            </a-button>
          </template>

          <a-list :data-source="projectProgress" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar
                      :style="{ backgroundColor: item.color }"
                      shape="square"
                    >
                      {{ item.name.charAt(0) }}
                    </a-avatar>
                  </template>
                  <template #title>
                    {{ item.name }}
                  </template>
                  <template #description>
                    <a-progress
                      :percent="item.progress"
                      :status="getProgressStatus(item)"
                      size="small"
                    />
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 团队工作量 -->
      <a-col :xs="24" :lg="12">
        <a-card title="团队工作量分布" :bordered="false">
          <TeamWorkloadChart
            :data="teamWorkloadData"
            :height="300"
          />
        </a-card>
      </a-col>

      <!-- 缺陷分析 -->
      <a-col :xs="24" :lg="12">
        <a-card title="缺陷统计分析" :bordered="false">
          <template #extra>
            <a-select v-model:value="bugChartType" size="small" style="width: 120px">
              <a-select-option value="severity">按严重程度</a-select-option>
              <a-select-option value="module">按模块</a-select-option>
              <a-select-option value="status">按状态</a-select-option>
            </a-select>
          </template>

          <BugAnalysisChart
            :data="bugAnalysisData"
            :type="bugChartType"
            :height="300"
          />
        </a-card>
      </a-col>

      <!-- 文档活跃度 -->
      <a-col :xs="24" :lg="12">
        <a-card title="文档活跃度" :bordered="false">
          <DocumentActivityChart
            :data="documentActivityData"
            :height="300"
          />

          <a-divider />

          <a-row :gutter="16}>
            <a-col :span="8">
              <a-statistic
                title="总文档数"
                :value="documentStats.total"
                :prefix="() => <FileTextOutlined />"
              />
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="今日更新"
                :value="documentStats.todayUpdated"
                :prefix="() => <EditOutlined />"
              />
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="平均查看次数"
                :value="documentStats.avgViews"
                :prefix="() => <EyeOutlined />"
              />
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <!-- 变更影响分析 -->
      <a-col :xs="24" :lg="12">
        <a-card title="变更影响分析" :bordered="false">
          <ChangeImpactChart
            :data="changeImpactData"
            :height="300"
          />

          <a-divider />

          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="本周变更">
              {{ changeStats.weeklyChanges }}
            </a-descriptions-item>
            <a-descriptions-item label="自动通知率">
              {{ changeStats.autoNotifyRate }}%
            </a-descriptions-item>
            <a-descriptions-item label="平均响应时间">
              {{ changeStats.avgResponseTime }}h
            </a-descriptions-item>
            <a-descriptions-item label="重大变更">
              {{ changeStats.criticalChanges }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表格 -->
    <a-card title="详细数据" :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="detailTab">
        <a-tab-pane key="tasks" tab="任务明细">
          <TaskDetailTable :project-id="selectedProject" :date-range="dateRange" />
        </a-tab-pane>

        <a-tab-pane key="members" tab="成员贡献">
          <MemberContributionTable :project-id="selectedProject" :date-range="dateRange" />
        </a-tab-pane>

        <a-tab-pane key="quality" tab="质量指标">
          <QualityMetricsTable :project-id="selectedProject" :date-range="dateRange" />
        </a-tab-pane>

        <a-tab-pane key="timeline" tab="时间线分析">
          <TimelineAnalysisChart :project-id="selectedProject" :date-range="dateRange" />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 自定义报告 -->
    <a-card title="自定义报告" :bordered="false" style="margin-top: 16px">
      <a-space>
        <a-button type="primary" @click="handleCreateReport">
          <template #icon><PlusOutlined /></template>
          创建报告
        </a-button>
        <a-button @click="handleScheduleReport">
          <template #icon><ClockCircleOutlined /></template>
          定时报告
        </a-button>
      </a-space>

      <a-divider />

      <a-list
        :data-source="customReports"
        :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card
              hoverable
              size="small"
              @click="handleViewReport(item)"
            >
              <a-card-meta
                :title="item.name"
                :description="item.description"
              >
                <template #avatar>
                  <FileTextOutlined style="font-size: 24px; color: #1890ff" />
                </template>
              </a-card-meta>

              <template #actions>
                <a @click.stop="handleEditReport(item)">
                  <EditOutlined /> 编辑
                </a>
                <a @click.stop="handleExportReport(item)">
                  <ExportOutlined /> 导出
                </a>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<style scoped lang="scss">
.analytics-dashboard {
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;

    h2 {
      margin: 0;
    }
  }
}
</style>
```

---

## 7. 核心功能设计总结

### 7.1 功能设计对比表

| 功能模块 | 核心价值 | 关键特性 | 效率目标 |
|---------|---------|---------|---------|
| 文档管理 | 单一真实数据源 | 版本控制、实时协作、智能搜索 | 信息查找时间 -60% |
| 任务管理 | 高效协作执行 | 看板视图、甘特图、拖拽排序 | 任务管理效率 +30% |
| 变更管理 | 自动影响分析 | 智能通知、审批流程、影响追踪 | 变更响应时间 -40% |
| 智能搜索 | 快速信息获取 | 全文检索、智能建议、高级筛选 | 搜索准确率 90%+ |
| 通知系统 | 及时信息同步 | 多渠道推送、智能聚合、免打扰 | 信息遗漏率 <5% |
| 数据分析 | 决策支持 | 可视化图表、趋势分析、自定义报告 | 数据洞察时间 -50% |

### 7.2 用户体验优化要点

1. **一致性**: 所有功能模块使用统一的设计语言和交互模式
2. **响应性**: 关键操作响应时间 < 300ms，页面加载 < 3秒
3. **可访问性**: 符合 WCAG 2.1 AA 标准，支持键盘导航
4. **容错性**: 友好的错误提示和恢复机制
5. **渐进式**: 支持从简单到复杂的功能发现路径

### 7.3 性能优化策略

```typescript
// 1. 虚拟滚动 - 大数据列表
<a-table :virtual="true" :scroll="{ y: 600 }" />

// 2. 懒加载 - 图片和组件
<img v-lazy="imageUrl" />
const ComponentLazy = defineAsyncComponent(() => import('./Component.vue'))

// 3. 防抖节流 - 搜索和滚动
import { useDebounceFn } from '@vueuse/core'
const handleSearch = useDebounceFn((value) => {
  // 搜索逻辑
}, 300)

// 4. 缓存策略 - API响应
import { useQuery } from '@tanstack/vue-query'
const { data } = useQuery({
  queryKey: ['projects'],
  queryFn: fetchProjects,
  staleTime: 5 * 60 * 1000 // 5分钟缓存
})

// 5. 代码分割 - 路由级别
const routes = [
  {
    path: '/documents',
    component: () => import('@/views/Documents.vue')
  }
]
```

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**下一部分**: Part 6 - 可用性和性能设计规范