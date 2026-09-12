package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.entity.Project;
import com.testplatform.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 项目管理 Controller
 * 提供项目的增删改查 REST 接口
 * 接口路径统一前缀：/api/projects
 */
@RestController
@RequestMapping("/api/projects")
// 允许跨域，前端 Vue 开发服务器端口不同，需要这个注解
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 查询项目列表
     * GET /api/projects
     */
    @GetMapping
    public Result<List<Project>> list() {
        List<Project> list = projectService.findAll();
        return Result.success(list);
    }

    /**
     * 根据ID查询单个项目
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Integer id) {
        Project project = projectService.findById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 新增项目
     * POST /api/projects
     * 请求体：{ "name": "xxx", "description": "xxx", "status": "进行中" }
     */
    @PostMapping
    public Result<Project> add(@RequestBody Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        Project saved = projectService.add(project);
        return Result.success(saved);
    }

    /**
     * 删除项目
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        boolean success = projectService.delete(id);
        if (!success) {
            return Result.error("删除失败，项目不存在");
        }
        return Result.success();
    }

    /**
     * 更新项目
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Project project) {
        project.setId(id);
        boolean success = projectService.update(project);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success();
    }
}
