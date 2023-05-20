package near.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    // 메시지  타입 : 입장, 채팅
    public enum MessageType{
        ENTER, TALK, OPEN, START, MAP, POINT, READY, TIME, WIN, LOSE
    }

    private MessageType type; // 메시지 타입
    private String roomId; // 방 번호
    private String accountId;
    private int r;
    private int c;
    private List<String> names;
    private Map<String, Integer> points;
    private int map[][];
    private int time;
    private int winNum;
}