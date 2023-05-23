package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Getter
@Setter
public class Player {
    private int point;
    private String accountId;
    private List<Card> myCard;
    private WebSocketSession session;
    @Builder
    public Player(int point, String accountId, WebSocketSession session) {
        this.point = point;
        this.accountId = accountId;
        this.session = session;
        this.myCard = new ArrayList<>();
    }
}
