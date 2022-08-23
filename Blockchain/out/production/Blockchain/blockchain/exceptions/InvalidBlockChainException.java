package blockchain.exceptions;

public class InvalidBlockChainException extends RuntimeException {
    public InvalidBlockChainException(String message) {
        super(message);
    }
}
