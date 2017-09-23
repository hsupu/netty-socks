package cc.xpcas.nettysocks.proxy.ssocks;

import java.net.*;
import java.nio.channels.*;

import javax.crypto.SecretKey;

import cc.xpcas.nettysocks.proxy.ssocks.cipher.Cipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;

/**
 * @author xp
 */
public class SSocksConnectHandler extends ChannelDuplexHandler {

    private final Cipher cipher;

    private final SecretKey key;

    private final ChannelPromise promise;

    private final SocketAddress proxyAddress;

    private SocketAddress destinationAddress;

    private PendingWriteQueue pendingWrites;

    private boolean done = false;

    public SSocksConnectHandler(ChannelPromise promise, SocketAddress proxyAddress, Cipher cipher, String password) {
        this.promise = promise;
        this.proxyAddress = proxyAddress;
        this.cipher = cipher;
        this.key = SSocksSecretKey.of(cipher.getKeyLength(), password);
    }

    @SuppressWarnings("unchecked")
    public final <T extends SocketAddress> T proxyAddress() {
        return (T) proxyAddress;
    }

    @SuppressWarnings("unchecked")
    public final <T extends SocketAddress> T destinationAddress() {
        return (T) destinationAddress;
    }

    public final ChannelPromise connectFuture() {
        return promise;
    }

    public final boolean isConnected() {
        return connectFuture().isSuccess();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            doChannelActive(ctx);
        }
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (destinationAddress != null) {
            promise.setFailure(new ConnectionPendingException());
            return;
        }

        destinationAddress = remoteAddress;
        ctx.connect(proxyAddress, localAddress, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        doChannelActive(ctx);
        ctx.fireChannelActive();
    }

    private void doChannelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        String name = ctx.name();

        SSocksDecoder decoder = new SSocksDecoder(cipher, key);
        pipeline.addBefore(name, name + ".payload.decoder", decoder);

        SSocksEncoder encoder = new SSocksEncoder(cipher, key);
        pipeline.addBefore(name, name + ".payload.encoder", encoder);

        ByteBuf encodedTargetAddress = getEncodedTargetAddress(ctx.alloc(), false);
        ctx.writeAndFlush(encodedTargetAddress).addListener((ChannelFutureListener) future -> {
            pipeline.remove(SSocksConnectHandler.this);
            if (future.isSuccess()) {
                try {
                    doConnectSucceed(ctx);
                } catch (Exception e) {
                    doConnectFailed(ctx, e);
                }
            } else {
                doConnectFailed(ctx, future.cause());
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (done) {
            ctx.fireChannelInactive();
        } else {
            doConnectFailed(ctx, new ProxyConnectException("proxy disconnected"));
        }
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (done) {
            ctx.fireExceptionCaught(cause);
        } else {
            doConnectFailed(ctx, cause);
        }
    }

    protected void doConnectSucceed(ChannelHandlerContext ctx) throws Exception {
        done = true;
        connectFuture().setSuccess();
        flush(ctx);
    }

    protected void doConnectFailed(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        done = true;
        failPendingWrites(cause);
        connectFuture().setFailure(cause);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (done) {
            writePendingWrites();
            ctx.write(msg, promise);
        } else {
            addPendingWrite(ctx, msg, promise);
        }
    }

    @Override
    public final void flush(ChannelHandlerContext ctx) throws Exception {
        if (done) {
            writePendingWrites();
            ctx.flush();
        }
    }

    private void addPendingWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PendingWriteQueue pendingWrites = this.pendingWrites;
        if (pendingWrites == null) {
            this.pendingWrites = pendingWrites = new PendingWriteQueue(ctx);
        }
        pendingWrites.add(msg, promise);
    }

    private void writePendingWrites() {
        if (pendingWrites != null) {
            pendingWrites.removeAndWriteAll();
            pendingWrites = null;
        }
    }

    private void failPendingWrites(Throwable cause) {
        if (pendingWrites != null) {
            pendingWrites.removeAndFailAll(cause);
            pendingWrites = null;
        }
    }

    private ByteBuf getEncodedTargetAddress(ByteBufAllocator allocator, boolean resolve) throws ProxyConnectException {
        InetSocketAddress remoteAddress = destinationAddress();
        SocksAddressType remoteAddressType;
        String remoteHost;
        if (!resolve || remoteAddress.isUnresolved()) {
            remoteAddressType = SocksAddressType.DOMAIN;
            remoteHost = remoteAddress.getHostString();
        } else {
            remoteHost = remoteAddress.getAddress().getHostAddress();
            if (NetUtil.isValidIpV4Address(remoteHost)) {
                remoteAddressType = SocksAddressType.IPv4;
            } else if (NetUtil.isValidIpV6Address(remoteHost)) {
                remoteAddressType = SocksAddressType.IPv6;
            } else {
                throw new ProxyConnectException("unknown address type: " + StringUtil.simpleClassName(remoteHost));
            }
        }
        int remotePort = remoteAddress.getPort();
        SocksCmdRequest request = new SocksCmdRequest(SocksCmdType.UNKNOWN, remoteAddressType, remoteHost, remotePort);
        return SSocksAddressEncoder.INSTANCE.encode(allocator, request);
    }
}
