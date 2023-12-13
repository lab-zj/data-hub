/**
 * 解析publicPath路径
 * 由于之前代码里基本都是 /icon/xxxx 的形式，所以返回有两种情况
 * 1、publicPath 是 '/'，返回是''（空字符串）避免出现 //icon/xxx 的情况
 * 2、publictPath 是 '/yyy/' 或者 '/yyy'，返回的是 '/yyy'，不带最后的斜杠
 *
 */
function parsePublicPath() {
  let baseUrl = process.env.BASE_URL || ''
  if (baseUrl === '/') {
    baseUrl = ''
  } else {
    baseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  }
  return baseUrl
}

export const publicPath = parsePublicPath()

export default {}
