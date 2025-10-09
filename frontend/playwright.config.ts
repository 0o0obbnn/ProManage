import { defineConfig, devices } from '@playwright/test';

/**
 * ProManage Playwright 配置
 * 用于前后端集成UI测试
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e',

  // 测试超时时间
  timeout: 30000,

  // 每个测试的超时时间
  expect: {
    timeout: 5000
  },

  // 完全并行运行测试
  fullyParallel: true,

  // CI环境下失败时不重试
  forbidOnly: !!process.env.CI,

  // 失败时重试次数
  retries: process.env.CI ? 2 : 0,

  // 并行worker数量
  workers: process.env.CI ? 1 : undefined,

  // 报告器配置
  reporter: [
    ['html'],
    ['list'],
    ['junit', { outputFile: 'test-results/junit.xml' }]
  ],

  // 共享配置
  use: {
    // 基础URL
    baseURL: 'http://localhost:5173',

    // 追踪设置（失败时保留）
    trace: 'retain-on-failure',

    // 截图设置
    screenshot: 'only-on-failure',

    // 视频设置
    video: 'retain-on-failure',

    // 浏览器上下文选项
    viewport: { width: 1280, height: 720 },
    ignoreHTTPSErrors: true,

    // 导航超时
    navigationTimeout: 15000,

    // 操作超时
    actionTimeout: 10000,
  },

  // 测试项目配置（不同浏览器）
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // 如需测试其他浏览器，可以取消注释
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    // 移动端测试
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
  ],

  // 开发服务器配置（如果需要自动启动）
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: !process.env.CI,
  //   timeout: 120000,
  // },
});
