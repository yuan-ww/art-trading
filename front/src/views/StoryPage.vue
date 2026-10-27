<template>
  <div class="platform-main">
    <!-- 品牌故事 -->
    <section class="brand-story">
      <h2 class="section-title">品牌故事</h2>
      <div class="timeline">
        <div class="timeline-item">
          <div class="timeline-icon">🎨</div>
          <div class="timeline-content">
            <h3>创办理念</h3>
            <p>博雅诞生于2024年，由资深艺术收藏家张三先生与其他知名专家团队共同创立。我们致力于打破传统艺术交易壁垒，通过数字技术让艺术收藏触手可及。</p>
          </div>
        </div>
        <div class="timeline-item">
          <div class="timeline-icon">🖋️</div>
          <div class="timeline-content">
            <h3>名字由来</h3>
            <p>"博雅"取自《诗经》"博我以文，约我以礼"，寓意平台兼具博大的艺术包容性与高雅的文化品格，打造值得信赖的艺术交流空间。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 平台特色 -->
    <section class="platform-features">
      <h2 class="section-title">核心特色</h2>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">🔍</div>
          <h3>三重鉴定体系</h3>
          <ul>
            <li>科学检测：采用X射线荧光光谱仪等设备</li>
            <li>专家团队：10位国家级鉴定师常驻</li>
            <li>区块链存证：作品DNA信息永久上链</li>
          </ul>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🌐</div>
          <h3>全球艺术网络</h3>
          <ul>
            <li>与30+国际拍卖行数据互通</li>
            <li>海外仓储覆盖15个国家</li>
            <li>多语言客服支持</li>
          </ul>
        </div>
      </div>
    </section>

    <!-- 原有内容保持不变，新增以下模块 -->

    <!-- 艺术生态 -->
    <section class="art-ecosystem">
      <h2 class="section-title">艺术生态建设</h2>
      <div class="ecosystem-grid">
        <div class="ecosystem-item">
          <h3>青年艺术家计划</h3>
          <p>每年扶持50+新锐艺术家，提供：
            <br>• 专属线上展厅
            <br>• 创作基金支持
            <br>• 国际交流机会
          </p>
        </div>
        <div class="ecosystem-item">
          <h3>藏家学院</h3>
          <p>开设系列课程：
            <br>• 艺术品投资入门
            <br>• 收藏品养护指南
            <br>• 艺术史专题研讨
          </p>
        </div>
      </div>
    </section>

    <!-- 原有亮点、艺术品、统计数据模块保持 -->

  </div>
</template>

<style scoped>
/* 新增样式 */
.brand-story {
  background: #f8f6f2;
  padding: 4rem 5%;
  margin: 2rem 0;
}

.timeline {
  position: relative;
  max-width: 800px;
  margin: 3rem auto;
}

.timeline-item {
  display: flex;
  gap: 2rem;
  margin-bottom: 3rem;
}

.timeline-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.timeline-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 3px 20px rgba(0,0,0,0.08);
  position: relative;
}

.timeline-content::before {
  content: '';
  position: absolute;
  left: -20px;
  top: 30px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 10px 20px 10px 0;
  border-color: transparent white transparent transparent;
}

.platform-features {
  padding: 4rem 5%;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  margin-top: 2rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  border: 1px solid #eee;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.art-ecosystem {
  background: #2A2A2A;
  color: white;
  padding: 4rem 5%;
}

.ecosystem-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 3rem;
  max-width: 1200px;
  margin: 2rem auto;
}

.ecosystem-item {
  background: rgba(255,255,255,0.1);
  padding: 2rem;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .feature-grid,
  .ecosystem-grid {
    grid-template-columns: 1fr;
  }

  .timeline-item {
    flex-direction: column;
  }

  .timeline-content::before {
    display: none;
  }
}
</style>