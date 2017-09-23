package cc.xpcas.nettysocks.handler;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.utils.NettyUtils;
import io.netty.channel.ChannelDuplexHandler;
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
        if (LOG.isInfoEnabled()) {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            LOG.info(remoteAddress + " read timeout");
        }
        NettyUtils.closeAfterFlush(ctx);
    }

    private void handleWriteTimeout(ChannelHandlerContext ctx) {
        if (LOG.isInfoEnabled()) {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            LOG.info(remoteAddress + " write timeout");
        }
        NettyUtils.closeAfterFlush(ctx);
    }
}
