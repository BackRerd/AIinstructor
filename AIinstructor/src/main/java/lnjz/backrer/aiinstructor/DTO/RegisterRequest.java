package lnjz.backrer.aiinstructor.DTO;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String phoneNumber;

    private String email;

    private String wechatId;

    private String studentId;
}