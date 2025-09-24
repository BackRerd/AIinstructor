package lnjz.backrer.aiinstructor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lnjz.backrer.aiinstructor.entity.Users;
import lnjz.backrer.aiinstructor.service.UsersService;
import lnjz.backrer.aiinstructor.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author JoBackRer
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-09-22 16:57:00
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




