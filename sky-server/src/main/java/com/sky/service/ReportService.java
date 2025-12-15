package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end);

    UserReportVO userReport(LocalDate begin, LocalDate end);

    OrderReportVO orderReport(LocalDate begin, LocalDate end);

    SalesTop10ReportVO salesTop10Report(LocalDate begin, LocalDate end);
}
