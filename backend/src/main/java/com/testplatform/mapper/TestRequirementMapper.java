package com.testplatform.mapper;

import com.testplatform.entity.TestRequirement;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 测试需求 Mapper 接口
 * 需求归属某个项目，通过 project_id 关联
 */
@Mapper
public interface TestRequirementMapper {

    // 根据项目ID查询该项目下的所有需求
    List<TestRequirement> findByProjectId(@Param("projectId") Integer projectId);

    // 根据ID查询单个需求
    TestRequirement findById(@Param("id") Integer id);

    // 新增需求
    @Insert("INSERT INTO test_requirement (req_no, project_id, title, content, priority, status) " +
            "VALUES (#{reqNo}, #{projectId}, #{title}, #{content}, #{priority}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TestRequirement requirement);

    // 删除需求
    @Delete("DELETE FROM test_requirement WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 更新需求
    @Update("UPDATE test_requirement SET title=#{title}, content=#{content}, " +
            "priority=#{priority}, status=#{status}, " +
            "update_time=datetime('now','localtime') WHERE id=#{id}")
    int update(TestRequirement requirement);
}
