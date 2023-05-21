package near.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("")
    public String getName() {
        try {
            Process p = Runtime.getRuntime().exec("/home/ubuntu/.nvm/versions/node/v16.13.2/bin/near send glitch-hackathon-project.winty2.testnet termo.testnet 5");
            log.info(p.getErrorStream().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hi";
    }
}
