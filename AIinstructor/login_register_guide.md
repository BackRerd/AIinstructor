# AI Instructor 登录注册功能使用文档

## 1. 概述
本文档详细介绍了AI Instructor系统中的用户认证功能，包括用户注册、登录、自动登录（Token验证）以及相关辅助接口的使用方法。

## 2. 接口列表

| 接口名称 | 请求URL | 请求方法 | 说明 |
| ------- | ------- | ------- | ---- |
| 用户注册 | `/auth/register` | POST | 新用户注册接口 |
| 用户登录 | `/auth/login` | POST | 现有用户登录接口 |
| 自动登录 | `/auth/verify-token` | POST | 基于Token的自动登录接口 |
| 检查用户名 | `/auth/check-username` | GET | 检查用户名是否已被注册 |
| 检查手机号 | `/auth/check-phone` | GET | 检查手机号是否已被注册 |

## 3. 详细接口说明

### 3.1 用户注册接口

#### 请求URL
`/auth/register`

#### 请求方法
POST

#### 请求体（JSON格式）
```json
{
  "username": "string",    // 用户名（必填）
  "password": "string",    // 密码（必填）
  "phoneNumber": "string", // 手机号（必填）
  "email": "string",       // 邮箱（选填）
  "wechatId": "string",    // 微信ID（选填）
  "studentId": "string"     // 学号（选填）
}
```

#### 响应格式
```json
{
  "success": boolean,       // 操作是否成功
  "message": "string",      // 响应消息
  "data": {
    "token": "string",      // JWT令牌
    "tokenType": "Bearer",  // 令牌类型
    "username": "string",   // 用户名
    "uid": "string"         // 用户唯一ID
  },
  "code": integer           // 响应代码
}
```

#### 示例

**请求示例：**
```json
POST /auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "Password123!",
  "phoneNumber": "13800138000",
  "email": "user123@example.com"
}
```

**成功响应：**
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "user123",
    "uid": "f8e7d6c5-b4a3-4210-9876-abcdef123456"
  },
  "code": 200
}
```

**失败响应：**
```json
{
  "success": false,
  "message": "用户名已存在",
  "data": null,
  "code": 500
}
```

### 3.2 用户登录接口

#### 请求URL
`/auth/login`

#### 请求方法
POST

#### 请求体（JSON格式）
```json
{
  "username": "string",    // 用户名（必填）
  "password": "string"     // 密码（必填）
}
```

#### 响应格式
```json
{
  "success": boolean,       // 操作是否成功
  "message": "string",      // 响应消息
  "data": {
    "token": "string",      // JWT令牌
    "tokenType": "Bearer",  // 令牌类型
    "username": "string",   // 用户名
    "uid": "string"         // 用户唯一ID
  },
  "code": integer           // 响应代码
}
```

#### 示例

**请求示例：**
```json
POST /auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "Password123!"
}
```

**成功响应：**
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "user123",
    "uid": "f8e7d6c5-b4a3-4210-9876-abcdef123456"
  },
  "code": 200
}
```

**失败响应：**
```json
{
  "success": false,
  "message": "用户名或密码错误",
  "data": null,
  "code": 500
}
```

### 3.3 自动登录（Token验证）接口

#### 请求URL
`/auth/verify-token`

#### 请求方法
POST

#### 请求头
```
Authorization: Bearer {token}  // 必须包含JWT令牌
```

#### 响应格式
```json
{
  "success": boolean,       // 操作是否成功
  "message": "string",      // 响应消息
  "data": {
    "token": "string",      // 刷新后的JWT令牌
    "tokenType": "Bearer",  // 令牌类型
    "username": "string",   // 用户名
    "uid": "string"         // 用户唯一ID
  },
  "code": integer           // 响应代码
}
```

#### 示例

**请求示例：**
```
POST /auth/verify-token
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**成功响应：**
```json
{
  "success": true,
  "message": "token验证成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",  // 刷新后的新令牌
    "tokenType": "Bearer",
    "username": "user123",
    "uid": "f8e7d6c5-b4a3-4210-9876-abcdef123456"
  },
  "code": 200
}
```

**失败响应示例：**
```json
// token过期
{
  "success": false,
  "message": "token已过期",
  "data": null,
  "code": 500
}

// 无效的token
{
  "success": false,
  "message": "无效的token",
  "data": null,
  "code": 500
}
```

### 3.4 检查用户名是否存在接口

#### 请求URL
`/auth/check-username?username={username}`

#### 请求方法
GET

#### 请求参数
- `username`: 要检查的用户名

#### 响应格式
```json
{
  "success": boolean,       // 操作是否成功
  "message": "string",      // 响应消息（"用户名已存在"或"用户名可用"）
  "data": boolean,          // true表示用户名已存在，false表示用户名可用
  "code": integer           // 响应代码
}
```

#### 示例

**请求示例：**
```
GET /auth/check-username?username=user123
```

**响应示例：**
```json
// 用户名已存在
{
  "success": true,
  "message": "用户名已存在",
  "data": true,
  "code": 200
}

// 用户名可用
{
  "success": true,
  "message": "用户名可用",
  "data": false,
  "code": 200
}
```

### 3.5 检查手机号是否存在接口

#### 请求URL
`/auth/check-phone?phoneNumber={phoneNumber}`

#### 请求方法
GET

#### 请求参数
- `phoneNumber`: 要检查的手机号

#### 响应格式
```json
{
  "success": boolean,       // 操作是否成功
  "message": "string",      // 响应消息（"手机号已注册"或"手机号可用"）
  "data": boolean,          // true表示手机号已存在，false表示手机号可用
  "code": integer           // 响应代码
}
```

#### 示例

**请求示例：**
```
GET /auth/check-phone?phoneNumber=13800138000
```

**响应示例：**
```json
// 手机号已注册
{
  "success": true,
  "message": "手机号已注册",
  "data": true,
  "code": 200
}

// 手机号可用
{
  "success": true,
  "message": "手机号可用",
  "data": false,
  "code": 200
}
```

## 4. Token使用说明

### 4.1 Token获取
用户成功登录或注册后，系统会返回一个JWT格式的Token，格式为：`eyJhbGciOiJIUzI1NiJ9...`

### 4.2 Token存储
前端应用应将Token安全地存储在本地，建议使用localStorage或sessionStorage存储。

### 4.3 Token使用
对于需要认证的API请求，需在请求头中添加Authorization字段：
```
Authorization: Bearer {token}
```

### 4.4 Token刷新
当用户再次打开应用时，应先调用`/auth/verify-token`接口验证本地存储的Token是否有效。如果Token有效，系统会返回一个新的Token；如果Token已过期或无效，则需要用户重新登录。

## 5. 前端实现建议

### 5.1 登录流程
1. 收集用户输入的用户名和密码
2. 调用登录接口 `/auth/login`
3. 接收响应，若成功则存储Token并跳转至主页
4. 若失败则显示错误信息

### 5.2 注册流程
1. 收集用户注册信息
2. 可先调用检查用户名和手机号接口，确保唯一性
3. 调用注册接口 `/auth/register`
4. 接收响应，若成功则存储Token并自动登录到主页
5. 若失败则显示错误信息

### 5.3 自动登录流程
1. 应用启动时，检查本地是否有存储的Token
2. 若有Token，调用验证接口 `/auth/verify-token`
3. 若Token有效，使用返回的新Token更新本地存储并自动登录
4. 若Token无效，引导用户重新登录

## 6. 错误处理

| 错误信息 | 含义 | 解决方法 |
| ------- | ---- | ------- |
| 用户名或密码错误 | 登录凭证不匹配 | 检查用户名和密码是否正确 |
| 用户名已存在 | 注册的用户名已被使用 | 更换其他用户名 |
| 手机号已被注册 | 注册的手机号已被使用 | 更换其他手机号 |
| 邮箱已被注册 | 注册的邮箱已被使用 | 更换其他邮箱 |
| token已过期 | JWT令牌已过期 | 重新登录获取新Token |
| 无效的token | JWT令牌格式错误或已被篡改 | 重新登录获取新Token |
| 无效的token签名 | JWT令牌签名验证失败 | 重新登录获取新Token |