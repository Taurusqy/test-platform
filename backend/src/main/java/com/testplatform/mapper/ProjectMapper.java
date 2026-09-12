package com.testplatform.mapper;

import com.testplatform.entity.Project;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 项目 Mapper 接口
 * 负责 project 表的增删改查
 */
@Mapper
public interface ProjectMapper {

    // 查询所有项目（按创建时间倒序）
    List<Project> findAll();

    // 根据ID查询单个项目
    Project findById(@Param("id") Integer id);

    // 新增项目
    @Insert("INSERT INTO project (name, description, status, start_date, end_date) VALUES (#{name}, #{description}, #{status}, #{startDate}, #{endDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Project project);

    // 根据ID删除项目
    @Delete("DELETE FROM project WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 更新项目
    @Update("UPDATE project SET name=#{name}, description=#{description}, status=#{status}, " +
            "start_date=#{startDate}, end_date=#{endDate}, " +
            "update_time=datetime('now','localtime') WHERE id=#{id}")
    int update(Project project);
}
