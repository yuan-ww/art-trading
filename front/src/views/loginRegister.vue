<template>
	<div class="login-register">
		<div class="contain">
			<div class="big-box" :class="{ active: isLogin }">
				<div class="big-contain" v-if="isLogin">
					<div class="btitle">账户登录</div>
					<div class="bform">
						<input type="email" placeholder="邮箱" v-model="form.useremail" @blur="validateEmail">
						<input type="password" placeholder="密码" v-model="form.userpwd" @blur="validatePassword">
					</div>
					<button class="bbutton" @click="login" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
				</div>
				<div class="big-contain" v-else>
					<div class="btitle">创建账户</div>
					<div class="bform">
						<input type="text" placeholder="用户名" v-model="form.username" @input="validateUsername">
						<span class="errTips" v-if="nameExisted">用户名已存在！</span>
						<span class="errTips" v-if="nameError">用户名由3~20位数字、字母组成！</span>
						<input type="email" placeholder="邮箱" v-model="form.useremail" @input="validateEmail">
						<span class="errTips" v-if="emailExisted">该邮箱已注册！</span>
						<span class="errTips" v-if="emailError">请输入有效的邮箱地址！</span>
						<input type="password" placeholder="密码" v-model="form.userpwd" @input="validatePassword">
						<span class="errTips" v-if="pwError">密码必须至少为 6 位！</span>
					</div>
					<button class="bbutton" @click="register" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
				</div>
			</div>
			<div class="small-box" :class="{ active: isLogin }">
				<div class="small-contain" v-if="isLogin">
					<div class="stitle">你好，朋友!</div>
					<p class="scontent">开始注册，和我们一起探索</p>
					<button class="sbutton" @click="changeType">注册</button>
				</div>
				<div class="small-contain" v-else>
					<div class="stitle">欢迎回来!</div>
					<p class="scontent">与我们保持联系，请登录你的账户</p>
					<button class="sbutton" @click="changeType">登录</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import { mapActions } from 'vuex';
import { getUserById} from '@/utils/api';
export default {
	
	name: 'LoginRegister',
	data() {
		return {
			isLogin: true,
			emailError: false,
			nameError:false,
			pwError:false,
			nameExisted:false,
			emailExisted:false,
			loading: false,
			form: {
				username: '',
				useremail: '',
				userpwd: ''
			}
		}
	},
	methods: {
		...mapActions(['login']), 
		changeType() {
			this.isLogin = !this.isLogin;
			this.resetForm();
		},
		resetForm() {
			this.form.username = '';
			this.form.useremail = '';
			this.form.userpwd = '';
			this.emailError = false;
			this.passwordError = false;
			this.emailExisted = false;
			this.nameExisted = false;
			this.pwError = false;
		},
		validateEmail() {
			this.emailError = !/\S+@\S+\.\S+/.test(this.form.useremail);
			this.emailExisted = false;
		},
		validatePassword() {
			this.pwError = this.form.userpwd.length < 6;
		},
		validateUsername() {
			const usernamePattern = /^[a-zA-Z0-9]{3,20}$/; 
			this.nameError = !usernamePattern.test(this.form.username);
			this.nameExisted = false;
		},
		async login() {
			if (this.form.useremail && this.form.userpwd && !this.emailError && !this.passwordError) {
				this.loading = true;
				try {
					const response = await this.$axios.post('http://localhost:10202/api/user/login', {
						email: this.form.useremail,
						password: this.form.userpwd
					});
					this.handleLoginResponse(response);
				} catch (error) {
					this.handleError(error);
				} finally {
					this.loading = false;
				}
			} else {
				alert("请确保所有字段填写正确！");
			}
		},
		async handleLoginResponse(res) {
			console.log("API 响应:", res.data);
			const { status, user } = res.data;
			if (user && user.id) { 
				try {
					const response = await getUserById(user.id);

					if (response && response.role) {
						user.role = response.role;
					} else {
						console.warn("无法获取用户角色，使用默认角色");
						user.role = "User"; 
					}
				} catch (error) {
					console.error("获取用户角色失败:", error);
					user.role = "User"; 
				}
			}
			console.log(res.data)
			switch (status) {
				case 0:
					this.$store.commit('setUser', {
						username: user.username,
						id: user.id,
						avatarUrl: user.avatarUrl || 'http://localhost:10202/images/avatars/default.png',
						role: user.role || 'User',
					});
					this.$store.commit('setLoginStatus', true); 
					localStorage.setItem('user', JSON.stringify(user));
					this.$router.push( '/' );
					this.$message.success("登录成功")
					break;
				case -1:
					alert("邮箱未注册！");
					break;
				case 1:
					alert("密码错误！");
					break;
					}
				},
		handleError(err) {
			if (err.response) {
				alert("发生错误！");
			} 
		},
		register() {
			if (this.form.username && this.form.useremail && this.form.userpwd && !this.emailError && !this.passwordError) {
				this.loading = true;
				this.$axios.post('http://localhost:10202/api/user/add', {
					username: this.form.username,
					email: this.form.useremail,
					password: this.form.userpwd
				})
				.then(this.handleRegisterResponse)
				.catch(this.handleError)
				.finally(() => {
					this.loading = false;
				});
			} else {
				alert("请确保所有字段填写正确！");
			}
		},
		handleRegisterResponse(res) {
			switch (res.data) {
				case 0:
					this.changeType();
					this.$message.success("注册成功")
					break;
				case -1:
					this.emailExisted = true;
					break;
				case -2:
					this.nameExisted = true;
					break;
				case -3:
					this.emailExisted = true;
					this.nameExisted = true;
					break;
			}
		}
	}
}
</script>
<style scoped src="@/assets/css/loginRegister.css"></style>