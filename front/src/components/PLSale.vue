<template>
    <div>
        <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="6">
                <el-button  @click="handleAddArtwork">发布艺术品</el-button>
            </el-col>
            <el-col :span="8">
                <el-input
                    v-model="searchQuery"
                    placeholder="输入艺术品名称搜索"
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
            <div v-if="isLoading" class="loading-overlay">
            <div class="loading-spinner"></div>
            </div>
            <transition-group
            name="fade-in"
            tag="div"
            :class="['product-list', { 'filter-enabled': showFilter }]"
            v-if="filteredArtworks.length > 0 && !isLoading"
            >
            <div v-for="artwork in filteredArtworks" :key="artwork.artworkId" class="product-item">
                <div class="product-details">
                <div class="name-author">
                    <p class="product-name">《{{ artwork.name }}》</p>
                    <p class="product-author">{{ artwork.author }}</p>
                </div>
                <div class="price-status">
                    <p class="starting-price">￥{{ artwork.fixedPrice }}</p>
                    <p class="auntion-status">{{ artwork.status }}</p>
                    <p class="product-author">{{ artwork.saleType }}</p>
                </div>
                </div>
                <img :src="artwork.images[0].imageUrl" alt="商品图片" class="product-image" />
                <div class="action-buttons">
                <el-button @click="handleViewDetails(artwork)">查看</el-button>
                <el-button @click="handleEdit(artwork)" :disabled="artwork.status === '已售'">编辑</el-button>
                <el-popconfirm 
                    title="确定删除艺术品?"
                    @confirm="() => handleDelete(artwork)"
                >
                    <template #reference>
                    <el-button>删除</el-button>
                    </template>
                </el-popconfirm>
                </div>
            </div>
            </transition-group>
            <div v-else-if="!isLoading" class="nodata">
            <p>没有数据~</p>
            </div>
        </div>
        <el-dialog v-model="editDialogVisible" title="艺术品管理" width="60%" height="50%" @close="resetEditForm">
            <el-row :gutter="20">
            <el-col :span="8">
            <el-form :model="editForm" label-width="80px" :rules="formRules">
                <el-form-item label="名称">
                    <el-input v-model="editForm.name" />
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="editForm.description" type="textarea" />
                </el-form-item>
                <el-form-item label="作者">
                    <el-input v-model="editForm.author" />
                </el-form-item>
                <el-form-item label="分类">
                    <el-select v-model="editForm.categoryId" placeholder="选择分类">
                    <el-option v-for="category in categories"
                        :key="category.categoryId"
                        :label="category.name"
                        :value="category.categoryId"  />
                    </el-select>
                </el-form-item>
                <el-form-item label="形式">
                    <el-radio-group v-model="editForm.saleType" >
                        <el-radio-button label="直售" value="直售" />
                        <el-radio-button label="拍卖" value="拍卖" />
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="定价">
                    <el-input v-model="editForm.fixedPrice" />
                </el-form-item>
                <div  v-if="editForm.saleType == '拍卖'" class="auction-info-container">
                    <el-form-item label="开始">
                    <el-date-picker 
                    v-model="auctionForm.startTime"
                     type="datetime" 
                     placeholder="选择开始时间"            
                    :disabled-date="disabledStartDate" 
                      @change="handleStartTimeChange"/>
                    </el-form-item>
                    <el-form-item label="结束">
                    <el-date-picker
                     v-model="auctionForm.endTime"
                      type="datetime" 
                      placeholder="选择结束时间"
                     :disabled-date="disabledEndDate"/>
                    </el-form-item>
                </div>            
                </el-form>
            </el-col>
            <el-col :span="16">
                <div style="display: flex; flex-wrap: wrap; justify-content: flex-start; gap: 10px;">
                    <div v-if="imagePreviewUrls && imagePreviewUrls.length > 0" class="image-preview-container">
                    <div v-for="(url, index) in imagePreviewUrls" :key="index" class="image-preview-wrapper">
                        <img :src="url" alt="Image Preview" @click="removeImage(index)" class="image-preview" title="点击删除"/>
                    </div>
                    </div>
                        <div v-if="artworkImages && artworkImages.length > 0" class="image-preview-container">
                            <div v-for="(image, index) in artworkImages" :key="index" class="image-preview-wrapper">
                                <img 
                                :src="image.imageUrl"
                                alt="Artwork Image" 
                                class="image-preview"
                                @click="artworkImages.length > 1 ? deleteImageHandler(image.imageId) : null"
                                :title="artworkImages.length === 1 ? '无法删除最后一张图片' : '点击删除'"
                                :style="{ cursor: artworkImages.length === 1 ? 'not-allowed' : 'pointer' }"
                                />
                            </div>
                        </div>
                    <div>
                    <button v-if="!isEditing" @click="triggerFileInput" class="upload-button">点击上传图片</button>
                    <input
                        type="file"
                        ref="imageInput"
                        class="upload-input"
                        @change="handleFileUpload"
                        multiple
                        accept="image/*"
                        hidden
                    />
                    </div>
                </div>
            </el-col>
        </el-row>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="editDialogVisible = false">取消</el-button>
                    <el-button v-if="isEditing" @click="saveEdit">保存</el-button>
                    <el-button v-else @click="saveArtwork">添加</el-button>
                </div>
            </template>
        </el-dialog>
        <el-dialog
        v-model="infodialogVisible"
        title="艺术品详情"
        width="50%"
        >
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
            </el-row>
        </el-form>
    </el-dialog>
    </div>
</template>
  
<script>
import { mapState } from 'vuex';
import {  updateArtworkInfo,addArtwork, batchDeleteArtworks,createArtworkWithAuction,
    fetchArtworkImages,fetchCategories ,fetchArtworksByUserId,deleteArtworkImage } from '../utils/artworkService';
import {getAuctionsByArtworkId,createAuction,updateAuction,deleteAuction } from '../utils/auctionService';

export default {
    data() {
        return { 
            isLoading:true,
            imagePreviewUrls:[],
            artworks: [],
            images: [],
            artworkImages: [],
            categories: [], 
            searchQuery: '',
            selectedArtworks: [],
            editDialogVisible: false,
            infodialogVisible: false, 
            selectedArtworkInFo: {},
            editForm: {
                artworkId: '',     
                name: '',           
                description: '',     
                author: '',          
                categoryId: '',     
                userId:'',
                fixedPrice: '',    
                saleType:'',
                status: '',       
            },
            isEditing: false,
            auctionForm: {
                auctionId:'',
                artworkId: '', 
                startingPrice: '',      
                depositPercentage: '',   
                startTime: null,        
                endTime: null,
            },
            formRules: {
            fixedPrice: [
                { required: true, message: '请输入定价', trigger: 'blur' },
                { 
                pattern: /^\d+(\.\d{1,2})?$/, 
                message: '定价格式不正确', 
                trigger: 'blur' 
                }
            ]
            }
        };  
    },
    computed: {
        ...mapState({
        userId: state => state.user.id,
        }),
        filteredArtworks() {
            if (!Array.isArray(this.artworks) || this.artworks.length === 0) {
                 return [];
            }   
            if (this.searchQuery && this.searchQuery.trim()) {
                return this.artworks.filter(artwork =>
                    artwork.name && artwork.name.toLowerCase().includes(this.searchQuery.toLowerCase())
                );
            }
            return this.artworks;
        },
        shouldShowAuctionDialog() {
            return this.editForm.saleType === '拍卖';
        }
    },
    mounted() {  
        this.fetchArtworksList();
        this.fetchCategoriesList();
    },
    methods: {
        disabledStartDate(time) {
            return time.getTime() < new Date().getTime(); 
        },
        handleStartTimeChange() {
            this.auctionForm.endTime = null; 
        },
        disabledEndDate(time) {
            return time.getTime() < new Date(this.auctionForm.startTime).getTime();
        },
        deleteImageHandler(imageId) {
        this.$confirm('您确定要删除该图片吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
            }).then(() => {
                deleteArtworkImage(imageId)
                .then(() => {
                    this.$message.success('图片删除成功');
                    this.artworkImages= this.artworkImages.filter(image => image.imageId !== imageId);
                })
                .catch(error => {
                    console.error('图片删除失败:', error);
                    this.$message.error('图片删除失败');
                });
            }).catch(() => {
                this.$message.info('取消删除');
            });
        },
        fetchAuctions(artworkId){
            getAuctionsByArtworkId(artworkId)
                .then((response) => {
                        this.auctionForm = response.data; 
                        this.auctionForm.startTime = new Date(this.auctionForm.startTime); 
                        this.auctionForm.endTime = new Date(this.auctionForm.endTime); 
                        this.auctionForm.startTime.setHours(this.auctionForm.startTime.getHours() - 8);
                        this.auctionForm.endTime.setHours(this.auctionForm.endTime.getHours() - 8); 
                })
                .catch((error) => {
                console.error('Error fetching Auctions:', error);
                });
        },
        fetchImages(artworkId) {
            fetchArtworkImages(artworkId)
                .then((response) => {
                    this.artworkImages = response.data; 
                })
                .catch((error) => {
                console.error('Error fetching images:', error);
                });
        },
        triggerFileInput() {
        const fileInput = this.$refs.imageInput; 
        fileInput.click(); 
        },
        removeImage(index) {
            this.imagePreviewUrls.splice(index, 1);
            this.images.splice(index, 1);
        },
        handleFileUpload(event) {
            const files = event.target.files ? Array.from(event.target.files) : [];  
            files.forEach(file => {
                if (file.type === 'image/gif' || file.name.toLowerCase().endsWith('.gif')) {
                    this.$message.error('GIF图片不允许上传');
                    return; 
                }
                const fileUrl = URL.createObjectURL(file);
                this.images.push(file); 
                this.imagePreviewUrls.push(fileUrl); 
            });
        },
        filteredCategories() {
            if (!this.categories || this.categories.length === 0) {
            return []; 
            }
            if (this.searchQuery) {
            return this.categories.filter(category =>
                category.name.toLowerCase().includes(this.searchQuery.toLowerCase())
            );
        }
        return this.categories;
        },
        handleEdit(row) {
            this.editForm = { ...row };
            this.fetchImages(this.editForm.artworkId);
            if(this.editForm.saleType == "拍卖"){
                this.fetchAuctions(this.editForm.artworkId);
            }
            this.editDialogVisible = true;
            this.isEditing = true;
        },
        async saveEdit() {
            const requiredFields = [
                { field: 'name', message: '请输入名称' },
                { field: 'description', message: '请输入描述' },
                { field: 'author', message: '请输入作者' },
                { field: 'fixedPrice', message: '请输入定价' },
            ];
            for (const { field, message } of requiredFields) {
                if (!this.editForm[field]) {
                    this.$message.error(message);
                    return;
                }
            }
            try {
                    if (this.editForm.saleType=='拍卖') {
                    const response = await getAuctionsByArtworkId(this.editForm.artworkId)
                    const auction =response.data
                    const now = new Date(); 
                    const startTime = new Date(auction.startTime);
                    const endtTime = new Date(auction.endTime);
                    if (now >= startTime && now <= endtTime) {
                        this.$message.warning('拍卖已开始，不可修改');
                        return;
                    }
                }
                await updateArtworkInfo(this.editForm.artworkId, this.editForm);
                if(this.editForm.saleType == "拍卖"){
                    this.auctionForm.artworkId=this.editForm.artworkId;
                    this.auctionForm.startingPrice=this.editForm.fixedPrice;
                    this.auctionForm.startTime.setHours(this.auctionForm.startTime.getHours() + 8);
                    this.auctionForm.endTime.setHours(this.auctionForm.endTime.getHours() + 8);
                    this.auctionForm.startTime =this.auctionForm.startTime.toISOString();
                    this.auctionForm.endTime =this.auctionForm.endTime.toISOString();
                    if(!this.auctionForm.auctionId){ 
                       await createAuction(this.auctionForm)
                    }else{
                       await updateAuction(this.auctionForm.auctionId,this.auctionForm)
                    }                   
                }else{
                   if(this.auctionForm.auctionId){
                      await deleteAuction(this.auctionForm.auctionId);                     
                   }
                }
                await this.fetchArtworksList();
                this.$message.success("更新成功");
            } catch (error) {
                alert('更新失败');
            }
            this.editDialogVisible = false;
        },
        async saveArtwork() {
            if (!this.editForm.userId) {
                this.editForm.userId = this.userId; 
            }
            const requiredFields = [
                { field: 'name', message: '请输入名称' },
                { field: 'description', message: '请输入描述' },
                { field: 'author', message: '请输入作者' },
                { field: 'fixedPrice', message: '请输入定价' },
                { field: 'categoryId', message: '请选择分类' },
                { field: 'saleType', message: '请选择售卖类型' }
            ];
            for (const { field, message } of requiredFields) {
                if (!this.editForm[field]) {
                    this.$message.error(message);
                    return;
                }
            }
            if(!this.images || this.images.length === 0){
                this.$message.error('请上传图片');
                return;
            }
            let formData = new FormData();
            if(this.editForm.saleType == "拍卖" ){
                this.auctionForm.startingPrice=this.editForm.fixedPrice;
                this.auctionForm.startTime.setHours(this.auctionForm.startTime.getHours() + 8);//调整时区转换导致的时差
                this.auctionForm.endTime.setHours(this.auctionForm.endTime.getHours() + 8);
                this.auctionForm.startTime =this.auctionForm.startTime.toISOString();
                this.auctionForm.endTime =this.auctionForm.endTime.toISOString();
                for (const key in this.auctionForm) {
                if (Object.prototype.hasOwnProperty.call(this.auctionForm, key)) {
                    formData.append(key, this.auctionForm[key]);
                }
            }
            }          
            for (const key in this.editForm) {
                if (Object.prototype.hasOwnProperty.call(this.editForm, key)) {
                    formData.append(key, this.editForm[key]);
                }
            }
                this.images.forEach((image) => {
                formData.append(`images`, image);
            });
            try {
                let response;
                if(this.editForm.saleType == "拍卖"){
                    for (let pair of formData.entries()) {
                        console.log(pair[0] + ': ' + pair[1]);
                    }
                    response = await createArtworkWithAuction(formData);
                }else{
                    response = await addArtwork(formData);  
                }                       
                if(response){         
                    this.$message.success("艺术品添加成功");
                    this.editDialogVisible=false
                    this.resetEditForm();
                    this.fetchArtworksList();
                }else{
                    this.$message.error("艺术品添加失败");
                }             
            } catch (error) {
                console.error("添加艺术品出错:", error);
                return;
            }
        },
        resetEditForm() {
            this.editForm = {
                name: '',
                description: '',
                author: '',
            };
            this.auctionForm = {
                auctionId:'',
                artworkId:'',
                startingPrice: '',
                depositPercentage: '',
                startTime: null,
                endTime: null,
            };
            this.artworkImages=[];
            this.imagePreviewUrls =  [];
            this.images = [];
            this.isEditing = false;
            this.htmlContent = '';const editor = this.$el.querySelector('.editor');
            if (editor) {
                editor.innerHTML = ''; 
            }
        },
        async handleAddArtwork() {
            this.resetEditForm();
            this.editDialogVisible = true;
        },
        async handleDelete(row) {
            if (row.saleType=='拍卖') {
                const response = await getAuctionsByArtworkId(row.artworkId)
                const auction =response.data
                const now = new Date(); 
                const startTime = new Date(auction.startTime);
                 const endtTime = new Date(auction.endTime);
                if (now >= startTime && now <= endtTime) {
                    this.$message.warning('拍卖已开始，无法删除');
                    return;
                }
            }
            const artworkIds = [row.artworkId];
            batchDeleteArtworks(artworkIds).then((response) => {
                this.$message.success(response.data);
                this.fetchArtworksList();
            }).catch((error) => {
                console.error('删除失败:', error);
                this.$message.error('删除失败');
            });
        },
        handleSelectionChange(val) {
            this.selectedArtworks = val;
        },
        async fetchCategoriesList() {
            try {
                const response = await fetchCategories();
                this.categories = response.data;
            } catch (error) {
                console.error('获取分类数据失败:', error);
            }
        },
        async fetchArtworksList() {
            try {
                const response = await fetchArtworksByUserId(this.userId);
                this.artworks = response;
                this.isLoading =false
            } catch (error) {
                console.error('获取艺术品数据失败:', error);
            }
        },
        handleViewDetails(artworks){
            this.selectedArtworkInFo = artworks;
            this.infodialogVisible = true;
        },
    },
};
</script>
<style scoped src="@/assets/css/HomePage.css"></style>
<style scoped src="@/assets/css/ProductItemAll.css"></style>
  