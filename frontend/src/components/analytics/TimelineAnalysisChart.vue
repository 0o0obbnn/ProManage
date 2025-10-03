<template>
  <div class="timeline-analysis-chart">
    <a-spin :spinning="loading">
      <div v-if="timelineAnalysis" class="timeline-content">
        <!-- 阶段时间线 -->
        <div v-if="timelineAnalysis.phases.length" class="phases-section">
          <h4>项目阶段</h4>
          <div class="phases-timeline">
            <div
              v-for="phase in timelineAnalysis.phases"
              :key="phase.name"
              class="phase-item"
              :style="{ backgroundColor: phase.color }"
            >
              <div class="phase-info">
                <div class="phase-name">{{ phase.name }}</div>
                <div class="phase-dates">
                  {{ formatDate(phase.startDate) }} - {{ formatDate(phase.endDate) }}
                </div>
              </div>
              <a-progress
                :percent="phase.progress"
                :show-info="true"
                :stroke-color="phase.color"
                class="phase-progress"
              />
            </div>
          </div>
        </div>

        <!-- 事件时间线 -->
        <div class="events-section">
          <h4>关键事件</h4>
          <a-timeline mode="left">
            <a-timeline-item
              v-for="event in timelineAnalysis.events"
              :key="event.id"
              :color="getEventColor(event.importance)"
            >
              <template #dot>
                <component
                  :is="getEventIcon(event.type)"
                  :style="{ fontSize: '16px' }"
                />
              </template>

              <div class="event-card">
                <div class="event-header">
                  <div class="event-title">
                    <strong>{{ event.title }}</strong>
                    <a-tag :color="getImportanceColor(event.importance)" size="small">
                      {{ getImportanceText(event.importance) }}
                    </a-tag>
                  </div>
                  <div class="event-date">{{ formatDateTime(event.date) }}</div>
                </div>

                <div v-if="event.description" class="event-description" v-html="sanitizeHtml(event.description)">
                </div>

                <div v-if="event.relatedItems && event.relatedItems.length" class="event-related">
                  <a-space :size="4" wrap>
                    <a-tag
                      v-for="item in event.relatedItems"
                      :key="item.id"
                      size="small"
                    >
                      {{ item.type }}: {{ item.name }}
                    </a-tag>
                  </a-space>
                </div>

                <div class="event-status">
                  <a-badge
                    :status="getStatusBadge(event.status)"
                    :text="getStatusText(event.status)"
                  />
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>

      <a-empty v-else description="暂无时间线数据" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, onMounted } from 'vue'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  FlagOutlined,
  RocketOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { sanitizeHtml } from '@/utils/security'

interface Props {
  projectId?: string
  dateRange?: [string, string]
}

const props = defineProps<Props>()

const analyticsStore = useAnalyticsStore()

const loading = computed(() => analyticsStore.tableLoading)
const timelineAnalysis = computed(() => analyticsStore.timelineAnalysis)

const getEventIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    milestone: FlagOutlined,
    release: RocketOutlined,
    change: ClockCircleOutlined,
    incident: WarningOutlined
  }
  return iconMap[type] || CheckCircleOutlined
}

const getEventColor = (importance: string) => {
  const colorMap: Record<string, string> = {
    critical: 'red',
    high: 'orange',
    medium: 'blue',
    low: 'gray'
  }
  return colorMap[importance] || 'blue'
}

const getImportanceColor = (importance: string) => {
  const colorMap: Record<string, string> = {
    critical: 'error',
    high: 'warning',
    medium: 'processing',
    low: 'default'
  }
  return colorMap[importance] || 'default'
}

const getImportanceText = (importance: string) => {
  const textMap: Record<string, string> = {
    critical: '关键',
    high: '重要',
    medium: '一般',
    low: '较低'
  }
  return textMap[importance] || importance
}

const getStatusBadge = (status: string) => {
  const badgeMap: Record<string, any> = {
    completed: 'success',
    in_progress: 'processing',
    pending: 'default'
  }
  return badgeMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    completed: '已完成',
    in_progress: '进行中',
    pending: '待处理'
  }
  return textMap[status] || status
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchData = async () => {
  await analyticsStore.fetchTimelineAnalysis()
}

watch(
  () => [props.projectId, props.dateRange],
  () => {
    if (props.projectId) {
      analyticsStore.setSelectedProject(props.projectId)
    }
    if (props.dateRange) {
      analyticsStore.setDateRange(props.dateRange)
    }
    fetchData()
  },
  { deep: true }
)

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.timeline-analysis-chart {
  .timeline-content {
    .phases-section {
      margin-bottom: 32px;

      h4 {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }

      .phases-timeline {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .phase-item {
          padding: 16px;
          border-radius: 8px;
          background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));

          .phase-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .phase-name {
              font-weight: 600;
              font-size: 14px;
              color: #262626;
            }

            .phase-dates {
              font-size: 12px;
              color: #595959;
            }
          }

          .phase-progress {
            margin: 0;
          }
        }
      }
    }

    .events-section {
      h4 {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }

      .event-card {
        padding: 12px;
        background: #fafafa;
        border-radius: 4px;
        border-left: 3px solid #1890ff;

        .event-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 8px;

          .event-title {
            display: flex;
            align-items: center;
            gap: 8px;
            flex: 1;

            strong {
              color: #262626;
              font-size: 14px;
            }
          }

          .event-date {
            color: #8c8c8c;
            font-size: 12px;
            white-space: nowrap;
            margin-left: 8px;
          }
        }

        .event-description {
          color: #595959;
          font-size: 13px;
          line-height: 1.6;
          margin-bottom: 8px;
        }

        .event-related {
          margin-bottom: 8px;
        }

        .event-status {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
