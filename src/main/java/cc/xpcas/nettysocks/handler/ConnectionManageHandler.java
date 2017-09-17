package cc.xpcas.nettysocks.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xp
 */
public class ConnectionManageHandler extends ChannelTrafficShapingHandler {

    public static final String NAME = ConnectionManageHandler.class.getName();

    public static ConnectionManageHandler getFromHandlerContext(ChannelHandlerContext ctx) {
        return (ConnectionManageHandler) ctx.pipeline().get(NAME);
    }

    public static void setChannelUsername(ChannelHandlerContext ctx, String username) {
        getFromHandlerContext(ctx).setUsername(username);
    }

    @Getter
    @Setter
    private String username;

    public ConnectionManageHandler(long checkInterval) {
        super(checkInterval);
    }
}
