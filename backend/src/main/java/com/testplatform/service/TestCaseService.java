package com.testplatform.service;

import com.testplatform.dto.ExecuteCaseRequest;
import com.testplatform.entity.Bug;
import com.testplatform.entity.TestCase;
import com.testplatform.entity.TestRequirement;
import com.testplatform.mapper.BugMapper;
import com.testplatform.mapper.TestCaseMapper;
import com.testplatform.mapper.TestRequirementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 测试用例 Service 层
 */
@Service
public class TestCaseService {

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private BugMapper bugMapper;

    @Autowired
    private TestRequirementMapper requirementMapper;

    // 根据需求ID查询用例列表
    public List<TestCase> findByRequirementId(Integer requirementId) {
        return testCaseMapper.findByRequirementId(requirementId);
    }

    // 根据ID查询单个用例
    public TestCase findById(Integer id) {
        return testCaseMapper.findById(id);
    }

    // 新增用例（自动生成用例编号 TC-001）
    public TestCase add(TestCase testCase) {
        if (testCase.getPriority() == null || testCase.getPriority().isEmpty()) {
            testCase.setPriority("P1");
        }
        if (testCase.getCaseType() == null || testCase.getCaseType().isEmpty()) {
            testCase.setCaseType("功能测试");
        }
        if (testCase.getStatus() == null || testCase.getStatus().isEmpty()) {
            testCase.setStatus("未执行");
        }
        // 自动生成用例编号：查询当前需求下用例数量，编号为 TC-001、TC-002...
        List<TestCase> existing = testCaseMapper.findByRequirementId(testCase.getRequirementId());
        int nextNum = existing.size() + 1;
        testCase.setCaseNo(String.format("TC-%03d", nextNum));
        testCaseMapper.insert(testCase);
        return testCase;
    }

    // 编辑用例
    public boolean update(TestCase testCase) {
        return testCaseMapper.update(testCase) > 0;
    }

    // 删除用例
    public boolean delete(Integer id) {
        return testCaseMapper.deleteById(id) > 0;
    }

    /**
     * 执行用例
     * 1. 更新用例的执行状态、实际结果、执行人
     * 2. 如果状态为"失败"且请求了联动创建Bug，则自动创建一条Bug
     */
    public Bug execute(Integer caseId, ExecuteCaseRequest request) {
        TestCase testCase = testCaseMapper.findById(caseId);
        if (testCase == null) {
            throw new RuntimeException("用例不存在");
        }

        testCase.setStatus(request.getStatus());
        testCase.setActualResult(request.getActualResult());
        testCase.setAttachment(request.getAttachment());
        testCase.setExecutor(request.getExecutor());
        testCaseMapper.execute(testCase);

        if ("失败".equals(request.getStatus())
                && Boolean.TRUE.equals(request.getCreateBug())) {
            TestRequirement requirement = requirementMapper.findById(testCase.getRequirementId());
            Integer projectId = (requirement != null) ? requirement.getProjectId() : null;

            Bug bug = new Bug();
            bug.setCaseId(caseId);
            bug.setProjectId(projectId);
            bug.setModule(testCase.getModule());
            bug.setTitle(request.getBugTitle() != null ? request.getBugTitle() : "用例执行失败：" + testCase.getTitle());
            bug.setDescription(request.getActualResult());
            bug.setReproduceSteps(testCase.getSteps());
            bug.setExpectedResult(testCase.getExpectedResult());
            bug.setActualResult(request.getActualResult());
            bug.setAttachment(request.getAttachment());
            bug.setSeverity(request.getBugSeverity() != null ? request.getBugSeverity() : "一般");
            bug.setBugPriority("P2");
            bug.setStatus("新建");
            bug.setReporter(request.getExecutor());
            generateBugNo(bug, projectId);
            bugMapper.insert(bug);
            return bug;
        }
        return null;
    }

    // 生成Bug编号 BUG-001
    private void generateBugNo(Bug bug, Integer projectId) {
        if (projectId != null) {
            List<Bug> existing = bugMapper.findByProjectId(projectId);
            int nextNum = existing.size() + 1;
            bug.setBugNo(String.format("BUG-%03d", nextNum));
        }
    }
}
