package cc.xpcas.nettysocks.proxy.ssocks.cipher;

/**
 * @author xp
 */
public abstract class AbstractCipher implements Cipher {

    private final String name;

    public AbstractCipher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
