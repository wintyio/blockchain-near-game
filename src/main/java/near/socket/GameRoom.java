package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Data
@Slf4j
public class GameRoom {
    private String roomId; // 채팅방 아이디
    private Map<WebSocketSession, Player> players = new LinkedHashMap<>();
    private Card[][] matrix;
    private int time;
    private int r;
    private int c;

    private static int[] shuffle(int[] numberList) {
        for (int i = 0; i < numberList.length; i++) {
            int a = (int) (Math.random() * numberList.length);

            int tmp = numberList[a];
            numberList[a] = numberList[i];
            numberList[i] = tmp;
        }

        return numberList;
    }

    @Builder
    public GameRoom(String roomId, int r, int c) {
        this.roomId = roomId;
        this.r = r;
        this.c = c;
        this.time = 63;
        log.info("game room builder called");

        matrix = new Card[r][c];
        int order[] = new int[r*c];
        order[0] = -1;
        order[1] = -1;
        order[2] = -1;
        order[3] = -1;
        int now = 1;
        int cnt = 0;
        for (int i=4; i<r*c; i++) {
            order[i] = now;
            cnt ^= 1;
            if (cnt == 0) now++;
        }
        shuffle(order);
        for (int i=0; i<r; i++) {
            for (int j=0; j<c; j++) {
                matrix[i][j] = Card.builder().num(order[j*r + i]).build();
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
                synchronized (this) {
                    if (card.isOpened() || card.isClosed() || player.getMyCard().size() >= 2) break;
                    player.getMyCard().add(card);
                    card.open(player.getAccountId());
                }
                sendMap(service);
                if (player.getMyCard().size() >= 2) {
                    Card card1 = player.getMyCard().get(0);
                    Card card2 = player.getMyCard().get(1);
                    if (card1.getNum() == card2.getNum()) {
                        card1.setClosed(true);
                        card2.setClosed(true);
                        if (card1.getNum() == -1) {
                            player.setPoint(player.getPoint() - 1);
                        } else {
                            player.setPoint(player.getPoint() + 1);
                        }
                        sendPoint(service);
                    } else {
                        card1.setOpened(false);
                        card2.setOpened(false);
                    }
                    player.getMyCard().clear();
                }
                Thread.sleep(600);
                sendMap(service);
                break;
        }
    }

    public <T> void sendTime(GameService service) {
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.TIME).roomId(getRoomId()).time(time).build();
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
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
        Map<String, Integer> points = new HashMap<>();
        for (Player player : players.values()) {
            points.put(player.getAccountId(), player.getPoint());
        }
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.POINT).roomId(getRoomId()).points(points).build();
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
    }

    public <T> void sendLose(GameService service) {
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.LOSE).build();
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
    }

    public <T> void sendStart(GameService service) {
        List<String> names = new ArrayList<>();
        for (Player player : players.values()) {
            names.add(player.getAccountId());
        }
        ChatDTO chatDTO = ChatDTO.builder().type(ChatDTO.MessageType.START).roomId(getRoomId()).names(names).build();
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), chatDTO));
        sendMap(service);
    }

    public <T> void sendMessage(T message, GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), message));
    }

    public void openCard(GameService service) {
        players.entrySet().parallelStream().forEach(entry -> service.sendMessage(entry.getKey(), matrix));
    }
}