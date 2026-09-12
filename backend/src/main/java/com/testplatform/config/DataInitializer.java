package com.testplatform.config;

import com.testplatform.entity.Project;
import com.testplatform.entity.TestCase;
import com.testplatform.entity.TestRequirement;
import com.testplatform.mapper.ProjectMapper;
import com.testplatform.mapper.TestCaseMapper;
import com.testplatform.mapper.TestRequirementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 应用启动时自动插入示例数据（如果项目表为空）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private TestRequirementMapper requirementMapper;
    @Autowired
    private TestCaseMapper testCaseMapper;

    @Override
    public void run(String... args) {
        if (projectMapper.findAll().size() > 0) {
            return;
        }

        // 1. 示例项目
        Project project = new Project();
        project.setName("示例项目-电商平台");
        project.setDescription("用于演示的电商系统测试项目");
        project.setStatus("进行中");
        project.setStartDate("2026-09-01");
        project.setEndDate("2026-12-31");
        projectMapper.insert(project);

        // 2. 需求
        TestRequirement req1 = new TestRequirement();
        req1.setReqNo("REQ-001");
        req1.setProjectId(project.getId());
        req1.setTitle("用户登录功能");
        req1.setContent("支持账号密码登录，错误密码提示");
        req1.setPriority("高");
        req1.setStatus("已实现");
        requirementMapper.insert(req1);

        TestRequirement req2 = new TestRequirement();
        req2.setReqNo("REQ-002");
        req2.setProjectId(project.getId());
        req2.setTitle("购物车功能");
        req2.setContent("添加商品、修改数量、删除商品");
        req2.setPriority("中");
        req2.setStatus("分析中");
        requirementMapper.insert(req2);

        // 3. 测试用例
        TestCase case1 = new TestCase();
        case1.setCaseNo("TC-001");
        case1.setRequirementId(req1.getId());
        case1.setTitle("正确账号密码登录成功");
        case1.setTestPurpose("验证正常登录流程");
        case1.setPrecondition("用户已注册");
        case1.setSteps("1.输入正确账号\n2.输入正确密码\n3.点击登录");
        case1.setExpectedResult("登录成功，跳转到首页");
        case1.setPriority("P0");
        case1.setCaseType("功能测试");
        case1.setStatus("未执行");
        testCaseMapper.insert(case1);

        TestCase case2 = new TestCase();
        case2.setCaseNo("TC-002");
        case2.setRequirementId(req1.getId());
        case2.setTitle("错误密码登录失败");
        case2.setTestPurpose("验证错误密码的异常提示");
        case2.setPrecondition("用户已注册");
        case2.setSteps("1.输入正确账号\n2.输入错误密码\n3.点击登录");
        case2.setExpectedResult("提示密码错误，停留在登录页");
        case2.setPriority("P1");
        case2.setCaseType("功能测试");
        case2.setStatus("未执行");
        testCaseMapper.insert(case2);

        TestCase case3 = new TestCase();
        case3.setCaseNo("TC-003");
        case3.setRequirementId(req2.getId());
        case3.setTitle("添加商品到购物车");
        case3.setTestPurpose("验证购物车添加功能");
        case3.setPrecondition("用户已登录，商品有库存");
        case3.setSteps("1.进入商品详情页\n2.点击加入购物车");
        case3.setExpectedResult("购物车数量+1，商品显示在列表中");
        case3.setPriority("P1");
        case3.setCaseType("功能测试");
        case3.setStatus("未执行");
        testCaseMapper.insert(case3);

        System.out.println("====== 示例数据初始化完成 ======");
    }
}
