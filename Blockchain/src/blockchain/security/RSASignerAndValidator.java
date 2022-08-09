package blockchain.security;

import blockchain.exceptions.BlockChainSecurityException;
import blockchain.model.SignedTransaction;

import java.security.*;

/**
 * Helper class with static methods to sign a message with a private key and verify a received signature
 * with the public key.
 */
public class RSASignerAndValidator {

    private RSASignerAndValidator() {
        // prevent instances
    }

    public static byte[] sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new BlockChainSecurityException("Exception signing chat message: " + e.getMessage());
        }
    }

    /**
     * method to verify a signed transaction by a receiver regarding authenticity,
     * The SignedTransaction object contains everything to do that (transaction that was signed - given by toString(),
     * public key and signature).
     * @param signedTransaction transaction
     * @return the verification result.
     */
    public static boolean isValid(SignedTransaction signedTransaction) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(signedTransaction.getPublicKey());
            signature.update(signature.toString().getBytes());
            return signature.verify(signedTransaction.getSigned());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new BlockChainSecurityException("Exception signing chat message: " + e.getMessage());
        }
    }
}
