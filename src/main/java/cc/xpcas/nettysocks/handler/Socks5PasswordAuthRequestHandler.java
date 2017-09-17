package cc.xpcas.nettysocks.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import cc.xpcas.nettysocks.authenticator.Authenticator;

public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

	private final Authenticator authenticator;
	
	public Socks5PasswordAuthRequestHandler(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	protected void accepted(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        ConnectionManageHandler.setChannelUsername(ctx, msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
        ctx.writeAndFlush(response);
    }

    protected void rejected(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
        ConnectionManageHandler.setChannelUsername(ctx, "[rejected] " + msg.username());

        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
		if (authenticator.identify(msg.username(), msg.password())) {
		    accepted(ctx, msg);
		} else {
		    rejected(ctx, msg);
		}
	}
}
