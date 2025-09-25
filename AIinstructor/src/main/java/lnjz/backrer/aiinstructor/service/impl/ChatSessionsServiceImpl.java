package lnjz.backrer.aiinstructor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lnjz.backrer.aiinstructor.entity.ChatSessions;
import lnjz.backrer.aiinstructor.service.ChatSessionsService;
import lnjz.backrer.aiinstructor.mapper.ChatSessionsMapper;
import org.springframework.stereotype.Service;

/**
* @author JoBackRer
* @description 针对表【chat_sessions(聊天会话索引表（Tab级）)】的数据库操作Service实现
* @createDate 2025-09-24 15:21:25
*/
@Service
public class ChatSessionsServiceImpl extends ServiceImpl<ChatSessionsMapper, ChatSessions>
    implements ChatSessionsService{

}




