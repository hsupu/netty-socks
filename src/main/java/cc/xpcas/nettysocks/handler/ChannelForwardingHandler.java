package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.utils.NettyUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * @author xp
 */
public class ChannelForwardingHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelForwardingHandler.class);

    private final Channel destinationChannel;

    private final boolean isReadLocalWriteRemote;

    public ChannelForwardingHandler(Channel destinationChannel, boolean isReadLocalWriteRemote) {
        this.destinationChannel = destinationChannel;
        this.isReadLocalWriteRemote = isReadLocalWriteRemote;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (LOG.isTraceEnabled()) {
            Channel sourceChannel = ctx.channel();
            if (isReadLocalWriteRemote) {
                LOG.trace(String.format("%s forwarded to %s -> %s",
                        sourceChannel.remoteAddress(),
                        destinationChannel.localAddress(), destinationChannel.remoteAddress()));
            } else {
                LOG.trace(String.format("%s -> %s forwarded to %s",
                        sourceChannel.remoteAddress(), sourceChannel.localAddress(),
                        destinationChannel.remoteAddress()));
            }
        }
        doWriteAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeAfterFlush();
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (LOG.isWarnEnabled()) {
            Channel channel = ctx.channel();
            LOG.warn(String.format("channel %s <-> %s thrown", channel.localAddress(), channel.remoteAddress()), cause);
        }
        closeAfterFlush();
    }

    protected ChannelFuture doWriteAndFlush(Object msg) {
        return destinationChannel.writeAndFlush(msg);
    }

    protected void closeAfterFlush() {
        NettyUtils.closeAfterFlush(destinationChannel);
    }
}
