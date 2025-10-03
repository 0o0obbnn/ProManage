/**
 * 时间范围枚举
 */
export enum TimeRange {
  WEEK = 'week',
  MONTH = 'month',
  QUARTER = 'quarter',
  YEAR = 'year'
}

/**
 * 图表类型枚举
 */
export enum ChartType {
  LINE = 'line',
  BAR = 'bar',
  PIE = 'pie',
  AREA = 'area'
}

/**
 * 仪表板关键指标
 */
export interface DashboardStats {
  completionRate: number;
  completionRateDelta: number;
  onTimeRate: number;
  onTimeRateDelta: number;
  collaborationScore: number;
  collaborationScoreDelta: number;
  testReuseRate: number;
  testReuseRateDelta: number;
}

/**
 * 项目进度
 */
export interface ProjectProgress {
  id: string;
  name: string;
  progress: number;
  status: 'on_track' | 'at_risk' | 'delayed';
  color: string;
  startDate: string;
  endDate: string;
  tasksTotal: number;
  tasksCompleted: number;
  membersCount: number;
}

/**
 * 任务趋势数据点
 */
export interface TaskTrendDataPoint {
  date: string;
  created: number;
  inProgress: number;
  completed: number;
  total: number;
}

/**
 * 任务趋势数据
 */
export interface TaskTrendData {
  period: TimeRange;
  data: TaskTrendDataPoint[];
}

/**
 * 团队成员工作量
 */
export interface MemberWorkload {
  userId: string;
  userName: string;
  avatar?: string;
  todo: number;
  inProgress: number;
  completed: number;
  total: number;
  efficiency: number;
}

/**
 * 团队工作量数据
 */
export interface TeamWorkload {
  members: MemberWorkload[];
  avgWorkload: number;
  maxWorkload: number;
}

/**
 * 缺陷数据项
 */
export interface BugDataItem {
  name: string;
  value: number;
  percentage: number;
  color?: string;
}

/**
 * 缺陷分析数据
 */
export interface BugAnalysis {
  bySeverity: BugDataItem[];
  byModule: BugDataItem[];
  byStatus: BugDataItem[];
  total: number;
  resolved: number;
  pending: number;
  resolveRate: number;
}

/**
 * 文档活跃度数据点
 */
export interface DocumentActivityPoint {
  date: string;
  created: number;
  updated: number;
  views: number;
}

/**
 * 文档活跃度
 */
export interface DocumentActivity {
  data: DocumentActivityPoint[];
  stats: {
    total: number;
    todayCreated: number;
    todayUpdated: number;
    avgViews: number;
    activeDocuments: number;
  };
}

/**
 * 变更影响数据点
 */
export interface ChangeImpactPoint {
  date: string;
  changes: number;
  affectedModules: number;
  notifiedUsers: number;
  avgResponseTime: number;
}

/**
 * 变更影响数据
 */
export interface ChangeImpactData {
  data: ChangeImpactPoint[];
  stats: {
    weeklyChanges: number;
    autoNotifyRate: number;
    avgResponseTime: number;
    criticalChanges: number;
  };
}

/**
 * 任务明细
 */
export interface TaskDetail {
  id: string;
  title: string;
  status: string;
  priority: string;
  assignee: {
    id: string;
    name: string;
    avatar?: string;
  };
  startDate: string;
  endDate?: string;
  completedDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  projectName: string;
  progress: number;
}

/**
 * 成员贡献数据
 */
export interface MemberContribution {
  userId: string;
  userName: string;
  avatar?: string;
  tasksCompleted: number;
  codeCommits: number;
  documentsUpdated: number;
  comments: number;
  reviewsCompleted: number;
  contributionScore: number;
  trend: 'up' | 'down' | 'stable';
}

/**
 * 质量指标
 */
export interface QualityMetrics {
  projectId: string;
  projectName: string;
  testCoverage: number;
  bugDensity: number;
  codeQuality: number;
  documentationScore: number;
  overallScore: number;
  trend: 'up' | 'down' | 'stable';
  status: 'excellent' | 'good' | 'fair' | 'poor';
}

/**
 * 时间线事件
 */
export interface TimelineEvent {
  id: string;
  type: 'milestone' | 'release' | 'change' | 'incident';
  title: string;
  description?: string;
  date: string;
  status: 'completed' | 'in_progress' | 'pending';
  importance: 'critical' | 'high' | 'medium' | 'low';
  relatedItems?: {
    type: string;
    id: string;
    name: string;
  }[];
}

/**
 * 时间线分析数据
 */
export interface TimelineAnalysis {
  events: TimelineEvent[];
  phases: {
    name: string;
    startDate: string;
    endDate: string;
    progress: number;
    color: string;
  }[];
}

/**
 * 报告类型枚举
 */
export enum ReportType {
  PROJECT_OVERVIEW = 'project_overview',
  TASK_STATISTICS = 'task_statistics',
  QUALITY_REPORT = 'quality_report',
  TEAM_ANALYSIS = 'team_analysis'
}

/**
 * 报告配置
 */
export interface ReportConfig {
  id?: string;
  name: string;
  description?: string;
  type: ReportType;
  projectId?: string;
  dateRange?: {
    start: string;
    end: string;
  };
  includeCharts: boolean;
  includeTables: boolean;
  includeStats: boolean;
  saveAsTemplate: boolean;
  schedule?: {
    enabled: boolean;
    frequency: 'daily' | 'weekly' | 'monthly';
    recipients: string[];
    channels: ('email' | 'notification')[];
    format: 'pdf' | 'excel';
  };
}

/**
 * 自定义报告
 */
export interface CustomReport {
  id: string;
  name: string;
  description?: string;
  type: ReportType;
  config: ReportConfig;
  createdAt: string;
  createdBy: {
    id: string;
    name: string;
  };
  lastGenerated?: string;
  fileUrl?: string;
}

/**
 * 图表配置选项
 */
export interface ChartOptions {
  title?: string;
  legend?: boolean;
  grid?: {
    top?: number;
    right?: number;
    bottom?: number;
    left?: number;
  };
  tooltip?: boolean;
  dataZoom?: boolean;
  exportable?: boolean;
  animation?: boolean;
}

/**
 * 导出报告参数
 */
export interface ExportReportParams {
  type: ReportType;
  projectId?: string;
  dateRange?: {
    start: string;
    end: string;
  };
  format: 'pdf' | 'excel' | 'csv';
  includeCharts?: boolean;
  includeTables?: boolean;
}

/**
 * 分析查询参数
 */
export interface AnalyticsQueryParams {
  projectId?: string;
  dateRange?: {
    start: string;
    end: string;
  };
  period?: TimeRange;
  groupBy?: string;
  limit?: number;
}
