<template>
  <div class="page">
    <div class="page-top">
      <el-button text class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回需求列表
      </el-button>
    </div>

    <div class="page-hero">
      <div>
        <h1 class="hero-title">测试用例</h1>
        <p class="hero-desc">管理和执行测试用例，失败时可联动创建Bug</p>
      </div>
      <div class="hero-btns">
        <el-button size="large" @click="exportExcel" class="ghost-btn">
          <el-icon><Download /></el-icon> 导出Excel
        </el-button>
        <el-button type="primary" size="large" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新建用例
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索用例标题" clearable style="width: 220px" :prefix-icon="Search" />
        <el-select v-model="filterPriority" placeholder="优先级" clearable style="width: 120px">
          <el-option v-for="p in priorities" :key="p" :label="p" :value="p" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="执行状态" clearable style="width: 120px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filterType" placeholder="用例类型" clearable style="width: 130px">
          <el-option v-for="t in caseTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">用例列表</span>
          <span class="card-count">共 {{ filteredList.length }} 条用例</span>
        </div>
      </template>
      <el-table :data="filteredList" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="caseNo" label="用例编号" width="90" align="center">
          <template #default="{ row }">
            <span class="case-no">{{ row.caseNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="用例标题" min-width="160">
          <template #default="{ row }">
            <span class="case-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="caseType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="info">{{ row.caseType || '功能测试' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="dark">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="执行状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="dark">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="附件" width="80" align="center">
          <template #default="{ row }">
            <div v-if="row.attachment" class="cell-attachments" @click.stop="previewFirstImage(row.attachment)">
              <img v-if="getFirstImage(row.attachment)" :src="getFirstImage(row.attachment)" class="cell-thumb" />
              <el-icon v-else :size="22" color="#909399"><Document /></el-icon>
              <span v-if="getAttachCount(row.attachment) > 1" class="attach-count">+{{ getAttachCount(row.attachment) - 1 }}</span>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联Bug" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.relatedBugNos" class="bug-link" @click="goBugs">
              {{ row.relatedBugNos }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="executor" label="执行人" width="80" align="center">
          <template #default="{ row }"><span>{{ row.executor || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="openExecuteDialog(row)">
              <el-icon><VideoPlay /></el-icon> 执行
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
        <el-empty description="暂无数据" />
      </div>
    </el-card>

    <!-- 新增/编辑用例弹窗 -->
    <el-dialog v-model="showFormDialog" :title="isEdit ? '编辑用例' : '新建用例'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用例标题" required>
          <el-input v-model="form.title" size="large" />
        </el-form-item>
        <el-form-item label="所属模块">
          <el-input v-model="form.module" placeholder="如：登录模块、购物车模块" />
        </el-form-item>
        <el-form-item label="测试目的">
          <el-input v-model="form.testPurpose" placeholder="一句话说明这条用例测什么" />
        </el-form-item>
        <el-form-item label="用例类型">
          <el-select v-model="form.caseType" size="large" style="width: 100%">
            <el-option v-for="t in caseTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="P0">P0 冒烟</el-radio>
            <el-radio value="P1">P1 核心</el-radio>
            <el-radio value="P2">P2 重要</el-radio>
            <el-radio value="P3">P3 次要</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="前置条件">
          <el-input v-model="form.precondition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="测试步骤">
          <el-input v-model="form.steps" type="textarea" :rows="4" placeholder="每行一个步骤，如：1.打开页面 2.输入账号" />
        </el-form-item>
        <el-form-item label="预期结果">
          <el-input v-model="form.expectedResult" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填，补充说明或注意事项" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行用例弹窗（显示用例详情） -->
    <el-dialog v-model="showExecuteDialog" title="执行用例" width="640px">
      <!-- 用例详情展示 -->
      <div class="case-detail" v-if="currentCase">
        <div class="detail-header">
          <el-tag type="info" size="small">{{ currentCase.caseNo }}</el-tag>
          <span class="detail-title">{{ currentCase.title }}</span>
          <el-tag :type="priorityType(currentCase.priority)" size="small">{{ currentCase.priority }}</el-tag>
        </div>
        <div class="detail-grid">
          <div class="detail-item" v-if="currentCase.testPurpose">
            <span class="detail-label">测试目的：</span>{{ currentCase.testPurpose }}
          </div>
          <div class="detail-item" v-if="currentCase.precondition">
            <span class="detail-label">前置条件：</span>{{ currentCase.precondition }}
          </div>
          <div class="detail-item" v-if="currentCase.steps">
            <span class="detail-label">测试步骤：</span>
            <div class="detail-steps">{{ currentCase.steps }}</div>
          </div>
          <div class="detail-item" v-if="currentCase.expectedResult">
            <span class="detail-label">预期结果：</span>{{ currentCase.expectedResult }}
          </div>
        </div>
      </div>
      <el-divider />
      <el-form :model="execForm" label-width="90px">
        <el-form-item label="执行结果" required>
          <el-radio-group v-model="execForm.status" size="large">
            <el-radio value="通过"><el-tag type="success" size="small">通过</el-tag></el-radio>
            <el-radio value="失败"><el-tag type="danger" size="small">失败</el-tag></el-radio>
            <el-radio value="阻塞"><el-tag type="warning" size="small">阻塞</el-tag></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实际结果">
          <el-input v-model="execForm.actualResult" type="textarea" :rows="3" placeholder="请描述实际执行结果" />
        </el-form-item>
        <el-form-item label="执行人">
          <el-input v-model="execForm.executor" placeholder="请输入执行人" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :action="uploadUrl"
            :headers="{}"
            :show-file-list="false"
            :on-success="handleExecUploadSuccess"
            :before-upload="beforeUpload"
            multiple
            accept="image/*,.txt,.log,.doc,.docx,.pdf,.zip,.rar">
            <el-button size="default">
              <el-icon><Upload /></el-icon> 上传附件（可多选）
            </el-button>
          </el-upload>
          <div v-if="execForm.attachment.length > 0" class="attachment-list">
            <div v-for="(file, idx) in execForm.attachment" :key="idx" class="attachment-item">
              <img v-if="isImage(file)" :src="file" class="attachment-img" @click="previewImage(file)" />
              <div v-else class="attachment-file" @click="downloadFile(file)">
                <el-icon :size="28"><Document /></el-icon>
                <span>{{ getFileName(file) }}</span>
              </div>
              <el-button type="danger" link size="small" class="remove-btn" @click="execForm.attachment.splice(idx, 1)">移除</el-button>
            </div>
          </div>
        </el-form-item>
        <template v-if="execForm.status === '失败'">
          <el-form-item label="联动建Bug">
            <el-switch v-model="execForm.createBug" active-text="自动创建Bug" />
          </el-form-item>
          <template v-if="execForm.createBug">
            <el-form-item label="Bug标题">
              <el-input v-model="execForm.bugTitle" />
            </el-form-item>
            <el-form-item label="严重程度">
              <el-select v-model="execForm.bugSeverity">
                <el-option label="致命" value="致命" />
                <el-option label="严重" value="严重" />
                <el-option label="一般" value="一般" />
                <el-option label="轻微" value="轻微" />
                <el-option label="建议" value="建议" />
              </el-select>
            </el-form-item>
          </template>
        </template>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showExecuteDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitExecute">提交执行</el-button>
      </template>
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
const requirementId = route.params.requirementId
const list = ref([])
const projectId = ref(null)

// 筛选
const searchKeyword = ref('')
const filterPriority = ref('')
const filterStatus = ref('')
const filterType = ref('')

const priorities = ['P0', 'P1', 'P2', 'P3']
const statuses = ['未执行', '通过', '失败', '阻塞']
const caseTypes = ['功能测试', '性能测试', '接口测试', '安全测试', '兼容性测试', 'UI测试']

const showFormDialog = ref(false)
const showExecuteDialog = ref(false)
const showImagePreview = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const currentCase = ref(null)
const form = ref({ id: null, title: '', module: '', testPurpose: '', precondition: '', steps: '', expectedResult: '', priority: 'P1', caseType: '功能测试', attachment: '', remark: '' })

// 文件上传地址（通过Vite代理转发到后端）
const uploadUrl = '/api/upload'
const execForm = ref({ status: '', actualResult: '', executor: '', attachment: [], createBug: false, bugTitle: '', bugSeverity: '一般' })
const currentCaseId = ref(null)

// 筛选后的列表
const filteredList = computed(() => {
  return list.value.filter(item => {
    if (searchKeyword.value && !item.title.includes(searchKeyword.value)) return false
    if (filterPriority.value && item.priority !== filterPriority.value) return false
    if (filterStatus.value && item.status !== filterStatus.value) return false
    if (filterType.value && item.caseType !== filterType.value) return false
    return true
  })
})

function resetFilters() {
  searchKeyword.value = ''
  filterPriority.value = ''
  filterStatus.value = ''
  filterType.value = ''
}

onMounted(() => {
  loadRequirementInfo()
  loadList()
})

async function loadRequirementInfo() {
  const req = await request.get(`/requirements/${requirementId}`)
  projectId.value = req.projectId
}

async function loadList() {
  list.value = await request.get('/cases', { params: { requirementId } })
}

function openAddDialog() {
  isEdit.value = false
  form.value = { id: null, title: '', module: '', testPurpose: '', precondition: '', steps: '', expectedResult: '', priority: 'P1', caseType: '功能测试', attachment: '', remark: '' }
  showFormDialog.value = true
}
function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row }
  showFormDialog.value = true
}
async function submitForm() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入用例标题'); return }
  if (isEdit.value) {
    await request.put(`/cases/${form.value.id}`, form.value)
    ElMessage.success('编辑成功')
  } else {
    await request.post('/cases', { ...form.value, requirementId: Number(requirementId) })
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
// 获取第一张图片（没有图片返回null）
function getFirstImage(str) {
  const arr = toAttachArray(str)
  return arr.find(f => isImage(f)) || null
}
// 点击附件预览：有图片预览第一张，没有图片下载第一个文件
function previewFirstImage(str) {
  const img = getFirstImage(str)
  if (img) {
    previewImage(img)
  } else {
    const arr = toAttachArray(str)
    if (arr.length > 0) downloadFile(arr[0])
  }
}

// 上传成功回调（编辑用例）
function handleUploadSuccess(response) {
  if (response.code === 200) {
    form.value.attachment = response.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 上传成功回调（执行用例）
function handleExecUploadSuccess(response) {
  if (response.code === 200) {
    execForm.value.attachment.push(response.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

function openExecuteDialog(row) {
  currentCaseId.value = row.id
  currentCase.value = row
  execForm.value = { status: '', actualResult: '', executor: '', attachment: [], createBug: false, bugTitle: row.title + ' 执行失败', bugSeverity: '一般' }
  showExecuteDialog.value = true
}
async function submitExecute() {
  if (!execForm.value.status) { ElMessage.warning('请选择执行结果'); return }
  // 附件数组转逗号分隔字符串提交
  const payload = { ...execForm.value, attachment: execForm.value.attachment.join(',') }
  const bug = await request.post(`/cases/${currentCaseId.value}/execute`, payload)
  if (bug) {
    ElMessage.success(`执行完成，已联动创建Bug ${bug.bugNo}`)
  } else {
    ElMessage.success('执行完成')
  }
  showExecuteDialog.value = false
  loadList()
}

async function deleteItem(row) {
  await ElMessageBox.confirm(`确定删除用例「${row.title}」吗？`, '删除确认', { type: 'warning' })
  await request.delete(`/cases/${row.id}`)
  ElMessage.success('删除成功')
  loadList()
}

function goBack() {
  if (projectId.value) {
    router.push(`/project/${projectId.value}/requirements`)
  } else {
    router.push('/')
  }
}
function goBugs() {
  if (projectId.value) {
    router.push(`/project/${projectId.value}/bugs`)
  } else {
    ElMessage.warning('正在加载项目信息，请稍候重试')
  }
}

// 导出Excel（带样式，附件嵌入图片）
async function exportExcel() {
  if (filteredList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = [
    { key: 'index', label: '序号', width: 6 },
    { key: 'caseNo', label: '用例编号', width: 10 },
    { key: 'module', label: '所属模块', width: 15 },
    { key: 'title', label: '用例标题', width: 25 },
    { key: 'testPurpose', label: '测试目的', width: 20 },
    { key: 'caseType', label: '用例类型', width: 12 },
    { key: 'priority', label: '优先级', width: 8 },
    { key: 'precondition', label: '前置条件', width: 18 },
    { key: 'steps', label: '测试步骤', width: 30 },
    { key: 'expectedResult', label: '预期结果', width: 20 },
    { key: 'status', label: '执行状态', width: 10 },
    { key: 'actualResult', label: '实际结果', width: 20 },
    { key: 'attachment', label: '附件', width: 16 },
    { key: 'relatedBugNos', label: '关联Bug', width: 12 },
    { key: 'remark', label: '备注', width: 15 },
    { key: 'executor', label: '执行人', width: 10 },
    { key: 'executeTime', label: '执行时间', width: 18 }
  ]
  const data = filteredList.value.map((item, index) => ({
    index: index + 1,
    caseNo: item.caseNo || '',
    module: item.module || '',
    title: item.title,
    testPurpose: item.testPurpose || '',
    caseType: item.caseType || '功能测试',
    priority: item.priority,
    precondition: item.precondition || '',
    steps: item.steps || '',
    expectedResult: item.expectedResult || '',
    status: item.status,
    actualResult: item.actualResult || '',
    attachment: item.attachment || '',
    relatedBugNos: item.relatedBugNos || '',
    remark: item.remark || '',
    executor: item.executor || '',
    executeTime: item.executeTime || ''
  }))
  try {
    await exportStyledExcel('测试用例', '测试用例', headers, data, 'attachment')
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败:', e)
    ElMessage.error('导出失败：' + (e && e.message ? e.message : String(e)))
  }
}

function priorityType(p) { return { 'P0': 'danger', 'P1': 'warning', 'P2': 'primary', 'P3': 'info' }[p] || 'info' }
function statusType(s) { return { '通过': 'success', '失败': 'danger', '阻塞': 'warning', '未执行': 'info' }[s] || 'info' }
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
.case-no { font-family: monospace; font-weight: 600; color: #667eea; font-size: 15px; }
.case-title { font-weight: 600; color: #303133; }
.empty-tip { padding: 20px 0; }
.attachment-list { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 8px; }
.attachment-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.attachment-item .remove-btn { margin-top: -4px; padding: 0; font-size: 13px; }
.attachment-img { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; border: 1px solid #e0e0e0; cursor: pointer; }
.attachment-file { display: flex; flex-direction: column; align-items: center; gap: 4px; color: #606266; font-size: 13px; max-width: 100px; text-align: center; }
.attachment-file span { word-break: break-all; }
.cell-thumb { width: 44px; height: 44px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 1px solid #e0e0e0; }
.cell-thumb:hover { opacity: 0.8; }
.file-icon { cursor: pointer; }
.file-icon:hover { color: #667eea; }
.cell-attachments { position: relative; display: inline-block; cursor: pointer; }
.attach-count {
  position: absolute; bottom: -4px; right: -8px;
  background: #667eea; color: #fff;
  font-size: 11px; padding: 1px 5px; border-radius: 8px;
  line-height: 1.2;
}
.bug-link { color: #f56c6c; cursor: pointer; font-weight: 600; font-size: 15px; }
.bug-link:hover { text-decoration: underline; }
/* 执行弹窗用例详情 */
.case-detail { background: #f8f9fc; border-radius: 8px; padding: 16px; }
.detail-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.detail-title { font-weight: 600; font-size: 15px; color: #303133; flex: 1; }
.detail-grid { display: flex; flex-direction: column; gap: 8px; }
.detail-item { font-size: 15px; color: #606266; line-height: 1.6; }
.detail-label { font-weight: 600; color: #303133; }
.detail-steps { white-space: pre-line; margin-top: 4px; padding-left: 4px; }
</style>
