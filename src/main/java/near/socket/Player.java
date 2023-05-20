package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class Player {
    private int point;
    @Builder
    public Player(int point) {
        this.point = point;
    }
}
