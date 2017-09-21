package cc.xpcas.nettysocks.handler;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.config.Address;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.proxy.Socks5ProxyHandler;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    private final EventLoopGroup forwarders;

    private final Address upstream;

    public Socks5CommandRequestHandler(EventLoopGroup forwarders, Address upstream) {
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
                            ChannelPipeline pipeline = channel.pipeline();
                            if (upstream != null) {
                                SocketAddress address = new InetSocketAddress(upstream.getHost(), upstream.getPort());
                                pipeline.addFirst("proxy", new Socks5ProxyHandler(address));
                            }
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
