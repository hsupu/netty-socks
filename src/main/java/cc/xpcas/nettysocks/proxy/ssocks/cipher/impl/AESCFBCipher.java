package cc.xpcas.nettysocks.proxy.ssocks.cipher.impl;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;

import cc.xpcas.nettysocks.proxy.ssocks.cipher.AbstractCipher;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.worker.CipherWorker;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.worker.StreamCipherWorker;

/**
 * @author xp
 */
public class AESCFBCipher extends AbstractCipher {

    private final int bits;

    public static final AESCFBCipher AES_128_CFB = new AESCFBCipher(128);

    public static final AESCFBCipher AES_192_CFB = new AESCFBCipher(192);

    public static final AESCFBCipher AES_256_CFB = new AESCFBCipher(256);

    private AESCFBCipher(int bits) {
        super("aes-" + bits + "-cfb");
        this.bits = bits;
    }

    @Override
    public int getKeyLength() {
        return bits / 8;
    }

    @Override
    public int getIVLength() {
        return 16;
    }

    public CipherWorker newWorker() {
        AESEngine engine = new AESEngine();
        CFBBlockCipher core = new CFBBlockCipher(engine, getIVLength() * 8);
        return new StreamCipherWorker(core);
    }
}
