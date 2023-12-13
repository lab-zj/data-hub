// import yaml from "js-yaml";

// 判断代码是否为注释
const isLineComment = (line: string) => {
  const pureLine = line.trim()
  return pureLine.startsWith('#') || pureLine.startsWith('---')
}

// 解析注释
const parseComment = (template: string) => {
  // 保存结果，其中添加了layer属性，表示层级关系
  const result: any[] = [
    {
      type: 'object',
      properties: {},
      layer: -1
    }
  ]

  // 找到父节点
  const findParent = (layer: number) => {
    // 找到最近的layer小于自己的
    for (let i = result.length - 1; i >= 0; i -= 1) {
      if (result[i].layer === layer - 1) {
        return result[i]
      }
    }
    return {}
  }

  const lines = template.replaceAll('"', '').split('\n')

  // 前面一行是否为注释
  let preComment = ''

  lines.forEach((line: string) => {
    const isComment = isLineComment(line)

    // 如果是注释行
    if (isComment) {
      preComment = line.replace('#', '').trim()
    }
  })

  return {}
}

// 解析模板成schema
export const parseToSchema = (template: string) => {
  // 保存结果，其中添加了layer属性，表示层级关系
  const result: any[] = [
    {
      type: 'object',
      properties: {},
      layer: -1
    }
  ]

  // 找到父节点
  const findParent = (layer: number) => {
    // 找到最近的layer小于自己的
    for (let i = result.length - 1; i >= 0; i -= 1) {
      if (result[i].layer === layer - 1) {
        return result[i]
      }
    }
    return {}
  }

  // 判断字段数据类型
  const computeKeyTypeValue = (line: string) => {
    let type = 'string'
    let values = [line, `"${line}"`]

    if (line === 'true' || line === 'false') {
      type = 'boolean'
      values = values.concat([true, false, 'true', 'false'])
    } else if (!isNaN(line)) {
      type = 'number'
      values = values.concat([parseInt(line), parseFloat(line)])
    }
    return [type, values]
  }

  // 以换行符为分割
  const lines = template.replaceAll('"', '').split('\n')

  // 前面一行是否为注释
  let preComment = ''

  // 逐行解析
  lines.forEach((line: string) => {
    const isComment = isLineComment(line)

    // 如果是注释行
    if (isComment) {
      preComment = line.replace('#', '').trim()
    }

    // 如果是代码行
    else {
      const description = preComment
      let codeLine = line.replace(/(")*/, '')

      // 如果不是根节点，我们再解析的时候手动在最前面加两个空格，方便判断层级
      if (codeLine.startsWith(' ')) {
        codeLine = '  ' + codeLine
      }

      // 层级关系，4个空格为1级别
      const layer = Math.ceil((codeLine.length - codeLine.trimStart().length) / 4)

      // console.log(codeLine, layer)

      const [key, ...rest] = codeLine.split(':')
      const value = rest.join(':') // 防止值里面包含冒号，如https://wwwxxx.xxx，要拼接回去

      // 定义新节点
      const node: any = {
        name: key.trim(),
        description,
        type: 'string',
        layer
      }

      // 找到父节点
      const parent = findParent(layer)

      // 如果key是 - 开头，说明是数组元素
      if (key.trim().startsWith('-')) {
        parent.type = 'array'
        parent.items = {
          properties: {},
          type: 'object'
        }
        node.name = key.replace('-', '').trim()
      }

      // 如果值不为空，说明是子节点，将其值保存到值列表中
      if (value !== '') {
        const [type, values] = computeKeyTypeValue(value.trim())
        node.enum = values
        // [value.trim(), `"${value.trim()}"`]
        node.type = type
      }

      // 如果定义了父节点的类型
      if (parent.type === 'object') {
        parent.properties[node.name] = node
      } else if (parent.type === 'array') {
        parent.items.properties[node.name] = node
      } else {
        parent.type = 'object'
        parent.properties = {}
        parent.properties[node.name] = node
      }

      result.push(node)
    }
  })

  return result[0]
}

// 删除代码中的注释
export const sliceComment = (template: string) => {
  return template
    .split('\n')
    .filter((line) => {
      return !isLineComment(line)
    })
    .join('\n')
}
