<template>
  <el-container class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="app-header">
      <div class="header-left" @click="$router.push('/')">
        <div class="logo-icon">
          <el-icon :size="22"><Box /></el-icon>
        </div>
        <div class="header-text">
          <span class="title">TestFlow</span>
          <span class="subtitle">测试用例&amp;缺陷管理平台</span>
        </div>
      </div>
      <div class="header-right"></div>
    </el-header>
    <!-- 页面内容区 -->
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
// 主布局组件
</script>

<style>
/* 全局字号放大 */
:root {
  --el-font-size-base: 17px;
  --el-font-size-small: 16px;
}
* { box-sizing: border-box; }
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f7fa;
  font-size: 18px;
}
.app-container { min-height: 100vh; }
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  height: 72px !important;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}
.logo-icon {
  width: 42px;
  height: 42px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(10px);
}
.header-text { display: flex; flex-direction: column; line-height: 1.2; }
.header-text .title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}
.header-text .subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.75);
  margin-top: 3px;
}
.nav-btn {
  color: rgba(255,255,255,0.75);
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
}
.nav-btn:hover {
  color: #fff;
  background: rgba(255,255,255,0.1);
}
.app-main {
  background: #f5f7fa;
  padding: 20px 28px 36px;
  max-width: 1760px;
  margin: 0 auto;
  width: 100%;
}
/* 卡片统一样式 */
.el-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
/* 表格优化 */
.el-table {
  border-radius: 8px;
  --el-table-border-color: #f0f0f0;
}
.el-table th.el-table__cell {
  background: #fafbfc !important;
  color: #606266;
  font-weight: 600;
  font-size: 17px;
}
.el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fafbfc;
}
.el-table__body tr:hover > td {
  background: #f0f4ff !important;
}
.el-table .cell { padding: 18px 0; font-size: 17px; }
.el-table__row { height: 60px; }
/* 面包屑 */
.el-breadcrumb__inner {
  color: #909399;
  font-size: 16px;
  cursor: pointer;
}
.el-breadcrumb__inner:hover {
  color: #409eff;
}
/* 弹窗优化 */
.el-dialog {
  border-radius: 12px;
  overflow: hidden;
}
.el-dialog__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.el-dialog__title { font-weight: 600; font-size: 22px; }
.el-dialog__body { padding: 32px; }
.el-dialog__footer { padding: 20px 32px 24px; border-top: 1px solid #f0f0f0; }
/* 表单项字号 */
.el-form-item__label { font-size: 17px; }
.el-input__inner, .el-textarea__inner, .el-select__placeholder { font-size: 17px; }
.el-button { font-size: 17px; }
.el-radio, .el-checkbox { font-size: 17px; }
.el-tag { font-size: 15px; }
/* 页面标题 */
.page-title { font-size: 22px; }
.page-subtitle { font-size: 15px; }
/* 按钮优化：保留渐变，去掉凸起阴影 */
.el-button--primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}
.el-button--primary:hover {
  background: linear-gradient(135deg, #7c8ff0, #8b5fb5);
}
</style>
