package cc.xpcas.nettysocks.proxy.ssocks;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.proxy.ssocks.cipher.Cipher;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.CipherUtils;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.worker.CipherWorker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author xp
 */
public class SSocksEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger LOG = LoggerFactory.getLogger(SSocksEncoder.class);

    private final Cipher cipher;

    private final SecretKey key;

    private final CipherWorker worker;

    private boolean initialized = false;

    public SSocksEncoder(Cipher cipher, SecretKey key) {
        this.cipher = cipher;
        this.key = key;
        this.worker = cipher.newWorker();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        if (!initialized) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("init encoder IV");
            }
            byte[] iv = CipherUtils.genRandomBytes(cipher.getIVLength());
            worker.init(true, key.getEncoded(), iv);
            out.writeBytes(iv);
            initialized = true;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("encode payload");
        }
        byte[] plain = new byte[msg.readableBytes()];
        msg.readBytes(plain);
        byte[] encoded = worker.process(plain);
        out.writeBytes(encoded);
    }
}
