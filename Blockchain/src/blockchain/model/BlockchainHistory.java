package blockchain.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Used to save blockchain history and undo it
 */
class BlockchainHistory implements Serializable {
    private final Blockchain blockchain;
    private final Deque<Blockchain.BlockchainState> history = new ArrayDeque<>();

    BlockchainHistory(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public void save() {
        history.push(blockchain.getState());
    }

    public void undo() {
        if (!history.isEmpty()) {
            blockchain.setState(history.pop());
        }
    }

    public void clear() {
        history.clear();
    }
}
