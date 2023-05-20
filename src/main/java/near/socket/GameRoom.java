package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class GameRoom {
    private String roomId; // 채팅방 아이디
    private Map<WebSocketSession, Player> players = new LinkedHashMap<>();
    private Card[][] matrix;
    private int r;
    private int c;
    @Builder
    public GameRoom(String roomId, int r, int c) {
        this.roomId = roomId;
        this.r = r;
        this.c = c;
        log.info("game room builder called");

        matrix = new Card[r][c];
        int now = 1;
        int cnt = 0;
        for (int i=0; i<r; i++) {
            for (int j=0; j<c; j++) {
                matrix[i][j] = Card.builder().num(now).build();
                cnt ^= 1;
                if (cnt == 0) now++;
            }
        }
        // Math.random() 사용 배열 섞기
    }

    public void addPlayer(Player player) {
        players.put(player.getSession(), player);
    }

    public void handleAction(WebSocketSession session, ChatDTO message, GameService service) throws Exception {
        Player player = players.get(session);
        for (Player p : players.values()) {
            log.info("player : {}", p);
        }
        switch (message.getType()) {
            case TALK:
                sendMessage(message, service);
                break;
            case OPEN:
                Card card = matrix[message.getR()][message.getC()];
                if (card.isOpened() || card.isClosed()) {
                    // 카드 오픈 실패
                }
                else {
                    // 카드 오픈 성공
                    // 동시성 제어 필요
                    card.open(player.getAccountId());
                    for (int i=0; i<r; i++) {
                        for (int j=0; j<c; j++) {
                            if (i == message.getR() && j == message.getC()) continue;
                            if (matrix[i][j].isOpened() && !matrix[i][j].isClosed() && matrix[i][j].getAccountId().equals(player.getAccountId())) {
                                Card card2 = matrix[i][j];
                                if (card.getNum() == card2.getNum()) {
                                    card.setClosed(true);
                                    card2.setClosed(true);
                                    if (card.getNum() == -1) {
                                        player.setPoint(player.getPoint() - 1);
                                    } else {
                                        player.setPoint(player.getPoint() + 1);
                                    }
                                    sendPoint(service);
                                } else {
                                    sendMap(service);
                                    card.setOpened(false);
                                    card2.setOpened(false);
                                    Thread.sleep(50);
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
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.MAP).roomId(getRoomId()).build();
        int mp[][] = new int[r][c];
        for (int i=0; i<r; i++) {
            for (int j=0; j<c; j++) {
                if (matrix[i][j].isClosed() || matrix[i][j].isOpened()) mp[i][j] = matrix[i][j].getNum();
                else mp[i][j] = 0;
            }
        }
        chatDTO.setMap(mp);
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
    }

    public <T> void sendPoint(GameService service) {
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.POINT).roomId(getRoomId()).build();
        chatDTO.init();

        for (Player player : players.values()) {
            chatDTO.getPoints().put(player.getAccountId(), player.getPoint());
        }
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
    }

    public <T> void sendStart(GameService service) {
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.START).roomId(getRoomId()).build();
        chatDTO.init();

        for (Player player : players.values()) {
            log.info(player.getAccountId());
            chatDTO.addPlayer(player.getAccountId());
            chatDTO.setAccountId(player.getAccountId());
        }
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
    }

    public <T> void sendMessage(T message, GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), message));
    }

    public void openCard(GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), matrix));
    }
}