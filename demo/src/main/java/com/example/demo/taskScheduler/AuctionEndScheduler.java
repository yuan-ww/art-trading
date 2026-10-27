package com.example.demo.taskScheduler;

import com.example.demo.service.AuctionService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AuctionEndScheduler {

    private final ScheduledExecutorService scheduler;
    private final AuctionService auctionService;

    public AuctionEndScheduler(AuctionService auctionService) {
        this.auctionService = auctionService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    // 启动定时任务
    @PostConstruct
    public void start() {
        // 每隔一分钟检查一次拍卖任务
        scheduler.scheduleAtFixedRate(this::checkAuctionTasks, 0, 1, TimeUnit.MINUTES);
    }
    // 定时任务逻辑：检查拍卖状态并更新
    private void checkAuctionTasks() {
        System.out.println("检查拍卖任务...");
        // 执行拍卖状态更新和订单生成
        auctionService.updateAuctionStatusToEnded();  // 更新拍卖状态并生成订单
    }
}
