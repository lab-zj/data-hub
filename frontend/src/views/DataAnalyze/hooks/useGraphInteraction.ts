/**
 * 画布交互操作
 */
import { useDataGraph } from '@/stores/dataGraphStore'
import useGraphDataFlow from '@/views/DataAnalyze/hooks/useGraphDataFlow'
import { NODE_TYPES, EDGE_LINE_STYLES } from '@/constants/dataGraph'
import { ElMessage } from 'element-plus'
import _InfiniteScroll from 'element-plus/es/components/infinite-scroll'

export default function useGraphInteraction() {
  const dataGraphStore = useDataGraph()
  const { upsertTaskNode, saveGraph, deleteTaskBatch } = useGraphDataFlow()

  /**
   * 重命名
   * @param param0
   */
  async function rename({ id, _name }: { id: string | number; _name: string }) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    dataGraphStore.updateNode(id, {
      name: _name
    })
    const cell = graph.getCellById(id)
    const data = cell.getData()
    cell.setData({
      ...data,
      name: _name
    })
    await upsertTaskNode({
      id,
      type: data.type,
      name: _name,
      dataset: data.dataset
    })
    await saveGraph()
    dataGraphStore.graphActionMode = ''
  }

  /**
   * 删除节点、边或者空数据节点
   * @param list 节点/边List
   * @param type ['node','edge','empty-dataset']
   */
  async function deleteFromGraph({ list, type }: { list: any; type: string }) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    if (type === 'node') {
      const _list: any = []
      list.forEach((_node: any) => {
        _list.push(_node.id)
        dataGraphStore.removeNode(_node.id)
        dataGraphStore.removeDetail(_node.id)
        graph.removeNode(_node.id)
      })
      await deleteTaskBatch(_list)
    } else if (type === 'edge') {
      list.forEach((_edge: any) => {
        dataGraphStore.removeEdge(_edge.id)
        graph.removeEdge(_edge.id)
      })
    } else if (type === 'empty-dataset') {
      graph.removeNode('empty-dataset')
    }
    dataGraphStore.init()
    graph.cleanSelection()
    dataGraphStore.cleanTargetNodeList()
    dataGraphStore.graphActionMode = ''
    saveGraph()
  }

  /**
   * 复制节点
   */
  async function onCopyNode() {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    if (dataGraphStore.targetNodeList.length === 0) {
      return
    }
    graph.cleanSelection()
    graph.copy(dataGraphStore.targetNodeList)
    const cells = graph.paste({
      offset: 32
    })
    // 复制后的节点，id会变，port id不会变
    console.log('paste', cells)
    // 复制出来的节点、边，都是随机id，relation用来记录随机id和接口生成的id之间的映射关系
    const relation: any = {}
    cells.forEach((_cell: any) => {
      relation[_cell.id] = ''
      if (_cell.shape === 'edge') {
        relation[_cell.id] = {
          source: _cell.source,
          target: _cell.target
        }
      }
    })
    const newCellList = []
    for (let i = 0; i < cells.length; i++) {
      const { shape } = cells[i]
      const _cell = cells[i]
      const _id = cells[i].id
      if (shape !== 'edge') {
        const data = _cell.getData()
        const { taskUuid, taskId } = await upsertTaskNode({ type: data.type, name: data.name, dataset: data.dataset })
        const dataset = {
          ...data.dataset,
          taskUuid,
          taskId
        }
        _cell.setData({
          ...data,
          id: taskUuid,
          dataset,
          status: '' // 清空状态
        })
        const _newCell = graph.updateCellId(_cell, taskUuid)
        newCellList.push(_newCell)
        nodeAddedEvent(_newCell)
        relation[_id] = taskUuid
      } else {
        const { id, labels } = _cell
        // 构造边的起点、终点
        const _source = {
          cell: relation[relation[id].source.cell],
          port: relation[id].source.port
        }
        const _target = {
          cell: relation[relation[id].target.cell],
          port: relation[id].target.port
        }
        // 构造边id
        const _id = `${_source.cell}${_source.port ? '##' : ''}${_source.port || ''}##${_target.cell}##${_target.port ? '##' : ''}${_target.port || ''}`
        // 移除复制的edge
        graph.removeEdge(id)
        // 新增edge
        const edge = graph.addEdge({
          id: _id,
          labels,
          source: _source,
          target: _target,
          attrs: {
            line: EDGE_LINE_STYLES.LINE,
            text: EDGE_LINE_STYLES.TEXT
          }
          // connector: { name: 'rounded' }
        })
        edgeAddedEvent(edge)
      }
    }
    await saveGraph()
    graph.resetSelection(newCellList)
    dataGraphStore.setTargetNodeList([...newCellList])
  }

  /**
   * 新增节点事件
   * @param _node 画布上的节点
   */
  function nodeAddedEvent(_node: any) {
    const data = _node.getData()
    const position = _node.position()
    dataGraphStore.addNode({
      ...data,
      position
    })
    dataGraphStore.addDetail({
      ...data.dataset,
      taskId: data.taskId,
      taskUuid: data.id
    })
  }
  /**
   * 新增边事件
   * @param _edge
   */
  function edgeAddedEvent(_edge: any) {
    const { id, labels, source, target } = _edge
    dataGraphStore.addEdge({
      id,
      name: labels.length > 0 ? labels[0].attrs.label.text : '',
      source,
      target
    })
  }

  /**
   * 节点定位
   */
  function locateNode({ type, id }: { type: string; id: string | number }) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    if (type === NODE_TYPES.DATA || type === NODE_TYPES.ALGO) {
      const cell = graph.getCellById(id)
      // 清空选择区并选中指定cell
      graph.resetSelection(cell)
      dataGraphStore.setTargetNodeList([cell])
      dataGraphStore.graphActionMode = 'locate'
    }
  }

  /**
   * 添加空节点
   */
  async function addEmptyNode(type: string) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    const { x, y } = computedPosition()
    if (type === NODE_TYPES.DATA) {
      const emptyNode = graph.getCellById('empty-dataset')
      if (!emptyNode) {
        graph.addNode({
          id: 'empty-dataset',
          shape: 'empty-node',
          x,
          y,
          data: {
            type: 'empty-dataset' // 右键删除菜单
          }
        })
      }
    } else if (type === NODE_TYPES.SQL) {
      const id = await upsertTaskNode({
        name: '空sql节点',
        type: NODE_TYPES.SQL,
        dataset: {
          sql: ''
        }
      })
      const newNode = graph.addNode({
        id,
        shape: 'default-node',
        x,
        y,
        data: {
          id,
          name: '空sql节点',
          type: NODE_TYPES.SQL,
          dataset: {
            sql: ''
          },
          status: ''
        }
      })
      nodeAddedEvent(newNode)
      saveGraph()
    }
  }

  /**
   * 计算拖拽或者点击添加的新节点放置在画布的什么位置上
   */
  function computedPosition() {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    // 空画布直接放置在最左上方
    if (dataGraphStore.rawData.nodes?.length === 0) {
      return {
        x: 60,
        y: 40
      }
    }
    // 在已有pipeline左下侧添加
    const maxHeight = graph.container.clientHeight - 60
    // Returns the bounding box that surrounds all cells in the graph
    const { left, bottom } = graph.getAllCellsBBox()
    return {
      x: left < 0 ? 0 : left,
      y: bottom + 4 < maxHeight ? bottom + 4 : maxHeight
    }
  }

  /**
   * sql面板保存
   * @param _node
   */
  async function onSqlSave(_node: any) {
    const { id, taskId, type, name, dataset } = _node
    const { taskUuid } = await upsertTaskNode({
      name,
      type,
      id,
      dataset: {
        sql: dataset.sql
      }
    })
    if (taskUuid) {
      ElMessage.success('保存成功！')
    }
    // 更新detail_list中的值
    dataGraphStore.updateDetail(id, {
      sql: dataset.sql
    })
    // 更新rawData中的值
    dataGraphStore.updateNode(id, {
      dataset: {
        sql: dataset.sql,
        taskId,
        taskUuid: id
      }
    })
    // 更新画布中节点数据
    updateNodeData(taskUuid, {
      dataset: {
        sql: dataset.sql
      }
    })
  }

  /**
   * 更新画布上某一节点的数据
   * @returns
   */
  function updateNodeData(id: string, newData: any) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    // 更新节点
    const cell = graph.getCellById(id)
    const data = cell.getData()
    const _data = {
      ...data,
      ...newData
    }
    cell.setData(_data, { overwrite: true })
  }

  /**
   * 已有数据节点更换数据
   */
  async function changeDataIntoExistDatasetNode(_nodeData: any, replaced: any) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    const id = dataGraphStore.targetNodeList[0].getData().id
    const { name, directory, path, type } = _nodeData
    const cell = graph.getCellById(id)
    const data = cell.getData()
    const _value = {
      ...data,
      name: replaced ? name : data.name,
      dataset: {
        name,
        path,
        directory,
        type
      }
    }
    cell.setData(_value, { overwrite: true })
    // nodeData.value = _value
    await upsertTaskNode(_value)
    dataGraphStore.updateNode(id, _value)
    saveGraph()
  }

  /**
   * 从空数据节点选择数据
   */
  async function setDataIntoEmptyDatasetNode(_nodeData: any) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    const id = await upsertTaskNode(_nodeData)
    const { name, directory, path } = _nodeData
    const _node = {
      id,
      name,
      type: NODE_TYPES.DATA,
      dataset: {
        directory,
        path,
        type: getFileType(directory, name),
        name
      },
      status: '',
      position: {
        x: 200,
        y: 100
      }
    }
    const newNode = graph.addNode({
      id,
      shape: 'default-node',
      position: _node.position,
      data: _node
    })
    // 移除empty-node
    graph.removeNode('empty-dataset')
    nodeAddedEvent(newNode)
    saveGraph()
  }

  /**
   * 设置入参
   */
  async function setInputParamsOnAlgoNode({ cellId, inputList }: { cellId: string | number; inputList: any }) {
    console.log('setInputParamsOnAlgoNode:', cellId, inputList)
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    // 在dataGraphStore.rawData.edges中去掉对应的name，即边的label
    dataGraphStore.rawData.edges.forEach((_edge: any) => {
      if (_edge.target.cell === cellId) {
        _edge.name = ''
      }
    })
    // 先移除所有相关边的label（边的个数多于入参个数的时候，有可能存在更换入参时，两条边都显示同样的入参名称）
    const edges = dataGraphStore.rawData.edges.filter((_edge: any) => _edge.target.cell === cellId)
    edges.forEach((_edge: any) => {
      const edge = graph.getCellById(_edge.id)
      edge.removeLabelAt(0)
    })
    // 更新画布
    inputList.forEach(({ name, value }: { name: string; value: string | number }) => {
      const edge = graph.getCellById(value)
      dataGraphStore.updateEdge(value, {
        name
      })
      edge.setLabels([
        {
          attrs: {
            label: {
              text: name
            },
            line: EDGE_LINE_STYLES.LINE,
            text: EDGE_LINE_STYLES.TEXT
          }
        }
      ])
    })
    await saveGraph()
  }

  /**
   * 更新算子节点输出
   */
  function outputUpdate({ id, outputList }: { id: string | number; outputList: any }) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    const _cell = graph.getCellById(id)
    const algo = _cell.getData()
    const data = {
      ...algo,
      dataset: {
        ...algo.dataset,
        outputList
      }
    }
    dataGraphStore.updateNode(id, data)
    _cell.setData(data, { overwrite: true })
    dataGraphStore.cleanTargetNodeList()
    saveGraph()
  }

  /**
   * 已有算子节点更换算子
   */
  async function changeAlgoIntoExistAlgoNode(_nodeData: any) {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    const id = dataGraphStore.targetNodeList[0].getData().id
    const { algorithm, directory, inputParamTemplate, outputParamTemplate, taskId, taskUuid } = _nodeData
    const cell = graph.getCellById(id)
    const data = cell.getData()
    const _value = {
      ...data,
      dataset: {
        algorithm,
        directory,
        inputParamTemplate,
        outputParamTemplate,
        taskId,
        taskUuid
      }
    }
    cell.setData(_value, { overwrite: true })
    await upsertTaskNode(_value)
    dataGraphStore.updateNode(id, _value)
    // 更换算子之后，移除掉该算子节点前后的连线
    dataGraphStore.rawData.edges.forEach((_edge: any) => {
      if (_edge.source.cell === id || _edge.target.cell === id) {
        graph.removeEdge(_edge.id)
      }
    })
    dataGraphStore.removeEdgesOfNode(id)
    saveGraph()
  }

  /**
   * 清空画布
   */
  function clearGraph() {
    const graph: any = dataGraphStore.graph
    if (!graph) {
      return
    }
    graph.clearCells()
  }

  return {
    rename,
    deleteFromGraph,
    onCopyNode,
    locateNode,
    addEmptyNode,
    onSqlSave,
    changeDataIntoExistDatasetNode,
    setDataIntoEmptyDatasetNode,
    setInputParamsOnAlgoNode,
    outputUpdate,
    nodeAddedEvent,
    clearGraph,
    changeAlgoIntoExistAlgoNode
  }
}
