<template>
    <div class="body">
      <header-2 class="header-2">
        <div class="banner">
          <div class="banner-right">
          <h1>与历史对话，收藏永恒</h1>
          <p>在古老的文化遗产中，我们赋予每一件艺术品全新的生命</p>
        </div>
        <div class="banner-left">
          <img src="@/assets/images/art3.jpg" alt="Auction Banner" class="banner-image">
        </div>
      </div>
    </header-2>
        <div class="sideshow">
          <AllArtworks :initialQuery="searchQueryFromRoute" />
        </div>
    </div>
  </template>
  
  <script>

import AllArtworks from '@/components/AllArtworks.vue';
  
  export default {
    components: {
        AllArtworks 
    },
    data() {
    return {
      searchKeyword: ''
    }
  },
  computed: {
    searchQueryFromRoute() {
      return this.$route.query.q || '';
    }
  }
  };
  </script>

  <style scoped src="@/assets/css/ArtworkView.css"></style>

  