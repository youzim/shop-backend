<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, register } from '../api/api'
import request from '@/utils/request'
const router = useRouter()
const userStore = useUserStore()

// 当前模式：login / register / forgot
const mode = ref('login')

const form = ref({
  phone: '',
  password: '',
  name: ''
})

// 忘记密码表单
const forgotForm = ref({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const countdown = ref(0)
const codeTimer = ref(null)

// 登录
const doLogin = async () => {
  if (!form.value.phone || !form.value.password) {
    alert('请填写手机号和密码')
    return
  }
  const res = await login({
    phone: form.value.phone,
    password: form.value.password
  })
  if (res.code === 200) {
    userStore.login(res.data)
    router.push('/home')
  } else {
    alert(res.msg || '登录失败')
  }
}

// 注册
const doRegister = async () => {
  if (!form.value.phone || !form.value.password) {
    alert('请填写手机号和密码')
    return
  }
  const res = await register({
    phone: form.value.phone,
    password: form.value.password,
    name: form.value.name || '用户' + form.value.phone.slice(-4)
  })
  if (res.code === 200) {
    alert('注册成功，请登录')
    switchMode('login')
    form.value.password = ''
  } else {
    alert(res.msg || '注册失败')
  }
}

const sendCode = async () => {
  if (!forgotForm.value.phone) {
    alert('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(forgotForm.value.phone)) {
    alert('手机号格式不正确')
    return
  }
  if (countdown.value > 0) return

  try {
    console.log('开始请求验证码...', forgotForm.value.phone)
    const res = await request.post('/user/sendCode', {
      phone: forgotForm.value.phone
    })
    console.log('验证码接口返回:', res)
    if (res.code === 200) {
      alert('验证码已发送，请注意查收')
    } else {
      alert(res.msg || '发送失败')
      return
    }
  } catch (err) {
    // 关键：打印详细错误，别只弹窗
    console.error('请求出错详情:', err)
    console.error('错误响应:', err.response)
    alert('网络错误: ' + (err.message || '请检查后端是否启动'))
    return
  }

  countdown.value = 60
  codeTimer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(codeTimer.value)
    }
  }, 1000)
}

// ========== 重置密码（调后端） ==========
const doReset = async () => {
  if (!forgotForm.value.phone || !forgotForm.value.code || !forgotForm.value.password) {
    alert('请填写完整信息')
    return
  }
  if (forgotForm.value.password !== forgotForm.value.confirmPassword) {
    alert('两次密码输入不一致')
    return
  }

  try {
    const res = await request.post('/user/resetPassword', {
      phone: forgotForm.value.phone,
      code: forgotForm.value.code,
      password: forgotForm.value.password
    })
    if (res.code === 200) {
      alert('密码重置成功，请使用新密码登录')
      switchMode('login')
      forgotForm.value = { phone: '', code: '', password: '', confirmPassword: '' }
      countdown.value = 0
      clearInterval(codeTimer.value)
    } else {
      alert(res.msg || '重置失败')
    }
  } catch (err) {
    alert('网络错误，请稍后重试')
  }
}


const switchMode = (target) => {
  mode.value = target
  form.value = { phone: '', password: '', name: '' }
}

const goHome = () => router.push('/home')
</script>

<template>
  <div class="login-wrapper">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo" @click="goHome">🛒</div>
        <h1 class="brand-title">优选好物商城</h1>
        <p class="brand-desc">品质生活，从这里开始</p>
        <div class="brand-features">
          <div class="feature-item"><span class="feature-icon">✓</span><span>正品保障</span></div>
          <div class="feature-item"><span class="feature-icon">✓</span><span>极速发货</span></div>
          <div class="feature-item"><span class="feature-icon">✓</span><span>7天无理由退换</span></div>
          <div class="feature-item"><span class="feature-icon">✓</span><span>新人专享优惠</span></div>
        </div>
      </div>
      <div class="brand-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-right">
      <div class="form-card">
        <!-- 登录 -->
        <template v-if="mode === 'login'">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录您的账户以继续购物</p>
          </div>

          <div class="input-group">
            <label>手机号</label>
            <div class="input-wrap">
              <span class="input-icon">📱</span>
              <input v-model="form.phone" type="text" placeholder="请输入手机号" maxlength="11" />
            </div>
          </div>

          <div class="input-group">
            <label>密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" />
            </div>
          </div>

          <div class="form-options">
            <label class="remember"><input type="checkbox" /><span>记住我</span></label>
            <a class="forgot" @click="switchMode('forgot')">忘记密码？</a>
          </div>

          <button class="btn-submit" @click="doLogin">登 录</button>

          <div class="divider"><span>还没有账号？</span></div>
          <button class="btn-switch" @click="switchMode('register')">立即注册</button>
        </template>

        <!-- 注册 -->
        <template v-else-if="mode === 'register'">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>填写以下信息完成注册</p>
          </div>

          <div class="input-group">
            <label>手机号</label>
            <div class="input-wrap">
              <span class="input-icon">📱</span>
              <input v-model="form.phone" type="text" placeholder="请输入手机号" maxlength="11" />
            </div>
          </div>

          <div class="input-group">
            <label>密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input v-model="form.password" type="password" placeholder="请设置密码（至少6位）" />
            </div>
          </div>

          <div class="input-group">
            <label>昵称</label>
            <div class="input-wrap">
              <span class="input-icon">👤</span>
              <input v-model="form.name" type="text" placeholder="请输入昵称（可选）" />
            </div>
          </div>

          <button class="btn-submit btn-register" @click="doRegister">注 册</button>

          <div class="divider"><span>已有账号？</span></div>
          <button class="btn-switch" @click="switchMode('login')">返回登录</button>
        </template>
      </div>

      <p class="copyright">© 2026 优选好物商城 版权所有</p>
    </div>

    <!-- ========== 忘记密码弹窗 ========== -->
    <div v-if="mode === 'forgot'" class="modal-overlay" @click.self="switchMode('login')">
      <div class="modal-card">
        <div class="modal-header">
          <h3>🔐 重置密码</h3>
          <span class="modal-close" @click="switchMode('login')">✕</span>
        </div>

        <div class="modal-body">
          <div class="input-group">
            <label>手机号</label>
            <div class="input-wrap">
              <span class="input-icon">📱</span>
              <input v-model="forgotForm.phone" type="text" placeholder="请输入注册手机号" maxlength="11" />
            </div>
          </div>

          <div class="input-group">
            <label>验证码</label>
            <div class="input-wrap code-wrap">
              <span class="input-icon">🔢</span>
              <input v-model="forgotForm.code" type="text" placeholder="请输入6位验证码" maxlength="6" />
              <button class="btn-code" :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? countdown + 's后重发' : '获取验证码' }}
              </button>
            </div>
          </div>

          <div class="input-group">
            <label>新密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input v-model="forgotForm.password" type="password" placeholder="请设置新密码" />
            </div>
          </div>

          <div class="input-group">
            <label>确认密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input v-model="forgotForm.confirmPassword" type="password" placeholder="请再次输入新密码" @keyup.enter="doReset" />
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="switchMode('login')">返回登录</button>
          <button class="btn-save" @click="doReset">确认重置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ========== 左侧品牌区 ========== */
.login-left {
  position: relative;
  flex: 1;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 50%, #ff8e8e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 40px;
}

.brand-logo {
  font-size: 72px;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.15));
  cursor: pointer;
  transition: transform 0.3s;
}
.brand-logo:hover {
  transform: scale(1.1);
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.brand-desc {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 48px;
  letter-spacing: 1px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  max-width: 280px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.95;
}

.feature-icon {
  width: 24px;
  height: 24px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.brand-decoration {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
}

.c1 { width: 400px; height: 400px; top: -100px; right: -100px; }
.c2 { width: 300px; height: 300px; bottom: 10%; left: -80px; }
.c3 { width: 200px; height: 200px; top: 40%; right: 15%; background: rgba(255,255,255,0.05); }

/* ========== 右侧表单区 ========== */
.login-right {
  width: 480px;
  min-width: 480px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 28px;
  color: #333;
  font-weight: 700;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: #999;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  opacity: 0.6;
  z-index: 1;
}

.input-wrap input {
  width: 100%;
  height: 46px;
  padding: 0 16px 0 42px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.25s;
  background: #fafafa;
}

.input-wrap input:focus {
  border-color: #ff4757;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.08);
}

.input-wrap input::placeholder {
  color: #bbb;
}

/* 验证码行 */
.code-wrap input {
  padding-right: 110px;
}

.btn-code {
  position: absolute;
  right: 4px;
  top: 4px;
  bottom: 4px;
  padding: 0 14px;
  border: none;
  background: #fff0f0;
  color: #ff4757;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-code:hover:not(:disabled) {
  background: #ff4757;
  color: #fff;
}

.btn-code:disabled {
  color: #ccc;
  background: #f5f5f5;
  cursor: not-allowed;
}

/* 辅助选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 13px;
}

.remember {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  cursor: pointer;
}

.remember input {
  width: 16px;
  height: 16px;
  accent-color: #ff4757;
  cursor: pointer;
}

.forgot {
  color: #ff4757;
  cursor: pointer;
  transition: opacity 0.2s;
}

.forgot:hover {
  opacity: 0.8;
}

/* 按钮 */
.btn-submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff4757, #ff6b81);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(255, 71, 87, 0.3);
  letter-spacing: 2px;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(255, 71, 87, 0.4);
}

.btn-submit:active {
  transform: translateY(0);
}

.btn-register {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  box-shadow: 0 4px 16px rgba(82, 196, 26, 0.3);
}

.btn-register:hover {
  box-shadow: 0 6px 24px rgba(82, 196, 26, 0.4);
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
  color: #ccc;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #eee;
}

.divider span {
  padding: 0 16px;
}

/* 切换按钮 */
.btn-switch {
  width: 100%;
  height: 44px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s;
}

.btn-switch:hover {
  border-color: #ff4757;
  color: #ff4757;
  background: #fff5f5;
}

.copyright {
  position: absolute;
  bottom: 24px;
  font-size: 12px;
  color: #ccc;
}

/* ========== 忘记密码弹窗 ========== */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.modal-card {
  width: 440px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  animation: slideUp 0.25s ease;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.modal-close {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  border-color: #999;
  color: #333;
}

.btn-save {
  padding: 10px 24px;
  border: none;
  background: linear-gradient(135deg, #ff4757, #ff6b81);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover {
  opacity: 0.9;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式 */
@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right { width: 100%; min-width: auto; }
}
</style>