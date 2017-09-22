package cc.xpcas.nettysocks.upstream;

import cc.xpcas.nettysocks.config.Address;
import io.netty.channel.Channel;

/**
 * @author xp
 */
public abstract class Upstream<T extends Channel> {

    public static final String HANDLER_NAME = "proxy";

    private Address address;

    protected Upstream setAddress(Address address) {
        this.address = address;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public abstract void initChannel(T channel);
}
