const { defineConfig } = require('@vue/cli-service');

const apiTarget = process.env.VUE_APP_API_BASE_URL || 'http://localhost:10202';

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
      },
    },
  },
});
