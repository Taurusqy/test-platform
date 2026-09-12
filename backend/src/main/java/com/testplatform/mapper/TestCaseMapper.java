package com.testplatform.mapper;

import com.testplatform.entity.TestCase;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 测试用例 Mapper 接口
 * 用例归属某个需求，通过 requirement_id 关联
 */
@Mapper
public interface TestCaseMapper {

    // 根据需求ID查询用例列表
    List<TestCase> findByRequirementId(@Param("requirementId") Integer requirementId);

    // 根据ID查询单个用例
    TestCase findById(@Param("id") Integer id);

    // 新增用例
    @Insert("INSERT INTO test_case (case_no, requirement_id, module, title, test_purpose, precondition, steps, expected_result, priority, case_type, attachment, remark) " +
            "VALUES (#{caseNo}, #{requirementId}, #{module}, #{title}, #{testPurpose}, #{precondition}, #{steps}, #{expectedResult}, #{priority}, #{caseType}, #{attachment}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TestCase testCase);

    // 编辑用例
    @Update("UPDATE test_case SET title=#{title}, module=#{module}, test_purpose=#{testPurpose}, precondition=#{precondition}, " +
            "steps=#{steps}, expected_result=#{expectedResult}, priority=#{priority}, case_type=#{caseType}, attachment=#{attachment}, remark=#{remark}, " +
            "update_time=datetime('now','localtime') WHERE id=#{id}")
    int update(TestCase testCase);

    // 执行用例（更新状态、实际结果、附件、执行人、执行时间）
    @Update("UPDATE test_case SET status=#{status}, actual_result=#{actualResult}, attachment=#{attachment}, " +
            "executor=#{executor}, execute_time=datetime('now','localtime'), " +
            "update_time=datetime('now','localtime') WHERE id=#{id}")
    int execute(TestCase testCase);

    // 删除用例
    @Delete("DELETE FROM test_case WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
