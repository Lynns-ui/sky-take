package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/report")
@RestController
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 订单统计
     */
//    @GetMapping("/ordersStatistics")
//    public Result<OrderReportVO>

}
