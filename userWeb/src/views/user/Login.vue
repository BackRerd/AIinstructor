<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <div class="logo-container">
        <h1>AI Instructor</h1>
        <p>智能教学助手平台</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
            <el-link type="primary" @click="switchToRegister">没有账号？立即注册</el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '@/api/auth';

const router = useRouter();
const loginForm = reactive({
  username: '',
  password: ''
});

const loginFormRef = ref(null);
const rememberPassword = ref(false);
const loading = ref(false);

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
};

// 切换到注册页面
const switchToRegister = () => {
  router.push('/register');
};

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) {
    ElMessage.error('表单验证失败，请重试');
    return;
  }

  try {
    // 验证表单
    await loginFormRef.value.validate();
    
    loading.value = true;
    
    // 调用登录API
    const result = await login(loginForm.username, loginForm.password);
    
    if (result.success) {
      ElMessage.success(result.message || '登录成功');
      
      // 记住密码功能
      if (rememberPassword.value) {
        localStorage.setItem('rememberedUsername', loginForm.username);
        localStorage.setItem('rememberedPassword', loginForm.password);
      } else {
        localStorage.removeItem('rememberedUsername');
        localStorage.removeItem('rememberedPassword');
      }
      
      // 登录成功后跳转到首页
      router.push('/');
    } else {
      ElMessage.error(result.message || '登录失败，请重试');
    }
  } catch (error) {
    console.error('登录错误:', error);
    ElMessage.error(error.message || '登录失败，请检查网络连接或稍后重试');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时，检查是否有记住的密码
onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername');
  const rememberedPassword = localStorage.getItem('rememberedPassword');
  
  if (rememberedUsername && rememberedPassword) {
    loginForm.username = rememberedUsername;
    loginForm.password = rememberedPassword;
    rememberPassword.value = true;
  }
});
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form-wrapper {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.logo-container {
  text-align: center;
  margin-bottom: 30px;
}

.logo-container h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-container p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 自定义输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

/* 自定义按钮样式 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  padding: 10px;
  font-size: 16px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
}

/* 响应式设计 */
@media (max-width: 500px) {
  .login-form-wrapper {
    width: 90%;
    padding: 20px;
  }
}
</style>