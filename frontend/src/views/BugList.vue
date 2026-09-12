<template>
  <div class="page">
    <div class="page-top">
      <el-button text class="back-btn" @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon> 返回项目列表
      </el-button>
    </div>

    <div class="page-hero">
      <div>
        <h1 class="hero-title">Bug缺陷管理</h1>
        <p class="hero-desc">跟踪缺陷全生命周期，点击状态标签可直接流转</p>
      </div>
      <div class="hero-btns">
        <el-button size="large" @click="exportExcel" class="ghost-btn">
          <el-icon><Download /></el-icon> 导出Excel
        </el-button>
        <el-button type="primary" size="large" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新建Bug
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索Bug标题" clearable style="width: 220px" :prefix-icon="Search" />
        <el-select v-model="filterSeverity" placeholder="严重程度" clearable style="width: 110px">
          <el-option v-for="s in severities" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filterPriority" placeholder="优先级" clearable style="width: 100px">
          <el-option v-for="p in priorities" :key="p" :label="p" :value="p" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 110px">
          <el-option v-for="s in allStatuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Bug列表</span>
          <span class="card-count">共 {{ filteredList.length }} 个Bug</span>
        </div>
      </template>
      <el-table :data="filteredList" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="bugNo" label="Bug编号" width="90" align="center">
          <template #default="{ row }">
            <span class="bug-no">{{ row.bugNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="Bug标题" min-width="180">
          <template #default="{ row }">
            <span class="bug-title link" @click="openDetail(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small" effect="dark">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bugPriority" label="优先级" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.bugPriority)" size="small" effect="plain">{{ row.bugPriority || 'P2' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bugType" label="Bug类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.bugType" size="small" effect="plain" type="info">{{ row.bugType }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <!-- 状态列：彩色标签，点击弹出选择 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-popover placement="bottom" :width="120" trigger="click" v-model="row._showStatus">
              <template #reference>
                <el-tag :type="bugStatusType(row.status)" size="small" effect="dark" class="status-tag">
                  {{ row.status }}
                </el-tag>
              </template>
              <div class="status-picker">
                <div v-for="s in allStatuses" :key="s" class="status-option" :class="{ active: s === row.status }" @click="changeStatus(row, s)">
                  <el-tag :type="bugStatusType(s)" size="small" effect="dark">{{ s }}</el-tag>
                </div>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="修复人" width="80" align="center">
          <template #default="{ row }"><span>{{ row.assignee || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="附件" width="70" align="center">
          <template #default="{ row }">
            <div v-if="row.attachment" class="cell-attachments" @click.stop="previewFirstImage(row.attachment)">
              <img v-if="getFirstImage(row.attachment)" :src="getFirstImage(row.attachment)" class="cell-thumb" />
              <el-icon v-else :size="20" color="#909399"><Document /></el-icon>
              <span v-if="getAttachCount(row.attachment) > 1" class="attach-count">+{{ getAttachCount(row.attachment) - 1 }}</span>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <!-- 关联用例：显示编号+标题，点击跳转 -->
        <el-table-column label="关联用例" width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.caseId" class="case-link" @click="goToCase(row)">
              <el-icon><Link /></el-icon> {{ row.caseNo ? row.caseNo + ' ' : '' }}{{ row.caseTitle || '用例#' + row.caseId }}
            </span>
            <span v-else class="text-muted">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" link size="small" @click="deleteItem(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="filteredList.length === 0" class="empty-tip">
        <el-empty description="暂无Bug" />
      </div>
    </el-card>

    <!-- 新增/编辑Bug弹窗 -->
    <el-dialog v-model="showFormDialog" :title="isEdit ? '编辑Bug' : '新建Bug'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="Bug标题" required>
          <el-input v-model="form.title" size="large" />
        </el-form-item>
        <el-form-item label="所属模块">
          <el-input v-model="form.module" placeholder="如：登录模块、支付模块" />
        </el-form-item>
        <el-form-item label="Bug描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="简要描述Bug现象" />
        </el-form-item>
        <el-form-item label="重现步骤">
          <el-input v-model="form.reproduceSteps" type="textarea" :rows="3" placeholder="1.xxx 2.xxx 3.xxx" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="预期结果" style="flex: 1">
            <el-input v-model="form.expectedResult" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="实际结果" style="flex: 1">
            <el-input v-model="form.actualResult" type="textarea" :rows="2" />
          </el-form-item>
        </div>
        <el-form-item label="测试环境">
          <el-input v-model="form.environment" placeholder="如 Chrome120/Win11/V1.2" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="影响版本" style="flex: 1">
            <el-input v-model="form.affectedVersion" placeholder="如 V1.0、V1.2" />
          </el-form-item>
          <el-form-item label="Bug类型" style="flex: 1">
            <el-select v-model="form.bugType" placeholder="请选择" style="width: 100%">
              <el-option label="代码错误" value="代码错误" />
              <el-option label="界面优化" value="界面优化" />
              <el-option label="配置问题" value="配置问题" />
              <el-option label="安全问题" value="安全问题" />
              <el-option label="性能问题" value="性能问题" />
              <el-option label="安装部署" value="安装部署" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="附件">
          <el-upload
            :action="uploadUrl"
            :headers="{}"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            multiple
            accept="image/*,.txt,.log,.doc,.docx,.pdf,.zip,.rar">
            <el-button size="default">
              <el-icon><Upload /></el-icon> 上传附件（可多选）
            </el-button>
          </el-upload>
          <div v-if="form.attachment.length > 0" class="attachment-list">
            <div v-for="(file, idx) in form.attachment" :key="idx" class="attachment-item">
              <img v-if="isImage(file)" :src="file" class="attachment-img" @click="previewImage(file)" />
              <div v-else class="attachment-file" @click="downloadFile(file)">
                <el-icon :size="28"><Document /></el-icon>
                <span>{{ getFileName(file) }}</span>
              </div>
              <el-button type="danger" link size="small" class="remove-btn" @click="form.attachment.splice(idx, 1)">移除</el-button>
            </div>
          </div>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="严重程度" style="flex: 1">
            <el-radio-group v-model="form.severity">
              <el-radio value="致命">致命</el-radio>
              <el-radio value="严重">严重</el-radio>
              <el-radio value="一般">一般</el-radio>
              <el-radio value="轻微">轻微</el-radio>
              <el-radio value="建议">建议</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="优先级" style="flex: 1">
            <el-radio-group v-model="form.bugPriority">
              <el-radio value="P0">P0 立即</el-radio>
              <el-radio value="P1">P1 本版本</el-radio>
              <el-radio value="P2">P2 下版本</el-radio>
              <el-radio value="P3">P3 有空</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="修复人" style="flex: 1">
            <el-input v-model="form.assignee" placeholder="请输入修复人" />
          </el-form-item>
          <el-form-item label="关联用例" style="flex: 1">
            <el-select v-model="form.caseId" placeholder="选填，选择关联用例" clearable style="width: 100%">
              <el-option v-for="c in caseOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- Bug详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="Bug详情" width="640px">
      <div v-if="detailBug" class="bug-detail">
        <div class="detail-header">
          <el-tag type="danger" size="small">{{ detailBug.bugNo }}</el-tag>
          <span class="detail-title">{{ detailBug.title }}</span>
          <el-tag :type="bugStatusType(detailBug.status)" size="small" effect="dark">{{ detailBug.status }}</el-tag>
        </div>
        <div class="detail-tags">
          <el-tag size="small" type="info">模块：{{ detailBug.module || '-' }}</el-tag>
          <el-tag size="small" type="info">影响版本：{{ detailBug.affectedVersion || '-' }}</el-tag>
          <el-tag v-if="detailBug.bugType" size="small" effect="plain" type="info">{{ detailBug.bugType }}</el-tag>
          <el-tag :type="severityType(detailBug.severity)" size="small">严重程度：{{ detailBug.severity }}</el-tag>
          <el-tag :type="priorityType(detailBug.bugPriority)" size="small" effect="plain">优先级：{{ detailBug.bugPriority || 'P2' }}</el-tag>
          <el-tag size="small" type="info">提交人：{{ detailBug.reporter || '-' }}</el-tag>
          <el-tag size="small" type="info">修复人：{{ detailBug.assignee || '-' }}</el-tag>
          <el-tag size="small" type="info">环境：{{ detailBug.environment || '-' }}</el-tag>
        </div>
        <el-descriptions :column="1" border size="small" class="detail-desc">
          <el-descriptions-item label="Bug描述">{{ detailBug.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="重现步骤">
            <div style="white-space: pre-line">{{ detailBug.reproduceSteps || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="预期结果">{{ detailBug.expectedResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际结果">{{ detailBug.actualResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联用例">
            <span v-if="detailBug.caseId" class="case-link" @click="goToCase(detailBug)">
              {{ detailBug.caseNo ? detailBug.caseNo + ' ' : '' }}{{ detailBug.caseTitle || '用例#' + detailBug.caseId }}
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="附件">
            <div v-if="detailBug.attachment" class="detail-attachments">
              <template v-for="(file, idx) in toAttachArray(detailBug.attachment)" :key="idx">
                <img v-if="isImage(file)" :src="file" class="detail-thumb" @click="previewImage(file)" />
                <a v-else :href="file" target="_blank" class="file-link detail-file-link">
                  <el-icon><Document /></el-icon> {{ getFileName(file) }}
                </a>
              </template>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailBug.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="showImagePreview" width="auto" :show-close="true" align-center>
      <img :src="previewUrl" style="max-width: 80vw; max-height: 80vh; display: block; margin: 0 auto" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { exportStyledExcel } from '../utils/excelExport'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const projectId = route.params.projectId
const list = ref([])
const caseOptions = ref([]) // 项目下所有用例（用于下拉选择关联用例）

// 筛选
const searchKeyword = ref('')
const filterSeverity = ref('')
const filterPriority = ref('')
const filterStatus = ref('')

const severities = ['致命', '严重', '一般', '轻微', '建议']
const priorities = ['P0', 'P1', 'P2', 'P3']
const allStatuses = ['新建', '待修复', '已修复', '已验证', '关闭']

const showFormDialog = ref(false)
const showDetailDialog = ref(false)
const showImagePreview = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const detailBug = ref(null)
const form = ref({ id: null, title: '', module: '', description: '', reproduceSteps: '', expectedResult: '', actualResult: '', environment: '', affectedVersion: '', bugType: '', attachment: [], severity: '一般', bugPriority: 'P2', assignee: '', caseId: null })

// 文件上传地址
const uploadUrl = '/api/upload'

const filteredList = computed(() => {
  return list.value.filter(item => {
    if (searchKeyword.value && !item.title.includes(searchKeyword.value)) return false
    if (filterSeverity.value && item.severity !== filterSeverity.value) return false
    if (filterPriority.value && item.bugPriority !== filterPriority.value) return false
    if (filterStatus.value && item.status !== filterStatus.value) return false
    return true
  })
})

function resetFilters() {
  searchKeyword.value = ''
  filterSeverity.value = ''
  filterPriority.value = ''
  filterStatus.value = ''
}

onMounted(() => loadList())

async function loadList() {
  list.value = await request.get('/bugs', { params: { projectId } })
}

function bugStatusType(s) {
  return { '新建': 'info', '待修复': 'warning', '已修复': 'success', '已验证': 'primary', '关闭': 'info' }[s] || 'info'
}

async function changeStatus(row, newStatus) {
  if (row.status === newStatus) { row._showStatus = false; return }
  try {
    await request.put(`/bugs/${row.id}/status`, { status: newStatus })
    ElMessage.success(`状态已更新为「${newStatus}」`)
    row._showStatus = false
    loadList()
  } catch (e) {
    row._showStatus = false
    loadList()
  }
}

async function goToCase(row) {
  if (!row.caseId) return
  try {
    const testCase = await request.get(`/cases/${row.caseId}`)
    if (testCase && testCase.requirementId) {
      showDetailDialog.value = false
      router.push(`/requirement/${testCase.requirementId}/cases`)
    } else {
      ElMessage.warning('该用例不存在或已被删除')
    }
  } catch (e) {
    ElMessage.warning('该用例不存在或已被删除')
  }
}

function openDetail(row) {
  detailBug.value = row
  showDetailDialog.value = true
}

// 加载项目下所有用例（用于下拉选择关联用例）
async function loadCaseOptions() {
  try {
    const reqs = await request.get('/requirements', { params: { projectId } })
    const allCases = []
    for (const req of reqs) {
      const cases = await request.get('/cases', { params: { requirementId: req.id } })
      cases.forEach(c => allCases.push({
        value: c.id,
        label: `${c.caseNo || ''} ${c.title}`
      }))
    }
    caseOptions.value = allCases
  } catch (e) {
    caseOptions.value = []
  }
}

function openAddDialog() {
  isEdit.value = false
  form.value = { id: null, title: '', module: '', description: '', reproduceSteps: '', expectedResult: '', actualResult: '', environment: '', affectedVersion: '', bugType: '', attachment: [], severity: '一般', bugPriority: 'P2', assignee: '', caseId: null }
  loadCaseOptions()
  showFormDialog.value = true
}
function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row, attachment: row.attachment ? row.attachment.split(',').filter(s => s.trim()) : [] }
  loadCaseOptions()
  showFormDialog.value = true
}
async function submitForm() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入Bug标题'); return }
  // 附件数组转逗号分隔字符串提交
  const payload = { ...form.value, attachment: form.value.attachment.join(',') }
  if (isEdit.value) {
    await request.put(`/bugs/${form.value.id}`, payload)
    ElMessage.success('编辑成功')
  } else {
    await request.post('/bugs', { ...payload, projectId: Number(projectId), reporter: '当前用户' })
    ElMessage.success('新建成功')
  }
  showFormDialog.value = false
  loadList()
}

// 文件上传前校验
function beforeUpload(file) {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) { ElMessage.error('文件大小不能超过10MB'); return false }
  return true
}

// 判断是否为图片
function isImage(url) {
  if (!url) return false
  return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(url)
}

// 从URL提取原始文件名（去掉UUID前缀）
function getFileName(url) {
  if (!url) return ''
  const name = url.substring(url.lastIndexOf('/') + 1)
  // 新格式：UUID_文件名，去掉UUID前缀
  const underscoreIdx = name.indexOf('_')
  if (underscoreIdx > 0 && underscoreIdx < 40) {
    return name.substring(underscoreIdx + 1)
  }
  return name
}

// 预览图片
function previewImage(url) {
  previewUrl.value = url
  showImagePreview.value = true
}

// 下载文件
function downloadFile(url) {
  window.open(url, '_blank')
}

// 把逗号分隔的附件字符串转成数组
function toAttachArray(str) {
  if (!str) return []
  return str.split(',').filter(s => s.trim())
}
// 获取附件数量
function getAttachCount(str) {
  return toAttachArray(str).length
}
// 获取第一张图片
function getFirstImage(str) {
  const arr = toAttachArray(str)
  return arr.find(f => isImage(f)) || null
}
// 点击附件预览
function previewFirstImage(str) {
  const img = getFirstImage(str)
  if (img) {
    previewImage(img)
  } else {
    const arr = toAttachArray(str)
    if (arr.length > 0) downloadFile(arr[0])
  }
}

// 上传成功回调
function handleUploadSuccess(response) {
  if (response.code === 200) {
    form.value.attachment.push(response.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

async function deleteItem(row) {
  await ElMessageBox.confirm(`确定删除Bug「${row.title}」吗？`, '删除确认', { type: 'warning' })
  await request.delete(`/bugs/${row.id}`)
  ElMessage.success('删除成功')
  loadList()
}

function severityType(s) {
  return { '致命': 'danger', '严重': 'danger', '一般': 'warning', '轻微': 'info', '建议': 'success' }[s] || 'info'
}
function priorityType(p) { return { 'P0': 'danger', 'P1': 'warning', 'P2': 'primary', 'P3': 'info' }[p] || 'info' }

// 导出Excel（带样式）
async function exportExcel() {
  if (filteredList.value.length === 0) { ElMessage.warning('暂无数据可导出'); return }
  const headers = [
    { key: 'index', label: '序号', width: 6 },
    { key: 'bugNo', label: 'Bug编号', width: 10 },
    { key: 'module', label: '所属模块', width: 15 },
    { key: 'title', label: 'Bug标题', width: 25 },
    { key: 'description', label: 'Bug描述', width: 20 },
    { key: 'reproduceSteps', label: '重现步骤', width: 25 },
    { key: 'expectedResult', label: '预期结果', width: 18 },
    { key: 'actualResult', label: '实际结果', width: 18 },
    { key: 'environment', label: '测试环境', width: 16 },
    { key: 'affectedVersion', label: '影响版本', width: 12 },
    { key: 'bugType', label: 'Bug类型', width: 12 },
    { key: 'severity', label: '严重程度', width: 10 },
    { key: 'bugPriority', label: '优先级', width: 8 },
    { key: 'status', label: '状态', width: 10 },
    { key: 'attachment', label: '附件', width: 16 },
    { key: 'reporter', label: '提交人', width: 10 },
    { key: 'assignee', label: '修复人', width: 10 },
    { key: 'caseTitle', label: '关联用例', width: 18 },
    { key: 'createTime', label: '创建时间', width: 18 }
  ]
  const data = filteredList.value.map((item, index) => ({
    index: index + 1,
    bugNo: item.bugNo || '',
    module: item.module || '',
    title: item.title,
    description: item.description || '',
    reproduceSteps: item.reproduceSteps || '',
    expectedResult: item.expectedResult || '',
    actualResult: item.actualResult || '',
    environment: item.environment || '',
    affectedVersion: item.affectedVersion || '',
    bugType: item.bugType || '',
    severity: item.severity,
    bugPriority: item.bugPriority || 'P2',
    status: item.status,
    attachment: item.attachment || '',
    reporter: item.reporter || '',
    assignee: item.assignee || '',
    caseTitle: (item.caseNo ? item.caseNo + ' ' : '') + (item.caseTitle || (item.caseId ? '用例#' + item.caseId : '未关联')),
    createTime: item.createTime
  }))
  try {
    await exportStyledExcel('Bug列表', 'Bug列表', headers, data, 'attachment')
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败:', e)
    ElMessage.error('导出失败：' + (e && e.message ? e.message : String(e)))
  }
}
</script>

<style scoped>
.page-top { margin-bottom: 12px; }
.back-btn { color: #909399; font-size: 15px; padding: 4px 0; }
.back-btn:hover { color: #667eea; }
.page-hero { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.hero-title { margin: 0; font-size: 32px; font-weight: 700; color: #1a1a2e; }
.hero-desc { margin: 6px 0 0; font-size: 17px; color: #909399; }
.hero-btns { display: flex; gap: 12px; }
.ghost-btn { background: #fff; border: 1px solid #dcdfe6; color: #606266; }
.ghost-btn:hover { color: #667eea; border-color: #667eea; }
.filter-card { margin-bottom: 16px; border: 1px solid #ebeef5; }
.filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; color: #303133; }
.card-count { font-size: 15px; color: #909399; }
.bug-no { font-family: monospace; font-weight: 600; color: #f56c6c; font-size: 15px; }
.bug-title { font-weight: 600; color: #303133; }
.bug-title.link { cursor: pointer; }
.bug-title.link:hover { color: #667eea; }
.text-muted { color: #909399; font-size: 15px; }
.status-tag { cursor: pointer; transition: transform 0.15s; }
.status-tag:hover { transform: scale(1.08); }
.status-picker { display: flex; flex-direction: column; gap: 6px; }
.status-option { padding: 4px 8px; border-radius: 6px; cursor: pointer; transition: background 0.15s; }
.status-option:hover { background: #f5f7fa; }
.status-option.active { background: #f0f4ff; }
.case-link { color: #667eea; cursor: pointer; display: flex; align-items: center; gap: 4px; font-size: 15px; }
.case-link:hover { text-decoration: underline; }
.empty-tip { padding: 20px 0; }
.form-row { display: flex; gap: 16px; }
.attachment-list { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 8px; }
.attachment-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.attachment-item .remove-btn { margin-top: -4px; padding: 0; font-size: 13px; }
.attachment-img { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; border: 1px solid #e0e0e0; cursor: pointer; }
.attachment-file { display: flex; flex-direction: column; align-items: center; gap: 4px; color: #606266; font-size: 13px; max-width: 100px; text-align: center; }
.attachment-file span { word-break: break-all; }
.file-link { display: flex; align-items: center; gap: 6px; color: #667eea; text-decoration: none; }
.file-link:hover { text-decoration: underline; }
.cell-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 1px solid #e0e0e0; }
.cell-thumb:hover { opacity: 0.8; }
.file-icon { cursor: pointer; }
.file-icon:hover { color: #667eea; }
.detail-thumb { width: 120px; height: 120px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 1px solid #e0e0e0; }
.detail-thumb:hover { opacity: 0.8; }
.cell-attachments { position: relative; display: inline-block; cursor: pointer; }
.attach-count {
  position: absolute; bottom: -4px; right: -8px;
  background: #667eea; color: #fff;
  font-size: 11px; padding: 1px 5px; border-radius: 8px;
  line-height: 1.2;
}
.detail-attachments { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.detail-file-link { font-size: 15px; }
/* Bug详情 */
.bug-detail { }
.detail-header { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.detail-title { font-weight: 600; font-size: 16px; color: #303133; flex: 1; }
.detail-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.detail-desc { margin-top: 8px; }
</style>
