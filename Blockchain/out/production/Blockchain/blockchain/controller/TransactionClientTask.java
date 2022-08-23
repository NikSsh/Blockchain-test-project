package blockchain.controller;

import blockchain.model.BlockchainFacade;
import blockchain.model.BlockchainFacadeImpl;
import blockchain.model.SignedTransaction;
import blockchain.security.RSASignerAndValidator;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static blockchain.config.BlockchainConfig.*;

/**
 * Runnable implementation, that is performed in the clients thread pool.
 * Creates random digitally signed transaction among miners and clients in random time intervals all
 * configurable in the BlockchainConfig.
 */
public class TransactionClientTask implements Runnable{
    private static final Random RANDOM = new Random();

    private final BlockchainFacade blockchain;
    private final String name;
    private final KeyPair keyPair;

    public TransactionClientTask(BlockchainFacade blockchain, String name, KeyPair keyPair) {
       this.blockchain = blockchain;
       this.name = name;
       this.keyPair = keyPair;
    }

    /**
     * the transaction client task is to send a random transaction digitally signed to the blockchain, which in
     * turn provides the ledger to choose possible money sender's
     * The amount is generated in a way, that it intentionally may exceed the balance (1.02 * possible amount) in rare
     * cases. In that case, the blockchain must reject the transaction.
     */
    protected void performClientTask() {
        Map<String, Integer> ledger = blockchain.getLedger();
        if (ledger.isEmpty()) {
            return;
        }
        String moneySender = new ArrayList<>(ledger.keySet()).get(RANDOM.nextInt(ledger.size()));
        int tryAmount = findRandomAmount(ledger.get(moneySender));
        String moneyReceiver = findRandomReceiver(moneySender);
        SignedTransaction transaction =
                new SignedTransaction(moneySender, tryAmount, moneyReceiver, keyPair.getPublic());
        transaction.setSigned(RSASignerAndValidator.sign(transaction.toString(), keyPair.getPrivate()));
        blockchain.offerTransaction(transaction);
    }

    /**
     * The amount is generated in a way, that it intentionally may exceed the balance (1.02 * possible amount) in rare
     * cases.
     * @param senderBalance the balance of the sender
     * @return the random amount - which may be slightly too high.
     */
    private int findRandomAmount(int senderBalance) {
        int amount = RANDOM.nextInt((int) (senderBalance * 1.02));
        return amount == 0 ? 1 : amount;
    }

    /**
     * randomly finds a money receiver different form the sender given. While the sender is randomly chosen only
     * among the participants who have a positive account balance in the blockchain's ledger, the receiver is chosen
     * over all miners and clients.
     * @param sender the money sender previously chosen
     * @return the name of the money receiver
     */
    private String findRandomReceiver(String sender) {
        String receiver;
        do {
            int random = RANDOM.nextInt(MINER_COUNT + CLIENT_COUNT);
            if (random < MINER_COUNT) {
                receiver =  String.format("miner%d", random + 1);
            } else {
                receiver = CLIENTS.get(random - MINER_COUNT);
            }
        } while (sender.equals(receiver));
        return receiver;
    }

    /**
     * Loop to create and sign random client objects in random time intervals both configured via BlockchainConfig.
     * Simultaneously listening to interrupt - probably superfluous since thread mostly sleeps.
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                //Thread.sleep(RANDOM.nextInt(MAX_CLIENT_PAUSE_MILLISECONDS));
                Thread.sleep(RANDOM.nextInt(5));
                performClientTask();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
