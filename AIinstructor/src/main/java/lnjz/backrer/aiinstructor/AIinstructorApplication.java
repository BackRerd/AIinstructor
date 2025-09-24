package lnjz.backrer.aiinstructor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("lnjz.backrer.aiinstructor.mapper")
@SpringBootApplication
public class AIinstructorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIinstructorApplication.class, args);
    }
}
