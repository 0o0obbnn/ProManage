/**
 * 测试管理 API
 */
import { get, post, put, del } from '../request'
import type {
  TestCase,
  TestCaseQueryParams,
  TestCaseFormData,
  TestExecution,
  TestExecutionData,
  TestPlan,
  Bug,
  BugQueryParams,
  BugFormData,
  BugComment,
  TestStatistics
} from '@/types/test'
import type { PageResult } from '@/types/global'

/**
 * 获取测试用例列表
 */
export function getTestCaseList(params: TestCaseQueryParams) {
  return get<PageResult<TestCase>>('/test/cases', { params })
}

/**
 * 获取测试用例详情
 */
export function getTestCaseDetail(id: number) {
  return get<TestCase>(`/test/cases/${id}`)
}

/**
 * 创建测试用例
 */
export function createTestCase(data: TestCaseFormData) {
  return post<TestCase>('/test/cases', data)
}

/**
 * 更新测试用例
 */
export function updateTestCase(id: number, data: Partial<TestCaseFormData>) {
  return put<TestCase>(`/test/cases/${id}`, data)
}

/**
 * 删除测试用例
 */
export function deleteTestCase(id: number) {
  return del(`/test/cases/${id}`)
}

/**
 * 批量删除测试用例
 */
export function batchDeleteTestCases(ids: number[]) {
  return post('/test/cases/batch-delete', { ids })
}

/**
 * 复制测试用例
 */
export function copyTestCase(id: number) {
  return post<TestCase>(`/test/cases/${id}/copy`)
}

/**
 * 批量复制测试用例
 */
export function batchCopyTestCases(ids: number[]) {
  return post<TestCase[]>('/test/cases/batch-copy', { ids })
}

/**
 * 执行测试用例
 */
export function executeTestCase(data: TestExecutionData) {
  return post<TestExecution>('/test/executions', data)
}

/**
 * 批量执行测试用例
 */
export function batchExecuteTestCases(testCaseIds: number[], testPlanId?: number) {
  return post('/test/cases/batch-execute', { testCaseIds, testPlanId })
}

/**
 * 获取测试用例执行历史
 */
export function getTestCaseExecutions(testCaseId: number) {
  return get<TestExecution[]>(`/test/cases/${testCaseId}/executions`)
}

/**
 * 获取测试计划列表
 */
export function getTestPlans(projectId?: number) {
  return get<TestPlan[]>('/test/plans', { params: { projectId } })
}

/**
 * 获取测试计划详情
 */
export function getTestPlanDetail(id: number) {
  return get<TestPlan>(`/test/plans/${id}`)
}

/**
 * 创建测试计划
 */
export function createTestPlan(data: {
  name: string
  description: string
  projectId: number
  startDate: string
  endDate: string
  testCaseIds: number[]
  memberIds?: number[]
}) {
  return post<TestPlan>('/test/plans', data)
}

/**
 * 更新测试计划
 */
export function updateTestPlan(id: number, data: Partial<{
  name: string
  description: string
  startDate: string
  endDate: string
  testCaseIds: number[]
  memberIds?: number[]
  status: string
}>) {
  return put<TestPlan>(`/test/plans/${id}`, data)
}

/**
 * 删除测试计划
 */
export function deleteTestPlan(id: number) {
  return del(`/test/plans/${id}`)
}

/**
 * 获取缺陷列表
 */
export function getBugList(params: BugQueryParams) {
  return get<PageResult<Bug>>('/test/bugs', { params })
}

/**
 * 获取缺陷详情
 */
export function getBugDetail(id: number) {
  return get<Bug>(`/test/bugs/${id}`)
}

/**
 * 创建缺陷
 */
export function createBug(data: BugFormData) {
  return post<Bug>('/test/bugs', data)
}

/**
 * 更新缺陷
 */
export function updateBug(id: number, data: Partial<BugFormData>) {
  return put<Bug>(`/test/bugs/${id}`, data)
}

/**
 * 更新缺陷状态
 */
export function updateBugStatus(id: number, status: string, comment?: string) {
  return put<Bug>(`/test/bugs/${id}/status`, { status, comment })
}

/**
 * 删除缺陷
 */
export function deleteBug(id: number) {
  return del(`/test/bugs/${id}`)
}

/**
 * 批量删除缺陷
 */
export function batchDeleteBugs(ids: number[]) {
  return post('/test/bugs/batch-delete', { ids })
}

/**
 * 获取缺陷评论
 */
export function getBugComments(bugId: number) {
  return get<BugComment[]>(`/test/bugs/${bugId}/comments`)
}

/**
 * 添加缺陷评论
 */
export function addBugComment(bugId: number, content: string) {
  return post<BugComment>(`/test/bugs/${bugId}/comments`, { content })
}

/**
 * 删除缺陷评论
 */
export function deleteBugComment(bugId: number, commentId: number) {
  return del(`/test/bugs/${bugId}/comments/${commentId}`)
}

/**
 * 获取测试统计信息
 */
export function getTestStatistics(projectId?: number) {
  return get<TestStatistics>('/test/statistics', { params: { projectId } })
}

/**
 * 导出测试用例
 */
export function exportTestCases(params: Partial<TestCaseQueryParams>) {
  return get<Blob>('/test/cases/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导入测试用例
 */
export function importTestCases(file: File, projectId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('projectId', String(projectId))

  return post<{ successCount: number; failCount: number; errors: string[] }>(
    '/test/cases/import',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
}

/**
 * 上传测试附件
 */
export function uploadTestAttachment(file: File, type: 'testcase' | 'bug' | 'execution') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return post<{ id: number; name: string; url: string; size: number }>(
    '/test/attachments/upload',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
}

/**
 * 获取标签列表
 */
export function getTestTags(projectId?: number) {
  return get<string[]>('/test/tags', { params: { projectId } })
}

/**
 * 获取模块列表
 */
export function getTestModules(projectId: number) {
  return get<{ id: number; name: string }[]>('/test/modules', { params: { projectId } })
}
