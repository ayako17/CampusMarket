<template>
  <div class="secondhand-container">
    <el-backtop :right="100" :bottom="100" />
    <!-- 搜索和发布区域 -->
    <el-card class="search-card">
      <div class="card-header">
        <h2 class="page-title">易二手</h2>
        <el-button type="success" @click="showAddDialog = true">发布商品</el-button>
      </div>
      <div class="search-header">
        <el-input v-model="searchQuery" placeholder="搜索二手商品..." class="search-input" clearable>
          <template #prefix>
            <el-icon>
              <search />
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <div class="items-grid">
      <el-empty v-if="items.length === 0" description="暂无商品" />
      <el-card v-for="item in items" :key="item.id" class="item-card" @click="viewBookDetail(item)">
        <div class="item-image">
          <el-image :src="`${item.imageUrl}`" fit="cover" />
          <div class="item-price">¥{{ item.price }}</div>
        </div>
        <div class="item-info">
          <h4 class="item-title">{{ item.title }}</h4>
          <p class="item-description">{{ item.description }}</p>
          <div>
            <span v-if="item.distance !== Infinity" class="distance-info">
              距离：{{ item.distance.toFixed(2) }} 公里
            </span>
            <span v-else class="distance-info">距离未知</span>
          </div>
          <div class="item-footer">
            <div class="seller-info">
              <el-avatar :src="item.user.avatarUrl" :size="24" />
              <span>{{ item.user.username }}</span>
            </div>
            <el-button type="primary" size="small" @click.stop="contactSeller(item)">
              联系卖家
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 查看商品详情的弹窗 -->
    <el-dialog title="商品详情" v-model="showDetailDialog" width="600px" :close-on-click-modal="false">
      <div v-if="selectedBook">
        <div class="book-detail">
          <el-image :src="selectedBook.imageUrl" fit="cover" class="book-image"
            :preview-src-list="[selectedBook.imageUrl]" preview-teleported />
          <h2>{{ selectedBook.title }}</h2>
          <p v-if="selectedBook.author"><strong>作者：</strong> {{ selectedBook.author }}</p>
          <p>{{ selectedBook.description }}</p>
          <p><strong>价格：</strong> ¥{{ selectedBook.price }}</p>
          <p><strong>新旧程度：</strong> {{ selectedBook.situation }}</p>
          <div class=" seller-info">
            <el-avatar :src="selectedBook.user.avatarUrl" @click="handleImageClick" />
            <span>卖家：{{ selectedBook.user.username }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加商品的弹窗 -->
    <el-dialog title="发布商品" v-model="showAddDialog" width="500">
      <el-form :model="newItem" :rules="rules" ref="addFormRef">
        <el-form-item label="标题" prop="title">
          <input id="name" v-model.trim="newItem.title" type="text" placeholder="请输入商品名称" maxlength="60" required />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <input id="author" v-model.trim="newItem.author" type="text" placeholder="请输入作者（可选）" maxlength="50" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <textarea id="description" v-model.trim="newItem.description" rows="4" placeholder="请输入详细商品描述" type="textarea"
                    required />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <input id="price" v-model.number="newItem.price" type="number" min="0" step="0.01" placeholder="0.00"
                 required />
        </el-form-item>

        <el-form-item label="新旧程度" prop="situation">
          <el-select v-model="newItem.situation" placeholder="请选择商品新旧程度" style="width: 100%;">
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
            <el-option label="七成新" value="七成新" />
            <el-option label="六成新及以下" value="六成新及以下" />
          </el-select>
        </el-form-item>

        <el-form-item label="图片" prop="image" style="margin-left: 10px;">
          <el-upload action="http://localhost:6005/api/files/upload" method="post" :show-file-list="false"
                     :on-success="handleUploadSuccess" :on-error="handleUploadError" :before-upload="beforeUpload">
            <div style="width: 100px; height: 100px; cursor: pointer; display: flex; align-items: center;
               justify-content: center; background-color: #f5f5f5; border: 1px dashed #ccc;">
              <el-image v-if="newItem.imageUrl" :src="newItem.imageUrl" style="width: 100%; height: 100%;"
                        fit="cover" />
              <el-icon v-else>
                <plus />
              </el-icon>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="publishItem">确认发布</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { bookApi } from '../api/book'
import { authApi } from '../api/auth'
import { profileApi } from '../api/profile'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
const router = useRouter()

const searchQuery = ref('')
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const selectedBook = ref(null)

const items = ref([])
const addFormRef = ref(null)

// 更新 newItem 对象，添加缺少的字段
const newItem = reactive({
  title: '',
  author: '',        // 添加作者字段
  description: '',
  price: '',
  situation: '九成新', // 添加新旧程度字段，设置默认值
  imageUrl: ''
})

// 更新验证规则
const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  situation: [{ required: true, message: '请选择商品新旧程度', trigger: 'change' }]
}
// 获取当前用户 ID
const userId = ref(authStore.userId || null)
const token = ref(authStore.token || '')

const publishItem = async () => {
  if (!addFormRef.value) return
  try {
    await addFormRef.value.validate()
    const bookData = { ...newItem, user: { id: userId.value } }
    const response = await bookApi.addBook(bookData)
    ElMessage.success(response.data.message || '商品添加成功')
    // Object.keys(newItem).forEach((key) => (newItem[key] = ''))
    Object.keys(newItem).forEach((key) => {
      if (key === 'situation') {
        newItem[key] = '九成新' // 重置为默认值
      } else {
        newItem[key] = ''
      }
    })
    showAddDialog.value = false
    fetchBooks()
  } catch (error) {
    console.log(error)
    ElMessage.error(error.response?.data?.message || '添加商品失败')
  }
}
//搜索商品
const handleSearch = async () => {
  try {
    const response = await bookApi.searchBooks(searchQuery.value);
    const books = response.data.books;

    const userProfile = await profileApi.getProfile(userId.value);
    const userLatitude = userProfile.data.latitude;
    const userLongitude = userProfile.data.longitude;

    const booksWithDistance = await Promise.all(
      books.map(async (book) => {
        const sellerProfile = await profileApi.getProfile(book.user.id);
        const sellerLatitude = sellerProfile.data.latitude;
        const sellerLongitude = sellerProfile.data.longitude;
        const avatarUrl = sellerProfile.data.avatarUrl;
        const distance = calculateDistance(
          userLatitude,
          userLongitude,
          sellerLatitude,
          sellerLongitude
        );
        return { ...book, user: { ...book.user, avatarUrl }, distance };
      })
    );

    items.value = booksWithDistance.sort((a, b) => a.distance - b.distance);
    ElMessage.success(response.data.message || '搜索成功');
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '搜索失败');
  }
};

//查看教材详情
const viewBookDetail = (book) => {
  selectedBook.value = book
  showDetailDialog.value = true
}

//联系商家
const contactSeller = (item) => {
  const sellerUsername = item.user.username
  router.push({ name: 'Message', query: { receiver: sellerUsername } })
}

const handleUploadSuccess = (response) => {
  newItem.imageUrl = response
  ElMessage.success('图片上传成功')
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const fetchUserAvatar = async (userId) => {
  try {
    const response = await profileApi.getProfile(userId)
    return response.data.avatarUrl
  } catch (error) {
    console.error(`获取用户头像失败 (userId: ${userId})`, error)
    return 'https://via.placeholder.com/40'
  }
}

//计算距离
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  if (!lat1 || !lng1 || !lat2 || !lng2) return Infinity // 如果经纬度未初始化，返回无穷大（排最后）
  const toRad = (value) => (value * Math.PI) / 180
  const R = 6371 // 地球半径，单位：公里

  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c // 返回距离，单位：公里
}

// 获取教材列表
const fetchBooks = async () => {
  try {
    const response = await bookApi.getAllBooks()
    const books = response.data.books

    // 获取当前用户的经纬度
    let userProfile
    try {
      userProfile = await profileApi.getProfile(userId.value)
    } catch (error) {
      ElMessage.warning('请先完善个人资料')
      items.value = []
      return
    }
    const userLatitude = userProfile.data.latitude
    const userLongitude = userProfile.data.longitude

    // 为每本书计算距离
    const booksWithDistance = await Promise.all(
      books.map(async (book) => {
        const sellerProfile = await profileApi.getProfile(book.user.id)
        const sellerLatitude = sellerProfile.data.latitude
        const sellerLongitude = sellerProfile.data.longitude
        const avatarUrl = sellerProfile.data.avatarUrl
        const distance = calculateDistance(
          userLatitude,
          userLongitude,
          sellerLatitude,
          sellerLongitude
        )
        return { ...book, user: { ...book.user, avatarUrl }, distance }
      })
    )

    // 按距离排序（距离越近越靠前）
    items.value = booksWithDistance.sort((a, b) => a.distance - b.distance)
  } catch (error) {
    console.error('获取教材列表失败:', error)
    ElMessage.error('获取教材列表失败')
  }
}

const handleImageClick = () => {
  const sellerId = selectedBook.value.user.id
  const sellerName = selectedBook.value.user.username
  viewOtherProfile(sellerId, sellerName)
}

// 跳转到他人主页
const viewOtherProfile = (userId, userName) => {
  router.push(`/other-profile/${userId}`)
}

// 初始化加载教材列表
onMounted(() => {
  if (!token.value) {
    ElMessage.error('请先登录')
    return
  }
  fetchBooks()
})
</script>

<style scoped>
.secondhand-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.search-input {
  flex: 1;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.items-grid .el-empty {
  grid-column: 1 / -1; /* 占满整行 */
}

.item-card {
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
}

.item-price {
  position: absolute;
  bottom: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 8px;
  font-size: 16px;
  font-weight: bold;
}

.item-info {
  padding: 12px;
}

.item-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.item-description {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  /* 强制单行显示 */
  overflow: hidden;
  /* 超出部分隐藏 */
  text-overflow: ellipsis;
  /* 超出部分显示省略号 */
}

.item-footer {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

input,
textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

textarea::placeholder,
input::placeholder {
  color: #999999;
  font-size: 14px;
  font-family: '微软雅黑';
}

.card-header {
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

.book-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.book-image {
  width: 300px;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.book-detail h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.book-detail p {
  margin: 0;
  font-size: 16px;
  color: #666;
}

.distance-info {
  font-size: 12px;
  color: #666;
}
</style>