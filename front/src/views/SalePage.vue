<template>
  <div class="body">
    <header-1 class="header-1">
      <div class="banner">
      <div class="banner-right">
        <h1>与经典邂逅，传承不朽</h1>
        <p>在绵延的历史长河中，我们珍视每一件艺术品不朽的光芒</p>
      </div>
      <div class="banner-left">
        <img src="@/assets/images/art4.jpg" alt="Auction Banner" class="banner-image">
      </div>
    </div>
  </header-1>
      <div class="sideshow">
        <SaleShow/>
      </div>
  </div>
</template>

<script>

import SaleShow from '@/components/SaleShow.vue';

export default {
  components: {
     SaleShow 
  },
};
</script>
<style scoped src="@/assets/css/ArtworkView.css"></style>
