package blockchain.config;

import java.util.List;

public class BlockchainConfig {
    private BlockchainConfig() {
        // no instantiation
    }

    public static final int BLOCKCHAIN_LENGTH = 5;

    public static final int BLOCK_REWARD = 100;

    public static final String SERIALIZE_PATH = "./blockchain.txt";
    public static final String KEY_PAIRS_PATH_PREFIX = "../";
    public static final String PUBLIC_KEY_SUFFIX = "_rsa.pub";
    public static final String PRIVATE_KEY_SUFFIX = "_rsa";
    public static final int RSA_KEY_LENGTH = 1024;

//    public static final int BLOCK_MIN_CREATION_SECONDS = 10;
//    public static final int BLOCK_MAX_CREATION_SECONDS = 60;
//    public static final int MAX_CLIENT_PAUSE_MILLISECONDS = 200;

    public static final int BLOCK_MIN_CREATION_SECONDS = 0;
    public static final int BLOCK_MAX_CREATION_SECONDS = 1;
    public static final int MAX_CLIENT_PAUSE_MILLISECONDS = 700;

    public static final List<String> CLIENTS = List.of("Peter", "Mary", "Caspar", "Balthazar");
    public static final int CLIENT_COUNT = CLIENTS.size();
    public static final int MINER_COUNT = Runtime.getRuntime().availableProcessors() - CLIENT_COUNT;
}
