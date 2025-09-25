import axios from "axios";

const instance = axios.create({
    baseURL: '/',
    timeout: 60000
});

// 请求拦截器 - 在请求发送前添加token
instance.interceptors.request.use(
    config => {
        // 从localStorage中获取token
        const token = localStorage.getItem('token');
        // 如果token存在，则在请求头中添加Authorization字段
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器 - 处理token过期等情况
instance.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        if (error.response && error.response.status === 401) {
            // 未授权，可能是token过期或无效
            // 清除本地存储的token
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            // 跳转到登录页面
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

/**
 * 用户登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<Object>} 登录结果
 */
export const login = async (username, password) => {
    try {
        const response = await instance.post('/auth/login', {
            username,
            password
        });
        
        // 登录成功，保存token和用户信息
        if (response.data.success) {
            const { token, username, uid } = response.data.data;
            localStorage.setItem('token', token);
            localStorage.setItem('userInfo', JSON.stringify({
                username,
                uid
            }));
        }
        
        return response.data;
    } catch (error) {
        console.error("登录请求失败：", error);
        throw error;
    }
};

/**
 * 用户注册
 * @param {Object} userData - 用户注册信息
 * @returns {Promise<Object>} 注册结果
 */
export const register = async (userData) => {
    try {
        const response = await instance.post('/auth/register', userData);
        
        // 注册成功，保存token和用户信息
        if (response.data.success) {
            const { token, username, uid } = response.data.data;
            localStorage.setItem('token', token);
            localStorage.setItem('userInfo', JSON.stringify({
                username,
                uid
            }));
        }
        
        return response.data;
    } catch (error) {
        console.error("注册请求失败：", error);
        throw error;
    }
};

/**
 * 验证token并自动登录
 * @returns {Promise<Object>} 验证结果
 */
export const verifyToken = async () => {
    try {
        const response = await instance.post('/auth/verify-token');
        
        // token验证成功，更新本地token和用户信息
        if (response.data.success) {
            const { token, username, uid } = response.data.data;
            localStorage.setItem('token', token);
            localStorage.setItem('userInfo', JSON.stringify({
                username,
                uid
            }));
        }
        
        return response.data;
    } catch (error) {
        console.error("Token验证请求失败：", error);
        // 验证失败，清除本地存储的token
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        throw error;
    }
};

/**
 * 检查用户名是否已存在
 * @param {string} username - 要检查的用户名
 * @returns {Promise<Object>} 检查结果
 */
export const checkUsername = async (username) => {
    try {
        const response = await instance.get(`/auth/check-username?username=${encodeURIComponent(username)}`);
        return response.data;
    } catch (error) {
        console.error("检查用户名请求失败：", error);
        throw error;
    }
};

/**
 * 检查手机号是否已存在
 * @param {string} phoneNumber - 要检查的手机号
 * @returns {Promise<Object>} 检查结果
 */
export const checkPhoneNumber = async (phoneNumber) => {
    try {
        const response = await instance.get(`/auth/check-phone?phoneNumber=${encodeURIComponent(phoneNumber)}`);
        return response.data;
    } catch (error) {
        console.error("检查手机号请求失败：", error);
        throw error;
    }
};

/**
 * 退出登录
 */
export const logout = () => {
    // 清除本地存储的token和用户信息
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    // 跳转到登录页面
    window.location.href = '/login';
};

/**
 * 获取当前登录用户信息
 * @returns {Object|null} 用户信息或null
 */
export const getCurrentUser = () => {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
};

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export const isLoggedIn = () => {
    return !!localStorage.getItem('token');
};

export default instance;