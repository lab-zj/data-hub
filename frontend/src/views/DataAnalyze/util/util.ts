/**
 * 输入/输出模板转换list
 * @param _template
 */
export function convertParams(_template: any) {
  if (!_template) {
    return undefined
  }
  const _parse = JSON.parse(_template)
  const keys = Object.keys(_parse)
  return keys.map((key: string) => {
    return {
      name: key
    }
  })
}
