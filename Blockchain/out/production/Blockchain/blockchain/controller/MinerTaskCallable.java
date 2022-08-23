package blockchain.controller;

import blockchain.model.Block;
import blockchain.model.BlockchainFacade;
import blockchain.model.BlockchainFacadeImpl;

import java.util.concurrent.Callable;

/**
 * Callable implementation, that is performed in the miners thread pool.
 * Creates a miner and returns a block containing incoming data (transactions)
 */
public class MinerTaskCallable implements Callable<Block> {
    private final BlockchainFacade blockchain;
    private final String data;

    public MinerTaskCallable(BlockchainFacade blockchain) {
        this.blockchain = blockchain;
        data = blockchain.getData();
    }

    @Override
    public Block call() {
        return blockchain.createBlock(data);
    }
}
