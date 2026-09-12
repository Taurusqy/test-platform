import ExcelJS from 'exceljs'

/**
 * 导出带样式的Excel（表头加粗+边框，每个附件单独占一列）
 * @param {string} filename 文件名（不含扩展名）
 * @param {string} sheetName 工作表名
 * @param {Array} headers 表头数组 [{key, label, width}]
 * @param {Array} data 数据数组
 * @param {string} imageKey 附件列的key（可选），指定后每个附件单独占一列
 */
export async function exportStyledExcel(filename, sheetName, headers, data, imageKey = null) {
  const workbook = new ExcelJS.Workbook()
  const worksheet = workbook.addWorksheet(sheetName)

  let finalHeaders = headers
  let finalData = data
  let attachColIndexes = [] // 附件列在 finalHeaders 中的索引列表

  // 如果有附件列，拆分成多列（每个附件占一列）
  if (imageKey) {
    const attachColIndex = headers.findIndex(h => h.key === imageKey)
    if (attachColIndex >= 0) {
      // 计算最大附件数量
      let maxAttach = 0
      const splitData = data.map(row => {
        const val = row[imageKey]
        const files = val ? val.split(',').map(s => s.trim()).filter(s => s) : []
        if (files.length > maxAttach) maxAttach = files.length
        return { ...row, _attachFiles: files }
      })

      // 构建新表头：替换附件列为多个附件列
      const attachHeaders = []
      for (let i = 0; i < maxAttach; i++) {
        attachHeaders.push({ key: `_attach_${i}`, label: `附件${i + 1}`, width: 18 })
      }
      finalHeaders = [
        ...headers.slice(0, attachColIndex),
        ...attachHeaders,
        ...headers.slice(attachColIndex + 1)
      ]

      // 构建新数据：把附件拆分到对应列
      finalData = splitData.map(row => {
        const newRow = { ...row }
        delete newRow[imageKey]
        delete newRow._attachFiles
        row._attachFiles.forEach((file, idx) => {
          newRow[`_attach_${idx}`] = file
        })
        return newRow
      })

      // 记录附件列索引
      attachColIndexes = attachHeaders.map((_, i) => attachColIndex + i)
    }
  }

  // 设置列
  worksheet.columns = finalHeaders.map(h => ({
    header: h.label,
    key: h.key,
    width: h.width || 15
  }))

  // 写入数据行
  worksheet.addRows(finalData)

  // 表头样式：加粗、居中、边框
  const headerRow = worksheet.getRow(1)
  headerRow.height = 25
  headerRow.eachCell((cell) => {
    cell.font = { bold: true, size: 11 }
    cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true }
    cell.border = {
      top: { style: 'thin' }, left: { style: 'thin' },
      bottom: { style: 'thin' }, right: { style: 'thin' }
    }
  })

  // 数据行样式：边框、自动换行（遍历所有列，确保空单元格也有边框）
  const totalCols = finalHeaders.length
  for (let i = 2; i <= finalData.length + 1; i++) {
    const row = worksheet.getRow(i)
    row.height = 22
    for (let c = 1; c <= totalCols; c++) {
      const cell = row.getCell(c)
      cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true }
      cell.border = {
        top: { style: 'thin' }, left: { style: 'thin' },
        bottom: { style: 'thin' }, right: { style: 'thin' }
      }
    }
  }

  // 处理附件列：图片嵌入，文件显示文件名
  for (const colIdx of attachColIndexes) {
    for (let i = 0; i < finalData.length; i++) {
      const rowIdx = i + 2 // Excel行号（数据从第2行开始）
      const cellValue = finalData[i][finalHeaders[colIdx].key]
      const cell = worksheet.getRow(rowIdx).getCell(colIdx + 1)

      if (!cellValue) {
        cell.value = ''
        continue
      }

      const isImg = /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(cellValue)

      if (isImg) {
        try {
          const resp = await fetch(cellValue)
          const blob = await resp.blob()
          const arrayBuffer = await blob.arrayBuffer()
          const ext = cellValue.substring(cellValue.lastIndexOf('.') + 1).toLowerCase()
          const imageId = workbook.addImage({
            buffer: arrayBuffer,
            extension: ext === 'jpg' ? 'jpeg' : ext
          })
          // 图片嵌入到当前单元格
          worksheet.addImage(imageId, {
            tl: { col: colIdx, row: i + 1 },
            ext: { width: 90, height: 65 }
          })
          cell.value = '' // 清空文本，只显示图片
          worksheet.getRow(rowIdx).height = 75
        } catch (e) {
          console.warn('图片嵌入失败:', cellValue, e)
          const fullName = cellValue.substring(cellValue.lastIndexOf('/') + 1)
          const underscoreIdx = fullName.indexOf('_')
          const showName = (underscoreIdx > 0 && underscoreIdx < 40) ? fullName.substring(underscoreIdx + 1) : fullName
          const linkUrl = encodeURI(window.location.origin + cellValue)
          cell.value = { formula: `=HYPERLINK("${linkUrl}","${showName}")`, result: showName }
          cell.font = { color: { argb: 'FF0563C1' }, underline: true, size: 11 }
        }
      } else {
        // 非图片文件：用Excel HYPERLINK公式，点击可下载/查看
        const fullName = cellValue.substring(cellValue.lastIndexOf('/') + 1)
        const underscoreIdx = fullName.indexOf('_')
        const showName = (underscoreIdx > 0 && underscoreIdx < 40) ? fullName.substring(underscoreIdx + 1) : fullName
        const linkUrl = encodeURI(window.location.origin + cellValue)
        cell.value = { formula: `=HYPERLINK("${linkUrl}","${showName}")`, result: showName }
        cell.font = { color: { argb: 'FF0563C1' }, underline: true, size: 11 }
        cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true }
      }
    }
  }

  // 生成并下载（用原生a标签下载）
  const buffer = await workbook.xlsx.writeBuffer()
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const d = new Date()
  const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  const downloadUrl = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = downloadUrl
  a.download = `${filename}_${dateStr}.xlsx`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  // 延迟释放blob URL，避免浏览器还没下载完就被回收导致"网络错误"
  setTimeout(() => URL.revokeObjectURL(downloadUrl), 5000)
}
