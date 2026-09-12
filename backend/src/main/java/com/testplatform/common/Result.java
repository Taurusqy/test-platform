package com.testplatform.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 * 所有接口都返回这个格式，前端可以统一处理
 * 格式：{ code: 200, message: "成功", data: {...} }
 */
@Data
public class Result<T> {

    private Integer code;    // 状态码：200成功，500失败
    private String message;  // 提示信息
    private T data;          // 返回的数据（泛型，可放任意类型）

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功响应（不带数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
