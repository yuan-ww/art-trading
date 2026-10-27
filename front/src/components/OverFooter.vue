<template>
    <footer v-if="!route.meta.hideHeaderAndFooter" class="footer">
      <p>&copy; 2024 BooYah. All rights reserved.</p>
    </footer>
</template>


<script>
import { useRoute } from 'vue-router';

export default {
  setup() {
    const route = useRoute();
    return { route }; 
  }
}
</script>
<style>

.footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 1rem 0;
  margin-top: 10rem;
}
</style>
