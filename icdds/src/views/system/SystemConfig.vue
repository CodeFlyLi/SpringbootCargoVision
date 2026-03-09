<template>
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <div class="search-bar-container">
        <!-- PC 操作栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 保存配置按钮 - 需要修改配置权限 -->
          <el-button
            v-if="canUpdateConfig"
            type="primary"
            :icon="Check"
            @click="saveAllConfigs"
            :loading="loading"
          >
            保存配置
          </el-button>
          <!-- 刷新配置按钮 - 需要查询配置权限 -->
          <el-button v-if="canQueryConfig" @click="fetchConfigs" :icon="Refresh">
            <span>刷新配置</span>
          </el-button>
        </div>

        <!-- 移动端操作栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 保存按钮 - 需要修改配置权限 -->
          <el-button
            v-if="canUpdateConfig"
            type="primary"
            @click="saveAllConfigs"
            :loading="loading"
            :icon="Check"
          >
            <span>保存</span>
          </el-button>
          <!-- 刷新按钮 - 需要查询配置权限 -->
          <el-button v-if="canQueryConfig" @click="fetchConfigs" :icon="Refresh">
            <span>刷新</span>
          </el-button>
        </div>
      </div>

      <div class="config-content">
        <el-scrollbar>
          <div class="form-wrapper">
            <!-- AI 检测参数部分 -->
            <div class="form-section">
              <h3 class="section-title">AI 检测参数</h3>
              <el-form
                :model="aiConfig"
                :label-width="isMobile ? '100px' : '160px'"
                :label-position="isMobile ? 'top' : 'right'"
                class="config-form"
              >
                <el-form-item label="置信度阈值">
                  <el-slider
                    v-model="aiConfig.confidenceThreshold"
                    :min="0.1"
                    :max="0.99"
                    :step="0.01"
                    :show-input="!isMobile"
                    class="config-slider"
                  />
                  <div class="form-tip">低于此阈值的检测结果将被标记为"低置信度/不确定"</div>
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :span="isMobile ? 24 : 12">
                    <el-form-item label="百度云 API Key">
                      <el-input
                        v-model="aiConfig.baiduApiKey"
                        placeholder="请输入百度云 API Key"
                        show-password
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="isMobile ? 24 : 12">
                    <el-form-item label="百度云 Secret Key">
                      <el-input
                        v-model="aiConfig.baiduSecretKey"
                        placeholder="请输入百度云 Secret Key"
                        show-password
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="百度云 API Endpoint">
                  <el-input
                    v-model="aiConfig.baiduEndpoint"
                    placeholder="默认为通用物体识别接口地址"
                  />
                </el-form-item>
                <el-form-item label="EasyDL 自定义接口">
                  <el-input
                    v-model="aiConfig.baiduCustomEndpoint"
                    placeholder="请输入 EasyDL 破损检测自定义接口地址"
                  />
                  <div class="form-tip">
                    配置此项后将优先使用 EasyDL 定制化模型。
                    <br />
                    如配置的是<b>物体检测模型</b>接口，系统将自动在图片上<b>标注破损位置</b>。
                  </div>
                </el-form-item>
              </el-form>
            </div>

            <el-divider />

            <!-- 存储与其他设置部分 -->
            <div class="form-section">
              <h3 class="section-title">存储与其他设置</h3>
              <el-form
                :model="sysConfig"
                :label-width="isMobile ? '100px' : '160px'"
                :label-position="isMobile ? 'top' : 'right'"
                class="config-form"
              >
                <el-form-item label="默认照片存储路径">
                  <el-input v-model="sysConfig.uploadDir" placeholder="例如: /uploads/" />
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted, reactive, onUnmounted, computed } from 'vue'
import { Refresh, Check } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

const route = useRoute()
import { ElMessage } from 'element-plus'
// 需要在 api/system.js 中添加相关接口
import { getSystemConfigs, updateSystemConfigs } from '@/api/system'

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canQueryConfig = computed(() => hasPermission(perms.value, PERMISSIONS.CONFIG_QUERY))
const canUpdateConfig = computed(() => hasPermission(perms.value, PERMISSIONS.CONFIG_UPDATE))

const loading = ref(false)

// 响应式布局
const isMobile = ref(window.innerWidth < 768)
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const aiConfig = reactive({
  confidenceThreshold: 0.5,
  baiduApiKey: '',
  baiduSecretKey: '',
  baiduEndpoint: '',
  baiduCustomEndpoint: '',
})

const sysConfig = reactive({
  uploadDir: '',
})

// 映射配置 Key 到前端模型
const configKeyMap = {
  'ai.confidence.threshold': (val) => (aiConfig.confidenceThreshold = parseFloat(val)),
  'ai.baidu.api_key': (val) => (aiConfig.baiduApiKey = val),
  'ai.baidu.secret_key': (val) => (aiConfig.baiduSecretKey = val),
  'ai.baidu.endpoint': (val) => (aiConfig.baiduEndpoint = val),
  'ai.baidu.custom_endpoint': (val) => (aiConfig.baiduCustomEndpoint = val),
  'file.upload.dir': (val) => (sysConfig.uploadDir = val),
}

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await getSystemConfigs()
    // 响应拦截器直接返回了 data (List)，或者原始 response
    // 如果是数组，说明是 data 部分
    const configs = Array.isArray(res) ? res : res.data || []

    if (configs.length > 0) {
      configs.forEach((item) => {
        if (configKeyMap[item.configKey]) {
          configKeyMap[item.configKey](item.configValue)
        }
      })
    }
  } catch (error) {
    console.error('Failed to fetch configs', error)
  } finally {
    loading.value = false
  }
}

const saveAllConfigs = async () => {
  const configs = {
    'ai.confidence.threshold': aiConfig.confidenceThreshold.toString(),
    'ai.baidu.api_key': aiConfig.baiduApiKey,
    'ai.baidu.secret_key': aiConfig.baiduSecretKey,
    'ai.baidu.endpoint': aiConfig.baiduEndpoint,
    'ai.baidu.custom_endpoint': aiConfig.baiduCustomEndpoint,
    'file.upload.dir': sysConfig.uploadDir,
  }
  await submitConfigs(configs)
}

const submitConfigs = async (configs) => {
  loading.value = true
  try {
    await updateSystemConfigs(configs)
    // 拦截器已处理非200错误，此处直接视为成功
    ElMessage.success('配置已更新')
    await fetchConfigs()
  } catch (error) {
    console.error(error)
    // 错误消息通常已由拦截器处理，此处不再重复弹窗，除非是网络等未知错误
    if (!error.code) {
      // 简单判断是否已被拦截器处理
      ElMessage.error('系统错误')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchConfigs()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 布局样式已提取到全局 */

.search-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mobile-search-bar {
  display: flex;
  gap: 10px;
  width: 100%;
}

.config-content {
  flex: 1;
  overflow: hidden;
}

.form-wrapper {
  padding: 24px;
  max-width: 1000px;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
}

.config-form {
  padding-left: 12px;
}

.config-slider {
  width: 100%;
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin-top: 4px;
}

.card-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: flex-start;
  background-color: var(--el-bg-color);
}

@media (max-width: 768px) {
  /* 移动端适配样式已迁移至全局或使用媒体查询 */

  .form-wrapper {
    padding: 16px;
  }

  .config-form {
    padding-left: 0;
  }

  .section-title {
    font-size: 15px;
    margin-bottom: 16px;
  }

  .card-footer {
    padding: 12px 16px;
  }

  .mobile-save-btn {
    width: 100%;
  }

  :deep(.el-form-item__label) {
    margin-bottom: 4px !important;
    line-height: 1.4 !important;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
