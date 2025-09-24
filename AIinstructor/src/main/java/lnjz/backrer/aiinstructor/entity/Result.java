package lnjz.backrer.aiinstructor.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private T data;
    private Integer code;

    // 成功响应
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        result.setCode(200);
        return result;
    }

    public static <T> Result<T> success(String message) {
        return success(message, null);
    }

    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }

    // 错误响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(500);
        return result;
    }

    public static <T> Result<T> error(String message, Integer code) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(code);
        return result;
    }
}