<template>
  <div class="page">
    <div class="page-top">
      <el-button text class="back-btn" @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon> 返回项目列表
      </el-button>
    </div>

    <div class="page-hero">
      <div>
        <h1 class="hero-title">需求管理</h1>
        <p class="hero-desc">管理项目下的测试需求，点击进入用例管理</p>
      </div>
      <div class="hero-btns">
        <el-button type="primary" size="large" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新建需求
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">需求列表</span>
          <span class="card-count">共 {{ list.length }} 条需求</span>
        </div>
      </template>
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="reqNo" label="需求编号" width="90" align="center">
          <template #default="{ row }">
            <span class="req-no">{{ row.reqNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="需求标题" min-width="160">
          <template #default="{ row }">
            <span class="req-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="需求内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-muted">{{ row.content || '暂无内容' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="light" round>{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="reqStatusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goCases(row)">
              <el-icon><Document /></el-icon> 用例
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
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无需求，点击右上角新建" />
      </div>
    </el-card>

    <!-- 新增/编辑需求弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑需求' : '新建需求'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="需求标题" required>
          <el-input v-model="form.title" placeholder="请输入需求标题" size="large" />
        </el-form-item>
        <el-form-item label="需求内容">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入需求详细内容" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="高">高</el-radio>
            <el-radio value="中">中</el-radio>
            <el-radio value="低">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需求状态">
          <el-select v-model="form.status" size="large" style="width: 100%">
            <el-option label="待分析" value="待分析" />
            <el-option label="分析中" value="分析中" />
            <el-option label="已实现" value="已实现" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const projectId = route.params.projectId
const list = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, title: '', content: '', priority: '中', status: '待分析' })

onMounted(() => loadList())

async function loadList() {
  list.value = await request.get('/requirements', { params: { projectId } })
}

function openAddDialog() {
  isEdit.value = false
  form.value = { id: null, title: '', content: '', priority: '中', status: '待分析' }
  showDialog.value = true
}
function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row }
  showDialog.value = true
}
async function submitForm() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入需求标题'); return }
  if (isEdit.value) {
    await request.put(`/requirements/${form.value.id}`, form.value)
    ElMessage.success('编辑成功')
  } else {
    await request.post('/requirements', { ...form.value, projectId: Number(projectId) })
    ElMessage.success('新建成功')
  }
  showDialog.value = false
  loadList()
}

async function deleteItem(row) {
  await ElMessageBox.confirm(`确定删除需求「${row.title}」吗？其下所有用例将一并删除。`, '删除确认', { type: 'warning' })
  await request.delete(`/requirements/${row.id}`)
  ElMessage.success('删除成功')
  loadList()
}

function goCases(row) { router.push(`/requirement/${row.id}/cases`) }
function goBugs() { router.push(`/project/${projectId}/bugs`) }
function priorityType(p) { return { '高': 'danger', '中': 'warning', '低': 'info' }[p] || 'info' }
function reqStatusType(s) { return { '待分析': 'info', '分析中': 'warning', '已实现': 'success' }[s] || 'info' }
</script>

<style scoped>
.page-top { margin-bottom: 12px; }
.back-btn {
  color: #909399;
  font-size: 15px;
  padding: 4px 0;
}
.back-btn:hover { color: #409eff; }
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.hero-title { margin: 0; font-size: 32px; font-weight: 700; color: #1a1a2e; }
.hero-desc { margin: 6px 0 0; font-size: 17px; color: #909399; }
.hero-btns { display: flex; gap: 12px; }
.ghost-btn {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}
.ghost-btn:hover {
  color: #409eff;
  border-color: #409eff;
}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; color: #303133; }
.card-count { font-size: 15px; color: #909399; }
.req-title { font-weight: 600; color: #303133; }
.req-no { font-family: monospace; font-weight: 600; color: #67c23a; font-size: 15px; }
.text-muted { color: #909399; font-size: 15px; }
.empty-tip { padding: 20px 0; }
</style>
