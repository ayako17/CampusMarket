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
                            <el-button type="primary" @click="contactSeller">
                                联系TA
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
                                <h2>发布的教材</h2>
                            </template>
                        </el-tab-pane>
                    </el-tabs>

                    <div v-if="activeTab === 'books'">
                        <div class="books-card">
                            <el-empty v-if="userBooks.length === 0" description="暂无教材" />
                            <el-card v-for="item in userBooks" :key="item.id" class="item-card"
                                @click="viewBookDetail(item)">
                                <div class="item-image">
                                    <el-image :src="`${item.imageUrl}`" fit="cover" />
                                    <div class="item-price">¥{{ item.price }}</div>
                                </div>
                                <div class="item-info">
                                    <h4 class="item-title">{{ item.title }}</h4>
                                    <p class="item-description">{{ item.description }}</p>
                                </div>
                            </el-card>
                        </div>
                    </div>

                    <!-- 查看教材详情的弹窗 -->
                    <el-dialog title="教材详情" v-model="showDetailDialog" width="600px" :close-on-click-modal="false">
                        <div v-if="selectedBook">
                            <div class="book-detail">
                                <el-image :src="selectedBook.imageUrl" fit="cover" class="book-image"
                                    :preview-src-list="[selectedBook.imageUrl]" preview-teleported />
                                <h2>{{ selectedBook.title }}</h2>
                                <p>{{ selectedBook.description }}</p>
                                <p><strong>价格：</strong> ¥{{ selectedBook.price }}</p>
                            </div>
                        </div>
                        <template #footer>
                            <el-button @click="showDetailDialog = false">关闭</el-button>
                        </template>
                    </el-dialog>
                </el-card>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '../api/profile'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const userInfo = reactive({
    avatarUrl: '',
    name: '',
    grade: '',
    college: '',
    contact: '',
    address: '',
    bio: '',
})

const userBooks = ref([])
const activeTab = ref('books')

const showDetailDialog = ref(false) // 控制弹窗显示
const selectedBook = ref(null) // 当前选中的教材

// 查看教材详情
const viewBookDetail = (book) => {
    selectedBook.value = book // 设置选中的教材
    showDetailDialog.value = true // 打开弹窗
}

//获取用户名
const fetchUsername = async (userId) => {
  try {
    const response = await profileApi.getUsernameById(userId);
    return response.data; // 返回用户名
  } catch (error) {
    ElMessage.error('获取用户名失败');
    return null; // 如果失败，返回 null
  }
};

// 获取用户的个人资料
const fetchProfile = async () => {
    try {
        const userId = route.params.userId
        const response = await profileApi.getProfile(userId)
        Object.assign(userInfo, response.data)
    } catch (error) {
        ElMessage.error('获取用户资料失败')
    }
}

// 获取用户发布的教材
const fetchUserBooks = async () => {
    try {
        const userId = route.params.userId
        const response = await profileApi.getUserBooks(userId)
        userBooks.value = response.data
    } catch (error) {
        ElMessage.error('获取用户发布的教材失败')
    }
}

//联系该用户
const contactSeller = async () => {
  const userId = route.params.userId;
  const sellerUsername = await fetchUsername(userId); // 等待获取用户名

  if (sellerUsername) {
    console.log(sellerUsername);
    router.push({ name: 'Message', query: { receiver: sellerUsername } }); // 跳转到消息页面
  } else {
    ElMessage.error('无法联系该用户，用户名获取失败');
  }
};

// 处理标签页切换
const handleTabClick = (tab) => {
    if (tab.name === 'books') {
        fetchUserBooks()
    } else if (tab.name === 'notes') {
        // 待实现
    } else if (tab.name === 'requests') {
        // 待实现
    }
}

// 页面加载时获取数据
onMounted(() => {
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
</style>