package cc.xpcas.nettysocks.upstream;

import io.netty.channel.socket.SocketChannel;

/**
 * @author xp
 */
public class DirectUpstream extends Upstream<SocketChannel> {

    public DirectUpstream() {
    }

    @Override
    public void initChannel(SocketChannel channel) {
        // do nth.
    }
}
