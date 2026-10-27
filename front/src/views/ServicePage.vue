<template>
    <div class="service-page">
      <div class="header-section">
        <h1>艺术品交易服务中心</h1>
        <p>为您提供专业、安全的在线交易解决方案</p>
      </div>
      <div class="faq-container">
        <h2 class="section-title">
          <el-icon><QuestionFilled /></el-icon> 常见问题解答
        </h2>
        <el-collapse accordion class="custom-collapse">
          <el-collapse-item name="1">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何保证艺术品的真实性？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 所有艺术品均经过权威机构鉴定认证</p>
              <p>• 提供电子版鉴定证书和详细溯源信息</p>
              <p>• 支持线下实物验货服务</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="2">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                交易流程需要多长时间？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 数字艺术品：即时交易（约5分钟）</p>
              <p>• 实物艺术品：3-7个工作日（含包装物流）</p>
              <p>• 拍卖交易：通常1-3个工作日</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="3">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何确保交易的安全性？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 多重身份验证及安全登录</p>
              <p>• 全程SSL加密保护数据传输</p>
              <p>• 专业第三方托管支付平台</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="4">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                艺术品的运输方式有哪些？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 合作专业物流公司负责运输</p>
              <p>• 定制包装保障艺术品安全</p>
              <p>• 提供运输保险服务</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="5">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何申请退货或售后服务？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 在规定时间内联系平台客服</p>
              <p>• 提供完整交易及物流记录</p>
              <p>• 平台审核后安排退货或赔偿</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="6">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何处理运输过程中艺术品损坏或丢失的问题？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 平台与保险公司合作，提供完善的运输保险</p>
              <p>• 如出现损坏或丢失，请及时联系客服，并提交相关证明材料</p>
              <p>• 根据保险条款及交易协议进行理赔处理</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="7">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何保护我的个人隐私和数据安全？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 平台采用先进的加密技术确保数据传输安全</p>
              <p>• 严格遵守数据保护法规及隐私政策</p>
              <p>• 定期进行系统安全检测和漏洞修复</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="8">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如果遇到支付问题怎么办？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 请确认支付信息无误后重新尝试</p>
              <p>• 联系发卡银行或支付平台确认账户状态</p>
              <p>• 如问题持续，请联系客服寻求帮助</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="9">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何查看订单和物流进度？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 登录“我的订单”查看详细信息</p>
              <p>• 平台提供实时物流跟踪服务</p>
              <p>• 如有疑问可随时咨询客服</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="10">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                如何参与平台在线拍卖？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 拍卖前完成实名认证及支付保证金</p>
              <p>• 拍卖期间可实时出价，竞价透明</p>
              <p>• 拍卖结束后系统将自动处理支付与交割</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="11">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                平台如何应对欺诈行为？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 建立多重安全检测和风控系统</p>
              <p>• 定期审核交易记录，及时发现异常行为</p>
              <p>• 发现欺诈行为后将立即冻结账户并启动处理流程</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="12">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                平台支持哪些支付方式？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 支持银行卡、支付宝、微信支付等多种方式</p>
              <p>• 所有支付过程均全程加密，确保安全</p>
            </div>
          </el-collapse-item>
          <el-collapse-item name="13">
            <template #title>
              <div class="collapse-title">
                <el-icon><HelpFilled /></el-icon>
                艺术品购买后是否提供完善的售后保障？
              </div>
            </template>
            <div class="collapse-content">
              <p>• 提供完善的退换货及赔偿政策</p>
              <p>• 如遇质量问题可申请售后服务</p>
              <p>• 具体售后政策请参考平台服务条款</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div class="guide-container">
      <h2 class="section-title">
        <el-icon><Reading /></el-icon> 交易指南
      </h2>
      <el-collapse accordion class="custom-collapse">
        <el-collapse-item name="guide1">
          <template #title>
            <div class="collapse-title">
              <el-icon><InfoFilled /></el-icon>
              首次用户注册指南
            </div>
          </template>
          <div class="collapse-content">
            <el-steps :active="3" simple>
              <el-step title="身份认证"  />
              <el-step title="支付绑定"  />
              <el-step title="安全验证"  />
            </el-steps>
          </div>
        </el-collapse-item>
        <el-collapse-item name="guide2">
          <template #title>
            <div class="collapse-title">
              <el-icon><InfoFilled /></el-icon>
              艺术品购买流程
            </div>
          </template>
          <div class="collapse-content">
            <el-timeline>
              <el-timeline-item  timestamp="浏览艺术品" placement="top">
                通过分类或搜索功能查找心仪的艺术品。
              </el-timeline-item>
              <el-timeline-item timestamp="查看详情" placement="top">
                了解作品背景、尺寸、材质及价格等信息。
              </el-timeline-item>
              <el-timeline-item timestamp="下单支付" placement="top">
                选择支付方式，完成购买订单。
              </el-timeline-item>
              <el-timeline-item timestamp="物流配送" placement="top">
                平台安排专业物流运输，并提供追踪信息。
              </el-timeline-item>
              <el-timeline-item timestamp="收货验收" placement="top">
                收到艺术品后，仔细检查并确认无误。
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-collapse-item>
        <el-collapse-item name="guide3">
          <template #title>
            <div class="collapse-title">
              <el-icon><InfoFilled /></el-icon>
              平台费用说明
            </div>
          </template>
          <div class="collapse-content">
            <p>• **直接购买**：订单不收取交易服务费。</p>
            <p>• **在线拍卖**：卖家支付 <strong>3%</strong> 的拍卖押金,活动结束后退回</p>
          </div>
        </el-collapse-item>
        <el-collapse-item name="guide4">
          <template #title>
            <div class="collapse-title">
              <el-icon><InfoFilled /></el-icon>
              买家与卖家交易须知
            </div>
          </template>
          <div class="collapse-content">
            <p><strong>🔹 买家须知：</strong></p>
            <ul>
              <li>请确认艺术品信息，确保符合您的需求。</li>
              <li>下单后，平台托管资金，确保交易安全。</li>
              <li>收货后请尽快确认订单，保障双方权益。</li>
            </ul>
            <p><strong>🔹 卖家须知：</strong></p>
            <ul>
              <li>上传艺术品时需提供**高清图片、详细描述及价格**。</li>
              <li>确认订单后，应在规定时间内安排发货。</li>
              <li>买家确认收货后，平台将资金结算给卖家。</li>
            </ul>
          </div>
        </el-collapse-item>

        <el-collapse-item name="guide5">
          <template #title>
            <div class="collapse-title">
              <el-icon><InfoFilled /></el-icon>
              艺术品保养与收藏建议
            </div>
          </template>
          <div class="collapse-content">
            <p>• **书画作品**：应存放在通风、干燥处，避免阳光直射。</p>
            <p>• **油画**：建议每年进行一次表面清洁，并避免温度变化过大。</p>
            <p>• **雕塑**：定期擦拭，金属材质需涂防锈油，木质雕塑避免潮湿。</p>
            <p>• **艺术品增值**：收藏艺术家作品时，关注市场行情和展览动态。</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
      <div class="contact-section">
        <h3>需要更多帮助？</h3>
        <el-button type="primary" >联系客服</el-button>
        <el-button type="success" >在线咨询</el-button>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ServicePage",
    data() {
      return {
      };
    },
    methods: {
    }
  };
  </script>

  <style scoped src="@/assets/css/ServicePage.css"></style>
  