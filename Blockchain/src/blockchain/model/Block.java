package blockchain.model;

import blockchain.config.BlockchainConfig;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

public class Block implements Serializable {
    private final int startQtyOfZeros;
    private final long id;
    private final long timeStamp;
    private final String prevBlockHash;
    private final String hash;
    private final String data;
    private final long minerId;
    private long generatedHashTime;
    private int endQtyOfZeros;
    private int magicNumber = 0;

    Block (String data, String prevBlockHash, long id, int qtyOfZeros) {
        this.data = data.strip();
        this.prevBlockHash = prevBlockHash;
        this.id = id;
        this.startQtyOfZeros = qtyOfZeros;
        timeStamp = new Date().getTime();
        hash = generateHash(qtyOfZeros);
        minerId = Thread.currentThread().getId();
    }

    public String getHash() {
        return hash;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public int getQtyOfZeros() {
        return endQtyOfZeros;
    }

    public long getMinerId() {
        return minerId;
    }

    public String generateHash(int qtyOfZeros) {
        try {

            LocalTime start = LocalTime.now();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            boolean generated = false;
            StringBuilder hexString;

            Random random = new Random();
            do {
                String blockLines = id + timeStamp + prevBlockHash + magicNumber;
                /* Applies sha256 to our content */
                byte[] hash = digest.digest(blockLines.getBytes(StandardCharsets.UTF_8));

                hexString = new StringBuilder();

                for (byte elem : hash) {
                    String hex = Integer.toHexString(0xff & elem);
                    if (hex.length() == 1) hexString.append('0');

                    if (!generated && hexString.length() >= qtyOfZeros) {
                        if (!hexString.toString().startsWith("0".repeat(qtyOfZeros))) {
                            magicNumber =  Math.abs(random.nextInt());
                            break;
                        }
                        generated = true;
                    }
                    hexString.append(hex);
                }

            } while (!generated);

            LocalTime end = LocalTime.now();
            generatedHashTime = Duration.between(start, end).toSeconds();

            endQtyOfZeros = generatedHashTime < BlockchainConfig.BLOCK_MIN_CREATION_SECONDS ? ++qtyOfZeros : (qtyOfZeros < BlockchainConfig.BLOCK_MAX_CREATION_SECONDS ? qtyOfZeros : --qtyOfZeros);
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Block:\n" +
                "Created by: miner" + minerId + "\n" +
                "miner" + minerId + " gets " + BlockchainConfig.BLOCK_REWARD + " VC\n" +
                "Id: " + id + "\n" +
                "Timestamp: " + timeStamp + "\n" +
                "Magic number: " + magicNumber + "\n" +
                "Hash of the previous block:\n" + prevBlockHash + "\n" +
                "Hash of the block:\n" + hash + "\n" +
                "Block data:\n" + (!data.isEmpty() ? data : "No transactions") + "\n" +
                "Block was generating for " + generatedHashTime + " seconds\n" +
                "N " + (endQtyOfZeros - startQtyOfZeros > 0 ? "was increased by " + (endQtyOfZeros - startQtyOfZeros)
                : (endQtyOfZeros - startQtyOfZeros < 0 ? "was decreased by " + (endQtyOfZeros - startQtyOfZeros) : "stays the same")) + "\n";
    }

}
