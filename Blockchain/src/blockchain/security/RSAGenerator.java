package blockchain.security;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * generator class, that is instantiated for the prupose of creating an RSA keypair.
 * Constructor takes the length argument - e.g. 1024.
 * the createKEys access point creates and stores the key to the given path. After creation the
 * pair can be accessed also by a getter.
 */
public class RSAGenerator {

    private final KeyPairGenerator keyGen;
    private KeyPair keyPair;

    public RSAGenerator(int keyLength) throws NoSuchAlgorithmException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keyLength);
    }

    public void createKeys(String publicKeyPath, String privateKeyPath) throws IOException {
        keyPair = keyGen.generateKeyPair();
        writeToFile(privateKeyPath, keyPair.getPrivate().toString());
        writeToFile(publicKeyPath, keyPair.getPublic().toString());
    }

    public void writeToFile(String path, String key) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(key);
            fileWriter.flush();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
