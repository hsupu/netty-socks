package cc.xpcas.nettysocks.proxy.ssocks;

import java.nio.charset.*;
import java.security.*;

import javax.crypto.SecretKey;

import cc.xpcas.nettysocks.utils.DigestUtils;

/**
 * @author xp
 */
public class SSocksSecretKey implements SecretKey {

    private static final int KEY_LENGTH = 32;

    private final int keyLength;

    private final byte[] key;

    private SSocksSecretKey(int keyLength, byte[] key) {
        this.keyLength = keyLength;
        this.key = key;
    }

    @Override
    public String getAlgorithm() {
        return "ssocks";
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        return key;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public static SSocksSecretKey of(int keyLength, String password) {
        MessageDigest md = DigestUtils.getMD5();

        byte[] pass = password.getBytes(StandardCharsets.UTF_8);
        byte[] keys = new byte[KEY_LENGTH];

        int i = 0;
        byte[] hash = null;
        byte[] temp = null;
        while (i < keys.length) {
            if (i == 0) {
                hash = md.digest(pass);
                temp = new byte[hash.length + pass.length];
            } else {
                System.arraycopy(hash, 0, temp, 0, hash.length);
                System.arraycopy(pass, 0, temp, hash.length, pass.length);
                hash = md.digest(temp);
            }
            System.arraycopy(hash, 0, keys, i, hash.length);
            i += hash.length;
        }
        if (keyLength < keys.length) {
            byte[] sliced = new byte[keyLength];
            System.arraycopy(keys, 0, sliced, 0, keyLength);
            keys = sliced;
        }
        return new SSocksSecretKey(keyLength, keys);
    }
}
