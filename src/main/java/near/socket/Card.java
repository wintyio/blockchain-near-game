package near.socket;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class Card {
    private int num;
    private boolean opened;
    private boolean closed;
    private long time;
    private Player player;
    @Builder
    public Card(int num, boolean opened, boolean closed, long time, Player player) {
        this.num = num;
        this.opened = false;
        this.closed = false;
        this.player = null;
        this.time = System.currentTimeMillis();
    }

    public void open(Player player) {
        this.opened = true;
        this.player = player;
        this.time = System.currentTimeMillis();
    }
}
