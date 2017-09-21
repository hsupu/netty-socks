package cc.xpcas.nettysocks.handler;

import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import cc.xpcas.nettysocks.authenticator.Authenticator;

@ChannelHandler.Sharable
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

    private final Authenticator authenticator;

    public Socks5PasswordAuthRequestHandler(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
        if (authenticator.identify(msg.username(), msg.password())) {
            accepted(ctx, msg);
        } else {
            rejected(ctx, msg);
        }
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Socks5PasswordAuthRequestDecoder.class.getName());
        pipeline.remove(Socks5PasswordAuthRequestHandler.class.getName());
    }

    private void accepted(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        ConnectionManageHandler.setChannelUsername(ctx, msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
        ctx.writeAndFlush(response);
    }

    private void rejected(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        ConnectionManageHandler.setChannelUsername(ctx, "[rejected] " + msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
