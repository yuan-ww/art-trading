<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button type="danger" @click="handleBatchDelete" :disabled="selectedUsers.length === 0">批量删除</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleAddUser">新增用户</el-button>
                </el-col>
                <el-col :span="8">
                <el-input
                  v-model="searchQuery"
                   placeholder="请输入用户名搜索"
                  clearable
                />
              </el-col>
              <el-col :span="4">  
            </el-col>
              <el-col :span="8">
                <div class="statistics">共 {{ filteredUsers.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
            <el-table :data="filteredUsers" class="my-table" height="550"  @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column  prop="userId"
                  label="序号"
                  width="100"
                  sortable
                  :sort-method="sortById"/>
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="realName" label="姓名" width="120" />
              <el-table-column prop="email" label="邮箱" width="180" />
              <el-table-column prop="phone" label="电话" width="180" />
              <el-table-column prop="address" label="地址" width="180" />
              <el-table-column prop="walletBalance" label="余额" width="120" />
              <el-table-column  label="操作" width="200">
                <template #default="{ row }">
                  <el-button  @click="handleEdit(row)" type="info"> 编辑 </el-button>
                  <el-popconfirm 
                    title="确定删除用户?"
                    @confirm="() => handleDelete(row)"
                    >
                    <template #reference>
                      <el-button type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
            <el-dialog v-model="editDialogVisible" title="用户管理" width="32%" @close="resetEditForm">
              <el-form :model="editForm" ref="editForm" label-width="100px" class="custom-form"> 
                <el-form-item label="用户名" class="form-item">
                  <el-input v-model="editForm.username" class="custom-input"/>
                </el-form-item>
                <el-form-item label="姓名" class="form-item">
                  <el-input v-model="editForm.realName" class="custom-input"/>
                </el-form-item>
                <el-form-item label="邮箱" class="form-item">
                  <el-input v-model="editForm.email"  class="custom-input"/>
                </el-form-item>
                <el-form-item label="电话" class="form-item">
                  <el-input v-model="editForm.phone" class="custom-input"/>
                </el-form-item>
                <el-form-item label="余额" class="form-item">
                  <el-input v-model="editForm.walletBalance" class="custom-input"/>
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="editDialogVisible = false">取消</el-button>
                  <el-button v-if="isEditing" @click="saveEdit">保存</el-button>
                  <el-button v-else @click="saveUser">添加</el-button>
                </div>
              </template>
            </el-dialog>
          </div>
  </template>
  
  <script>
import { ElMessageBox} from 'element-plus';
import { fetchUsersByRole, updateUserInfo,checkUsernameAvailability,
  checkEmailAvailability,addUser,batchDeleteUsers} from '../utils/api';
  export default {
    data() {
      return {
        selectedFile: null,
        users: [],
        searchQuery: '',
        selectedUsers: [] ,
        editDialogVisible: false,
        editForm: {
            userId: '',
            username: '',
            realName: '',
            password:'',
            email: '',
            phone: '',
            role:'User',
            address:'',
            walletBalance: ''
        },
        isEditing: false
      };
    },
    computed:{
        filteredUsers() {
      if (this.searchQuery) {
        return this.users.filter(user =>
          user.username.toLowerCase().includes(this.searchQuery.toLowerCase()) 
        );
      }
      return this.users; 
    }
    },
        async mounted() {
        await this.fetchUsersList();
    },
    methods: {
      handleEdit(row) {
      this.editForm = { ...row };
      this.editDialogVisible = true;
      this.isEditing = true
    },
    async saveEdit() {
        if(!this.editForm.username || !this.editForm.email){
            alert('用户名和邮箱不能为空');
            return;
        } else if (this.editForm.username.length < 3 || this.editForm.username.length > 20) {
            alert('用户名长度应在 3 到 20 个字符之间');
            return;
        }  
        try {
          const usernameAvailable = await checkUsernameAvailability(this.editForm.username, this.editForm.userId);
          const emailAvailable = await checkEmailAvailability(this.editForm.email, this.editForm.userId);
           if (usernameAvailable) {
            alert('该用户名已存在，请选择其他用户名');
            return;
            } else if (emailAvailable){
                alert('该邮箱已被注册，请选择其他邮箱');
                return;
            }
          } catch (error) {
              console.error("检查用户名出错:", error);
              alert('检查用户名时出错');
              return;
          }
        try {
            await updateUserInfo(this.editForm.userId, this.editForm);
            await this.fetchUsersList(this.pageRole);
            this.$message.success("更新成功")
        } catch (error) {
            alert('更新失败')
        }
      this.editDialogVisible = false; 
    },
    async saveUser() {
      if(!this.editForm.username || !this.editForm.email){
            alert('用户名和邮箱不能为空');
            return;
        } else if (this.editForm.username.length < 3 || this.editForm.username.length > 20) {
            alert('用户名长度应在 3 到 20 个字符之间');
            return;
        }  
        try {
            const response = Number(await addUser(this.editForm));
            const code = response;
            switch (code) {
              case 0:
                this.$message.success("添加用户成功");
                this.resetEditForm();
                await this.fetchUsersList(this.pageRole);
                break;
              case -1:
                this.$message.error("邮箱已被注册");
                break;
              case -2:
                this.$message.error("用户名已被使用");
                break;
              case -3:
                this.$message.error("当前用户名和邮箱不可用");
                break;
              default:
                this.$message.error("发生未知错误");
            }   
          } catch (error) {
              console.error("出错了:", error);
              return;
          }
    },
    resetEditForm() {
      this.editForm = {
        userId: '',
        username: '',
        realName: '',
        email: '',
        phone: '',
        walletBalance: '',
      };
      this.isEditing = false;
    },
    async handleAddUser() {
      this.editDialogVisible = true; 
    },
    handleDelete(row) {
      const userIds = [row.userId];
      console.log(userIds)
      batchDeleteUsers(userIds).then((response) => {
      this.$message.success(response.data); 
      this.fetchUsersList(this.pageRole);
    }).catch((error) => {
      console.error('删除失败:', error);
      this.$message.error('删除失败');
    });
    },
    handleSelectionChange(val) {
    this.selectedUsers = val;
    },
    handleBatchDelete() {
      ElMessageBox.confirm(
        '确定删除这些用户吗?',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      .then(async () => {
          const userIds = this.selectedUsers.map(user => user.userId);

          try {
            const response = await batchDeleteUsers(userIds);
            this.$message.success(response.data);
            this.selectedUsers = [];
            this.fetchUsersList();
          } catch (error) {
            this.$message.error('删除失败');
          }
        })
        .catch(() => {
          this.$message.info('删除取消');
        })
    },
    sortById(a, b) {
      return a.id - b.id; 
    },
    async fetchUsersList() {
      try {
        const response = await fetchUsersByRole('User'); 
        this.users = response.data; 
      } catch (error) {
        console.error('获取用户数据失败:', error);
      }
    },
    }
  };
  </script>
  
  <style scoped src="@/assets/css/HomePage.css"></style>
  