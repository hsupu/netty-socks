package cc.xpcas.nettysocks.utils;

import java.security.*;

import cc.xpcas.nettysocks.utils.hex.HexUtils;

/**
 * @author xp
 */
public class HashUtils {

    private static final MessageDigest digest;

    public static final String MD5 = "MD5";

    static {
        try {
            digest = MessageDigest.getInstance(MD5);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String md5(byte[] bytes) {
        byte[] result = digest.digest(bytes);
        return HexUtils.bin2hexl(result);
    }
}
