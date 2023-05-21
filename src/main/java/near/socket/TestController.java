package near.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("")
    public String getName() {
        try {
            Runtime.getRuntime().exec("near send glitch-hackathon-project.winty2.testnet termo.testnet 5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hi";
    }
}
