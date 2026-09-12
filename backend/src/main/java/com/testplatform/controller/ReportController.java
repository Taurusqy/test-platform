package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 测试报告 Controller
 * 接口路径统一前缀：/api/reports
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 生成项目测试报告数据
     * GET /api/reports/{projectId}
     * 返回用例通过率、Bug严重程度分布等统计数据，供前端渲染HTML报告
     */
    @GetMapping("/{projectId}")
    public Result<Map<String, Object>> getReport(@PathVariable Integer projectId) {
        Map<String, Object> report = reportService.generateReport(projectId);
        return Result.success(report);
    }
}
