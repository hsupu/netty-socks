package cc.xpcas.nettysocks.authenticator;

import java.nio.charset.*;
import java.util.*;

import cc.xpcas.nettysocks.utils.DigestUtils;

/**
 * @author xp
 */
public class BasicAuthenticator implements Authenticator {

    private Map<String, String> authMap = new HashMap<>();

    public boolean has(String key) {
        return authMap.containsKey(key);
    }

    private static String encodeValue(String value) {
        return DigestUtils.md5AndHex(value.getBytes(StandardCharsets.UTF_8));
    }

    public void set(String key, String value) {
        authMap.put(key, encodeValue(value));
    }

    public void batchSet(Map<String, String> batchAuthMap) {
        batchAuthMap.forEach(this::set);
    }

    public boolean remove(String key, Object value) {
        return authMap.remove(key, value);
    }

    public void clear() {
        authMap.clear();
    }

    @Override
    public boolean identify(String username, String password) {
        String actualPassword = authMap.get(username);
        return actualPassword != null && actualPassword.equals(encodeValue(password));
    }
}
