package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
@Slf4j
public class MyTask {

    /**
     * 定时任务 每隔五秒执行
     * 秒 分 时 日 月 周
     */
    //@Scheduled(cron = "*/5 * * * * *")
    public void executeTask() {
        log.info("定时任务执行：{}", new Date());
    }

}