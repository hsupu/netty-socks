package cc.xpcas.nettysocks.proxy.ssocks.cipher.worker;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * @author xp
 */
public class StreamCipherWorker implements CipherWorker {

    private final StreamCipher core;

    public StreamCipherWorker(StreamCipher core) {
        this.core = core;
    }

    @Override
    public void init(boolean isEncrypt, byte[] key, byte[] iv) {
        ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key), iv);
        core.init(isEncrypt, parametersWithIV);
    }

    @Override
    public byte[] process(byte[] input) {
        byte[] output = new byte[input.length];
        core.processBytes(input, 0, input.length, output, 0);
        return output;
    }
}
