package net.biomodels.jummp.utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.logging.Logger;

public class ProxySetting {
    private static final Logger LOGGER = Logger.getLogger(String.valueOf(ProxySetting.class));
    public static Proxy detect() {
        LOGGER.info("Detecting Proxy...");
        String host = System.getenv().getOrDefault("https.proxyHost", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("https.proxyPort", String.valueOf(80)));
        boolean noSetProxyHost = host.equalsIgnoreCase("localhost") || host == null;
        boolean notSetProxyPort = port == 80 || port == 0;
        boolean noHttpProxy = noSetProxyHost && notSetProxyPort;
        Proxy proxy;
        if (noHttpProxy) {
            proxy = null;
        } else {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }
        return proxy;
    }
}
