package near.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    // 메시지  타입 : 입장, 채팅
    public enum MessageType{
        ENTER, TALK, OPEN, START
    }

    private MessageType type; // 메시지 타입
    private String roomId; // 방 번호
    private String accountId;
    private int x;
    private int y;
}