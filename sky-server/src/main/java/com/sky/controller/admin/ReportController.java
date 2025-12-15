package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequestMapping("/admin/report")
@RestController
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * begin - end 之间的营业额
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("从 {} 到 {} 的营业额", begin, end);
        TurnoverReportVO turnoverReportVO = reportService.turnoverReport(begin, end);
        return Result.success(turnoverReportVO);
    }

    /**
     * begin - end 之间的用户统计详情
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userReport(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("从 {} 到 {} 用户统计详情", begin, end);
        UserReportVO reportVO = reportService.userReport(begin, end);
        return Result.success(reportVO);
    }

    /**
     * begin - end 之间的订单统计详情
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("从 {} 到 {} 订单统计详情", begin, end);
        OrderReportVO orderReportVO = reportService.orderReport(begin, end);
        return Result.success(orderReportVO);
    }

    /**
     * begin - end 之间的 前top10 销量
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> salesTop10Report(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("从 {} 到 {} top10的菜品", begin, end);
        SalesTop10ReportVO salesTop10Report = reportService.salesTop10Report(begin, end);
        return Result.success(salesTop10Report);
    }
}
