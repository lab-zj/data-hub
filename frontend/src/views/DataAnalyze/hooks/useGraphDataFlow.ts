/**
 * 画布数据流：数据相关接口请求等
 */
import { ref } from 'vue'
import { NODE_TYPES, NODE_TYPES_ZHCN } from '@/constants/dataGraph'
import { useDataGraph } from '@/stores/dataGraphStore'
import { ElMessage } from 'element-plus'
import {
  saveGraphApi,
  upsertTaskNodeApi,
  getGraphListApi,
  getGraphApi,
  getGraphJobStatusApi,
  getGraphTaskDetailsBatchApi,
  executeGraphApi,
  deleteTaskBatchApi,
  stopGraphJobApi,
  cancelGraphJobApi,
  settTaskBuildBatchInitApi,
  cleanGraphApi,
  graphTablesApi,
  getGraphListOfProjectApi
} from '@/api/graph.ts'
import { useRoute } from 'vue-router'
import { convertParams } from '../util/util'

export default function useGraphDataFlow() {
  const route = useRoute()
  const dataGraphStore = useDataGraph()

  async function getGraphListOfProject() {
    dataGraphStore.reset()
    const response = await getGraphListOfProjectApi({
      data: {
        projectId: +route.params.id
      }
    })
    const { code, data } = response.data
    if (code === 200 && data?.length > 0) {
      dataGraphStore.id = data[0].id
      dataGraphStore.graphUuid = data[0].graphUuid
    }
  }

  /**
   * 获取用户所有画布list
   */
  async function getGraphList() {
    const response = await getGraphListApi()
    const { code, data } = response.data
    if (code === 200 && data?.length > 0) {
      dataGraphStore.id = data[0].id
      dataGraphStore.graphUuid = data[0].graphUuid
    }
  }

  /**
   * 获取画布信息
   */
  async function getGraph() {
    if (!dataGraphStore.graphUuid) {
      return
    }
    const response = await getGraphApi(dataGraphStore.graphUuid)
    const { code, data } = response.data
    if (code === 200) {
      const _rawData = data?.data
        ? JSON.parse(data.data)
        : {
            nodes: [],
            edges: []
          }
      dataGraphStore.rawData.nodes = _rawData.nodes || []
      dataGraphStore.rawData.edges = _rawData.edges || []
      console.log('getGraph:', _rawData)
    }
  }

  /**
   * 获取画布上节点状态
   * 画布未运行过，该接口查不到状态，接口返回4000
   */
  async function getGraphJobStatus() {
    if (!dataGraphStore.graphUuid) {
      return
    }
    const response = await getGraphJobStatusApi(dataGraphStore.graphUuid)
    const { code, data, message } = response.data
    if (code === 200) {
      dataGraphStore.status_list = data?.taskRuntimeVOList || []
      dataGraphStore.current_status = data
    } else {
      console.error(message)
    }
  }

  /**
   * 批量获取画布上节点详情
   */
  async function getGraphTaskDetailsBatch() {
    const taskUuidList = dataGraphStore.rawData.nodes.map((_node: any) => _node.id)
    if (taskUuidList?.length === 0) {
      return
    }
    const response = await getGraphTaskDetailsBatchApi({
      data: {
        taskUuidList
      }
    })
    const { code, data, message } = response.data
    if (code === 200) {
      dataGraphStore.detail_list = data
    } else {
      console.error(message)
    }
  }

  /**
   * 删除节点
   */
  async function deleteTaskBatch(_list: any) {
    const response = await deleteTaskBatchApi({
      params: {
        taskUuidList: _list
      }
    })
    const { code, message } = response.data
    if (code !== 200) {
      console.error(message)
    }
  }

  /**
   * 生成新的task_node
   * @param _node
   */
  async function upsertTaskNode(_node?: any) {
    const params = buildTaskNodeParameters(_node)
    const response = await upsertTaskNodeApi({
      data: params
    })
    const { data, code } = response.data
    if (code === 200) {
      return {
        taskUuid: data.taskUuid,
        taskId: data.id
      }
    }
    return {
      taskUuid: undefined,
      taskId: undefined
    }
  }

  /**
   * 构建task node参数
   * @param _node
   */
  function buildTaskNodeParameters(_node: any) {
    const { id, type, name, dataset } = _node
    const detail = dataGraphStore.getDetail(id)
    if (type === NODE_TYPES.DATA) {
      return {
        id: detail?.dataset?.taskId,
        taskUuid: id,
        configurationReference: type === NODE_TYPES.DATA ? dataset.path : '', // data类型时，存放数据集的path
        taskTypeName: type, // 枚举值：sql、data、algorithm
        name
      }
    } else if (type === NODE_TYPES.SQL) {
      return {
        id: detail?.dataset?.taskId,
        taskUuid: id,
        configuration: dataset.sql || '', // data类型时，存放数据集的path
        taskTypeName: type,
        name
      }
    } else {
      return {
        id: detail?.dataset?.taskId,
        taskUuid: id,
        taskTypeName: type,
        name,
        configurationReference: dataset.algorithm, // 存放算法id
        inputParamTemplate: dataset.inputParamTemplate,
        outputParamTemplate: dataset.outputParamTemplate
      }
    }
  }

  /**
   * 保存画布信息
   */
  async function saveGraph() {
    const params = buildSaveParameters()
    console.log('saveGraph:', params)
    const response = await saveGraphApi({
      data: params
    })
    const { data, code, message } = response.data
    if (code !== 200) {
      console.error(message)
      return
    }
    dataGraphStore.id = data.id
    dataGraphStore.graphUuid = data.graphUuid
  }

  /**
   * 构建画布保存参数
   */
  function buildSaveParameters() {
    const _nodes = dataGraphStore.rawData?.nodes?.map((_node: any) => {
      const { dataset, status, ...rest } = _node
      return rest
    })
    const _edges = dataGraphStore.rawData?.edges?.map((_edge: any) => {
      const { dataset, ...rest } = _edge
      return rest
    })
    return {
      id: dataGraphStore.id,
      graphUuid: dataGraphStore.graphUuid,
      data: JSON.stringify({
        nodes: _nodes,
        edges: _edges
      }),
      projectId: +dataGraphStore.projectId
    }
  }

  /**
   * 运行
   */
  async function onExecute() {
    // 画布在运行中
    if (dataGraphStore.graph_running) {
      ElMessage.warning('画布在运行中...')
      return
    }
    // 如果前面有未运行的节点 或者 运行失败的节点，进行提示
    if (!checkExecutable()) {
      return
    }
    const { vertexIdList, edgeList, selectedVertexIdList } = buildExecuteParameters()
    const response = await executeGraphApi({
      data: {
        graphUuid: dataGraphStore.graphUuid,
        vertexIdList,
        edgeList,
        selectedVertexIdList,
        forceRun: true
      }
    })
    const { code, message } = response.data
    if (code === 200) {
      setStatusesInterval(true)
    } else {
      console.error(message)
    }
  }

  /**
   *  部分节点运行时判断是否可运行
   *  前置节点未运行、运行失败：都不可运行
   *  前置节点被暂停、被终止但是result有值：待确认
   *  return true: 可运行
   *  return false: 不可运行
   *  注意：
   *  1、选中要运行的部分节点中有【前置节点】：不需要看前置节点状态(相当于一个小执行链路，当成一个小画布即可)
   *  比如：直接框选中一个数据节点，此时不用判断数据节点的状态，直接可运行
   *  2、被cancel或者stop的节点，只要result里有值，就不影响任务运行
   *  3、sql节点需要有输入，sql节点后面可以接其他节点、也可以不接其他节点
   *  4、算法节点需要完整的输入和输出，输出节点目前限制为mysql和postgresql数据类型
   */
  function checkExecutable() {
    let vertexIdList: any = []
    // 部分节点执行
    if (dataGraphStore.targetNodeList?.length > 0) {
      vertexIdList = dataGraphStore.targetNodeList?.map((_node: any) => _node.id) || []
    } else {
      // TODO 整个画布运行时，每个节点都会运行到，不需要判断
      // 如果第一个不是数据节点，是否需要提醒用户？----不需要提醒用户，可以运行
      vertexIdList = dataGraphStore.rawData?.nodes?.map((_node: any) => _node.id) || []
    }
    return vertexIdList.every((_node: any) => {
      return checkNodeExecutable(_node, dataGraphStore.rawData?.edges || [], vertexIdList)
    })
  }

  /**
   * 检查sql节点或者算子节点是否可运行
   * 1、算子节点需要有完整的前置输入节点和后置输出节点
   * 2、单独选中者算子节点，不能运行！算法节点必须框选中前置输入、算子和后置输出节点
   * 3、单独选中者sql节点，不能运行！sql节点必须框选中前置输入和sql节点
   * @param _node
   * @param edges
   * @param _list: 所有选中的节点列表
   * @returns
   */
  function checkNodeExecutable(_node: any, edges: any, _list: any) {
    const _type = getNodeType(_node)
    const preList = getPreList(_node, edges || [])
    const postList = getPostList(_node, edges || [])

    if ([NODE_TYPES.ALGO].includes(_type) && (preList.length === 0 || postList.length === 0)) {
      ElMessage.warning(`${NODE_TYPES_ZHCN[_type]}需要连接前置输入节点和后置输出节点！`)
      return false
    }
    if ([NODE_TYPES.SQL].includes(_type) && preList.length === 0) {
      ElMessage.warning(`${NODE_TYPES_ZHCN[_type]}需要连接前置输入节点！`)
      return false
    }
    if (
      [NODE_TYPES.ALGO].includes(_type) &&
      preList.length > 0 &&
      postList.length > 0 &&
      (preList.every((_id: string | number) => !_list.includes(_id)) || postList.every((_id: string | number) => !_list.includes(_id)))
    ) {
      ElMessage.warning(`运行${NODE_TYPES_ZHCN[_type]}需要框选中前置输入节点、该${NODE_TYPES_ZHCN[_type]}和后置输出节点！`)
      return false
    }

    if ([NODE_TYPES.SQL].includes(_type) && preList.length > 0 && preList.every((_id: string | number) => !_list.includes(_id))) {
      ElMessage.warning(`运行${NODE_TYPES_ZHCN[_type]}需要框选中前置输入节点和该${NODE_TYPES_ZHCN[_type]}！`)
      return false
    }

    // 检查算子节点是否设置入参
    const hasAlgoInputsDone = checkAlgoInputs(_node, edges)
    if (_type === NODE_TYPES.ALGO && !hasAlgoInputsDone) {
      ElMessage.warning(`请在${NODE_TYPES_ZHCN[_type]}的右键菜单中设置入参！`)
      return false
    }

    const preStatus = preList.every((_id: string | number) => {
      const _node_status = dataGraphStore.status_list.find((_status: any) => _status.taskUuid === _id)
      // 直接框选中一个数据节点，此时不用判断数据节点的状态，直接可运行
      return (_node_status && ['success'].includes(_node_status.result)) || _list.includes(_id)
    })
    if (!preStatus) {
      ElMessage.warning('请检查前置节点状态！')
      return false
    }
    return true
  }

  /**
   * 获取前置节点
   * @param _id
   */
  function getPreList(_id: string | number, edges: any) {
    const preList = edges?.filter((_edge: any) => _id === _edge.target.cell)?.map((_edge: any) => _edge.source.cell)
    return preList
  }

  /**
   * 获取后置节点
   */
  function getPostList(_id: string | number, edges: any) {
    const postList = edges?.filter((_edge: any) => _id === _edge.source.cell)?.map((_edge: any) => _edge.target.cell)
    return postList
  }

  /**
   * 获取节点
   */
  function getNode(_id: string | number) {
    const node = dataGraphStore.rawData?.nodes?.find((_node: any) => _node.id === _id)
    return node
  }

  /**
   * 获取节点类型
   */
  function getNodeType(_id: string | number) {
    const node = dataGraphStore.rawData?.nodes?.find((_node: any) => _node.id === _id)
    return node.type
  }

  /**
   * 获取节点所有入边
   */
  function getInComeEdegs(_id: string | number, edges: any) {
    const inComeEdges = edges?.filter((_edge: any) => _edge.target.cell === _id)
    return inComeEdges
  }

  /**
   * 检查算子节点的入参是否设置
   * @param _id
   * @param edges
   * @returns
   */
  function checkAlgoInputs(_id: string | number, edges: any) {
    const nodeInfo = getNode(_id)
    const inComeEdges = getInComeEdegs(_id, edges)
    const inputList = convertParams(nodeInfo?.dataset?.inputParamTemplate)
    const allDone = inputList?.every((_input: any) => {
      const { name } = _input
      return inComeEdges?.some((_edge: any) => _edge.name === name)
    })
    return allDone
  }

  /**
   * 构建运行参数
   */
  function buildExecuteParameters() {
    let vertexIdList: any = null
    let edgeList = null
    let selectedVertexIdList: any = null

    // 节点的输出port，算子节点自定义输出port，其他节点默认输出port:'default'
    const getFromPram = (port: string) => (!port || port.includes('output+') ? 'default' : port)
    // 组装执行job需要的edge
    const packageEdge = (_edge: any, index: number) => {
      return {
        from: {
          uuid: _edge.source.cell,
          param: getFromPram(_edge.source.port)
        },
        to: {
          uuid: _edge.target.cell,
          param: _edge.name || `input_${index + 1}`
        }
      }
    }

    // 部分节点执行
    if (dataGraphStore.targetNodeList?.length > 0) {
      selectedVertexIdList = dataGraphStore.targetNodeList?.map((_node: any) => _node.id) || []
      vertexIdList = [].concat(selectedVertexIdList)
      edgeList = dataGraphStore.rawData?.edges
        ?.filter((_edge: any) => vertexIdList.includes(_edge.target.cell))
        ?.map((_edge: any, index: number) => {
          if (!vertexIdList.includes(_edge.source.cell)) {
            vertexIdList.push(_edge.source.cell)
          }
          return packageEdge(_edge, index)
        })
    } else {
      // 全部节点执行
      selectedVertexIdList = []
      vertexIdList = dataGraphStore.rawData?.nodes?.map((_node: any) => _node.id)
      edgeList = dataGraphStore.rawData?.edges?.map((_edge: any, index: number) => {
        return packageEdge(_edge, index)
      })
    }
    return {
      vertexIdList,
      edgeList,
      selectedVertexIdList
    }
  }

  /**
   * 暂停画布执行
   */
  async function stopGraphJob() {
    if (!dataGraphStore.graphUuid || !dataGraphStore.graph_running) {
      ElMessage.warning('没有正在运行的任务！')
      return
    }
    const response = await stopGraphJobApi(dataGraphStore.graphUuid)
    const { code, message, data } = response.data
    if (code !== 200) {
      console.error(message)
    } else {
      console.log('更新状态')
      dataGraphStore.status_list = data?.taskRuntimeVOList || []
      dataGraphStore.current_status = data
    }
  }

  /**
   * 取消画布执行（终止执行）
   * 还未执行的节点直接被取消，已经在执行的节点无法取消就还一直在执行
   */
  async function cancelGraphJob() {
    if (!dataGraphStore.graphUuid || !dataGraphStore.graph_running) {
      ElMessage.warning('没有正在运行的任务！')
      return
    }
    const canceledTaskList = dataGraphStore.status_list?.filter((_status: any) => _status.status === 'canceled')?.map((_status: any) => _status.taskId)
    if (canceledTaskList?.length > 0) {
      await setTaskBuildBatchInit(canceledTaskList)
    }
    const response = await cancelGraphJobApi(dataGraphStore.graphUuid)
    const { code, message, data } = response.data
    if (code !== 200) {
      console.error(message)
    } else {
      console.log('更新状态')
      dataGraphStore.status_list = data?.taskRuntimeVOList || []
      dataGraphStore.current_status = data
    }
  }

  /**
   *
   * @param taskUuidList
   */
  async function setTaskBuildBatchInit(taskUuidList: any) {
    await settTaskBuildBatchInitApi({
      params: {
        taskUuidList
      }
    })
  }

  // 画布执行中，不可进行其他任何操作
  const isExecuting = ref(false)

  async function intervalFunc() {
    await getGraphJobStatus()
    // 画布执行结束，停止状态轮询
    const done = dataGraphStore.graph_completion
    if (done) {
      clearStatusesInterval()
    } else {
      isExecuting.value = true
    }
  }

  /**
   * 轮询画布上所有节点的状态
   */
  function setStatusesInterval(immediate?: boolean) {
    if (!dataGraphStore.statusInterval) {
      if (immediate) {
        intervalFunc()
      }
      const interval = setInterval(intervalFunc, 1000 * 1)
      dataGraphStore.setInterval(interval)
    }
  }

  /**
   * 停止轮询
   */
  function clearStatusesInterval() {
    clearInterval(dataGraphStore.statusInterval)
    dataGraphStore.setInterval(null)
  }

  /**
   * 清空画布
   * @param _list
   */
  async function cleanGraph(_list: any) {
    const response = await cleanGraphApi({
      params: {
        graphUuidList: _list
      }
    })
    const { code, message } = response.data
    if (code !== 200) {
      console.error(message)
    } else {
      ElMessage.success('清空画布成功！')
      return true
    }
  }

  /**
   * 查询当前sql节点对应的sql面板可选择的table表
   */
  async function fetchSqlTable() {
    const { vertexIdList, edgeList, currentVertexId } = buildSqlTableFetchParameters()
    const response = await graphTablesApi({
      data: {
        graphUuid: dataGraphStore.graphUuid,
        vertexIdList,
        edgeList,
        currentVertexId
      }
    })
    const { code, message, data } = response.data
    if (code === 200) {
      const { tableList } = data
      const keys = Object.keys(tableList)
      let result: any = []
      keys.forEach((key: any) => {
        result = result.concat(tableList[key])
      })
      return result
    } else {
      console.error(message)
      return [
        {
          showName: 'no suggest',
          value: ''
        }
      ]
    }
  }

  /**
   * 构建获取sql可查询表的参数
   */
  function buildSqlTableFetchParameters() {
    let vertexIdList: any = null
    let edgeList = null
    let currentVertexId: string = ''

    // 节点的输出port，算子节点自定义输出port，其他节点默认输出port:'default'
    const getFromPram = (port: string) => (!port || port.includes('output+') ? 'default' : port)
    // 组装执行job需要的edge
    const packageEdge = (_edge: any, index: number) => {
      return {
        from: {
          uuid: _edge.source.cell,
          param: getFromPram(_edge.source.port)
        },
        to: {
          uuid: _edge.target.cell,
          param: _edge.name || `input_${index + 1}`
        }
      }
    }

    // 当前选中sql节点
    if (dataGraphStore.targetNodeList?.length > 0) {
      currentVertexId = dataGraphStore.targetNodeList?.[0]?.id
      vertexIdList = []
      edgeList = dataGraphStore.rawData?.edges
        ?.filter((_edge: any) => _edge.target.cell === currentVertexId)
        ?.map((_edge: any, index: number) => {
          if (!vertexIdList.includes(_edge.source.cell) && _edge.source.cell !== currentVertexId) {
            vertexIdList.push(_edge.source.cell)
          }
          return packageEdge(_edge, index)
        })
    }
    return {
      vertexIdList,
      edgeList,
      currentVertexId
    }
  }

  return {
    getGraphList,
    getGraph,
    getGraphJobStatus,
    getGraphTaskDetailsBatch,
    deleteTaskBatch,
    upsertTaskNode,
    saveGraph,
    onExecute,
    stopGraphJob,
    cancelGraphJob,
    setStatusesInterval,
    clearStatusesInterval,
    cleanGraph,
    fetchSqlTable,
    getGraphListOfProject
  }
}
