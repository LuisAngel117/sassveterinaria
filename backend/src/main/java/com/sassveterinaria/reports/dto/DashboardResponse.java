package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    long todayAppointmentsCount,
    long todayInProgressVisitsCount,
    BigDecimal todaySalesTotal,
    long lowStockCount,
    List<TopServiceItem> topServicesThisWeek
) {
}
