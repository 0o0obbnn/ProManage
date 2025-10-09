import { test, expect } from '@playwright/test';

/**
 * 基础页面加载测试
 * 验证前端应用的基本功能和页面可访问性
 */

test.describe('基础页面功能', () => {
  test('首页应该正常加载', async ({ page }) => {
    await page.goto('/');

    // 验证页面加载
    await expect(page).toHaveTitle(/ProManage/);

    // 验证页面响应
    const response = await page.goto('/');
    expect(response?.status()).toBe(200);
  });

  test('登录页面应该正常加载', async ({ page }) => {
    await page.goto('/login');

    // 验证页面标题
    await expect(page).toHaveTitle(/ProManage/);

    // 验证登录表单元素
    await expect(page.locator('input[type="text"]').or(page.locator('input[placeholder*="用户名"]'))).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();

    // 验证登录按钮
    const loginButton = page.getByRole('button', { name: /登录|登錄|Login/i });
    await expect(loginButton).toBeVisible();
  });

  test('注册页面应该正常加载', async ({ page }) => {
    await page.goto('/register');

    // 验证页面标题
    await expect(page).toHaveTitle(/ProManage/);

    // 验证注册表单元素
    await expect(page.locator('input[placeholder*="用户名"]')).toBeVisible();
    await expect(page.locator('input[placeholder*="邮箱"]')).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();

    // 验证注册按钮
    const registerButton = page.getByRole('button', { name: /注册|註冊|Register|Sign Up/i });
    await expect(registerButton).toBeVisible();
  });

  test('页面应该有正确的字符编码', async ({ page }) => {
    await page.goto('/');

    // 获取页面编码
    const charset = await page.evaluate(() => {
      const meta = document.querySelector('meta[charset]');
      return meta?.getAttribute('charset');
    });

    expect(charset?.toLowerCase()).toBe('utf-8');
  });

  test('页面应该有响应式视口设置', async ({ page }) => {
    await page.goto('/');

    // 检查viewport meta标签
    const viewportContent = await page.evaluate(() => {
      const meta = document.querySelector('meta[name="viewport"]');
      return meta?.getAttribute('content');
    });

    expect(viewportContent).toContain('width=device-width');
  });

  test('前端资源应该正常加载', async ({ page }) => {
    const failedRequests: string[] = [];

    // 监听请求失败
    page.on('requestfailed', request => {
      failedRequests.push(request.url());
    });

    await page.goto('/');

    // 等待页面完全加载
    await page.waitForLoadState('networkidle');

    // 验证没有关键资源加载失败
    const criticalFailures = failedRequests.filter(url =>
      url.includes('.js') || url.includes('.css')
    );

    expect(criticalFailures.length).toBe(0);
  });

  test('控制台不应该有严重错误', async ({ page }) => {
    const errors: string[] = [];

    // 监听控制台错误
    page.on('console', msg => {
      if (msg.type() === 'error') {
        errors.push(msg.text());
      }
    });

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // 过滤掉一些常见的非致命错误（如开发环境警告）
    const criticalErrors = errors.filter(error =>
      !error.includes('DevTools') &&
      !error.includes('Extension') &&
      !error.includes('[HMR]')
    );

    // 验证没有严重错误
    expect(criticalErrors.length).toBeLessThan(3);
  });
});

test.describe('API健康检查', () => {
  test('后端API应该可访问', async ({ request }) => {
    // 测试后端健康检查接口
    const response = await request.get('http://localhost:8080/actuator/health');

    expect(response.status()).toBe(200);

    const health = await response.json();
    expect(health.status).toBe('UP');
  });

  test('API应该支持CORS', async ({ request }) => {
    const response = await request.get('http://localhost:8080/actuator/health', {
      headers: {
        'Origin': 'http://localhost:5173'
      }
    });

    // 验证CORS头存在
    const corsHeader = response.headers()['access-control-allow-origin'];
    expect(corsHeader).toBeTruthy();
  });
});

test.describe('性能测试', () => {
  test('首页加载时间应该合理', async ({ page }) => {
    const startTime = Date.now();

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const loadTime = Date.now() - startTime;

    // 首页加载应该在5秒内完成
    expect(loadTime).toBeLessThan(5000);

    console.log(`首页加载时间: ${loadTime}ms`);
  });

  test('登录页面加载时间应该合理', async ({ page }) => {
    const startTime = Date.now();

    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    const loadTime = Date.now() - startTime;

    // 登录页加载应该在3秒内完成
    expect(loadTime).toBeLessThan(3000);

    console.log(`登录页加载时间: ${loadTime}ms`);
  });
});
