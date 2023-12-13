export const PORT_CONFIG = {
  groups: {
    nodePort: {
      // 节点通用的port形式
      position: {
        name: 'absolute'
      },
      attrs: {
        circle: {
          r: 4,
          magnet: true,
          stroke: '#6973FF',
          strokeOpacity: '0',
          fill: '#6973FF',
          strokeWidth: 8,
          paintOrder: 'stroke',
          visibility: 'hidden'
        }
      }
    }
  }
}
/**
 * 节点类型
 */
export const NODE_TYPES = {
  DATA: 'data',
  ALGO: 'algorithm',
  SQL: 'sql'
}

/**
 * 节点类型中文名称
 */
export const NODE_TYPES_ZHCN = {
  [NODE_TYPES.DATA]: '数据节点',
  [NODE_TYPES.ALGO]: '算子节点',
  [NODE_TYPES.SQL]: 'SQL节点'
}

/**
 * 连线样式
 */
export const EDGE_LINE_STYLES: any = {
  LINE: {
    strokeWidth: 1,
    stroke: '#B6BDD2'
  },
  TEXT: {
    fill: '#B6BDD2',
    fontSize: '12px'
  }
}
