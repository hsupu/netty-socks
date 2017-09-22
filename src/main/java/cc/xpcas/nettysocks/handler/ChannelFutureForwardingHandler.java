package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * @author xp
 */
public class ChannelFutureForwardingHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelFutureForwardingHandler.class);

    private final ChannelFuture dst;

    public ChannelFutureForwardingHandler(ChannelFuture dst) {
        this.dst = dst;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        dst.channel().writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        dst.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(cause.getLocalizedMessage(), cause);
        closeAfterFlush(ctx.channel());
    }

    private static void closeAfterFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
