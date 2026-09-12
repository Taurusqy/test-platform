import { createRouter, createWebHistory } from 'vue-router'

// 路由配置：5个页面
const routes = [
  { path: '/', name: 'ProjectList', component: () => import('../views/ProjectList.vue'), meta: { title: '项目列表' } },
  { path: '/project/:projectId/requirements', name: 'RequirementList', component: () => import('../views/RequirementList.vue'), meta: { title: '需求管理' } },
  { path: '/requirement/:requirementId/cases', name: 'CaseList', component: () => import('../views/CaseList.vue'), meta: { title: '测试用例' } },
  { path: '/project/:projectId/bugs', name: 'BugList', component: () => import('../views/BugList.vue'), meta: { title: 'Bug管理' } },
  { path: '/project/:projectId/report', name: 'ReportView', component: () => import('../views/ReportView.vue'), meta: { title: '测试报告' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
