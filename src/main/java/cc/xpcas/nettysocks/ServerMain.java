package cc.xpcas.nettysocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.config.Address;
import cc.xpcas.nettysocks.config.SocksProperties;
import cc.xpcas.nettysocks.initializer.Socks5WorkerChannelInitializer;
import cc.xpcas.nettysocks.proxy.ssocks.cipher.impl.AESCFBCipher;
import cc.xpcas.nettysocks.upstream.DirectUpstream;
import cc.xpcas.nettysocks.upstream.SSocksUpstream;
import cc.xpcas.nettysocks.upstream.Socks5Upstream;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
                    .childHandler(new Socks5WorkerChannelInitializer(socksProperties, forwarders));

            Address address = socksProperties.getListen();
            ChannelFuture future = bootstrap.bind(address.getHost(), address.getPort()).sync();
            future.channel().closeFuture().sync();
        } finally {
            forwarders.shutdownGracefully();
            workers.shutdownGracefully();
            acceptors.shutdownGracefully();
        }
    }

    private static void directMain(int port) throws InterruptedException {
        new Thread(() -> {
            try {
                SocksProperties socksProperties = new SocksProperties();

                socksProperties.setListen(new Address("127.0.0.1", port));
                socksProperties.setUpstream(new DirectUpstream());
                socksProperties.setAuth(false);

                ServerMain server = new ServerMain(socksProperties);
                server.start();
            } catch (InterruptedException e) {
                LOG.info(port + " interrupted");
            }
        }).start();
    }

    private static void socks5Main(int port) throws InterruptedException {
        new Thread(() -> {
            try {
                SocksProperties socksProperties = new SocksProperties();

                socksProperties.setListen(new Address("127.0.0.1", port));
                socksProperties.setUpstream(new Socks5Upstream(new Address("127.0.0.1", 1080)));
                socksProperties.setAuth(false);

                ServerMain server = new ServerMain(socksProperties);
                server.start();
            } catch (InterruptedException e) {
                LOG.info(port + " interrupted");
            }
        }).start();
    }

    private static void ssocksMain(int port) throws InterruptedException {
        new Thread(() -> {
            try {
                SocksProperties socksProperties = new SocksProperties();

                socksProperties.setListen(new Address("127.0.0.1", port));
                socksProperties.setUpstream(new SSocksUpstream(
                        new Address("::1", 8388),
                        AESCFBCipher.AES_256_CFB, "HelloWorld."
                ));
                socksProperties.setAuth(false);

                ServerMain server = new ServerMain(socksProperties);
                server.start();
            } catch (InterruptedException e) {
                LOG.info(port + " interrupted");
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
//        directMain(6000);
//        socks5Main(6001);
        ssocksMain(6002);
    }
}
