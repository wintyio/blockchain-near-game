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
    private String accountId;
    @Builder
    public Card(int num, boolean opened, boolean closed, long time, String accountId) {
        this.num = num;
        this.opened = false;
        this.closed = false;
        this.accountId = "";
        this.time = System.currentTimeMillis();
    }

    public void open(String accountId) {
        this.opened = true;
        this.accountId = accountId;
        this.time = System.currentTimeMillis();
    }
}
