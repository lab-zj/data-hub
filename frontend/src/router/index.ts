import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import DataAnalyze from '@/views/DataAnalyze/DataAnalyze.vue'
import ProjectProcedureView from '@/views/ProjectProcedureView.vue'
import AlgorithmView from '@/views/AlgorithmView.vue'
import ProjectManagement from '@/views/ProjectManagement/ProjectManagement.vue'
import UserCenter from '@/views/UserCenter/UserView.vue'
import UserConfig from '@/views/UserCenter/components/UserConfig.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      redirect: '/login',
      component: LoginView,
      children: [
        {
          path: 'login',
          name: 'LoginForm',
          component: () => import(/* webpackChunkName: "project-view" */ '../components/login/LoginForm.vue')
        },
        {
          path: 'register',
          name: 'RegisterForm',
          component: () => import(/* webpackChunkName: "project-view" */ '../components/login/RegisterForm.vue')
        }
      ]
    },
    {
      path: '/algorithm',
      name: 'Algorithm',
      component: AlgorithmView
    },
    {
      path: '/management',
      name: 'home',
      component: HomeView,
      children: [
        {
          path: 'projects',
          name: '项目管理',
          component: ProjectManagement
        },
        {
          path: 'data',
          name: 'Data',
          component: () => import(/* webpackChunkName: "project-view" */ '../views/DataPage/DataPage.vue'),
          children: [
            {
              path: 'path=:path',
              name: '数据管理',
              component: () => import(/* webpackChunkName: "project-view" */ '../views/DataPage/DataPage.vue')
            }
          ]
        },
        {
          path: 'detail',
          name: 'Detail',
          component: () => import(/* webpackChunkName: "project-view" */ '../components/data/DataDetail.vue'),
          children: [
            {
              path: 'path=:path',
              name: '数据详情',
              component: () => import(/* webpackChunkName: "project-view" */ '../components/data/DataDetail.vue')
            }
          ]
        }
      ]
    },
    {
      path: '/project/:id',
      name: 'projectProcedureView',
      component: ProjectProcedureView,
      children: [
        {
          path: 'data-analyze',
          name: 'dataAnalyze',
          component: DataAnalyze
        }
      ]
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: UserCenter,
      children: [
        {
          path: 'config',
          name: 'userConfig',
          component: UserConfig
        }
      ]
    }
  ]
})

export default router
