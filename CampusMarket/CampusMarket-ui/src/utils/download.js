/**
 * 通用下载函数
 * @param {string} url - 文件URL
 * @param {string} filename - 文件名
 */
export const downloadFile = (url, filename) => {
  // 创建一个隐藏的a标签
  const link = document.createElement('a')
  link.style.display = 'none'
  link.href = url
  link.download = filename
  
  // 添加到文档中
  document.body.appendChild(link)
  
  // 触发点击
  link.click()
  
  // 清理DOM
  document.body.removeChild(link)
}

/**
 * 下载Blob数据
 * @param {Blob} blob - Blob数据
 * @param {string} filename - 文件名
 */
export const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  downloadFile(url, filename)
  // 清理URL对象
  window.URL.revokeObjectURL(url)
}

/**
 * 从URL下载文件
 * @param {string} url - 文件URL
 * @param {string} filename - 文件名
 */
export const downloadFromUrl = async (url, filename) => {
  try {
    const response = await fetch(url)
    if (!response.ok) throw new Error('下载失败')
    
    const blob = await response.blob()
    downloadBlob(blob, filename)
  } catch (error) {
    console.error('下载出错:', error)
    throw error
  }
} 