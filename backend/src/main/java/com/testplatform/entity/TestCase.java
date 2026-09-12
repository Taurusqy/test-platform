package com.testplatform.entity;

import lombok.Data;
import java.util.List;

/**
 * 测试用例实体类
 * 对应数据库表：test_case
 * 多对一关系：多个用例 归属 一个需求（requirementId）
 * 一对多关系：一个用例执行失败后 可能产生 多个Bug（bugs）
 */
@Data
public class TestCase {

    private Integer id;              // 用例ID
    private String caseNo;           // 用例编号，如 TC-001
    private Integer requirementId;   // 所属需求ID（外键）
    private String module;           // 所属模块
    private String title;            // 用例标题
    private String testPurpose;      // 测试目的
    private String precondition;     // 前置条件
    private String steps;            // 测试步骤
    private String expectedResult;   // 预期结果
    private String priority;         // 优先级：P0/P1/P2/P3
    private String caseType;         // 用例类型：功能测试/性能测试/接口测试等
    private String status;           // 执行状态：未执行/通过/失败/阻塞
    private String actualResult;     // 实际结果
    private String attachment;       // 附件（截图等，存相对路径）
    private String remark;           // 备注
    private String executor;         // 执行人
    private String executeTime;      // 执行时间
    private String createTime;
    private String updateTime;

    // 一对多关联：一个用例可能关联多个Bug
    private List<Bug> bugs;

    // 非数据库字段，查询时关联带出的关联Bug编号（逗号分隔，仅展示用）
    private String relatedBugNos;
}
