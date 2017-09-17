package cc.xpcas.nettysocks.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author xp
 */
public class ChannelFutureForwardingHandler extends ChannelInboundHandlerAdapter {

    private final ChannelFuture dst;

    public ChannelFutureForwardingHandler(ChannelFuture dst) {
        this.dst = dst;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        dst.channel().writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        dst.addListener(ChannelFutureListener.CLOSE);
    }
}
