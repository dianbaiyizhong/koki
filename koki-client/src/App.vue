<template>
  <v-app>
    <v-app-bar>
      <v-container class="d-flex align-center py-0">
        <v-app-bar-title class="pl-0">
          <div class="d-flex align-center">
            <v-avatar rounded="1" class="mr-3" image="./koki.webp" />

            柯基（柯举答题基）
          </div>
        </v-app-bar-title>
      </v-container>
    </v-app-bar>

    <v-main>
      <section id="hero">
        <v-sheet class="d-flex align-center pb-16" color="grey-darken-3">
          <v-container class="text-center">
            <v-responsive class="mx-auto" width="500">
              <v-img src="./koki.webp" height="400" />

              <v-btn class="mt-6" @click="share" rel="noopener noreferrer">
                打开屏幕分享
              </v-btn>

              <p class="mt-4 text-medium-emphasis">
                此软件仅个人使用，不作推广
              </p>
            </v-responsive>
          </v-container>
        </v-sheet>
      </section>




    </v-main>

    <v-dialog v-model="dialog" width="100%" color="grey-lighten-4">
      <v-card>
        <v-toolbar>
          <v-toolbar-title>屏幕共享中</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-toolbar-items>
            <v-btn variant="text" @click="dialog = false">
              关闭
            </v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-container>
          <v-row>
            <v-col cols="10">
              <video ref="videoPlayer" class="video-js"></video>

            </v-col>
            <v-col cols="2">
              <v-list lines="three">
                <v-list-item v-for="item in answerItems" :key="item.id" :title="item.question"
                  :subtitle="item.answer"></v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-container>



        <v-card-actions>
          <v-btn color="primary" block @click="dialog = false">Close Dialog</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>



    <v-footer>
      <v-container class="text-overline d-flex align-center justify-space-between">
        <div>Copyright &copy; 2023 nntk</div>

        <v-icon icon="mdi-vuetify" size="x-large" />
      </v-container>
    </v-footer>
  </v-app>
</template>

<script>
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import axios from 'axios'

import { createWorker, PSM } from 'tesseract.js'
let worker = null
let that = null
let img = null
export default {
  name: "App",
  data() {
    return {
      answerItems: [
        {
          question: 'Item #1',
          id: 1,
        },
        {
          question: 'Item #2',
          id: 2,
        },
        {
          question: 'Item #3',
          id: 3,
        },
      ],
      dialog: false,
      player: null,
      videoOptions: {
        width: 'auto',
        autoplay: false,
        controls: true,
      },
      ocrRangePoint: {
        left: 767,
        top: 370,
        width: 376,
        height: 100,
      },
    }
  },
  methods: {
    share() {
      navigator.mediaDevices.getDisplayMedia({ video: true }).then(this.gotDisplayStream, function (error) { })
    },
    // 拿到屏幕数据流
    gotDisplayStream(stream) {
      window.stream = stream

      that.dialog = true

      setTimeout(function () {
        that.initPlayer()
        const videoDom = that.player.tech().el()
        videoDom && (videoDom.srcObject = stream)
        that.player.play = () => {
          videoDom.play()
        }
        that.player.play()
        stream.getVideoTracks()[0].addEventListener('ended', () => { })
        that.startTimer()
      }, 100)

    },
    startTimer() {
      let timer = setInterval(() => {
        that.getFrame()
      }, 2000)
    },

    grey(cnx, width, height) {
      var imgPixels = cnx.getImageData(0, 0, width, height);

      for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
          var i = (y * 4) * width + x * 4;
          var avg = (imgPixels.data[i] + imgPixels.data[i + 1] + imgPixels.data[i + 2]) / 3;
          imgPixels.data[i] = avg;
          imgPixels.data[i + 1] = avg;
          imgPixels.data[i + 2] = avg;
        }
      }
      cnx.putImageData(imgPixels, 0, 0, 0, 0, imgPixels.width, imgPixels.height);
    },
    getFrame() {
      let canvas = document.createElement('canvas')
      var ctx = canvas.getContext('2d')
      // canvas.width = that.player.videoWidth()
      // canvas.height = that.player.videoHeight()
      let rectangle = JSON.parse(JSON.stringify(that.ocrRangePoint))

      canvas.width = rectangle.width
      canvas.height = rectangle.height


      // ctx.drawImage(that.player.children_[0], 0, 0, canvas.width, canvas.height)
      // ctx.drawImage(that.player.children_[0], rectangle.left, rectangle.top, rectangle.width, rectangle.height)
      ctx.drawImage(that.player.children_[0], rectangle.left, rectangle.top, rectangle.width, rectangle.height, 0, 0, rectangle.width, rectangle.height);

      that.grey(ctx, rectangle.width, rectangle.height)
      let blobUrl = canvas.toDataURL()

      // var blob = that.dataURLtoBlob(blobUrl);
      // var file = that.blobToFile(blob, "img")


      // that.uploadFile(file)
      this.ocrGo(canvas)
    },

    uploadFile(file) {
      let fd = new FormData()
      fd.append("file", file)

      axios
        .post('http://127.0.0.1:8088/koki/answer/file', fd)
        .then((res) => {
          console.log(res.data)
        })
    },
    ocrGo: async (img) => {
      let rectangle = JSON.parse(JSON.stringify(that.ocrRangePoint))
      // const {
      //   data: { text },
      // } = await worker.recognize(img, { rectangle: rectangle })
      const {
        data: { text },
      } = await worker.recognize(img)

      console.info(text)

      axios
        .post('http://127.0.0.1:8088/koki/answer', {
          text: text,
        })
        .then((res) => {
          console.log(res.data)
          that.answerItems = res.data
        })
    },

    blobToFile(theBlob, fileName) {
      theBlob.lastModifiedDate = new Date();
      theBlob.name = fileName;
      return theBlob;
    },
    dataURLtoBlob(dataurl) {
      var arr = dataurl.split(','),
        mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]),
        n = bstr.length,
        u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      return new Blob([u8arr], { type: mime });
    },

    initPlayer() {
      if (that.player == null) {
        that.player = videojs(that.$refs.videoPlayer, that.videoOptions, function onPlayerReady() { })
        that.player.on('click', function (event) {
          event.preventDefault()
          that.ocrRangePoint.left = event.offsetX
          that.ocrRangePoint.top = event.offsetY
          console.info('========================' + that.ocrRangePoint.left + "===" + that.ocrRangePoint.top)

          console.info(that.ocrRangePoint)
        })
      }
    },

    initOcr: async () => {
      if (worker == null) {
        worker = await createWorker({
          langPath: '../lang-data',
          workerPath: '../ocr/worker.min.js',
          corePath: '../ocr/tesseract-core-simd.wasm.js',
          logger: (m) => {
            // console.log(m)
          },
        })
        await worker.loadLanguage('chi_sim')
        await worker.initialize('chi_sim')
        await worker.setParameters({
          tessedit_pageseg_mode: PSM.SINGLE_BLOCK,
        })
      }
    },
  },
  mounted() {
    this.initOcr()


    setTimeout(function () {

      that.ocrGo('../demo2.png')
    }, 3000)

  },
  created() {
    that = this
  },
};
</script>
<style>
.video-js.vjs-playing .vjs-tech {
  pointer-events: none;
}
</style>
