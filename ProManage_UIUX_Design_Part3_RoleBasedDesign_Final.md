# ProManage UI/UX 设计文档 - Part 3 (完结): 剩余角色界面设计

## 6. UI Designer (UI设计师) 界面设计

### 6.1 角色特征
```
主要职责:
- 设计文件管理
- 设计评审与反馈
- 设计规范维护
- 原型设计与管理
- 设计资产管理

关键需求:
- 可视化的文件预览
- 版本对比功能
- 评审反馈收集
- 设计规范文档化
- 高效的协作流程
```

### 6.2 设计工作室

```typescript
<template>
  <div class="designer-workspace">
    <!-- 顶部快捷操作 -->
    <div class="quick-actions">
      <a-space size="large">
        <a-button type="primary" @click="handleUploadDesign">
          <template #icon><UploadOutlined /></template>
          上传设计
        </a-button>
        <a-button @click="handleCreateArtboard">
          <template #icon><AppstoreAddOutlined /></template>
          创建画板
        </a-button>
        <a-button @click="handleManageAssets">
          <template #icon><PictureOutlined /></template>
          资产库
        </a-button>
      </a-space>

      <a-input-search
        v-model:value="searchText"
        placeholder="搜索设计文件、组件、资产..."
        style="width: 400px"
        @search="handleSearch"
      />
    </div>

    <!-- 关键指标 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="设计文件"
            :value="stats.totalDesigns"
            :prefix="() => <FileImageOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="待评审"
            :value="stats.pendingReviews"
            :prefix="() => <CommentOutlined />"
            :value-style="stats.pendingReviews > 0 ? { color: '#fa8c16' } : {}"
          >
            <template #suffix>
              <a-button type="link" size="small" @click="gotoPendingReviews">
                查看
              </a-button>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="组件库"
            :value="stats.componentCount"
            :prefix="() => <AppstoreOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="评审周期"
            :value="stats.avgReviewDays"
            suffix="天"
            :prefix="() => <ClockCircleOutlined />"
            :value-style="stats.avgReviewDays <= 2 ? { color: '#52c41a' } : { color: '#faad14' }"
          >
            <template #footer>
              <span style="font-size: 12px; color: #8c8c8c">
                目标: 缩短50%
              </span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主要工作区 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="18">
        <!-- 设计文件库 -->
        <a-card :bordered="false">
          <template #title>
            <a-space>
              <FileImageOutlined />
              <span>设计文件</span>
            </a-space>
          </template>

          <template #extra>
            <a-space>
              <a-radio-group
                v-model:value="viewMode"
                button-style="solid"
                size="small"
              >
                <a-radio-button value="grid">
                  <AppstoreOutlined /> 网格
                </a-radio-button>
                <a-radio-button value="list">
                  <UnorderedListOutlined /> 列表
                </a-radio-button>
              </a-radio-group>

              <a-select
                v-model:value="sortBy"
                style="width: 120px"
                size="small"
              >
                <a-select-option value="updated">最近更新</a-select-option>
                <a-select-option value="created">创建时间</a-select-option>
                <a-select-option value="name">名称</a-select-option>
              </a-select>

              <a-dropdown>
                <a-button size="small">
                  <FilterOutlined /> 筛选
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item-group title="项目">
                      <a-menu-item
                        v-for="project in projects"
                        :key="project.id"
                      >
                        {{ project.name }}
                      </a-menu-item>
                    </a-menu-item-group>
                    <a-menu-divider />
                    <a-menu-item-group title="文件类型">
                      <a-menu-item key="sketch">Sketch</a-menu-item>
                      <a-menu-item key="figma">Figma</a-menu-item>
                      <a-menu-item key="xd">Adobe XD</a-menu-item>
                      <a-menu-item key="png">PNG/JPG</a-menu-item>
                    </a-menu-item-group>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>

          <!-- 网格视图 -->
          <template v-if="viewMode === 'grid'">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="design in designFiles"
                :key="design.id"
                :xs="24"
                :sm="12"
                :md="8"
                :xl="6"
              >
                <a-card
                  hoverable
                  class="design-card"
                  @click="handleViewDesign(design)"
                >
                  <!-- 设计预览图 -->
                  <div class="design-preview">
                    <img :src="design.thumbnail" :alt="design.name" />

                    <!-- 悬浮操作层 -->
                    <div class="design-overlay">
                      <a-space>
                        <a-button
                          type="primary"
                          shape="circle"
                          @click.stop="handleViewDesign(design)"
                        >
                          <EyeOutlined />
                        </a-button>
                        <a-button
                          shape="circle"
                          @click.stop="handleDownloadDesign(design)"
                        >
                          <DownloadOutlined />
                        </a-button>
                        <a-dropdown>
                          <a-button shape="circle" @click.stop>
                            <MoreOutlined />
                          </a-button>
                          <template #overlay>
                            <a-menu>
                              <a-menu-item @click="handleEditDesign(design)">
                                <EditOutlined /> 编辑信息
                              </a-menu-item>
                              <a-menu-item @click="handleShareDesign(design)">
                                <ShareAltOutlined /> 分享
                              </a-menu-item>
                              <a-menu-item @click="handleVersionHistory(design)">
                                <HistoryOutlined /> 版本历史
                              </a-menu-item>
                              <a-menu-divider />
                              <a-menu-item danger @click="handleDeleteDesign(design)">
                                <DeleteOutlined /> 删除
                              </a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </a-space>
                    </div>

                    <!-- 文件类型标签 -->
                    <a-tag
                      :color="getFileTypeColor(design.fileType)"
                      class="file-type-tag"
                    >
                      {{ design.fileType }}
                    </a-tag>

                    <!-- 评审状态 -->
                    <a-badge
                      v-if="design.reviewStatus"
                      :status="getReviewStatusBadge(design.reviewStatus)"
                      class="review-status-badge"
                    />
                  </div>

                  <!-- 文件信息 -->
                  <a-card-meta
                    :title="design.name"
                    :description="design.projectName"
                  >
                    <template #avatar>
                      <a-avatar :src="design.author.avatar" :size="32" />
                    </template>
                  </a-card-meta>

                  <!-- 底部信息 -->
                  <div class="design-footer">
                    <a-space split="|" size="small">
                      <span>
                        <EyeOutlined /> {{ design.views }}
                      </span>
                      <span>
                        <CommentOutlined /> {{ design.comments }}
                      </span>
                      <span>
                        <HeartOutlined /> {{ design.likes }}
                      </span>
                    </a-space>
                    <span style="color: #8c8c8c; font-size: 12px">
                      {{ formatTime(design.updatedAt) }}
                    </span>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </template>

          <!-- 列表视图 -->
          <template v-else-if="viewMode === 'list'">
            <a-table
              :columns="designColumns"
              :data-source="designFiles"
              :pagination="pagination"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'name'">
                  <a-space>
                    <img
                      :src="record.thumbnail"
                      style="width: 40px; height: 40px; border-radius: 4px"
                    />
                    <div>
                      <a @click="handleViewDesign(record)">{{ record.name }}</a>
                      <div style="color: #8c8c8c; font-size: 12px">
                        {{ record.projectName }}
                      </div>
                    </div>
                  </a-space>
                </template>

                <template v-else-if="column.key === 'reviewStatus'">
                  <a-badge
                    :status="getReviewStatusBadge(record.reviewStatus)"
                    :text="getReviewStatusText(record.reviewStatus)"
                  />
                </template>

                <template v-else-if="column.key === 'actions'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleViewDesign(record)">
                      查看
                    </a-button>
                    <a-button type="link" size="small" @click="handleRequestReview(record)">
                      请求评审
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </template>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="6">
        <!-- 评审反馈 -->
        <a-card title="待处理反馈" :bordered="false">
          <template #extra>
            <a-badge :count="feedbacks.length" />
          </template>

          <a-list :data-source="feedbacks" size="small">
            <template #renderItem="{ item }">
              <a-list-item @click="handleViewFeedback(item)">
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :src="item.reviewer.avatar" />
                  </template>
                  <template #title>
                    {{ item.designName }}
                  </template>
                  <template #description>
                    <div>{{ item.comment }}</div>
                    <div style="color: #8c8c8c; font-size: 12px; margin-top: 4px">
                      {{ item.reviewer.name }} · {{ formatTime(item.createdAt) }}
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- 设计规范 -->
        <a-card
          title="设计规范"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-menu mode="inline" :selected-keys="[]">
            <a-menu-item key="colors" @click="openDesignSpec('colors')">
              <BgColorsOutlined /> 色彩规范
            </a-menu-item>
            <a-menu-item key="typography" @click="openDesignSpec('typography')">
              <FontSizeOutlined /> 字体规范
            </a-menu-item>
            <a-menu-item key="icons" @click="openDesignSpec('icons')">
              <SmileOutlined /> 图标库
            </a-menu-item>
            <a-menu-item key="components" @click="openDesignSpec('components')">
              <AppstoreOutlined /> 组件库
            </a-menu-item>
            <a-menu-item key="layout" @click="openDesignSpec('layout')">
              <LayoutOutlined /> 布局规范
            </a-menu-item>
          </a-menu>

          <a-button
            type="dashed"
            block
            style="margin-top: 16px"
            @click="handleUpdateSpec"
          >
            <PlusOutlined /> 更新规范
          </a-button>
        </a-card>

        <!-- 组件复用统计 -->
        <a-card
          title="组件复用"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-list :data-source="topComponents" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    {{ item.name }}
                  </template>
                  <template #description>
                    <a-space>
                      <span>使用 {{ item.usageCount }} 次</span>
                      <a-progress
                        :percent="(item.usageCount / stats.totalUsage) * 100"
                        :show-info="false"
                        size="small"
                        style="width: 60px"
                      />
                    </a-space>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 设计评审流程 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="评审进行中" :bordered="false">
          <a-table
            :columns="reviewColumns"
            :data-source="ongoingReviews"
            :pagination="{ pageSize: 5 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'design'">
                <a-space>
                  <img
                    :src="record.thumbnail"
                    style="width: 60px; height: 60px; border-radius: 4px"
                  />
                  <div>
                    <a @click="handleViewDesign(record)">{{ record.name }}</a>
                    <div style="color: #8c8c8c; font-size: 12px">
                      版本 {{ record.version }}
                    </div>
                  </div>
                </a-space>
              </template>

              <template v-else-if="column.key === 'reviewers'">
                <a-avatar-group :max-count="3">
                  <a-avatar
                    v-for="reviewer in record.reviewers"
                    :key="reviewer.id"
                    :src="reviewer.avatar"
                    :title="reviewer.name"
                  />
                </a-avatar-group>
              </template>

              <template v-else-if="column.key === 'progress'">
                <a-progress
                  :percent="(record.approvedCount / record.reviewers.length) * 100"
                  :format="() => `${record.approvedCount}/${record.reviewers.length}`"
                />
              </template>

              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button type="link" size="small" @click="handleViewReview(record)">
                    查看详情
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleRemindReviewers(record)"
                  >
                    提醒评审
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
.designer-workspace {
  .quick-actions {
    background: #ffffff;
    padding: 16px 24px;
    margin-bottom: 16px;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .metric-card {
    border-left: 3px solid #eb2f96; // UI Designer 主题色
  }

  .design-card {
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

      .design-overlay {
        opacity: 1;
      }
    }

    .design-preview {
      position: relative;
      width: 100%;
      height: 180px;
      overflow: hidden;
      border-radius: 4px;
      background: #f5f5f5;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .design-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
      }

      .file-type-tag {
        position: absolute;
        top: 8px;
        left: 8px;
      }

      .review-status-badge {
        position: absolute;
        top: 8px;
        right: 8px;
      }
    }

    .design-footer {
      margin-top: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #8c8c8c;
      font-size: 12px;
    }
  }
}
</style>
```

### 6.3 设计评审界面

```typescript
<template>
  <div class="design-review-panel">
    <a-row :gutter="16">
      <!-- 设计预览区 -->
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" class="preview-card">
          <template #title>
            <a-space>
              <a-button type="text" @click="goBack">
                <ArrowLeftOutlined />
              </a-button>
              <span>{{ design.name }}</span>
              <a-tag :color="getReviewStatusColor(design.reviewStatus)">
                {{ getReviewStatusText(design.reviewStatus) }}
              </a-tag>
            </a-space>
          </template>

          <template #extra>
            <a-space>
              <a-button-group>
                <a-button :type="viewScale === 50 ? 'primary' : 'default'" @click="setScale(50)">
                  50%
                </a-button>
                <a-button :type="viewScale === 100 ? 'primary' : 'default'" @click="setScale(100)">
                  100%
                </a-button>
                <a-button :type="viewScale === 150 ? 'primary' : 'default'" @click="setScale(150)">
                  150%
                </a-button>
              </a-button-group>

              <a-button @click="handleDownload">
                <DownloadOutlined /> 下载
              </a-button>

              <a-dropdown>
                <a-button>
                  版本 v{{ design.version }} <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item
                      v-for="version in design.versions"
                      :key="version.id"
                      @click="switchVersion(version)"
                    >
                      v{{ version.number }} - {{ formatTime(version.createdAt) }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>

          <!-- 设计预览画布 -->
          <div class="design-canvas" :style="{ transform: `scale(${viewScale / 100})` }">
            <img :src="design.previewUrl" :alt="design.name" />

            <!-- 评论标记点 -->
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-marker"
              :style="{
                left: comment.position.x + '%',
                top: comment.position.y + '%'
              }"
              @click="handleViewComment(comment)"
            >
              <a-badge :count="comment.replies.length + 1">
                <a-avatar :src="comment.author.avatar" :size="32" />
              </a-badge>
            </div>
          </div>

          <!-- 添加评论工具 -->
          <div class="comment-tools">
            <a-button
              :type="isAddingComment ? 'primary' : 'default'"
              @click="toggleCommentMode"
            >
              <CommentOutlined /> {{ isAddingComment ? '点击画布添加评论' : '添加评论' }}
            </a-button>
          </div>
        </a-card>
      </a-col>

      <!-- 评审信息侧栏 -->
      <a-col :xs="24" :lg="8">
        <!-- 设计信息 -->
        <a-card title="设计信息" :bordered="false">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="项目">
              {{ design.projectName }}
            </a-descriptions-item>
            <a-descriptions-item label="设计师">
              <a-space>
                <a-avatar :src="design.author.avatar" :size="24" />
                {{ design.author.name }}
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="上传时间">
              {{ formatTime(design.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="文件大小">
              {{ formatFileSize(design.fileSize) }}
            </a-descriptions-item>
            <a-descriptions-item label="尺寸">
              {{ design.width }} × {{ design.height }}
            </a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <div>
            <h4>标签</h4>
            <a-space wrap>
              <a-tag v-for="tag in design.tags" :key="tag">{{ tag }}</a-tag>
            </a-space>
          </div>
        </a-card>

        <!-- 评审人员 -->
        <a-card
          title="评审人员"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-list :data-source="design.reviewers" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :src="item.avatar" />
                  </template>
                  <template #title>
                    {{ item.name }}
                  </template>
                  <template #description>
                    <a-badge
                      :status="getReviewerStatusBadge(item.status)"
                      :text="getReviewerStatusText(item.status)"
                    />
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>

          <a-button
            type="dashed"
            block
            style="margin-top: 12px"
            @click="handleAddReviewer"
          >
            <UserAddOutlined /> 添加评审人
          </a-button>
        </a-card>

        <!-- 评论列表 -->
        <a-card
          title="评论"
          :bordered="false"
          style="margin-top: 16px"
        >
          <template #extra>
            <a-badge :count="comments.length" />
          </template>

          <a-list
            :data-source="comments"
            :pagination="{ pageSize: 5 }"
          >
            <template #renderItem="{ item }">
              <a-comment
                :author="item.author.name"
                :avatar="item.author.avatar"
                :content="item.content"
                :datetime="formatTime(item.createdAt)"
              >
                <template #actions>
                  <span @click="handleReplyComment(item)">
                    <CommentOutlined /> 回复
                  </span>
                  <span v-if="item.resolved" style="color: #52c41a">
                    <CheckOutlined /> 已解决
                  </span>
                  <span v-else @click="handleResolveComment(item)">
                    解决
                  </span>
                </template>

                <!-- 回复列表 -->
                <template v-if="item.replies && item.replies.length > 0">
                  <a-comment
                    v-for="reply in item.replies"
                    :key="reply.id"
                    :author="reply.author.name"
                    :avatar="reply.author.avatar"
                    :content="reply.content"
                    :datetime="formatTime(reply.createdAt)"
                  />
                </template>
              </a-comment>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.design-review-panel {
  .preview-card {
    min-height: 600px;
  }

  .design-canvas {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 500px;
    background: #f5f5f5;
    border-radius: 4px;
    overflow: auto;

    img {
      max-width: 100%;
      height: auto;
    }

    .comment-marker {
      position: absolute;
      cursor: pointer;
      transform: translate(-50%, -50%);
      transition: all 0.3s;

      &:hover {
        transform: translate(-50%, -50%) scale(1.1);
      }
    }
  }

  .comment-tools {
    margin-top: 16px;
    text-align: center;
  }
}
</style>
```

---

## 7. Operations (运维人员) 界面设计

### 7.1 角色特征
```
主要职责:
- 环境管理与部署
- 系统监控与告警
- 日志分析
- 备份与恢复
- 性能优化

关键需求:
- 清晰的环境信息
- 部署文档快速访问
- 实时监控面板
- 告警及时响应
- 操作记录可追溯
```

### 7.2 运维指挥中心

```typescript
<template>
  <div class="ops-workspace">
    <!-- 顶部快捷操作 -->
    <div class="quick-actions">
      <a-space size="large">
        <a-button type="primary" @click="handleDeploy">
          <template #icon><RocketOutlined /></template>
          执行部署
        </a-button>
        <a-button @click="handleViewLogs">
          <template #icon><FileTextOutlined /></template>
          查看日志
        </a-button>
        <a-button @click="handleCreateBackup">
          <template #icon><DatabaseOutlined /></template>
          创建备份
        </a-button>
      </a-space>

      <a-space>
        <a-badge :count="alerts.length" :overflow-count="99">
          <a-button @click="handleViewAlerts">
            <BellOutlined /> 告警
          </a-button>
        </a-badge>
        <a-button type="text" @click="handleRefreshData">
          <ReloadOutlined /> 刷新
        </a-button>
      </a-space>
    </div>

    <!-- 系统健康状态 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="系统可用性"
            :value="stats.availability"
            :precision="2"
            suffix="%"
            :value-style="stats.availability >= 99.9 ? { color: '#52c41a' } : { color: '#ff4d4f' }"
            :prefix="() => <CheckCircleOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="活跃实例"
            :value="stats.activeInstances"
            :suffix="`/ ${stats.totalInstances}`"
            :prefix="() => <CloudServerOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="部署成功率"
            :value="stats.deploySuccessRate"
            suffix="%"
            :prefix="() => <RocketOutlined />"
            :value-style="{ color: '#52c41a' }"
          >
            <template #footer>
              <span style="font-size: 12px; color: #8c8c8c">
                目标: 提升15%
              </span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="未处理告警"
            :value="stats.unhandledAlerts"
            :prefix="() => <WarningOutlined />"
            :value-style="stats.unhandledAlerts > 0 ? { color: '#ff4d4f' } : { color: '#52c41a' }"
          >
            <template #suffix>
              <a-button
                v-if="stats.unhandledAlerts > 0"
                type="link"
                size="small"
                @click="handleViewAlerts"
              >
                处理
              </a-button>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 环境监控 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="16">
        <a-card title="环境监控" :bordered="false">
          <template #extra>
            <a-tabs v-model:activeKey="currentEnv" size="small">
              <a-tab-pane key="production" tab="生产环境" />
              <a-tab-pane key="staging" tab="测试环境" />
              <a-tab-pane key="development" tab="开发环境" />
            </a-tabs>
          </template>

          <!-- 服务状态 -->
          <div class="services-status">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="service in services"
                :key="service.name"
                :xs="24"
                :sm="12"
                :md="8"
              >
                <a-card size="small" class="service-card">
                  <div class="service-header">
                    <a-space>
                      <a-badge :status="getServiceStatusBadge(service.status)" />
                      <strong>{{ service.name }}</strong>
                    </a-space>
                    <a-dropdown>
                      <a-button type="text" size="small">
                        <MoreOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleRestartService(service)">
                            <ReloadOutlined /> 重启服务
                          </a-menu-item>
                          <a-menu-item @click="handleViewServiceLogs(service)">
                            <FileTextOutlined /> 查看日志
                          </a-menu-item>
                          <a-menu-item @click="handleScaleService(service)">
                            <ArrowsAltOutlined /> 扩缩容
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>

                  <a-descriptions :column="1" size="small" style="margin-top: 12px">
                    <a-descriptions-item label="实例">
                      {{ service.runningInstances }} / {{ service.totalInstances }}
                    </a-descriptions-item>
                    <a-descriptions-item label="CPU">
                      <a-progress
                        :percent="service.cpuUsage"
                        :stroke-color="getResourceColor(service.cpuUsage)"
                        size="small"
                      />
                    </a-descriptions-item>
                    <a-descriptions-item label="内存">
                      <a-progress
                        :percent="service.memoryUsage"
                        :stroke-color="getResourceColor(service.memoryUsage)"
                        size="small"
                      />
                    </a-descriptions-item>
                    <a-descriptions-item label="响应时间">
                      {{ service.avgResponseTime }}ms
                    </a-descriptions-item>
                  </a-descriptions>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <!-- 系统资源图表 -->
          <a-divider />
          <div class="resource-charts">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <h4>CPU 使用率</h4>
                <CPUUsageChart :data="resourceData.cpu" :env="currentEnv" />
              </a-col>
              <a-col :xs="24" :md="12">
                <h4>内存使用率</h4>
                <MemoryUsageChart :data="resourceData.memory" :env="currentEnv" />
              </a-col>
            </a-row>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <!-- 部署历史 -->
        <a-card title="部署历史" :bordered="false">
          <template #extra>
            <a-button type="link" @click="viewAllDeployments">
              查看全部
            </a-button>
          </template>

          <a-timeline>
            <a-timeline-item
              v-for="deployment in recentDeployments"
              :key="deployment.id"
              :color="getDeploymentColor(deployment.status)"
            >
              <template #dot>
                <CheckCircleOutlined v-if="deployment.status === 'success'" />
                <CloseCircleOutlined v-else-if="deployment.status === 'failed'" />
                <LoadingOutlined v-else />
              </template>

              <p>
                <strong>{{ deployment.projectName }}</strong>
                <a-tag
                  :color="getDeploymentColor(deployment.status)"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ getDeploymentStatusText(deployment.status) }}
                </a-tag>
              </p>
              <p style="color: #8c8c8c; font-size: 12px">
                {{ deployment.environment }} · v{{ deployment.version }}
              </p>
              <p style="color: #8c8c8c; font-size: 12px">
                {{ deployment.deployedBy }} · {{ formatTime(deployment.deployedAt) }}
              </p>
              <a-space size="small">
                <a @click="viewDeploymentDetail(deployment)">查看详情</a>
                <a v-if="deployment.status === 'failed'" @click="retryDeployment(deployment)">
                  重试
                </a>
                <a v-if="deployment.status === 'success'" @click="rollbackDeployment(deployment)">
                  回滚
                </a>
              </a-space>
            </a-timeline-item>
          </a-timeline>
        </a-card>

        <!-- 告警信息 -->
        <a-card
          title="最近告警"
          :bordered="false"
          style="margin-top: 16px"
        >
          <template #extra>
            <a-badge :count="alerts.length" />
          </template>

          <a-list :data-source="alerts" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge
                      :status="getAlertSeverityBadge(item.severity)"
                    />
                  </template>
                  <template #title>
                    {{ item.title }}
                    <a-tag
                      :color="getAlertSeverityColor(item.severity)"
                      size="small"
                    >
                      {{ item.severity }}
                    </a-tag>
                  </template>
                  <template #description>
                    <div>{{ item.message }}</div>
                    <div style="color: #8c8c8c; font-size: 12px; margin-top: 4px">
                      {{ item.serviceName }} · {{ formatTime(item.createdAt) }}
                    </div>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleResolveAlert(item)"
                  >
                    处理
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 部署文档与操作指南 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card :bordered="false">
          <a-tabs v-model:activeKey="docTab">
            <a-tab-pane key="deployDocs" tab="部署文档">
              <DeploymentDocList :env="currentEnv" />
            </a-tab-pane>

            <a-tab-pane key="runbooks" tab="运维手册">
              <RunbookList />
            </a-tab-pane>

            <a-tab-pane key="scripts" tab="脚本库">
              <ScriptLibrary />
            </a-tab-pane>

            <a-tab-pane key="configs" tab="配置管理">
              <ConfigManagement :env="currentEnv" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">
.ops-workspace {
  .quick-actions {
    background: #ffffff;
    padding: 16px 24px;
    margin-bottom: 16px;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .metric-card {
    border-left: 3px solid #13c2c2; // Operations 主题色
  }

  .services-status {
    .service-card {
      border: 1px solid #f0f0f0;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      .service-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }

  .resource-charts {
    margin-top: 16px;

    h4 {
      margin-bottom: 12px;
    }
  }
}
</style>
```

---

## 8. Third-party Personnel (第三方人员) 界面设计

### 8.1 角色特征
```
主要职责:
- 访问受限资源
- 查看指定文档
- 提交反馈
- 参与特定讨论

关键需求:
- 明确的权限范围
- 简化的界面
- 只读/受限操作
- 数据安全保护
- 访问记录可追溯
```

### 8.2 受限访问工作区

```typescript
<template>
  <div class="external-workspace">
    <!-- 顶部信息提示 -->
    <a-alert
      message="您当前以第三方人员身份访问系统"
      description="您的权限受到限制，只能访问被授权的内容。所有操作将被记录。"
      type="info"
      show-icon
      closable
      style="margin-bottom: 16px"
    />

    <!-- 访问概览 -->
    <a-row :gutter="16">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="可访问项目"
            :value="stats.accessibleProjects"
            :prefix="() => <ProjectOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="可查看文档"
            :value="stats.accessibleDocuments"
            :prefix="() => <FileTextOutlined />"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="访问有效期"
            :value="stats.accessDaysLeft"
            suffix="天"
            :prefix="() => <ClockCircleOutlined />"
            :value-style="stats.accessDaysLeft <= 7 ? { color: '#faad14' } : {}"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card">
          <a-statistic
            title="数据安全"
            value="已加密"
            :prefix="() => <SafetyOutlined />"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 可访问资源 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="16">
        <!-- 授权项目 -->
        <a-card title="授权项目" :bordered="false">
          <a-list
            :data-source="authorizedProjects"
            :pagination="{ pageSize: 5 }"
          >
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
                    <a @click="handleViewProject(item)">{{ item.name }}</a>
                    <a-tag
                      color="blue"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getAccessLevelText(item.accessLevel) }}
                    </a-tag>
                  </template>

                  <template #description>
                    <a-space split="|">
                      <span>
                        <FileTextOutlined />
                        {{ item.documentCount }} 个文档
                      </span>
                      <span>
                        <CalendarOutlined />
                        访问期限: {{ formatDate(item.accessExpiresAt) }}
                      </span>
                    </a-space>
                  </template>
                </a-list-item-meta>

                <template #actions>
                  <a-button type="link" @click="handleViewProject(item)">
                    查看详情
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- 文档列表 -->
        <a-card
          title="可访问文档"
          :bordered="false"
          style="margin-top: 16px"
        >
          <template #extra>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索文档..."
              style="width: 200px"
              @search="handleSearch"
            />
          </template>

          <a-table
            :columns="documentColumns"
            :data-source="authorizedDocuments"
            :pagination="pagination"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <a-space>
                  <FileTextOutlined style="font-size: 20px; color: #1890ff" />
                  <div>
                    <a @click="handleViewDocument(record)">{{ record.name }}</a>
                    <div style="color: #8c8c8c; font-size: 12px">
                      {{ record.projectName }}
                    </div>
                  </div>
                </a-space>
              </template>

              <template v-else-if="column.key === 'permissions'">
                <a-space>
                  <a-tag v-if="record.canView" color="green" size="small">
                    <EyeOutlined /> 查看
                  </a-tag>
                  <a-tag v-if="record.canDownload" color="blue" size="small">
                    <DownloadOutlined /> 下载
                  </a-tag>
                  <a-tag v-if="record.canComment" color="orange" size="small">
                    <CommentOutlined /> 评论
                  </a-tag>
                </a-space>
              </template>

              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleViewDocument(record)"
                  >
                    查看
                  </a-button>
                  <a-button
                    v-if="record.canDownload"
                    type="link"
                    size="small"
                    @click="handleDownloadDocument(record)"
                  >
                    下载
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <!-- 访问权限说明 -->
        <a-card title="权限说明" :bordered="false">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="访问级别">
              <a-tag color="blue">受限访问</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="授权人">
              <a-space>
                <a-avatar :src="accessInfo.authorizer.avatar" :size="24" />
                {{ accessInfo.authorizer.name }}
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="授权时间">
              {{ formatTime(accessInfo.grantedAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="有效期至">
              <span :style="{ color: getExpiryColor(accessInfo.expiresAt) }">
                {{ formatDate(accessInfo.expiresAt) }}
              </span>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <div>
            <h4>权限范围</h4>
            <a-space direction="vertical" style="width: 100%">
              <div>
                <CheckOutlined style="color: #52c41a" />
                <span style="margin-left: 8px">查看授权文档</span>
              </div>
              <div>
                <CheckOutlined style="color: #52c41a" />
                <span style="margin-left: 8px">下载指定文件</span>
              </div>
              <div>
                <CheckOutlined style="color: #52c41a" />
                <span style="margin-left: 8px">提交反馈意见</span>
              </div>
              <div>
                <CloseOutlined style="color: #ff4d4f" />
                <span style="margin-left: 8px">编辑任何内容</span>
              </div>
              <div>
                <CloseOutlined style="color: #ff4d4f" />
                <span style="margin-left: 8px">查看敏感数据</span>
              </div>
              <div>
                <CloseOutlined style="color: #ff4d4f" />
                <span style="margin-left: 8px">导出批量数据</span>
              </div>
            </a-space>
          </div>
        </a-card>

        <!-- 帮助与支持 -->
        <a-card
          title="帮助与支持"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-space direction="vertical" style="width: 100%">
            <a-button block @click="handleViewGuide">
              <BookOutlined /> 使用指南
            </a-button>
            <a-button block @click="handleContactSupport">
              <CustomerServiceOutlined /> 联系支持
            </a-button>
            <a-button block @click="handleSubmitFeedback">
              <MessageOutlined /> 提交反馈
            </a-button>
          </a-space>
        </a-card>

        <!-- 访问记录 -->
        <a-card
          title="我的访问记录"
          :bordered="false"
          style="margin-top: 16px"
        >
          <a-list :data-source="accessLogs" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    {{ item.action }}
                  </template>
                  <template #description>
                    <div>{{ item.resourceName }}</div>
                    <div style="color: #8c8c8c; font-size: 12px">
                      {{ formatTime(item.timestamp) }}
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 安全提示 -->
    <a-card
      :bordered="false"
      style="margin-top: 16px; background: #fffbe6; border: 1px solid #ffe58f"
    >
      <a-space>
        <LockOutlined style="color: #faad14; font-size: 20px" />
        <div>
          <h4 style="margin: 0">数据安全提示</h4>
          <p style="margin: 8px 0 0 0; color: #595959">
            所有数据传输均已加密，您的访问行为将被记录用于安全审计。
            请勿尝试访问未授权的资源或执行超出权限的操作。
            如需扩展访问权限，请联系项目管理员。
          </p>
        </div>
      </a-space>
    </a-card>
  </div>
</template>

<style scoped lang="scss">
.external-workspace {
  .metric-card {
    border-left: 3px solid #8c8c8c; // Third-party 主题色（中性灰）
  }
}
</style>
```

---

## 9. 角色切换与权限可视化

### 9.1 角色切换器（仅超级管理员）

```typescript
<template>
  <a-dropdown v-if="currentUser.role === 'admin'">
    <a-button type="dashed" size="small">
      <SwapOutlined /> 切换角色视图
    </a-button>
    <template #overlay>
      <a-menu @click="handleSwitchRole">
        <a-menu-item key="admin">
          <a-badge :status="currentRole === 'admin' ? 'processing' : 'default'" />
          超级管理员
        </a-menu-item>
        <a-menu-item key="pm">
          <a-badge :status="currentRole === 'pm' ? 'processing' : 'default'" />
          项目经理
        </a-menu-item>
        <a-menu-item key="developer">
          <a-badge :status="currentRole === 'developer' ? 'processing' : 'default'" />
          开发人员
        </a-menu-item>
        <a-menu-item key="tester">
          <a-badge :status="currentRole === 'tester' ? 'processing' : 'default'" />
          测试人员
        </a-menu-item>
        <a-menu-item key="designer">
          <a-badge :status="currentRole === 'designer' ? 'processing' : 'default'" />
          UI设计师
        </a-menu-item>
        <a-menu-item key="ops">
          <a-badge :status="currentRole === 'ops' ? 'processing' : 'default'" />
          运维人员
        </a-menu-item>
        <a-menu-item key="external">
          <a-badge :status="currentRole === 'external' ? 'processing' : 'default'" />
          第三方人员
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>
```

### 9.2 权限提示组件

```typescript
<template>
  <!-- 无权限提示 -->
  <a-result
    v-if="!hasPermission"
    status="403"
    title="权限不足"
    sub-title="抱歉，您没有权限访问此功能"
  >
    <template #extra>
      <a-button type="primary" @click="goBack">
        返回
      </a-button>
      <a-button @click="requestAccess">
        申请权限
      </a-button>
    </template>
  </a-result>

  <!-- 禁用操作提示 -->
  <a-tooltip v-else-if="isDisabled" title="您没有执行此操作的权限">
    <a-button :disabled="true">
      <LockOutlined /> 受限操作
    </a-button>
  </a-tooltip>
</template>
```

---

## 10. 角色界面设计总结

### 10.1 设计对比表

| 角色 | 主题色 | 核心功能 | 效率目标 | 关键指标 |
|------|--------|----------|----------|----------|
| Super Administrator | Deep Purple (#722ed1) | 系统管理、用户权限 | 管理效率+50% | 系统可用性 99.9%+ |
| Project Manager | Royal Blue (#1890ff) | 项目协调、资源管理 | 项目延期-25% | 整体进度可视化 |
| Developer | Tech Green (#52c41a) | 任务执行、文档查阅 | 开发效率+20% | 任务完成速度 |
| Tester | Alert Orange (#fa8c16) | 测试用例、缺陷管理 | 测试效率+40% | 用例复用率 70%+ |
| UI Designer | Creative Magenta (#eb2f96) | 设计管理、评审反馈 | 评审周期-50% | 平均评审时间 |
| Operations | Stable Cyan (#13c2c2) | 部署监控、系统维护 | 部署成功率+15% | 系统可用性监控 |
| Third-party | Neutral Gray (#8c8c8c) | 受限访问、文档查看 | 零数据泄露 | 访问安全审计 |

### 10.2 共同设计原则

1. **一致性**: 所有角色使用相同的设计系统基础
2. **差异化**: 通过主题色和布局突出角色特点
3. **可访问性**: 符合 WCAG 2.1 AA 标准
4. **响应式**: 适配桌面、平板、移动端
5. **性能优化**: 页面加载 < 3秒，API响应 < 300ms

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**下一部分**: Part 4 - 核心功能UI/UX设计