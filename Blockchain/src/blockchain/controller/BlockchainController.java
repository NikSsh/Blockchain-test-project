package blockchain.controller;
import blockchain.exceptions.InvalidBlockChainException;
import blockchain.model.Block;
import blockchain.model.Blockchain;
import blockchain.model.BlockchainFacade;
import blockchain.security.RSAGenerator;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static blockchain.config.BlockchainConfig.*;

/**
 * Application logic class, that contains the run() method started by Main.
 * Presently it creates and validates a blockchain of given length.
 */
public class BlockchainController {
    private BlockchainFacade blockchain;
    private ExecutorService clients;

    /**
     * entry point invoked by Main after creation of this controller.
     */
    public void run() {
        clients = Executors.newFixedThreadPool(CLIENT_COUNT);
        try {
            blockchain = new BlockchainFacade(Blockchain.deserialize(SERIALIZE_PATH));
            blockchain.clear(); // for test reasons
            startClients(clients);
            continueGeneration(blockchain.size());
            blockchain.displayBlockchain();
            //blockchain.displayLedger();
        } catch (InvalidBlockChainException exception) {
            errorExit("Invalid blockchain detected: ", exception);
        }
        clients.shutdownNow();
    }

    /**
     * start all chat clients before the blockchain generation starts. They will produce and digitally sign
     * chat messages during all subsequent program run. The chat clients thread pool is stopped at the end
     * of the run() method, that calls this method.
     * @param clients the thread pool where the ClientTask's are submitted to
     */
    private void startClients(ExecutorService clients) {
        List <KeyPair> keys = generateKeyPairs();
        for (int i = 0; i < CLIENT_COUNT; i++) {
            clients.submit(new TransactionClientTask(blockchain, CLIENTS.get(i), keys.get(i)));
        }
    }

    /**
     * Generates keys for each client using RSA-encoding using RSAGenerator
     * @return list of all keyPairs
     */
    private List<KeyPair> generateKeyPairs() {
        List <KeyPair> keyList = new ArrayList<>(CLIENT_COUNT);
        try {
            RSAGenerator keyGenerator = new RSAGenerator(RSA_KEY_LENGTH);
            for (int i = 0; i < CLIENT_COUNT; i++) {
                keyGenerator.createKeys(KEY_PAIRS_PATH_PREFIX + CLIENTS.get(i) + PUBLIC_KEY_SUFFIX,
                        KEY_PAIRS_PATH_PREFIX + CLIENTS.get(i) + PRIVATE_KEY_SUFFIX);
                keyList.add(keyGenerator.getKeyPair());
                //log.info("Private/Public keypair generated for chat client " + CLIENTS.get(i));
            }
        } catch (NoSuchAlgorithmException | IOException exception) {
            errorExit("Error RSA-encoding key pair:", exception);
        }
        return keyList;
    }

    /**
     * this method starts all the computational block creation work. This is done asynchronously by use of a
     * miner thread pool. The thread pool call invokeAny lets all miners run with the same competing task of
     * generating the next block and the fastest wins
     * The block generation time is balanced by the blockchain in adapting the requested leading hash zeros,
     * which determine the computational complexity.
     * @param createdBlocks size of the blockchain at invocation time (> 0 if deserialized blockchain loaded)
     */
    private void continueGeneration(int createdBlocks) {
        ExecutorService miners = Executors.newFixedThreadPool(MINER_COUNT);

        try {
            // creating first block without transactions
            if (blockchain.size() == 0) {
                blockchain.addBlock(blockchain.createBlock(""));
                ++createdBlocks;
            }

            while (createdBlocks < BLOCKCHAIN_LENGTH) {
                if (!blockchain.isDataQueueEmpty()) {
                    if (!blockchain.addBlock(miners.invokeAny(getMineTasks()))) {
                        throw new InvalidBlockChainException("Invalid block received by miner !");
                    }
                    ++createdBlocks;
                }
            }
        } catch (Exception exception) {
            miners.shutdownNow();
            errorExit("Exception while blockchain creation: ", exception);
            Thread.currentThread().interrupt(); // to soothe Sonar...
        }
        miners.shutdownNow();
    }

    /**
     * creates a MineTask list (Callable<Block>) with as many copies as threads in the pool.
     * All miners get the same task, that has the computational difficulty as quantity of leading zeros in hash.
     * In addition, the mine task includes all chat messages available at this time
     * which are requested by the blockchain. They are stored as block data.
     * @return List of mine tasks for creating a new block
     */
    private List<Callable<Block>> getMineTasks() {
        MinerTaskCallable minerTask = new MinerTaskCallable(blockchain);
        return Collections.nCopies(MINER_COUNT, minerTask);
    }

    private void errorExit(String message, Exception exception) {
        clients.shutdownNow();
        System.err.println(message);
        exception.printStackTrace();
        System.exit(1);
    }
}
