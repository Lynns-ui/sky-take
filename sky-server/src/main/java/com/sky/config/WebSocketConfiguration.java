package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * websocketServer配置类，用于注册webserver的bean对象
 */
@Configuration
public class WebSocketConfiguration {

    /**
     * ServerEndpointExporter 作用：
     * 1. 扫描项目中所有使用了 @ServerEndPoint 注解的类
     * 2. 将这些类注册为WebSocket断点
     * 3. 管理WebSocket连接的生命周期
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
