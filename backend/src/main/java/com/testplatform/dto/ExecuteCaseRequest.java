package com.testplatform.dto;

import lombok.Data;

/**
 * 执行测试用例的请求对象
 * 包含用例执行结果，以及可选的Bug信息（用例失败时可以联动创建Bug）
 */
@Data
public class ExecuteCaseRequest {

    // 用例执行状态：通过/失败/阻塞
    private String status;

    // 实际执行结果
    private String actualResult;

    // 执行人
    private String executor;

    // 执行时上传的截图附件
    private String attachment;

    // ===== 以下为可选字段，仅当 status="失败" 且需要联动创建Bug时填写 =====
    private Boolean createBug;   // 是否联动创建Bug，true=创建
    private String bugTitle;     // Bug标题
    private String bugDescription; // Bug描述
    private String bugSeverity;  // Bug严重程度：致命/严重/一般/轻微/建议
}
