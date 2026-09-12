package com.testplatform.service;

import com.testplatform.entity.Project;
import com.testplatform.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 项目 Service 层
 * 处理业务逻辑，Controller 调用 Service，Service 调用 Mapper
 * 目前业务比较简单，主要是转发调用
 */
@Service
public class ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    // 查询所有项目
    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    // 根据ID查询项目
    public Project findById(Integer id) {
        return projectMapper.findById(id);
    }

    // 新增项目
    public Project add(Project project) {
        // 如果没传状态，默认"进行中"
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("进行中");
        }
        projectMapper.insert(project);
        return project;  // insert 后 id 已回填
    }

    // 删除项目
    public boolean delete(Integer id) {
        return projectMapper.deleteById(id) > 0;
    }

    // 更新项目
    public boolean update(Project project) {
        return projectMapper.update(project) > 0;
    }
}
