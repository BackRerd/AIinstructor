package lnjz.backrer.aiinstructor.service;

import lnjz.backrer.aiinstructor.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author JoBackRer
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2025-09-24 15:21:25
*/
public interface UsersService extends IService<Users> {
    Users findByUsername(String username);

    Users findByPhoneNumber(String phoneNumber);

    Users findByEmail(String email);

    boolean checkUsernameExists(String username);

    boolean checkPhoneNumberExists(String phoneNumber);

    boolean checkEmailExists(String email);

    Users registerUser(Users user);
}
