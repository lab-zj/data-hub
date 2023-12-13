/**
 * el-select下拉框，滚动加载
 * element-plus中，需要设置下拉框不加载到body中，否则读取不到dom
 * 设置下拉框的高度、overflow属性为auto
 */
const loadMore = {
  install(app: any) {
    app.directive('loadmore', (el: any, binding: any) => {
      const select_dom = el.querySelector('.el-select-dropdown .el-select-dropdown__wrap')
      select_dom.addEventListener('scroll', () => {
        const condition = select_dom.clientHeight >= select_dom.scrollHeight - select_dom.scrollTop - 1
        if (condition) {
          binding.value()
        }
      })
    })
  }
}

export default loadMore
