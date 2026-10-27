<template>
    <div class="container">
        <div class="user-info-container left-panel">
            <div class="form-group">
            <label>原密码:</label>
            <input class="input" 
                v-model="currentPassword" 
                type="password" 
                id="currentPassword"
                required />
          </div>
          <div class="form-group">
            <label>输入新密码:</label>
            <input class="input"
                v-model="newPassword" 
                type="password" 
                id="newPassword" 
                required/> 
          </div>
          <div class="form-group">
            <label>再次输入新密码:</label>
            <input class="input"  
                v-model="confirmPassword" 
                type="password" 
                id="confirmPassword" 
                required/>
          </div>
          <div class="button-container">
            <button class="save-button" @click="submitPasswordChange">保存</button>
          </div>
        </div>
        </div>
      </template>
    <script>
import { updatePassword } from '@/utils/api';
import { mapState } from 'vuex';

export default {
  data() {
    return {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
      errorMessage: '',
      isSubmitting: false,
    };
  },
  computed: {
    ...mapState({
      userId: (state) => state.user.id, // 从 Vuex 中获取用户 ID
    }),
  },
  methods: {
    validatePasswords() {
      if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
        alert('输入不能为空');
        return false;
      }
      if (this.newPassword !== this.confirmPassword) {
        alert('两次密码不一致');
        return false;
      }
      if (this.newPassword === this.currentPassword) {
        alert('新密码不能与原密码相同');
        return false;
      }
      this.errorMessage = ''; 
      return true;
    },
    async submitPasswordChange() {
      if (!this.validatePasswords()) return;
      try {
        const response = await updatePassword(this.userId, this.currentPassword, this.newPassword);
        if (response==="密码更新成功") {
          this.$message.success(response)
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmPassword = '';
        } else {
            alert(response);
        }
      } catch (error) {
        console.error(error);
      }
    }
  }
};

</script>
<style scoped src="@/assets/css/UserDetails.css"></style>