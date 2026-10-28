<template>
<div class="container">
  <div class="user-info-container right-panel" :class="{ 'shifted-right': showrechargeForm ,'hidden': showRealNameForm}">
            <h2>余额充值</h2>
            <div class="form-group">
                <label>请输入充值金额:</label>
                <input v-model="rechargeAmount" class="input" placeholder=" " />
            </div>
            <div class="button-container">
                <el-button class="save-button" @click="submitRecharge">去支付</el-button>
                <el-button class="cancel-button" @click="cancelRealNameForm">取消</el-button>
            </div>
        </div>
    <div class="user-info-container right-panel" :class="{ 'shifted-right': showRealNameForm ,'hidden':showrechargeForm}">
            <h2>输入实名信息</h2>
            <div class="form-group">
                <label>姓名:</label>
                <input v-model="user.realName" class="input" placeholder="请输入姓名" />
            </div>
            <div class="form-group">
                <label>身份证号:</label>
                <input v-model="user.idCardNumber" class="input" placeholder="请输入身份证号" />
            </div>
            <div class="button-container">
                <el-button class="save-button" @click="submitRealNameInfo">提交</el-button>
                <el-button class="cancel-button" @click="cancelRealNameForm">取消</el-button>
            </div>
        </div>
    <div class="user-info-container left-panel" :class="{ 'shifted-left': showRealNameForm || showrechargeForm}">
      <h2 class="title">个人信息</h2>
      <div  class="form-group avatar-container">
        <img :src="user.avatar || require('@/assets/images/avatars/avatar.png') " alt="头像" @click="uploadAvatar"  title="点击更改头像" class="avatar" style="cursor: pointer; width: 100px; height: 100px;" />
        <input type="file" ref="avatarInput" @change="handleAvatarUpload" accept="image/jpeg,image/png,image/gif" style="display: none;" />
      </div>
      <div class="real-info">
                <el-button v-if="!isRealName" class="real-info-bt" @click="toggleRealNameForm">
                    去实名
                </el-button>          
                <el-popover placement="right" :width="390" trigger="click" v-if="isRealName">
                <template #reference>
                    <el-button class="real-info-bt">实名信息</el-button>
                </template>
                    <el-table v-if="isRealName" :data="gridData">
                        <el-table-column width="80" property="name" label="姓名" />
                        <el-table-column width="280" property="IdNumber" label="身份证号" />
                    </el-table>
                </el-popover>
            </div>
        <div class="form-group">
        <label>用户名:</label>
        <input class="input" v-model="user.username"/>
      </div>
      <div class="form-group">
        <label>电子邮件:</label>
        <input class="input" v-model="user.email" disabled /> 
      </div>
      <div class="form-group">
        <label>电话号码:</label>
        <input class="input" v-model="user.phone" @input="filterPhoneInput"/>
      </div>
      <div class="form-group">
        <label>地址:</label>
        <input class="input" v-model="user.address" />
      </div>
      <div class="form-group balance-container">
            <label>余额:</label>
            <div class="balance-input-container"> 
                <input class="input balance-input" v-model="user.walletBalance" disabled />
                <button class="recharge-button" @click="recharge">充值</button>
        </div>
      </div>
      <div class="button-container">
        <button class="save-button" @click="updateUser">保存</button>
      </div>
    </div>
    </div>
  </template>
<script>
import { getUserById, updateUserInfo, uploadAvatar,
    checkUsernameAvailability, getRealNameInfo,recharge} from '@/utils/api'; // 确保路径是正确的
import { mapState } from 'vuex';
  
  export default {
    data() {
      return {
        rechargeAmount: null,
        showRealNameForm: false,
        showrechargeForm:false, 
        isRealName: false,
        gridData: [],
        user: {
          username: '',
          phone:'',
          email: '',
          address:'',
          walletBalance:'',
          avatar: '',
          realName: '', 
          idCardNumber: '', 
        },
      };
    },
    computed: {
      ...mapState({
        userId: (state) => state.user.id, 
      }),
    },
    async created() {
      await this.fetchUserInfo();
      await this.checkRealName();
    },
    methods: {
      filterPhoneInput(event) {
      const input = event.target.value;
      const filteredInput = input.replace(/\D/g, '');
      this.user.phone = filteredInput;
    },
        toggleRealNameForm() {
            this.showRealNameForm = !this.showRealNameForm; 
        },
        recharge(){
          this.showrechargeForm = !this.showrechargeForm; 
        },
       async fetchUserInfo() {
        try {
          const userData = await getUserById(this.userId);
          this.user = userData;
        } catch (error) {
          console.error('获取用户信息失败:', error);
        }
      },
      async updateUser() {
        if(!this.user.username){
            alert('用户名不能为空');
            return;
        } else if (this.user.username.length < 3 || this.user.username.length > 20) {
            alert('用户名长度应在 3 到 20 个字符之间');
            return;
        } 
        try {
        const usernameAvailable = await checkUsernameAvailability(this.user.username, this.userId);
            if (usernameAvailable === true) {
                alert('该用户名已存在，请选择其他用户名');
                return;
            }
        } catch (error) {
            console.error("检查用户名出错:", error);
            alert('检查用户名时出错');
            return;
        }
        try {
            const updatedUser = await updateUserInfo(this.userId, this.user);
            this.$store.commit('setUser', {
            username: updatedUser.username, 
            avatarUrl: this.$store.state.user.avatarUrl, 
            id: this.userId
            });
            this.$message.success("更新成功")
        } catch (error) {
            alert('更新失败')
        }
     },
     async checkRealName() {
        try {
                const realNameInfo = await getRealNameInfo(this.userId);
                if (realNameInfo && realNameInfo.realName && realNameInfo.idCardNumber) {
                    this.isRealName = true; 
                    this.gridData = [{ 
                        name: realNameInfo.realName,
                        IdNumber: realNameInfo.idCardNumber,
                    }];
                } else {
                    this.isRealName = false; 
                    this.gridData = []; 
                }
            } catch (error) {
                this.isRealName = false; 
                this.gridData = []; 
            }
        },
        submitRealNameInfo() {
          if(!this.user.idCardNumber || !this.user.idCardNumber)
          {
            alert('输入不能为空');
            return ;
          }else if (this.user.idCardNumber.length !==16) {
              alert('请输入16位身份证号码');
              return;
          }else{
            this.updateUser();
            this.checkRealName();
            this.cancelRealNameForm();
          }
        },
     uploadAvatar() {
      this.$refs.avatarInput.click();
     },
     async handleAvatarUpload(event) {
        const file = event.target.files[0];
        if (!file) {
        alert('请先选择一个文件。');
        return;
        }
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!allowedTypes.includes(file.type)) {
            alert('请上传有效的图像文件（JPEG, PNG, GIF）');
            return;
        }
        if (file) {
            try {
            const avatarUrl = await uploadAvatar(this.userId,file); 
            this.$store.dispatch('updateAvatar', avatarUrl);
            this.$message.success("头像更新成功")
            await this.fetchUserInfo(); 
            } catch (error) {
            console.error('上传头像失败:', error);
            }
        }
     },
        async submitRecharge() {
          if(isNaN(this.rechargeAmount) || this.rechargeAmount ===null){
            this.$message.error("请输入有效金额")
            return
          }
         try{
          const response = await recharge(this.userId, this.rechargeAmount);
        if (response.data === "充值成功") {
            this.$message.success("充值成功")
            await this.fetchUserInfo();
            this.rechargeAmount = null;
          } else {
            alert(response.data);
          }
         }catch{
          this.$message.error("请输入有效金额")
         }
        },
        cancelRealNameForm() {
            this.showRealNameForm = false;
            this.showrechargeForm = false; 
            this.realName = ''; 
            this.idCardNumber = '';
        },
    },
  };
</script>
<style scoped src="@/assets/css/UserDetails.css"></style>
