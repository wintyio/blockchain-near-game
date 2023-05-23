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
    private int needMap;
    private int needPoint;
    private String accountId;
    private ArrayList<Card> myCard;
    private WebSocketSession session;
    @Builder
    public Player(int point, int needMap, int needPoint, String accountId, ArrayList<Card> myCard, WebSocketSession session) {
        this.point = point;
        this.accountId = accountId;
        this.session = session;
        this.myCard = new ArrayList<>();
        this.needMap = needMap;
        this.needPoint = needPoint;
    }
}
