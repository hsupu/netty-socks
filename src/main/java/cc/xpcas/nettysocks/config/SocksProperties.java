package cc.xpcas.nettysocks.config;

import java.util.*;

import lombok.Data;

/**
 * @author xp
 */
@Data
public class SocksProperties {

    private Address listen = new Address("127.0.0.1", 6000);

    private Address upstream = null;

    private boolean auth = false;

    private Map<String, String> authMap;

    private int acceptors = 4;

    private int backlog = 1024;

    private int connectTimeoutMillis = 3000;
}
