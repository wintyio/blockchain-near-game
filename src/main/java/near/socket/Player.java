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
    private WebSocketSession session;
    private ArrayList<Card> myCard;
    @Builder
    public Player(int point, String accountId, WebSocketSession session, ArrayList<Card> myCard) {
        this.point = point;
        this.accountId = accountId;
        this.session = session;
        this.myCard = new ArrayList<>();
    }
}
