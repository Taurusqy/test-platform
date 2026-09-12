package com.testplatform.entity;

import lombok.Data;

/**
 * Bug缺陷实体类
 * 对应数据库表：bug
 * 多对一关系：多个Bug 可关联 同一个测试用例（caseId）
 * 多对一关系：多个Bug 归属 同一个项目（projectId）
 *
 * Bug状态流转：新建 → 待修复 → 已修复 → 已验证 → 关闭
 */
@Data
public class Bug {

    private Integer id;              // BugID
    private String bugNo;            // Bug编号，如 BUG-001
    private Integer caseId;          // 关联的测试用例ID（可为空）
    private Integer projectId;       // 所属项目ID
    private String module;           // 所属模块
    private String title;            // Bug标题
    private String description;      // Bug描述
    private String reproduceSteps;   // 重现步骤
    private String expectedResult;   // 预期结果
    private String actualResult;     // 实际结果
    private String environment;      // 测试环境
    private String affectedVersion;  // 影响版本
    private String bugType;          // Bug类型
    private String attachment;       // 附件截图
    private String severity;         // 严重程度：致命/严重/一般/轻微/建议
    private String bugPriority;      // Bug优先级：P0/P1/P2/P3
    private String status;           // Bug状态：新建/待修复/已修复/已验证/关闭
    private String reporter;         // 提交人
    private String assignee;         // 修复人
    private String createTime;
    private String updateTime;

    // 非数据库字段，查询时关联带出的关联用例标题（仅展示用）
    private String caseTitle;
    // 非数据库字段，查询时关联带出的关联用例编号（仅展示用）
    private String caseNo;
}
