<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <div class="logo-container">
        <h1>AI Instructor</h1>
        <p>创建新账号</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            @blur="checkUsernameUnique"
          />
          <div v-if="usernameCheckStatus" class="check-message">
            <span v-if="usernameCheckStatus.valid" class="valid-message">
              <el-icon><check /></el-icon> 用户名可用
            </span>
            <span v-else class="invalid-message">
              <el-icon><close /></el-icon> {{ usernameCheckStatus.message }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phoneNumber">
          <el-input
            v-model="registerForm.phoneNumber"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            clearable
            @blur="checkPhoneUnique"
          />
          <div v-if="phoneCheckStatus" class="check-message">
            <span v-if="phoneCheckStatus.valid" class="valid-message">
              <el-icon><check /></el-icon> 手机号可用
            </span>
            <span v-else class="invalid-message">
              <el-icon><close /></el-icon> {{ phoneCheckStatus.message }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（选填）"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="学号" prop="studentId">
          <el-input
            v-model="registerForm.studentId"
            placeholder="请输入学号（选填）"
            prefix-icon="Document"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-options">
            <el-link type="primary" @click="switchToLogin">已有账号？立即登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Check, Close } from '@element-plus/icons-vue';
import { register, checkUsername, checkPhoneNumber } from '@/api/auth';

const router = useRouter();
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phoneNumber: '',
  email: '',
  studentId: ''
});

const registerFormRef = ref(null);
const loading = ref(false);
const usernameCheckStatus = ref(null);
const phoneCheckStatus = ref(null);

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, 
      message: '密码必须包含大小写字母和数字', 
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/, 
      message: '请输入正确的手机号码格式', 
      trigger: 'blur'
    }
  ],
  email: [
    {
      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, 
      message: '请输入正确的邮箱格式', 
      trigger: 'blur'
    }
  ],
  studentId: [
    {
      pattern: /^\d{8,20}$/, 
      message: '学号只能包含数字，长度在8-20位之间', 
      trigger: 'blur'
    }
  ]
};

// 切换到登录页面
const switchToLogin = () => {
  router.push('/login');
};

// 检查用户名是否唯一
const checkUsernameUnique = async () => {
  if (!registerForm.username.trim()) {
    return;
  }

  try {
    const result = await checkUsername(registerForm.username);
    if (result.success) {
      if (result.data) {
        // 用户名已存在
        usernameCheckStatus.value = {
          valid: false,
          message: '用户名已存在'
        };
      } else {
        // 用户名可用
        usernameCheckStatus.value = {
          valid: true,
          message: '用户名可用'
        };
      }
    } else {
      usernameCheckStatus.value = {
        valid: false,
        message: result.message || '检查失败'
      };
    }
  } catch (error) {
    console.error('检查用户名失败:', error);
    usernameCheckStatus.value = {
      valid: false,
      message: error.message || '网络错误，请重试'
    };
  }
};

// 检查手机号是否唯一
const checkPhoneUnique = async () => {
  if (!registerForm.phoneNumber.trim()) {
    return;
  }

  // 先验证手机号格式
  const phonePattern = /^1[3-9]\d{9}$/;
  if (!phonePattern.test(registerForm.phoneNumber)) {
    return;
  }

  try {
    const result = await checkPhoneNumber(registerForm.phoneNumber);
    if (result.success) {
      if (result.data) {
        // 手机号已存在
        phoneCheckStatus.value = {
          valid: false,
          message: '手机号已被注册'
        };
      } else {
        // 手机号可用
        phoneCheckStatus.value = {
          valid: true,
          message: '手机号可用'
        };
      }
    } else {
      phoneCheckStatus.value = {
        valid: false,
        message: result.message || '检查失败'
      };
    }
  } catch (error) {
    console.error('检查手机号失败:', error);
    phoneCheckStatus.value = {
      valid: false,
      message: error.message || '网络错误，请重试'
    };
  }
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) {
    ElMessage.error('表单验证失败，请重试');
    return;
  }

  try {
    // 验证表单
    await registerFormRef.value.validate();
    
    // 检查用户名和手机号是否唯一（如果还未检查）
    if (!usernameCheckStatus.value || !usernameCheckStatus.value.valid) {
      await checkUsernameUnique();
      if (!usernameCheckStatus.value.valid) {
        ElMessage.error('用户名已存在或不可用');
        return;
      }
    }
    
    if (!phoneCheckStatus.value || !phoneCheckStatus.value.valid) {
      await checkPhoneUnique();
      if (!phoneCheckStatus.value.valid) {
        ElMessage.error('手机号已存在或不可用');
        return;
      }
    }
    
    loading.value = true;
    
    // 准备注册数据
    const registerData = {
      username: registerForm.username,
      password: registerForm.password,
      phoneNumber: registerForm.phoneNumber,
      email: registerForm.email || undefined,
      studentId: registerForm.studentId || undefined
    };
    
    // 调用注册API
    const result = await register(registerData);
    
    if (result.success) {
      ElMessage.success(result.message || '注册成功');
      
      // 注册成功后自动登录并跳转到首页
      setTimeout(() => {
        router.push('/');
      }, 1000);
    } else {
      ElMessage.error(result.message || '注册失败，请重试');
    }
  } catch (error) {
    console.error('注册错误:', error);
    ElMessage.error(error.message || '注册失败，请检查网络连接或稍后重试');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时的初始化
onMounted(() => {
  // 可以在这里添加一些初始化逻辑
});
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-form-wrapper {
  width: 500px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-height: 90vh;
  overflow-y: auto;
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

.register-form {
  width: 100%;
}

.check-message {
  margin-top: 5px;
  font-size: 12px;
  display: flex;
  align-items: center;
}

.valid-message {
  color: #67c23a;
}

.invalid-message {
  color: #f56c6c;
}

:deep(.el-icon) {
  margin-right: 4px;
}

.register-options {
  text-align: center;
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
@media (max-width: 600px) {
  .register-form-wrapper {
    width: 90%;
    padding: 20px;
  }
}
</style>