<template>
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <div class="search-bar-container">
        <!-- PC 搜索栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 新建检测按钮 - 需要新建检测权限 -->
          <el-button v-if="canAddDetect" type="primary" @click="handleBatchDetection" :icon="Plus">
            新建检测
          </el-button>
          <!-- 导出报告按钮 - 需要导出检测权限 -->
          <el-button v-if="canExportDetect" type="primary" @click="handleExport" :icon="Download">
            导出报告
          </el-button>
          <!-- 导出选中按钮 - 需要导出选中检测权限 -->
          <el-button
            v-if="canExportSelectedDetect"
            type="success"
            @click="handleExportSelected"
            :disabled="selectedRows.length === 0"
            :icon="Download"
          >
            导出选中
          </el-button>
          <el-input
            v-model="searchForm.detectionNo"
            placeholder="检测编号"
            clearable
            class="w-200"
          />
          <el-input
            v-model="searchForm.transportNo"
            placeholder="运输单号"
            clearable
            class="w-200"
          />
          <el-input v-model="searchForm.sceneType" placeholder="检测地点" clearable class="w-180" />
          <el-input v-model="searchForm.nodeName" placeholder="责任主体" clearable class="w-180" />
          <el-input v-model="searchForm.goodsName" placeholder="货物名称" clearable class="w-180" />
          <el-select
            v-model="searchForm.damageLevel"
            placeholder="破损等级"
            clearable
            class="w-150"
          >
            <el-option
              v-for="item in damageLevelList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="w-260"
          />
          <!-- 搜索按钮 - 需要查询检测权限 -->
          <el-button v-if="canQueryDetect" type="primary" @click="handleSearch" :icon="Search">
            搜索
          </el-button>
          <!-- 重置按钮 - 需要重置或查询检测权限 -->
          <el-button v-if="canResetDetect" @click="resetSearch" :icon="Refresh"> 重置 </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 新建按钮 - 需要新建检测权限 -->
          <el-button v-if="canAddDetect" type="primary" @click="handleBatchDetection" :icon="Plus">
            新建
          </el-button>
          <!-- 导出按钮 - 需要导出检测权限 -->
          <el-button v-if="canExportDetect" type="primary" @click="handleExport" :icon="Download">
            导出
          </el-button>
          <!-- 筛选按钮 - 需要查询检测权限 -->
          <el-button
            v-if="canQueryDetect"
            type="info"
            @click="filterDrawerVisible = true"
            :icon="Filter"
          >
            筛选
          </el-button>
        </div>
      </div>

      <!-- PC 表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="detectionList"
        stripe
        class="detection-table"
        height="100%"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <el-empty v-if="!loading" description="暂无检测数据" />
          <div v-else style="height: 100px"></div>
        </template>
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column label="原始图片" width="120">
          <template #default="scope">
            <el-image
              class="table-img"
              :src="
                scope.row.imageList?.[0]?.originalUrl || scope.row.imageUrl || scope.row.image_url
              "
              :preview-src-list="
                scope.row.imageList && scope.row.imageList.length > 0
                  ? scope.row.imageList.map((img) => img.originalUrl).filter(Boolean)
                  : [scope.row.imageUrl || scope.row.image_url].filter(Boolean)
              "
              fit="cover"
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="处理后图片" width="120">
          <template #default="scope">
            <el-image
              class="table-img"
              :src="
                scope.row.imageList?.[0]?.processedUrl ||
                scope.row.imageList?.[0]?.originalUrl ||
                scope.row.processedImageUrl ||
                scope.row.processed_image_url
              "
              :preview-src-list="
                scope.row.imageList && scope.row.imageList.length > 0
                  ? scope.row.imageList
                      .map((img) => img.processedUrl || img.originalUrl)
                      .filter(Boolean)
                  : [scope.row.processedImageUrl || scope.row.processed_image_url].filter(Boolean)
              "
              fit="cover"
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <span class="no-image-text">无</span>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column
          prop="detectionNo"
          label="检测编号"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="detectionLocation"
          label="检测地点"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.detectionLocation || scope.row.sceneType || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="responsibilitySubject"
          label="责任主体"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.responsibilitySubject || scope.row.nodeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="transportNo"
          label="运输单号"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="goodsName" label="货物名称" min-width="180" show-overflow-tooltip />
        <el-table-column
          prop="detectionTime"
          label="检测时间"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="operatorName" label="操作员" min-width="100" show-overflow-tooltip />
        <el-table-column prop="damageLevel" label="破损等级" width="120">
          <template #default="scope">
            <el-tag :type="getDamageLevelTagType(scope.row.damageLevel)">
              {{ getDamageLevelText(scope.row.damageLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="100">
          <template #default="scope">
            {{ scope.row.confidence ? (Number(scope.row.confidence) * 100).toFixed(1) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="damageType" label="破损类型" min-width="120" show-overflow-tooltip />
        <el-table-column
          prop="damageDescription"
          label="破损描述"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="suggestion" label="处理建议" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="380" fixed="right">
          <template #default="scope">
            <!-- 查看详情按钮 - 需要查询检测权限 -->
            <el-button
              v-if="canQueryDetect"
              type="primary"
              size="small"
              :icon="View"
              @click="handleViewDetail(scope.row)"
            >
              查看详情
            </el-button>
            <!-- 修正按钮 - 需要编辑检测权限 -->
            <el-button
              v-if="canEditDetect"
              type="warning"
              size="small"
              :icon="Edit"
              @click="handleEdit(scope.row)"
            >
              修正
            </el-button>
            <!-- 生成报告按钮 - 需要导出检测权限 -->
            <el-button
              v-if="canExportDetect"
              type="success"
              size="small"
              :icon="Files"
              @click="handleGenerateReport(scope.row)"
            >
              生成报告
            </el-button>
            <!-- 删除按钮 - 需要删除检测权限 -->
            <el-button
              v-if="canEditDetect"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="detectionList.length === 0" description="暂无检测数据" />
        <div v-for="row in detectionList" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <div class="card-id">ID: {{ row.id }}</div>
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </div>
            <div class="card-images">
              <div class="img-box">
                <span class="img-label">原始</span>
                <el-image
                  class="card-img"
                  :src="row.imageList?.[0]?.originalUrl || row.imageUrl || row.image_url"
                  :preview-src-list="
                    row.imageList && row.imageList.length > 0
                      ? row.imageList.map((img) => img.originalUrl).filter(Boolean)
                      : [row.imageUrl || row.image_url].filter(Boolean)
                  "
                  fit="cover"
                  preview-teleported
                />
              </div>
              <div class="img-box">
                <span class="img-label">处理后</span>
                <el-image
                  class="card-img"
                  :src="
                    row.imageList?.[0]?.processedUrl ||
                    row.imageList?.[0]?.originalUrl ||
                    row.processedImageUrl ||
                    row.processed_image_url ||
                    row.imageUrl ||
                    row.image_url
                  "
                  :preview-src-list="
                    row.imageList && row.imageList.length > 0
                      ? row.imageList
                          .map((img) => img.processedUrl || img.originalUrl)
                          .filter(Boolean)
                      : [
                          row.processedImageUrl ||
                            row.processed_image_url ||
                            row.imageUrl ||
                            row.image_url,
                        ].filter(Boolean)
                  "
                  fit="cover"
                  preview-teleported
                >
                  <template #error>
                    <div class="image-slot">
                      <span class="no-image-text">无</span>
                    </div>
                  </template>
                </el-image>
              </div>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">检测编号:</span>
                <span class="value">{{ row.detectionNo }}</span>
              </div>
              <div class="content-item">
                <span class="label">运输单号:</span>
                <span class="value">{{ row.transportNo }}</span>
              </div>
              <div class="content-item">
                <span class="label">检测地点:</span>
                <span class="value">{{ row.detectionLocation || row.sceneType || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">责任主体:</span>
                <span class="value">{{ row.responsibilitySubject || row.nodeName || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">货物名称:</span>
                <span class="value">{{ row.goodsName || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">操作员:</span>
                <span class="value">{{ row.operatorName || '系统自动' }}</span>
              </div>
              <div class="content-item">
                <span class="label">破损等级:</span>
                <el-tag :type="getDamageLevelTagType(row.damageLevel)" size="small">
                  {{ getDamageLevelText(row.damageLevel) }}
                </el-tag>
              </div>
              <div class="content-item">
                <span class="label">置信度:</span>
                <span class="value">{{
                  row.confidence ? (Number(row.confidence) * 100).toFixed(1) + '%' : '-'
                }}</span>
              </div>
              <div class="content-item">
                <span class="label">破损类型:</span>
                <span class="value">{{ row.damageType || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">破损描述:</span>
                <span class="value">{{ row.damageDescription || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">处理建议:</span>
                <span class="value">{{ row.suggestion || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">检测时间:</span>
                <span class="value">{{ row.detectionTime }}</span>
              </div>
            </div>
            <div class="card-actions">
              <!-- 详情按钮 - 需要查询检测权限 -->
              <el-button
                v-if="canQueryDetect"
                type="primary"
                size="small"
                :icon="View"
                @click="handleViewDetail(row)"
              >
                详情
              </el-button>
              <!-- 修正按钮 - 需要编辑检测权限 -->
              <el-button
                v-if="canEditDetect"
                type="warning"
                size="small"
                :icon="Edit"
                @click="handleEdit(row)"
              >
                修正
              </el-button>
              <!-- 报告按钮 - 需要导出检测权限 -->
              <el-button
                v-if="canExportDetect"
                type="success"
                size="small"
                :icon="Files"
                @click="handleGenerateReport(row)"
              >
                报告
              </el-button>
              <!-- 删除按钮 - 需要删除检测权限 -->
              <el-button
                v-if="canEditDetect"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <div class="pagination">
        <div v-if="isMobile" class="mobile-pagination">
          <el-button
            size="small"
            :disabled="pagination.currentPage === 1"
            @click="handleCurrentChange(1)"
          >
            首页
          </el-button>
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next"
            small
            @current-change="handleCurrentChange"
          />
          <el-button
            size="small"
            :disabled="pagination.currentPage >= Math.ceil(pagination.total / pagination.pageSize)"
            @click="handleCurrentChange(Math.ceil(pagination.total / pagination.pageSize))"
          >
            末页
          </el-button>
        </div>
        <el-pagination
          v-else
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 移动端筛选抽屉 -->
    <el-drawer
      v-model="filterDrawerVisible"
      title="条件筛选"
      size="80%"
      direction="rtl"
      class="mobile-filter-drawer"
    >
      <el-form :model="searchForm" label-position="top">
        <el-form-item label="检测编号">
          <el-input v-model="searchForm.detectionNo" placeholder="请输入检测编号" clearable />
        </el-form-item>
        <el-form-item label="运输单号">
          <el-input v-model="searchForm.transportNo" placeholder="请输入运输单号" clearable />
        </el-form-item>
        <el-form-item label="检测地点">
          <el-input v-model="searchForm.sceneType" placeholder="请输入检测地点" clearable />
        </el-form-item>
        <el-form-item label="责任主体">
          <el-input v-model="searchForm.nodeName" placeholder="请输入责任主体" clearable />
        </el-form-item>
        <el-form-item label="货物名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入货物名称" clearable />
        </el-form-item>
        <el-form-item label="破损等级">
          <el-select
            v-model="searchForm.damageLevel"
            placeholder="请选择破损等级"
            clearable
            class="w-full"
          >
            <el-option
              v-for="item in damageLevelList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleFilterConfirm">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>

  <!-- 检测详情抽屉 -->
  <el-drawer
    v-model="detailDialogVisible"
    title="检测详情"
    :size="isMobile ? '100%' : '900px'"
    class="detection-detail-drawer"
  >
    <div class="detail-content detail-container" v-if="currentDetection">
      <div class="detail-header-wrapper">
        <el-descriptions :column="isMobile ? 1 : 2" stripe>
          <el-descriptions-item label="检测编号">{{
            currentDetection.detectionNo
          }}</el-descriptions-item>
          <el-descriptions-item label="运输单号">{{
            currentDetection.transportNo
          }}</el-descriptions-item>
          <el-descriptions-item label="检测地点">{{
            currentDetection.detectionLocation || currentDetection.sceneType || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="责任主体">{{
            currentDetection.responsibilitySubject || currentDetection.nodeName || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="货物名称">{{
            currentDetection.goodsName
          }}</el-descriptions-item>
          <el-descriptions-item label="检测时间">{{
            currentDetection.detectionTime
          }}</el-descriptions-item>
          <el-descriptions-item label="操作员">{{
            currentDetection.operatorName || '系统自动'
          }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="splitpane-wrapper">
        <template v-if="!isMobile">
          <splitpanes class="default-theme h-full">
            <pane size="50" min-size="30">
              <div class="pane-content">
                <h3 class="section-title">检测图片</h3>
                <div class="image-container">
                  <div
                    v-for="(img, index) in currentDetection.imageList || []"
                    :key="index"
                    class="img-group"
                  >
                    <div class="img-header">
                      <span class="img-index">图片 {{ index + 1 }}</span>
                      <el-tag :type="getDamageLevelTagType(img.damageLevel)" size="small">
                        {{ getDamageLevelText(img.damageLevel) }}
                      </el-tag>
                    </div>
                    <div class="img-pair">
                      <div class="img-wrapper">
                        <p class="img-label">处理后</p>
                        <el-image
                          :src="img.processedUrl || img.originalUrl"
                          class="detail-image"
                          :preview-src-list="[img.processedUrl || img.originalUrl, img.originalUrl]"
                          fit="contain"
                        />
                      </div>
                      <div class="img-wrapper">
                        <p class="img-label">原始</p>
                        <el-image
                          :src="img.originalUrl"
                          class="detail-image"
                          :preview-src-list="[img.originalUrl, img.processedUrl]"
                          fit="contain"
                        />
                      </div>
                    </div>
                    <div class="img-meta">
                      <span>类型: {{ img.damageType || '未知' }}</span>
                      <span
                        >置信度:
                        {{
                          img.confidence ? (Number(img.confidence) * 100).toFixed(1) + '%' : '-'
                        }}</span
                      >
                      <span v-if="img.damageArea"
                        >破损面积: {{ (Number(img.damageArea) * 100).toFixed(2) + '%' }}</span
                      >
                    </div>
                    <el-divider v-if="index < (currentDetection.imageList?.length || 0) - 1" />
                  </div>
                  <!-- Fallback for old data if imageList is empty but old fields exist -->
                  <div
                    v-if="
                      (!currentDetection.imageList || currentDetection.imageList.length === 0) &&
                      (currentDetection.imageUrl || currentDetection.image_url)
                    "
                    class="img-group"
                  >
                    <div class="img-pair">
                      <div class="img-wrapper">
                        <p class="img-label">处理后</p>
                        <el-image
                          :src="
                            currentDetection.processedImageUrl ||
                            currentDetection.processed_image_url
                          "
                          class="detail-image"
                          :preview-src-list="[
                            currentDetection.processedImageUrl ||
                              currentDetection.processed_image_url,
                          ]"
                          fit="contain"
                        />
                      </div>
                      <div class="img-wrapper">
                        <p class="img-label">原始</p>
                        <el-image
                          :src="currentDetection.imageUrl || currentDetection.image_url"
                          class="detail-image"
                          :preview-src-list="[
                            currentDetection.imageUrl || currentDetection.image_url,
                          ]"
                          fit="contain"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </pane>
            <pane size="50" min-size="30">
              <div class="pane-content">
                <h3 class="section-title">检测结果</h3>
                <div class="info-list">
                  <div class="info-item">
                    <span class="label">破损等级：</span>
                    <el-tag
                      :type="getDamageLevelTagType(currentDetection.damageLevel)"
                      size="large"
                    >
                      {{ getDamageLevelText(currentDetection.damageLevel) }}
                    </el-tag>
                  </div>
                  <div class="info-item">
                    <span class="label">破损面积：</span>
                    <span class="value">{{ currentDetection.damageArea }}%</span>
                  </div>
                  <div class="info-item">
                    <span class="label">破损类型：</span>
                    <span class="value">{{ currentDetection.damageType || '暂无' }}</span>
                  </div>
                  <div class="info-item full-width">
                    <span class="label">破损描述：</span>
                    <div class="text-box">
                      {{ currentDetection.damageDescription || '暂无描述' }}
                    </div>
                  </div>
                  <div class="info-item full-width">
                    <span class="label">处理建议：</span>
                    <div class="text-box suggestion-box">
                      {{ currentDetection.suggestion || '暂无建议' }}
                    </div>
                  </div>
                  <div class="info-item">
                    <span class="label">置信度：</span>
                    <span class="value">
                      {{
                        currentDetection.confidence
                          ? (Number(currentDetection.confidence) * 100).toFixed(1) + '%'
                          : '-'
                      }}
                    </span>
                  </div>
                </div>
              </div>
            </pane>
          </splitpanes>
        </template>
        <div v-else class="mobile-detail-content">
          <div class="mobile-section">
            <h3 class="section-title">检测图片</h3>
            <div class="image-container">
              <div
                v-for="(img, index) in currentDetection.imageList || []"
                :key="index"
                class="img-group"
              >
                <div class="img-header">
                  <span class="img-index">图片 {{ index + 1 }}</span>
                  <el-tag :type="getDamageLevelTagType(img.damageLevel)" size="small">
                    {{ getDamageLevelText(img.damageLevel) }}
                  </el-tag>
                </div>
                <div class="img-pair">
                  <div class="img-wrapper">
                    <p class="img-label">处理后</p>
                    <el-image
                      :src="img.processedUrl || img.originalUrl"
                      class="detail-image"
                      :preview-src-list="[img.processedUrl || img.originalUrl, img.originalUrl]"
                      fit="contain"
                    />
                  </div>
                  <div class="img-wrapper">
                    <p class="img-label">原始</p>
                    <el-image
                      :src="img.originalUrl"
                      class="detail-image"
                      :preview-src-list="[img.originalUrl, img.processedUrl]"
                      fit="contain"
                    />
                  </div>
                </div>
                <div class="img-meta">
                  <div class="meta-item">
                    <span class="label">类型:</span>
                    <span class="value">{{ img.damageType || '未知' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="label">置信度:</span>
                    <span class="value">
                      {{ img.confidence ? (Number(img.confidence) * 100).toFixed(1) + '%' : '-' }}
                    </span>
                  </div>
                </div>
                <el-divider v-if="index < (currentDetection.imageList?.length || 0) - 1" />
              </div>

              <!-- Fallback for old data if imageList is empty but old fields exist -->
              <div
                v-if="
                  (!currentDetection.imageList || currentDetection.imageList.length === 0) &&
                  (currentDetection.imageUrl || currentDetection.image_url)
                "
                class="img-group"
              >
                <div class="img-pair">
                  <div class="img-wrapper">
                    <p class="img-label">处理后</p>
                    <el-image
                      :src="
                        currentDetection.processedImageUrl ||
                        currentDetection.processed_image_url ||
                        currentDetection.imageUrl ||
                        currentDetection.image_url
                      "
                      class="detail-image"
                      :preview-src-list="[
                        currentDetection.processedImageUrl ||
                          currentDetection.processed_image_url ||
                          currentDetection.imageUrl ||
                          currentDetection.image_url,
                      ]"
                      fit="contain"
                    />
                  </div>
                  <div class="img-wrapper">
                    <p class="img-label">原始</p>
                    <el-image
                      :src="currentDetection.imageUrl || currentDetection.image_url"
                      class="detail-image"
                      :preview-src-list="[currentDetection.imageUrl || currentDetection.image_url]"
                      fit="contain"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="mobile-section">
            <h3 class="section-title">检测结果</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="label">破损等级：</span>
                <el-tag :type="getDamageLevelTagType(currentDetection.damageLevel)" size="small">
                  {{ getDamageLevelText(currentDetection.damageLevel) }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">破损面积：</span>
                <span class="value">{{ currentDetection.damageArea }}%</span>
              </div>
              <div class="info-item">
                <span class="label">破损类型：</span>
                <span class="value">{{ currentDetection.damageType || '暂无' }}</span>
              </div>
              <div class="info-item full-width">
                <span class="label">破损描述：</span>
                <div class="text-box">
                  {{ currentDetection.damageDescription || '暂无描述' }}
                </div>
              </div>
              <div class="info-item full-width">
                <span class="label">处理建议：</span>
                <div class="text-box suggestion-box">
                  {{ currentDetection.suggestion || '暂无建议' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex-auto">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <!-- 生成报告按钮 - 需要导出检测权限 -->
        <el-button
          v-if="canExportDetect"
          type="primary"
          @click="handleGenerateReport(currentDetection)"
        >
          生成报告
        </el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 修正抽屉 -->
  <el-drawer v-model="editDialogVisible" title="修正检测结果" :size="isMobile ? '100%' : '900px'">
    <div class="edit-drawer-content" v-if="editForm.id">
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="破损等级" prop="damageLevel">
              <el-select v-model="editForm.damageLevel" class="w-full">
                <el-option
                  v-for="item in damageLevelList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="破损类型" prop="damageType">
              <el-input v-model="editForm.damageType" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="破损描述" prop="damageDescription">
          <el-input v-model="editForm.damageDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处理建议" prop="suggestion">
          <el-input
            v-model="editForm.suggestion"
            type="textarea"
            :rows="2"
            placeholder="请输入处理建议..."
          />
        </el-form-item>
      </el-form>

      <el-divider>图片标注修正</el-divider>

      <div class="edit-image-list">
        <div v-for="(img, index) in editForm.imageList" :key="index" class="edit-image-item">
          <div class="image-info-bar">
            <span class="img-name">图片 {{ index + 1 }}</span>
            <el-tag :type="getDamageLevelTagType(img.damageLevel)" size="small">
              {{ getDamageLevelText(img.damageLevel) }}
            </el-tag>
          </div>

          <!-- 内联标注区域 -->
          <div
            v-if="currentEditImage && currentEditImage.id === img.id"
            class="inline-annotation-container"
          >
            <div class="annotation-header">
              <div class="header-tips">
                <el-icon><info-filled /></el-icon>
                <span>操作提示：拖拽鼠标绘制红框，绿色为 AI 原始框。</span>
              </div>
              <div class="header-btns">
                <el-button size="small" @click="undoAnnotation">撤销</el-button>
                <el-button size="small" type="danger" @click="clearAllAnnotations">清空</el-button>
                <el-button size="small" type="primary" @click="saveAnnotation">保存标注</el-button>
                <el-button size="small" @click="currentEditImage = null">取消</el-button>
              </div>
            </div>
            <div class="annotation-body">
              <div class="canvas-container">
                <img
                  ref="annotationImgRef"
                  :src="img.originalUrl"
                  @load="initCanvas"
                  class="annotation-img"
                  crossorigin="anonymous"
                />
                <canvas
                  ref="annotationCanvasRef"
                  @mousedown="startDrawing"
                  @mousemove="draw"
                  @mouseup="stopDrawing"
                  @mouseleave="stopDrawing"
                  @touchstart="startDrawing"
                  @touchmove="draw"
                  @touchend="stopDrawing"
                  class="annotation-canvas"
                ></canvas>
              </div>
            </div>
          </div>

          <!-- 默认预览区域 -->
          <div v-else class="image-previews">
            <div class="preview-box clickable" @click="handleImageCorrection(img)">
              <p class="preview-label">处理后 (点击更正标注)</p>
              <div class="img-with-overlay">
                <el-image :src="img.processedUrl" fit="contain" class="preview-img" />
                <div class="image-edit-overlay">
                  <el-icon><edit /></el-icon>
                  <span>点击更正标注</span>
                </div>
              </div>
            </div>
            <div class="preview-box">
              <p class="preview-label">原始图</p>
              <el-image :src="img.originalUrl" fit="contain" class="preview-img" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editing">确定提交</el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 批量检测抽屉 -->
  <el-drawer
    v-model="batchDialogVisible"
    title="新建检测记录"
    :size="isMobile ? '100%' : '640px'"
    :close-on-click-modal="false"
    class="batch-detection-drawer"
  >
    <div class="dialog-scroll-container">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="运输单号">
          <el-select
            v-model="batchForm.transportId"
            placeholder="请选择运输单"
            filterable
            clearable
            class="w-full"
          >
            <el-option
              v-for="item in transportList"
              :key="item.id"
              :label="`${item.transportNo} - ${item.goodsName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图片上传">
          <div class="upload-wrapper">
            <el-upload
              ref="batchUploadRef"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleBatchFileChange"
              :on-remove="handleBatchFileRemove"
              :file-list="batchFileList"
              multiple
              accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="upload-tip">支持多张图片上传，建议单次不超过 20 张</div>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchUpload" :loading="batchUploading">
          开始检测
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'

const route = useRoute()
import {
  Download,
  Search,
  Refresh,
  Plus,
  View,
  Edit,
  Files,
  Delete,
  InfoFilled,
  Filter,
} from '@element-plus/icons-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  getDetections,
  getDetectionDetail,
  detectBatchImages,
  exportDetections,
  exportSelectedDetections,
  generateDetectionReport,
  updateDetection,
  updateDetectionImage,
  deleteDetection,
} from '@/api/detection'
import { getTransports } from '@/api/transport'
import { getDamageLevelDict, getDetectionStatusDict } from '@/api/common'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const loading = ref(false)
const damageLevelList = ref([])

// 响应式处理
const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchDamageLevelDict()
  fetchTransportList()
  handleSearch()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
  handleSearch()
}
const canAddDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_ADD))
const canEditDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_EDIT))
const canExportDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_EXCELS))
const canExportSelectedDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_EXCEL))
const canQueryDetect = computed(() => hasPermission(perms.value, PERMISSIONS.DETECT_QUERY))
const canResetDetect = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.DETECT_RESET) ||
    hasPermission(perms.value, PERMISSIONS.DETECT_QUERY),
)

// 修正相关
const editDialogVisible = ref(false)
const editing = ref(false)
const editForm = reactive({
  id: '',
  damageLevel: 0,
  damageType: '',
  damageDescription: '',
  suggestion: '',
  imageList: [],
})

// 标注相关状态
const annotationImgRef = ref(null)
const annotationCanvasRef = ref(null)
const isDrawing = ref(false)
const startX = ref(0)
const startY = ref(0)
const manualBoxes = ref([]) // 数组：存储多个标注框 {x, y, w, h}
const currentEditImage = ref(null) // 当前正在编辑的图片对象

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.damageLevel = row.damageLevel
  editForm.damageType = row.damageType
  editForm.damageDescription = row.damageDescription
  editForm.suggestion = row.suggestion || ''
  editForm.imageList = row.imageList ? JSON.parse(JSON.stringify(row.imageList)) : []
  editDialogVisible.value = true
  currentEditImage.value = null
}

const handleImageCorrection = (img) => {
  currentEditImage.value = img
  manualBoxes.value = []
  if (img.boundingBoxes) {
    try {
      const existingBoxes = JSON.parse(img.boundingBoxes)
      manualBoxes.value = existingBoxes.map((box) => ({
        x: box.location.left,
        y: box.location.top,
        w: box.location.width,
        h: box.location.height,
        name: box.name,
        score: box.score,
        isOriginal: true,
      }))
    } catch (e) {
      console.error('解析现有边框失败', e)
    }
  }
  nextTick(() => {
    // 强制尝试初始化一次，以防图片已加载
    initCanvas()
  })
}

const initCanvas = () => {
  // 处理 Vue 3 在 v-for 中 ref 可能是数组的情况
  const img = Array.isArray(annotationImgRef.value)
    ? annotationImgRef.value[0]
    : annotationImgRef.value
  const canvas = Array.isArray(annotationCanvasRef.value)
    ? annotationCanvasRef.value[0]
    : annotationCanvasRef.value

  if (!img || !canvas) return

  // 确保图片已加载且有尺寸
  if (img.width === 0 || img.height === 0) {
    // 如果还没尺寸，等一会再试
    setTimeout(initCanvas, 100)
    return
  }

  canvas.width = img.width
  canvas.height = img.height

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

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

const getCoordinates = (e) => {
  let clientX, clientY
  if (e.touches && e.touches.length > 0) {
    clientX = e.touches[0].clientX
    clientY = e.touches[0].clientY
  } else if (e.changedTouches && e.changedTouches.length > 0) {
    clientX = e.changedTouches[0].clientX
    clientY = e.changedTouches[0].clientY
  } else {
    clientX = e.clientX
    clientY = e.clientY
  }
  return { clientX, clientY }
}

const startDrawing = (e) => {
  // 阻止默认行为，防止移动端滚动
  if (e.type === 'touchstart') e.preventDefault()

  isDrawing.value = true
  const canvas = Array.isArray(annotationCanvasRef.value)
    ? annotationCanvasRef.value[0]
    : annotationCanvasRef.value
  const rect = canvas.getBoundingClientRect()
  const { clientX, clientY } = getCoordinates(e)
  startX.value = clientX - rect.left
  startY.value = clientY - rect.top
}

const draw = (e) => {
  if (!isDrawing.value) return
  if (e.type === 'touchmove') e.preventDefault()

  const canvas = Array.isArray(annotationCanvasRef.value)
    ? annotationCanvasRef.value[0]
    : annotationCanvasRef.value
  const img = Array.isArray(annotationImgRef.value)
    ? annotationImgRef.value[0]
    : annotationImgRef.value
  if (!canvas || !img) return

  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()

  const { clientX, clientY } = getCoordinates(e)
  const currentX = clientX - rect.left
  const currentY = clientY - rect.top

  const width = currentX - startX.value
  const height = currentY - startY.value

  const scaleX = img.width / img.naturalWidth
  const scaleY = img.height / img.naturalHeight

  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.lineWidth = 2

  manualBoxes.value.forEach((box) => {
    ctx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C'
    ctx.strokeRect(box.x * scaleX, box.y * scaleY, box.w * scaleX, box.h * scaleY)
  })

  ctx.strokeStyle = '#409EFF'
  ctx.setLineDash([5, 5])
  ctx.strokeRect(startX.value, startY.value, width, height)
  ctx.setLineDash([])
}

const stopDrawing = (e) => {
  if (!isDrawing.value) return
  isDrawing.value = false

  const canvas = Array.isArray(annotationCanvasRef.value)
    ? annotationCanvasRef.value[0]
    : annotationCanvasRef.value
  const img = Array.isArray(annotationImgRef.value)
    ? annotationImgRef.value[0]
    : annotationImgRef.value
  if (!canvas || !img) return

  const rect = canvas.getBoundingClientRect()
  const { clientX, clientY } = getCoordinates(e)
  const endX = clientX - rect.left
  const endY = clientY - rect.top

  const scaleX = img.naturalWidth / img.width
  const scaleY = img.naturalHeight / img.height

  const displayW = Math.abs(endX - startX.value)
  const displayH = Math.abs(endY - startY.value)

  if (displayW > 5 && displayH > 5) {
    const newBox = {
      x: Math.min(startX.value, endX) * scaleX,
      y: Math.min(startY.value, endY) * scaleY,
      w: displayW * scaleX,
      h: displayH * scaleY,
      name: currentEditImage.value.damageType || '手动标注',
      score: 1.0,
      isOriginal: false,
    }
    manualBoxes.value.push(newBox)
  }

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

const undoAnnotation = () => {
  if (manualBoxes.value.length > 0) {
    manualBoxes.value.pop()
    initCanvas()
  }
}

const clearAllAnnotations = () => {
  manualBoxes.value = []
  initCanvas()
}

const saveAnnotation = () => {
  const img = Array.isArray(annotationImgRef.value)
    ? annotationImgRef.value[0]
    : annotationImgRef.value
  const canvas = Array.isArray(annotationCanvasRef.value)
    ? annotationCanvasRef.value[0]
    : annotationCanvasRef.value

  if (!currentEditImage.value || !canvas || !img) return

  // 1. 保存坐标信息
  if (manualBoxes.value.length > 0) {
    const formattedBoxes = manualBoxes.value.map((box) => ({
      name: box.name || '破损',
      score: box.score || 1.0,
      location: {
        left: Math.round(box.x),
        top: Math.round(box.y),
        width: Math.round(box.w),
        height: Math.round(box.h),
      },
    }))
    currentEditImage.value.boundingBoxes = JSON.stringify(formattedBoxes)
  } else {
    currentEditImage.value.boundingBoxes = null
  }

  // 2. 将标注后的 Canvas 转换为 Base64 图片，用于后端直接保存
  // 注意：这里需要创建一个临时的 Canvas，先把原始图片画上去，再画标注框，以获得一张完整的标注图
  const tempCanvas = document.createElement('canvas')
  tempCanvas.width = img.naturalWidth
  tempCanvas.height = img.naturalHeight
  const tempCtx = tempCanvas.getContext('2d')

  // 画原始图
  tempCtx.drawImage(img, 0, 0)

  // 画标注框
  tempCtx.lineWidth = Math.max(2, (img.naturalWidth / 500) * 2) // 根据图片大小调整线宽
  manualBoxes.value.forEach((box) => {
    tempCtx.strokeStyle = box.isOriginal ? '#67C23A' : '#F56C6C'
    tempCtx.strokeRect(box.x, box.y, box.w, box.h)
  })

  // 保存 Base64 并更新预览
  const base64Data = tempCanvas.toDataURL('image/jpeg', 0.9)
  currentEditImage.value.base64Image = base64Data
  currentEditImage.value.processedUrl = base64Data // 临时更新预览图，让用户能立刻看到效果

  console.log('标注已保存为 Base64', {
    imageId: currentEditImage.value.id,
    boxesCount: manualBoxes.value.length,
    base64Length: base64Data.length,
  })

  currentEditImage.value = null
  ElMessage.success('图片标注已暂存，请点击确定提交更新')
}

const submitEdit = async () => {
  editing.value = true
  try {
    // 1. 处理图片标注的更正
    // 找出所有被修改过标注的图片并提交
    const originalDetection = detectionList.value.find((d) => d.id === editForm.id)
    if (originalDetection && originalDetection.imageList) {
      for (const img of editForm.imageList) {
        const originalImg = originalDetection.imageList.find((oi) => oi.id === img.id)
        // 如果标注框、破损等级或破损类型发生了变化，或者有新的 Base64 数据
        const isBoundingBoxesChanged =
          img.boundingBoxes !== (originalImg ? originalImg.boundingBoxes : null)
        const isLevelChanged = img.damageLevel !== (originalImg ? originalImg.damageLevel : null)
        const isTypeChanged = img.damageType !== (originalImg ? originalImg.damageType : null)
        const hasNewBase64 = !!img.base64Image

        if (isBoundingBoxesChanged || isLevelChanged || isTypeChanged || hasNewBase64) {
          await updateDetectionImage({
            id: img.id,
            boundingBoxes: img.boundingBoxes,
            base64Image: img.base64Image,
            damageLevel: img.damageLevel,
            damageType: img.damageType,
          })
        }
      }
    }

    // 2. 提交主记录的更正
    await updateDetection(editForm.id, {
      damageLevel: editForm.damageLevel,
      damageType: editForm.damageType,
      damageDescription: editForm.damageDescription,
      suggestion: editForm.suggestion,
    })

    ElMessage.success('修正成功')
    editDialogVisible.value = false
    fetchDetectionList()
  } catch (error) {
    console.error('修正失败', error)
    ElMessage.error(error.message || '修正失败')
  } finally {
    editing.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条检测记录吗？该操作将同时删除服务器上的图片文件，且无法撤销。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
    .then(async () => {
      try {
        await deleteDetection(row.id)
        ElMessage.success('删除成功')
        fetchDetectionList()
      } catch (error) {
        console.error('删除失败', error)
        ElMessage.error(error.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 批量检测相关
const batchDialogVisible = ref(false)
const batchUploading = ref(false)
const batchForm = reactive({
  transportId: '',
})
const batchFileList = ref([])
const batchUploadRef = ref(null)

const handleBatchDetection = () => {
  batchForm.transportId = ''
  batchFileList.value = []
  batchDialogVisible.value = true
}

const handleBatchFileChange = (uploadFile, uploadFiles) => {
  batchFileList.value = uploadFiles
}

const handleBatchFileRemove = (uploadFile, uploadFiles) => {
  batchFileList.value = uploadFiles
}

const submitBatchUpload = async () => {
  if (batchFileList.value.length === 0) {
    ElMessage.warning('请至少选择一张图片')
    return
  }

  // 限制数量提示
  if (batchFileList.value.length > 20) {
    ElMessage.warning('建议单次上传不超过20张图片')
  }

  batchUploading.value = true
  try {
    const files = batchFileList.value.map((f) => f.raw)
    const data = {}
    if (batchForm.transportId) {
      data.transportId = batchForm.transportId
    }

    const res = await detectBatchImages(files, data)

    ElMessage.success('批量检测完成')
    batchDialogVisible.value = false

    // 刷新列表
    fetchDetectionList()

    // 如果返回了结果，可以考虑直接显示详情，或者提示用户
    if (res && res.id) {
      ElNotification({
        title: '检测完成',
        message: `检测编号: ${res.detectionNo}, 破损等级: ${getDamageLevelText(res.damageLevel)}`,
        type: 'success',
        duration: 5000,
      })
    }
  } catch (error) {
    console.error('批量检测失败:', error)
    ElMessage.error(error.message || '批量检测失败')
  } finally {
    batchUploading.value = false
  }
}

// 搜索表单
const searchForm = reactive({
  detectionNo: '',
  transportNo: '',
  goodsName: '',
  damageLevel: '',
  dateRange: [],
})

// 检测列表数据
const detectionList = ref([])

// 运输列表 (用于新建检测时选择)
const transportList = ref([])

// 获取破损等级字典
const fetchDamageLevelDict = async () => {
  try {
    const res = await getDamageLevelDict()
    if (res) {
      damageLevelList.value = res
    }
  } catch (error) {
    console.error('获取破损等级字典失败:', error)
  }
}

// 获取运输列表
const fetchTransportList = async () => {
  try {
    const res = await getTransports({ page: 1, size: 1000 })
    if (res.list || res.records) {
      transportList.value = res.list || res.records
    } else if (Array.isArray(res)) {
      transportList.value = res
    }
  } catch (error) {
    console.error('获取运输列表失败:', error)
  }
}

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

// 获取检测列表
const fetchDetectionList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      detectionNo: searchForm.detectionNo || undefined,
      transportNo: searchForm.transportNo || undefined,
      goodsName: searchForm.goodsName || undefined,
      damageLevel: searchForm.damageLevel !== '' ? searchForm.damageLevel : undefined,
      startTime: searchForm.dateRange?.[0] ? formatDateTime(searchForm.dateRange[0]) : undefined,
      endTime: searchForm.dateRange?.[1] ? formatDateTime(searchForm.dateRange[1]) : undefined,
    }
    const res = await getDetections(params)
    // 后端返回结构分析:
    // request.js 拦截器已处理 Result wrapper，res 即为 Result.data
    // 若为 PageInfo，包含 list/records 和 total
    if (res.list || res.records) {
      detectionList.value = res.list || res.records
      pagination.total = Number(res.total)
    } else if (Array.isArray(res)) {
      detectionList.value = res
      pagination.total = res.length
    } else {
      detectionList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取检测列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 详情查看
const detailDialogVisible = ref(false)
const currentDetection = ref(null)

// 获取破损等级文本
const getDamageLevelText = (level) => {
  const item = damageLevelList.value.find((item) => item.value === Number(level))
  return item ? item.label : '未知'
}

// 获取破损等级标签类型
const getDamageLevelTagType = (level) => {
  const item = damageLevelList.value.find((item) => item.value === Number(level))
  return item ? item.type : 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '待确认',
    1: '已确认',
    2: '误报',
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
  }
  return map[status] || 'info'
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchDetectionList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchDetectionList()
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  fetchDetectionList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = ''
  })
  searchForm.dateRange = []
  pagination.currentPage = 1
  fetchDetectionList()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    // 如果需要，获取完整详情，或者直接使用行数据
    const detail = await getDetectionDetail(row.id)
    currentDetection.value = detail || row
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败', error)
    currentDetection.value = row
    detailDialogVisible.value = true
  }
}

// 下载 Blob 文件辅助函数
const downloadBlob = (blob, filename) => {
  if (!blob) return
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

// 生成报告
const handleGenerateReport = async (row) => {
  ElNotification({
    title: '报告生成中',
    message: `正在生成检测报告：${row.detectionNo}`,
    type: 'info',
    duration: 2000,
  })

  try {
    const blob = await generateDetectionReport(row.id)
    downloadBlob(blob, `检测报告_${row.detectionNo}.pdf`)
    ElMessage.success('检测报告已生成并下载')
  } catch (error) {
    console.error('生成报告失败', error)
    ElMessage.error('生成报告失败')
  }
}

// 选中的行
const selectedRows = ref([])

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 导出选中报告
const handleExportSelected = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的记录')
    return
  }

  ElNotification({
    title: '导出中',
    message: `正在导出 ${selectedRows.value.length} 条选中记录`,
    type: 'info',
    duration: 2000,
  })

  try {
    const ids = selectedRows.value.map((row) => row.id)
    const blob = await exportSelectedDetections(ids)
    downloadBlob(blob, `选中检测记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success('选中记录已导出')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  }
}

// 导出报告
const handleExport = async () => {
  ElNotification({
    title: '导出中',
    message: '正在导出检测记录',
    type: 'info',
    duration: 2000,
  })

  try {
    const params = {
      detectionNo: searchForm.detectionNo || undefined,
      transportNo: searchForm.transportNo || undefined,
      goodsName: searchForm.goodsName || undefined,
      damageLevel: searchForm.damageLevel || undefined,
    }
    const blob = await exportDetections(params)
    downloadBlob(blob, `检测记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success('检测记录已导出')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  }
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  // 检查是否已经是字符串
  if (typeof date === 'string') return date

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}
</script>

<style scoped>
.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar-container {
  margin-bottom: 15px;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.mobile-search-bar {
  display: flex;
  gap: 10px;
  padding: 0 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 移动端卡片样式增强 */

.card-id {
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.card-images {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.img-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.img-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.card-img {
  width: 100%;
  height: 100px;
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
}

/* 通用宽度类 */
.w-120 {
  width: 120px;
}
.w-150 {
  width: 150px;
}
.w-180 {
  width: 180px;
}
.w-200 {
  width: 200px;
}
.w-260 {
  width: 260px;
}
.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.flex-auto {
  flex: auto;
}

.detection-table {
  width: 100%;
  flex: 1;
}

.table-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 20px;
}

.no-image-text {
  font-size: 14px;
}

@media screen and (max-width: 768px) {
  .detection-management-container {
    height: auto;
  }

  :deep(.el-card) {
    border: none;
    background: transparent;
  }

  :deep(.el-card__header) {
    display: none;
  }

  :deep(.el-card__body) {
    padding: 10px 5px;
  }

  .search-bar-container {
    padding: 10px;
    background: #fff;
    margin: -10px -5px 15px -5px;
    border-bottom: 1px solid #f0f0f0;
    position: static; /* 移动端不需要 sticky 或者有特殊处理 */
  }

  .pagination {
    margin-bottom: 20px;
  }
}

/* PC端样式补充 - 布局样式已提取到全局 */

/* 修正抽屉特定样式 */
.edit-drawer-content {
  padding: 0 10px;
}

.edit-image-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 10px;
}

.edit-image-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 15px;
  background-color: var(--el-fill-color-blank);
}

.image-info-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.img-name {
  font-weight: bold;
  margin-right: 12px;
}

.image-previews {
  display: flex;
  gap: 15px;
}

.preview-box {
  flex: 1;
  text-align: center;
}

.preview-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 5px;
}

.preview-img {
  width: 100%;
  height: 180px;
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
}

/* 标注样式 */
.inline-annotation-container {
  border: 1px solid var(--el-color-primary-light-5);
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--el-fill-color-blank);
  margin-bottom: 15px;
}

.header-tips {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.clickable {
  cursor: pointer;
}

.img-with-overlay {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 4px;
  overflow: hidden;
}

.image-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.img-with-overlay:hover .image-edit-overlay {
  opacity: 1;
}

.image-edit-overlay .el-icon {
  font-size: 24px;
}

.annotation-header {
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: var(--el-fill-color-light);
}

.annotation-body {
  padding: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.canvas-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--el-border-color-lighter);
}

.annotation-img {
  display: block;
  max-width: 100%;
  max-height: 500px;
}

.annotation-canvas {
  position: absolute;
  top: 0;
  left: 0;
  cursor: crosshair;
}

.ml-10 {
  margin-left: 10px;
}

/* 详情抽屉样式 */
.detail-content {
  padding: 10px;
}

.detail-header-wrapper {
  margin-bottom: 20px;
}

.pane-content {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  background-color: transparent;
  /* 适配主题背景 */
}

.image-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.img-group {
  margin-bottom: 20px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  padding-bottom: 20px;
}

.img-group:last-child {
  border-bottom: none;
}

.img-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.img-index {
  font-weight: bold;
  font-size: 14px;
}

.img-pair {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.img-meta {
  display: flex;
  gap: 20px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.img-wrapper {
  flex: 1;
  width: 0; /* 防止 flex item 溢出 */
  text-align: center;
  padding: 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background-color: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
}

.img-label {
  margin-bottom: 10px;
  font-weight: bold;
  color: var(--el-text-color-regular);
}

.detail-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

/* 结果信息列表 */
.info-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  width: 45%;
  display: flex;
  align-items: center;
}

.info-item.full-width {
  width: 100%;
  display: block;
}

.label {
  font-weight: bold;
  color: var(--el-text-color-regular);
  min-width: 80px;
}

.value {
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.text-box {
  margin-top: 8px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  color: var(--el-text-color-primary);
  line-height: 1.6;
  font-size: 14px;
  border: 1px solid var(--el-border-color-lighter);
}

.suggestion-box {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  border-color: var(--el-color-success-light-5);
}

.uploaded-image {
  max-width: 100%;
  max-height: 200px;
  display: block;
  margin: 0 auto;
}

/* 批量检测抽屉布局优化 */
.batch-detection-drawer :deep(.el-drawer__body) {
  padding: 10px 20px;
}

.dialog-scroll-container {
  padding-right: 0;
}

.upload-wrapper {
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background-color: var(--el-fill-color-blank);
  min-height: 150px;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
  line-height: 1.4;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  background-color: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color-darker);
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
}

/* 通用样式 */
.w-200 {
  width: 200px;
}

.w-300 {
  width: 300px;
}

.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.flex-auto {
  flex: auto;
}

.flex-col {
  display: flex;
  flex-direction: column;
}

.detection-table {
  width: 100%;
  flex: 1;
}

.table-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 20px;
}

.no-image-text {
  font-size: 14px;
}

/* 详情页特定样式 */
.detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.splitpane-wrapper {
  flex: 1;
  overflow: hidden;
  margin-top: 20px;
}

@media screen and (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .w-200,
  .w-300 {
    width: 100% !important;
  }

  .search-bar .el-button {
    width: 100%;
    margin-left: 0 !important;
    margin-bottom: 8px;
  }

  /* 移除搜索栏中日期选择器的固定宽度 */
  .search-bar .el-date-editor {
    width: 100% !important;
  }

  .img-pair {
    flex-direction: column;
  }

  .info-item {
    width: 100%;
  }

  :deep(.el-card__body) {
    padding: 10px;
  }

  /* 分页器在手机端居中并换行 */
  .pagination {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
