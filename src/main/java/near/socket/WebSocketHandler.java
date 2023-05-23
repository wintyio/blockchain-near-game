package near.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;

    private final GameService service;

    //클라이언트가 연결 되었을 때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{} 연결됨", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload {}", payload);

        ChatDTO chatMessage = mapper.readValue(payload, ChatDTO.class);
        log.info("session {}", chatMessage.toString());
        if (chatMessage.getType().equals(ChatDTO.MessageType.ENTER)) {
            // 중복 검사 로직 필요
            service.ready(session, chatMessage.getAccountId());
        }
        else if (chatMessage.getType().equals(ChatDTO.MessageType.OPEN)) {
            GameRoom room = service.findRoomById(chatMessage.getRoomId());
            if (room == null) return;
            if (room.getPlayers().get(session) == null) return;
            room.handleAction(session, chatMessage, service);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        service.exit(session);
        log.info("{} 연결 끊김.", session.getId());
    }
}