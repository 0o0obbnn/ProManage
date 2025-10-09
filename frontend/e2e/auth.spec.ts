import { test, expect } from '@playwright/test';

/**
 * 认证功能E2E测试
 * 测试用户注册、登录、登出等功能
 */

test.describe('用户认证功能', () => {
  // 生成唯一的测试用户名
  const timestamp = Date.now();
  const testUsername = `e2e_user_${timestamp}`;
  const testEmail = `e2e_test_${timestamp}@example.com`;
  const testPassword = 'Test@123456';
  const testRealName = 'E2E Test User';

  test.beforeEach(async ({ page }) => {
    // 每个测试前访问首页
    await page.goto('/');
  });

  test('应该显示登录页面', async ({ page }) => {
    await page.goto('/login');

    // 验证页面标题
    await expect(page).toHaveTitle(/ProManage/);

    // 验证登录表单存在
    await expect(page.locator('input[type="text"]').first()).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();

    // 验证登录按钮存在
    await expect(page.getByRole('button', { name: /登录|登錄|Login/i })).toBeVisible();
  });

  test('应该成功注册新用户', async ({ page }) => {
    // 导航到注册页面
    await page.goto('/register');

    // 等待表单加载
    await page.waitForLoadState('networkidle');

    // 填写注册表单
    await page.fill('input[placeholder*="用户名"]', testUsername);
    await page.fill('input[placeholder*="邮箱"]', testEmail);
    await page.fill('input[placeholder*="真实姓名"]', testRealName);

    // 填写密码
    const passwordInputs = page.locator('input[type="password"]');
    await passwordInputs.nth(0).fill(testPassword);
    await passwordInputs.nth(1).fill(testPassword);

    // 点击注册按钮
    await page.click('button:has-text("注册")');

    // 等待注册成功提示或跳转
    await page.waitForTimeout(2000);

    // 验证注册成功（可能跳转到登录页或首页）
    const currentUrl = page.url();
    expect(currentUrl).toMatch(/\/(login|dashboard|home)/);
  });

  test('应该成功登录', async ({ page }) => {
    // 导航到登录页面
    await page.goto('/login');

    // 填写登录表单
    await page.fill('input[placeholder*="用户名"]', testUsername);

    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.fill(testPassword);

    // 点击登录按钮
    await page.click('button:has-text("登录")');

    // 等待登录成功
    await page.waitForTimeout(3000);

    // 验证登录成功（跳转到仪表板）
    await expect(page).toHaveURL(/\/(dashboard|home)/);

    // 验证用户信息显示
    await expect(page.locator(`text=${testRealName}`).or(page.locator(`text=${testUsername}`))).toBeVisible({ timeout: 5000 });
  });

  test('登录失败应该显示错误提示', async ({ page }) => {
    await page.goto('/login');

    // 使用错误的凭据
    await page.fill('input[placeholder*="用户名"]', 'wronguser');
    await page.fill('input[type="password"]', 'wrongpassword');

    // 点击登录
    await page.click('button:has-text("登录")');

    // 等待错误提示
    await page.waitForTimeout(2000);

    // 验证错误消息显示
    const errorMessage = page.locator('text=/用户名或密码错误|登录失败|Invalid/i');
    await expect(errorMessage).toBeVisible({ timeout: 3000 });
  });

  test('注册时密码不匹配应该显示错误', async ({ page }) => {
    await page.goto('/register');

    // 填写表单，密码不匹配
    await page.fill('input[placeholder*="用户名"]', `temp_${timestamp}`);
    await page.fill('input[placeholder*="邮箱"]', `temp_${timestamp}@example.com`);
    await page.fill('input[placeholder*="真实姓名"]', 'Temp User');

    const passwordInputs = page.locator('input[type="password"]');
    await passwordInputs.nth(0).fill('Password123');
    await passwordInputs.nth(1).fill('DifferentPassword123');

    // 点击注册按钮
    await page.click('button:has-text("注册")');

    // 验证错误提示
    await expect(page.locator('text=/密码不一致|密码不匹配/i')).toBeVisible({ timeout: 3000 });
  });

  test('应该能够登出', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"]', testUsername);
    await page.fill('input[type="password"]', testPassword);
    await page.click('button:has-text("登录")');

    // 等待登录成功
    await page.waitForURL(/\/(dashboard|home)/);
    await page.waitForTimeout(2000);

    // 查找并点击登出按钮（可能在用户菜单中）
    const logoutButton = page.locator('button:has-text("退出"), a:has-text("退出"), button:has-text("登出"), a:has-text("登出")').first();

    if (await logoutButton.isVisible({ timeout: 3000 })) {
      await logoutButton.click();
    } else {
      // 如果没有直接的登出按钮，尝试点击用户头像/菜单
      const userMenu = page.locator('[class*="user"], [class*="avatar"]').first();
      await userMenu.click();
      await page.waitForTimeout(500);
      await page.locator('button:has-text("退出"), a:has-text("退出")').first().click();
    }

    // 等待跳转到登录页
    await page.waitForTimeout(2000);

    // 验证已登出（回到登录页）
    await expect(page).toHaveURL(/\/login/);
  });
});

test.describe('页面访问控制', () => {
  test('未登录时访问受保护页面应该重定向到登录页', async ({ page }) => {
    // 尝试直接访问受保护的页面
    await page.goto('/dashboard');

    // 等待可能的重定向
    await page.waitForTimeout(2000);

    // 验证被重定向到登录页
    expect(page.url()).toMatch(/\/login/);
  });
});
