package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.*;

@ChannelHandler.Sharable
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5InitialRequestHandler.class);

    private final boolean auth;

    private final Socks5AuthMethod authMethod;

    public Socks5InitialRequestHandler(boolean auth) {
        this.auth = auth;
        authMethod = auth ? Socks5AuthMethod.PASSWORD : Socks5AuthMethod.NO_AUTH;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        if (msg.decoderResult().isFailure()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("decode failed");
            }
            ctx.fireChannelRead(msg);
        } else {
            if (msg.version().equals(SocksVersion.SOCKS5)) {
                Socks5InitialResponse response = new DefaultSocks5InitialResponse(authMethod);
                ctx.writeAndFlush(response);

            } else {
                if (LOGGER.isInfoEnabled()) {
                    SocksVersion version = msg.version();
                    LOGGER.info(String.format("unsupported version: %s(%d)", version.name(), version.byteValue()));
                }
                ctx.fireChannelRead(msg);
            }
        }
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Socks5InitialRequestDecoder.class.getName());
        pipeline.remove(Socks5InitialRequestHandler.class.getName());
    }
}
