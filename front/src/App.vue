<template>
  <div id="app">
	<div>
        <OverHeader/>
    </div>
    <router-view></router-view>
	<div>
        <OverFooter/>
    </div>
  </div>
</template>
<script>
  import OverHeader from '@/components/OverHeader.vue';
  import OverFooter from '@/components/OverFooter.vue';
  
  export default {
    components: {
        OverHeader,
        OverFooter,
    },
  };
  </script>
<style>
	#app{
		width: 100vw;
		height: 100vh;
		background-color: #ffffff;
	}
  body::-webkit-scrollbar {
  display: none;
}
</style>
