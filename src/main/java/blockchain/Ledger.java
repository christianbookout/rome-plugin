package blockchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Ledger {
    // fake blockchain :(

    private String prevBlockHash = "0000000000000000000000000000000000000000000000000000000000000000";
    private final HashMap<UUID, Float> balance;
    private final ArrayList<Transaction> pendingTransactions;

    public Ledger() {
        this.balance = new HashMap<>();
        pendingTransactions = new ArrayList<>();
    }

    public float getBalance(UUID target) {
        return balance.get(target);
    }

    public synchronized boolean enqueueTransaction(Transaction t) {
        if (balance.get(t.sender) < t.amount) {
            return false;
        }
        pendingTransactions.add(t);
        return true;
    }

    private synchronized void incrementBalance(UUID target) {
        balance.put(target, balance.get(target) + 1);
    }

    private void handleTransaction(Transaction t) {
        balance.put(t.sender, balance.get(t.sender) - t.amount);
        balance.put(t.receiver, balance.get(t.receiver) + t.amount);
    }

    public String getPrevHash() {
        return prevBlockHash;
    }

    public Block getMineTarget(UUID miner) {
        return new Block(prevBlockHash, Instant.now().getEpochSecond(), "", miner, pendingTransactions);
    }

    public synchronized void submitValidBlock(Block block) {
        System.out.println("block mined " + block);
        this.prevBlockHash = block.getHash();
        incrementBalance(block.getMiner());
        for (Transaction transaction : block.getTransactions()) {
            handleTransaction(transaction);
        }
        this.pendingTransactions.clear();
    }
}
