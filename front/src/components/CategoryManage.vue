<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button type="danger" @click="handleBatchDelete" :disabled="selectedCategories.length === 0">批量删除</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleAddCategory">新增分类</el-button>
                </el-col>
                <el-col :span="8">
                <el-input
                  v-model="searchQuery"
                   placeholder="请输入分类名搜索"
                  clearable
                />
              </el-col>
              <el-col :span="4">  
             </el-col>
              <el-col :span="8">
                <div class="statistics">共 {{ filteredCategories.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
            <el-table :data="filteredCategories" class="my-table" height="550"  @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column  prop="categoryId"
                  label="序号"
                  width="180"
                  sortable
                  :sort-method="sortById"/>
              <el-table-column prop="name" label="分类名称" width="220" />
              <el-table-column prop="description" label="分类描述" width="600" />
              <el-table-column  label="操作" width="200">
                <template #default="{ row }">
                  <el-button  @click="handleEdit(row)" type="info"> 编辑 </el-button>
                  <el-popconfirm 
                    title="确定删除分类?"
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
            <el-dialog v-model="editDialogVisible" title="分类管理" width="32%" @close="resetEditForm">
              <el-form :model="editForm" ref="editForm" label-width="100px" class="custom-form"> 
                <el-form-item label="分类名称" class="form-item">
                  <el-input v-model="editForm.name" class="custom-input"/>
                </el-form-item>
                <el-form-item label="分类描述" class="form-item">
                  <el-input v-model="editForm.description" type="textarea" :autosize="true" class="custom-input"/>
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="editDialogVisible = false">取消</el-button>
                  <el-button v-if="isEditing" @click="saveEdit">保存</el-button>
                  <el-button v-else @click="saveCategory">添加</el-button>
                </div>
              </template>
            </el-dialog>
          </div>
  </template>
  
  <script>
import { ElMessageBox} from 'element-plus';
import { fetchcategories, updateCategoryInfo,checkCategorynameAvailability,
addCategory,batchDeleteCategories} from '../utils/categoryService';
  export default {
    data() {
      return {
        categories: [],
        searchQuery: '',
        selectedCategories: [] ,
        editDialogVisible: false,
        editForm: {
            categoryId: '',
            name: '',
            description: ''
        },
        isEditing: false
      };
    },
    computed:{
        filteredCategories() {
            if (!this.categories || this.categories.length === 0) {
            return [];  // 返回空数组，避免错误
            }
            if (this.searchQuery) {
            return this.categories.filter(category =>
                category.name.toLowerCase().includes(this.searchQuery.toLowerCase())
            );
            }
            return this.categories;
  
    }
    },
        async mounted() {
        await this.fetchcategoriesList();
    },
    methods: {
      handleEdit(row) {
      this.editForm = { ...row };
      this.editDialogVisible = true;
      this.isEditing = true
    },
    async saveEdit() {
        if(!this.editForm.name){
            alert('分类名称不能为空');
            return;
        } 
        try {
          const nameAvailable = await checkCategorynameAvailability(this.editForm.name, this.editForm.id);
           if (nameAvailable) {
            alert('该分类名已存在，请选择其他分类名');
            return;
            } 
          } catch (error) {
              console.error("检查分类名出错:", error);
              return;
          }
        try {
            await updateCategoryInfo(this.editForm.id, this.editForm);
            await this.fetchcategoriesList();
            this.$message.success("更新成功")
        } catch (error) {
            alert('更新失败')
        }
      this.editDialogVisible = false; 
    },
    async saveCategory() {
        if (!this.editForm.name) {
            alert('分类名称不能为空');
            return;
        }
        try {
            const response = Number(await addCategory(this.editForm));
            const code = response;
            switch (code) {
              case 0:
                this.$message.success("分类添加成功");
                this.resetEditForm();
                this.fetchcategoriesList();
                break;
             case -1:
              this.$message.error("分类已存在");
              break;
            }
          } catch (error) {
              console.error("检查分类名出错:", error);
              return;
          }
    },
    resetEditForm() {
      this.editForm = {
        id: '',
        name: '',
        description: ''
      };
      this.isEditing = false;
    },
    async handleAddCategory() {
      this.resetEditForm();
      this.editDialogVisible = true; 
    },
    handleDelete(row) {
      const categoryIds = [row.categoryId];
      batchDeleteCategories(categoryIds).then((response) => {
      this.$message.success(response.data); 
      this.fetchcategoriesList(this.pageRole);
    }).catch((error) => {
      console.error('删除失败:', error);
      this.$message.error('删除失败');
    });
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    handleBatchDelete() {
      ElMessageBox.confirm(
        '确定删除这些分类吗?',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      .then(async () => {
        const categoryIds = this.selectedCategories.map(category => category.categoryId); 
        console.log(categoryIds);
        try {
            const response = await batchDeleteCategories(categoryIds);
            this.$message.success(response.data);
            this.selectedCategories = [];
            this.fetchcategoriesList();
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
    async fetchcategoriesList() {
      try {
        const response = await fetchcategories(); 
        this.categories = response.data;
      } catch (error) {
        console.error('获取分类数据失败:', error);
      }
    },
    }
  };
  </script>
  
  <style scoped src="@/assets/css/HomePage.css"></style>
  