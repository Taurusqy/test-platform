package com.testplatform.service;

import com.testplatform.entity.Project;
import com.testplatform.mapper.ProjectMapper;
import com.testplatform.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 测试报告 Service
 * 组装用例通过率、Bug分布等统计数据
 */
@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 生成项目测试报告数据
     * 包含：项目基本信息、用例统计、Bug严重程度分布、Bug状态分布
     */
    public Map<String, Object> generateReport(Integer projectId) {
        Map<String, Object> report = new LinkedHashMap<>();

        // 1. 项目基本信息
        Project project = projectMapper.findById(projectId);
        report.put("projectId", projectId);
        report.put("projectName", project != null ? project.getName() : "未知项目");

        // 2. 用例统计
        Map<String, Object> caseStats = buildCaseStats(projectId);
        report.put("caseStats", caseStats);

        // 3. Bug严重程度分布
        Map<String, Object> bugSeverityStats = buildBugSeverityStats(projectId);
        report.put("bugSeverityStats", bugSeverityStats);

        // 4. Bug状态分布
        List<Map<String, Object>> bugStatusList = reportMapper.countBugStatusByProjectId(projectId);
        report.put("bugStatusStats", bugStatusList);

        return report;
    }

    // 组装用例统计：总数、通过、失败、阻塞、未执行、通过率
    private Map<String, Object> buildCaseStats(Integer projectId) {
        List<Map<String, Object>> rows = reportMapper.countCaseStatusByProjectId(projectId);
        Map<String, Object> stats = new LinkedHashMap<>();
        int total = 0, passed = 0, failed = 0, blocked = 0, pending = 0;

        for (Map<String, Object> row : rows) {
            String status = (String) row.get("status");
            int count = ((Number) row.get("count")).intValue();
            total += count;
            switch (status) {
                case "通过" -> passed = count;
                case "失败" -> failed = count;
                case "阻塞" -> blocked = count;
                default -> pending += count;  // 未执行或其他
            }
        }
        stats.put("total", total);
        stats.put("passed", passed);
        stats.put("failed", failed);
        stats.put("blocked", blocked);
        stats.put("pending", pending);
        // 通过率 = (通过+失败+阻塞) > 0 ? 通过 / 已执行总数 : 0
        int executed = passed + failed + blocked;
        double passRate = (executed > 0) ? (passed * 100.0 / executed) : 0.0;
        stats.put("passRate", String.format("%.1f%%", passRate));
        stats.put("passRateValue", Math.round(passRate * 10) / 10.0);
        return stats;
    }

    // 组装Bug严重程度分布：致命、严重、一般、轻微、建议
    private Map<String, Object> buildBugSeverityStats(Integer projectId) {
        List<Map<String, Object>> rows = reportMapper.countBugSeverityByProjectId(projectId);
        // 用 LinkedHashMap 保证顺序
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("致命", 0);
        stats.put("严重", 0);
        stats.put("一般", 0);
        stats.put("轻微", 0);
        stats.put("建议", 0);
        int total = 0;
        for (Map<String, Object> row : rows) {
            String severity = (String) row.get("severity");
            int count = ((Number) row.get("count")).intValue();
            stats.put(severity, count);
            total += count;
        }
        stats.put("total", total);
        return stats;
    }
}
