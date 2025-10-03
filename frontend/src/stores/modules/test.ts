/**
 * 测试管理状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  TestCase,
  TestCaseQueryParams,
  TestExecution,
  TestPlan,
  Bug,
  BugQueryParams,
  TestStatistics
} from '@/types/test'
import type { PageResult } from '@/types/global'
import * as testApi from '@/api/modules/test'
import { message } from 'ant-design-vue'

export const useTestStore = defineStore('test', () => {
  // State - Test Cases
  const testCaseList = ref<TestCase[]>([])
  const currentTestCase = ref<TestCase | null>(null)
  const testCaseExecutions = ref<TestExecution[]>([])
  const testCasePagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })
  const testCaseQueryParams = ref<Partial<TestCaseQueryParams>>({
    keyword: '',
    projectId: undefined,
    moduleId: undefined,
    type: undefined,
    priority: undefined,
    status: undefined,
    assigneeId: undefined,
    tags: [],
    sort: 'createdAt',
    order: 'desc'
  })

  // State - Test Plans
  const testPlans = ref<TestPlan[]>([])
  const currentTestPlan = ref<TestPlan | null>(null)

  // State - Bugs
  const bugList = ref<Bug[]>([])
  const currentBug = ref<Bug | null>(null)
  const bugPagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })
  const bugQueryParams = ref<Partial<BugQueryParams>>({
    keyword: '',
    projectId: undefined,
    moduleId: undefined,
    severity: undefined,
    priority: undefined,
    status: undefined,
    assigneeId: undefined,
    tags: [],
    sort: 'createdAt',
    order: 'desc'
  })

  // State - Common
  const statistics = ref<TestStatistics | null>(null)
  const tags = ref<string[]>([])
  const modules = ref<{ id: number; name: string }[]>([])
  const loading = ref(false)
  const selectedTestCaseIds = ref<number[]>([])
  const selectedBugIds = ref<number[]>([])

  // Getters - Test Cases
  const hasTestCases = computed(() => testCaseList.value.length > 0)
  const selectedTestCases = computed(() =>
    testCaseList.value.filter(tc => selectedTestCaseIds.value.includes(tc.id))
  )
  const selectedTestCaseCount = computed(() => selectedTestCaseIds.value.length)

  // Getters - Bugs
  const hasBugs = computed(() => bugList.value.length > 0)
  const selectedBugs = computed(() =>
    bugList.value.filter(bug => selectedBugIds.value.includes(bug.id))
  )
  const selectedBugCount = computed(() => selectedBugIds.value.length)

  /**
   * 获取测试用例列表
   */
  const fetchTestCases = async (params?: Partial<TestCaseQueryParams>) => {
    try {
      loading.value = true

      const mergedParams: TestCaseQueryParams = {
        page: testCasePagination.value.page,
        pageSize: testCasePagination.value.pageSize,
        ...testCaseQueryParams.value,
        ...params
      }

      const result = await testApi.getTestCaseList(mergedParams)

      testCaseList.value = result.list
      testCasePagination.value.total = result.total
      testCasePagination.value.page = result.page
      testCasePagination.value.pageSize = result.pageSize
      testCasePagination.value.totalPages = result.totalPages

      return result
    } catch (error) {
      console.error('Fetch test cases failed:', error)
      message.error('获取测试用例列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取测试用例详情
   */
  const fetchTestCaseDetail = async (id: number) => {
    try {
      loading.value = true
      const testCase = await testApi.getTestCaseDetail(id)
      currentTestCase.value = testCase
      return testCase
    } catch (error) {
      console.error('Fetch test case detail failed:', error)
      message.error('获取测试用例详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建测试用例
   */
  const createTestCase = async (data: any) => {
    try {
      const testCase = await testApi.createTestCase(data)
      message.success('测试用例创建成功')
      await fetchTestCases()
      return testCase
    } catch (error) {
      console.error('Create test case failed:', error)
      message.error('测试用例创建失败')
      throw error
    }
  }

  /**
   * 更新测试用例
   */
  const updateTestCase = async (id: number, data: any) => {
    try {
      const testCase = await testApi.updateTestCase(id, data)

      const index = testCaseList.value.findIndex(tc => tc.id === id)
      if (index !== -1) {
        testCaseList.value[index] = testCase
      }

      if (currentTestCase.value?.id === id) {
        currentTestCase.value = testCase
      }

      message.success('测试用例更新成功')
      return testCase
    } catch (error) {
      console.error('Update test case failed:', error)
      message.error('测试用例更新失败')
      throw error
    }
  }

  /**
   * 删除测试用例
   */
  const deleteTestCase = async (id: number) => {
    try {
      await testApi.deleteTestCase(id)
      testCaseList.value = testCaseList.value.filter(tc => tc.id !== id)
      testCasePagination.value.total -= 1
      message.success('测试用例删除成功')
      return true
    } catch (error) {
      console.error('Delete test case failed:', error)
      message.error('测试用例删除失败')
      throw error
    }
  }

  /**
   * 批量删除测试用例
   */
  const batchDeleteTestCases = async (ids: number[]) => {
    try {
      await testApi.batchDeleteTestCases(ids)
      message.success(`成功删除 ${ids.length} 个测试用例`)
      await fetchTestCases()
      selectedTestCaseIds.value = []
      return true
    } catch (error) {
      console.error('Batch delete test cases failed:', error)
      message.error('批量删除失败')
      throw error
    }
  }

  /**
   * 复制测试用例
   */
  const copyTestCase = async (id: number) => {
    try {
      const testCase = await testApi.copyTestCase(id)
      message.success('测试用例复制成功')
      await fetchTestCases()
      return testCase
    } catch (error) {
      console.error('Copy test case failed:', error)
      message.error('测试用例复制失败')
      throw error
    }
  }

  /**
   * 批量复制测试用例
   */
  const batchCopyTestCases = async (ids: number[]) => {
    try {
      const testCases = await testApi.batchCopyTestCases(ids)
      message.success(`成功复制 ${ids.length} 个测试用例`)
      await fetchTestCases()
      return testCases
    } catch (error) {
      console.error('Batch copy test cases failed:', error)
      message.error('批量复制失败')
      throw error
    }
  }

  /**
   * 执行测试用例
   */
  const executeTestCase = async (data: any) => {
    try {
      const execution = await testApi.executeTestCase(data)
      message.success('测试用例执行成功')
      await fetchTestCases()
      return execution
    } catch (error) {
      console.error('Execute test case failed:', error)
      message.error('测试用例执行失败')
      throw error
    }
  }

  /**
   * 批量执行测试用例
   */
  const batchExecuteTestCases = async (testCaseIds: number[], testPlanId?: number) => {
    try {
      await testApi.batchExecuteTestCases(testCaseIds, testPlanId)
      message.success(`已开始执行 ${testCaseIds.length} 个测试用例`)
      await fetchTestCases()
    } catch (error) {
      console.error('Batch execute test cases failed:', error)
      message.error('批量执行失败')
      throw error
    }
  }

  /**
   * 获取测试用例执行历史
   */
  const fetchTestCaseExecutions = async (testCaseId: number) => {
    try {
      const executions = await testApi.getTestCaseExecutions(testCaseId)
      testCaseExecutions.value = executions
      return executions
    } catch (error) {
      console.error('Fetch test case executions failed:', error)
      message.error('获取执行历史失败')
      throw error
    }
  }

  /**
   * 获取测试计划列表
   */
  const fetchTestPlans = async (projectId?: number) => {
    try {
      const plans = await testApi.getTestPlans(projectId)
      testPlans.value = plans
      return plans
    } catch (error) {
      console.error('Fetch test plans failed:', error)
      message.error('获取测试计划失败')
      throw error
    }
  }

  /**
   * 创建测试计划
   */
  const createTestPlan = async (data: any) => {
    try {
      const plan = await testApi.createTestPlan(data)
      message.success('测试计划创建成功')
      await fetchTestPlans(data.projectId)
      return plan
    } catch (error) {
      console.error('Create test plan failed:', error)
      message.error('测试计划创建失败')
      throw error
    }
  }

  /**
   * 获取缺陷列表
   */
  const fetchBugs = async (params?: Partial<BugQueryParams>) => {
    try {
      loading.value = true

      const mergedParams: BugQueryParams = {
        page: bugPagination.value.page,
        pageSize: bugPagination.value.pageSize,
        ...bugQueryParams.value,
        ...params
      }

      const result = await testApi.getBugList(mergedParams)

      bugList.value = result.list
      bugPagination.value.total = result.total
      bugPagination.value.page = result.page
      bugPagination.value.pageSize = result.pageSize
      bugPagination.value.totalPages = result.totalPages

      return result
    } catch (error) {
      console.error('Fetch bugs failed:', error)
      message.error('获取缺陷列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取缺陷详情
   */
  const fetchBugDetail = async (id: number) => {
    try {
      loading.value = true
      const bug = await testApi.getBugDetail(id)
      currentBug.value = bug
      return bug
    } catch (error) {
      console.error('Fetch bug detail failed:', error)
      message.error('获取缺陷详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建缺陷
   */
  const createBug = async (data: any) => {
    try {
      const bug = await testApi.createBug(data)
      message.success('缺陷创建成功')
      await fetchBugs()
      return bug
    } catch (error) {
      console.error('Create bug failed:', error)
      message.error('缺陷创建失败')
      throw error
    }
  }

  /**
   * 更新缺陷
   */
  const updateBug = async (id: number, data: any) => {
    try {
      const bug = await testApi.updateBug(id, data)

      const index = bugList.value.findIndex(b => b.id === id)
      if (index !== -1) {
        bugList.value[index] = bug
      }

      if (currentBug.value?.id === id) {
        currentBug.value = bug
      }

      message.success('缺陷更新成功')
      return bug
    } catch (error) {
      console.error('Update bug failed:', error)
      message.error('缺陷更新失败')
      throw error
    }
  }

  /**
   * 更新缺陷状态
   */
  const updateBugStatus = async (id: number, status: string, comment?: string) => {
    try {
      const bug = await testApi.updateBugStatus(id, status, comment)

      const index = bugList.value.findIndex(b => b.id === id)
      if (index !== -1) {
        bugList.value[index] = bug
      }

      if (currentBug.value?.id === id) {
        currentBug.value = bug
      }

      message.success('缺陷状态更新成功')
      return bug
    } catch (error) {
      console.error('Update bug status failed:', error)
      message.error('缺陷状态更新失败')
      throw error
    }
  }

  /**
   * 删除缺陷
   */
  const deleteBug = async (id: number) => {
    try {
      await testApi.deleteBug(id)
      bugList.value = bugList.value.filter(b => b.id !== id)
      bugPagination.value.total -= 1
      message.success('缺陷删除成功')
      return true
    } catch (error) {
      console.error('Delete bug failed:', error)
      message.error('缺陷删除失败')
      throw error
    }
  }

  /**
   * 批量删除缺陷
   */
  const batchDeleteBugs = async (ids: number[]) => {
    try {
      await testApi.batchDeleteBugs(ids)
      message.success(`成功删除 ${ids.length} 个缺陷`)
      await fetchBugs()
      selectedBugIds.value = []
      return true
    } catch (error) {
      console.error('Batch delete bugs failed:', error)
      message.error('批量删除失败')
      throw error
    }
  }

  /**
   * 获取测试统计信息
   */
  const fetchStatistics = async (projectId?: number) => {
    try {
      const stats = await testApi.getTestStatistics(projectId)
      statistics.value = stats
      return stats
    } catch (error) {
      console.error('Fetch statistics failed:', error)
      throw error
    }
  }

  /**
   * 获取标签列表
   */
  const fetchTags = async (projectId?: number) => {
    try {
      const result = await testApi.getTestTags(projectId)
      tags.value = result
      return result
    } catch (error) {
      console.error('Fetch tags failed:', error)
      throw error
    }
  }

  /**
   * 获取模块列表
   */
  const fetchModules = async (projectId: number) => {
    try {
      const result = await testApi.getTestModules(projectId)
      modules.value = result
      return result
    } catch (error) {
      console.error('Fetch modules failed:', error)
      throw error
    }
  }

  /**
   * 设置测试用例查询参数
   */
  const setTestCaseQueryParams = (params: Partial<TestCaseQueryParams>) => {
    testCaseQueryParams.value = { ...testCaseQueryParams.value, ...params }
  }

  /**
   * 重置测试用例查询参数
   */
  const resetTestCaseQueryParams = () => {
    testCaseQueryParams.value = {
      keyword: '',
      projectId: undefined,
      moduleId: undefined,
      type: undefined,
      priority: undefined,
      status: undefined,
      assigneeId: undefined,
      tags: [],
      sort: 'createdAt',
      order: 'desc'
    }
  }

  /**
   * 设置缺陷查询参数
   */
  const setBugQueryParams = (params: Partial<BugQueryParams>) => {
    bugQueryParams.value = { ...bugQueryParams.value, ...params }
  }

  /**
   * 重置缺陷查询参数
   */
  const resetBugQueryParams = () => {
    bugQueryParams.value = {
      keyword: '',
      projectId: undefined,
      moduleId: undefined,
      severity: undefined,
      priority: undefined,
      status: undefined,
      assigneeId: undefined,
      tags: [],
      sort: 'createdAt',
      order: 'desc'
    }
  }

  /**
   * 设置测试用例分页
   */
  const setTestCasePagination = (page: number, pageSize?: number) => {
    testCasePagination.value.page = page
    if (pageSize) {
      testCasePagination.value.pageSize = pageSize
    }
  }

  /**
   * 设置缺陷分页
   */
  const setBugPagination = (page: number, pageSize?: number) => {
    bugPagination.value.page = page
    if (pageSize) {
      bugPagination.value.pageSize = pageSize
    }
  }

  /**
   * 选择测试用例
   */
  const toggleTestCaseSelect = (id: number) => {
    const index = selectedTestCaseIds.value.indexOf(id)
    if (index > -1) {
      selectedTestCaseIds.value.splice(index, 1)
    } else {
      selectedTestCaseIds.value.push(id)
    }
  }

  /**
   * 全选测试用例
   */
  const selectAllTestCases = () => {
    selectedTestCaseIds.value = testCaseList.value.map(tc => tc.id)
  }

  /**
   * 清空测试用例选择
   */
  const clearTestCaseSelection = () => {
    selectedTestCaseIds.value = []
  }

  /**
   * 选择缺陷
   */
  const toggleBugSelect = (id: number) => {
    const index = selectedBugIds.value.indexOf(id)
    if (index > -1) {
      selectedBugIds.value.splice(index, 1)
    } else {
      selectedBugIds.value.push(id)
    }
  }

  /**
   * 全选缺陷
   */
  const selectAllBugs = () => {
    selectedBugIds.value = bugList.value.map(b => b.id)
  }

  /**
   * 清空缺陷选择
   */
  const clearBugSelection = () => {
    selectedBugIds.value = []
  }

  return {
    // State
    testCaseList,
    currentTestCase,
    testCaseExecutions,
    testCasePagination,
    testCaseQueryParams,
    testPlans,
    currentTestPlan,
    bugList,
    currentBug,
    bugPagination,
    bugQueryParams,
    statistics,
    tags,
    modules,
    loading,
    selectedTestCaseIds,
    selectedBugIds,
    // Getters
    hasTestCases,
    selectedTestCases,
    selectedTestCaseCount,
    hasBugs,
    selectedBugs,
    selectedBugCount,
    // Actions
    fetchTestCases,
    fetchTestCaseDetail,
    createTestCase,
    updateTestCase,
    deleteTestCase,
    batchDeleteTestCases,
    copyTestCase,
    batchCopyTestCases,
    executeTestCase,
    batchExecuteTestCases,
    fetchTestCaseExecutions,
    fetchTestPlans,
    createTestPlan,
    fetchBugs,
    fetchBugDetail,
    createBug,
    updateBug,
    updateBugStatus,
    deleteBug,
    batchDeleteBugs,
    fetchStatistics,
    fetchTags,
    fetchModules,
    setTestCaseQueryParams,
    resetTestCaseQueryParams,
    setBugQueryParams,
    resetBugQueryParams,
    setTestCasePagination,
    setBugPagination,
    toggleTestCaseSelect,
    selectAllTestCases,
    clearTestCaseSelection,
    toggleBugSelect,
    selectAllBugs,
    clearBugSelection
  }
})
