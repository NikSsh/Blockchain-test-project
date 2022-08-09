package blockchain.model;

import blockchain.config.BlockchainConfig;
import blockchain.exceptions.InvalidBlockChainException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static blockchain.config.BlockchainConfig.BLOCK_REWARD;

public class Blockchain implements Serializable {
    private static final long serialVersionUID = 1L;
    private final BlockchainHistory blockchainHistory = new BlockchainHistory(this);
    private final Map<String, Integer> ledger = new ConcurrentHashMap<>();
    private List<Block> blockchainList = new ArrayList<>();
    private transient int qtyOfZeros = 0;
    final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    final Lock readLock = reentrantLock.readLock();
    final Lock writeLock = reentrantLock.writeLock();
    public int getBlockchainSize() {
        return blockchainList.size();
    }

    public void setState(BlockchainState state) {
        this.blockchainList = state.blockchainList;
    }

    public BlockchainState getState() {
        return new BlockchainState(blockchainList);
    }

    public Block generateNewBlock(String data) {
        String prevBlockHash;
        int id;
        synchronized (readLock) {
            prevBlockHash = blockchainList.size() > 0 ? blockchainList.get(blockchainList.size() - 1).getHash() : "0";
            id = blockchainList.size() + 1;
        }
        return new Block(data, prevBlockHash, id, qtyOfZeros);
    }

    /**
    @param  newBlock corresponds to incoming block.
    @return boolean correctness execution of the operation
     After adding block to the blockchain it checks whether its valid, if not then processing undo operation
     to the state the was before adding new block.
     Also adds or updates information about miner to the ledger
     */
    public boolean addNewBlock(Block newBlock) {
        synchronized (writeLock) {
            blockchainHistory.save();
            blockchainList.add(newBlock);
            qtyOfZeros = newBlock.getQtyOfZeros();
            if (!isBlockchainValid()) {
                blockchainHistory.undo();
                return false;
            }
            addToLedger(newBlock.getMinerId());
            serialize(BlockchainConfig.SERIALIZE_PATH);
        }
        return true;
    }

    public void addTransactionToLedger(SignedTransaction transaction) {
        ledger.put(transaction.getSender(), ledger.get(transaction.getSender()) - transaction.getAmount());
        ledger.put(transaction.getReceiver(),
                ledger.getOrDefault(transaction.getReceiver(), 0) + transaction.getAmount());
    }

    /**
     * checks, if the sender has a sufficient balance for the offered transaction
     * @param transaction the transaction dat
     * @return the validity check result
     */
    public boolean isTransactionValid(SignedTransaction transaction) {
        return ledger.getOrDefault(transaction.getSender(), 0) >= transaction.getAmount();
              //  && RSASignerAndValidator.isValid(transaction);
    }

    public Map<String, Integer> getLedger() {
        return ledger;
    }

    /**
     *Checks whether blockchain is valid
     * @throws InvalidBlockChainException
     * Displays result to the console
     */
    public void displayBlockchain() throws InvalidBlockChainException {
        if (!isBlockchainValid()) {
            throw new InvalidBlockChainException("Invalid blockchain");
        }
        blockchainList.forEach(System.out::println);
    }

    public void clear() {
        blockchainList.clear();
        blockchainHistory.clear();
        ledger.clear();
    }

    public void serialize(String fileName) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
            oos.writeObject(this);
        } catch (IOException exception) {
            System.err.println("cannot serialize to file " + fileName +"\n" + exception.getMessage());
        }
    }

    public static Blockchain deserialize(String fileName) {
        try (ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
            Blockchain blockchain = (Blockchain) oos.readObject();
            if (!blockchain.isBlockchainValid()) {
                throw new InvalidBlockChainException("An invalid blockchain was deserialized");
            }
            return blockchain;
        } catch (InvalidBlockChainException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Cannot deserialize from file " + fileName +"\n It might be empty or not exists!");
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot deserialize file " + fileName +". File corrupted!\n"
                    + e.getMessage());
        }

        return new Blockchain();
    }

    static class BlockchainState implements Serializable{
        private final List<Block> blockchainList;

        private BlockchainState(List<Block> blockchainList) {
            this.blockchainList = blockchainList;
        }
    }

    private void addToLedger(long minerId) {
        String miner = String.format("miner%d", minerId);
        ledger.put(miner, ledger.getOrDefault(miner, 0) + BLOCK_REWARD);
    }
    private boolean isBlockchainValid() {
        if (blockchainList.size() < 2) {
            return true; //empty blockchain
        }

        boolean valid = true;
        for (int i = 1; i < blockchainList.size(); i++) {
            Block prevBlock = blockchainList.get(i - 1);
            Block currentBlock = blockchainList.get(i);
            if (!prevBlock.getHash().equals(currentBlock.getPrevBlockHash())) {
                valid = false;
                break;
            }
        }

        return valid;
    }
}
