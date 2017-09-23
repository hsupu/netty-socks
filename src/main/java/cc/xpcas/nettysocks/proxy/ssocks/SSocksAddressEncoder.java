package cc.xpcas.nettysocks.proxy.ssocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.socks.SocksCmdRequest;

/**
 * @author xp
 * <p>
 * SOCKS request 格式
 * <p>
 * +-----+-----+-------+------+----------+----------+
 * | VER | CMD | RSV   | ATYP | DST.ADDR | DST.PORT |
 * +-----+-----+-------+------+----------+----------+
 * | 1   | 1   | X'00' | 1    | Variable | 2        |
 * +-----+-----+-------+------+----------+----------+
 * <p>
 * Shadowsocks 的地址采用 SOCKS 地址格式, 即 SOCKS request 中跳过前 3 字节的余下部分
 */
public class SSocksAddressEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(SSocksAddressEncoder.class);

    public static final SSocksAddressEncoder INSTANCE = new SSocksAddressEncoder();

    public ByteBuf encode(ByteBufAllocator allocator, SocksCmdRequest msg) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("encode target address");
        }

        ByteBuf buf = allocator.directBuffer();
        msg.encodeAsByteBuf(buf);
        buf.skipBytes(3);

        if (LOG.isTraceEnabled()) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
        }

        return buf;
    }
}
