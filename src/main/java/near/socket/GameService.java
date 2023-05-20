package near.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Slf4j
@Data
@Service
@EnableScheduling
public class GameService {
    private final ObjectMapper mapper;
    private Map<String, GameRoom> gameRooms;
    private Set<Player> readyQueue;

    static final int LIMIT = 1;
    @Scheduled(cron = "0/5 * * * * ?")
    public void autoUpdate() throws Exception {

        // 세션 끊기면 삭제하기 필요
        log.info("queue size : {}", readyQueue.size());
        if (readyQueue.size() >= LIMIT) {
            String roomId = UUID.randomUUID().toString();
            GameRoom room = createRoom();
            log.info("roomId {} created", roomId);
            for (int i=0; i<LIMIT; i++) {
                Player player = readyQueue.iterator().next();
                readyQueue.remove(player);
                room.addPlayer(player);
            }
            gameRooms.put(roomId, room);
            room.sendStart(this);
        }
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void overTime() throws Exception {
        log.info("overTime");
        for (GameRoom room : gameRooms.values()) {
            Card[][] mp = room.getMatrix();
            int x = room.getX();
            int y = room.getY();
            boolean ok = false;
            for (int i=0; i<x; i++) {
                for (int j=0; j<y; j++) {
                    if (mp[i][j].getTime() + 3000 >= System.currentTimeMillis() && mp[i][j].isOpened() && !mp[i][j].isClosed()) {
                        mp[i][j].setOpened(false);
                        ok = true;
                    }
                }
            }
            if (ok) room.sendMap(this);
        }
    }

    @PostConstruct
    private void init() {
        gameRooms = new LinkedHashMap<>();
        readyQueue = new LinkedHashSet<>();
    }

    public void ready(WebSocketSession session, String accountId) {
        Player player = Player.builder()
                .point(0)
                .accountId(accountId)
                .session(session)
                .build();
        readyQueue.add(player);
    }

    public List<GameRoom> findAllRoom(){
        return new ArrayList<>(gameRooms.values());
    }

    public GameRoom findRoomById(String roomId){
        return gameRooms.get(roomId);
    }

    public GameRoom createRoom() {
        String roomId = UUID.randomUUID().toString();

        GameRoom room = GameRoom.builder()
                .x(5)
                .y(10)
                .roomId(roomId)
                .build();

        gameRooms.put(roomId, room);
        return room;
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        log.info("howwww");
        try{
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}