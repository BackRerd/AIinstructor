package lnjz.backrer.aiinstructor.DTO;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String uid;

    public LoginResponse(String token, String username, String uid) {
        this.token = token;
        this.username = username;
        this.uid = uid;
    }
}