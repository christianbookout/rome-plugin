package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

public class Block {
    //https://www.baeldung.com/java-blockchain
    //https://www.baeldung.com/sha-256-hashing-java
    //https://levelup.gitconnected.com/creating-a-blockchain-from-scratch-9a7b123e1f3e
    private String hash;
    private String lastHash;
    private final String data;
    private final long timeStamp;
    private final UUID miner;
    private final ArrayList<Transaction> transactions;
    private int nonce;
    private static final String PREFIX = "00";

    //what is a constructor?
    public Block(String lastHash, long timeStamp, String data, UUID miner, ArrayList<Transaction> transactions) {
        this.lastHash = lastHash;
        this.timeStamp = timeStamp;
        this.data = data;
        this.miner = miner;
        this.transactions = transactions;
        this.hash = calculateHash();
    }

    public String getHash() {
        return hash;
    }

    //Calculates a hash based on the last hash, block data, timestamp, and nonce
    private String calculateHash() {
        MessageDigest digest;
        byte[] encodedHash;
        try {
            digest = MessageDigest.getInstance("SHA3-256");
        
            StringBuilder toHash = new StringBuilder(lastHash + data + timeStamp + nonce);
            for (Transaction transaction : transactions) {
                toHash.append(transaction.toString());
            }
            encodedHash = digest.digest(toHash.toString().getBytes(StandardCharsets.UTF_8));
            assert encodedHash != null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        StringBuilder toReturn = new StringBuilder();
        for (byte b : encodedHash) {
            toReturn.append(String.format("%02x", b));
        }
        return toReturn.toString();
    }

    //Determines if the hash is valid (if the hash begins with PREFIX)
    public static boolean isValidHash(String hash) {
        return hash.startsWith(PREFIX);
    }

    //Thread that attempts to mine
    public boolean attemptMine(int r) {
        nonce = r; //what will we do
        hash = calculateHash();
        return (isValidHash(hash));
    }

    public UUID getMiner() {
        return miner;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}