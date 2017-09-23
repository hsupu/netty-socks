package cc.xpcas.nettysocks.proxy.ssocks.cipher.worker;

/**
 * @author xp
 */
public interface CipherWorker {

    void init(boolean isEncrypt, byte[] key, byte[] iv);

    byte[] process(byte[] input);
}
