/**
 * Lighthouse 性能分析脚本
 */
const { execSync } = require('child_process')
const fs = require('fs')
const path = require('path')

// 配置
const config = {
  url: 'http://localhost:3000',
  outputDir: path.resolve(__dirname, '../lighthouse-reports'),
  threshold: {
    performance: 90,
    accessibility: 90,
    bestPractices: 90,
    seo: 90,
    pwa: 80
  }
}

// 创建报告目录
if (!fs.existsSync(config.outputDir)) {
  fs.mkdirSync(config.outputDir, { recursive: true })
}

// 运行 Lighthouse 分析
async function runLighthouse() {
  console.log('开始 Lighthouse 性能分析...')
  
  try {
    // 检查是否安装了 lighthouse
    execSync('lighthouse --version', { stdio: 'pipe' })
  } catch (error) {
    console.error('请先安装 Lighthouse: npm install -g lighthouse')
    process.exit(1)
  }

  // 生成报告文件名
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
  const reportPath = path.join(config.outputDir, `lighthouse-report-${timestamp}.html`)
  const jsonPath = path.join(config.outputDir, `lighthouse-report-${timestamp}.json`)

  // 运行 Lighthouse
  const command = `lighthouse "${config.url}" --output=html,json --output-path="${reportPath}" --chrome-flags="--headless"`
  
  try {
    execSync(command, { stdio: 'inherit' })
    console.log(`\n报告已生成: ${reportPath}`)
    
    // 读取 JSON 报告
    const reportData = JSON.parse(fs.readFileSync(jsonPath, 'utf8'))
    
    // 输出评分
    console.log('\n=== Lighthouse 评分 ===')
    console.log(`Performance: ${reportData.lhr.categories.performance.score * 100}/100`)
    console.log(`Accessibility: ${reportData.lhr.categories.accessibility.score * 100}/100`)
    console.log(`Best Practices: ${reportData.lhr.categories['best-practices'].score * 100}/100`)
    console.log(`SEO: ${reportData.lhr.categories.seo.score * 100}/100`)
    console.log(`PWA: ${reportData.lhr.categories.pwa ? reportData.lhr.categories.pwa.score * 100 : 'N/A'}/100`)
    
    // 检查是否达到阈值
    const scores = {
      performance: reportData.lhr.categories.performance.score * 100,
      accessibility: reportData.lhr.categories.accessibility.score * 100,
      bestPractices: reportData.lhr.categories['best-practices'].score * 100,
      seo: reportData.lhr.categories.seo.score * 100,
      pwa: reportData.lhr.categories.pwa ? reportData.lhr.categories.pwa.score * 100 : 0
    }
    
    let allPassed = true
    for (const [category, score] of Object.entries(scores)) {
      if (category === 'pwa' && !reportData.lhr.categories.pwa) continue
      
      if (score < config.threshold[category]) {
        console.log(`\n⚠️ ${category} 未达到目标分数 ${config.threshold[category]}，当前分数: ${score}`)
        allPassed = false
      }
    }
    
    if (allPassed) {
      console.log('\n✅ 所有指标均已达到目标分数！')
    } else {
      console.log('\n❌ 部分指标未达到目标分数，请查看报告进行优化')
    }
    
    // 输出性能指标详情
    const performanceMetrics = reportData.lhr.audits
    console.log('\n=== 性能指标详情 ===')
    console.log(`First Contentful Paint: ${performanceMetrics['first-contentful-paint'].displayValue}`)
    console.log(`Largest Contentful Paint: ${performanceMetrics['largest-contentful-paint'].displayValue}`)
    console.log(`Time to Interactive: ${performanceMetrics['interactive'].displayValue}`)
    console.log(`Speed Index: ${performanceMetrics['speed-index'].displayValue}`)
    console.log(`Cumulative Layout Shift: ${performanceMetrics['cumulative-layout-shift'].displayValue}`)
    console.log(`Total Blocking Time: ${performanceMetrics['total-blocking-time'].displayValue}`)
    
  } catch (error) {
    console.error('Lighthouse 分析失败:', error.message)
    process.exit(1)
  }
}

// 检查开发服务器是否运行（跨平台兼容）
function checkDevServer() {
  const http = require('http')
  const url = require('url')
  
  return new Promise((resolve) => {
    const parsedUrl = url.parse(config.url)
    const options = {
      hostname: parsedUrl.hostname,
      port: parsedUrl.port || 80,
      path: '/',
      method: 'HEAD',
      timeout: 3000
    }
    
    const req = http.request(options, (res) => {
      resolve(res.statusCode >= 200 && res.statusCode < 400)
    })
    
    req.on('error', () => resolve(false))
    req.on('timeout', () => {
      req.destroy()
      resolve(false)
    })
    
    req.end()
  })
}

// 主函数
async function main() {
  console.log('检查开发服务器是否运行...')
  
  const isRunning = await checkDevServer()
  if (!isRunning) {
    console.log(`开发服务器未运行，请先运行: npm run dev`)
    console.log('或者修改脚本中的 URL 为目标地址')
    process.exit(1)
  }
  
  await runLighthouse()
}

main().catch(console.error)