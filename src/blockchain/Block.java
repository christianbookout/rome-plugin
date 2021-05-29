package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Block {
    //https://www.baeldung.com/java-blockchain
    //https://www.baeldung.com/sha-256-hashing-java
    //https://levelup.gitconnected.com/creating-a-blockchain-from-scratch-9a7b123e1f3e
    private String hash;
    private String lastHash;
    private String data;
    private long timeStamp;
    private int nonce;
    private static final String PREFIX = "00";

    //what is a constructor?
    public Block(String lastHash, long timeStamp, String data) {
        this.lastHash = lastHash;
        this.timeStamp = timeStamp;
        this.data = data;
        this.hash = getHash();
    }

    //Calculates a hash based on the last hash, block data, timestamp, and nonce
    private String getHash() {
        MessageDigest digest;
        byte[] encodedHash = new byte[0];
        try {
            digest = MessageDigest.getInstance("SHA3-256");
        
            String toHash = lastHash + data + Long.toString(timeStamp) + Integer.toString(nonce);
            encodedHash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuffer toReturn = new StringBuffer();
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
    public Thread mine() {
        Runnable runnable = () -> {
            Random r = new Random();
            nonce = r.nextInt(); //what will we do
            String s;
            if (isValidHash(s = getHash())) {
                //do stuff
            }
        };
        return new Thread(runnable);
    }



}