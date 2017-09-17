package cc.xpcas.nettysocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.authenticator.BasicAuthenticator;
import cc.xpcas.nettysocks.config.Address;
import cc.xpcas.nettysocks.config.SocksProperties;
import cc.xpcas.nettysocks.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author xp
 */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    private final SocksProperties socksProperties;

    public ServerMain(final SocksProperties socksProperties) {
        this.socksProperties = socksProperties;
    }

    public void start() throws InterruptedException {
        EventLoopGroup acceptors = new NioEventLoopGroup(socksProperties.getAcceptors());
        EventLoopGroup workers = new NioEventLoopGroup();
        EventLoopGroup forwarders = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(acceptors, workers)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, socksProperties.getBacklog())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, socksProperties.getConnectTimeoutMillis())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {

                            // 连接管理
                            channel.pipeline().addLast(ConnectionManageHandler.NAME, new ConnectionManageHandler(3000));

                            // 超时中断
                            channel.pipeline().addLast(new IdleStateHandler(3, 30, 0));
                            channel.pipeline().addLast(new IdleStateEventHandler());

                            // netty log
                            //channel.pipeline().addLast(new LoggingHandler());

                            // 负责将输出的 Socks5Message 转为 ByteBuf
                            channel.pipeline().addLast(Socks5ServerEncoder.DEFAULT);

                            // init
                            channel.pipeline().addLast(new Socks5InitialRequestDecoder());
                            channel.pipeline().addLast(new Socks5InitialRequestHandler(socksProperties.isAuth()));

                            if(socksProperties.isAuth()) {
                                // auth
                                BasicAuthenticator authenticator = new BasicAuthenticator();
                                authenticator.batchSet(socksProperties.getAuthMap());

                                channel.pipeline().addLast(new Socks5PasswordAuthRequestDecoder());
                                channel.pipeline().addLast(new Socks5PasswordAuthRequestHandler(authenticator));
                            }

                            // connection
                            channel.pipeline().addLast(new Socks5CommandRequestDecoder());
                            channel.pipeline().addLast(new Socks5CommandRequestHandler(forwarders, socksProperties.getUpstream()));
                        }
                    });

            Address address = socksProperties.getListen();
            ChannelFuture future = bootstrap.bind(address.getHost(), address.getPort()).sync();
            future.channel().closeFuture().sync();
        } finally {
            forwarders.shutdownGracefully();
            workers.shutdownGracefully();
            acceptors.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        SocksProperties socksProperties = new SocksProperties();

        socksProperties.setListen(new Address("127.0.0.1", 6000));
        socksProperties.setUpstream(new Address("127.0.0.1", 1080));

//        Map<String, String> authMap = new HashMap<>();
//        authMap.put("username", "password");
//
//        socksProperties.setAuth(true);
//        socksProperties.setAuthMap(authMap);

        socksProperties.setAuth(false);

        ServerMain server = new ServerMain(socksProperties);
        server.start();
    }
}
