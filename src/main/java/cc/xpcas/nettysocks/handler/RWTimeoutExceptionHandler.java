package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;

/**
 * @author xp
 */
public class RWTimeoutExceptionHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RWTimeoutExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            handleReadTimeout(ctx);
        } else if (cause instanceof WriteTimeoutException) {
            handleWriteTimeout(ctx);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    private void handleReadTimeout(ChannelHandlerContext ctx) {
        LOG.info("read timeout");
        closeOnFlush(ctx.channel());
    }

    private void handleWriteTimeout(ChannelHandlerContext ctx) {
        LOG.info("write timeout");
        closeOnFlush(ctx.channel());
    }

    private static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
