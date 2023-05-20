package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Data
@Builder
@Getter
@Setter
public class Player {
    private int point;
    private String accountId;
    private WebSocketSession session;
    @Builder
    public Player(int point, String accountId, WebSocketSession session) {
        this.point = point;
        this.accountId = accountId;
        this.session = session;
    }
}
