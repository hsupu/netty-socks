package cc.xpcas.nettysocks.proxy.ssocks;

import java.util.*;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.proxy.ssocks.cipher.Cipher;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.worker.CipherWorker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author xp
 */
public class SSocksDecoder extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(SSocksDecoder.class);

    private final Cipher cipher;

    private final SecretKey key;

    private final CipherWorker worker;

    private boolean initialized = false;

    public SSocksDecoder(Cipher cipher, SecretKey key) {
        this.cipher = cipher;
        this.key = key;
        this.worker = cipher.newWorker();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!initialized) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("init decoder IV");
            }
            byte[] iv = new byte[cipher.getIVLength()];
            in.readBytes(iv);
            worker.init(false, key.getEncoded(), iv);
            initialized = true;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("decode payload");
        }
        byte[] encoded = new byte[in.readableBytes()];
        in.readBytes(encoded);
        byte[] plain = worker.process(encoded);
        out.add(Unpooled.copiedBuffer(plain));
    }
}
