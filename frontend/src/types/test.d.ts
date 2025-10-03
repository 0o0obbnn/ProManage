/**
 * 测试管理类型定义
 */

/**
 * 测试用例类型枚举
 */
export enum TestCaseType {
  FUNCTIONAL = 'FUNCTIONAL',
  PERFORMANCE = 'PERFORMANCE',
  SECURITY = 'SECURITY',
  INTEGRATION = 'INTEGRATION',
  UNIT = 'UNIT'
}

/**
 * 测试用例优先级枚举
 */
export enum TestCasePriority {
  CRITICAL = 'CRITICAL',
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW'
}

/**
 * 测试用例状态枚举
 */
export enum TestCaseStatus {
  DRAFT = 'DRAFT',
  READY = 'READY',
  PASSED = 'PASSED',
  FAILED = 'FAILED',
  BLOCKED = 'BLOCKED',
  SKIPPED = 'SKIPPED'
}

/**
 * 缺陷严重程度枚举
 */
export enum BugSeverity {
  BLOCKER = 'BLOCKER',
  CRITICAL = 'CRITICAL',
  MAJOR = 'MAJOR',
  MINOR = 'MINOR',
  TRIVIAL = 'TRIVIAL'
}

/**
 * 缺陷状态枚举
 */
export enum BugStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  REOPENED = 'REOPENED'
}

/**
 * 测试步骤接口
 */
export interface TestStep {
  id: number
  stepNumber: number
  description: string
  expectedResult: string
  actualResult?: string
  status?: 'passed' | 'failed' | 'blocked'
}

/**
 * 测试用例接口
 */
export interface TestCase {
  id: number
  title: string
  description: string
  priority: TestCasePriority
  status: TestCaseStatus
  type: TestCaseType
  projectId: number
  projectName?: string
  moduleId?: number
  moduleName?: string
  preconditions?: string
  steps: TestStep[]
  testData?: string
  attachments?: {
    id: number
    name: string
    url: string
    size: number
  }[]
  assignee?: {
    id: number
    name: string
    avatar?: string
  }
  author: {
    id: number
    name: string
    avatar?: string
  }
  tags: string[]
  linkedBugs?: number[]
  executionCount: number
  passedCount: number
  failedCount: number
  lastExecutionTime?: string
  createdAt: string
  updatedAt: string
}

/**
 * 测试用例查询参数
 */
export interface TestCaseQueryParams {
  page: number
  pageSize: number
  keyword?: string
  projectId?: number
  moduleId?: number
  type?: TestCaseType
  priority?: TestCasePriority
  status?: TestCaseStatus
  assigneeId?: number
  authorId?: number
  tags?: string[]
  startDate?: string
  endDate?: string
  sort?: 'title' | 'priority' | 'status' | 'createdAt' | 'updatedAt'
  order?: 'asc' | 'desc'
}

/**
 * 测试执行记录接口
 */
export interface TestExecution {
  id: number
  testCaseId: number
  testCaseTitle: string
  testPlanId?: number
  executor: {
    id: number
    name: string
    avatar?: string
  }
  status: 'passed' | 'failed' | 'blocked' | 'skipped'
  steps: {
    stepId: number
    status: 'passed' | 'failed' | 'blocked'
    actualResult?: string
    screenshot?: string
  }[]
  notes?: string
  duration?: number
  environment?: string
  attachments?: {
    id: number
    name: string
    url: string
    size: number
  }[]
  bugIds?: number[]
  executedAt: string
}

/**
 * 测试计划接口
 */
export interface TestPlan {
  id: number
  name: string
  description: string
  projectId: number
  projectName?: string
  startDate: string
  endDate: string
  status: 'planning' | 'in_progress' | 'completed' | 'cancelled'
  testCaseIds: number[]
  testCaseCount: number
  passedCount: number
  failedCount: number
  blockedCount: number
  skippedCount: number
  progress: number
  owner: {
    id: number
    name: string
    avatar?: string
  }
  members?: {
    id: number
    name: string
    avatar?: string
  }[]
  createdAt: string
  updatedAt: string
}

/**
 * 缺陷接口
 */
export interface Bug {
  id: number
  title: string
  description: string
  severity: BugSeverity
  priority: TestCasePriority
  status: BugStatus
  projectId: number
  projectName?: string
  moduleId?: number
  moduleName?: string
  environment?: string
  stepsToReproduce: string[]
  expectedBehavior?: string
  actualBehavior?: string
  attachments?: {
    id: number
    name: string
    url: string
    size: number
  }[]
  assignee?: {
    id: number
    name: string
    avatar?: string
  }
  reporter: {
    id: number
    name: string
    avatar?: string
  }
  linkedTestCases?: number[]
  tags: string[]
  comments?: BugComment[]
  statusHistory?: {
    status: BugStatus
    changedBy: string
    changedAt: string
    comment?: string
  }[]
  createdAt: string
  updatedAt: string
  resolvedAt?: string
  closedAt?: string
}

/**
 * 缺陷查询参数
 */
export interface BugQueryParams {
  page: number
  pageSize: number
  keyword?: string
  projectId?: number
  moduleId?: number
  severity?: BugSeverity
  priority?: TestCasePriority
  status?: BugStatus
  assigneeId?: number
  reporterId?: number
  tags?: string[]
  startDate?: string
  endDate?: string
  sort?: 'title' | 'severity' | 'priority' | 'status' | 'createdAt' | 'updatedAt'
  order?: 'asc' | 'desc'
}

/**
 * 缺陷评论接口
 */
export interface BugComment {
  id: number
  bugId: number
  content: string
  author: {
    id: number
    name: string
    avatar?: string
  }
  attachments?: {
    id: number
    name: string
    url: string
  }[]
  createdAt: string
  updatedAt: string
}

/**
 * 测试统计信息
 */
export interface TestStatistics {
  testCaseStats: {
    total: number
    draft: number
    ready: number
    passed: number
    failed: number
    blocked: number
    skipped: number
    passRate: number
  }
  bugStats: {
    total: number
    open: number
    inProgress: number
    resolved: number
    closed: number
    reopened: number
    byPriority: {
      critical: number
      high: number
      medium: number
      low: number
    }
    bySeverity: {
      blocker: number
      critical: number
      major: number
      minor: number
      trivial: number
    }
  }
  executionStats: {
    totalExecutions: number
    recentExecutions: TestExecution[]
    passRate: number
    avgDuration: number
  }
  testPlanStats: {
    total: number
    planning: number
    inProgress: number
    completed: number
    cancelled: number
  }
}

/**
 * 测试用例创建/更新参数
 */
export interface TestCaseFormData {
  title: string
  description: string
  priority: TestCasePriority
  type: TestCaseType
  projectId: number
  moduleId?: number
  preconditions?: string
  steps: Omit<TestStep, 'id'>[]
  testData?: string
  assigneeId?: number
  tags?: string[]
}

/**
 * 缺陷创建/更新参数
 */
export interface BugFormData {
  title: string
  description: string
  severity: BugSeverity
  priority: TestCasePriority
  projectId: number
  moduleId?: number
  environment?: string
  stepsToReproduce: string[]
  expectedBehavior?: string
  actualBehavior?: string
  assigneeId?: number
  tags?: string[]
  linkedTestCases?: number[]
}

/**
 * 测试执行参数
 */
export interface TestExecutionData {
  testCaseId: number
  testPlanId?: number
  status: 'passed' | 'failed' | 'blocked' | 'skipped'
  steps: {
    stepId: number
    status: 'passed' | 'failed' | 'blocked'
    actualResult?: string
  }[]
  notes?: string
  duration?: number
  environment?: string
  createBug?: BugFormData
}
