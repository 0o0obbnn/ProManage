/**
 * 打包分析脚本
 */
const { execSync } = require('child_process')
const fs = require('fs')
const path = require('path')

// 设置环境变量
process.env.ANALYZE = 'true'

// 创建 dist 目录
const distPath = path.resolve(__dirname, '../dist')
if (!fs.existsSync(distPath)) {
  fs.mkdirSync(distPath, { recursive: true })
}

// 运行打包分析
console.log('开始打包分析...')
try {
  execSync('vite build', {
    stdio: 'inherit',
    cwd: path.resolve(__dirname, '..')
  })
  console.log('打包分析完成！')
} catch (error) {
  console.error('打包分析失败：', error)
  process.exit(1)
}