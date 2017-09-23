package cc.xpcas.nettysocks.proxy.ssocks.cipher;

import cc.xpcas.nettysocks.proxy.ssocks.cipher.worker.CipherWorker;

/**
 * @author xp
 */
public interface Cipher {

    int getKeyLength();

    int getIVLength();

    CipherWorker newWorker();
}
