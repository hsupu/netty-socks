package cc.xpcas.nettysocks.upstream;

import java.net.*;

import cc.xpcas.nettysocks.config.Address;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.proxy.Socks5ProxyHandler;

/**
 * @author xp
 */
public class Socks5Upstream extends Upstream<SocketChannel> {

    public Socks5Upstream(final Address address) {
        setAddress(address);
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        Address upstreamAddress = getAddress();
        SocketAddress address = new InetSocketAddress(upstreamAddress.getHost(), upstreamAddress.getPort());
        pipeline.addFirst(HANDLER_NAME, new Socks5ProxyHandler(address));
    }
}
