package lnjz.backrer.aiinstructor.config;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class InsecureWebClientConfig {

    /**
     * 一个忽略 SSL 校验的 WebClient Bean
     * 调用接口时直接注入使用即可
     */
    @Bean
    public WebClient insecureWebClient() {
        try {
            HttpClient httpClient = HttpClient.create()
                    .secure(ssl -> {
                        try {
                            ssl.sslContext(
                                    SslContextBuilder.forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                            .build()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("配置 SSL 出错", e);
                        }
                    });

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("创建 Insecure WebClient 失败", e);
        }
    }
}

