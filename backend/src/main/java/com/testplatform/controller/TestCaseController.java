package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.dto.ExecuteCaseRequest;
import com.testplatform.entity.Bug;
import com.testplatform.entity.TestCase;
import com.testplatform.service.TestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 测试用例管理 Controller
 * 接口路径统一前缀：/api/cases
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class TestCaseController {

    @Autowired
    private TestCaseService testCaseService;

    /**
     * 根据需求ID查询用例列表
     * GET /api/cases?requirementId=1
     */
    @GetMapping
    public Result<List<TestCase>> list(@RequestParam Integer requirementId) {
        List<TestCase> list = testCaseService.findByRequirementId(requirementId);
        return Result.success(list);
    }

    /**
     * 根据ID查询单个用例
     * GET /api/cases/{id}
     */
    @GetMapping("/{id}")
    public Result<TestCase> getById(@PathVariable Integer id) {
        TestCase testCase = testCaseService.findById(id);
        if (testCase == null) {
            return Result.error("用例不存在");
        }
        return Result.success(testCase);
    }

    /**
     * 新增用例
     * POST /api/cases
     */
    @PostMapping
    public Result<TestCase> add(@RequestBody TestCase testCase) {
        if (testCase.getRequirementId() == null) {
            return Result.error("必须指定所属需求ID");
        }
        if (testCase.getTitle() == null || testCase.getTitle().trim().isEmpty()) {
            return Result.error("用例标题不能为空");
        }
        TestCase saved = testCaseService.add(testCase);
        return Result.success(saved);
    }

    /**
     * 编辑用例
     * PUT /api/cases/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody TestCase testCase) {
        testCase.setId(id);
        boolean success = testCaseService.update(testCase);
        if (!success) {
            return Result.error("更新失败，用例不存在");
        }
        return Result.success();
    }

    /**
     * 删除用例
     * DELETE /api/cases/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        boolean success = testCaseService.delete(id);
        if (!success) {
            return Result.error("删除失败，用例不存在");
        }
        return Result.success();
    }

    /**
     * 执行用例（标记通过/失败/阻塞）
     * POST /api/cases/{id}/execute
     * 如果状态为"失败"且 createBug=true，会联动创建一条Bug
     * 返回值：如果创建了Bug则返回Bug对象，否则返回null
     */
    @PostMapping("/{id}/execute")
    public Result<Bug> execute(@PathVariable Integer id, @RequestBody ExecuteCaseRequest request) {
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            return Result.error("执行状态不能为空");
        }
        // 校验状态值合法性
        if (!List.of("通过", "失败", "阻塞").contains(request.getStatus())) {
            return Result.error("状态只能是：通过/失败/阻塞");
        }
        try {
            Bug createdBug = testCaseService.execute(id, request);
            return Result.success(createdBug);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
