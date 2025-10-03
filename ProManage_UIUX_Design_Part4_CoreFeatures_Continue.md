# ProManage UI/UX 设计文档 - Part 4 (续): 核心功能UI/UX设计

## 2. 任务管理功能设计

### 2.1 任务看板界面

```typescript
<template>
  <div class="task-board">
    <!-- 看板头部 -->
    <div class="board-header">
      <div class="header-left">
        <h2>{{ currentProject.name }} - 任务看板</h2>
        <a-space>
          <a-select
            v-model:value="currentSprintId"
            style="width: 200px"
            placeholder="选择迭代"
          >
            <a-select-option
              v-for="sprint in sprints"
              :key="sprint.id"
              :value="sprint.id"
            >
              {{ sprint.name }}
              <a-tag
                v-if="sprint.status === 'active'"
                color="green"
                size="small"
                style="margin-left: 8px"
              >
                进行中
              </a-tag>
            </a-select-option>
          </a-select>

          <a-button @click="handleManageSprints">
            <SettingOutlined /> 管理迭代
          </a-button>
        </a-space>
      </div>

      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="handleCreateTask">
            <template #icon><PlusOutlined /></template>
            新建任务
          </a-button>

          <!-- 视图切换 -->
          <a-radio-group v-model:value="boardView" button-style="solid">
            <a-radio-button value="kanban">
              <AppstoreOutlined /> 看板
            </a-radio-button>
            <a-radio-button value="list">
              <UnorderedListOutlined /> 列表
            </a-radio-button>
            <a-radio-button value="gantt">
              <BarChartOutlined /> 甘特图
            </a-radio-button>
          </a-radio-group>

          <!-- 筛选 -->
          <a-dropdown>
            <a-button>
              <FilterOutlined /> 筛选
            </a-button>
            <template #overlay>
              <div style="padding: 16px; width: 300px; background: #ffffff">
                <a-form layout="vertical">
                  <a-form-item label="负责人">
                    <a-select
                      v-model:value="filters.assignees"
                      mode="multiple"
                      placeholder="选择负责人"
                    >
                      <a-select-option
                        v-for="user in teamMembers"
                        :key="user.id"
                        :value="user.id"
                      >
                        <a-avatar :src="user.avatar" :size="20" />
                        {{ user.name }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="优先级">
                    <a-checkbox-group v-model:value="filters.priorities">
                      <a-checkbox value="urgent">紧急</a-checkbox>
                      <a-checkbox value="high">高</a-checkbox>
                      <a-checkbox value="medium">中</a-checkbox>
                      <a-checkbox value="low">低</a-checkbox>
                    </a-checkbox-group>
                  </a-form-item>

                  <a-form-item label="标签">
                    <a-select
                      v-model:value="filters.tags"
                      mode="tags"
                      placeholder="输入标签"
                    />
                  </a-form-item>

                  <a-form-item>
                    <a-space>
                      <a-button type="primary" @click="handleApplyFilters">
                        应用筛选
                      </a-button>
                      <a-button @click="handleResetFilters">
                        重置
                      </a-button>
                    </a-space>
                  </a-form-item>
                </a-form>
              </div>
            </template>
          </a-dropdown>

          <!-- 分组 -->
          <a-select v-model:value="groupBy" style="width: 120px">
            <a-select-option value="status">按状态</a-select-option>
            <a-select-option value="assignee">按负责人</a-select-option>
            <a-select-option value="priority">按优先级</a-select-option>
            <a-select-option value="label">按标签</a-select-option>
          </a-select>
        </a-space>
      </div>
    </div>

    <!-- 看板统计 -->
    <div class="board-stats">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic
            title="总任务数"
            :value="boardStats.total"
            :prefix="() => <CheckSquareOutlined />"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="已完成"
            :value="boardStats.completed"
            :prefix="() => <CheckCircleFilled />"
            :value-style="{ color: '#52c41a' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="进行中"
            :value="boardStats.inProgress"
            :prefix="() => <SyncOutlined />"
            :value-style="{ color: '#1890ff' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="完成率"
            :value="boardStats.completionRate"
            suffix="%"
            :prefix="() => <RiseOutlined />"
          />
        </a-col>
      </a-row>
    </div>

    <!-- 看板视图 -->
    <div v-if="boardView === 'kanban'" class="kanban-board">
      <div class="kanban-container">
        <div
          v-for="column in kanbanColumns"
          :key="column.id"
          class="kanban-column"
          @drop="handleDrop($event, column.id)"
          @dragover.prevent
        >
          <!-- 列头 -->
          <div class="column-header">
            <a-space>
              <a-badge
                :color="column.color"
                :text="column.title"
              />
              <a-tag>{{ column.tasks.length }}</a-tag>
            </a-space>

            <a-dropdown>
              <a-button type="text" size="small">
                <MoreOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleAddTask(column.id)">
                    <PlusOutlined /> 添加任务
                  </a-menu-item>
                  <a-menu-item @click="handleEditColumn(column)">
                    <EditOutlined /> 编辑列
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item danger @click="handleDeleteColumn(column)">
                    <DeleteOutlined /> 删除列
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>

          <!-- 任务卡片列表 -->
          <div class="column-content">
            <TransitionGroup name="task-list">
              <div
                v-for="task in column.tasks"
                :key="task.id"
                class="task-card"
                draggable="true"
                @dragstart="handleDragStart($event, task)"
                @click="handleOpenTask(task)"
              >
                <!-- 任务标题 -->
                <div class="task-header">
                  <div class="task-title">
                    <a-checkbox
                      v-model:checked="task.completed"
                      @click.stop
                      @change="handleTaskComplete(task)"
                    />
                    <span>{{ task.title }}</span>
                  </div>

                  <!-- 优先级标签 -->
                  <a-tag
                    v-if="task.priority === 'urgent'"
                    color="red"
                    size="small"
                  >
                    紧急
                  </a-tag>
                  <a-tag
                    v-else-if="task.priority === 'high'"
                    color="orange"
                    size="small"
                  >
                    高
                  </a-tag>
                </div>

                <!-- 任务描述 -->
                <div v-if="task.description" class="task-description">
                  {{ truncateText(task.description, 80) }}
                </div>

                <!-- 任务标签 -->
                <div v-if="task.tags && task.tags.length > 0" class="task-tags">
                  <a-space wrap size="small">
                    <a-tag
                      v-for="tag in task.tags.slice(0, 3)"
                      :key="tag"
                      size="small"
                    >
                      {{ tag }}
                    </a-tag>
                  </a-space>
                </div>

                <!-- 任务底部信息 -->
                <div class="task-footer">
                  <a-space size="small">
                    <!-- 子任务进度 -->
                    <span v-if="task.subtaskCount > 0" class="task-meta">
                      <CheckSquareOutlined />
                      {{ task.completedSubtasks }}/{{ task.subtaskCount }}
                    </span>

                    <!-- 附件数量 -->
                    <span v-if="task.attachmentCount > 0" class="task-meta">
                      <PaperClipOutlined />
                      {{ task.attachmentCount }}
                    </span>

                    <!-- 评论数量 -->
                    <span v-if="task.commentCount > 0" class="task-meta">
                      <CommentOutlined />
                      {{ task.commentCount }}
                    </span>

                    <!-- 截止日期 -->
                    <span
                      v-if="task.dueDate"
                      class="task-meta"
                      :class="{ 'overdue': isOverdue(task.dueDate) }"
                    >
                      <ClockCircleOutlined />
                      {{ formatDueDate(task.dueDate) }}
                    </span>
                  </a-space>

                  <!-- 负责人 -->
                  <a-avatar
                    v-if="task.assignee"
                    :src="task.assignee.avatar"
                    :size="24"
                    :title="task.assignee.name"
                  />
                </div>
              </div>
            </TransitionGroup>

            <!-- 添加任务按钮 -->
            <a-button
              type="dashed"
              block
              class="add-task-btn"
              @click="handleAddTask(column.id)"
            >
              <PlusOutlined /> 添加任务
            </a-button>
          </div>
        </div>

        <!-- 添加列按钮 -->
        <div class="add-column-btn">
          <a-button type="dashed" @click="handleAddColumn">
            <PlusOutlined /> 添加列
          </a-button>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else-if="boardView === 'list'" class="list-view">
      <a-table
        :columns="taskColumns"
        :data-source="allTasks"
        :pagination="pagination"
        :row-selection="rowSelection"
        :expandable="{ expandedRowRender, expandIcon }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a-checkbox
              v-model:checked="record.completed"
              style="margin-right: 8px"
              @change="handleTaskComplete(record)"
            />
            <a @click="handleOpenTask(record)">{{ record.title }}</a>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-select
              v-model:value="record.status"
              size="small"
              style="width: 100px"
              @change="handleStatusChange(record)"
            >
              <a-select-option value="todo">待处理</a-select-option>
              <a-select-option value="in_progress">进行中</a-select-option>
              <a-select-option value="testing">测试中</a-select-option>
              <a-select-option value="done">已完成</a-select-option>
            </a-select>
          </template>

          <template v-else-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ getPriorityText(record.priority) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'assignee'">
            <a-select
              v-model:value="record.assigneeId"
              size="small"
              style="width: 150px"
              show-search
              @change="handleAssigneeChange(record)"
            >
              <a-select-option
                v-for="user in teamMembers"
                :key="user.id"
                :value="user.id"
              >
                <a-avatar :src="user.avatar" :size="20" />
                {{ user.name }}
              </a-select-option>
            </a-select>
          </template>

          <template v-else-if="column.key === 'dueDate'">
            <a-date-picker
              v-model:value="record.dueDate"
              size="small"
              :disabled-date="disabledDate"
              @change="handleDueDateChange(record)"
            />
          </template>

          <template v-else-if="column.key === 'progress'">
            <a-progress
              :percent="record.progress"
              :stroke-color="getProgressColor(record.progress)"
              size="small"
            />
          </template>
        </template>
      </a-table>
    </div>

    <!-- 甘特图视图 -->
    <div v-else-if="boardView === 'gantt'" class="gantt-view">
      <GanttChart
        :tasks="allTasks"
        :start-date="ganttStartDate"
        :end-date="ganttEndDate"
        @task-click="handleOpenTask"
        @task-update="handleTaskUpdate"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.task-board {
  .board-header {
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

  .board-stats {
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;
  }

  .kanban-board {
    background: #ffffff;
    border-radius: 4px;
    padding: 16px;

    .kanban-container {
      display: flex;
      gap: 16px;
      overflow-x: auto;
      padding-bottom: 16px;

      .kanban-column {
        flex: 0 0 300px;
        background: #f5f5f5;
        border-radius: 4px;
        max-height: calc(100vh - 400px);
        display: flex;
        flex-direction: column;

        .column-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 16px;
          border-bottom: 1px solid #e8e8e8;
          font-weight: 600;
        }

        .column-content {
          flex: 1;
          overflow-y: auto;
          padding: 12px;

          .task-card {
            background: #ffffff;
            border-radius: 4px;
            padding: 12px;
            margin-bottom: 8px;
            cursor: pointer;
            transition: all 0.3s;
            border: 1px solid #f0f0f0;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              transform: translateY(-2px);
            }

            .task-header {
              display: flex;
              justify-content: space-between;
              align-items: start;
              margin-bottom: 8px;

              .task-title {
                display: flex;
                align-items: start;
                gap: 8px;
                flex: 1;
                font-weight: 500;
              }
            }

            .task-description {
              color: #8c8c8c;
              font-size: 12px;
              margin-bottom: 8px;
              line-height: 1.5;
            }

            .task-tags {
              margin-bottom: 8px;
            }

            .task-footer {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 12px;
              color: #8c8c8c;

              .task-meta {
                display: inline-flex;
                align-items: center;
                gap: 4px;

                &.overdue {
                  color: #ff4d4f;
                }
              }
            }
          }

          .add-task-btn {
            margin-top: 8px;
          }
        }
      }

      .add-column-btn {
        flex: 0 0 200px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .list-view,
  .gantt-view {
    background: #ffffff;
    border-radius: 4px;
    padding: 16px;
  }
}

.task-list-move {
  transition: transform 0.3s;
}
</style>
```

---

## 3. 变更管理功能设计

### 3.1 变更请求界面

```typescript
<template>
  <div class="change-management">
    <!-- 变更管理头部 -->
    <div class="change-header">
      <h2>变更管理</h2>
      <a-space>
        <a-button type="primary" @click="handleCreateChange">
          <template #icon><PlusOutlined /></template>
          创建变更请求
        </a-button>
        <a-button @click="handleImportChanges">
          <template #icon><ImportOutlined /></template>
          批量导入
        </a-button>
      </a-space>
    </div>

    <!-- 变更统计 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="待审批"
            :value="changeStats.pending"
            :prefix="() => <ClockCircleOutlined />"
            :value-style="changeStats.pending > 0 ? { color: '#faad14' } : {}"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="进行中"
            :value="changeStats.inProgress"
            :prefix="() => <SyncOutlined />"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="已完成"
            :value="changeStats.completed"
            :prefix="() => <CheckCircleOutlined />"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <a-statistic
            title="自动通知率"
            :value="changeStats.autoNotifyRate"
            suffix="%"
            :prefix="() => <BellOutlined />"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 筛选工具栏 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-space size="large">
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索变更请求..."
          style="width: 300px"
          @search="handleSearch"
        />

        <a-select
          v-model:value="filterStatus"
          placeholder="状态"
          style="width: 150px"
          allow-clear
        >
          <a-select-option value="pending">待审批</a-select-option>
          <a-select-option value="approved">已批准</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
          <a-select-option value="in_progress">进行中</a-select-option>
          <a-select-option value="completed">已完成</a-select-option>
        </a-select>

        <a-select
          v-model:value="filterType"
          placeholder="类型"
          style="width: 150px"
          allow-clear
        >
          <a-select-option value="feature">新功能</a-select-option>
          <a-select-option value="enhancement">功能优化</a-select-option>
          <a-select-option value="bugfix">缺陷修复</a-select-option>
          <a-select-option value="refactor">代码重构</a-select-option>
          <a-select-option value="config">配置变更</a-select-option>
        </a-select>

        <a-select
          v-model:value="filterImpact"
          placeholder="影响范围"
          style="width: 150px"
          allow-clear
        >
          <a-select-option value="low">低影响</a-select-option>
          <a-select-option value="medium">中等影响</a-select-option>
          <a-select-option value="high">高影响</a-select-option>
          <a-select-option value="critical">重大变更</a-select-option>
        </a-select>

        <a-range-picker
          v-model:value="dateRange"
          placeholder="['开始日期', '结束日期']"
        />
      </a-space>
    </a-card>

    <!-- 变更请求列表 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="changeColumns"
        :data-source="changes"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 变更标题 -->
          <template v-if="column.key === 'title'">
            <a @click="handleViewChange(record)">
              <strong>{{ record.code }}</strong> - {{ record.title }}
            </a>
            <div style="color: #8c8c8c; font-size: 12px; margin-top: 4px">
              {{ record.projectName }}
            </div>
          </template>

          <!-- 变更类型 -->
          <template v-else-if="column.key === 'type'">
            <a-tag :color="getChangeTypeColor(record.type)">
              {{ getChangeTypeText(record.type) }}
            </a-tag>
          </template>

          <!-- 影响范围 -->
          <template v-else-if="column.key === 'impact'">
            <a-tooltip :title="getImpactTooltip(record)">
              <a-tag :color="getImpactColor(record.impact)">
                {{ getImpactText(record.impact) }}
              </a-tag>
              <a-button
                type="link"
                size="small"
                @click="handleAnalyzeImpact(record)"
              >
                <FundViewOutlined /> 影响分析
              </a-button>
            </a-tooltip>
          </template>

          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getChangeStatusBadge(record.status)"
              :text="getChangeStatusText(record.status)"
            />
          </template>

          <!-- 发起人 -->
          <template v-else-if="column.key === 'requester'">
            <a-space>
              <a-avatar :src="record.requester.avatar" :size="24" />
              <span>{{ record.requester.name }}</span>
            </a-space>
          </template>

          <!-- 受影响模块 -->
          <template v-else-if="column.key === 'affectedModules'">
            <a-space wrap>
              <a-tag
                v-for="module in record.affectedModules.slice(0, 2)"
                :key="module"
                size="small"
              >
                {{ module }}
              </a-tag>
              <a-tag
                v-if="record.affectedModules.length > 2"
                size="small"
              >
                +{{ record.affectedModules.length - 2 }}
              </a-tag>
            </a-space>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button
                v-if="record.status === 'pending'"
                type="primary"
                size="small"
                @click="handleApproveChange(record)"
              >
                审批
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleViewChange(record)"
              >
                查看详情
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleEditChange(record)">
                      <EditOutlined /> 编辑
                    </a-menu-item>
                    <a-menu-item @click="handleNotifyStakeholders(record)">
                      <BellOutlined /> 通知相关人
                    </a-menu-item>
                    <a-menu-item @click="handleExportChange(record)">
                      <ExportOutlined /> 导出
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger @click="handleDeleteChange(record)">
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 变更详情抽屉 -->
    <a-drawer
      v-model:visible="detailVisible"
      title="变更请求详情"
      :width="800"
      :body-style="{ paddingBottom: '80px' }"
    >
      <template v-if="currentChange">
        <!-- 变更基本信息 -->
        <a-descriptions title="基本信息" :column="2" bordered>
          <a-descriptions-item label="变更编号">
            {{ currentChange.code }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-badge
              :status="getChangeStatusBadge(currentChange.status)"
              :text="getChangeStatusText(currentChange.status)"
            />
          </a-descriptions-item>
          <a-descriptions-item label="变更类型">
            <a-tag :color="getChangeTypeColor(currentChange.type)">
              {{ getChangeTypeText(currentChange.type) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="影响范围">
            <a-tag :color="getImpactColor(currentChange.impact)">
              {{ getImpactText(currentChange.impact) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="发起人">
            <a-space>
              <a-avatar :src="currentChange.requester.avatar" />
              {{ currentChange.requester.name }}
            </a-space>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatTime(currentChange.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="所属项目" :span="2">
            {{ currentChange.projectName }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 变更描述 -->
        <a-divider />
        <h3>变更描述</h3>
        <div class="change-description" v-html="currentChange.description"></div>

        <!-- 受影响模块 -->
        <a-divider />
        <h3>受影响模块</h3>
        <a-space wrap>
          <a-tag
            v-for="module in currentChange.affectedModules"
            :key="module"
            color="blue"
          >
            {{ module }}
          </a-tag>
        </a-space>

        <!-- 影响分析 -->
        <a-divider />
        <h3>影响分析</h3>
        <a-alert
          v-if="currentChange.impact === 'critical'"
          message="重大变更警告"
          description="此变更将影响多个核心模块，建议召开评审会议并通知所有相关人员"
          type="error"
          show-icon
          style="margin-bottom: 16px"
        />

        <a-row :gutter="16">
          <a-col :span="8">
            <a-card size="small">
              <a-statistic
                title="受影响的文档"
                :value="currentChange.impactAnalysis.affectedDocs"
                prefix="📄"
              />
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card size="small">
              <a-statistic
                title="受影响的测试用例"
                :value="currentChange.impactAnalysis.affectedTests"
                prefix="🧪"
              />
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card size="small">
              <a-statistic
                title="需要通知的人员"
                :value="currentChange.impactAnalysis.affectedUsers"
                prefix="👥"
              />
            </a-card>
          </a-col>
        </a-row>

        <!-- 审批流程 -->
        <a-divider />
        <h3>审批流程</h3>
        <a-steps
          :current="getCurrentApprovalStep(currentChange)"
          direction="vertical"
        >
          <a-step
            v-for="approval in currentChange.approvals"
            :key="approval.id"
            :title="approval.approver.name"
            :description="getApprovalDescription(approval)"
            :status="getApprovalStatus(approval)"
          >
            <template #icon>
              <a-avatar :src="approval.approver.avatar" />
            </template>
          </a-step>
        </a-steps>

        <!-- 相关文档 -->
        <a-divider />
        <h3>相关文档</h3>
        <a-list
          :data-source="currentChange.relatedDocs"
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
                  更新于 {{ formatTime(item.updatedAt) }}
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>

        <!-- 变更历史 -->
        <a-divider />
        <h3>变更历史</h3>
        <a-timeline>
          <a-timeline-item
            v-for="history in currentChange.history"
            :key="history.id"
            :color="getHistoryColor(history.action)"
          >
            <p>
              <a-avatar :src="history.user.avatar" :size="20" />
              <strong>{{ history.user.name }}</strong>
              {{ history.action }}
            </p>
            <p style="color: #8c8c8c; font-size: 12px">
              {{ formatTime(history.createdAt) }}
            </p>
            <p v-if="history.comment">{{ history.comment }}</p>
          </a-timeline-item>
        </a-timeline>
      </template>

      <!-- 底部操作栏 -->
      <div class="drawer-footer">
        <a-space>
          <a-button
            v-if="currentChange.status === 'pending'"
            type="primary"
            @click="handleApproveChange(currentChange)"
          >
            批准
          </a-button>
          <a-button
            v-if="currentChange.status === 'pending'"
            danger
            @click="handleRejectChange(currentChange)"
          >
            拒绝
          </a-button>
          <a-button @click="detailVisible = false">
            关闭
          </a-button>
        </a-space>
      </div>
    </a-drawer>
  </div>
</template>

<style scoped lang="scss">
.change-management {
  .change-header {
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

  .change-description {
    padding: 12px;
    background: #fafafa;
    border-radius: 4px;
    line-height: 1.6;
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
}
</style>
```

---

## 4. 智能搜索功能设计

### 4.1 全局搜索界面

```typescript
<template>
  <div class="global-search">
    <!-- 搜索框 -->
    <div class="search-container">
      <a-input-search
        v-model:value="searchQuery"
        placeholder="搜索项目、文档、任务、用户..."
        size="large"
        allow-clear
        @search="handleSearch"
        @change="handleSearchChange"
      >
        <template #prefix>
          <SearchOutlined />
        </template>
        <template #suffix>
          <a-space size="small">
            <a-tooltip title="快捷键 Ctrl+K">
              <a-tag>⌘K</a-tag>
            </a-tooltip>
            <a-button
              type="link"
              size="small"
              @click="showAdvancedSearch = true"
            >
              高级搜索
            </a-button>
          </a-space>
        </template>
      </a-input-search>

      <!-- 搜索建议下拉 -->
      <div v-if="showSuggestions && suggestions.length > 0" class="search-suggestions">
        <div class="suggestions-header">
          <span>搜索建议</span>
          <a @click="showSuggestions = false">
            <CloseOutlined />
          </a>
        </div>

        <a-list :data-source="suggestions" size="small">
          <template #renderItem="{ item }">
            <a-list-item
              class="suggestion-item"
              @click="handleSelectSuggestion(item)"
            >
              <a-list-item-meta>
                <template #avatar>
                  <component
                    :is="getSuggestionIcon(item.type)"
                    :style="{ fontSize: '20px', color: getSuggestionColor(item.type) }"
                  />
                </template>
                <template #title>
                  <span v-html="highlightText(item.title, searchQuery)"></span>
                </template>
                <template #description>
                  <a-space split="|" size="small">
                    <span>{{ item.typeName }}</span>
                    <span v-if="item.projectName">{{ item.projectName }}</span>
                  </a-space>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults" class="search-results">
      <!-- 结果统计 -->
      <div class="results-header">
        <div class="results-info">
          找到 <strong>{{ searchResults.total }}</strong> 个结果
          <span style="color: #8c8c8c">（耗时 {{ searchResults.took }}ms）</span>
        </div>

        <a-space>
          <!-- 结果筛选 -->
          <a-select
            v-model:value="filterType"
            style="width: 150px"
            placeholder="所有类型"
            allow-clear
          >
            <a-select-option value="all">
              所有类型 ({{ searchResults.total }})
            </a-select-option>
            <a-select-option value="project">
              项目 ({{ searchResults.counts.project }})
            </a-select-option>
            <a-select-option value="document">
              文档 ({{ searchResults.counts.document }})
            </a-select-option>
            <a-select-option value="task">
              任务 ({{ searchResults.counts.task }})
            </a-select-option>
            <a-select-option value="change">
              变更 ({{ searchResults.counts.change }})
            </a-select-option>
            <a-select-option value="user">
              用户 ({{ searchResults.counts.user }})
            </a-select-option>
          </a-select>

          <a-select v-model:value="sortBy" style="width: 150px">
            <a-select-option value="relevance">相关性</a-select-option>
            <a-select-option value="date_desc">最新</a-select-option>
            <a-select-option value="date_asc">最旧</a-select-option>
            <a-select-option value="name_asc">名称A-Z</a-select-option>
          </a-select>
        </a-space>
      </div>

      <!-- 结果列表 -->
      <div class="results-list">
        <a-list
          :data-source="filteredResults"
          :pagination="pagination"
        >
          <template #renderItem="{ item }">
            <a-card
              class="result-card"
              hoverable
              @click="handleOpenResult(item)"
            >
              <div class="result-header">
                <a-space>
                  <component
                    :is="getResultIcon(item.type)"
                    :style="{ fontSize: '24px', color: getResultColor(item.type) }"
                  />
                  <div>
                    <h3 v-html="highlightText(item.title, searchQuery)"></h3>
                    <a-space split="|" size="small" class="result-meta">
                      <span>{{ item.typeName }}</span>
                      <span v-if="item.projectName">{{ item.projectName }}</span>
                      <span v-if="item.author">{{ item.author.name }}</span>
                      <span>{{ formatRelativeTime(item.updatedAt) }}</span>
                    </a-space>
                  </div>
                </a-space>

                <a-space>
                  <a-tag v-if="item.score" color="blue">
                    匹配度: {{ Math.round(item.score * 100) }}%
                  </a-tag>
                  <a-button
                    type="text"
                    size="small"
                    @click.stop="handleQuickAction(item)"
                  >
                    快速打开 →
                  </a-button>
                </a-space>
              </div>

              <!-- 搜索结果摘要 -->
              <div
                v-if="item.snippet"
                class="result-snippet"
                v-html="highlightText(item.snippet, searchQuery)"
              ></div>

              <!-- 标签 -->
              <div v-if="item.tags && item.tags.length > 0" class="result-tags">
                <a-space wrap size="small">
                  <a-tag
                    v-for="tag in item.tags"
                    :key="tag"
                    size="small"
                  >
                    {{ tag }}
                  </a-tag>
                </a-space>
              </div>

              <!-- 快捷操作 -->
              <div class="result-actions">
                <a-space>
                  <a @click.stop="handleOpenResult(item)">
                    <EyeOutlined /> 查看
                  </a>
                  <a
                    v-if="item.type === 'document'"
                    @click.stop="handleDownload(item)"
                  >
                    <DownloadOutlined /> 下载
                  </a>
                  <a @click.stop="handleShareResult(item)">
                    <ShareAltOutlined /> 分享
                  </a>
                </a-space>
              </div>
            </a-card>
          </template>
        </a-list>
      </div>
    </div>

    <!-- 空状态 -->
    <a-empty
      v-else-if="searchQuery && !loading"
      description="未找到相关结果"
    >
      <template #image>
        <SearchOutlined style="font-size: 48px; color: #d9d9d9" />
      </template>
      <a-space direction="vertical">
        <span>尝试使用其他关键词或</span>
        <a-button type="primary" @click="showAdvancedSearch = true">
          使用高级搜索
        </a-button>
      </a-space>
    </a-empty>

    <!-- 搜索历史 -->
    <div v-else class="search-history">
      <h3>搜索历史</h3>
      <a-space wrap>
        <a-tag
          v-for="history in searchHistory"
          :key="history"
          @click="handleSearchHistory(history)"
        >
          <ClockCircleOutlined /> {{ history }}
        </a-tag>
      </a-space>

      <a-divider />

      <h3>热门搜索</h3>
      <a-space wrap>
        <a-tag
          v-for="hot in hotSearches"
          :key="hot.keyword"
          color="blue"
          @click="handleSearchHistory(hot.keyword)"
        >
          <FireOutlined /> {{ hot.keyword }}
        </a-tag>
      </a-space>
    </div>
  </div>
</template>

<style scoped lang="scss">
.global-search {
  .search-container {
    position: relative;
    margin-bottom: 24px;

    .search-suggestions {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      margin-top: 8px;
      background: #ffffff;
      border-radius: 4px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      z-index: 1000;
      max-height: 400px;
      overflow-y: auto;

      .suggestions-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #f0f0f0;
        font-weight: 600;
      }

      .suggestion-item {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: #f5f5f5;
        }
      }
    }
  }

  .search-results {
    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 16px;

      .results-info {
        font-size: 14px;
      }
    }

    .results-list {
      .result-card {
        margin-bottom: 16px;

        .result-header {
          display: flex;
          justify-content: space-between;
          align-items: start;
          margin-bottom: 12px;

          h3 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
          }

          .result-meta {
            color: #8c8c8c;
            font-size: 12px;
            margin-top: 4px;
          }
        }

        .result-snippet {
          color: #595959;
          line-height: 1.6;
          margin-bottom: 12px;

          ::v-deep(.highlight) {
            background: #ffe58f;
            font-weight: 600;
          }
        }

        .result-tags {
          margin-bottom: 12px;
        }

        .result-actions {
          padding-top: 12px;
          border-top: 1px solid #f0f0f0;

          a {
            color: #8c8c8c;
            font-size: 12px;

            &:hover {
              color: #1890ff;
            }
          }
        }
      }
    }
  }

  .search-history {
    h3 {
      margin-bottom: 12px;
    }
  }
}

::v-deep(.highlight) {
  background: #ffe58f;
  font-weight: 600;
  padding: 0 2px;
}
</style>
```

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**下一部分**: Part 5 - 通知系统、数据分析功能设计