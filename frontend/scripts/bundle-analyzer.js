/**
 * 简单的打包分析工具
 */
const fs = require('fs')
const path = require('path')

// 分析 dist 目录中的文件大小
const distPath = path.resolve(__dirname, '../dist')

function analyzeDist() {
  if (!fs.existsSync(distPath)) {
    console.log('请先运行 npm run build')
    return
  }

  const files = getAllFiles(distPath)
  const fileStats = []

  files.forEach(file => {
    const stats = fs.statSync(file)
    const relativePath = path.relative(distPath, file)
    fileStats.push({
      path: relativePath,
      size: stats.size,
      sizeKB: (stats.size / 1024).toFixed(2)
    })
  })

  // 按大小排序
  fileStats.sort((a, b) => b.size - a.size)

  // 输出分析结果
  console.log('\n=== 打包体积分析 ===\n')
  console.log('文件大小 (KB) | 文件路径')
  console.log('----------------|------------------------')
  
  fileStats.forEach(file => {
    console.log(`${file.sizeKB.padStart(12)} | ${file.path}`)
  })

  // 计算总体积
  const totalSize = fileStats.reduce((sum, file) => sum + file.size, 0)
  console.log('\n总体积:', (totalSize / 1024).toFixed(2), 'KB /', (totalSize / 1024 / 1024).toFixed(2), 'MB')

  // 分析最大的文件
  const largestFiles = fileStats.slice(0, 5)
  console.log('\n=== 最大的5个文件 ===')
  largestFiles.forEach((file, index) => {
    console.log(`${index + 1}. ${file.path} - ${file.sizeKB} KB`)
  })
}

function getAllFiles(dirPath, arrayOfFiles = []) {
  const files = fs.readdirSync(dirPath)

  files.forEach(file => {
    const fullPath = path.join(dirPath, file)
    if (fs.statSync(fullPath).isDirectory()) {
      arrayOfFiles = getAllFiles(fullPath, arrayOfFiles)
    } else {
      arrayOfFiles.push(fullPath)
    }
  })

  return arrayOfFiles
}

analyzeDist()