package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author xp
 */
public class ChannelHandlerContextForwardingHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelHandlerContextForwardingHandler.class);

    private final ChannelHandlerContext dst;

    public ChannelHandlerContextForwardingHandler(ChannelHandlerContext dst) {
        this.dst = dst;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        dst.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        dst.channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(cause.getLocalizedMessage(), cause);
        closeOnFlush(ctx.channel());
    }

    private static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
