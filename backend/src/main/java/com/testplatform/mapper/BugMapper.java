package com.testplatform.mapper;

import com.testplatform.entity.Bug;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Bug Mapper 接口
 * 完整的 Bug 增删改查
 */
@Mapper
public interface BugMapper {

    // 新增Bug
    @Insert("INSERT INTO bug (bug_no, case_id, project_id, module, title, description, reproduce_steps, expected_result, actual_result, environment, affected_version, bug_type, attachment, severity, bug_priority, status, reporter, assignee) " +
            "VALUES (#{bugNo}, #{caseId}, #{projectId}, #{module}, #{title}, #{description}, #{reproduceSteps}, #{expectedResult}, #{actualResult}, #{environment}, #{affectedVersion}, #{bugType}, #{attachment}, #{severity}, #{bugPriority}, #{status}, #{reporter}, #{assignee})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Bug bug);

    // 根据项目ID查询Bug列表
    List<Bug> findByProjectId(@Param("projectId") Integer projectId);

    // 根据ID查询单个Bug
    Bug findById(@Param("id") Integer id);

    // 更新Bug状态
    @Update("UPDATE bug SET status=#{status}, update_time=datetime('now','localtime') WHERE id=#{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    // 更新Bug基本信息
    @Update("UPDATE bug SET title=#{title}, module=#{module}, description=#{description}, reproduce_steps=#{reproduceSteps}, " +
            "expected_result=#{expectedResult}, actual_result=#{actualResult}, environment=#{environment}, affected_version=#{affectedVersion}, bug_type=#{bugType}, attachment=#{attachment}, " +
            "severity=#{severity}, bug_priority=#{bugPriority}, assignee=#{assignee}, case_id=#{caseId}, " +
            "update_time=datetime('now','localtime') WHERE id=#{id}")
    int update(Bug bug);

    // 删除Bug
    @Delete("DELETE FROM bug WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
