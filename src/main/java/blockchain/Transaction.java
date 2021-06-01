package blockchain;

import java.util.UUID;

public class Transaction {
    public final UUID sender;
    public final UUID receiver;
    public final float amount;

    public Transaction(UUID sender, UUID receiver, float amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "" + sender + receiver + amount;
    }
}
