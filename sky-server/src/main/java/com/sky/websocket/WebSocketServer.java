package com.sky.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")    // 这个注解的作用，与配置类配合，可以让该类被Spring容器管理
public class WebSocketServer {

    // 存放的建立好的session会话，可以在会话中进行数据交互
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 与客户端建立连接成功执行的函数，属于回调函数
     * @param session 连接成功的会话
     * @param sid 客户端的id
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 来自客户端的消息
     * @param sid 客户端那边的动态的id
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到客户端：" + sid + "的信息：" + message);
    }

    /**
     * 断开连接
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开：" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 服务端主动向客户端发送消息，这是群发，因为遍历了sessions
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // 服务端主动向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
