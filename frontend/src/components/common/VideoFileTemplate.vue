<template>
  <div class="preview-video">
    <video ref="videoPlayer" class="video-js"></video>
  </div>
</template>
<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import useVideoPreview from '@/components/hooks/useVideoPreview'

const props = defineProps({
  path: { type: String, default: '' },
  pictureInPictureToggle: { type: Boolean, default: true }
})

const { videoPreviewContent, videoPreview } = useVideoPreview()

const videoPlayer: any = ref(null)
const myPlayer: any = ref(null)

// 更换数据源
watch(
  () => props.path,
  () => {
    videoPreview(props.path)
    myPlayer.value.src({
      src: videoPreviewContent.value,
      type: 'video/mp4'
    })
  }
)

onMounted(() => {
  videoPreview(props.path)
  init()
})

function init() {
  myPlayer.value = videojs(
    videoPlayer.value,
    {
      // poster: "//vjs.zencdn.net/v/oceans.png",
      controls: true,
      sources: [
        {
          src: videoPreviewContent.value,
          type: 'video/mp4'
        }
      ],
      controlBar: {
        remainingTimeDisplay: {
          displayNegative: false
        },
        volumePanel: {
          inline: false
        },
        pictureInPictureToggle: props.pictureInPictureToggle
      },
      playbackRates: [0.5, 1, 1.5, 2]
    },
    () => {
      // myPlayer.value.log("play.....")
    }
  )
}

onUnmounted(() => {
  if (myPlayer.value) {
    myPlayer.value.dispose()
  }
})
</script>
<style lang="less">
.preview-video {
  width: 100%;
  height: 100%;
  .video-js {
    margin: auto auto;
    width: 100%;
    height: 100%;
  }
}
</style>
