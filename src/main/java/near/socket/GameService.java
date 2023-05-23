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
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Data
@Service
@EnableScheduling
public class GameService {
    private final ObjectMapper mapper;
    private Map<String, GameRoom> gameRooms;
    private Set<Player> readyQueue;

    static final int LIMIT = 2;
    @Scheduled(cron = "0/3 * * * * ?")
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
            room.sendStart(this);
        }
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void overTime() throws Exception {
        for (Player player : readyQueue) {
            if (!player.getSession().isOpen()) {
                readyQueue.remove(player);
                break;
             }
        }
        for (GameRoom room : gameRooms.values()) {
            room.setTime(room.getTime() - 1);
            if (room.getTime() == 0) {
                List<Player> best = new ArrayList<>();
                // 동점자 처리 필요
                for (Player player : room.getPlayers().values()) {
                    if (best.isEmpty()) {
                        best.add(player);
                    }
                    else if (best.get(0).getPoint() == player.getPoint()) {
                        best.add(player);
                    }
                    else if (best.get(0).getPoint() < player.getPoint()) {
                        best.clear();
                        best.add(player);
                    }
                }
                double reward = 4.0 / best.size();
                for (Player player : best) {
                    sendMessage(player.getSession(), ChatDTO.builder().type(ChatDTO.MessageType.WIN).winNum(best.size()).build());

                    String requestURL = "http://pseong.com:3000/transfer";
                    try {
                        Map<String,Object> params = new LinkedHashMap<>();
                        params.put("amt", "" + reward);
                        params.put("rcv", "" + player.getAccountId());

                        StringBuilder postData = new StringBuilder();
                        for(Map.Entry<String,Object> param : params.entrySet()) {
                            if(postData.length() != 0) postData.append('&');
                            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                            postData.append('=');
                            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                        }
                        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                        URL url = new URL(requestURL);
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                        conn.setDoOutput(true);
                        conn.getOutputStream().write(postDataBytes); // 호출

                        StringBuilder result = new StringBuilder();
                        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            String line;
                            while ((line = buffer.readLine()) != null) {
                                result.append(line);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    room.getPlayers().remove(player.getSession());
                }
                room.sendLose(this);
                for (Player p : room.getPlayers().values()) {
                    p.getSession().close();
                }
                gameRooms.remove(room.getRoomId());
                continue;
            }
            for (Player player : room.getPlayers().values()) {
                if (!player.getSession().isOpen()) {
                    room.getPlayers().remove(player.getSession());
                    break;
                }
            }
            room.sendTime(this);
            Card[][] mp = room.getMatrix();
            int r = room.getR();
            int c = room.getC();
            boolean ok = false;
            for (int i=0; i<r; i++) {
                for (int j=0; j<c; j++) {
                    if (mp[i][j].getTime() + 2000 <= System.currentTimeMillis() && mp[i][j].isOpened() && !mp[i][j].isClosed()) {
                        mp[i][j].setOpened(false);
                        ok = true;
                    }
                }
            }
            if (ok) room.sendMap(this);
        }
    }

    public void exit(WebSocketSession session) {
        for (Player player : readyQueue) {
            if (player.getSession().equals(session)) {
                readyQueue.remove(player);
                break;
            }
        }
        for (GameRoom room : gameRooms.values()) {
            for (Player player : room.getPlayers().values()) {
                if (player.getSession().equals(session)) {
                    room.getPlayers().remove(player.getSession());
                    break;
                }
            }
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
        List<String> names = new ArrayList<>();
        for (Player p : readyQueue) {
            names.add(p.getAccountId());
        }
        ChatDTO chatDTO = ChatDTO.builder()
                .type(ChatDTO.MessageType.READY)
                .names(names)
                .build();
        for (Player p : readyQueue) {
            sendMessage(p.getSession(), chatDTO);
        }
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
                .r(5)
                .c(10)
                .roomId(roomId)
                .build();

        gameRooms.put(roomId, room);
        return room;
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}