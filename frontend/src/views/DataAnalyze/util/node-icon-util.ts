/**
 *  pipeline 节点 icon 及相关背景色
 * @author
 */

interface KeyValue {
  [key: string]: any
}

// 算子列表 图标  graph 画布算子节点图标
export const AlgTypeNodeIconEnum: KeyValue = {
  1: { icon: 'icon-juleiDBSCAN', color: '#aed495', opacity: 1 }, // DBSCAN
  2: { icon: 'icon-juleiK-Means', color: '#aed495', opacity: 1 }, // KMEANS
  3: { icon: 'icon-xianxinghuigui1', color: '#f5a4ac', opacity: 1 }, // 线性回归
  4: { icon: 'icon-jiangweipac', color: '#f5a4ac', opacity: 1 }, //  降维 PCA
  5: { icon: 'icon-T-SNE', color: '#f5a4ac', opacity: 1 }, // 降维 TSNE
  6: { icon: 'icon-LLE3beifen', color: '#f5a4ac', opacity: 1 }, // 降维 LLE
  7: { icon: 'icon-stat-anomaly2beifen3', color: '#fbaf85', opacity: 1 }, // 异常检测 LT
  8: { icon: 'icon-iForest2', color: '#fbaf85', opacity: 1 }, // 独立森林
  9: { icon: 'icon-luojihuigui1', color: '#f5a4ac', opacity: 1 }, // LOGREGRE
  10: { icon: 'icon-pinfanFP', color: '#aed495', opacity: 1 }, // 频繁挖掘 FP_GROWTH
  11: { icon: 'icon-PrefixSpan', color: '#aed495', opacity: 1 } // 频繁挖掘 PREFIX_SPAN
}
// ETL 节点图标
export const ETLAlgTypeNodeIconEnum: KeyValue = {
  1: { icon: 'icon-Filterbeifen1', color: '#aed495', opacity: 1 }, // FILTER
  2: { icon: 'icon-interJion', color: '#aed495', opacity: 1 }, // JOIN
  3: { icon: 'icon-Sample', color: '#f5a4ac', opacity: 1 }, // SAMPLE
  4: { icon: 'icon-Union', color: '#f5a4ac', opacity: 1 }, //  UNION
  6: { icon: 'icon-sql2', color: '#f5a4ac', opacity: 1 }, // SQL
  7: { icon: 'icon-shujutoushi', color: '#f5a4ac', opacity: 1 } // PIVOT TABLE
}
// JOIN 节点图标
export const JOINModeIconEnum: KeyValue = {
  1: { icon: 'icon-Jionbeifen-copy', color: '#aed495', opacity: 1 }, // LEFT JOIN
  2: { icon: 'icon-Jionbeifen', color: '#aed495', opacity: 1 }, // RIGHT JOIN
  3: { icon: 'icon-interJion', color: '#aed495', opacity: 1 }, // INNER JOIN
  4: { icon: 'icon-Jionbeifen3', color: '#aed495', opacity: 1 }, // FULL OUTER JOIN
  5: { icon: 'icona-Jionbeifen8', color: '#aed495', opacity: 1 }, // LEFT JOIN EXCLUDING INNER JOIN
  6: { icon: 'icona-Jionbeifen4', color: '#aed495', opacity: 1 }, // RIGHT JOIN EXCLUDING INNER JOIN
  7: { icon: 'icona-Jionbeifen5', color: '#aed495', opacity: 1 } // FULL OUTER JOIN EXCLUDING INNER JOIN
}

export const EconomicOperatorIconEnum: KeyValue = {
  1: { icon: 'icon-tongjiyichangjiance', color: '#aed495', opacity: 1 }, // 异常值检测-统计
  2: { icon: 'icon-jinlinfenxi', color: '#aed495', opacity: 1 }, // 异常值检测-近邻
  3: { icon: 'icon-tongyongchabu', color: '#aed495', opacity: 1 }, // 缺失值插补-统计
  4: { icon: 'icon-duozhongchabu', color: '#aed495', opacity: 1 }, // 缺失值插补-多重
  5: { icon: 'icon-biaozhunhua', color: '#aed495', opacity: 1 }, // 标准化
  6: { icon: 'icon-shijianxuliefenjie', color: '#aed495', opacity: 1 }, // 季节性调整
  7: { icon: 'icon-shujupinghua', color: '#aed495', opacity: 1 }, // 数据平滑
  8: { icon: 'icon-monituiyan', color: '#aed495', opacity: 1 }, // 模拟推演
  9: { icon: 'icon-monituiyan', color: '#aed495', opacity: 1 }, // 模拟推演-多参数
  10: { icon: 'icon-shijianxulieweiyi', color: '#aed495', opacity: 1 }, // 时间序列位移
  11: { icon: 'icon-fanxiangtuiyan', color: '#aed495', opacity: 1 }, // 反向模拟推演
  12: { icon: 'icon-monituiyan', color: '#aed495', opacity: 1 }, // 模拟推演-改进
  13: { icon: 'icon-juleiDBSCAN', color: '#aed495', opacity: 1 }, // dbscan
  14: { icon: 'icon-juleiK-Means', color: '#aed495', opacity: 1 }, // kmeans
  15: { icon: 'icon-jiangweipac', color: '#aed495', opacity: 1 }, // pca
  16: { icon: 'icon-T-SNE', color: '#aed495', opacity: 1 }, // tsne
  17: { icon: 'icon-juleiK-Means', color: '#aed495', opacity: 1 }, // new_kmeans
  18: { icon: 'icon-juleiDBSCAN', color: '#aed495', opacity: 1 }, // new_dbscan
  19: { icon: 'icon-jiangweipac', color: '#aed495', opacity: 1 }, // new_pca
  20: { icon: 'icon-T-SNE', color: '#aed495', opacity: 1 }, // new_tsne
  21: { icon: 'icon-jiangweipac', color: '#f5a4ac', opacity: 1 }, // new_lle
  22: { icon: 'icon-tongjiyichangjiance', color: '#aed495', opacity: 1 }, // new_iso_forest
  23: { icon: 'icon-pinfanFP', color: '#aed495', opacity: 1 }, // new_fp_growth
  24: { icon: 'icon-pinfanFP', color: '#aed495', opacity: 1 }, // new_prefix_span
  25: { icon: 'icon-fenlei1', color: '#aed495', opacity: 1 }, // new_arima
  26: { icon: 'icon-shijianxulieweiyi', color: '#aed495', opacity: 1 }, // new_holt_winters
  27: { icon: 'icon-shijianxulieweiyi', color: '#aed495', opacity: 1 }, // new_shift
  28: { icon: 'icon-shijianxulieweiyi', color: '#aed495', opacity: 1 } // new_adf
}

// 数据 图标
export const DataIcon = {
  icon: 'icon-shujubeifen',
  color: '#a4abfb',
  opacity: 1
}
// ETL 图标
export const EtlIcon = { icon: 'icon-ETL', color: '#74a0fa', opacity: 0.8 }
// 清洗节点 icon
export const ClearNodeIcon = {
  icon: 'icon-qingxibeifen2',
  color: '#74a0fa',
  opacity: 0.8
}
// 模型 icon
export const ModelIcon = {
  icon: 'icon-moxing',
  color: '#9AA2FF',
  opacity: 1
}
// 自定义 icon
export const SelfDefinedIcon = {
  icon: 'icon-zidingyibeifen',
  color: '#a2dced',
  opacity: 1
}

/**
 * 新增的算法、模型
 */
export const customizeIcon: KeyValue = {
  1: { icon: 'icon-juleiDBSCAN', color: '#aed495', opacity: 1 }, // 聚类
  2: { icon: 'icon-jiangweipac', color: '#aed495', opacity: 1 }, // 降维
  3: { icon: 'icon-tongjiyichangjiance', color: '#aed495', opacity: 1 }, // 异常检测
  4: { icon: 'icon-pinfanFP', color: '#aed495', opacity: 1 }, // 频繁
  5: { icon: 'icon-xianxinghuigui1', color: '#aed495', opacity: 1 }, // 回归
  6: { icon: 'icon-fenlei1', color: '#aed495', opacity: 1 }, // 分类
  7: { icon: 'icon-biaozhunhua', color: '#aed495', opacity: 1 }, // 特征
  8: { icon: 'icon-shijianxulieweiyi', color: '#aed495', opacity: 1 }, // 时间
  9: { icon: 'icon-suanfa1', color: '#aed495', opacity: 1 }, // 其他
  10: { icon: 'icon-jingjilingyu', color: '#aed495', opacity: 1 } // 经济领域
}

/**
 * 根据 type algType 返回节点图标相关信息
 * @param type 节点类型：1-系统算子节点，2-ETL节点, 3-数据节点，4-自定义算子节点，5-清新节点， 6-图网络节点， 7-模型节点， 8-py算子节点
 * @param algType
 */
export function nodeIconWithBackGround(type: number, algType: number) {
  let icon: KeyValue
  switch (type) {
    case 0:
      // pipeline join 节点的不同 join 类型 《组件 NodeCapsule 使用 nodeIconWithBackGround(0, mod)》
      icon = JOINModeIconEnum[Number(algType)]
      break
    case 1:
      icon = AlgTypeNodeIconEnum[Number(algType)]
      break
    case 2:
      icon = ETLAlgTypeNodeIconEnum[Number(algType)]
      break
    case 3:
      // eslint-disable-next-line prefer-destructuring
      icon = DataIcon
      break
    case 4:
      icon = SelfDefinedIcon
      break
    case 5:
      icon = ClearNodeIcon
      break
    case 7:
      icon = ModelIcon
      break
    case 8:
      icon = EconomicOperatorIconEnum[Number(algType)] || SelfDefinedIcon
      break
    case 9:
      icon = SelfDefinedIcon
      break
    case 10:
      icon = customizeIcon[Number(algType)] || SelfDefinedIcon
      break
    default:
      // 通用的 算子图标 （用自定义算子图标）
      icon = SelfDefinedIcon
      break
  }
  return icon
}

/**
 * 新版 节点图标
 */
export const newIcon: KeyValue = {
  3: 'icon-shujubeifen', // 数据节点
  4: 'icon-zidingyibeifen', // 自定义
  5: 'icon-Filterbeifen1', //  清洗节点
  7: 'icon-zidingyi', // 自定义
  8: 'icon-qingxi-copy' // 清洗
}

// 算子列表分组图标
export const OperatorGroupIconEnum: KeyValue = {
  0: 'icon-ETL', // ETL 操作
  1: 'icon-julei', // 聚类
  2: 'icon-huigui', // 回归
  3: 'icon-jiangwei', // 降维
  4: 'icon-yichangjiance', // 异常检测
  5: 'icon-pinfanmoshiwajue', //  频繁模式挖掘
  6: 'icon-shoucang', // 收藏
  7: 'icon-zidingyi', // 自定义
  8: 'icon-qingxi-copy' // 清洗
}

// 算子列表分组图标 背景色
export const OperatorGroupIconBackGround: KeyValue = {
  0: { color: '#74a0fa', opacity: 0.8 }, // ETL 操作
  1: { color: '#aed495', opacity: 1 }, // 聚类
  2: { color: '#f5a4ac', opacity: 1 }, // 回归
  3: { color: '#f5a4ac', opacity: 1 }, // 降维
  4: { color: '#fbaf85', opacity: 1 }, // 异常检测
  5: { color: '#aed495', opacity: 1 }, //  频繁模式挖掘
  7: { color: '#a2dced', opacity: 1 }, // 自定义
  8: { color: '#74a0fa', opacity: 0.8 } // 清洗
}
