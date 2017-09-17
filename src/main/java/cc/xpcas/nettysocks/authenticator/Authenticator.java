package cc.xpcas.nettysocks.authenticator;

/**
 * @author xp
 */
public interface Authenticator {

    boolean identify(String username, String password);
}
