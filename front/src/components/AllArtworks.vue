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
      <div class="statistics">共 {{ total }} 条记录</div>
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
      </el-collapse-item>
      <el-collapse-item title="售卖形式" name="5" class="collapse-item">
        <div>
          <el-checkbox-group v-model="selectedType" class="checkbox-column">
                <el-checkbox label="拍卖" >拍卖</el-checkbox>
                <el-checkbox label="直售">直售</el-checkbox>
            </el-checkbox-group>
        </div>
      </el-collapse-item>
      <el-collapse-item title="状态" name="4" class="collapse-item">
        <div>
          <el-checkbox-group v-model="selectedStatus" class="checkbox-column">
                <el-checkbox label="在售" >在售</el-checkbox>
                <el-checkbox label="已售">已售</el-checkbox>
            </el-checkbox-group>
        </div>
      </el-collapse-item>
    </el-collapse>
      </div>
    <div class="container">
      <div :class="['product-list', { 'filter-enabled': showFilter }]" v-if="filteredProducts.length > 0">
        <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: artwork.artworkId } }" @click="setArtworkInVuex(artwork)" v-for="artwork in filteredProducts" :key="artwork.artworkId" class="product-item">
          <img :src="thumbnail(artwork.images?.[0]?.imageUrl)" @error="useOriginalImage($event, artwork.images?.[0]?.imageUrl)" alt="商品图片" class="product-image" />
          <div class="product-details">
            <div class="name-author">
            <p class="product-name">《{{ artwork.name }}》</p>
            <p class="product-author">{{ artwork.author }}</p>
          </div>
          <p class="description">{{ artwork.description }}</p>
          <p class="starting-price">￥{{ artwork.fixedPrice }}</p>
          </div>
        </router-link>
      </div>
      <div v-else class="nodata">
        <p>没有找到相关内容~</p>
      </div>
      <el-pagination v-if="total > pageSize" layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="page + 1" @current-change="changePage" />
    </div>
  </div>
  </template>
  
  <script>
  import { fetchCategories } from '../utils/artworkService';
  import axios from 'axios';
  import { API_BASE_URL } from '../config/api';
  export default {
  data() {
    return {
      auctionArtworks:[],
      page: 0,
      pageSize: 20,
      total: 0,
      searchTimer: null,
      requestSequence: 0,
      searchQuery: '',
      selectedFilter: [], 
      selectedCategories: [],
      selectedAuthors: [],
      selectedStatus:[],
      selectedType:[],
      selectedTags: [],
      showFilter: true,
      categories: [], 
    };
  },
  computed: {
    filteredProducts() {
        return this.auctionArtworks;
      },
  },
  mounted() { 
      if (this.$route.query.q) {
      this.searchQuery = this.$route.query.q;
    } 
        this.fetchCategoriesList();
        this.fetchCategorizedArtworks();
    },
  methods: {
    onSearch() { clearTimeout(this.searchTimer); this.searchTimer = setTimeout(() => this.resetPage(), 300); },
    resetPage() { this.page = 0; this.fetchCategorizedArtworks(); },
    changePage(page) { this.page = page - 1; this.fetchCategorizedArtworks(); },
    thumbnail(url) {
      if (!url) return '';
      return url.replace(/\/uploads\/([^/]+)\.[a-zA-Z0-9]+$/, '/uploads/thumbnails/$1.png');
    },
    useOriginalImage(event, url) {
      if (url && !event.target.dataset.originalFallback) {
        event.target.dataset.originalFallback = 'true';
        event.target.src = url;
      }
    },
    setArtworkInVuex(artwork) {
        this.$store.dispatch('updateArtwork', artwork);
        localStorage.setItem('artwork', JSON.stringify(artwork));
      },
    async fetchCategorizedArtworks() {
        const sequence = ++this.requestSequence;
        try {
          const params = new URLSearchParams({ page: this.page, size: this.pageSize });
          if (this.searchQuery.trim()) params.append('keyword', this.searchQuery.trim());
          this.selectedCategories.forEach(value => params.append('categories', value));
          this.selectedAuthors.forEach(value => params.append('authors', value));
          this.selectedStatus.forEach(value => params.append('statuses', value));
          this.selectedType.forEach(value => params.append('types', value));
          const { data } = await axios.get(`${API_BASE_URL}/api/artworks/public-page`, { params });
          if (sequence !== this.requestSequence) return;
          this.auctionArtworks = data.content.map(item => ({ ...item, category: { name: item.categoryName || '' }, user: { username: item.sellerName || '' }, images: item.imageUrl ? [{ imageUrl: item.imageUrl }] : [] }));
          this.total = data.totalElements;
        } catch (error) {
          console.error("Error fetching categorized artworks:", error);
        }
      },
    removeTag(index) {
      const tagToRemove = this.selectedTags[index];
        this.selectedCategories = this.selectedCategories.filter(category => category !== tagToRemove);
        this.selectedAuthors = this.selectedAuthors.filter(author => author !== tagToRemove);
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
    '$route.query.q': {
      immediate: true,
      handler(newQuery) {
        if (newQuery !== undefined) {
          this.searchQuery = newQuery;
          this.onSearch();
        }
      }
    },
    selectedCategories(newCategories) {
      this.selectedTags = [...newCategories, ...this.selectedAuthors];
      this.resetPage();
    },
    selectedAuthors(newAuthors) {
      this.selectedTags = [...this.selectedCategories, ...newAuthors];
      this.resetPage();
    },
    selectedStatus(newStatus) {
    this.selectedTags = [...this.selectedAuthors, ...newStatus];
    this.resetPage();
  },
   selectedType(newType) {
    this.selectedTags = [...this.selectedStatus, ...newType];
    this.resetPage();
  },
  }
  };
  </script>
  <style  scoped src="@/assets/css/ArtworkShow.css"></style>
