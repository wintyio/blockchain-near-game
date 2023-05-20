package near.socket;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Data
public class GameRoom {
    private String roomId; // 채팅방 아이디
    private Map<WebSocketSession, Player> players = new LinkedHashMap<>();
    private Card[][] matrix;
    private int x;
    private int y;
    @Builder
    public GameRoom(String roomId, int x, int y){
        this.roomId = roomId;
        matrix = new Card[x][y];
        this.x = x;
        this.y = y;

        int now = 1;
        int cnt = 0;
        for (int i=0; i<x; i++) {
            for (int j=0; j<y; j++) {
                matrix[i][j] = Card.builder().num(now).build();
                cnt ^= 1;
                if (cnt == 0) now++;
            }
        }
        // Math.random() 사용 배열 섞기
    }

    public void addPlayer(WebSocketSession session) {
        players.put(session, Player.builder().point(0).build());
    }

    public void handleAction(WebSocketSession session, ChatDTO message, GameService service) {
        Player player = players.get(session);

        switch (message.getType()) {
            case TALK:
                sendMessage(message, service);
                break;
            case OPEN:
                Card card = matrix[message.getX()][message.getY()];
                if (card.isOpened() || card.isClosed()) {
                    // 카드 오픈 실패
                }
                else {
                    // 카드 오픈 성공
                    // 동시성 제어 필요
                    card.open(player);
                    for (int i=0; i<x; i++) {
                        for (int j = 0; j < y; j++) {
                            if (i == message.getX() && j == message.getY()) continue;
                            if (matrix[i][j].isOpened() && matrix[i][j].getPlayer().equals(player)) {
                                Card card2 = matrix[message.getX()][message.getY()];
                                if (card.getNum() == card2.getNum()) {
                                    card.setClosed(true);
                                    card2.setClosed(true);
                                    if (card.getNum() == -1) {
                                        player.setPoint(player.getPoint() - 1);
                                    }
                                } else {
                                    card.setOpened(false);
                                    card2.setOpened(false);
                                }
                            }
                        }
                    }
                    sendMap(service);
                }
                break;
        }
    }

    public <T> void sendMap(GameService service) {
        int mp[][] = new int[x][y];
        for (int i=0; i<x; i++) {
            for (int j=0; j<y; j++) {
                if (matrix[i][j].isClosed() || matrix[i][j].isOpened()) mp[i][j] = matrix[i][j].getNum();
                else mp[i][j] = 0;
            }
        }
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), mp));
    }

    public <T> void sendStart(GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), "start"));
    }

    public <T> void sendMessage(T message, GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), message));
    }

    public void openCard(GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), matrix));
    }
}