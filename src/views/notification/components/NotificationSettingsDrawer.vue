<template>
  <a-drawer
    v-model:open="visible"
    title="通知设置"
    placement="right"
    width="480"
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="notification-settings">
        <a-form :model="formData" layout="vertical">
          <!-- 通知渠道 -->
          <section class="settings-section">
            <h3 class="section-title">通知渠道</h3>
            <a-form-item label="站内通知">
              <a-switch v-model:checked="formData.channels.inApp" />
            </a-form-item>
            <a-form-item label="邮件通知">
              <a-switch v-model:checked="formData.channels.email" />
            </a-form-item>
            <a-form-item label="移动推送">
              <a-switch v-model:checked="formData.channels.push" />
            </a-form-item>
          </section>

          <a-divider />

          <!-- 通知类型配置 -->
          <section class="settings-section">
            <h3 class="section-title">通知类型</h3>

            <div v-for="(config, type) in formData.types" :key="type" class="notification-type-item">
              <div class="type-header">
                <a-checkbox v-model:checked="config.enabled">
                  {{ getTypeText(type) }}
                </a-checkbox>
              </div>
              <div v-if="config.enabled" class="type-channels">
                <a-checkbox-group v-model:value="channelValues[type]" @change="handleChannelChange(type, $event)">
                  <a-checkbox value="inApp">站内</a-checkbox>
                  <a-checkbox value="email">邮件</a-checkbox>
                  <a-checkbox value="push">推送</a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </section>

          <a-divider />

          <!-- 免打扰设置 -->
          <section class="settings-section">
            <h3 class="section-title">免打扰模式</h3>
            <a-form-item label="启用免打扰">
              <a-switch v-model:checked="formData.doNotDisturb.enabled" />
            </a-form-item>

            <template v-if="formData.doNotDisturb.enabled">
              <a-form-item label="工作日免打扰时段">
                <a-time-range-picker
                  v-model:value="doNotDisturbTime"
                  format="HH:mm"
                  :placeholder="['开始时间', '结束时间']"
                  style="width: 100%"
                />
              </a-form-item>

              <a-form-item label="周末免打扰">
                <a-switch v-model:checked="formData.doNotDisturb.weekendEnabled" />
              </a-form-item>
            </template>
          </section>

          <a-divider />

          <!-- 其他设置 -->
          <section class="settings-section">
            <h3 class="section-title">其他设置</h3>
            <a-form-item label="通知音效">
              <a-switch v-model:checked="formData.sound" />
            </a-form-item>
            <a-form-item label="浏览器通知">
              <div class="browser-notification-item">
                <a-switch v-model:checked="formData.browserNotification" @change="handleBrowserNotificationChange" />
                <a-button
                  v-if="formData.browserNotification && browserPermission !== 'granted'"
                  type="link"
                  size="small"
                  @click="requestBrowserPermission"
                >
                  授权浏览器通知
                </a-button>
              </div>
            </a-form-item>
          </section>
        </a-form>
      </div>
    </a-spin>

    <template #footer>
      <div class="drawer-footer">
        <a-space>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleClose">取消</a-button>
          <a-button type="primary" :loading="saving" @click="handleSave">
            保存
          </a-button>
        </a-space>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { type Dayjs } from 'dayjs';
import { useNotificationStore } from '@/stores/modules/notification';
import { notificationTypeTexts } from '@/utils/notification';
import { requestNotificationPermission } from '@/utils/notification';
import type { NotificationSettings, NotificationType } from '@/types/notification';

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const notificationStore = useNotificationStore();

const visible = ref(false);
const loading = ref(false);
const saving = ref(false);
const browserPermission = ref<NotificationPermission>('default');

const formData = reactive<NotificationSettings>({
  channels: {
    inApp: true,
    email: true,
    push: false
  },
  types: {
    task_assigned: {
      enabled: true,
      channels: { inApp: true, email: true, push: false }
    },
    task_mentioned: {
      enabled: true,
      channels: { inApp: true, email: true, push: false }
    },
    change_request: {
      enabled: true,
      channels: { inApp: true, email: true, push: false }
    },
    review_request: {
      enabled: true,
      channels: { inApp: true, email: false, push: false }
    },
    approval_pending: {
      enabled: true,
      channels: { inApp: true, email: true, push: false }
    },
    comment_reply: {
      enabled: true,
      channels: { inApp: true, email: false, push: false }
    },
    system: {
      enabled: true,
      channels: { inApp: true, email: false, push: false }
    },
    deadline_reminder: {
      enabled: true,
      channels: { inApp: true, email: true, push: true }
    },
    status_changed: {
      enabled: true,
      channels: { inApp: true, email: false, push: false }
    },
    priority_changed: {
      enabled: true,
      channels: { inApp: true, email: false, push: false }
    }
  },
  doNotDisturb: {
    enabled: false,
    workdayStart: '22:00',
    workdayEnd: '08:00',
    weekendEnabled: false
  },
  sound: true,
  browserNotification: false
});

const doNotDisturbTime = ref<[Dayjs, Dayjs] | null>(null);

// 用于双向绑定的通道选择值
const channelValues = reactive<Record<string, string[]>>({});

const getTypeText = (type: string) => {
  return notificationTypeTexts[type as NotificationType] || type;
};

const initChannelValues = () => {
  Object.keys(formData.types).forEach(type => {
    const config = formData.types[type as NotificationType];
    if (config) {
      const values: string[] = [];
      if (config.channels.inApp) values.push('inApp');
      if (config.channels.email) values.push('email');
      if (config.channels.push) values.push('push');
      channelValues[type] = values;
    }
  });
};

const handleChannelChange = (type: string, values: string[]) => {
  const config = formData.types[type as NotificationType];
  if (config) {
    config.channels.inApp = values.includes('inApp');
    config.channels.email = values.includes('email');
    config.channels.push = values.includes('push');
  }
};

const loadSettings = async () => {
  loading.value = true;
  try {
    await notificationStore.fetchSettings();
    if (notificationStore.settings) {
      Object.assign(formData, notificationStore.settings);

      // 初始化免打扰时间
      if (formData.doNotDisturb.workdayStart && formData.doNotDisturb.workdayEnd) {
        doNotDisturbTime.value = [
          dayjs(formData.doNotDisturb.workdayStart, 'HH:mm'),
          dayjs(formData.doNotDisturb.workdayEnd, 'HH:mm')
        ];
      }

      initChannelValues();
    }

    // 检查浏览器通知权限
    if ('Notification' in window) {
      browserPermission.value = Notification.permission;
    }
  } catch (error) {
    message.error('加载设置失败');
  } finally {
    loading.value = false;
  }
};

const handleBrowserNotificationChange = async (checked: boolean) => {
  if (checked && browserPermission.value !== 'granted') {
    const permission = await requestBrowserPermission();
    if (permission !== 'granted') {
      formData.browserNotification = false;
      message.warning('请先授权浏览器通知权限');
    } else {
      browserPermission.value = permission;
    }
  }
};

const requestBrowserPermission = async () => {
  const permission = await requestNotificationPermission();
  browserPermission.value = permission;
  if (permission === 'granted') {
    message.success('浏览器通知权限已授权');
  } else {
    message.error('浏览器通知权限被拒绝');
  }
};

const handleSave = async () => {
  // 更新免打扰时间
  if (doNotDisturbTime.value) {
    formData.doNotDisturb.workdayStart = doNotDisturbTime.value[0].format('HH:mm');
    formData.doNotDisturb.workdayEnd = doNotDisturbTime.value[1].format('HH:mm');
  }

  saving.value = true;
  try {
    await notificationStore.updateSettings(formData);
    message.success('保存成功');
    emit('saved');
    handleClose();
  } catch (error) {
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
};

const handleReset = async () => {
  await loadSettings();
  message.success('已重置');
};

const handleClose = () => {
  visible.value = false;
  emit('close');
};

const open = () => {
  visible.value = true;
  loadSettings();
};

defineExpose({
  open
});
</script>

<style scoped lang="scss">
.notification-settings {
  padding-bottom: 80px;
}

.settings-section {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0 0 16px 0;
  }
}

.notification-type-item {
  margin-bottom: 20px;

  .type-header {
    margin-bottom: 8px;
  }

  .type-channels {
    padding-left: 24px;

    :deep(.ant-checkbox-group) {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
  }
}

.browser-notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid var(--color-border-secondary);
}
</style>
