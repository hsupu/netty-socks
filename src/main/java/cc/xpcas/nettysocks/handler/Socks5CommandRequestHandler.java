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

    private static final Logger logger = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private final EventLoopGroup forwarders;

    private final Upstream<SocketChannel> upstream;

    public Socks5CommandRequestHandler(EventLoopGroup forwarders, Upstream<SocketChannel> upstream) {
        this.forwarders = forwarders;
        this.upstream = upstream;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        logger.info("command " + msg.type() + " " + msg.dstAddr() + ":" + msg.dstPort());

        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(forwarders)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            if (upstream != null) {
                                upstream.initChannel(channel);
                            }
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("toLocal", new ChannelHandlerContextForwardingHandler(ctx));
                        }
                    });

            ChannelFuture forwarderFuture = bootstrap.connect(msg.dstAddr(), msg.dstPort());

            forwarderFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.pipeline().addLast("toRemote", new ChannelFutureForwardingHandler(future));

                    Socks5CommandResponse response = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    ctx.writeAndFlush(response);
                } else {
                    Socks5CommandResponse response = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    ctx.writeAndFlush(response);

                    future.channel().close();
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
