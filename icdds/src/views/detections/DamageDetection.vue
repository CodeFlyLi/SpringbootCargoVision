<template>
  <!--
    DamageDetection.vue - 货物破损检测核心页面
    
    该页面是系统的核心功能模块，负责处理货物图片的上传、摄像头拍摄以及调用后端 AI 接口进行破损检测。
    
    主要业务流程：
    1. 图片采集：
       - 支持本地文件拖拽/点击上传（支持多选）
       - 支持调用设备摄像头实时拍照
       - 前端进行文件格式（JPG/PNG等）、大小（12MB限制）及数量（20张限制）校验
    2. 图片预览与管理：
       - 使用 Blob URL 技术实现零延迟的本地预览
       - 支持对上传列表中的图片进行查看、删除操作
    3. 参数配置：
       - 必须选择关联的运输单（Transport），以便将检测记录归档
       - 可选填检测地点和责任主体信息
    4. 智能检测：
       - 将图片列表和配置参数打包发送给后端 /api/v1/detections/detect/batch 接口
       - 处理后端返回的检测结果（破损等级、类型、置信度等）
    5. 结果可视化：
       - 动态展示检测到的破损区域（如果后端返回处理后的图片）
       - 使用不同颜色的标签（Tag）区分破损等级（正常、轻微、严重等）
  -->
  <div class="detection-page-container">
    <el-card class="detection-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <div class="search-bar-container">
        <!-- PC 搜索栏/配置栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 开始检测按钮 - 需要新增检测权限 -->
          <el-button
            v-if="canAddDetect"
            type="primary"
            @click="startBatchDetection"
            :disabled="fileList.length === 0 || isDetecting"
            :icon="isDetecting ? Loading : VideoCamera"
          >
            <span>{{ isDetecting ? '正在检测...' : '开始检测' }}</span>
          </el-button>

          <!-- 清空列表按钮 - 需要清空列表权限 -->
          <el-button
            v-if="canClearList"
            type="danger"
            :icon="Delete"
            @click="clearAll"
            :disabled="fileList.length === 0 || isDetecting"
          >
            <span>清空列表</span>
          </el-button>

          <!-- 摄像头拍摄按钮 - 需要摄像头权限 -->
          <el-button v-if="canUseCamera" type="info" :icon="Camera" @click="openCamera">
            <span>摄像头拍摄</span>
          </el-button>

          <el-divider direction="vertical" />

          <!-- 运输单选择下拉框 -->
          <el-select
            v-model="selectedTransportId"
            placeholder="关联运输单"
            filterable
            class="w-260"
            clearable
          >
            <el-option
              v-for="item in transportList"
              :key="item.id"
              :label="`${item.transportNo} - ${item.goodsName}`"
              :value="item.id"
            />
          </el-select>
          <!-- 检测地点输入框 -->
          <el-input v-model="detectionLocation" placeholder="检测地点" class="w-150" clearable />
          <!-- 责任主体输入框 -->
          <el-input
            v-model="responsibilitySubject"
            placeholder="责任主体"
            class="w-150"
            clearable
          />
        </div>

        <!-- 移动端搜索栏/配置栏 -->
        <div v-else class="mobile-search-bar">
          <div class="mobile-top-row">
            <!-- 摄像头拍摄按钮 - 需要摄像头权限 -->
            <el-button
              v-if="canUseCamera"
              type="info"
              :icon="Camera"
              @click="openCamera"
              class="flex-1"
            >
              拍摄
            </el-button>
            <!-- 筛选配置按钮 - 需要配置检测权限 -->
            <el-button
              v-if="canConfigDetect"
              :icon="Filter"
              @click="filterDrawerVisible = true"
              class="flex-1"
            >
              筛选
            </el-button>
          </div>

          <!-- 开始检测按钮 - 需要新增检测权限 -->
          <el-button
            v-if="canAddDetect"
            type="primary"
            @click="startBatchDetection"
            :disabled="fileList.length === 0 || isDetecting"
            :icon="isDetecting ? Loading : VideoCamera"
            class="mobile-full-btn"
          >
            <span>{{ isDetecting ? '检测' : '开始' }}</span>
          </el-button>

          <!-- 清空列表按钮 - 需要清空列表权限 -->
          <el-button
            v-if="canClearList"
            type="danger"
            :icon="Delete"
            @click="clearAll"
            :disabled="fileList.length === 0 || isDetecting"
            class="mobile-full-btn"
          >
            清空
          </el-button>
        </div>
      </div>

      <div class="detection-content">
        <!-- 左侧区域：图片上传与列表 -->
        <div class="upload-section">
          <div class="section-header">
            <h3 class="section-title">上传货物图片</h3>
          </div>
          <!-- 
            Element Plus 上传组件配置说明：
            - drag: 启用拖拽上传区域
            - action="#": 禁用默认上传行为，我们使用自定义的批量上传逻辑
            - auto-upload="false": 选择文件后不立即上传，等待用户点击"开始检测"
            - on-change: 监听文件选择变化，用于本地预览和校验
            - show-file-list="false": 禁用默认文件列表，使用下方自定义的 grid 布局列表
            - multiple: 允许一次选择多个文件
            - accept: 限制文件选择类型
          -->
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="false"
            multiple
            accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
          >
            <el-icon class="el-icon--upload">
              <upload />
            </el-icon>
            <div class="el-upload__text">拖拽文件到此处或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG, PNG, GIF, BMP, WEBP 格式，单张最大 12MB，单次最多上传 20 张
              </div>
            </template>
          </el-upload>

          <!-- 自定义图片预览网格列表 -->
          <div class="image-list" v-if="fileList.length > 0">
            <div
              v-for="(item, index) in fileList"
              :key="item.id"
              class="image-card"
              :class="{ active: currentIndex === index }"
              @click="selectImage(index)"
            >
              <!-- 缩略图：使用 URL.createObjectURL 生成的本地 blob 地址 -->
              <img :src="item.url" alt="预览图" class="list-img" />
              <!-- 状态标签：显示 待检测/检测中/已完成/失败 -->
              <div class="image-status" v-if="item.status">
                <el-tag size="small" :type="getStatusType(item.status)">{{
                  getStatusText(item.status)
                }}</el-tag>
              </div>
              <!-- 删除按钮：仅在鼠标悬停时显示 -->
              <!-- 删除图片按钮 -->
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                class="delete-btn"
                @click.stop="removeImage(index)"
              />
            </div>
          </div>
        </div>

        <!-- 右侧区域：检测结果详情展示 -->
        <div class="detection-section">
          <h3 class="section-title">检测结果详情</h3>
          <!-- 当选中某张图片且有结果时显示 -->
          <div class="detection-result" v-if="currentResult">
            <div class="result-image">
              <!-- 大图展示：优先显示后端处理过（带框）的图片，否则显示原图 -->
              <el-image
                :src="getFullUrl(currentResult.processedUrl || currentResult.originalUrl)"
                :preview-src-list="[
                  getFullUrl(currentResult.processedUrl || currentResult.originalUrl),
                ]"
                fit="contain"
                class="result-img"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <!-- 详细字段展示 -->
            <div class="result-info">
              <div class="result-item">
                <span class="label">破损等级：</span>
                <el-tag :type="getDamageLevelTagType(currentResult.damageLevel)">
                  {{ getDamageLevelText(currentResult.damageLevel) }}
                </el-tag>
                <!-- 如果等级大于0（非正常），显示异常警告 -->
                <span v-if="currentResult.damageLevel > 0" class="abnormal-alert">
                  <el-icon>
                    <warning />
                  </el-icon>
                  发现货物破损
                </span>
              </div>
              <div class="result-item">
                <span class="label">破损类型：</span>
                <span>{{ currentResult.damageType }}</span>
              </div>
              <div class="result-item">
                <span class="label">检测时间：</span>
                <span>{{ currentResult.detectionTime }}</span>
              </div>
              <div class="result-item" v-if="currentResult.detectionLocation">
                <span class="label">检测地点：</span>
                <span>{{ currentResult.detectionLocation }}</span>
              </div>
              <div class="result-item" v-if="currentResult.responsibilitySubject">
                <span class="label">责任主体：</span>
                <span>{{ currentResult.responsibilitySubject }}</span>
              </div>
              <div class="result-item">
                <span class="label">破损描述：</span>
                <p class="description">{{ currentResult.damageDescription }}</p>
              </div>
              <!-- 新增：人工复核/修正按钮 -->
              <div
                class="result-item"
                style="grid-column: span 2; margin-top: 10px; justify-content: center"
              >
                <!-- 人工复核按钮 - 需要编辑检测权限 -->
                <el-button
                  v-if="canEditDetect"
                  type="warning"
                  :icon="Edit"
                  @click="handleManualCorrection"
                  >人工复核 / 修正结果</el-button
                >
              </div>
            </div>
          </div>
          <!-- 空状态展示 -->
          <div class="no-result" v-else>
            <p v-if="fileList.length === 0">请先在左侧上传图片</p>
            <p v-else-if="isDetecting">AI 正在努力检测中，请稍候...</p>
            <p v-else>点击左侧图片缩略图查看详情，或点击下方按钮开始检测</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 移动端筛选抽屉 -->
    <el-drawer
      v-model="filterDrawerVisible"
      title="检测配置"
      direction="rtl"
      size="80%"
      :with-header="true"
    >
      <div class="mobile-filter-form">
        <el-form label-position="top">
          <el-form-item label="关联运输单">
            <el-select
              v-model="selectedTransportId"
              placeholder="请选择关联运输单"
              filterable
              class="w-full"
              clearable
            >
              <el-option
                v-for="item in transportList"
                :key="item.id"
                :label="`${item.transportNo} - ${item.goodsName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="检测地点">
            <el-input v-model="detectionLocation" placeholder="检测地点 (如: A区仓库)" clearable />
          </el-form-item>
          <el-form-item label="责任主体">
            <el-input
              v-model="responsibilitySubject"
              placeholder="责任主体 (如: 物流公司)"
              clearable
            />
          </el-form-item>
        </el-form>
        <div class="drawer-footer">
          <el-button type="primary" class="w-full" @click="handleFilterConfirm">确定</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 修正抽屉 -->
    <el-drawer
      v-model="correctionDialogVisible"
      title="人工复核 / 修正检测结果"
      :size="isMobile ? '100%' : '600px'"
      append-to-body
    >
      <el-form :model="correctionForm" label-width="100px">
        <!-- 新增：图片标注区域 -->
        <div
          class="annotation-area"
          style="position: relative; margin-bottom: 20px; text-align: center"
        >
          <p style="font-size: 12px; color: #909399; margin-bottom: 5px">
            在图片上拖拽鼠标可标记破损区域（仅作为记录保存）
            <el-button
              type="primary"
              link
              size="small"
              @click="clearAnnotation"
              v-if="manualBoxes.length > 0"
              style="margin-left: 10px"
            >
              撤销上一步
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="clearAllAnnotations"
              v-if="manualBoxes.length > 0"
              style="margin-left: 10px"
            >
              全部清除
            </el-button>
          </p>
          <div style="position: relative; display: inline-block">
            <img
              ref="annotationImgRef"
              :src="correctionImgUrl"
              style="max-width: 100%; max-height: 300px; display: block"
              @load="initCanvas"
            />
            <canvas
              ref="annotationCanvasRef"
              style="position: absolute; top: 0; left: 0; cursor: crosshair"
              @mousedown="startDrawing"
              @mousemove="draw"
              @mouseup="stopDrawing"
              @mouseleave="stopDrawing"
            ></canvas>
          </div>
        </div>

        <el-form-item label="破损等级">
          <el-radio-group v-model="correctionForm.damageLevel">
            <el-radio v-for="item in damageLevels" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="破损类型">
          <el-input
            v-model="correctionForm.damageType"
            placeholder="请输入破损类型（如：划痕、凹陷）"
          />
        </el-form-item>
        <el-form-item label="备注/描述">
          <el-input
            v-model="correctionForm.damageDescription"
            type="textarea"
            rows="3"
            placeholder="请输入详细描述或修正理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="correctionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCorrection" :loading="correctionSubmitting"
            >提交修正</el-button
          >
        </span>
      </template>
    </el-drawer>

    <!-- 破损等级说明卡片 -->
    <el-card class="mt-6">
      <template #header>
        <div class="card-header-content">
          <span>破损等级判定标准说明</span>
        </div>
      </template>
      <div class="damage-level-explanation">
        <div v-for="level in damageLevels" :key="level.value" class="level-item">
          <div class="level-header">
            <el-tag :type="level.type">{{ level.label }}</el-tag>
          </div>
          <p>{{ level.description }}</p>
        </div>
      </div>
    </el-card>

    <!-- 摄像头拍摄抽屉 -->
    <el-drawer
      v-model="cameraVisible"
      title="摄像头拍摄"
      :size="isMobile ? '100%' : '640px'"
      :close-on-click-modal="false"
      @close="stopCamera"
    >
      <div class="camera-container">
        <video ref="videoRef" autoplay playsinline class="camera-video"></video>
        <canvas ref="canvasRef" style="display: none"></canvas>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stopCamera">取消</el-button>
          <!-- 拍照按钮 -->
          <el-button type="primary" :icon="Camera" @click="captureImage">拍照</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 导入依赖
 * useRoute: 获取当前路由信息
 * ref, computed, onMounted: Vue 响应式 API
 * Element Plus 组件与图标
 */
import { useRoute } from 'vue-router'
import { ref, computed, onMounted, onUnmounted, nextTick, reactive, watch } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

const route = useRoute()
import {
  Upload,
  Delete,
  Loading,
  Warning,
  Picture,
  VideoCamera,
  Camera,
  Filter,
  Edit,
} from '@element-plus/icons-vue'
import { detectBatchImages } from '@/api/detection'
import { getTransports } from '@/api/transport'
import { getDamageLevelsDict } from '@/api/common'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

// 权限控制：检查当前用户是否有发起检测的权限
const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)
const canAddDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_START_DETECTION))
const canEditDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_EDIT))
const canUseCamera = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_CAMERA))
const canConfigDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_CONFIG))
const canClearList = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_CLEAR))

// 获取基础 API 地址，用于拼接后端返回的相对路径图片
const baseUrl = import.meta.env.VITE_APP_BASE_API || ''

/**
 * 获取完整的图片访问 URL
 * 兼容处理绝对路径、相对路径、Blob URL 和 Base64
 * @param {string} url 图片路径
 */
const getFullUrl = (url) => {
  if (!url) return ''
  // 1. 如果是完整 URL (http)、Blob (本地预览)、Base64 (Canvas)，直接返回
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) {
    return url
  }

  // 2. 如果是后端返回的 /uploads 路径，由于开发环境配置了代理，生产环境配置了 Nginx，通常直接使用即可
  if (url.startsWith('/uploads')) {
    return url
  }

  // 3. 兜底逻辑：确保路径以 / 开头
  return url.startsWith('/') ? url : `/${url}`
}

// 常量配置
const MAX_FILE_SIZE = 14 * 1024 * 1024 // 单张图片最大 14MB
const MAX_BATCH_COUNT = 20 // 单次批量检测最大数量 20 张
// 允许上传的文件 MIME 类型
const ALLOWED_TYPES = [
  'image/jpeg',
  'image/png',
  'image/jpg',
  'image/gif',
  'image/bmp',
  'image/webp',
]

/**
 * 响应式数据定义
 * fileList: 图片列表，核心数据结构
 * item 结构: {
 *   id: 唯一标识,
 *   file: File对象,
 *   url: 预览地址,
 *   status: 'pending'(待检测)|'detecting'(检测中)|'done'(完成)|'error'(失败),
 *   result: 检测结果对象
 * }
 */
const fileList = ref([])
const currentIndex = ref(-1) // 当前选中的图片索引
const isDetecting = ref(false) // 全局检测状态 loading
const transportList = ref([]) // 运输单下拉列表数据
const selectedTransportId = ref(null) // 当前选中的运输单 ID

// 字典数据：破损等级定义
const damageLevels = ref([])

// 业务字段：检测地点与责任主体
const detectionLocation = ref('')
const responsibilitySubject = ref('')

// 摄像头相关状态
const cameraVisible = ref(false)
const videoRef = ref(null)
const canvasRef = ref(null)
let mediaStream = null // 媒体流对象

/**
 * 打开摄像头
 * 请求用户授权并获取视频流
 */
const openCamera = async () => {
  cameraVisible.value = true
  await nextTick()
  try {
    // 检查浏览器兼容性
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      // 请求视频流
      mediaStream = await navigator.mediaDevices.getUserMedia({ video: true })
      if (videoRef.value) {
        videoRef.value.srcObject = mediaStream
        videoRef.value.play()
      }
    } else {
      ElMessage.error('您的浏览器不支持摄像头功能')
    }
  } catch (err) {
    console.error('无法启动摄像头:', err)
    ElMessage.error('无法启动摄像头，请确保已授予摄像头权限')
    cameraVisible.value = false
  }
}

/**
 * 关闭摄像头
 * 停止所有媒体轨道并释放资源
 */
const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
  cameraVisible.value = false
}

/**
 * 拍照功能
 * 将当前视频帧绘制到 Canvas 并转换为 File 对象
 */
const captureImage = () => {
  if (!videoRef.value || !canvasRef.value) {
    console.error('视频或画布元素未初始化')
    return
  }

  const video = videoRef.value
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  // 设置画布大小与视频源一致，保证清晰度
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight

  // 绘制当前帧
  context.drawImage(video, 0, 0, canvas.width, canvas.height)

  // 转换为 Blob 对象，再封装为 File 对象
  canvas.toBlob((blob) => {
    if (blob) {
      // 校验当前列表数量
      if (fileList.value.length >= MAX_BATCH_COUNT) {
        ElMessage.warning(`最多只能上传 ${MAX_BATCH_COUNT} 张图片`)
        return
      }

      const file = new File([blob], `camera_${Date.now()}.png`, { type: 'image/png' })
      const newItem = {
        id: Date.now() + Math.random(),
        file: file,
        url: URL.createObjectURL(blob),
        status: 'pending',
        result: null,
      }
      fileList.value.push(newItem)

      // 如果是第一张图片，自动选中以便预览
      if (fileList.value.length === 1) {
        currentIndex.value = 0
      }

      ElMessage.success('拍照成功')
      stopCamera()
    }
  }, 'image/png')
}

// 裁剪功能相关状态（预留功能，暂未使用）
const cropperVisible = ref(false)
const cropperImg = ref('')
const cropperRef = ref(null)
const cropping = ref(false)
const currentCropIndex = ref(-1)

const openCropper = (index) => {
  currentCropIndex.value = index
  cropperImg.value = fileList.value[index].url
  cropperVisible.value = true
}

const rotateLeft = () => cropperRef.value.rotateLeft()
const rotateRight = () => cropperRef.value.rotateRight()

const confirmCrop = () => {
  cropping.value = true
  cropperRef.value.getCropBlob((blob) => {
    const originalFile = fileList.value[currentCropIndex.value].file
    const newFile = new File([blob], originalFile.name, {
      type: 'image/png',
      lastModified: Date.now(),
    })

    URL.revokeObjectURL(fileList.value[currentCropIndex.value].url)

    fileList.value[currentCropIndex.value].file = newFile
    fileList.value[currentCropIndex.value].url = URL.createObjectURL(blob)

    cropping.value = false
    cropperVisible.value = false
    ElMessage.success('图片处理完成')
  })
}

/**
 * 初始化：获取运输单列表
 * 用于下拉框选择
 */
const fetchTransports = async () => {
  try {
    const res = await getTransports({ page: 1, size: 100 }) // 获取最近的运输单
    transportList.value = res.list || []
  } catch (error) {
    console.error('获取运输列表失败', error)
  }
}

/**
 * 初始化：获取破损等级字典
 * 用于展示等级说明和标签颜色
 */
const fetchDamageLevels = async () => {
  try {
    const res = await getDamageLevelsDict()
    if (res) {
      damageLevels.value = res
    }
  } catch (error) {
    console.error('获取破损等级说明失败', error)
  }
}

// 监听运输单选择，自动填充信息
watch(selectedTransportId, (newVal) => {
  if (!newVal) {
    detectionLocation.value = ''
    responsibilitySubject.value = ''
    return
  }
  const transport = transportList.value.find((t) => t.id === newVal)
  if (transport) {
    // 自动填充责任主体 (承运商)
    if (transport.carrier) {
      responsibilitySubject.value = transport.carrier
    }
    // 自动填充检测地点 (目的地)
    if (transport.destination) {
      detectionLocation.value = transport.destination
    }
  }
})

const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
}

onMounted(() => {
  fetchTransports()
  fetchDamageLevels()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 计算属性：当前选中图片的检测结果
const currentResult = computed(() => {
  if (currentIndex.value === -1 || !fileList.value[currentIndex.value]) return null
  return fileList.value[currentIndex.value].result
})

// 计算属性：是否有已完成的检测结果
const hasResults = computed(() => {
  return fileList.value.some((item) => item.status === 'done' && item.result)
})

/**
 * 文件选择回调处理
 * 负责文件校验和预览生成
 */
const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw

  // 1. 校验文件格式
  if (!ALLOWED_TYPES.includes(file.type)) {
    ElMessage.error('文件格式不正确，仅支持 JPG, PNG, GIF, BMP, WEBP 格式')
    return
  }

  // 2. 校验文件大小
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error(`文件 ${file.name} 大小超过 14MB 限制`)
    return
  }

  // 3. 校验数量限制
  if (fileList.value.length >= MAX_BATCH_COUNT) {
    ElMessage.warning(`最多只能上传 ${MAX_BATCH_COUNT} 张图片`)
    return
  }

  // 4. 生成列表项
  const newItem = {
    id: Date.now() + Math.random(),
    file: file,
    // 使用 URL.createObjectURL 生成本地预览地址，无需上传服务器即可预览
    url: URL.createObjectURL(file),
    status: 'pending',
    result: null,
  }
  fileList.value.push(newItem)

  // 自动选中第一张
  if (fileList.value.length === 1) {
    currentIndex.value = 0
  }
}

// 切换选中图片
const selectImage = (index) => {
  currentIndex.value = index
}

/**
 * 移除图片
 * 务必释放 URL 对象以防止内存泄漏
 */
const removeImage = (index) => {
  URL.revokeObjectURL(fileList.value[index].url)

  fileList.value.splice(index, 1)

  // 移除后重新计算当前选中索引
  if (fileList.value.length === 0) {
    currentIndex.value = -1
  } else if (index <= currentIndex.value) {
    currentIndex.value = Math.max(0, currentIndex.value - 1)
  }
}

// 清空所有图片
const clearAll = () => {
  fileList.value.forEach((item) => URL.revokeObjectURL(item.url))
  fileList.value = []
  currentIndex.value = -1
  isDetecting.value = false
}

/**
 * 【备用功能】前端绘制检测框辅助函数
 *
 * 说明：
 * 目前系统优先展示后端 AI 处理并返回的带有标注框的图片 (processedImageUrl)。
 * 如果后端只返回了 JSON 格式的坐标数据 (boxes)，可以使用此函数在前端 Canvas 上动态绘制红色检测框。
 *
 * @param {string} imageUrl 图片 URL
 * @param {Array} detections 检测结果/坐标数组
 * @returns {Promise<string>} 返回绘制了检测框的图片 Base64 字符串
 */
const drawBoxesOnImage = (imageUrl, detections) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'Anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')

      ctx.drawImage(img, 0, 0)
      ctx.lineWidth = 3
      ctx.font = '16px Arial'

      detections.forEach((det) => {
        let x, y, w, h, label, score

        if (Array.isArray(det)) {
          ;[x, y, w, h, score, label] = det
        } else {
          x = det.x || det.bbox?.[0] || 0
          y = det.y || det.bbox?.[1] || 0
          w = det.width || det.w || det.bbox?.[2] || 0
          h = det.height || det.h || det.bbox?.[3] || 0
          label = det.label || det.class || 'unknown'
          score = det.confidence || det.score || 0
        }

        const style = getComputedStyle(document.documentElement)
        const dangerColor = style.getPropertyValue('--el-color-danger').trim() || '#F56C6C'

        ctx.strokeStyle = dangerColor
        ctx.strokeRect(x, y, w, h)

        const text = `${label} ${(score * 100).toFixed(1)}%`
        const textWidth = ctx.measureText(text).width
        ctx.fillStyle = dangerColor
        ctx.fillRect(x, y - 25, textWidth + 10, 25)

        ctx.fillStyle = '#fff'
        ctx.fillText(text, x + 5, y - 7)
      })

      resolve(canvas.toDataURL('image/jpeg'))
    }
    img.onerror = reject
    img.src = imageUrl
  })
}

/**
 * 核心功能：开始批量检测
 * 调用后端 API 进行 AI 识别
 */
const startBatchDetection = async () => {
  // 1. 前置校验
  if (!selectedTransportId.value) {
    ElMessage.warning('请先选择关联的运输单，以便系统自动保存记录')
    return
  }

  // 筛选出待检测或之前失败的图片
  const pendingItems = fileList.value.filter(
    (item) => item.status === 'pending' || item.status === 'error',
  )

  if (pendingItems.length === 0) {
    ElMessage.warning('没有待检测的图片')
    return
  }

  isDetecting.value = true

  try {
    // 2. 准备请求参数
    const files = pendingItems.map((item) => item.file)

    // 判断检测模式：如果文件名包含 camera_ 前缀，则认为是摄像头模式
    const isCamera = files.some((f) => f.name.startsWith('camera_'))

    const data = {
      transportId: selectedTransportId.value,
      detectionMode: isCamera ? 2 : 1, // 1: 本地上传, 2: 摄像头
      detectionLocation: detectionLocation.value,
      responsibilitySubject: responsibilitySubject.value,
    }

    // 更新 UI 状态为检测中
    pendingItems.forEach((item) => {
      item.status = 'detecting'
    })

    // 3. 调用后端批量检测接口
    const results = await detectBatchImages(files, data)

    // 3. 处理返回结果
    let imageResults = []
    let commonInfo = {}

    if (Array.isArray(results)) {
      imageResults = results
    } else if (results.imageList && Array.isArray(results.imageList)) {
      // 如果返回的是 BizDetection 对象且包含 imageList
      imageResults = results.imageList
      commonInfo = {
        detectionNo: results.detectionNo,
        detectionTime: results.detectionTime,
        damageDescription: results.damageDescription,
      }
    } else {
      // 兜底：如果是单个对象且没有 imageList
      imageResults = [results]
    }

    pendingItems.forEach((item, index) => {
      // 按索引对应，防止数组越界
      const res = imageResults[index] || imageResults[0]

      if (res) {
        item.status = 'done'
        item.result = {
          id: res.id,
          detectionNo: res.detectionNo || res.detection_no || commonInfo.detectionNo,

          // 图片路径映射 (兼容新旧字段)
          originalUrl: res.originalUrl || res.imageUrl || res.image_url,
          processedUrl:
            res.processedUrl || res.processedImageUrl || res.processed_image_url || null,

          damageLevel: res.damageLevel ?? 0,
          damageArea: res.damageArea ?? '0',
          damageType: res.damageType || '未知',
          confidence: res.confidence || 0,
          boundingBoxes: res.boundingBoxes || res.bounding_boxes || null,
          detectionTime:
            res.detectionTime || commonInfo.detectionTime || new Date().toLocaleString(),
          damageDescription: res.damageDescription || commonInfo.damageDescription || '检测完成',
        }
      }
    })

    ElNotification.success({
      title: '批量检测完成',
      message: `成功处理 ${pendingItems.length} 张图片`,
      duration: 3000,
    })

    // 排序逻辑：检测完成后，将破损严重的图片排在最前面
    fileList.value.sort((a, b) => {
      // 1. 优先将已检测完成的排在前面 (status === 'done')
      if (a.status === 'done' && b.status !== 'done') return -1
      if (a.status !== 'done' && b.status === 'done') return 1

      // 2. 在已检测完成的图片中，按破损等级降序排列 (严重 -> 完好)
      if (a.status === 'done' && b.status === 'done') {
        const levelA = a.result ? a.result.damageLevel || 0 : 0
        const levelB = b.result ? b.result.damageLevel || 0 : 0
        return levelB - levelA
      }
      return 0
    })

    // 如果列表重排了，重置选中索引为第一个（即最严重的那个）
    if (fileList.value.length > 0) {
      selectImage(0)
    }
  } catch (error) {
    console.error('批量检测失败:', error)
    pendingItems.forEach((item) => {
      item.status = 'error'
    })
    ElMessage.error(error.message || '批量检测过程出错')
  } finally {
    isDetecting.value = false
  }
}

// 辅助函数
const getStatusText = (status) => {
  const map = {
    pending: '待检测',
    detecting: '检测中',
    done: '已完成',
    error: '失败',
  }
  return map[status]
}

const getStatusType = (status) => {
  const map = {
    pending: 'info',
    detecting: 'warning',
    done: 'success',
    error: 'danger',
  }
  return map[status]
}

const getDamageLevelText = (level) => {
  const item = damageLevels.value.find((item) => item.value === Number(level))
  return item ? item.label : '未知'
}

const getDamageLevelTagType = (level) => {
  const item = damageLevels.value.find((item) => item.value === Number(level))
  return item ? item.type : 'info'
}

// ===========================
// 人工修正相关逻辑
// ===========================
import { updateDetectionImage } from '@/api/detection' // 假设 api 文件中有这个方法，如果还没定义，需要稍后添加

const correctionDialogVisible = ref(false)
const correctionSubmitting = ref(false)
const correctionForm = reactive({
  id: null,
  damageLevel: 0,
  damageType: '',
  damageDescription: '',
})

// 标注相关状态
const annotationImgRef = ref(null)
const annotationCanvasRef = ref(null)
const correctionImgUrl = ref('')
const isDrawing = ref(false)
const startX = ref(0)
const startY = ref(0)
const manualBoxes = ref([]) // 数组：存储多个标注框 {x, y, w, h}

const handleManualCorrection = async () => {
  if (!currentResult.value) return

  // 初始化表单数据
  correctionForm.id = currentResult.value.id
  correctionForm.damageLevel = currentResult.value.damageLevel
  correctionForm.damageType = currentResult.value.damageType
  correctionForm.damageDescription = currentResult.value.damageDescription

  // 初始化图片 URL (优先使用 originalUrl 进行标注，因为标注坐标是基于原图比例的)
  correctionImgUrl.value = getFullUrl(currentResult.value.originalUrl)

  // 重置标注状态
  manualBoxes.value = []
  if (currentResult.value.boundingBoxes) {
    try {
      // 如果后端返回了现有的框，尝试加载
      const existingBoxes = JSON.parse(currentResult.value.boundingBoxes)
      // 需要映射回显示尺寸 (这一步在 initCanvas 之后做更合适，或者存原始坐标)
      // 为了简单，我们先存原始坐标，在重绘时转换
      manualBoxes.value = existingBoxes.map((box) => ({
        x: box.location.left,
        y: box.location.top,
        w: box.location.width,
        h: box.location.height,
        name: box.name,
        score: box.score,
        isOriginal: true, // 标记为原始 AI 框
      }))
    } catch (e) {
      console.error('解析现有边框失败', e)
    }
  }
  isDrawing.value = false

  correctionDialogVisible.value = true

  // 等待 DOM 更新后初始化 Canvas
  await nextTick()
  // 图片加载可能需要时间，initCanvas 会在 @load 中调用
}

const initCanvas = () => {
  if (!annotationImgRef.value || !annotationCanvasRef.value) return

  const img = annotationImgRef.value
  const canvas = annotationCanvasRef.value

  // 设置 Canvas 尺寸与图片显示尺寸一致
  canvas.width = img.width
  canvas.height = img.height

  // 清空画布
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 如果已有框，初始绘制一次
  if (manualBoxes.value.length > 0) {
    ctx.lineWidth = 2
    const dScaleX = img.width / img.naturalWidth
    const dScaleY = img.height / img.naturalHeight
    manualBoxes.value.forEach((box) => {
      ctx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C'
      ctx.strokeRect(box.x * dScaleX, box.y * dScaleY, box.w * dScaleX, box.h * dScaleY)
    })
  }
}

const startDrawing = (e) => {
  isDrawing.value = true
  const rect = annotationCanvasRef.value.getBoundingClientRect()
  startX.value = e.clientX - rect.left
  startY.value = e.clientY - rect.top
}

const draw = (e) => {
  if (!isDrawing.value) return

  const canvas = annotationCanvasRef.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  const img = annotationImgRef.value

  const currentX = e.clientX - rect.left
  const currentY = e.clientY - rect.top

  const width = currentX - startX.value
  const height = currentY - startY.value

  // 计算比例
  const scaleX = img.width / img.naturalWidth
  const scaleY = img.height / img.naturalHeight

  // 清空画布并重绘所有已存在的框
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.lineWidth = 2

  // 绘制已保存的框 (转换回显示尺寸)
  manualBoxes.value.forEach((box) => {
    ctx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C' // AI框绿色，手动框红色
    ctx.strokeRect(box.x * scaleX, box.y * scaleY, box.w * scaleX, box.h * scaleY)
  })

  // 绘制当前正在画的框
  ctx.strokeStyle = '#409EFF' // 当前正在画的框用蓝色
  ctx.setLineDash([5, 5])
  ctx.strokeRect(startX.value, startY.value, width, height)
  ctx.setLineDash([])
}

const stopDrawing = (e) => {
  if (!isDrawing.value) return
  isDrawing.value = false

  const canvas = annotationCanvasRef.value
  const img = annotationImgRef.value
  const rect = canvas.getBoundingClientRect()
  const endX = e.clientX - rect.left
  const endY = e.clientY - rect.top

  // 转换回原始图片比例的坐标
  const scaleX = img.naturalWidth / img.width
  const scaleY = img.naturalHeight / img.height

  const displayW = Math.abs(endX - startX.value)
  const displayH = Math.abs(endY - startY.value)

  // 只有当宽高都大于一定值（防误触）时才添加
  if (displayW > 5 && displayH > 5) {
    const newBox = {
      x: Math.min(startX.value, endX) * scaleX,
      y: Math.min(startY.value, endY) * scaleY,
      w: displayW * scaleX,
      h: displayH * scaleY,
      name: correctionForm.damageType || '手动标注',
      score: 1.0,
      isOriginal: false,
    }
    manualBoxes.value.push(newBox)
  }

  // 重绘最终状态
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.lineWidth = 2
  const dScaleX = img.width / img.naturalWidth
  const dScaleY = img.height / img.naturalHeight

  manualBoxes.value.forEach((box) => {
    ctx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C'
    ctx.strokeRect(box.x * dScaleX, box.y * dScaleY, box.w * dScaleX, box.h * dScaleY)
  })
}

const clearAnnotation = () => {
  // 撤销上一步 (移除最后一个框)
  if (manualBoxes.value.length > 0) {
    manualBoxes.value.pop()
  }

  // 重绘
  if (annotationCanvasRef.value && annotationImgRef.value) {
    const canvas = annotationCanvasRef.value
    const ctx = canvas.getContext('2d')
    const img = annotationImgRef.value
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.lineWidth = 2

    const dScaleX = img.width / img.naturalWidth
    const dScaleY = img.height / img.naturalHeight

    manualBoxes.value.forEach((box) => {
      ctx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C'
      ctx.strokeRect(box.x * dScaleX, box.y * dScaleY, box.w * dScaleX, box.h * dScaleY)
    })
  }
}

const clearAllAnnotations = () => {
  manualBoxes.value = []
  if (annotationCanvasRef.value) {
    const canvas = annotationCanvasRef.value
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
  }
}

const submitCorrection = async () => {
  if (!correctionForm.id) return

  try {
    correctionSubmitting.value = true

    // 格式化标注框为后端要求的 JSON 结构
    if (manualBoxes.value.length > 0) {
      const formattedBoxes = manualBoxes.value.map((box) => ({
        name: box.name || correctionForm.damageType || '破损',
        score: box.score || 1.0,
        location: {
          left: Math.round(box.x),
          top: Math.round(box.y),
          width: Math.round(box.w),
          height: Math.round(box.h),
        },
      }))
      correctionForm.boundingBoxes = JSON.stringify(formattedBoxes)

      if (!correctionForm.damageDescription.includes('人工标注')) {
        correctionForm.damageDescription += ' [人工已核对/标注区域]'
      }
    } else {
      correctionForm.boundingBoxes = null
    }

    // 调用后端更新接口
    const res = await updateDetectionImage(correctionForm)

    ElMessage.success('人工修正已提交')
    correctionDialogVisible.value = false

    // 更新前端显示的当前结果
    if (currentResult.value && currentResult.value.id === correctionForm.id) {
      currentResult.value.damageLevel = correctionForm.damageLevel
      currentResult.value.damageType = correctionForm.damageType
      currentResult.value.damageDescription = correctionForm.damageDescription
      // 如果后端返回了新的处理后图片地址，更新它
      if (res && res.processedUrl) {
        currentResult.value.processedUrl = res.processedUrl
      }
      if (correctionForm.boundingBoxes) {
        currentResult.value.boundingBoxes = correctionForm.boundingBoxes
      }
    }

    // 刷新整个检测记录（可选，为了确保数据一致性）
    // fetchDetections()
  } catch (error) {
    console.error('修正提交失败', error)
    ElMessage.error('修正提交失败: ' + (error.message || '未知错误'))
  } finally {
    correctionSubmitting.value = false
  }
}
</script>

<style scoped>
/* 布局样式已提取到全局 */

.detection-page-container {
  height: 100%;
  overflow-y: auto;
  padding: 10px;
}

.detection-card {
  min-height: auto;
}

.search-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mobile-search-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.mobile-top-row {
  display: flex;
  gap: 10px;
}

.flex-1 {
  flex: 1;
}

.mobile-full-btn {
  width: 100%;
  margin-left: 0 !important;
}

.abnormal-alert {
  color: var(--el-color-danger);
  font-weight: bold;
  margin-left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.level-item {
  text-align: center;
}

.level-item p {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

@media screen and (max-width: 768px) {
  .detection-content {
    flex-direction: column;
  }

  .upload-section {
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-light);
    padding-right: 0;
    padding-bottom: 20px;
  }

  .detection-section {
    padding-left: 0;
    padding-top: 20px;
  }

  .card-header-content {
    flex-direction: row;
    align-items: center;
  }
}

.detection-content {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.upload-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-light);
  padding-right: 20px;
}

.detection-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-left: 20px;
}

.image-list {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  grid-auto-rows: 140px;
  gap: 15px;
  align-content: start;
  padding: 5px;
}

.image-card {
  position: relative;
  border: 2px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background-color: var(--el-fill-color-light);
  height: 140px; /* 明确高度 */
  display: flex;
  flex-direction: column;
}

.image-card:hover {
  box-shadow: var(--el-box-shadow-light);
  border-color: var(--el-color-primary-light-5);
}

.image-card.active {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.list-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-status {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 0;
  text-align: center;
  backdrop-filter: blur(4px);
}

.image-status :deep(.el-tag) {
  border: none;
  background: transparent;
  font-weight: bold;
}

.delete-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-card:hover .delete-btn {
  opacity: 1;
}

.detection-result {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-image {
  flex: 2;
  background: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  overflow: hidden;
  min-height: 300px; /* 增加最小高度 */
  border: 1px solid var(--el-border-color-lighter);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--el-text-color-secondary);
}

.image-error .el-icon {
  font-size: 40px;
}

.result-img {
  width: 100%;
  height: 100%;
}

.result-info {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  align-content: start;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-item .label {
  color: var(--el-text-color-secondary);
  min-width: 70px;
}

.description {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

.no-result {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  border-radius: 4px;
  margin-top: 10px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.damage-level-explanation {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media screen and (max-width: 768px) {
  .damage-level-explanation {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .level-item {
    text-align: left !important;
    background: var(--el-fill-color-blank);
    padding: 10px;
    border-radius: 8px;
    border: 1px solid var(--el-border-color-lighter);
  }

  .level-header {
    margin-bottom: 5px;
  }

  .level-item p {
    margin-top: 5px !important;
    line-height: 1.4;
  }

  .mobile-filter-form {
    padding: 0 10px;
  }

  .drawer-footer {
    margin-top: 30px;
    padding-bottom: 20px;
  }

  .w-full {
    width: 100% !important;
  }

  .result-info {
    grid-template-columns: 1fr !important;
  }

  .image-list {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)) !important;
    grid-auto-rows: 100px !important;
  }

  .image-card {
    height: 100px !important;
  }

  .upload-section {
    padding-right: 0 !important;
    border-right: none !important;
    border-bottom: 1px solid var(--el-border-color-light);
    margin-bottom: 20px;
  }

  .detection-section {
    padding-left: 0 !important;
  }

  .action-buttons {
    position: sticky;
    bottom: 0;
    background: var(--el-bg-color);
    padding: 15px 0;
    z-index: 10;
    margin-top: 0;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  }
}

.mt-6 {
  margin-top: 0;
  flex-shrink: 0;
}

/* 重构后的样式 */
.w-300 {
  width: 300px;
}

.ml-10 {
  margin-left: 10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  margin: 0;
}

.w-200 {
  width: 200px;
}

.camera-container {
  width: 100%;
  height: 400px;
  background-color: #000;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
