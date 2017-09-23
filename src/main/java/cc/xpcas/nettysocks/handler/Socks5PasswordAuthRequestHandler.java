package cc.xpcas.nettysocks.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import cc.xpcas.nettysocks.authenticator.Authenticator;

@ChannelHandler.Sharable
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Socks5PasswordAuthRequestHandler.class);

    private final Authenticator authenticator;

    public Socks5PasswordAuthRequestHandler(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Socks5PasswordAuthRequestDecoder.class.getName());
        pipeline.remove(this);

        if (authenticator.identify(msg.username(), msg.password())) {
            accepted(ctx, msg);
        } else {
            rejected(ctx, msg);
        }
    }

    private void accepted(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("accept " + msg.username());
        }

        ConnectionManageHandler.setChannelUsername(ctx, msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
        ctx.writeAndFlush(response);
    }

    private void rejected(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        if (LOG.isInfoEnabled()) {
            LOG.info("reject " + msg.username());
        }

        ConnectionManageHandler.setChannelUsername(ctx, "[rejected] " + msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
