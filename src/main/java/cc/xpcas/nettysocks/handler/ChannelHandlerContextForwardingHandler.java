package cc.xpcas.nettysocks.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author xp
 */
public class ChannelHandlerContextForwardingHandler extends ChannelInboundHandlerAdapter {

    private final ChannelHandlerContext dst;

    public ChannelHandlerContextForwardingHandler(ChannelHandlerContext dst) {
        this.dst = dst;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        dst.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        dst.close();
    }
}
