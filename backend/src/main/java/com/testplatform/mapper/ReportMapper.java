package com.testplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;
import java.util.List;

/**
 * 报告统计 Mapper
 * 专门做统计查询，不对应单张表
 */
@Mapper
public interface ReportMapper {

    // 按项目统计用例状态分布（关联 project→requirement→test_case）
    // 返回每行：status(状态名), count(数量)
    List<Map<String, Object>> countCaseStatusByProjectId(@Param("projectId") Integer projectId);

    // 按项目统计Bug严重程度分布
    // 返回每行：severity(严重程度), count(数量)
    List<Map<String, Object>> countBugSeverityByProjectId(@Param("projectId") Integer projectId);

    // 按项目统计Bug状态分布
    List<Map<String, Object>> countBugStatusByProjectId(@Param("projectId") Integer projectId);
}
