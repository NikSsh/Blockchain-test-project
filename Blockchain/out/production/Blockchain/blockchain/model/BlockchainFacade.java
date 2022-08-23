package blockchain.model;

import java.util.Map;

public interface BlockchainFacade {
    void displayBlockchain();
    void displayLedger();
    boolean addBlock(Block newBlock);
    Block createBlock(String data);
    String getData();
    void clear();
    int size();
    void offerTransaction(SignedTransaction transaction);
    Map<String, Integer> getLedger();
    boolean isDataQueueEmpty();

}
