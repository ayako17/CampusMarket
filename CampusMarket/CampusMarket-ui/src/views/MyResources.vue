<template>
  <div class="resources-container">
    <el-card class="search-card">
      <div class="page-header">
        <h2 class="page-title">我的资料</h2>
        <div class="header-buttons">
          <el-button type="success" @click="goBack">返回资料库</el-button>
        </div>
      </div>
      <div class="search-header">
        <el-input v-model="searchQuery" placeholder="搜索我的资料..." class="search-input" clearable>
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
            <span>上传时间：{{ formatTime(resource.uploadTime) }}</span>
          </div>
        </div>
        <div class="resource-footer">
          <div class="resource-actions">
            <el-button type="primary" @click.stop="downloadResource(resource)">下载</el-button>
            <el-button type="warning" @click.stop="editResource(resource)">编辑</el-button>
            <el-button type="danger" @click.stop="deleteResource(resource)">删除</el-button>
          </div>
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
          <el-button type="warning" @click="handleEdit">编辑资料</el-button>
          <el-button type="danger" @click="handleDelete">删除资料</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="50%" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="资料标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="资料分类">
          <el-select v-model="editForm.category">
            <el-option v-for="tag in categories.filter(c => c.value !== '')" :key="tag.value" :label="tag.label"
              :value="tag.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="资料描述">
          <el-input v-model="editForm.description" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="资料图片">
          <el-upload action="#" list-type="picture-card" :auto-upload="false" v-model:file-list="editForm.images"
            :on-change="handleImageChange" :on-remove="handleImageRemove">
            <el-icon>
              <plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload action="#" :auto-upload="false" v-model:file-list="editForm.attachments"
            :on-change="handleAttachmentChange" :on-remove="handleAttachmentRemove">
            <el-button type="primary">添加附件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Document, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { downloadFromUrl } from '@/utils/download'
import { resourceApi } from '@/api/resource'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
const token = ref(authStore.token || '')
const userId = ref(authStore.userId || '')

const router = useRouter()
const searchQuery = ref('')
const selectedCategory = ref('')
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  title: '',
  category: '',
  description: '',
  images: [],
  attachments: []
})

const categories = [
  { label: '全部', value: '' },
  { label: '考试资料', value: 'exam' },
  { label: '课程笔记', value: 'notes' },
  { label: '实验报告', value: 'lab' },
  { label: '课程作业', value: 'homework' },
  { label: '其他资料', value: 'other' }
]

const detailDialogVisible = ref(false)
const currentResource = ref({})

// 添加原始数据列表
const originalResources = ref([])

const fetchMyResources = async () => {
  if (!userId.value) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  try {
    const response = await resourceApi.getUserNotes(userId.value)
    if (response.data && response.data.notes) {
      originalResources.value = response.data.notes.map(note => ({
        id: note.id,
        title: note.title,
        category: note.category,
        categoryLabel: getCategoryLabelFromContent(note.category),
        description: note.content,
        uploadTime: note.updateTime || '未知',
        downloads: 123,//占位
        images: note.imageUrls || [],
        attachments: (note.attachmentUrls || []).map(url => ({
          name: getFileNameFromUrl(url),
          url: url
        }))
      }))
    } else {
      originalResources.value = []
    }
  } catch (error) {
    ElMessage.error('获取资源列表失败')
  }
}

// 获取分类标签
const getCategoryLabelFromContent = (category) => {
  const categoryItem = categories.find(item => item.value === category)
  return categoryItem ? categoryItem.label : '其他资料'
}

const getFileNameFromUrl = (url) => {
  if (!url) return '未命名文件'
  const fullName = url.split('/').pop()
  const nameParts = fullName.split('_')
  if (nameParts.length < 2) {
    return fullName
  }
  return nameParts.slice(1).join('_')
}

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
      resource.description.toLowerCase().includes(query)
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

const editResource = (resource) => {
  // 转换图片格式以适应 el-upload 组件
  const formattedImages = resource.images ? resource.images.map(url => ({
    url,
    name: url.split('/').pop()
  })) : []

  // 转换附件格式以适应 el-upload 组件
  const formattedAttachments = resource.attachments ? resource.attachments.map(attachment => ({
    name: attachment.name,
    url: attachment.url
  })) : []

  editForm.value = {
    ...resource,
    images: formattedImages,
    attachments: formattedAttachments
  }
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  try {
    const noteData = {
      id: editForm.value.id,
      title: editForm.value.title,
      user: { id: userId.value },
      category: editForm.value.category,
      content: editForm.value.description || '',
      imageUrls: [],
      attachmentUrls: []
    }

    // 处理图片
    for (const image of editForm.value.images) {
      if (image.raw) {
        // 新上传的图片
        const response = await resourceApi.uploadFile(image.raw)
        noteData.imageUrls.push(response.data)
      } else if (image.url) {
        // 保留原有图片
        noteData.imageUrls.push(image.url)
      }
    }

    // 处理附件
    for (const attachment of editForm.value.attachments) {
      if (attachment.raw) {
        // 新上传的附件
        const response = await resourceApi.uploadFile(attachment.raw)
        noteData.attachmentUrls.push(response.data)
      } else if (attachment.url) {
        // 保留原有附件
        noteData.attachmentUrls.push(attachment.url)
      }
    }
    // 调用更新API
    await resourceApi.updateNote(noteData.id, noteData)
    ElMessage.success('编辑成功')
    editDialogVisible.value = false
    fetchMyResources()
  } catch (error) {
    console.log(error)
    ElMessage.error('更新失败: ' + (error.message || '请重试'))
  }
}

const deleteResource = (resource) => {
  ElMessageBox.confirm(
    '确定要删除这个资料吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await resourceApi.deleteNote(resource.id)
      ElMessage.success('删除成功')
      fetchMyResources()
    } catch (error) {
      console.error('删除资源失败:', error)
      ElMessage.error('删除失败：' + (error.message || '请重试'))
    }
  }).catch(() => { })
}

const goBack = () => {
  router.push('/resources')
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

const handleEdit = () => {
  //editForm.value = { ...currentResource.value }
  detailDialogVisible.value = false
  setTimeout(() => {
    // 调用已有的editResource函数处理当前资源
    editResource(currentResource.value)
  }, 50)
  //editDialogVisible.value = true
}

const handleDelete = () => {
  ElMessageBox.confirm(
    '确定要删除这个资料吗？删除后将无法恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await resourceApi.deleteNote(currentResource.value.id)
      ElMessage.success('删除成功')
      detailDialogVisible.value = false
      fetchMyResources()
    } catch (error) {
      console.error('删除资源失败:', error)
      ElMessage.error('删除失败：' + (error.message || '请重试'))
    }
  }).catch(() => { })
}

const handleImageChange = (file) => {
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
  const index = editForm.value.images.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    editForm.value.images.splice(index, 1)
  }
}

const handleAttachmentChange = (file) => {
  const isLt20M = file.raw.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.error('附件大小不能超过 20MB！')
    return false
  }
  return true
}

const handleAttachmentRemove = (file) => {
  const index = editForm.value.attachments.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    editForm.value.attachments.splice(index, 1)
  }
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

// 页面加载时获取数据
onMounted(() => {
  if (!token.value) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  fetchMyResources()
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

.resource-actions {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>