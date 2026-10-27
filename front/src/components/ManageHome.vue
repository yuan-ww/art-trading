<template>
    <div class="dashboard">
      <div class="top-half">
        <div class="chart-panel">
          <h3>最近成交量趋势</h3>
          <div class="canvas-wrapper">
            <canvas id="lineChart"></canvas>
          </div>
        </div>
        <div class="log-panel">
          <h3>待办</h3>
          <ul class="log-list">
            <h4>🕒 待处理直售审核：1</h4>
            <h4>🕒 待处理拍卖审核：0</h4>
          </ul>
        </div>
      </div>
      <div class="bottom-half">
        <div class="chart-box">
          <h3>艺术类别分布</h3>
          <div class="canvas-wrapper">
            <canvas id="categoryPieChart"></canvas>
          </div>
        </div>
        <div class="chart-box">
          <h3>销售类型分布</h3>
          <div class="canvas-wrapper">
            <canvas id="typePieChart"></canvas>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import { Chart } from "chart.js/auto";
  import { getAllOrders } from "../utils/orderServices";
  import { fetchArtworks } from "../utils/artworkService";
  
  export default {
    name: "DashboardCharts",
    data() {
      return {
        lineChart: null,
        categoryPieChart: null,
        typePieChart: null
      };
    },
    methods: {
      createChart(id, type, labels, data, colors) {
        const el = document.getElementById(id);
        if (!el) {
          console.error(`图表元素 ${id} 未找到`);
          return;
        }
  
        if (this[`${id}Chart`]) {
          this[`${id}Chart`].destroy();
        }
        const config = {
          type,
          data: {
            labels,
            datasets: [
              {
                data,
                backgroundColor: colors,
                borderColor: type === "line" ? "rgba(75, 192, 192, 1)" : undefined,
                tension: 0.3,
                fill: type === "line"
              }
            ]
          },
          options: this.getChartOptions(type) 
        };
        const chartInstance = new Chart(el, config);
        this[`${id}Chart`] = chartInstance;
      },
      getChartOptions(type) {
        const commonOptions = {
          maintainAspectRatio: false,
          animation: {
            duration: 2500,
            easing: type === "line" ? "easeOutQuart" : "easeOutElastic"
          }
        };
        const chartOptions = {
            line: {
            ...commonOptions,
            plugins: { legend: { display: false } },
            },
            pie: {
            ...commonOptions,
            elements: {
                arc: { borderWidth: 3,  borderColor: 'transparent'  }
            },
            plugins: { legend: { position: "left" } }
            }
        };

        return chartOptions[type] || commonOptions;
            },
      async loadDataAndCreateCharts() {
        try {
          const orders = await getAllOrders();
          const artworks = await fetchArtworks();
          const monthlyData = Array(12).fill(0);
          orders.forEach(order => {
            const month = new Date(order.createdAt).getMonth();
            monthlyData[month]++;
          });
  
          const lineLabels = monthlyData.map((_, i) => `${i + 1}月`);
          const categoriesCount = {};
          artworks.forEach(art => {
            const name = art.category?.name || "未知";
            categoriesCount[name] = (categoriesCount[name] || 0) + 1;
          });
  
          const fixedColors = ["#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"];
          const categoryLabels = [];
          const categoryData = [];
          const categoryColors = [];
  
          const sortedCategories = Object.entries(categoriesCount)
            .map(([name, count]) => ({ name, count }))
            .sort((a, b) => b.count - a.count);
  
          sortedCategories.slice(0, 4).forEach((item, idx) => {
            categoryLabels.push(item.name);
            categoryData.push(item.count);
            categoryColors.push(fixedColors[idx] || "#E0E0E0");
          });
  
          const remaining = sortedCategories.slice(4).reduce((sum, c) => sum + c.count, 0);
          if (remaining > 0) {
            categoryLabels.push("其他");
            categoryData.push(remaining);
            categoryColors.push("#E0E0E0");
          }
  
          // 饼图数据 - 成交类型
          const saleTypeCount = { 拍卖: 0, 直售: 0 };
          artworks.forEach(a => {
            if (a.saleType === "拍卖") saleTypeCount["拍卖"]++;
            else if (a.saleType === "直售") saleTypeCount["直售"]++;
          });
  
          // 创建所有图表
          this.createChart("lineChart", "line", lineLabels, monthlyData, ["rgba(75, 192, 192, 0.3)"]);
          this.createChart("categoryPieChart", "pie", categoryLabels, categoryData, categoryColors);
          this.createChart("typePieChart", "pie", Object.keys(saleTypeCount), Object.values(saleTypeCount), ["#FF9F40", "#36A2EB"]);
  
        } catch (error) {
          console.error("数据加载失败：", error);
        }
      }
    },
    async mounted() {
      await this.loadDataAndCreateCharts();
    },
    beforeUnmount() {
      [this.lineChart, this.categoryPieChart, this.typePieChart].forEach(c => c?.destroy()); 
    }
  };
  </script>
  
  <style scoped src="@/assets/css/ManageHome.css"></style>
  