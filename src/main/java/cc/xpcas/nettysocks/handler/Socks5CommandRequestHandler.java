package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.upstream.Upstream;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private final EventLoopGroup forwarders;

    private final Upstream<SocketChannel> upstream;

    public Socks5CommandRequestHandler(EventLoopGroup forwarders, Upstream<SocketChannel> upstream) {
        this.forwarders = forwarders;
        this.upstream = upstream;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Socks5CommandRequestDecoder.class.getName());
        pipeline.remove(this);

        if (LOG.isDebugEnabled()) {
            Channel channel = ctx.channel();
            LOG.debug(String.format("%s %s %s:%d",
                    channel.remoteAddress(),
                    msg.type(),
                    msg.dstAddr(), msg.dstPort()));
        }

        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            handleConnect(ctx, msg);
        } else {
            //TODO handle other command type
            ctx.close();
        }
    }

    private void handleConnect(final ChannelHandlerContext client, Socks5CommandRequest msg) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(forwarders)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        upstream.initChannel(channel);
                    }
                });

        ChannelFuture forwarderConnectFuture = bootstrap.connect(msg.dstAddr(), msg.dstPort());

        forwarderConnectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("upstream connected");
                }

                ChannelPipeline upstreamPipeline = future.channel().pipeline();
                upstreamPipeline.addLast("from-upstream", new ChannelHandlerContextForwardingHandler(client, false));

                ChannelPipeline clientPipeline = client.pipeline();
                clientPipeline.addLast("to-upstream", new ChannelForwardingHandler(future.channel(), true));

                client.writeAndFlush(socks5CommandResponse(msg, true));
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("upstream disconnected", future.cause());
                }

                client.writeAndFlush(socks5CommandResponse(msg, false)).addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private Socks5CommandResponse socks5CommandResponse(Socks5CommandRequest request, boolean success) {
        Socks5CommandStatus status = success ? Socks5CommandStatus.SUCCESS : Socks5CommandStatus.FAILURE;
        // bug: 不能使用 DOMAIN 会导致消息无法发出
        Socks5AddressType addressType = Socks5AddressType.IPv4;
        return new DefaultSocks5CommandResponse(status, addressType);
    }
}
