<template>
    <div class="header">
      <el-button @click="toggleFilter" class="filterBt">
      {{ showFilter ? '隐藏筛选' : '显示筛选' }}
      <img src="@/assets/images/icons/filter.png"  alt="Filter Icon" class="filter-icon" />
    </el-button>
    <div class="selected-tags">
      <span v-for="(tag, index) in selectedTags" :key="index" class="tag">
        {{ tag }} 
        <span class="delete-tag" @click="removeTag(index)">x</span>
      </span>
    </div>
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索作品/作者/卖家"
          @input="onSearch"
        />
      </div>
      <div class="statistics">共 {{ filteredProducts.length }} 条记录</div>
    </div>
    <div class="main-container">
       <div v-if="showFilter" class="filter">
        <el-collapse v-model="activeName" accordion>
          <el-collapse-item title="艺术品分类" name="1" class="collapse-item">
            <div class="selector-container">
                <el-checkbox-group v-model="selectedCategories" class="checkbox-column">
                    <el-checkbox
                        v-for="option in categories"
                        :key="option.categoryId"
                        :label="option.name">
                        {{ option.name }}
                    </el-checkbox>
                 </el-checkbox-group>
           </div>
      </el-collapse-item>
      <el-collapse-item title="艺术家" name="2" class="collapse-item">
        <div>
        <div class="selector-container">
            <el-checkbox-group v-model="selectedAuthors" class="checkbox-column">
                <el-checkbox label="毕加索" >毕加索</el-checkbox>
                <el-checkbox label="梵高">文森特·梵高</el-checkbox>
                <el-checkbox label="莫奈" >克劳德·莫奈</el-checkbox>
                <el-checkbox label="达芬奇">莱奥娜多·达·芬奇</el-checkbox>
            </el-checkbox-group>
           </div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="发布者" name="3" class="collapse-item">
        <div>
          <el-input
          v-model="searchQuery"
          placeholder="搜索发布者"
          @input="onSearch"
        />
        </div>
        <div>
  
        </div>
        <div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="状态" name="4" class="collapse-item">
        <div>
          <el-checkbox-group v-model="serlectStatus" class="checkbox-column">
                <el-checkbox label="在售" >在售</el-checkbox>
                <el-checkbox label="已售">已售</el-checkbox>
            </el-checkbox-group>
        </div>
        <div>
  
        </div>
      </el-collapse-item>
    </el-collapse>
      </div>
    <div class="container">
      <div :class="['product-list', { 'filter-enabled': showFilter }]" v-if="filteredProducts.length > 0">
        <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: artwork.artworkId } }" @click="setArtworkInVuex(artwork)" v-for="artwork in filteredProducts" :key="artwork.artworkId" class="product-item">
          <img :src="artwork.images[0].imageUrl" alt="商品图片" class="product-image" />
          <div class="product-details">
            <div class="name-author">
            <p class="product-name">《{{ artwork.name }}》</p>
            <p class="product-author">{{ artwork.author }}</p>
          </div>
          <p class="description">{{ artwork.description }}</p>
          <p class="starting-price">定价：￥{{ artwork.fixedPrice }}</p>
          </div>
        </router-link>
      </div>
      <div v-else class="nodata">
      <p>没有找到相关内容~</p>
    </div>
    </div>
  </div>
  </template>
  
  <script>
  import { fetchCategories } from '../utils/artworkService';
  import { getAllArtworksWithImages} from '../utils/startService';
  export default {
  data() {
    return {
      saleArtworks:[],
      searchQuery: '',
      selectedFilter: [], 
      selectedCategories: [],  // 分类筛选条件
      selectedAuthors: [],     // 作者筛选条件
      serlectStatus:[],
      selectedTags: [],
      showFilter: true,
      categories: [], 
    };
  },
  computed: {
    filteredProducts() {
        let filtered = this.saleArtworks;
        filtered = filtered.filter(artwork => artwork.status !== '审核');
        // 根据分类筛选
        if (this.selectedCategories && this.selectedCategories.length > 0) {
        filtered = filtered.filter(artwork =>
          this.selectedCategories.includes(artwork.category.name) 
        );
      }
      // 然后筛选艺术家
      if (this.selectedAuthors && this.selectedAuthors.length > 0) {
        filtered = filtered.filter(artwork =>
          this.selectedAuthors.includes(artwork.author)
        );
      }
      if (this.serlectStatus && this.serlectStatus.length > 0) {
        filtered = filtered.filter(artwork =>
          this.serlectStatus.includes(artwork.status)
        );
      }
        // 根据搜索关键字筛选
        if (this.searchQuery) {
          const query = this.searchQuery.toLowerCase();
          filtered = filtered.filter(artwork =>
            artwork.name.toLowerCase().includes(query) ||
            artwork.author.toLowerCase().includes(query) ||
            artwork.category.name.toLowerCase().includes(query) ||
            artwork.user.username.toLowerCase().includes(query)
          );
        }
        return filtered;
      },
  },
  mounted() {  
        this.fetchCategoriesList();
        this.fetchCategorizedArtworks();
    },
  methods: {
    setArtworkInVuex(artwork) {
      this.$store.dispatch('updateArtwork', artwork);
      localStorage.setItem('artwork', JSON.stringify(artwork));
    },
    async fetchCategorizedArtworks() {
        try {
          const response = await getAllArtworksWithImages();
          const artworks = response.data;  
          this.saleArtworks = artworks.filter(item => item.saleType === '直售');
        } catch (error) {
          console.error("Error fetching categorized artworks:", error);
        }
      },
    removeTag(index) {
      const tagToRemove = this.selectedTags[index];
        this.selectedCategories = this.selectedCategories.filter(category => category !== tagToRemove);
        this.selectedAuthors = this.selectedAuthors.filter(author => author !== tagToRemove);
        this.serlectStatus = this.serlectStatus.filter(status => status !== tagToRemove);
        this.selectedTags.splice(index, 1);
    },
    toggleFilter() {
      this.showFilter = !this.showFilter;
    },
    async fetchCategoriesList() {
            try {
                const response = await fetchCategories();
                this.categories = response.data;
            } catch (error) {
                console.error('获取分类数据失败:', error);
            }
        },
  },
  watch: {
    selectedCategories(newCategories) {
      this.selectedTags = [...newCategories, ...this.selectedAuthors];
    },
    selectedAuthors(newAuthors) {
      this.selectedTags = [...this.selectedCategories, ...newAuthors];
    },
    serlectStatus(newStatus) {
      this.selectedTags = [...this.selectedAuthors, ...newStatus];
    },
  }
  };
  </script>
<style scoped src="@/assets/css/ArtworkShow.css"></style>