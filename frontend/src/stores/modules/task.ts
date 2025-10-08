/**
 * 任务状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  TaskStatus,
  TaskPriority,
  type Task,
  type TaskColumn,
  type TaskQueryParams,
  type TaskFormData,
  type TaskViewMode,
  type TaskGroupBy,
  type TaskStats,
  type Sprint,
  type TaskComment,
  type SubTask
} from '@/types/task'
import type { PageResult } from '@/types/global'
import * as taskApi from '@/api/modules/task'
import { message } from 'ant-design-vue'

export const useTaskStore = defineStore('task', () => {
  // State
  const taskList = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const kanbanColumns = ref<TaskColumn[]>([
    {
      id: 'todo',
      title: '待处理',
      status: TaskStatus.TODO,
      color: '#d9d9d9',
      tasks: [],
      order: 0
    },
    {
      id: 'in_progress',
      title: '进行中',
      status: TaskStatus.IN_PROGRESS,
      color: '#1890ff',
      tasks: [],
      order: 1
    },
    {
      id: 'testing',
      title: '测试中',
      status: TaskStatus.TESTING,
      color: '#faad14',
      tasks: [],
      order: 2
    },
    {
      id: 'done',
      title: '已完成',
      status: TaskStatus.DONE,
      color: '#52c41a',
      tasks: [],
      order: 3
    }
  ])

  const sprints = ref<Sprint[]>([])
  const currentSprintId = ref<number>()
  const comments = ref<TaskComment[]>([])
  const subtasks = ref<SubTask[]>([])
  const stats = ref<TaskStats | null>(null)
  const tags = ref<string[]>([])

  const loading = ref(false)
  const dragging = ref(false)
  const draggedTask = ref<Task | null>(null)

  const viewMode = ref<TaskViewMode>('kanban')
  const groupBy = ref<TaskGroupBy>('status')

  const pagination = ref({
    page: 1,
    pageSize: 50,
    total: 0,
    totalPages: 0
  })

  const queryParams = ref<Partial<TaskQueryParams>>({
    keyword: '',
    projectId: undefined,
    sprintId: undefined,
    status: undefined,
    priority: undefined,
    assigneeId: undefined,
    tags: [],
    sort: 'createdAt',
    order: 'desc'
  })

  const filterParams = ref({
    assignees: [] as number[],
    priorities: [] as TaskPriority[],
    tags: [] as string[]
  })

  // Getters
  const hasTasks = computed(() => taskList.value.length > 0)
  const todoTasks = computed(() => taskList.value.filter(t => t.status === TaskStatus.TODO))
  const inProgressTasks = computed(() => taskList.value.filter(t => t.status === TaskStatus.IN_PROGRESS))
  const testingTasks = computed(() => taskList.value.filter(t => t.status === TaskStatus.TESTING))
  const doneTasks = computed(() => taskList.value.filter(t => t.status === TaskStatus.DONE))

  const activeSprint = computed(() => sprints.value.find(s => s.status === 'active'))

  /**
   * 获取任务列表
   */
  const fetchTasks = async (params?: Partial<TaskQueryParams>) => {
    try {
      loading.value = true

      const mergedParams: TaskQueryParams = {
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...queryParams.value,
        ...params
      }

      const result = await taskApi.getTaskList(mergedParams)

      taskList.value = result.list
      pagination.value.total = result.total
      pagination.value.page = result.page
      pagination.value.pageSize = result.pageSize
      pagination.value.totalPages = result.totalPages

      // 更新看板列
      updateKanbanColumns()

      return result
    } catch (error) {
      console.error('Fetch tasks failed:', error)
      message.error('获取任务列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取任务详情
   */
  const fetchTaskDetail = async (id: number) => {
    try {
      loading.value = true
      const task = await taskApi.getTaskDetail(id)
      currentTask.value = task
      return task
    } catch (error) {
      console.error('Fetch task detail failed:', error)
      message.error('获取任务详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建任务
   */
  const createTask = async (data: TaskFormData) => {
    try {
      const task = await taskApi.createTask(data)
      message.success('任务创建成功')

      // 刷新列表
      await fetchTasks()

      return task
    } catch (error) {
      console.error('Create task failed:', error)
      message.error('任务创建失败')
      throw error
    }
  }

  /**
   * 更新任务
   */
  const updateTask = async (id: number, data: Partial<TaskFormData>) => {
    try {
      const task = await taskApi.updateTask(id, data)

      // 更新列表中的任务
      const index = taskList.value.findIndex(t => t.id === id)
      if (index !== -1) {
        taskList.value[index] = task
      }

      // 更新当前任务
      if (currentTask.value?.id === id) {
        currentTask.value = task
      }

      // 更新看板列
      updateKanbanColumns()

      message.success('任务更新成功')
      return task
    } catch (error) {
      console.error('Update task failed:', error)
      message.error('任务更新失败')
      throw error
    }
  }

  /**
   * 删除任务
   */
  const deleteTask = async (id: number) => {
    try {
      await taskApi.deleteTask(id)
      message.success('任务删除成功')

      // 从列表中移除
      taskList.value = taskList.value.filter(t => t.id !== id)

      // 更新看板列
      updateKanbanColumns()

      return true
    } catch (error) {
      console.error('Delete task failed:', error)
      message.error('任务删除失败')
      throw error
    }
  }

  /**
   * 批量删除任务
   */
  const batchDeleteTasks = async (ids: number[]) => {
    try {
      await taskApi.batchDeleteTasks(ids)
      message.success(`成功删除 ${ids.length} 个任务`)

      // 刷新列表
      await fetchTasks()

      return true
    } catch (error) {
      console.error('Batch delete tasks failed:', error)
      message.error('批量删除失败')
      throw error
    }
  }

  /**
   * 更新任务状态
   */
  const updateTaskStatus = async (id: number, status: TaskStatus) => {
    try {
      const task = await taskApi.updateTaskStatus(id, status)

      // 更新列表中的任务
      const index = taskList.value.findIndex(t => t.id === id)
      if (index !== -1) {
        taskList.value[index] = task
      }

      // 更新看板列
      updateKanbanColumns()

      return task
    } catch (error) {
      console.error('Update task status failed:', error)
      message.error('更新任务状态失败')
      throw error
    }
  }

  /**
   * 分配任务
   */
  const assignTask = async (id: number, userId: number) => {
    try {
      const task = await taskApi.assignTask(id, userId)

      // 更新列表中的任务
      const index = taskList.value.findIndex(t => t.id === id)
      if (index !== -1) {
        taskList.value[index] = task
      }

      message.success('任务分配成功')
      return task
    } catch (error) {
      console.error('Assign task failed:', error)
      message.error('任务分配失败')
      throw error
    }
  }

  /**
   * 拖拽开始
   */
  const handleDragStart = (task: Task) => {
    dragging.value = true
    draggedTask.value = task
  }

  /**
   * 拖拽结束
   */
  const handleDragEnd = () => {
    dragging.value = false
    draggedTask.value = null
  }

  /**
   * 拖拽到新列
   */
  const handleDrop = async (targetStatus: TaskStatus) => {
    if (!draggedTask.value) return

    const task = draggedTask.value
    const oldStatus = task.status

    if (oldStatus === targetStatus) {
      handleDragEnd()
      return
    }

    try {
      await updateTaskStatus(task.id, targetStatus)
      message.success('任务状态已更新')
    } catch (error) {
      console.error('Drop task failed:', error)
    } finally {
      handleDragEnd()
    }
  }

  /**
   * 更新看板列
   */
  const updateKanbanColumns = () => {
    kanbanColumns.value.forEach(column => {
      column.tasks = taskList.value.filter(task => task.status === column.status)
    })
  }

  /**
   * 获取迭代列表
   */
  const fetchSprints = async (projectId?: number) => {
    try {
      const result = await taskApi.getSprintList(projectId)
      sprints.value = result
      return result
    } catch (error) {
      console.error('Fetch sprints failed:', error)
      message.error('获取迭代列表失败')
      throw error
    }
  }

  /**
   * 创建迭代
   */
  const createSprint = async (data: any) => {
    try {
      const sprint = await taskApi.createSprint(data)
      sprints.value.push(sprint)
      message.success('迭代创建成功')
      return sprint
    } catch (error) {
      console.error('Create sprint failed:', error)
      message.error('迭代创建失败')
      throw error
    }
  }

  /**
   * 获取任务评论
   */
  const fetchComments = async (taskId: number) => {
    try {
      const result = await taskApi.getTaskComments(taskId)
      comments.value = result
      return result
    } catch (error) {
      console.error('Fetch comments failed:', error)
      message.error('获取评论失败')
      throw error
    }
  }

  /**
   * 添加评论
   */
  const addComment = async (taskId: number, content: string, parentId?: number) => {
    try {
      const comment = await taskApi.addTaskComment(taskId, { content, parentId })
      comments.value.unshift(comment)
      message.success('评论添加成功')
      return comment
    } catch (error) {
      console.error('Add comment failed:', error)
      message.error('评论添加失败')
      throw error
    }
  }

  /**
   * 删除评论
   */
  const deleteComment = async (taskId: number, commentId: number) => {
    try {
      await taskApi.deleteTaskComment(taskId, commentId)
      comments.value = comments.value.filter(c => c.id !== commentId)
      message.success('评论删除成功')
    } catch (error) {
      console.error('Delete comment failed:', error)
      message.error('评论删除失败')
      throw error
    }
  }

  /**
   * 获取子任务
   */
  const fetchSubTasks = async (taskId: number) => {
    try {
      const result = await taskApi.getSubTasks(taskId)
      subtasks.value = result
      return result
    } catch (error) {
      console.error('Fetch subtasks failed:', error)
      message.error('获取子任务失败')
      throw error
    }
  }

  /**
   * 创建子任务
   */
  const createSubTask = async (taskId: number, data: any) => {
    try {
      const subtask = await taskApi.createSubTask(taskId, data)
      subtasks.value.push(subtask)
      message.success('子任务创建成功')
      return subtask
    } catch (error) {
      console.error('Create subtask failed:', error)
      message.error('子任务创建失败')
      throw error
    }
  }

  /**
   * 更新子任务
   */
  const updateSubTask = async (id: number, data: Partial<SubTask>) => {
    try {
      const subtask = await taskApi.updateSubTask(id, data)
      const index = subtasks.value.findIndex(s => s.id === id)
      if (index !== -1) {
        subtasks.value[index] = subtask
      }
      message.success('子任务更新成功')
      return subtask
    } catch (error) {
      console.error('Update subtask failed:', error)
      message.error('子任务更新失败')
      throw error
    }
  }

  /**
   * 删除子任务
   */
  const deleteSubTask = async (id: number) => {
    try {
      await taskApi.deleteSubTask(id)
      subtasks.value = subtasks.value.filter(s => s.id !== id)
      message.success('子任务删除成功')
    } catch (error) {
      console.error('Delete subtask failed:', error)
      message.error('子任务删除失败')
      throw error
    }
  }

  /**
   * 切换子任务完成状态
   */
  const toggleSubTaskComplete = async (id: number, completed: boolean) => {
    try {
      const subtask = await taskApi.toggleSubTaskComplete(id, completed)
      const index = subtasks.value.findIndex(s => s.id === id)
      if (index !== -1) {
        subtasks.value[index] = subtask
      }
      return subtask
    } catch (error) {
      console.error('Toggle subtask failed:', error)
      message.error('更新子任务状态失败')
      throw error
    }
  }

  /**
   * 获取统计信息
   */
  const fetchStats = async (projectId?: number, sprintId?: number) => {
    try {
      const result = await taskApi.getTaskStats(projectId, sprintId)
      stats.value = result
      return result
    } catch (error) {
      console.error('Fetch stats failed:', error)
      throw error
    }
  }

  /**
   * 获取标签列表
   */
  const fetchTags = async (projectId?: number) => {
    try {
      const result = await taskApi.getTaskTags(projectId)
      tags.value = result
      return result
    } catch (error) {
      console.error('Fetch tags failed:', error)
      throw error
    }
  }

  /**
   * 设置查询参数
   */
  const setQueryParams = (params: Partial<TaskQueryParams>) => {
    queryParams.value = { ...queryParams.value, ...params }
  }

  /**
   * 重置查询参数
   */
  const resetQueryParams = () => {
    queryParams.value = {
      keyword: '',
      projectId: undefined,
      sprintId: undefined,
      status: undefined,
      priority: undefined,
      assigneeId: undefined,
      tags: [],
      sort: 'createdAt',
      order: 'desc'
    }
  }

  /**
   * 设置分页
   */
  const setPagination = (page: number, pageSize?: number) => {
    pagination.value.page = page
    if (pageSize) {
      pagination.value.pageSize = pageSize
    }
  }

  /**
   * 切换视图模式
   */
  const setViewMode = (mode: TaskViewMode) => {
    viewMode.value = mode
    localStorage.setItem('task_view_mode', mode)
  }

  /**
   * 设置分组方式
   */
  const setGroupBy = (group: TaskGroupBy) => {
    groupBy.value = group
  }

  /**
   * 设置筛选参数
   */
  const setFilterParams = (params: Partial<typeof filterParams.value>) => {
    filterParams.value = { ...filterParams.value, ...params }
  }

  /**
   * 重置筛选参数
   */
  const resetFilterParams = () => {
    filterParams.value = {
      assignees: [],
      priorities: [],
      tags: []
    }
  }

  /**
   * 应用筛选
   */
  const applyFilters = async () => {
    const params: Partial<TaskQueryParams> = {}

    if (filterParams.value.assignees.length > 0) {
      params.assigneeId = filterParams.value.assignees[0]
    }

    if (filterParams.value.priorities.length > 0) {
      params.priority = filterParams.value.priorities[0]
    }

    if (filterParams.value.tags.length > 0) {
      params.tags = filterParams.value.tags
    }

    setQueryParams(params)
    await fetchTasks()
  }

  /**
   * 恢复视图模式
   */
  const restoreViewMode = () => {
    const savedMode = localStorage.getItem('task_view_mode') as TaskViewMode
    if (savedMode) {
      viewMode.value = savedMode
    }
  }

  return {
    // State
    taskList,
    currentTask,
    kanbanColumns,
    sprints,
    currentSprintId,
    comments,
    subtasks,
    stats,
    tags,
    loading,
    dragging,
    draggedTask,
    viewMode,
    groupBy,
    pagination,
    queryParams,
    filterParams,
    // Getters
    hasTasks,
    todoTasks,
    inProgressTasks,
    testingTasks,
    doneTasks,
    activeSprint,
    // Actions
    fetchTasks,
    fetchTaskDetail,
    createTask,
    updateTask,
    deleteTask,
    batchDeleteTasks,
    updateTaskStatus,
    assignTask,
    handleDragStart,
    handleDragEnd,
    handleDrop,
    updateKanbanColumns,
    fetchSprints,
    createSprint,
    fetchComments,
    addComment,
    deleteComment,
    fetchSubTasks,
    createSubTask,
    updateSubTask,
    deleteSubTask,
    toggleSubTaskComplete,
    fetchStats,
    fetchTags,
    setQueryParams,
    resetQueryParams,
    setPagination,
    setViewMode,
    setGroupBy,
    setFilterParams,
    resetFilterParams,
    applyFilters,
    restoreViewMode
  }
})