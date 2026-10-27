<template>
    <div>
        <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="4">
                <el-button  @click="open">添加轮播</el-button>
            </el-col>
            <el-col :span="8">
                <el-input
                    v-model="searchQuery"
                    placeholder="输入艺术品名称搜索"
                    clearable
                />
            </el-col>
            <el-col :span="4">  
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
                <el-popconfirm 
                    title="确定删除轮播?"
                    @confirm="() => handleDelete(artwork)"
                >
                    <template #reference>
                    <el-button>移除</el-button>
                    </template>
                </el-popconfirm>
                </div>
            </div>
            </transition-group>
            <div v-else-if="!isLoading" class="nodata">
            <p>没有数据~</p>
            </div>
        </div>
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
import { fetchArtworkImages,checkArtworkExists} from '../utils/artworkService';
import { addCarousel,getAllCarousels,deleteCarouselByArtworkId} from '../utils/carouselService';
import { ElMessage, ElMessageBox } from 'element-plus';
export default {
    data() {
        return { 
            isLoading:true,
            imagePreviewUrls:[],
            artworks: [],
            images: [],
            artworkImages: [],
            searchQuery: '',
            infodialogVisible: false, 
            selectedArtworkInFo: {},
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
    },
    mounted() {  
        this.fetchArtworksList();
    },
    methods: {
        fetchImages(artworkId) {
            fetchArtworkImages(artworkId)
                .then((response) => {
                    this.artworkImages = response.data; 
                })
                .catch((error) => {
                console.error('Error fetching images:', error);
                });
        },
        resetEditForm() {
            this.artworkImages=[];
            this.imagePreviewUrls =  [];
            this.images = [];
            this.htmlContent = '';const editor = this.$el.querySelector('.editor');
            if (editor) {
                editor.innerHTML = ''; 
            }
        },
        async handleAddArtwork() {
            this.resetEditForm();
            this.editDialogVisible = true;
        },
        async handleDelete(artwork) {
            const response= await deleteCarouselByArtworkId(artwork.artworkId);
            if(response){
                ElMessage.success(`删除成功`);
                this.fetchArtworksList();  
            }   
        },
        async fetchArtworksList() {
            try {
                const response = await getAllCarousels();
                this.artworks = response.map(carousel => carousel.artwork);
                this.isLoading =false
            } catch (error) {
                console.error('获取艺术品数据失败:', error);
            }
        },
        handleViewDetails(artworks){
            this.selectedArtworkInFo = artworks;
            this.infodialogVisible = true;
        },
        async open() {
            try {
                const { value } = await ElMessageBox.prompt('请输入商品编号', 'Tip', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                inputPattern: /^\d+$/,
                inputErrorMessage: '请输入正确编号',
                });
                const exists = await checkArtworkExists(value);
                if (exists.data) {
                    const response =await addCarousel(value);
                    if(response){
                        ElMessage.success(`添加成功`);
                        this.fetchArtworksList();
                    }else{
                        ElMessage.success(`商品已轮播`);
                    }
                } else {
                ElMessage.warning('商品不存在');
                }
            } catch (error) {
                if (error !== 'cancel') {
                ElMessage.error(error.message || 'Failed to check artwork existence');
                } else {
                ElMessage.info('取消输入');
                }
            }
            },
    },
};
</script>
<style scoped src="@/assets/css/HomePage.css"></style>
<style scoped src="@/assets/css/ProductItemAll.css"></style>
  