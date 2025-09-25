package lnjz.backrer.aiinstructor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lnjz.backrer.aiinstructor.entity.ChatRecords;
import lnjz.backrer.aiinstructor.service.ChatRecordsService;
import lnjz.backrer.aiinstructor.mapper.ChatRecordsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author JoBackRer
* @description 针对表【chat_records(聊天记录表)】的数据库操作Service实现
* @createDate 2025-09-24 15:21:25
*/
@Service
public class ChatRecordsServiceImpl extends ServiceImpl<ChatRecordsMapper, ChatRecords>
    implements ChatRecordsService{

    @Override
    public List<ChatRecords> getChatRecordsByRecords(String sessions) {
        return lambdaQuery()
                .eq(ChatRecords::getSessionId,sessions)
                .list();
    }
}




