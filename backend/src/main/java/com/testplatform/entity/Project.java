package com.testplatform.entity;

import lombok.Data;
import java.util.List;

/**
 * 项目实体类
 * 对应数据库表：project
 * 一对多关系：一个项目 包含 多个测试需求（requirements）
 */
@Data  // Lombok 注解：自动生成 getter、setter、toString、equals、hashCode
public class Project {

    private Integer id;           // 项目ID
    private String name;          // 项目名称
    private String description;   // 项目描述
    private String status;        // 项目状态
    private String startDate;     // 开始时间
    private String endDate;       // 结束时间
    private String createTime;    // 创建时间
    private String updateTime;    // 更新时间

    // 一对多关联：一个项目下有多个测试需求
    // 这个字段不对应数据库列，仅用于查询时组装关联数据
    private List<TestRequirement> requirements;
}
