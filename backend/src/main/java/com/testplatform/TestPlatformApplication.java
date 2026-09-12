package com.testplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot 应用主启动类
 * 运行这个类的 main 方法即可启动整个后端服务
 */
@SpringBootApplication
// @MapperScan 告诉 MyBatis 去哪个包下扫描 Mapper 接口
// 这样就不用在每个 Mapper 接口上单独写 @Mapper 注解了
@MapperScan("com.testplatform.mapper")
public class TestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestPlatformApplication.class, args);
        System.out.println("====== 测试用例&缺陷管理平台 后端启动成功 ======");
    }
}
