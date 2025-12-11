<template>
  <div class="profile-container">
    <el-container>
      <el-main>
        <!-- 个人信息模块 -->
        <el-card class="profile-card">
          <div class="profile-info">
            <!-- 左侧头像 -->
            <div class="avatar-section">
              <el-avatar :src="`${userInfo.avatarUrl}`" size="150" class="profile-avatar" />
              <el-button type="primary" plain class="edit-avatar-button" @click="showEditDialog = true">
                编辑资料
              </el-button>
            </div>
            <!-- 右侧信息 -->
            <div class="profile-details">
              <!-- 第一行 -->
              <div class="profile-row">
                <p><strong>名称：</strong>{{ userInfo.name }}</p>
                <p><strong>年级：</strong>{{ userInfo.grade }}</p>
                <p><strong>学院：</strong>{{ userInfo.college }}</p>
              </div>
              <!-- 第二行 -->
              <div class="profile-row">
                <p><strong>地址：</strong>{{ userInfo.address }}</p>
              </div>
              <!-- 第三行 -->
              <div class="profile-row">
                <p><strong>个人简介：</strong>{{ userInfo.bio }}</p>
              </div>
            </div>
          </div>
        </el-card>

        <el-card>
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane name="books">
              <template #label>
                <h2>已发布的教材</h2>
              </template>
            </el-tab-pane>
          </el-tabs>

          <div v-if="activeTab === 'books'">
            <div class="books-card">
              <el-empty v-if="userBooks.length === 0" description="暂无教材" />
              <el-card v-for="item in userBooks" :key="item.id" class="item-card">
                <div class="item-image">
                  <el-image :src="`${item.imageUrl}`" fit="cover" />
                  <div class="item-price">¥{{ item.price }}</div>
                </div>
                <div class="item-info">
                  <h4 class="item-title">{{ item.title }}</h4>
                  <p class="item-description">{{ item.description }}</p>
                  <div class="item-footer">
                    <el-button type="primary" size="small" @click="editBook(item)">编辑</el-button>
                    <el-button type="danger" size="small" @click="deleteBook(item.id)">删除</el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-card>

      </el-main>
    </el-container>

    <!-- 编辑资料弹窗 -->
    <el-dialog title="编辑资料" v-model="showEditDialog" width="500px">
      <el-form :model="editUserInfo" label-width="100px">
        <el-form-item label="头像" prop="image">
          <el-upload action="http://localhost:6005/api/files/upload" method="post" :show-file-list="false"
            :on-success="handleAvatarUploadSuccess" :on-error="handleUploadError">
            <!-- 条件渲染：显示图片或加号图标 -->
            <div style="width: 100px; height: 100px; cursor: pointer; display: flex; align-items: center;
               justify-content: center; background-color: #f5f5f5; border: 1px dashed #ccc;">
              <el-image v-if="editUserInfo.avatarUrl" :src="editUserInfo.avatarUrl" style="width: 100%; height: 100%;"
                fit="cover" />
              <el-icon v-else>
                <plus />
              </el-icon>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="editUserInfo.name" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editUserInfo.grade" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="editUserInfo.college" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="editUserInfo.contact" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editUserInfo.address" placeholder="请选择地址" readonly @click="openMapDialog" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input type="textarea" v-model="editUserInfo.bio" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUserInfo">保存</el-button>
      </template>
    </el-dialog>

    <!--地图弹窗-->
    <el-dialog title="选择地址" v-model="showMapDialog" width="800px">
      <div id="map" style="width: 100%; height: 400px;"></div>
      <template #footer>
        <el-button @click="showMapDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog title="编辑教材" v-model="showEditBookDialog" width="500px">
      <el-form :model="editBookForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="editBookForm.title" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="editBookForm.price" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="editBookForm.description" />
        </el-form-item>
        <el-form-item label="图片" prop="image" style="margin-left: 10px;">
          <el-upload action="http://localhost:6005/api/files/upload" method="post" :show-file-list="false"
            :on-success="handleUploadSuccess" :on-error="handleUploadError">
            <!-- 条件渲染：显示图片或加号图标 -->
            <div style="width: 100px; height: 100px; cursor: pointer; display: flex; align-items: center;
               justify-content: center; background-color: #f5f5f5; border: 1px dashed #ccc;">
              <el-image v-if="editBookForm.imageUrl" :src="editBookForm.imageUrl" style="width: 100%; height: 100%;"
                fit="cover" />
              <el-icon v-else>
                <plus />
              </el-icon>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditBookDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBook">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '../api/profile'
import { authApi } from '../api/auth'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

const userInfo = reactive({
  avatarUrl: '',
  name: '',
  grade: '',
  college: '',
  contact: '',
  address: '',
  bio: '',
  latitude: null, // 纬度
  longitude: null, // 经度
})

const editBookForm = reactive({
  id: null,
  title: '',
  price: '',
  description: '',
  imageUrl: ''
})

const editUserInfo = reactive({ ...userInfo })
const showEditDialog = ref(false)
const showEditBookDialog = ref(false)
const userId = ref(authStore.userId || null)
const token = ref(authStore.token || '')
const router = useRouter();
const activeTab = ref('books')

let map = null
let marker = null
const showMapDialog = ref(false)

// 打开地图弹窗
const openMapDialog = () => {
  showMapDialog.value = true
  setTimeout(() => {
    initializeMap()
  }, 300)
}

// 初始化地图
const initializeMap = () => {
  if (!window.BMapGL) {
    console.error('百度地图 API 未加载，请检查 API Key 或网络连接')
    return
  }
  if (!map) {
    map = new BMapGL.Map('map') // 创建地图实例
    const point = new BMapGL.Point(114.365248, 30.544178) // 默认中心点
    map.centerAndZoom(point, 17) // 初始化地图，设置中心点和缩放级别
    map.enableScrollWheelZoom(true) // 启用滚轮缩放

    // 添加点击事件
    map.addEventListener('click', (e) => {
      const { lat, lng } = e.latlng
      editUserInfo.latitude = lat
      editUserInfo.longitude = lng

      // 更新标记点
      if (!marker) {
        marker = new BMapGL.Marker(new BMapGL.Point(lng, lat))
        map.addOverlay(marker)
      } else {
        marker.setPosition(new BMapGL.Point(lng, lat))
      }

      // 逆地理编码获取地址
      const geocoder = new BMapGL.Geocoder()
      geocoder.getLocation(new BMapGL.Point(lng, lat), (result) => {
        if (result) {
          const components = result.addressComponents
          const address = `${components.street}${components.streetNumber}`

          // 检查是否有周边兴趣点
          if (result.surroundingPois && result.surroundingPois.length > 0) {
            const poi = result.surroundingPois[0] // 获取最近的兴趣点
            editUserInfo.address = `${poi.title}` // 使用兴趣点名称作为地址
          } else {
            editUserInfo.address = address // 如果没有兴趣点，使用默认地址
          }
        }
      })
    })
  }
}

// 确认选择地址
const confirmAddress = () => {
  if (!editUserInfo.address) {
    ElMessage.error('请先选择地址')
    return
  }
  showMapDialog.value = false
  ElMessage.success('地址已选择')
}

const goBackToHome = () => {
  router.push('/home')
}

// 获取用户的个人资料
const fetchProfile = async () => {
  try {
    const response = await profileApi.getProfile(userId.value)
    Object.assign(userInfo, response.data)
    Object.assign(editUserInfo, response.data)
  } catch (error) {
    if (error.response && error.response.status === 400) {
      ElMessage.info('用户资料尚未创建，请编辑后保存')
    } else {
      ElMessage.error('获取个人资料失败')
    }
  }
}

// 保存用户的个人资料
const saveUserInfo = async () => {
  try {
    const profileData = { ...editUserInfo, user: { id: userId.value } }
    const response = await profileApi.updateProfile(profileData)
    Object.assign(userInfo, response.data)
    showEditDialog.value = false
    ElMessage.success('个人资料已保存')
  } catch (error) {
    ElMessage.error('保存个人资料失败')
  }
}

const userBooks = ref([])

// 获取用户发布的教材
const fetchUserBooks = async () => {
  try {
    const response = await profileApi.getUserBooks(userId.value)
    userBooks.value = response.data
  } catch (error) {
    ElMessage.error('获取用户发布的教材失败')
  }
}

const handleTabClick = (tab) => {
  if (tab.name === 'books') {
    fetchUserBooks()
  } else if (tab.name === 'notes') {
    //待完善
  } else if (tab.name === 'requests') {
    //待完善
  }
}

//编辑教材
const editBook = (book) => {
  Object.assign(editBookForm, book)
  showEditBookDialog.value = true
}

//保存教材编辑
const saveBook = async () => {
  try {
    const response = await profileApi.updateBook(editBookForm) // 调用 API 更新教材
    ElMessage.success('教材已更新')
    showEditBookDialog.value = false
    fetchUserBooks() // 重新加载用户发布的教材
  } catch (error) {
    ElMessage.error('更新教材失败')
  }
}

// 删除教材
const deleteBook = async (bookId) => {
  try {
    await profileApi.deleteBook(bookId)
    ElMessage.success('教材已删除')
    fetchUserBooks() // 重新加载教材列表
  } catch (error) {
    ElMessage.error('删除教材失败')
  }
}

const handleAvatarUploadSuccess = (response) => {
  editUserInfo.avatarUrl = response
  ElMessage.success('头像上传成功');
};

const handleUploadSuccess = (response) => {
  editBookForm.imageUrl = response
  ElMessage.success('图片上传成功');
};

const handleUploadError = () => {
  ElMessage.error('上传失败');
};

// 页面加载时获取用户ID 、个人资料、已发布的教材
onMounted(() => {
  if (!token.value) {
    ElMessage.error('请先登录')
    return
  }
  fetchProfile()
  fetchUserBooks()
})
</script>

<style scoped>
.profile-card {
  margin-bottom: 20px;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
}

.profile-avatar {
  margin-bottom: 10px;
  width: 80px;
  height: 80px;
}

.edit-avatar-button {
  color: #fff;
  background-color: #409eff;
  border-color: #409eff;
}

.profile-details {
  flex-grow: 1;
}

.profile-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.profile-row p {
  margin: 0;
  flex: 1;
}

.books-card {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 5px;
}

.books-card .el-empty {
  grid-column: 1 / -1; /* 占满整行 */
}

.el-table {
  margin-top: 20px;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  z-index: 1000;
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
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-footer {
  display: flex;
  justify-content: center;
  gap: 40px;
  align-items: center;
}

#map {
  width: 100%;
  height: 400px;
}
</style>