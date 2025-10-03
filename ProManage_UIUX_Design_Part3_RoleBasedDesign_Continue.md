# ProManage UI/UX 设计文档 - Part 3 (续): 剩余角色界面设计

## 4. Developer (开发人员) 界面设计

### 4.1 角色特征
```
主要职责:
- 任务执行与代码开发
- 文档查阅与更新
- 缺陷修复
- 代码审查
- 技术方案实现

关键需求:
- 快速访问任务和文档
- 清晰的任务优先级
- 变更通知及时推送
- 代码相关文档关联
- 高效的搜索功能
```

### 4.2 开发人员工作台

```typescript
<template>
  <div class="developer-workspace">
    <!-- 顶部快捷操作栏 -->
    <div class="quick-actions">
      <a-space size="large">
        <a-button type="primary" @click="handleCreateTask">
          <template #icon><PlusOutlined /></template>
          创建任务
        </a-button>
        <a-button @click="handleSearchDocs">
          <template #icon><SearchOutlined /></template>
          搜索文档
        </a-button>
        <a-button @click="handleReportBug">
          <template #icon><BugOutlined /></template>
          报告缺陷
        </a-button>
      </a-space>

      <!-- 全局搜索 -->
      <a-input-search
        v-model:value="searchText"
        placeholder="快速搜索任务、文档、代码..."
        style="width: 400px"
        @search="handleGlobalSearch"
      >
        <template #enterButton>
          <a-button type="primary">搜索</a-button>
        </template>
      </a-input-search>
    </div>

    <!-- 我的任务看板 -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="16">
        <a-card title="我的任务" :bordered="false">
          <template #extra>
            <a-space>
              <a-select
                v-model:value="taskFilter"
                style="width: 120px"
                @change="handleFilterChange"
              >
                <a-select-option value="all">全部任务</a-select-option>
                <a-select-option value="today">今日任务</a-select-option>
                <a-select-option value="week">本周任务</a-select-option>
                <a-select-option value="overdue">逾期任务</a-select-option>
              </a-select>

              <a-radio-group
                v-model:value="taskViewMode"
                button-style="solid"
                size="small"
              >
                <a-radio-button value="list">
                  <UnorderedListOutlined />
                </a-radio-button>
                <a-radio-button value="kanban">
                  <AppstoreOutlined />
                </a-radio-button>
              </a-radio-group>
            </a-space>
          </template>

          <!-- 任务列表视图 -->
          <template v-if="taskViewMode === 'list'">
            <a-list
              :data-source="tasks"
              :pagination="{ pageSize: 10 }"
            >
              <template #renderItem="{ item }">
                <a-list-item class="task-item">
                  <template #actions>
                    <a-dropdown>
                      <a-button type="link" size="small">
                        {{ item.status }}
                        <DownOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu @click="({ key }) => handleStatusChange(item, key)">
                          <a-menu-item key="todo">待处理</a-menu-item>
                          <a-menu-item key="in_progress">进行中</a-menu-item>
                          <a-menu-item key="testing">测试中</a-menu-item>
                          <a-menu-item key="done">已完成</a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </template>

                  <a-list-item-meta>
                    <template #avatar>
                      <a-checkbox
                        :checked="item.status === 'done'"
                        @change="() => handleTaskComplete(item)"
                      />
                    </template>

                    <template #title>
                      <a @click="handleViewTask(item)">{{ item.title }}</a>
                      <a-tag
                        v-if="item.priority === 'high'"
                        color="red"
                        style="margin-left: 8px"
                      >
                        高优先级
                      </a-tag>
                      <a-tag
                        v-else-if="item.priority === 'urgent'"
                        color="magenta"
                        style="margin-left: 8px"
                      >
                        紧急
                      </a-tag>
                    </template>

                    <template #description>
                      <a-space split="|">
                        <span>
                          <FolderOutlined />
                          {{ item.projectName }}
                        </span>
                        <span>
                          <CalendarOutlined />
                          {{ formatDueDate(item.dueDate) }}
                        </span>
                        <span v-if="item.estimatedHours">
                          <ClockCircleOutlined />
                          预计 {{ item.estimatedHours }}h
                        </span>
                        <span v-if="item.tags">
                          <TagOutlined />
                          {{ item.tags.join(', ') }}
                        </span>
                      </a-space>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </template>

          <!-- 看板视图 -->
          <template v-else-if="taskViewMode === 'kanban'">
            <TaskKanban :tasks="tasks" @status-change="handleStatusChange" />
          </template>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <!-- 今日聚焦 -->
        <a-card title="今日聚焦" :bordered="false" class="focus-card">
          <a-statistic
            title="待完成任务"
            :value="todayStats.pending"
            :prefix="() => <CheckSquareOutlined />"
          />
          <a-statistic
            title="已完成任务"
            :value="todayStats.completed"
            :prefix="() => <CheckCircleFilled />"
            :value-style="{ color: '#52c41a' }"
            style="margin-top: 16px"
          />
          <a-statistic
            title="投入时间"
            :value="todayStats.hoursSpent"
            suffix="小时"
            :prefix="() => <ClockCircleOutlined />"
            style="margin-top: 16px"
          />
        </a-card>

        <!-- 最近访问的文档 -->
        <a-card
          title="最近访问"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-list
            :data-source="recentDocuments"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <FileTextOutlined style="font-size: 20px; color: #1890ff" />
                  </template>
                  <template #title>
                    <a @click="openDocument(item)">{{ item.name }}</a>
                  </template>
                  <template #description>
                    {{ formatTime(item.accessedAt) }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>

          <a-button type="dashed" block style="margin-top: 8px" @click="browseAllDocs">
            浏览全部文档
          </a-button>
        </a-card>

        <!-- 变更通知 -->
        <a-card
          title="变更通知"
          :bordered="false"
          style="margin-top: 16px"
        >
          <template #extra>
            <a-badge :count="changeNotifications.length" />
          </template>

          <a-timeline>
            <a-timeline-item
              v-for="change in changeNotifications"
              :key="change.id"
              :color="change.type === 'breaking' ? 'red' : 'blue'"
            >
              <template #dot>
                <ExclamationCircleOutlined v-if="change.type === 'breaking'" />
                <InfoCircleOutlined v-else />
              </template>
              <p>
                <strong>{{ change.title }}</strong>
                <a-tag
                  v-if="change.type === 'breaking'"
                  color="red"
                  size="small"
                  style="margin-left: 8px"
                >
                  重大变更
                </a-tag>
              </p>
              <p style="color: #8c8c8c; font-size: 12px">
                {{ change.affectedModules.join(', ') }}
              </p>
              <p style="color: #8c8c8c; font-size: 12px">
                {{ formatTime(change.createdAt) }}
              </p>
              <a @click="viewChangeDetail(change)">查看详情 →</a>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>

    <!-- 代码相关资源 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24">
        <a-card :bordered="false">
          <a-tabs v-model:activeKey="resourceTab">
            <a-tab-pane key="apis" tab="API文档">
              <ApiDocList :project-id="currentProject" />
            </a-tab-pane>

            <a-tab-pane key="database" tab="数据库设计">
              <DatabaseSchemaViewer :project-id="currentProject" />
            </a-tab-pane>

            <a-tab-pane key="architecture" tab="架构文档">
              <ArchitectureDocList :project-id="currentProject" />
            </a-tab-pane>

            <a-tab-pane key="changelog" tab="变更日志">
              <ChangelogViewer :project-id="currentProject" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.developer-workspace {
  .quick-actions {
    background: #ffffff;
    padding: 16px 24px;
    margin-bottom: 16px;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .task-item {
    &:hover {
      background: #f5f5f5;
      cursor: pointer;
    }
  }

  .focus-card {
    border-left: 3px solid #52c41a; // Developer 主题色
  }
}
</style>
```

### 4.3 任务详情页

```typescript
<template>
  <a-drawer
    v-model:visible="visible"
    title="任务详情"
    :width="720"
    :body-style="{ paddingBottom: '80px' }"
  >
    <!-- 任务头部信息 -->
    <div class="task-header">
      <h2>{{ task.title }}</h2>
      <a-space>
        <a-select
          v-model:value="task.status"
          style="width: 120px"
          @change="handleStatusChange"
        >
          <a-select-option value="todo">待处理</a-select-option>
          <a-select-option value="in_progress">进行中</a-select-option>
          <a-select-option value="testing">测试中</a-select-option>
          <a-select-option value="done">已完成</a-select-option>
        </a-select>

        <a-dropdown>
          <a-button>
            更多操作 <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="handleCloneTask">
                <CopyOutlined /> 克隆任务
              </a-menu-item>
              <a-menu-item @click="handleConvertToBug">
                <BugOutlined /> 转为缺陷
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item danger @click="handleDeleteTask">
                <DeleteOutlined /> 删除任务
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-space>
    </div>

    <!-- 任务详情 -->
    <a-descriptions :column="1" bordered style="margin-top: 16px">
      <a-descriptions-item label="所属项目">
        <a-space>
          <ProjectOutlined />
          {{ task.projectName }}
        </a-space>
      </a-descriptions-item>

      <a-descriptions-item label="优先级">
        <a-select v-model:value="task.priority" style="width: 120px">
          <a-select-option value="low">
            <a-tag color="default">低</a-tag>
          </a-select-option>
          <a-select-option value="medium">
            <a-tag color="blue">中</a-tag>
          </a-select-option>
          <a-select-option value="high">
            <a-tag color="orange">高</a-tag>
          </a-select-option>
          <a-select-option value="urgent">
            <a-tag color="red">紧急</a-tag>
          </a-select-option>
        </a-select>
      </a-descriptions-item>

      <a-descriptions-item label="截止日期">
        <a-date-picker
          v-model:value="task.dueDate"
          style="width: 100%"
          @change="handleDueDateChange"
        />
      </a-descriptions-item>

      <a-descriptions-item label="预计工时">
        <a-input-number
          v-model:value="task.estimatedHours"
          :min="0"
          :step="0.5"
          suffix="小时"
        />
      </a-descriptions-item>

      <a-descriptions-item label="实际工时">
        <a-statistic
          :value="task.actualHours"
          suffix="小时"
          :prefix="() => <ClockCircleOutlined />"
        />
        <a-button type="link" size="small" @click="handleLogTime">
          记录工时
        </a-button>
      </a-descriptions-item>

      <a-descriptions-item label="负责人">
        <a-select
          v-model:value="task.assigneeId"
          style="width: 100%"
          show-search
          :filter-option="filterUser"
        >
          <a-select-option
            v-for="user in teamMembers"
            :key="user.id"
            :value="user.id"
          >
            <a-avatar :size="20" :src="user.avatar" />
            {{ user.name }}
          </a-select-option>
        </a-select>
      </a-descriptions-item>

      <a-descriptions-item label="标签">
        <a-select
          v-model:value="task.tags"
          mode="tags"
          style="width: 100%"
          placeholder="添加标签"
        />
      </a-descriptions-item>
    </a-descriptions>

    <!-- 任务描述 -->
    <div style="margin-top: 24px">
      <h3>任务描述</h3>
      <div v-if="!editingDescription" class="task-description">
        <div v-html="task.description"></div>
        <a-button type="link" @click="editingDescription = true">
          编辑描述
        </a-button>
      </div>
      <div v-else>
        <RichTextEditor v-model:value="task.description" />
        <a-space style="margin-top: 8px">
          <a-button type="primary" @click="handleSaveDescription">
            保存
          </a-button>
          <a-button @click="editingDescription = false">
            取消
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 子任务 -->
    <div style="margin-top: 24px">
      <h3>
        子任务
        <a-button type="link" size="small" @click="handleAddSubtask">
          <PlusOutlined /> 添加
        </a-button>
      </h3>
      <a-list :data-source="task.subtasks">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-checkbox
              v-model:checked="item.completed"
              @change="() => handleSubtaskChange(item)"
            >
              {{ item.title }}
            </a-checkbox>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 相关文档 -->
    <div style="margin-top: 24px">
      <h3>
        相关文档
        <a-button type="link" size="small" @click="handleLinkDocument">
          <LinkOutlined /> 关联
        </a-button>
      </h3>
      <a-list :data-source="task.relatedDocs" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <FileTextOutlined style="font-size: 20px; color: #1890ff" />
              </template>
              <template #title>
                <a @click="openDocument(item)">{{ item.name }}</a>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 评论区 -->
    <div style="margin-top: 24px">
      <h3>评论 ({{ task.comments.length }})</h3>
      <a-list :data-source="task.comments">
        <template #renderItem="{ item }">
          <a-comment
            :author="item.author.name"
            :avatar="item.author.avatar"
            :content="item.content"
            :datetime="formatTime(item.createdAt)"
          />
        </template>
      </a-list>

      <a-comment>
        <template #avatar>
          <a-avatar :src="currentUser.avatar" />
        </template>
        <template #content>
          <a-textarea
            v-model:value="newComment"
            :rows="3"
            placeholder="添加评论..."
          />
          <a-button
            type="primary"
            style="margin-top: 8px"
            :disabled="!newComment"
            @click="handleAddComment"
          >
            发表评论
          </a-button>
        </template>
      </a-comment>
    </div>

    <!-- 底部操作栏 -->
    <div class="drawer-footer">
      <a-space>
        <a-button type="primary" @click="handleSave">
          保存
        </a-button>
        <a-button @click="visible = false">
          关闭
        </a-button>
      </a-space>
    </div>
  </a-drawer>
</template>

<style scoped lang="scss">
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-description {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  min-height: 100px;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 100%;
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>
```

---

## 5. Tester (测试人员) 界面设计

### 5.1 角色特征
```
主要职责:
- 测试用例设计与管理
- 执行测试计划
- 缺陷跟踪与管理
- 测试报告生成
- 质量把控

关键需求:
- 高效的测试用例库
- 测试用例复用（目标70%+）
- 变更影响分析
- 缺陷统计与趋势
- 测试覆盖率可视化
```

### 5.2 测试控制中心

```typescript
<template>
  <div class="tester-workspace">
    <!-- 顶部关键指标 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="测试用例总数"
            :value="stats.totalTestCases"
            :prefix="() => <ExperimentOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="用例复用率"
            :value="stats.reuseRate"
            suffix="%"
            :prefix="() => <RetweetOutlined />"
            :value-style="stats.reuseRate >= 70 ? { color: '#52c41a' } : { color: '#faad14' }"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="待测试任务"
            :value="stats.pendingTests"
            :prefix="() => <ClockCircleOutlined />"
            :value-style="stats.pendingTests > 0 ? { color: '#fa8c16' } : {}"
          >
            <template #suffix>
              <a-button type="link" size="small" @click="gotoPendingTests">
                查看
              </a-button>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="测试通过率"
            :value="stats.passRate"
            suffix="%"
            :prefix="() => <CheckCircleOutlined />"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 主要工作区 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="16">
        <!-- 测试用例库 -->
        <a-card title="测试用例库" :bordered="false">
          <template #extra>
            <a-space>
              <a-button type="primary" @click="handleCreateTestCase">
                <template #icon><PlusOutlined /></template>
                新建用例
              </a-button>
              <a-button @click="handleImportTestCases">
                <template #icon><ImportOutlined /></template>
                导入
              </a-button>
              <a-button @click="handleExportTestCases">
                <template #icon><ExportOutlined /></template>
                导出
              </a-button>
            </a-space>
          </template>

          <!-- 筛选工具栏 -->
          <div class="filter-bar">
            <a-space>
              <a-input-search
                v-model:value="searchText"
                placeholder="搜索测试用例..."
                style="width: 300px"
                @search="handleSearch"
              />

              <a-select
                v-model:value="filterProject"
                placeholder="项目"
                style="width: 150px"
                allow-clear
              >
                <a-select-option
                  v-for="project in projects"
                  :key="project.id"
                  :value="project.id"
                >
                  {{ project.name }}
                </a-select-option>
              </a-select>

              <a-select
                v-model:value="filterModule"
                placeholder="模块"
                style="width: 150px"
                allow-clear
              >
                <a-select-option
                  v-for="module in modules"
                  :key="module.id"
                  :value="module.id"
                >
                  {{ module.name }}
                </a-select-option>
              </a-select>

              <a-select
                v-model:value="filterPriority"
                placeholder="优先级"
                style="width: 120px"
                allow-clear
              >
                <a-select-option value="high">高</a-select-option>
                <a-select-option value="medium">中</a-select-option>
                <a-select-option value="low">低</a-select-option>
              </a-select>

              <a-select
                v-model:value="filterStatus"
                placeholder="状态"
                style="width: 120px"
                allow-clear
              >
                <a-select-option value="active">有效</a-select-option>
                <a-select-option value="deprecated">已废弃</a-select-option>
              </a-select>
            </a-space>
          </div>

          <!-- 测试用例表格 -->
          <a-table
            :columns="testCaseColumns"
            :data-source="testCases"
            :loading="loading"
            :pagination="pagination"
            :row-selection="rowSelection"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- 用例名称 -->
              <template v-if="column.key === 'name'">
                <a @click="handleViewTestCase(record)">{{ record.name }}</a>
                <a-tag
                  v-if="record.isReusable"
                  color="green"
                  size="small"
                  style="margin-left: 8px"
                >
                  可复用
                </a-tag>
              </template>

              <!-- 优先级 -->
              <template v-else-if="column.key === 'priority'">
                <a-tag :color="getPriorityColor(record.priority)">
                  {{ getPriorityText(record.priority) }}
                </a-tag>
              </template>

              <!-- 最后执行结果 -->
              <template v-else-if="column.key === 'lastResult'">
                <a-badge
                  :status="getTestResultBadge(record.lastResult)"
                  :text="getTestResultText(record.lastResult)"
                />
              </template>

              <!-- 复用次数 -->
              <template v-else-if="column.key === 'reuseCount'">
                <a-statistic
                  :value="record.reuseCount"
                  :value-style="{ fontSize: '14px' }"
                />
              </template>

              <!-- 操作 -->
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button
                    type="primary"
                    size="small"
                    @click="handleExecuteTest(record)"
                  >
                    执行
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleEditTestCase(record)"
                  >
                    编辑
                  </a-button>
                  <a-dropdown>
                    <a-button type="link" size="small">
                      更多 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleCloneTestCase(record)">
                          <CopyOutlined /> 克隆
                        </a-menu-item>
                        <a-menu-item @click="handleViewHistory(record)">
                          <HistoryOutlined /> 执行历史
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item
                          danger
                          @click="handleDeleteTestCase(record)"
                        >
                          <DeleteOutlined /> 删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>

          <!-- 批量操作 -->
          <div v-if="selectedRowKeys.length > 0" class="batch-actions">
            <a-space>
              <span>已选择 {{ selectedRowKeys.length }} 项</span>
              <a-button @click="handleBatchExecute">批量执行</a-button>
              <a-button @click="handleBatchExport">批量导出</a-button>
              <a-button danger @click="handleBatchDelete">批量删除</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <!-- 待测试列表 -->
        <a-card title="待测试" :bordered="false">
          <template #extra>
            <a-badge :count="pendingTests.length" />
          </template>

          <a-list :data-source="pendingTests" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge status="processing" />
                  </template>
                  <template #title>
                    <a @click="handleStartTest(item)">{{ item.name }}</a>
                  </template>
                  <template #description>
                    <a-space split="|">
                      <span>{{ item.projectName }}</span>
                      <span>{{ item.testCaseCount }} 个用例</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="primary" size="small" @click="handleStartTest(item)">
                    开始测试
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- 缺陷统计 -->
        <a-card title="缺陷统计" :bordered="false" style="margin-top: 16px">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-statistic
                title="未解决"
                :value="bugStats.open"
                :value-style="{ color: '#ff4d4f' }"
              />
            </a-col>
            <a-col :span="12">
              <a-statistic
                title="待验证"
                :value="bugStats.pending"
                :value-style="{ color: '#faad14' }"
              />
            </a-col>
          </a-row>

          <a-divider />

          <div class="bug-trend-chart">
            <h4>缺陷趋势（最近7天）</h4>
            <BugTrendChart :data="bugTrendData" />
          </div>

          <a-button type="link" block @click="gotoBugManagement">
            查看全部缺陷 →
          </a-button>
        </a-card>

        <!-- 变更影响分析 -->
        <a-card
          title="变更影响分析"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-alert
            v-if="recentChanges.length > 0"
            message="有新的变更需要测试"
            :description="`${recentChanges.length} 个变更可能影响现有测试用例`"
            type="warning"
            show-icon
            style="margin-bottom: 16px"
          />

          <a-list :data-source="recentChanges" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    {{ item.title }}
                  </template>
                  <template #description>
                    <a-space split="|">
                      <span>影响 {{ item.affectedCases }} 个用例</span>
                      <span>{{ formatTime(item.createdAt) }}</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small" @click="analyzeImpact(item)">
                    分析
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 测试报告 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="测试覆盖率" :bordered="false">
          <a-row :gutter="16">
            <a-col :xs="24" :md="12">
              <h4>模块覆盖率</h4>
              <ModuleCoverageChart :data="coverageData.modules" />
            </a-col>
            <a-col :xs="24" :md="12">
              <h4>功能覆盖率</h4>
              <FeatureCoverageChart :data="coverageData.features" />
            </a-col>
          </a-row>

          <a-button
            type="primary"
            style="margin-top: 16px"
            @click="generateTestReport"
          >
            <template #icon><FileTextOutlined /></template>
            生成测试报告
          </a-button>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.tester-workspace {
  .metric-card {
    border-left: 3px solid #fa8c16; // Tester 主题色
  }

  .filter-bar {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .batch-actions {
    margin-top: 16px;
    padding: 12px;
    background: #e6f7ff;
    border-radius: 4px;
  }

  .bug-trend-chart {
    margin-top: 16px;

    h4 {
      margin-bottom: 12px;
    }
  }
}
</style>
```

---

**文档持续...**

**下一部分**: UI Designer、Operations、Third-party Personnel 界面设计