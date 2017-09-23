package cc.xpcas.nettysocks.proxy.ssocks.cipher;

import java.security.*;

/**
 * @author xp
 */
public class CipherUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static byte[] genRandomBytes(int size) {
        byte[] iv = new byte[size];
        RANDOM.nextBytes(iv);
        return iv;
    }
}
