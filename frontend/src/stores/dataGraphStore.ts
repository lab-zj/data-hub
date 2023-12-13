import { cloneDeep, merge } from 'lodash'
import { defineStore } from 'pinia'
import { NODE_TYPES } from '@/constants/dataGraph'

interface INodeInfo {
  [key: string]: any
}

type IGraphActionMode = 'contextmenu' | 'rename' | 'locate' | 'viewoutput'

/**
 * 后端设计：项目和画布解耦
 * 一个用户可以有多个画布，以后可能会有画布列表
 * 现在是直接获取用户下面的所有画布（目前数组只有一个值，直接取第一个值即可）
 * 所以这里的projectId，在改造之后，没作用了
 */
export const useDataGraph = defineStore('dataGraph', {
  state: () => ({
    graph: null, // 画布
    targetNodeList: [] as INodeInfo[],
    /** @type {'contextmenu' | 'rename' | 'locate' | 'viewoutput'} */
    graphActionMode: '' as IGraphActionMode,
    rawData: {
      nodes: [],
      edges: []
    } as any,
    detail_list: [] as any, // 节点详情数组
    current_status: null, // 当前正在执行任务的整体状态
    status_list: [] as any, // 节点状态数组
    projectId: undefined,
    id: undefined, // 后端改造，对应后端返回的id
    graphUuid: undefined, // 后端改造，对应后端的graphUuid
    statusInterval: undefined, // 轮询画布运行定时器
    nodeOverviewVisible: false, // 右侧预览节点面板visible
    dataPreviewVisible: false, // 数据预览面板visible
    outputOverviewVisible: false, // 算法执行输出面板visible
    current_output: null // 当前正在查看的输入
  }),
  getters: {
    // 已被使用的数据集
    used_datasets: (state: any): any => {
      return state.rawData?.nodes?.filter((node: any) => node.type === NODE_TYPES.DATA)
    },
    // 已被使用的算法
    used_algos: (state: any): any => {
      return state.rawData?.nodes?.filter((node: any) => node.type === NODE_TYPES.ALGO)
    },
    // 画布是否在执行完毕
    graph_completion: (state: any): any => {
      // return state.status_list?.every((_status: any) => ['finished', 'canceled', 'stopped'].includes(_status.status))
      return ['finished', 'canceled', 'stopped'].includes(state.current_status?.status)
    },
    // 画布运行中
    graph_running: (state: any): any => {
      // return state.status_list?.some((_status: any) => ['init', 'start'].includes(_status.status))
      return ['init', 'start'].includes(state.current_status?.status)
    }
  },
  actions: {
    setTargetNodeList(list: any) {
      this.targetNodeList = list
    },
    cleanTargetNodeList() {
      this.targetNodeList = []
    },
    addNode(_node: any) {
      this.rawData.nodes.push(_node)
    },
    addEdge(_edge: any) {
      this.rawData.edges.push(_edge)
    },
    setRawData({ nodes, edges }: { nodes: any; edges: any }) {
      this.rawData.nodes = cloneDeep(nodes)
      this.rawData.edges = cloneDeep(edges)
    },
    updateNode(id: string | number, _node: any) {
      const index = this.rawData.nodes.findIndex((node: any) => id === node.id)
      if (index > -1) {
        const node = merge(this.rawData.nodes[index], _node)
        this.rawData.nodes.splice(index, 1, node)
      }
    },
    updateEdge(id: string | number, _edge: any) {
      const index = this.rawData.edges.findIndex((edge: any) => id === edge.id)
      if (index > -1) {
        const edge = merge(this.rawData.edges[index], _edge)
        this.rawData.edges.splice(index, 1, edge)
      }
    },
    removeNode(id: string | number) {
      // 移除节点
      const index = this.rawData.nodes.findIndex((node: any) => id === node.id)
      if (index > -1) {
        this.rawData.nodes.splice(index, 1)
      }
      // 移除节点相关的边
      const relatedEdges = this.rawData.edges.filter((edge: any) => edge.source.cell === id || edge.target.cell === id)?.map((edge: any) => edge.id)
      relatedEdges.forEach((edgeId: string | number) => {
        this.removeEdge(edgeId)
      })
    },
    removeEdge(id: string | number) {
      const index = this.rawData.edges.findIndex((edge: any) => id === edge.id)
      if (index > -1) {
        this.rawData.edges.splice(index, 1)
      }
    },
    removeEdgesOfNode(nodeId: string | number) {
      this.rawData.edges = this.rawData.edges.filter((_edge: any) => {
        return !(_edge.source.cell === nodeId || _edge.target.cell === nodeId)
      })
    },
    packageRawData() {
      this.rawData.nodes.forEach((_node: any) => {
        const { id } = _node
        const dataset = this.detail_list.find((detail: any) => detail.taskUuid === id)
        _node.dataset = dataset
        const status = (this.status_list || []).find((_status: any) => _status.taskUuid === id)
        _node.status = status || ''
      })
    },
    updateDetail(id: string | number, _detail: any) {
      const index = this.detail_list.findIndex((detail: any) => detail.taskUuid === id)
      if (index > -1) {
        const newDetail = merge(this.detail_list[index], _detail)
        this.detail_list.splice(index, 1, newDetail)
      }
    },
    getDetail(id: string | number) {
      return this.rawData.nodes?.find((_node: any) => _node.id === id)
    },
    addDetail(detail: any) {
      this.detail_list.push(detail)
    },
    removeDetail(id: string | number) {
      const index = this.detail_list.findIndex((detail: any) => id === detail.taskUuid)
      if (index > -1) {
        this.detail_list.splice(index, 1)
      }
    },
    updateStatus(id: string | number) {
      const index = this.status_list.findIndex((status: any) => id === status.taskUuid)
      if (index > -1) {
        this.status_list.splice(index, 1)
      }
    },
    setInterval(_interval: any) {
      this.statusInterval = _interval
    },
    setProjectId(_id: any) {
      this.projectId = _id
    },
    setVisible(_var: string, _visible: boolean) {
      this[_var] = _visible
    },
    init() {
      this.nodeOverviewVisible = false
      this.dataPreviewVisible = false
      this.outputOverviewVisible = false
    },
    reset() {
      this.id = undefined
      this.graphUuid = undefined
      this.targetNodeList = []
      this.rawData = {
        nodes: [],
        edges: []
      }
      this.detail_list = []
      this.status_list = []
      this.current_status = null
      this.init()
    }
  }
})
