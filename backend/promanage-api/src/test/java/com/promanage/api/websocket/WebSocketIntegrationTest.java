package com.promanage.api.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.websocket.WebSocketMessage;

/** WebSocket集成测试 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

  @LocalServerPort private int port;

  private WebSocketClient webSocketClient;
  private ObjectMapper objectMapper;
  private String webSocketUrl;

  @BeforeEach
  void setUp() {
    webSocketClient = new StandardWebSocketClient();
    objectMapper = new ObjectMapper();
    webSocketUrl = "ws://localhost:" + port + "/ws/notifications?token=userId:123";
  }

  @Test
  void testWebSocketConnection() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    WebSocketSession session = null;

    try {
      // 连接WebSocket
      session =
          webSocketClient
              .execute(
                  new WebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session)
                        throws Exception {
                      latch.countDown();
                    }

                    @Override
                    public void handleMessage(
                        WebSocketSession session,
                        org.springframework.web.socket.WebSocketMessage<?> message)
                        throws Exception {
                      // 处理消息
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception)
                        throws Exception {
                      // 处理错误
                    }

                    @Override
                    public void afterConnectionClosed(
                        WebSocketSession session, CloseStatus closeStatus) throws Exception {
                      // 连接关闭
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                      return false;
                    }
                  },
                  null,
                  URI.create(webSocketUrl))
              .get();

      // 等待连接建立
      assertTrue(latch.await(5, TimeUnit.SECONDS), "WebSocket连接应在5秒内建立");
      assertTrue(session.isOpen(), "WebSocket会话应该是打开状态");
    } finally {
      if (session != null && session.isOpen()) {
        session.close();
      }
    }
  }

  @Test
  void testSendMessage() throws Exception {
    CountDownLatch connectionLatch = new CountDownLatch(1);
    CountDownLatch messageLatch = new CountDownLatch(1);
    WebSocketSession session = null;
    WebSocketMessage[] receivedMessage = new WebSocketMessage[1];

    try {
      // 连接WebSocket
      session =
          webSocketClient
              .execute(
                  new WebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session)
                        throws Exception {
                      connectionLatch.countDown();
                      // 发送心跳消息
                      session.sendMessage(new TextMessage("heartbeat"));
                    }

                    @Override
                    public void handleMessage(
                        WebSocketSession session,
                        org.springframework.web.socket.WebSocketMessage<?> message)
                        throws Exception {
                      if (message instanceof TextMessage) {
                        String payload = ((TextMessage) message).getPayload();
                        receivedMessage[0] =
                            objectMapper.readValue(payload, WebSocketMessage.class);
                        messageLatch.countDown();
                      }
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception)
                        throws Exception {
                      // 处理错误
                    }

                    @Override
                    public void afterConnectionClosed(
                        WebSocketSession session, CloseStatus closeStatus) throws Exception {
                      // 连接关闭
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                      return false;
                    }
                  },
                  null,
                  URI.create(webSocketUrl))
              .get();

      // 等待连接建立和消息接收
      assertTrue(connectionLatch.await(5, TimeUnit.SECONDS), "WebSocket连接应在5秒内建立");
      assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "应在5秒内收到心跳响应");

      // 验证心跳响应
      assertNotNull(receivedMessage[0], "应该收到心跳响应消息");
      assertEquals("heartbeat", receivedMessage[0].getType(), "消息类型应该是heartbeat");
      assertEquals("pong", receivedMessage[0].getContent(), "心跳响应内容应该是pong");
    } finally {
      if (session != null && session.isOpen()) {
        session.close();
      }
    }
  }

  @Test
  void testReceiveNotificationMessage() throws Exception {
    CountDownLatch connectionLatch = new CountDownLatch(1);
    CountDownLatch messageLatch = new CountDownLatch(1);
    WebSocketSession session = null;
    WebSocketMessage[] receivedMessage = new WebSocketMessage[1];

    try {
      // 连接WebSocket
      session =
          webSocketClient
              .execute(
                  new WebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session)
                        throws Exception {
                      connectionLatch.countDown();
                    }

                    @Override
                    public void handleMessage(
                        WebSocketSession session,
                        org.springframework.web.socket.WebSocketMessage<?> message)
                        throws Exception {
                      if (message instanceof TextMessage) {
                        String payload = ((TextMessage) message).getPayload();
                        receivedMessage[0] =
                            objectMapper.readValue(payload, WebSocketMessage.class);
                        if ("notification".equals(receivedMessage[0].getType())) {
                          messageLatch.countDown();
                        }
                      }
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception)
                        throws Exception {
                      // 处理错误
                    }

                    @Override
                    public void afterConnectionClosed(
                        WebSocketSession session, CloseStatus closeStatus) throws Exception {
                      // 连接关闭
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                      return false;
                    }
                  },
                  null,
                  URI.create(webSocketUrl))
              .get();

      // 等待连接建立
      assertTrue(connectionLatch.await(5, TimeUnit.SECONDS), "WebSocket连接应在5秒内建立");

      // 这里需要手动触发发送通知消息，或者通过其他API调用
      // 在实际测试中，可能需要调用NotificationService来发送通知

      // 等待通知消息
      // assertTrue(messageLatch.await(10, TimeUnit.SECONDS), "应在10秒内收到通知消息");
      // assertNotNull(receivedMessage[0], "应该收到通知消息");
      // assertEquals("notification", receivedMessage[0].getType(), "消息类型应该是notification");
    } finally {
      if (session != null && session.isOpen()) {
        session.close();
      }
    }
  }
}
