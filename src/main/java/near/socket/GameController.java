package near.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class GameController {

    private final GameService service;

    @PostMapping
    public GameRoom createRoom(){
        return service.createRoom();
    }

    @GetMapping
    public List<GameRoom> findAllRooms(){
        return service.findAllRoom();
    }
}
