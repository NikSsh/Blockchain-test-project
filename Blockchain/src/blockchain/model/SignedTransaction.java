package blockchain.model;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Signed transaction, that can be stored and verified (using public key and digital signature)
 * as block data list element.
 */

public class SignedTransaction implements Serializable {

    private static final long serialVersionUID = 60L;

    private final String sender;
    private final int amount;
    private final String receiver;
    private final PublicKey publicKey;

    private byte[] signed;

    public SignedTransaction(String sender, int amount, String receiver, PublicKey publicKey) {
        this.sender = sender;
        this.amount = amount;
        this.receiver = receiver;
        this.publicKey = publicKey;
    }

    public String getSender() {
        return sender;
    }

    public int getAmount() {
        return amount;
    }

    public String getReceiver() {
        return receiver;
    }

    /**
     * set the signature. Not part of the constructor here, as the toString() method is signed after
     * instantiation.
     * @param signed the digital signature
     */
    public void setSigned(byte[] signed) {
        this.signed = signed;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getSigned() {
        return signed;
    }

    /**
     * String representation is just the transaction text who sent which amount to whom as print out.
     * @return string representation of SignedTransaction
     */
    @Override
    public String toString() {
        return String.format("%s sent %d VC to %s", sender, amount, receiver);
    }
}
