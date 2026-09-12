package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.entity.Bug;
import com.testplatform.service.BugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Bug缺陷管理 Controller
 * 接口路径统一前缀：/api/bugs
 */
@RestController
@RequestMapping("/api/bugs")
@CrossOrigin(origins = "*")
public class BugController {

    @Autowired
    private BugService bugService;

    /**
     * 按项目查询Bug列表
     * GET /api/bugs?projectId=1
     */
    @GetMapping
    public Result<List<Bug>> list(@RequestParam Integer projectId) {
        List<Bug> list = bugService.findByProjectId(projectId);
        return Result.success(list);
    }

    /**
     * 根据ID查询单个Bug
     * GET /api/bugs/{id}
     */
    @GetMapping("/{id}")
    public Result<Bug> getById(@PathVariable Integer id) {
        Bug bug = bugService.findById(id);
        if (bug == null) {
            return Result.error("Bug不存在");
        }
        return Result.success(bug);
    }

    /**
     * 新增Bug
     * POST /api/bugs
     */
    @PostMapping
    public Result<Bug> add(@RequestBody Bug bug) {
        if (bug.getProjectId() == null) {
            return Result.error("必须指定所属项目ID");
        }
        if (bug.getTitle() == null || bug.getTitle().trim().isEmpty()) {
            return Result.error("Bug标题不能为空");
        }
        Bug saved = bugService.add(bug);
        return Result.success(saved);
    }

    /**
     * 修改Bug状态（带流转校验）
     * PUT /api/bugs/{id}/status
     * 请求体：{ "status": "待修复" }
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return Result.error("状态不能为空");
        }
        String error = bugService.updateStatus(id, newStatus);
        if (error != null) {
            return Result.error(error);
        }
        return Result.success();
    }

    /**
     * 编辑Bug（标题、描述、严重程度、修复人、关联用例）
     * PUT /api/bugs/{id}
     * 绑定关联用例：传 caseId 即可；解绑传 caseId=null
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Bug bug) {
        bug.setId(id);
        boolean success = bugService.update(bug);
        if (!success) {
            return Result.error("更新失败，Bug不存在");
        }
        return Result.success();
    }

    /**
     * 删除Bug
     * DELETE /api/bugs/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        boolean success = bugService.delete(id);
        if (!success) {
            return Result.error("删除失败，Bug不存在");
        }
        return Result.success();
    }
}
