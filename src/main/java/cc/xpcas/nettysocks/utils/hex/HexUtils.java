package cc.xpcas.nettysocks.utils.hex;

/**
 * @author xp
 */
public class HexUtils {

    private static final char[] HEX_LOWER_CASE_DIGITS = "0123456789abcdef".toCharArray();

    private static final char[] HEX_UPPER_CASE_DIGITS = "0123456789ABCDEF".toCharArray();

    private static String bin2hex(final char[] digits, final byte[] data) {
        char[] hex = new char[digits.length * 2];
        int pos = 0;
        for (byte b : data) {
            hex[pos] = digits[(b >> 4) & 0xF];
            pos++;
            hex[pos] = digits[(b & 0xF)];
            pos++;
        }
        return new String(hex);
    }

    public static String bin2hexl(final byte[] data) {
        return bin2hex(HEX_LOWER_CASE_DIGITS, data);
    }

    public static String bin2hexu(final byte[] data) {
        return bin2hex(HEX_UPPER_CASE_DIGITS, data);
    }

    private static int atomicHex2bin(int index, char c) throws DecodeException {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        throw new DecodeException("illegal char " + c + " at " + index);
    }

    public static byte[] hex2bin(final char[] data) throws DecodeException {
        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new DecodeException("invalid data length: " + len);
        }

        final byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = atomicHex2bin(j, data[j]) << 4;
            j++;
            f = f | atomicHex2bin(j, data[j]);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }
}
