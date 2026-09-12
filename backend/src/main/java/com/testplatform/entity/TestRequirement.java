package com.testplatform.entity;

import lombok.Data;
import java.util.List;

/**
 * 测试需求实体类
 * 对应数据库表：test_requirement
 * 多对一关系：多个需求 归属 一个项目（projectId）
 * 一对多关系：一个需求 包含 多个测试用例（cases）
 */
@Data
public class TestRequirement {

    private Integer id;           // 需求ID
    private String reqNo;         // 需求编号，如 REQ-001
    private Integer projectId;    // 所属项目ID（外键）
    private String title;         // 需求标题
    private String content;       // 需求内容
    private String priority;      // 优先级：高/中/低
    private String status;        // 需求状态
    private String createTime;
    private String updateTime;

    // 一对多关联：一个需求下有多个测试用例
    private List<TestCase> cases;
}
