<template>
  <div class="resources-container">
    <el-card class="search-card">
      <div class="page-header">
        <h2 class="page-title">资料库</h2>
        <div class="header-buttons">
          <el-button type="success" @click="goToMyResources">我的资料</el-button>
          <el-button type="success" @click="uploadResource">上传资料</el-button>
        </div>
      </div>
      <div class="search-header">
        <el-input v-model="searchQuery" placeholder="搜索资料..." class="search-input" clearable>
          <template #prefix>
            <el-icon>
              <search />
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 资料分类 -->
      <div class="category-tags">
        <el-tag v-for="tag in categories" :key="tag.value" :type="selectedCategory === tag.value ? '' : 'info'"
          class="category-tag" @click="selectCategory(tag.value)">
          {{ tag.label }}
        </el-tag>
      </div>
    </el-card>

    <!-- 资料列表 -->
    <div class="resources-list">
      <el-empty v-if="resources.length === 0" description="暂无资料" />
      <el-card v-for="resource in resources" :key="resource.id" class="resource-item">
        <div class="resource-content" @click="showResourceDetail(resource)">
          <div class="resource-header">
            <div class="resource-title">
              <el-icon>
                <document />
              </el-icon>
              <h4>{{ resource.title }}</h4>
            </div>
            <el-tag size="small">{{ resource.categoryLabel }}</el-tag>
          </div>
          <p class="resource-description">{{ resource.description }}</p>
          <div class="resource-info">
            <span>上传者：{{ resource.uploader }}</span>
            <span>上传时间：{{ formatTime(resource.uploadTime) }}</span>
          </div>
        </div>
        <div class="resource-footer">
          <el-button type="primary" @click.stop="downloadResource(resource)">下载</el-button>
        </div>
      </el-card>
    </div>

    <!-- 资料详情浮窗 -->
    <el-dialog v-model="detailDialogVisible" :title="currentResource.title" width="60%" class="resource-detail-dialog">
      <div class="resource-detail-content">
        <div class="detail-section">
          <h4>资料简介</h4>
          <p>{{ currentResource.description }}</p>
        </div>

        <div class="detail-section" v-if="currentResource.images && currentResource.images.length">
          <h4>资料图片</h4>
          <div class="image-preview-list">
            <div v-for="(image, index) in currentResource.images" :key="index" class="image-preview-item"
              @click="showImagePreview(image)">
              <el-image :src="image" fit="cover" :preview-src-list="[image]" :initial-index="0" preview-teleported />
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentResource.attachments && currentResource.attachments.length">
          <h4>附件列表</h4>
          <div class="attachment-list">
            <div v-for="(attachment, index) in currentResource.attachments" :key="index" class="attachment-item">
              <span class="attachment-name">
                <el-icon>
                  <document />
                </el-icon>
                {{ attachment.name }}
              </span>
              <el-button type="primary" size="small" @click.stop="downloadAttachment(attachment)">
                下载
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadResource(currentResource)">下载资料</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 上传资料对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传资料" width="50%" class="upload-dialog">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="资料名称" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入资料名称" />
        </el-form-item>

        <el-form-item label="资料分类" prop="category">
          <el-select v-model="uploadForm.category" placeholder="请选择资料分类">
            <el-option v-for="item in categories.filter(c => c.value !== '')" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="资料简介">
          <el-input v-model="uploadForm.description" type="textarea" :rows="4" placeholder="请输入资料简介（选填）" />
        </el-form-item>

        <el-form-item label="上传图片">
          <el-upload v-model:file-list="uploadForm.imageList" action="#" list-type="picture-card" :auto-upload="false"
            :on-change="handleImageChange" :on-remove="handleImageRemove">
            <el-icon>
              <plus />
            </el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="上传附件">
          <el-upload v-model:file-list="uploadForm.attachmentList" action="#" :auto-upload="false"
            :on-change="handleAttachmentChange" :on-remove="handleAttachmentRemove">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持任意格式文件上传
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Document, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage,ElLoading } from 'element-plus'
import { downloadFromUrl } from '@/utils/download'
import { resourceApi } from '@/api/resource'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
const token = ref(authStore.token || '')

const router = useRouter()
const searchQuery = ref('')
const selectedCategory = ref('')
const detailDialogVisible = ref(false)
const currentResource = ref({})
const uploadDialogVisible = ref(false)
const uploadFormRef = ref(null)
const uploadForm = ref({
  title: '',
  category: '',
  description: '',
  imageList: [],
  attachmentList: []
})

const categories = [
  { label: '全部', value: '' },
  { label: '考试资料', value: 'exam' },
  { label: '课程笔记', value: 'notes' },
  { label: '实验报告', value: 'lab' },
  { label: '课程作业', value: 'homework' },
  { label: '其他资料', value: 'other' }
]

// 添加原始数据列表
const originalResources = ref([])

// 获取资源列表
const fetchResources = async () => {
  try {
    // 调用API获取笔记列表
    const response = await resourceApi.getAllNotes()
    // 处理返回的笔记数据，映射为我们需要的资源格式
    if (response.data && response.data.notes) {
      originalResources.value = response.data.notes.map(note => ({
        id: note.id,
        title: note.title,
        category: note.category,
        categoryLabel: getCategoryLabelFromContent(note.category),
        description: note.content,
        uploader: note.user ? note.user.username : '未知用户',
        uploadTime: note.updateTime,
        downloads: 123, // 暂时占位
        images: note.imageUrls || [],
        attachments: (note.attachmentUrls || []).map(url => ({
          name: getFileNameFromUrl(url),
          url: url
        }))
      }))
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
    ElMessage.error('获取资源列表失败')
  }
}

// 获取分类标签
const getCategoryLabelFromContent = (category) => {
  const categoryItem = categories.find(item => item.value === category)
  return categoryItem ? categoryItem.label : '其他资料'
}

const getFileNameFromUrl = (url) => {
  if (!url) return '未命名文件';
  const fullName = url.split('/').pop();
  const nameParts = fullName.split('_');
  if (nameParts.length < 2) {
    return fullName;
  }
  return nameParts.slice(1).join('_');
};

// 添加计算属性用于过滤后的资源列表
const resources = computed(() => {
  let filteredResources = [...originalResources.value]

  // 根据分类筛选
  if (selectedCategory.value) {
    filteredResources = filteredResources.filter(
      resource => resource.category === selectedCategory.value
    )
  }

  // 根据搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filteredResources = filteredResources.filter(resource =>
      resource.title.toLowerCase().includes(query) ||
      resource.description.toLowerCase().includes(query) ||
      resource.uploader.toLowerCase().includes(query)
    )
  }

  return filteredResources
})

const handleSearch = () => {
  // 搜索功能已通过计算属性实现
  // 这里可以添加额外的搜索逻辑，比如调用后端 API
}

const selectCategory = (category) => {
  selectedCategory.value = category
  // 分类筛选已通过计算属性实现
}

const uploadResource = () => {
  uploadDialogVisible.value = true
  // 重置表单
  if (uploadFormRef.value) {
    uploadFormRef.value.resetFields()
  }
  uploadForm.value = {
    title: '',
    category: '',
    description: '',
    imageList: [],
    attachmentList: []
  }
}

const downloadResource = async (resource) => {
  try {
    // 如果有附件，下载所有附件
    if (resource.attachments && resource.attachments.length > 0) {
      for (const attachment of resource.attachments) {
        await downloadFromUrl(attachment.url, attachment.name)
      }
      ElMessage.success('资料下载成功')
    } else {
      ElMessage.warning('该资料没有可下载的附件')
    }
  } catch (error) {
    ElMessage.error('下载失败，请重试')
  }
}

const goToMyResources = () => {
  router.push('/my-resources')
}

const showResourceDetail = (resource) => {
  currentResource.value = resource
  detailDialogVisible.value = true
}

const downloadAttachment = async (attachment) => {
  try {
    await downloadFromUrl(attachment.url, attachment.name)
    ElMessage.success('附件下载成功')
  } catch (error) {
    ElMessage.error('下载失败，请重试')
  }
}

const uploadRules = {
  title: [
    { required: true, message: '请输入资料名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择资料分类', trigger: 'change' }
  ]
}

const handleImageChange = (file) => {
  // 这里可以添加图片大小、格式等验证
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  const isLt2M = file.raw.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

const handleImageRemove = (file) => {
  const index = uploadForm.value.imageList.indexOf(file)
  if (index !== -1) {
    uploadForm.value.imageList.splice(index, 1)
  }
}

const handleAttachmentChange = (file) => {
  // 这里可以添加文件大小等验证
  const isLt10M = file.raw.size / 1024 / 1024 < 20
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 20MB！')
    return false
  }
  return true
}

const handleAttachmentRemove = (file) => {
  const index = uploadForm.value.attachmentList.indexOf(file)
  if (index !== -1) {
    uploadForm.value.attachmentList.splice(index, 1)
  }
}

const submitUpload = async () => {
  if (!uploadFormRef.value) return

  await uploadFormRef.value.validate(async (valid) => {
    if (valid) {
      const loadingInstance = ElLoading.service({
        text: '正在上传资料...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      try {
        const noteData = {
          title: uploadForm.value.title,
          category: uploadForm.value.category,
          content: uploadForm.value.description || '',
          imageUrls: [],
          attachmentUrls: [],
          user: { id: authStore.userId }
        }
        // 1. 上传图片文件
        for (const file of uploadForm.value.imageList) {
          const response = await resourceApi.uploadFile(file.raw)
          noteData.imageUrls.push(response.data)
        }
        // 2. 上传附件文件
        for (const file of uploadForm.value.attachmentList) {
          const response = await resourceApi.uploadFile(file.raw)
          noteData.attachmentUrls.push(response.data)
        }
        await resourceApi.addNotes(noteData)

        ElMessage.success('上传成功')
        uploadDialogVisible.value = false
        fetchResources()
        // TODO: 刷新资料列表
      } catch (error) {
        console.log(error)
        ElMessage.error('上传失败，请重试')
      }finally{
        loadingInstance.close()
      }
    }
  })
}

// 格式化时间方法
const formatTime = (uploadTime) => {
  const date = new Date(uploadTime);
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(date).replace(/\//g, '/');
};

//初始化列表
onMounted(() => {
  if (!token.value) {
    ElMessage.error('请先登录')
    return
  }
  fetchResources()
})
</script>

<style scoped>
.resources-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.search-header {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resource-item {
  transition: all 0.3s;
}

.resource-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.resource-content {
  cursor: pointer;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.resource-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.resource-title h4 {
  margin: 0;
  color: #333;
}

.resource-description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.resource-info {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  margin-bottom: 15px;
}

.resource-footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 10px;
}

.resource-detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.resource-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  h4 {
    margin: 0 0 12px 0;
    color: #333;
    font-size: 16px;
  }
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-preview-item {
  width: 120px;
  height: 120px;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;

  .el-image {
    width: 100%;
    height: 100%;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.attachment-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;

  .el-icon {
    color: #409eff;
  }
}

.upload-dialog :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.upload-dialog :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>