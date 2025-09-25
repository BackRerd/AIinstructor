package lnjz.backrer.aiinstructor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lnjz.backrer.aiinstructor.entity.Users;
import lnjz.backrer.aiinstructor.service.UsersService;
import lnjz.backrer.aiinstructor.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
* @author JoBackRer
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-09-24 15:21:25
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Users findByUsername(String username) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public Users findByPhoneNumber(String phoneNumber) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone_number", phoneNumber);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public Users findByEmail(String email) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean checkPhoneNumberExists(String phoneNumber) {
        return findByPhoneNumber(phoneNumber) != null;
    }

    @Override
    public boolean checkEmailExists(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public Users registerUser(Users user) {
        // 生成唯一UID
        user.setUid(UUID.randomUUID().toString());

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        userMapper.insert(user);

        return user;
    }
}




