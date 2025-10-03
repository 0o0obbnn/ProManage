# ProManage UI/UX 设计文档 - Part 3: 用户角色界面设计规范

## 1. 角色导向设计概述

### 1.1 设计原则
```
差异化体验 (Differentiated Experience)
- 每个角色看到最相关的信息和功能
- 减少无关信息的干扰
- 提升特定角色的工作效率

个性化工作空间 (Personalized Workspace)
- 角色主题色标识
- 定制化仪表板布局
- 快捷操作面板

权限可视化 (Permission Visibility)
- 清晰展示可操作范围
- 不可用功能灰化处理
- 权限提示友好明确
```

### 1.2 角色效率目标
```
Super Administrator:    管理效率提升 50%
Project Manager:        项目延期减少 25%
Developer:             开发效率提升 20%
Tester:                测试效率提升 40%
UI Designer:           设计评审周期缩短 50%
Operations:            部署成功率提升 15%
Third-party Personnel:  零数据泄露风险
```

---

## 2. Super Administrator (超级管理员) 界面设计

### 2.1 角色特征
```
主要职责:
- 系统配置与管理
- 用户权限管理
- 组织架构管理
- 系统监控与维护
- 数据备份与恢复

关键需求:
- 全局视图和控制能力
- 快速响应系统问题
- 批量操作能力
- 详细的操作日志
```

### 2.2 仪表板设计

```typescript
// 超级管理员仪表板布局
<template>
  <div class="admin-dashboard">
    <!-- 顶部关键指标 -->
    <a-row :gutter="16" class="metrics-row">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="系统用户"
            :value="stats.totalUsers"
            :prefix="() => <TeamOutlined />"
          >
            <template #suffix>
              <a-tag color="green">+12</a-tag>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="活跃项目"
            :value="stats.activeProjects"
            :prefix="() => <ProjectOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="系统可用性"
            :value="99.9"
            :precision="2"
            suffix="%"
            :value-style="{ color: '#52c41a' }"
            :prefix="() => <CheckCircleOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="存储使用"
            :value="stats.storageUsage"
            suffix="/ 1TB"
          >
            <template #suffix>
              <a-progress
                :percent="(stats.storageUsage / 1024) * 100"
                :show-info="false"
                size="small"
              />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 系统监控图表 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="16">
        <a-card title="系统性能监控" :bordered="false">
          <template #extra>
            <a-radio-group v-model:value="timeRange" button-style="solid">
              <a-radio-button value="1h">1小时</a-radio-button>
              <a-radio-button value="24h">24小时</a-radio-button>
              <a-radio-button value="7d">7天</a-radio-button>
            </a-radio-group>
          </template>

          <!-- CPU、内存、网络使用率图表 -->
          <SystemPerformanceChart :time-range="timeRange" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card title="系统健康状态" :bordered="false">
          <a-list :data-source="healthChecks" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge
                      :status="item.status === 'healthy' ? 'success' : 'error'"
                    />
                  </template>
                  <template #title>{{ item.name }}</template>
                  <template #description>
                    <span :class="item.status === 'healthy' ? 'text-success' : 'text-error'">
                      {{ item.message }}
                    </span>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户活动和操作日志 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="12">
        <a-card title="最近用户活动" :bordered="false">
          <template #extra>
            <a-button type="link" @click="viewAllActivities">
              查看全部
            </a-button>
          </template>

          <a-timeline>
            <a-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :color="getActivityColor(activity.type)"
            >
              <p>
                <a-avatar :size="24" :src="activity.user.avatar" />
                <strong>{{ activity.user.name }}</strong>
                {{ activity.action }}
              </p>
              <p class="activity-time">{{ formatTime(activity.createdAt) }}</p>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="系统操作日志" :bordered="false">
          <template #extra>
            <a-button type="link" @click="exportLogs">
              导出日志
            </a-button>
          </template>

          <a-table
            :columns="logColumns"
            :data-source="systemLogs"
            :pagination="{ pageSize: 5 }"
            size="small"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.admin-dashboard {
  .metrics-row {
    .ant-card {
      border-left: 3px solid #722ed1; // Super Admin 主题色
    }
  }

  .text-success {
    color: #52c41a;
  }

  .text-error {
    color: #ff4d4f;
  }

  .activity-time {
    color: #8c8c8c;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>
```

### 2.3 用户管理界面

```typescript
<template>
  <div class="user-management">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <TeamOutlined />
          <span>用户管理</span>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAddUser">
            <template #icon><UserAddOutlined /></template>
            添加用户
          </a-button>
          <a-button @click="handleBatchImport">
            <template #icon><ImportOutlined /></template>
            批量导入
          </a-button>
          <a-button @click="handleExportUsers">
            <template #icon><ExportOutlined /></template>
            导出用户
          </a-button>
        </a-space>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="8">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索用户名、邮箱、手机号"
              @search="handleSearch"
            />
          </a-col>

          <a-col :xs="24" :sm="4">
            <a-select
              v-model:value="filterRole"
              placeholder="角色筛选"
              style="width: 100%"
              allow-clear
            >
              <a-select-option value="admin">超级管理员</a-select-option>
              <a-select-option value="pm">项目经理</a-select-option>
              <a-select-option value="developer">开发人员</a-select-option>
              <a-select-option value="tester">测试人员</a-select-option>
              <a-select-option value="designer">UI设计师</a-select-option>
              <a-select-option value="ops">运维人员</a-select-option>
              <a-select-option value="external">第三方人员</a-select-option>
            </a-select>
          </a-col>

          <a-col :xs="24" :sm="4">
            <a-select
              v-model:value="filterStatus"
              placeholder="状态筛选"
              style="width: 100%"
              allow-clear
            >
              <a-select-option value="active">活跃</a-select-option>
              <a-select-option value="inactive">未激活</a-select-option>
              <a-select-option value="locked">已锁定</a-select-option>
            </a-select>
          </a-col>
        </a-row>
      </div>

      <!-- 用户表格 -->
      <a-table
        :columns="userColumns"
        :data-source="users"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="handleTableChange"
      >
        <!-- 用户信息列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <a-space>
              <a-avatar :src="record.avatar" />
              <div>
                <div>{{ record.name }}</div>
                <div style="color: #8c8c8c; font-size: 12px">
                  {{ record.email }}
                </div>
              </div>
            </a-space>
          </template>

          <!-- 角色列 -->
          <template v-else-if="column.key === 'role'">
            <a-tag :color="getRoleColor(record.role)">
              {{ getRoleLabel(record.role) }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getStatusBadge(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 'active'"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 'active' ? '禁用' : '启用' }}
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleResetPassword(record)">
                      重置密码
                    </a-menu-item>
                    <a-menu-item @click="handleViewLogs(record)">
                      查看日志
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger @click="handleDelete(record)">
                      删除用户
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑用户对话框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      :width="600"
      @ok="handleSubmit"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formData.username" />
        </a-form-item>

        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formData.email" />
        </a-form-item>

        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" />
        </a-form-item>

        <a-form-item label="角色" name="role">
          <a-select v-model:value="formData.role">
            <a-select-option value="admin">超级管理员</a-select-option>
            <a-select-option value="pm">项目经理</a-select-option>
            <a-select-option value="developer">开发人员</a-select-option>
            <a-select-option value="tester">测试人员</a-select-option>
            <a-select-option value="designer">UI设计师</a-select-option>
            <a-select-option value="ops">运维人员</a-select-option>
            <a-select-option value="external">第三方人员</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="所属组织" name="organizationId">
          <a-tree-select
            v-model:value="formData.organizationId"
            :tree-data="organizationTree"
            placeholder="请选择组织"
          />
        </a-form-item>

        <a-form-item label="权限组" name="permissionGroups">
          <a-select
            v-model:value="formData.permissionGroups"
            mode="multiple"
            placeholder="选择权限组"
          >
            <a-select-option
              v-for="group in permissionGroups"
              :key="group.id"
              :value="group.id"
            >
              {{ group.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped lang="scss">
.user-management {
  .search-bar {
    margin-bottom: 16px;
  }
}
</style>
```

### 2.4 系统配置界面

```typescript
<template>
  <div class="system-settings">
    <a-tabs v-model:activeKey="activeTab" type="card">
      <!-- 基础设置 -->
      <a-tab-pane key="basic" tab="基础设置">
        <a-card title="系统信息" :bordered="false">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 12 }">
            <a-form-item label="系统名称">
              <a-input v-model:value="settings.systemName" />
            </a-form-item>

            <a-form-item label="系统Logo">
              <a-upload
                :show-upload-list="false"
                :before-upload="handleUploadLogo"
              >
                <img
                  v-if="settings.logo"
                  :src="settings.logo"
                  style="width: 120px"
                />
                <a-button v-else>
                  <UploadOutlined /> 上传Logo
                </a-button>
              </a-upload>
            </a-form-item>

            <a-form-item label="时区设置">
              <a-select v-model:value="settings.timezone">
                <a-select-option value="Asia/Shanghai">
                  中国标准时间 (UTC+8)
                </a-select-option>
                <a-select-option value="UTC">
                  协调世界时 (UTC)
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="语言">
              <a-select v-model:value="settings.language">
                <a-select-option value="zh-CN">简体中文</a-select-option>
                <a-select-option value="en-US">English</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <!-- 安全设置 -->
      <a-tab-pane key="security" tab="安全设置">
        <a-card title="登录安全" :bordered="false">
          <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
            <a-form-item label="密码策略">
              <a-space direction="vertical" style="width: 100%">
                <a-checkbox v-model:checked="settings.security.requireUppercase">
                  必须包含大写字母
                </a-checkbox>
                <a-checkbox v-model:checked="settings.security.requireLowercase">
                  必须包含小写字母
                </a-checkbox>
                <a-checkbox v-model:checked="settings.security.requireNumber">
                  必须包含数字
                </a-checkbox>
                <a-checkbox v-model:checked="settings.security.requireSpecialChar">
                  必须包含特殊字符
                </a-checkbox>
                <a-input-number
                  v-model:value="settings.security.minPasswordLength"
                  :min="6"
                  :max="32"
                  addon-before="最小长度"
                />
              </a-space>
            </a-form-item>

            <a-form-item label="会话超时">
              <a-input-number
                v-model:value="settings.security.sessionTimeout"
                :min="5"
                :max="1440"
                addon-after="分钟"
              />
            </a-form-item>

            <a-form-item label="最大登录尝试">
              <a-input-number
                v-model:value="settings.security.maxLoginAttempts"
                :min="3"
                :max="10"
                addon-after="次"
              />
            </a-form-item>

            <a-form-item label="双因素认证">
              <a-switch v-model:checked="settings.security.enable2FA" />
              <span style="margin-left: 8px; color: #8c8c8c">
                启用后所有用户需要绑定2FA
              </span>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <!-- 邮件设置 -->
      <a-tab-pane key="email" tab="邮件设置">
        <a-card title="SMTP配置" :bordered="false">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 12 }">
            <a-form-item label="SMTP服务器">
              <a-input v-model:value="settings.email.smtpHost" />
            </a-form-item>

            <a-form-item label="端口">
              <a-input-number
                v-model:value="settings.email.smtpPort"
                :min="1"
                :max="65535"
              />
            </a-form-item>

            <a-form-item label="发件人邮箱">
              <a-input v-model:value="settings.email.fromEmail" />
            </a-form-item>

            <a-form-item label="发件人名称">
              <a-input v-model:value="settings.email.fromName" />
            </a-form-item>

            <a-form-item label="认证用户名">
              <a-input v-model:value="settings.email.username" />
            </a-form-item>

            <a-form-item label="认证密码">
              <a-input-password v-model:value="settings.email.password" />
            </a-form-item>

            <a-form-item label="启用SSL">
              <a-switch v-model:checked="settings.email.enableSSL" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 4 }">
              <a-space>
                <a-button type="primary" @click="handleTestEmail">
                  发送测试邮件
                </a-button>
                <a-button @click="handleSaveEmailSettings">
                  保存设置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <!-- 存储设置 -->
      <a-tab-pane key="storage" tab="存储设置">
        <a-card title="文件存储配置" :bordered="false">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 12 }">
            <a-form-item label="存储类型">
              <a-radio-group v-model:value="settings.storage.type">
                <a-radio value="local">本地存储</a-radio>
                <a-radio value="s3">AWS S3</a-radio>
                <a-radio value="oss">阿里云OSS</a-radio>
              </a-radio-group>
            </a-form-item>

            <template v-if="settings.storage.type === 's3'">
              <a-form-item label="Access Key">
                <a-input v-model:value="settings.storage.s3.accessKey" />
              </a-form-item>

              <a-form-item label="Secret Key">
                <a-input-password v-model:value="settings.storage.s3.secretKey" />
              </a-form-item>

              <a-form-item label="Bucket">
                <a-input v-model:value="settings.storage.s3.bucket" />
              </a-form-item>

              <a-form-item label="Region">
                <a-input v-model:value="settings.storage.s3.region" />
              </a-form-item>
            </template>

            <a-form-item label="最大文件大小">
              <a-input-number
                v-model:value="settings.storage.maxFileSize"
                :min="1"
                :max="1024"
                addon-after="MB"
              />
            </a-form-item>

            <a-form-item label="允许的文件类型">
              <a-select
                v-model:value="settings.storage.allowedFileTypes"
                mode="tags"
                placeholder="输入文件扩展名"
              />
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <!-- 备份设置 -->
      <a-tab-pane key="backup" tab="数据备份">
        <a-card title="自动备份设置" :bordered="false">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 12 }">
            <a-form-item label="启用自动备份">
              <a-switch v-model:checked="settings.backup.enabled" />
            </a-form-item>

            <a-form-item label="备份频率">
              <a-select v-model:value="settings.backup.frequency">
                <a-select-option value="daily">每天</a-select-option>
                <a-select-option value="weekly">每周</a-select-option>
                <a-select-option value="monthly">每月</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="备份时间">
              <a-time-picker
                v-model:value="settings.backup.time"
                format="HH:mm"
              />
            </a-form-item>

            <a-form-item label="保留份数">
              <a-input-number
                v-model:value="settings.backup.retentionCount"
                :min="1"
                :max="30"
              />
            </a-form-item>
          </a-form>
        </a-card>

        <a-card title="备份历史" :bordered="false" style="margin-top: 16px">
          <a-table
            :columns="backupColumns"
            :data-source="backupHistory"
            :pagination="{ pageSize: 10 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a-button type="link" size="small" @click="handleRestore(record)">
                    恢复
                  </a-button>
                  <a-button type="link" size="small" @click="handleDownloadBackup(record)">
                    下载
                  </a-button>
                  <a-button type="link" size="small" danger @click="handleDeleteBackup(record)">
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>

          <a-button
            type="primary"
            style="margin-top: 16px"
            @click="handleManualBackup"
          >
            立即备份
          </a-button>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <!-- 保存按钮 -->
    <div class="settings-footer">
      <a-space>
        <a-button type="primary" size="large" @click="handleSaveSettings">
          保存设置
        </a-button>
        <a-button size="large" @click="handleResetSettings">
          重置
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<style scoped lang="scss">
.system-settings {
  .settings-footer {
    position: fixed;
    bottom: 0;
    right: 0;
    width: calc(100% - 200px); // 减去侧边栏宽度
    padding: 16px 24px;
    background: #ffffff;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
    text-align: right;
    z-index: 100;
  }
}
</style>
```

---

## 3. Project Manager (项目经理) 界面设计

### 3.1 角色特征
```
主要职责:
- 项目规划与协调
- 资源分配与管理
- 进度跟踪与控制
- 风险识别与管理
- 团队沟通与协作

关键需求:
- 项目全局视图
- 资源利用率可视化
- 任务分配与跟踪
- 审批流程处理
- 报表生成与导出
```

### 3.2 项目经理仪表板

```typescript
<template>
  <div class="pm-dashboard">
    <!-- 顶部快捷指标 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card" :bordered="false">
          <a-statistic
            title="管理项目"
            :value="stats.totalProjects"
            :prefix="() => <ProjectOutlined />"
          >
            <template #suffix>
              <a-tag color="blue">{{ stats.activeProjects }} 进行中</a-tag>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card" :bordered="false">
          <a-statistic
            title="待审批"
            :value="stats.pendingApprovals"
            :prefix="() => <ClockCircleOutlined />"
            :value-style="stats.pendingApprovals > 0 ? { color: '#fa8c16' } : {}"
          >
            <template #suffix>
              <a-button type="link" size="small" @click="gotoApprovals">
                立即处理
              </a-button>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card" :bordered="false">
          <a-statistic
            title="团队成员"
            :value="stats.teamMembers"
            :prefix="() => <TeamOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card" :bordered="false">
          <a-statistic
            title="整体进度"
            :value="stats.overallProgress"
            suffix="%"
            :prefix="() => <RiseOutlined />"
            :value-style="{ color: '#52c41a' }"
          >
            <template #suffix>
              <a-progress
                :percent="stats.overallProgress"
                :show-info="false"
                size="small"
              />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 项目概览 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="16">
        <a-card title="项目组合视图" :bordered="false">
          <template #extra>
            <a-radio-group v-model:value="viewMode" button-style="solid">
              <a-radio-button value="list">
                <UnorderedListOutlined /> 列表
              </a-radio-button>
              <a-radio-button value="kanban">
                <AppstoreOutlined /> 看板
              </a-radio-button>
              <a-radio-button value="gantt">
                <BarChartOutlined /> 甘特图
              </a-radio-button>
            </a-radio-group>
          </template>

          <!-- 项目列表视图 -->
          <template v-if="viewMode === 'list'">
            <a-list
              :data-source="projects"
              :pagination="{ pageSize: 5 }"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #avatar>
                      <a-avatar
                        :style="{ backgroundColor: item.color }"
                        shape="square"
                        :size="48"
                      >
                        {{ item.name.charAt(0) }}
                      </a-avatar>
                    </template>

                    <template #title>
                      <a @click="gotoProject(item.id)">{{ item.name }}</a>
                      <a-tag
                        :color="getStatusColor(item.status)"
                        style="margin-left: 8px"
                      >
                        {{ getStatusText(item.status) }}
                      </a-tag>
                    </template>

                    <template #description>
                      <a-space direction="vertical" style="width: 100%">
                        <div>
                          <TeamOutlined />
                          <span style="margin-left: 4px">
                            {{ item.teamSize }} 人
                          </span>
                          <CalendarOutlined style="margin-left: 16px" />
                          <span style="margin-left: 4px">
                            {{ formatDateRange(item.startDate, item.endDate) }}
                          </span>
                        </div>

                        <div>
                          <span style="color: #262626; font-weight: 500">
                            进度: {{ item.progress }}%
                          </span>
                          <a-progress
                            :percent="item.progress"
                            :status="getProgressStatus(item)"
                            size="small"
                            style="margin-top: 4px"
                          />
                        </div>
                      </a-space>
                    </template>
                  </a-list-item-meta>

                  <template #actions>
                    <a-button type="link" @click="gotoProject(item.id)">
                      查看详情
                    </a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </template>

          <!-- 看板视图 -->
          <template v-else-if="viewMode === 'kanban'">
            <ProjectKanbanBoard :projects="projects" />
          </template>

          <!-- 甘特图视图 -->
          <template v-else-if="viewMode === 'gantt'">
            <ProjectGanttChart :projects="projects" />
          </template>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <!-- 待办事项 -->
        <a-card title="我的待办" :bordered="false" style="margin-bottom: 16px">
          <template #extra>
            <a-badge :count="todos.length" />
          </template>

          <a-list
            :data-source="todos"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-checkbox
                  v-model:checked="item.completed"
                  @change="handleTodoChange(item)"
                >
                  <span :class="{ 'todo-completed': item.completed }">
                    {{ item.title }}
                  </span>
                </a-checkbox>
                <template #extra>
                  <a-tag v-if="item.priority === 'high'" color="red">高</a-tag>
                  <a-tag v-else-if="item.priority === 'medium'" color="orange">
                    中
                  </a-tag>
                </template>
              </a-list-item>
            </template>
          </a-list>

          <a-button
            type="dashed"
            block
            style="margin-top: 8px"
            @click="handleAddTodo"
          >
            <PlusOutlined /> 添加待办
          </a-button>
        </a-card>

        <!-- 资源利用率 -->
        <a-card title="资源利用率" :bordered="false">
          <a-list
            :data-source="resourceUtilization"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space direction="vertical" style="width: 100%">
                  <div style="display: flex; justify-content: space-between">
                    <span>
                      <a-avatar :size="24" :src="item.avatar" />
                      {{ item.name }}
                    </span>
                    <span :style="{ color: getUtilizationColor(item.utilization) }">
                      {{ item.utilization }}%
                    </span>
                  </div>
                  <a-progress
                    :percent="item.utilization"
                    :stroke-color="getUtilizationColor(item.utilization)"
                    size="small"
                  />
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 审批流程 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="待我审批" :bordered="false">
          <template #extra>
            <a-button type="link" @click="viewAllApprovals">
              查看全部
            </a-button>
          </template>

          <a-table
            :columns="approvalColumns"
            :data-source="pendingApprovals"
            :pagination="{ pageSize: 5 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <a-tag :color="getApprovalTypeColor(record.type)">
                  {{ getApprovalTypeText(record.type) }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button
                    type="primary"
                    size="small"
                    @click="handleApprove(record)"
                  >
                    批准
                  </a-button>
                  <a-button
                    danger
                    size="small"
                    @click="handleReject(record)"
                  >
                    拒绝
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleViewDetail(record)"
                  >
                    查看详情
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.pm-dashboard {
  .metric-card {
    border-left: 3px solid #1890ff; // Project Manager 主题色
  }

  .todo-completed {
    text-decoration: line-through;
    color: #8c8c8c;
  }
}
</style>
```

### 3.3 项目详情页面

```typescript
<template>
  <div class="project-detail">
    <!-- 项目头部 -->
    <div class="project-header">
      <a-page-header
        :title="project.name"
        :sub-title="project.description"
        @back="goBack"
      >
        <template #tags>
          <a-tag :color="getStatusColor(project.status)">
            {{ getStatusText(project.status) }}
          </a-tag>
        </template>

        <template #extra>
          <a-space>
            <a-button @click="handleEditProject">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button @click="handleExportReport">
              <template #icon><ExportOutlined /></template>
              导出报告
            </a-button>
            <a-dropdown>
              <a-button>
                更多操作 <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleArchiveProject">
                    <InboxOutlined /> 归档项目
                  </a-menu-item>
                  <a-menu-item @click="handleCloneProject">
                    <CopyOutlined /> 克隆项目
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item danger @click="handleDeleteProject">
                    <DeleteOutlined /> 删除项目
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>

        <template #content>
          <a-descriptions :column="{ xs: 1, sm: 2, md: 3 }} size="small">
            <a-descriptions-item label="项目经理">
              <a-space>
                <a-avatar :src="project.manager.avatar" :size="24" />
                {{ project.manager.name }}
              </a-space>
            </a-descriptions-item>

            <a-descriptions-item label="团队规模">
              <TeamOutlined /> {{ project.teamSize }} 人
            </a-descriptions-item>

            <a-descriptions-item label="项目周期">
              {{ formatDateRange(project.startDate, project.endDate) }}
            </a-descriptions-item>

            <a-descriptions-item label="整体进度">
              <a-progress
                :percent="project.progress"
                :status="getProgressStatus(project)"
                size="small"
                style="width: 200px"
              />
            </a-descriptions-item>

            <a-descriptions-item label="剩余时间">
              <ClockCircleOutlined />
              {{ calculateRemainingDays(project.endDate) }} 天
            </a-descriptions-item>

            <a-descriptions-item label="预算使用">
              {{ project.budgetUsed }} / {{ project.totalBudget }} 万元
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-page-header>
    </div>

    <!-- 项目内容标签页 -->
    <a-tabs v-model:activeKey="activeTab" class="project-tabs">
      <!-- 概览 -->
      <a-tab-pane key="overview" tab="概览">
        <ProjectOverview :project="project" />
      </a-tab-pane>

      <!-- 任务 -->
      <a-tab-pane key="tasks">
        <template #tab>
          <a-badge :count="stats.pendingTasks" :offset="[10, 0]">
            任务
          </a-badge>
        </template>
        <TaskBoard :project-id="project.id" />
      </a-tab-pane>

      <!-- 文档 -->
      <a-tab-pane key="documents" tab="文档">
        <DocumentList :project-id="project.id" />
      </a-tab-pane>

      <!-- 变更 -->
      <a-tab-pane key="changes">
        <template #tab>
          <a-badge :count="stats.pendingChanges" :offset="[10, 0]">
            变更
          </a-badge>
        </template>
        <ChangeList :project-id="project.id" />
      </a-tab-pane>

      <!-- 团队 -->
      <a-tab-pane key="team" tab="团队">
        <TeamManagement :project-id="project.id" />
      </a-tab-pane>

      <!-- 测试 -->
      <a-tab-pane key="testing" tab="测试">
        <TestManagement :project-id="project.id" />
      </a-tab-pane>

      <!-- 统计 -->
      <a-tab-pane key="analytics" tab="统计">
        <ProjectAnalytics :project-id="project.id" />
      </a-tab-pane>

      <!-- 设置 -->
      <a-tab-pane key="settings" tab="设置">
        <ProjectSettings :project-id="project.id" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<style scoped lang="scss">
.project-detail {
  .project-header {
    background: #ffffff;
    margin-bottom: 16px;
  }

  .project-tabs {
    background: #ffffff;
    padding: 0 24px;
  }
}
</style>
```

---

**文档持续...**

**文档版本**: v1.0
**创建日期**: 2025-09-30
**下一部分**: Part 3 (续) - Developer、Tester、UI Designer、Operations、Third-party Personnel 界面设计