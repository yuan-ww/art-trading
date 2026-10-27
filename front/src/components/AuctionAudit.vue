<template>
  <div > 
          <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="2">
                <el-button  @click="handleStatusChange('')" >显示全部</el-button>
              </el-col>
             <el-col :span="2">
                <el-button  @click="handleStatusChange('审核中')">查看未审核</el-button>
              </el-col>
              <el-col :span="2">
                <el-button  @click="handleStatusChange('已结束')">查看已结束</el-button>
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
              <div class="statistics">共 {{ filteredAuctions.length }} 条记录</div>
            </el-col>
          </el-row>
          <div class="table-container">
          <el-table :data="filteredAuctions" class="my-table" height="550"  @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column  prop="auctionId"
                label="序号"
                width="80"
                sortable
                :sort-method="sortById"/>
                <el-table-column prop="startingPrice" label="起拍价" width="100" />
                <el-table-column prop="depositAmount" label="保证金" width="100" />
                <el-table-column prop="currentHighestBid" label="最高价" width="100" />
                <el-table-column label="出价者" width="120" :formatter="(row) => row.highestBidder ? row.highestBidder?.username : '未出价'"/>
                <el-table-column prop="startTime" label="开始时间" width="200" />
                <el-table-column prop="endTime" label="结束时间" width="200" />
                <el-table-column prop="status" label="状态" width="100" />
                <el-table-column label="拍品信息" width="100">
                    <template #default="{ row }">
                        <el-button @click="handleViewDetails(row)" type="info" >查看</el-button>
                    </template>
                </el-table-column>
            <el-table-column  label="操作" width="200">
              <template #default="{ row }">
                <el-button v-if="row.status == '审核中'" @click="handleEdit(row)" type="success"> 审核 </el-button>
                <el-button v-else @click="handleEdit(row)" type="info"> 编辑 </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
          <el-dialog v-model="editDialogVisible" title="拍卖" width="32%" @close="resetEditForm">
            <el-form :model="editForm" ref="editForm" label-width="100px"> 
              <el-form-item label="保证金百分比" class="form-item">
                <el-select v-model="editForm.depositPercentage" placeholder="选择百分比" @change="updateDepositAmount" class="custom-input">
                  <el-option 
                    v-for="(percent, index) in [5, 10, 15, 20]" 
                    :key="index"
                    :label="percent.toFixed(2)" 
                    :value="percent" />
                    </el-select>
              </el-form-item>
              <el-form-item label="自定义保证金" class="form-item">
                <el-input v-model="editForm.depositAmount" class="custom-input" @input="updateDepositPercentage"/>
              </el-form-item>
              <el-form-item label="状态" class="form-item">
                <el-radio-group v-model="editForm.status" >
                        <el-radio-button label="审核" value="审核中" />
                        <el-radio-button label="拍卖" value="进行中" />
                        <el-radio-button label="结束" value="已结束" />
                        <el-radio-button label="取消" value="已取消" />
                    </el-radio-group>
              </el-form-item>
            </el-form>
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="editDialogVisible = false">取消</el-button>
                <el-button @click="saveEdit">保存</el-button>
              </div>
            </template>
          </el-dialog>
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
                    <span>{{ selectedAuctionInFo.artworkId }}</span>
                    </el-form-item>
                    <el-form-item label="艺术品名称 :">
                    <span>{{ selectedAuctionInFo.artwork.name }}</span>
                    </el-form-item>
                    <el-form-item label="艺术品作者 :">
                    <span>{{ selectedAuctionInFo.artwork.author }}</span>
                    </el-form-item>
                    <el-form-item label="作品发布者 :">
                    <span>{{ selectedAuctionInFo.artwork.user?.username }}</span>
                    </el-form-item>
                    <el-form-item label="艺术品描述 :">
                    <span>{{ selectedAuctionInFo.artwork.description }}</span>
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="艺术分类 :">
                    <span>{{ selectedAuctionInFo.artwork.category?.name || '未分类' }}</span>
                    </el-form-item>
                    <el-form-item label="作品定价 :">
                    <span>{{ selectedAuctionInFo.artwork.fixedPrice }}</span>
                    </el-form-item>
                    <el-form-item label="销售形式 :">
                    <span>{{ selectedAuctionInFo.artwork.saleType }}</span>
                    </el-form-item>
                    <el-form-item label="当前状态 :">
                    <span>{{ selectedAuctionInFo.artwork.status }}</span>
                    </el-form-item>
                    <el-form-item label="创建时间 :">
                    <span>{{ selectedAuctionInFo.artwork.createdAt }}</span>
                    </el-form-item>
                    <el-form-item label="更新时间 :">
                    <span>{{ selectedAuctionInFo.artwork.updatedAt }}</span>
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
import {getAllAuctions,updateAuction} from '../utils/auctionService';
import {fetchArtworkImages} from '../utils/artworkService';

export default {
  data() {
    return {
      selectedImageUrl: "",
      imageView: false,
      imageUrls:[],
      auctions: [],
      searchQuery: '',
      selectedStatus: '审核中', 
      selectedCategories: [] ,
      editDialogVisible: false,
      infodialogVisible: false, 
      selectedAuctionInFo: {},
      editForm: {
          auctionId:'',
          artworkId:'',
          startingPrice:'',
          status:'', 
          depositAmount:'',
          depositPercentage: '',   
      },
    };
  },
  computed:{
      filteredAuctions() {
          if (!this.auctions || this.auctions.length === 0) {
          return []; 
          }
          let filtered = this.auctions.filter(auction => {
        // 使用可选链操作符
          const artworkName = auction.artwork?.name;
          if (artworkName) {
              return artworkName.toLowerCase().includes(this.searchQuery.toLowerCase());
          }
        });
          // 根据状态过滤
          if (this.selectedStatus) {
            filtered = filtered.filter(auction => auction.status === this.selectedStatus);
          }
          return filtered;
    }
  },
      async mounted() {
      await this.fetchAuctionsList();
  },
  methods: {
    handleStatusChange(status) {
    this.selectedStatus = status;  // 改变选择的状态
  },
    handleEdit(row) {
    this.editForm = { ...row };
    this.editDialogVisible = true;
  },
  async saveEdit() {
      if(!this.editForm.depositAmount || !this.editForm.depositPercentage){
          this.$message.error('输入不能为空');
          return;
      } 
      try {
          await updateAuction(this.editForm.auctionId, this.editForm);
          await this.fetchAuctionsList();
          this.$message.success("更新成功")
      } catch (error) {
          alert('更新失败')
      }
    this.editDialogVisible = false; 
  },
  resetEditForm() {
    this.editForm = {
        auctionId:'',
        artworkId:'',
        startingPrice:'', 
        depositAmount:'',
        depositPercentage: '',  
    };
    this.selectedImageUrl = ""; 
    this.imageView = false,
    this.ImageUrls=[];
  },
  async handleAddCategory() {
    this.resetEditForm();
    this.editDialogVisible = true; 
  },
  handleSelectionChange(val) {
      this.selectedCategories = val;
  },
  sortById(a, b) {
    return a.auctionId - b.auctionId; 
  },
  async fetchAuctionsList() {
    try {
      const response = await getAllAuctions(); 
      this.auctions = response.data;
    } catch (error) {
      console.error('获取数据失败:', error);
    }
  },
  handleViewDetails(auction){
      this.selectedAuctionInFo = auction;
      this.fetchImages(this.selectedAuctionInFo.artworkId); 
      this.infodialogVisible = true;
  },
  updateDepositAmount() {
  if (this.editForm.depositPercentage && this.editForm.startingPrice) {
        let calculatedAmount = (this.editForm.startingPrice * this.editForm.depositPercentage) / 100;
        calculatedAmount = Math.round(calculatedAmount / 10) * 10;
        this.editForm.depositAmount = Math.min(calculatedAmount, this.editForm.startingPrice);
  }
  },
  updateDepositPercentage() {
    if (this.editForm.startingPrice && this.editForm.depositAmount) {
        const calculatedPercentage = (this.editForm.depositAmount / this.editForm.startingPrice) * 100;
        this.editForm.depositPercentage = Math.min(calculatedPercentage, 100).toFixed(2);
      }
  },
  fetchImages(artworkId) {
     console.log(artworkId);
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
  watch: {
    'editForm.depositPercentage': 'updateDepositAmountFromPercentage',
    'editForm.depositAmount': 'updateDepositPercentageFromAmount',
  },
};
</script>

<style scoped src="@/assets/css/HomePage.css"></style>