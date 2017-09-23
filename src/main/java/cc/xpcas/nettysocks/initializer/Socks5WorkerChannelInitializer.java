package cc.xpcas.nettysocks.initializer;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.xpcas.nettysocks.authenticator.BasicAuthenticator;
import cc.xpcas.nettysocks.config.SocksProperties;
import cc.xpcas.nettysocks.handler.*;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * @author xp
 */
public class Socks5WorkerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5WorkerChannelInitializer.class);

    private final SocksProperties socksProperties;

    private Socks5InitialRequestHandler socks5InitialRequestHandler;

    private Socks5PasswordAuthRequestHandler socks5PasswordAuthRequestHandler;

    private Socks5CommandRequestHandler socks5CommandRequestHandler;

    public Socks5WorkerChannelInitializer(SocksProperties socksProperties, EventLoopGroup forwarders) {
        this.socksProperties = socksProperties;

        // 先初始化 shared handlers
        socks5InitialRequestHandler = new Socks5InitialRequestHandler(socksProperties.isAuth());
        if (socksProperties.isAuth()) {
            BasicAuthenticator authenticator = new BasicAuthenticator();
            authenticator.batchSet(socksProperties.getAuthMap());
            socks5PasswordAuthRequestHandler = new Socks5PasswordAuthRequestHandler(authenticator);
        } else {
            socks5PasswordAuthRequestHandler = null;
        }
        socks5CommandRequestHandler = new Socks5CommandRequestHandler(forwarders, socksProperties.getUpstream());
    }

    @Override
    protected void initChannel(io.netty.channel.socket.SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        // 连接管理
        pipeline.addLast(ConnectionManageHandler.NAME, new ConnectionManageHandler(3000));

        // 空闲超时
        pipeline.addLast(new IdleStateHandler(10, 10, 0));
        pipeline.addLast(new IdleStateEventHandler());

        // 读写超时
        pipeline.addLast(new ReadTimeoutHandler(socksProperties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS));
        pipeline.addLast(new WriteTimeoutHandler(socksProperties.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS));

        // netty log
        //pipeline.addLast(new LoggingHandler());

        // 负责将输出的 Socks5Message 转为 ByteBuf
        pipeline.addLast(Socks5ServerEncoder.DEFAULT);

        // init
        pipeline.addLast(Socks5InitialRequestDecoder.class.getName(), new Socks5InitialRequestDecoder());
        pipeline.addLast(Socks5InitialRequestHandler.class.getName(), socks5InitialRequestHandler);

        // auth
        if (socks5PasswordAuthRequestHandler != null) {
            pipeline.addLast(Socks5PasswordAuthRequestDecoder.class.getName(), new Socks5PasswordAuthRequestDecoder());
            pipeline.addLast(Socks5PasswordAuthRequestHandler.class.getName(), socks5PasswordAuthRequestHandler);
        }

        // connection
        pipeline.addLast(Socks5CommandRequestDecoder.class.getName(), new Socks5CommandRequestDecoder());
        pipeline.addLast(Socks5CommandRequestHandler.class.getName(), socks5CommandRequestHandler);
    }
}
