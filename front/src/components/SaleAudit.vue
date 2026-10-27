<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button  @click="handleStatusChange('')" >显示全部</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleStatusChange('审核')">查看未审核</el-button>
                </el-col>
                <el-col :span="2">
                  <el-button  @click="handleStatusChange('已售')">查看已结束</el-button>
                </el-col>
                <el-col :span="8">
                <el-input
                  v-model="searchQuery"
                   placeholder="请输入艺术品名搜索"
                   clearable
                />
              </el-col>
              <el-col :span="2">  
              </el-col>
              <el-col :span="8">
                <div class="statistics">共 {{ filteredArtworks.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
            <el-table :data="filteredArtworks" class="my-table" height="550"  @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column  prop="artworkId"
                  label="序号"
                  width="100"
                  sortable
                  :sort-method="sortById"/>
                <el-table-column prop="name" label="艺术品名称" width="200" />
                <el-table-column prop="author" label="艺术品作者" width="200" />
                <el-table-column prop="fixedPrice" label="定价" width="120" />
                <el-table-column label="发布者" width="120"  :formatter="(row) => row.user ? row.user.username : '*'" />
                <el-table-column label="分类" width="120"  :formatter="(row) => row.category ? row.category.name : '未分类'" />
                <el-table-column prop="status" label="状态" width="120" />
                <el-table-column label="拍品信息" width="100">
                    <template #default="{ row }">
                        <el-button @click="handleViewDetails(row)" type="info" >查看</el-button>
                    </template>
                </el-table-column>
            <el-table-column  label="操作" width="200">
              <template #default="{ row }">
                <el-button :disabled="row.status === '已售'" v-if="row.status == '审核'" @click="handleEdit(row)" type="success"> 上架 </el-button>
                <el-button :disabled="row.status === '已售'" v-else @click="handleEdit(row)" type="info"> 下架 </el-button>
              </template>
            </el-table-column>
            </el-table>
          </div>
            <el-dialog
              v-model="infodialogVisible"
              title="艺术品详情"
              width="50%"
              @close="resetEditForm"
              >
              <div v-if="!imageView">
              <el-form label-width="120px">
                  <el-row :gutter="20">
                  <el-col :span="12">
                      <el-form-item label="艺术品编号 :">
                      <span>{{ selectedArtworkInFo.artworkId }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品名称 :">
                      <span>{{ selectedArtworkInFo.name }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品作者 :">
                      <span>{{ selectedArtworkInFo.author }}</span>
                      </el-form-item>
                      <el-form-item label="作品发布者 :">
                      <span>{{ selectedArtworkInFo.user?.username }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品描述 :">
                      <span>{{ selectedArtworkInFo.description }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col :span="12">
                      <el-form-item label="艺术分类 :">
                      <span>{{ selectedArtworkInFo.category?.name || '未分类' }}</span>
                      </el-form-item>
                      <el-form-item label="作品定价 :">
                      <span>{{ selectedArtworkInFo.fixedPrice }}</span>
                      </el-form-item>
                      <el-form-item label="销售形式 :">
                      <span>{{ selectedArtworkInFo.saleType }}</span>
                      </el-form-item>
                      <el-form-item label="当前状态 :">
                      <span>{{ selectedArtworkInFo.status }}</span>
                      </el-form-item>
                      <el-form-item label="创建时间 :">
                      <span>{{ selectedArtworkInFo.createdAt }}</span>
                      </el-form-item>
                      <el-form-item label="更新时间 :">
                      <span>{{ selectedArtworkInFo.updatedAt }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col>
                    <div v-if="imageUrls && imageUrls.length > 0" class="image-preview-container">
                        <div v-for="(image, index) in imageUrls" :key="index" class="image-preview-wrapper">
                            <img 
                            :src="image.imageUrl"
                            alt="Artwork Image" 
                            class="image-preview"
                            @click="viewImage(image.imageUrl)"
                            />
                        </div>
                    </div>
  
                  </el-col>
                  </el-row>
              </el-form>
            </div>
            <div v-else class="full-image">
              <img :src="selectedImageUrl" alt="Selected Artwork Image" class="full-image" @click="closeImage" title="点击缩小"/>
            </div>
          </el-dialog>
          </div>
  </template>
  
  <script>
  import {fetchArtworkImages,fetchArtworks,updateArtworkStatus} from '../utils/artworkService';
  
  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        imageUrls:[],
        saleArtworks: [],
        searchQuery: '',
        selectedStatus: '审核', 
        selectedCategories: [] ,
        editDialogVisible: false,
        infodialogVisible: false, 
        selectedArtworkInFo: {},
        editForm: {
            artworkId:'',
            status:'', 
        },
      };
    },
    computed:{
      filteredArtworks() {
        if (!this.saleArtworks || this.saleArtworks.length === 0) {
            return []; 
        }

        let filtered = this.saleArtworks.filter(artwork => {
            let nameMatches = artwork.name.toLowerCase().includes(this.searchQuery.toLowerCase());
            let isDirectSale = artwork.saleType === '直售';
            return nameMatches && isDirectSale;
        });
        if (this.selectedStatus) {
            filtered = filtered.filter(artwork => artwork.status === this.selectedStatus);
        }

        return filtered;
    }
    },
        async mounted() {
        await this.fetchArtworksList();
    },
    methods: {
      handleStatusChange(status) {
      this.selectedStatus = status;  // 改变选择的状态
    },
    async handleEdit(row) {
      try {
        // 确定新的状态
        const newStatus = row.status === '审核' ? '在售' : '审核';
        // 调用更新状态的方法
        const updatedArtwork = await updateArtworkStatus(row.artworkId, newStatus);
        // 更新本地数据
        row.status = updatedArtwork.status;
        this.$message.success('状态更新成功！');
      } catch (error) {
        this.$message.error('状态更新失败，请稍后重试！');
      }
    },
    resetEditForm() {
      this.editForm = {
          artworkId:'', 
      };
      this.selectedImageUrl = ""; 
      this.imageView = false,
      this.ImageUrls=[];
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    sortById(a, b) {
      return a.artworkId - b.artworkId; 
    },
    async fetchArtworksList() {
      try {
        const response = await fetchArtworks(); 
        this.saleArtworks = response;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    handleViewDetails(artwork){
        this.selectedArtworkInFo = artwork;
        this.fetchImages(this.selectedArtworkInFo.artworkId); 
        this.infodialogVisible = true;
    },
    fetchImages(artworkId) {
        fetchArtworkImages(artworkId)
          .then((response) => {
            this.imageUrls = response.data;
          })
          .catch((error) => {
            console.error('Error fetching images:', error)
          })
      },
      viewImage(imageUrl) {
        this.selectedImageUrl = imageUrl; 
        this.imageView = true;
      },
      closeImage(){
        this.selectedImageUrl = ""; 
        this.imageView = false;
      },
    },
  };
  </script>
  
  <style scoped src="@/assets/css/HomePage.css"></style>