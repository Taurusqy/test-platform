<template>
  <div class="page">
    <div class="page-top">
      <el-button text class="back-btn" @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon> 返回项目列表
      </el-button>
    </div>

    <div class="page-hero">
      <div>
        <h1 class="hero-title">{{ report.projectName }}</h1>
        <p class="hero-desc">测试报告 — 用例执行情况与Bug分布统计</p>
      </div>
      <el-button type="primary" size="large" @click="printReport">
        <el-icon><Printer /></el-icon> 打印报告
      </el-button>
    </div>

    <!-- 用例统计卡片 -->
    <el-card class="report-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon :size="18" color="#409eff"><DataAnalysis /></el-icon>
          <span>用例执行统计</span>
        </div>
      </template>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-num total">{{ report.caseStats?.total || 0 }}</div>
          <div class="stat-label">用例总数</div>
        </div>
        <div class="stat-item">
          <div class="stat-num passed">{{ report.caseStats?.passed || 0 }}</div>
          <div class="stat-label">通过</div>
        </div>
        <div class="stat-item">
          <div class="stat-num failed">{{ report.caseStats?.failed || 0 }}</div>
          <div class="stat-label">失败</div>
        </div>
        <div class="stat-item">
          <div class="stat-num blocked">{{ report.caseStats?.blocked || 0 }}</div>
          <div class="stat-label">阻塞</div>
        </div>
        <div class="stat-item">
          <div class="stat-num pending">{{ report.caseStats?.pending || 0 }}</div>
          <div class="stat-label">未执行</div>
        </div>
      </div>
      <div class="pass-rate-section">
        <div class="pass-rate-label">
          <span>通过率</span>
          <span class="pass-rate-value">{{ report.caseStats?.passRate || '0.0%' }}</span>
        </div>
        <el-progress :percentage="report.caseStats?.passRateValue || 0" :color="progressColor" :stroke-width="16" />
      </div>
    </el-card>

    <div class="report-row">
      <!-- Bug严重程度分布 -->
      <el-card class="report-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="18" color="#e6a23c"><Warning /></el-icon>
            <span>Bug严重程度分布</span>
          </div>
        </template>
        <div class="severity-list">
          <div v-for="(count, key) in severityEntries" :key="key" class="severity-row">
            <span class="sev-label" :style="{ color: sevColor(key) }">{{ key }}</span>
            <el-progress :percentage="severityPercent(count)" :show-text="false" :stroke-width="16" :color="sevColor(key)" />
            <span class="sev-count">{{ count }}</span>
          </div>
        </div>
        <div class="bug-total">
          Bug总数：<b>{{ report.bugSeverityStats?.total || 0 }}</b>
        </div>
      </el-card>

      <!-- Bug状态分布 -->
      <el-card class="report-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="18" color="#9b59b6"><Tickets /></el-icon>
            <span>Bug状态分布</span>
          </div>
        </template>
        <div class="status-tags">
          <div v-for="item in report.bugStatusStats" :key="item.status" class="status-badge">
            <el-tag :type="bugStatusType(item.status)" size="large" effect="dark">{{ item.status }}</el-tag>
            <span class="status-num">{{ item.count }}</span>
          </div>
          <span v-if="!report.bugStatusStats?.length" class="empty">暂无Bug</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const projectId = route.params.projectId
const report = ref({})

onMounted(async () => {
  report.value = await request.get(`/reports/${projectId}`)
})

const severityEntries = computed(() => {
  const s = report.value.bugSeverityStats || {}
  return Object.fromEntries(Object.entries(s).filter(([k]) => k !== 'total'))
})

function severityPercent(count) {
  const total = report.value.bugSeverityStats?.total || 0
  return total > 0 ? Math.round(count * 100 / total) : 0
}
function sevColor(key) {
  return { '致命': '#f56c6c', '严重': '#e6a23c', '一般': '#409eff', '轻微': '#909399', '建议': '#67c23a' }[key] || '#909399'
}
function bugStatusType(s) {
  return { '新建': 'info', '待修复': 'warning', '已修复': 'success', '已验证': 'primary', '关闭': 'info' }[s] || 'info'
}
function progressColor(percentage) {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}
function printReport() { window.print() }
</script>

<style scoped>
.page-top { margin-bottom: 12px; }
.back-btn { color: #909399; font-size: 15px; padding: 4px 0; }
.back-btn:hover { color: #409eff; }
.page-hero { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px; }
.hero-title { margin: 0; font-size: 32px; font-weight: 700; color: #1a1a2e; }
.hero-desc { margin: 6px 0 0; font-size: 17px; color: #909399; }
.report-card { margin-bottom: 20px; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 15px; color: #303133; }
.stats-grid { display: flex; justify-content: space-around; text-align: center; padding: 10px 0; }
.stat-num { font-size: 36px; font-weight: 700; line-height: 1.2; }
.stat-label { color: #909399; margin-top: 6px; font-size: 15px; }
.stat-item.total .stat-num { color: #303133; }
.stat-item.passed .stat-num { color: #67c23a; }
.stat-item.failed .stat-num { color: #f56c6c; }
.stat-item.blocked .stat-num { color: #e6a23c; }
.stat-item.pending .stat-num { color: #909399; }
.pass-rate-section { margin-top: 20px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.pass-rate-label { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; color: #606266; }
.pass-rate-value { font-size: 22px; font-weight: 700; color: #409eff; }
.report-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.severity-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.sev-label { width: 50px; text-align: right; font-weight: 600; font-size: 14px; }
.sev-count { width: 36px; font-weight: 700; font-size: 16px; color: #303133; }
.bug-total { margin-top: 12px; text-align: right; color: #606266; font-size: 14px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.status-tags { display: flex; flex-wrap: wrap; gap: 16px; padding: 10px 0; }
.status-badge { display: flex; align-items: center; gap: 8px; }
.status-num { font-size: 20px; font-weight: 700; color: #303133; }
.empty { color: #909399; }
@media (max-width: 768px) {
  .report-row { grid-template-columns: 1fr; }
  .stats-grid { flex-wrap: wrap; gap: 16px; }
}
</style>
