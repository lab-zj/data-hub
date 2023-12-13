/**
 * 拖动改变宽度指令
 * <div v-dragWidth="{ dragSide: 'left', minWidth: 390, maxWidth: dragMaxWidth }">
 *  <div class="resize-line"></div>
 * </div>
 */
const dragWidth = {
  install(app: any) {
    app.directive('dragWidth', (el: any, binding: any) => {
      const dragDom = el
      const { dragSide, minWidth, maxWidth } = binding.value
      const resizeLineDom = el.querySelector('.resize-line')
      resizeLineDom.style.cursor = 'e-resize'
      resizeLineDom.onmousedown = (e: any) => {
        const disX = e.clientX
        const width = dragDom.clientWidth
        const minW = minWidth || 120
        const maxW = maxWidth || document.body.clientWidth
        let panelWidth
        document.onmousemove = function (e) {
          // 通过事件委托，计算移动的距离
          const l = dragSide === 'left' ? disX - e.clientX : e.clientX - disX
          // 改变当前元素宽度，不可超过最小最大值
          panelWidth = width + l
          panelWidth = panelWidth < minW ? minW : panelWidth
          panelWidth = panelWidth > maxW ? maxW : panelWidth
          dragDom.style.width = `${panelWidth}px`
        }

        document.onmouseup = function () {
          document.onmousemove = null
          document.onmouseup = null
        }
      }
    })
  }
}

export default dragWidth
