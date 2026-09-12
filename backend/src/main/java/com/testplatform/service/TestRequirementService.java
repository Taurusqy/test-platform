package com.testplatform.service;

import com.testplatform.entity.TestRequirement;
import com.testplatform.mapper.TestRequirementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 测试需求 Service 层
 */
@Service
public class TestRequirementService {

    @Autowired
    private TestRequirementMapper requirementMapper;

    // 根据项目ID查询需求列表
    public List<TestRequirement> findByProjectId(Integer projectId) {
        return requirementMapper.findByProjectId(projectId);
    }

    // 根据ID查询单个需求
    public TestRequirement findById(Integer id) {
        return requirementMapper.findById(id);
    }

    // 新增需求（自动生成需求编号 REQ-001）
    public TestRequirement add(TestRequirement requirement) {
        if (requirement.getPriority() == null || requirement.getPriority().isEmpty()) {
            requirement.setPriority("中");
        }
        if (requirement.getStatus() == null || requirement.getStatus().isEmpty()) {
            requirement.setStatus("待分析");
        }
        // 自动生成需求编号
        List<TestRequirement> existing = requirementMapper.findByProjectId(requirement.getProjectId());
        int nextNum = existing.size() + 1;
        requirement.setReqNo(String.format("REQ-%03d", nextNum));
        requirementMapper.insert(requirement);
        return requirement;
    }

    // 删除需求
    public boolean delete(Integer id) {
        return requirementMapper.deleteById(id) > 0;
    }

    // 更新需求
    public boolean update(TestRequirement requirement) {
        return requirementMapper.update(requirement) > 0;
    }
}
