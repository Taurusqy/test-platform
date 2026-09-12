<template>
  <div class="page">
    <!-- 页面标题区 -->
    <div class="page-hero">
      <div>
        <h1 class="hero-title">项目工作台</h1>
        <p class="hero-desc">管理所有测试项目，点击进入需求、用例、Bug与报告</p>
      </div>
      <el-button type="primary" size="large" @click="isEdit=false; resetForm(); showAddDialog = true" class="hero-btn">
        <el-icon><Plus /></el-icon> 新建项目
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon :size="22"><Folder /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ projectList.length }}</div>
          <div class="stat-label">项目总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><el-icon :size="22"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ runningCount }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><el-icon :size="22"><Warning /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ pausedCount }}</div>
          <div class="stat-label">已暂停</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><el-icon :size="22"><Finished /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ finishedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 项目表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">项目列表</span>
          <span class="card-count">共 {{ projectList.length }} 个项目</span>
        </div>
      </template>
      <el-table :data="projectList" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="180">
          <template #default="{ row }">
            <div class="project-name-cell">
              <div class="project-avatar">{{ row.name.charAt(0) }}</div>
              <span class="project-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="项目描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-muted">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="light" round>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="周期" width="180" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.startDate || '—' }} ~ {{ row.endDate || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goRequirements(row)">
              <el-icon><Document /></el-icon> 需求
            </el-button>
            <el-button type="warning" link size="small" @click="goBugs(row)">
              <el-icon><Warning /></el-icon> Bug
            </el-button>
            <el-button type="success" link size="small" @click="goReport(row)">
              <el-icon><DataAnalysis /></el-icon> 报告
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" link size="small" @click="deleteProject(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="projectList.length === 0" class="empty-tip">
        <el-empty description="暂无项目，点击右上角新建" />
      </div>
    </el-card>

    <!-- 新增/编辑项目弹窗 -->
    <el-dialog v-model="showAddDialog" :title="isEdit ? '编辑项目' : '新建项目'" width="520px">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.name" placeholder="请输入项目名称" size="large" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-radio-group v-model="form.status">
            <el-radio value="进行中">进行中</el-radio>
            <el-radio value="已完成">已完成</el-radio>
            <el-radio value="已暂停">已暂停</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitForm">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const projectList = ref([])
const showAddDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, name: '', description: '', status: '进行中', startDate: '', endDate: '' })

const runningCount = computed(() => projectList.value.filter(p => p.status === '进行中').length)
const pausedCount = computed(() => projectList.value.filter(p => p.status === '已暂停').length)
const finishedCount = computed(() => projectList.value.filter(p => p.status === '已完成').length)

onMounted(() => loadProjects())

async function loadProjects() {
  projectList.value = await request.get('/projects')
}

function resetForm() {
  form.value = { id: null, name: '', description: '', status: '进行中', startDate: '', endDate: '' }
}

function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row }
  showAddDialog.value = true
}

async function submitForm() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }
  if (isEdit.value) {
    await request.put(`/projects/${form.value.id}`, form.value)
    ElMessage.success('保存成功')
  } else {
    await request.post('/projects', form.value)
    ElMessage.success('创建成功')
  }
  showAddDialog.value = false
  resetForm()
  loadProjects()
}

async function deleteProject(row) {
  await ElMessageBox.confirm(`确定删除项目「${row.name}」吗？删除后其下所有需求、用例、Bug将一并清除。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  })
  await request.delete(`/projects/${row.id}`)
  ElMessage.success('删除成功')
  loadProjects()
}

function goRequirements(row) { router.push(`/project/${row.id}/requirements`) }
function goBugs(row) { router.push(`/project/${row.id}/bugs`) }
function goReport(row) { router.push(`/project/${row.id}/report`) }

function statusType(s) {
  return { '进行中': 'success', '已完成': 'info', '已暂停': 'warning' }[s] || 'info'
}
</script>

<style scoped>
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.hero-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}
.hero-desc {
  margin: 8px 0 0;
  font-size: 17px;
  color: #909399;
}
.hero-btn {
  padding: 12px 24px;
  font-size: 15px;
}
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-icon .el-icon { font-size: 28px; }
.stat-icon.blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.green { background: linear-gradient(135deg, #11998e, #38ef7d); }
.stat-icon.orange { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.purple { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-num { font-size: 32px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 16px; color: #909399; margin-top: 4px; }
.table-card { margin-bottom: 0; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title { font-weight: 600; font-size: 16px; color: #303133; }
.card-count { font-size: 15px; color: #909399; }
.project-name-cell { display: flex; align-items: center; gap: 10px; }
.project-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}
.project-name { font-weight: 600; color: #303133; }
.text-muted { color: #909399; font-size: 15px; }
.empty-tip { padding: 20px 0; }
@media (max-width: 900px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
}
</style>
