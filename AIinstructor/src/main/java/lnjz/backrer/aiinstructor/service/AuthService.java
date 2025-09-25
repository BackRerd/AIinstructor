package lnjz.backrer.aiinstructor.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lnjz.backrer.aiinstructor.DTO.LoginRequest;
import lnjz.backrer.aiinstructor.DTO.LoginResponse;
import lnjz.backrer.aiinstructor.DTO.RegisterRequest;
import lnjz.backrer.aiinstructor.entity.Result;
import lnjz.backrer.aiinstructor.entity.Users;
import lnjz.backrer.aiinstructor.utils.JwtTokenUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    @Getter
    @Autowired
    private UsersService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 登录方法 - 修正：使用正确的validateToken方法
    public Result<LoginResponse> login(LoginRequest loginRequest) {
        Users user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtTokenUtil.generateToken(user.getUserName(), user.getUid());
        LoginResponse loginResponse = new LoginResponse(token, user.getUserName(), user.getUid());

        return Result.success("登录成功", loginResponse);
    }

    // 注册方法
    public Result<?> register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userService.checkUsernameExists(registerRequest.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 检查手机号是否已存在
        if (userService.checkPhoneNumberExists(registerRequest.getPhoneNumber())) {
            return Result.error("手机号已被注册");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (StringUtils.hasText(registerRequest.getEmail()) &&
                userService.checkEmailExists(registerRequest.getEmail())) {
            return Result.error("邮箱已被注册");
        }

        // 创建用户对象
        Users user = new Users();
        user.setUserName(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setEmail(registerRequest.getEmail());
        user.setWechatId(registerRequest.getWechatId());
        user.setStudentId(registerRequest.getStudentId());

        try {
            // 注册用户
            Users registeredUser = userService.registerUser(user);

            // 注册成功后自动登录，返回token
            String token = jwtTokenUtil.generateToken(registeredUser.getUserName(), registeredUser.getUid());
            LoginResponse loginResponse = new LoginResponse(token, registeredUser.getUserName(), registeredUser.getUid());

            return Result.success("注册成功", loginResponse);

        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 验证并刷新Token
     * @param token 原始token（可能带有Bearer前缀）
     * @return 包含新token的响应
     */
    public Result<LoginResponse> verifyAndRefreshToken(String token) {
        try {
            // 移除可能存在的Bearer前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 验证token并获取用户信息
            String username = jwtTokenUtil.getUsernameFromToken(token);
            String uid = jwtTokenUtil.getUidFromToken(token);

            if (username == null || uid == null) {
                return Result.error("无效的token");
            }

            // 检查用户是否存在
            Users user = userService.findByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证token是否有效
            if (!jwtTokenUtil.validateToken(token, username)) {
                return Result.error("token已过期或无效");
            }

            // 生成新的token（刷新token）
            String newToken = jwtTokenUtil.refreshToken(token);
            LoginResponse loginResponse = new LoginResponse(newToken, username, uid);

            return Result.success("token验证成功", loginResponse);

        } catch (ExpiredJwtException e) {
            // token已过期的特殊处理
            return Result.error("token已过期");
        } catch (SignatureException e) {
            // 签名错误
            return Result.error("无效的token签名");
        } catch (Exception e) {
            // 其他错误
            return Result.error("token验证失败：" + e.getMessage());
        }
    }
}