package cc.xpcas.nettysocks.utils;

import java.security.*;

import cc.xpcas.nettysocks.utils.hex.HexUtils;

/**
 * @author xp
 */
public class DigestUtils {

    private static final String MD5 = "MD5";

    private static MessageDigest getMessageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MessageDigest getMD5() {
        return getMessageDigest(MD5);
    }

    public static byte[] md5(byte[] bytes) {
        return getMessageDigest(MD5).digest(bytes);
    }

    public static String md5AndHex(byte[] bytes) {
        byte[] result = md5(bytes);
        return HexUtils.bin2hexl(result);
    }
}
