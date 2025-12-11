<template>
  <div class="email-container">
    <!-- 左侧导航栏 -->
    <div class="nav-sidebar">
      <h2 class="app-title">邮件系统</h2>
      <div class="menu-items">
        <el-button v-for="item in menuItems" type="primary" :key="item.key" @click="activeTab = item.key"
          :class="{ active: activeTab === item.key }" class="menu-btn">
          <span>{{ item.icon }} {{ item.label }}</span>
          <span v-if="item.key === 'inbox' && unreadCount > 0" class="unread-dot">{{ unreadCount }}</span>
        </el-button>
      </div>
    </div>


    <!-- 右侧内容区 -->
    <div class="content-area">
      <!-- 邮件编写组件 -->
      <div v-if="activeTab === 'compose'" class="compose-section">
        <el-form :model="emailForm" label-width="80px">
          <el-form-item label="收件人">
            <el-select v-model="emailForm.receiver" filterable allow-create placeholder="选择或输入邮箱" class="full-width">
              <el-option v-for="contact in contacts" :key="contact.username" :label="contact.username"
                :value="`${contact.username}@hex.campus.com`">
                <span>{{ contact.username }}</span>
                <span class="email-address">({{ contact.username }}@hex.campus.com)</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="主题">
            <el-input v-model="emailForm.subject" placeholder="输入主题" />
          </el-form-item>

          <el-form-item label="附件">
            <el-upload class="upload-demo" action="#" :on-change="handleFileUpload" :auto-upload="false" multiple>
              <el-button type="primary">📎 添加附件</el-button>
            </el-upload>
          </el-form-item>

          <el-form-item label="正文">
            <el-input v-model="emailForm.content" type="textarea" :rows="12" resize="none" placeholder="输入邮件内容..."
              class="email-content" />
          </el-form-item>

          <el-form-item>
            <div class="button-container">
              <el-button type="success" @click="sendEmail">发送</el-button>
              <el-button @click="resetForm">重置</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 已发送 -->
      <div v-if="activeTab === 'sent'" class="sent-section">
        <el-table :data="sentEmails" style="width: 100%" @row-click="viewEmailDetails">
          <!-- 状态列 -->
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isRead ? 'success' : 'danger'" size="small">
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="receiver.username" label="收件人" width="180" />
          <el-table-column prop="subject" label="主题" />
          <el-table-column label="时间" width="180">
            <template #default="{ row }">
              {{ formatTimestamp(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click.stop="deleteMessage(row.id, true)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 收件箱 -->
      <div v-if="activeTab === 'inbox'" class="inbox-section">
        <el-table :data="inboxEmails" style="width: 100%" @row-click="viewEmailDetails"
          :row-class-name="getRowClassName">
          <!-- 状态列 -->
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isRead ? 'success' : 'danger'" size="small">
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sender.username" label="发件人" width="180" />
          <el-table-column prop="subject" label="主题" />
          <el-table-column label="时间" width="180">
            <template #default="{ row }">
              {{ formatTimestamp(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click.stop="deleteMessage(row.id, false)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 查看邮件详情 -->
      <el-dialog v-model="isEmailDetailsDialogVisible" title="邮件详情" width="600px" :close-on-click-modal="false">
        <div v-if="selectedEmail" class="email-details">
          <div class="email-details-title">
            <h3>主题：{{ selectedEmail.subject }}</h3>
          </div>
          <div class="email-details-info">
            <p>发件人： {{ selectedEmail.sender?.username }}</p>
            <p>时&emsp;间：{{ formatTimestamp(selectedEmail.timestamp) }}</p>
            <p>收件人： {{ selectedEmail.receiver?.username }}</p>
          </div>
          <div class="email-details-content">
            <h4 class="content-label">内容：</h4>
            <el-input type="textarea" :rows="10" v-model="selectedEmail.content" readonly resize="none"
              class="email-content-box" />
          </div>
        </div>

        <template #footer>
          <el-button type="primary" @click="isEmailDetailsDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 通讯录 -->
      <div v-if="activeTab === 'contacts'" class="contacts-section">
        <el-table :data="contacts" style="width: 100%" @row-click="loadConversation">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="email" label="邮箱">
            <template #default="{ row }">
              {{ row.username }}@hex.campus.com
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!--来往邮件 -->
      <div v-if="activeTab === 'conversation'" class="conversation-section">
        <h3>与 {{ selectedContact?.username }} 的来往邮件</h3>
        <el-table :data="conversationEmails" style="width: 100%" @row-click="viewEmailDetails">
          <el-table-column prop="sender.username" label="发件人" width="180" />
          <el-table-column prop="receiver.username" label="收件人" width="180" />
          <el-table-column prop="subject" label="主题" />
          <el-table-column label="时间" width="180">
            <template #default="{ row }">
              {{ formatTimestamp(row.timestamp) }}
            </template>
          </el-table-column>
        </el-table>
        <el-button @click="activeTab = 'contacts'" type="primary">返回通讯录</el-button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { messageApi } from '@/api/message'
import { authApi } from '../api/auth'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
const route = useRoute()

const userId = ref(authStore.userId || null)
const token = ref(authStore.token || '')

const selectedEmail = ref(null);
const isEmailDetailsDialogVisible = ref(false)

// 状态管理 
const activeTab = ref('compose')
const menuItems = [
  { key: 'compose', label: '写邮件', icon: '✉️' },
  { key: 'sent', label: '已发送', icon: '📨' },
  { key: 'inbox', label: '收件箱', icon: '📥' },
  { key: 'contacts', label: '通讯录', icon: '📇' }
]

// 邮件表单数据 
const emailForm = reactive({
  receiver: '',
  subject: '',
  content: '',
  attachments: []
})


// 示例数据 
const contacts = ref([/* 联系人数据 */])
const sentEmails = ref([]); // 已发送邮件数据
const inboxEmails = ref([/* 邮件数据 */])
const unreadCount = ref(0);

const conversationEmails = ref([]); // 当前选中用户的来往邮件
const selectedContact = ref(null); // 当前选中的联系人

// 方法 
const handleFileUpload = (file) => {
  emailForm.attachments.push(file)
}

//发送邮件
const sendEmail = async () => {
  try {
    // 提取 username
    const email = emailForm.receiver;
    const username = email.split('@')[0]; // 提取 @ 前的部分
    // 获取 receiver 的 ID
    const { data: receiverId } = await messageApi.getUserIdByUsername(username);
    if (!receiverId) {
      ElMessage.error('收件人不存在');
      return;
    }
    const emailData = {
      sender: { id: userId.value },
      receiver: { id: receiverId },
      subject: emailForm.subject,
      content: emailForm.content
    };
    const response = await messageApi.sendMessage(emailData);
    ElMessage.success('邮件发送成功');
    initialize();
    // 清空表单
    emailForm.receiver = '';
    emailForm.subject = '';
    emailForm.content = '';
    emailForm.attachments = [];
  } catch (error) {
    console.error('邮件发送失败:', error);
    ElMessage.error('邮件发送失败，请稍后再试');
  }
};

//重置发送表单
const resetForm = () => {
  emailForm.receiver = '';
  emailForm.subject = '';
  emailForm.content = '';
  emailForm.attachments = [];
  ElMessage.info('表单已重置');
};

//计算未读邮件数量
const calculateUnreadCount = () => {
  unreadCount.value = inboxEmails.value.filter(email => !email.isRead).length;
};

const getRowClassName = (row) => {
  return row.isRead ? '' : 'unread-row';
};

//加载已发送邮件
const loadSentEmails = async () => {
  try {
    const { data } = await messageApi.getSendMessage(userId.value);
    sentEmails.value = data.filter(email => !email.isDeletedBySender);
  } catch (error) {
    console.error('加载已发送邮件失败:', error);
    ElMessage.error('加载已发送邮件失败，请稍后再试');
  }
};

// 加载收件箱邮件
const loadInboxEmails = async () => {
  try {
    const { data } = await messageApi.getReceiveMessage(userId.value);
    inboxEmails.value = data.filter(email => !email.isDeletedByReceiver);
    calculateUnreadCount();
  } catch (error) {
    console.error('加载收件箱邮件失败:', error);
    ElMessage.error('加载收件箱邮件失败，请稍后再试');
  }
}

// 格式化时间方法
const formatTimestamp = (timestamp) => {
  const date = new Date(timestamp);
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

//查看邮件
const viewEmailDetails = async (email) => {
  selectedEmail.value = email;
  isEmailDetailsDialogVisible.value = true;

  // 判断收件人是否为当前用户
  if (email.receiver?.id == userId.value) {
    // 收件人为自己，执行收件箱逻辑
    if (!email.isRead) {
      try {
        await messageApi.markAsRead(email.id);
        email.isRead = true; // 更新前端数据
        calculateUnreadCount(); // 重新计算未读邮件数
        initialize();
      } catch (error) {
        console.error('标记邮件为已读失败:', error);
      }
    }
  }
};

//删除邮件
const deleteMessage = async (messageId, isSender) => {
  try {
    await messageApi.deleteMessage(messageId, userId.value, isSender);
    ElMessage.success("邮件删除成功");
    initialize(); // 重新加载数据
  } catch (error) {
    console.error("删除邮件失败:", error);
    ElMessage.error("删除邮件失败，请稍后再试");
  }
};

// 加载通讯录
const loadContacts = async () => {
  try {
    const { data } = await messageApi.getContacts(userId.value);
    contacts.value = data;
  } catch (error) {
    console.error('加载通讯录失败:', error);
    ElMessage.error('加载通讯录失败，请稍后再试');
  }
};

// 加载与特定用户的来往邮件
const loadConversation = async (contact) => {
  try {
    selectedContact.value = contact;
    const { data } = await messageApi.getConversation(userId.value, contact.id);
    conversationEmails.value = data;
    activeTab.value = 'conversation'; // 切换到会话视图
  } catch (error) {
    console.error('加载来往邮件失败:', error);
    ElMessage.error('加载来往邮件失败，请稍后再试');
  }
};

const initialize = async () => {
  if (!token.value) {
    ElMessage.error('请先登录');
    return;
  }

  if (userId.value) {
    loadInboxEmails();
    loadSentEmails();
    loadContacts();
  }
};

onMounted(() => {
  initialize();

  const receiver = route.query.receiver
  if (receiver) {
    emailForm.receiver = `${receiver}@hex.campus.com`
  }
});
</script>

<style scoped>
.button-container {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

.email-container {
  padding-left: 20px;
  padding-top: 20px;
  display: flex !important;
  height: 100%;
  background: #f5f6fa;
}

.nav-sidebar {
  width: 240px;
  background: #2c3e50;
  padding: 20px;
  color: white;
}

.app-title {
  text-align: center;
  margin-bottom: 30px;
  color: #ecf0f1;
}

.menu-items {
  display: flex !important;
  flex-direction: column;
  gap: 12px;
}

/* 统一图标和文字间距 */
.menu-btn span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* 强制覆盖Element默认样式 */
:deep(.el-button) {
  margin: 0 !important;
  text-align: left !important;
}

/* 确保按钮宽度一致 */
:deep(.el-button--primary) {
  width: 100%;
  --el-button-hover-bg-color: #409eff;
}

.content-area {
  flex: 1;
  padding: 30px;
  background: white;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

.full-width {
  width: 100%;
}

.email-content {
  font-family: 'Helvetica Neue', sans-serif;
  font-size: 14px;
}

.active {
  background: #3498db !important;
  color: white !important;
}

span {
  flex-grow: 1;
  /* 文字部分自动扩展 */
  white-space: nowrap;
  /* 保持单行显示 */
}

.email-address {
  color: #95a5a6;
  font-size: 0.9em;
  margin-left: 8px;
}

.email-details {
  padding: 10px;
}

.email-content-box {
  width: 100%;
  font-size: 14px;
  color: #2c3e50;
  line-height: 1.6;
  background-color: #f9f9f9;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.content-label {
  font-size: 16px;
  font-weight: bold;
  margin-top: 10px;
  margin-bottom: 10px;
  color: #2c3e50;
}

.unread-dot {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 6px;
  background-color: #ff4d4f;
  color: white;
  font-size: 12px;
  border-radius: 50%;
  line-height: 1;
}

.unread-row {
  font-weight: bold;
  background-color: #fef6f6;
}

.contacts-section,
.conversation-section {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.contacts-section h3,
.conversation-section h3 {
  margin-bottom: 20px;
  font-size: 18px;
  color: #2c3e50;
}
</style>