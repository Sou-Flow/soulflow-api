package com.souflow.models.services;

import com.souflow.models.responses.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardData(String filter);
}
