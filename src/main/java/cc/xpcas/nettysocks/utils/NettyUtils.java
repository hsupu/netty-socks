package cc.xpcas.nettysocks.utils;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * @author xp
 */
public class NettyUtils {

    private static final Logger LOG = LoggerFactory.getLogger("debug");

    public static void closeAfterFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void closeAfterFlush(ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void readIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }

    public static void dumpPipeline(String name, Channel channel) {
        if (LOG.isTraceEnabled()) {
            ChannelPipeline pipeline = channel.pipeline();
            List<ChannelInboundHandler> inboundHandlers = new LinkedList<>();
            List<ChannelOutboundHandler> outboundHandlers = new LinkedList<>();
            LOG.trace(name + " list:");
            for (Map.Entry<String, ChannelHandler> entry : pipeline) {
                String prefix;
                ChannelHandler handler = entry.getValue();
                if (handler instanceof ChannelInboundHandler) {
                    prefix = "in";
                    inboundHandlers.add((ChannelInboundHandler) handler);
                } else if (handler instanceof ChannelOutboundHandler) {
                    prefix = "out";
                    outboundHandlers.add((ChannelOutboundHandler) handler);
                } else {
                    prefix = "?";
                    LOG.trace(String.format("%s %s: %s", prefix, entry.getKey(), entry.getValue()));
                }
            }
            LOG.trace(name + " sorted:");
            for (ChannelInboundHandler handler : inboundHandlers) {
                LOG.trace(String.format("in %s", handler));
            }
            for (ChannelOutboundHandler handler : outboundHandlers) {
                LOG.trace(String.format("out %s", handler));
            }
        }
    }
}
