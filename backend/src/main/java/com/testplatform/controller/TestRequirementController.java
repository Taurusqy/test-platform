package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.entity.TestRequirement;
import com.testplatform.service.TestRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 测试需求管理 Controller
 * 需求归属某个项目，查询时需传 projectId
 * 接口路径统一前缀：/api/requirements
 */
@RestController
@RequestMapping("/api/requirements")
@CrossOrigin(origins = "*")
public class TestRequirementController {

    @Autowired
    private TestRequirementService requirementService;

    /**
     * 根据项目ID查询需求列表
     * GET /api/requirements?projectId=1
     */
    @GetMapping
    public Result<List<TestRequirement>> list(@RequestParam Integer projectId) {
        List<TestRequirement> list = requirementService.findByProjectId(projectId);
        return Result.success(list);
    }

    /**
     * 根据ID查询单个需求
     * GET /api/requirements/{id}
     */
    @GetMapping("/{id}")
    public Result<TestRequirement> getById(@PathVariable Integer id) {
        TestRequirement requirement = requirementService.findById(id);
        if (requirement == null) {
            return Result.error("需求不存在");
        }
        return Result.success(requirement);
    }

    /**
     * 新增需求
     * POST /api/requirements
     * 请求体必须包含 projectId 和 title
     */
    @PostMapping
    public Result<TestRequirement> add(@RequestBody TestRequirement requirement) {
        if (requirement.getProjectId() == null) {
            return Result.error("必须指定所属项目ID");
        }
        if (requirement.getTitle() == null || requirement.getTitle().trim().isEmpty()) {
            return Result.error("需求标题不能为空");
        }
        TestRequirement saved = requirementService.add(requirement);
        return Result.success(saved);
    }

    /**
     * 删除需求
     * DELETE /api/requirements/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        boolean success = requirementService.delete(id);
        if (!success) {
            return Result.error("删除失败，需求不存在");
        }
        return Result.success();
    }

    /**
     * 更新需求
     * PUT /api/requirements/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody TestRequirement requirement) {
        requirement.setId(id);
        boolean success = requirementService.update(requirement);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success();
    }
}
