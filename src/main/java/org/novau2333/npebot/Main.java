package org.novau2333.npebot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novau2333.npebot.fakeplayer.BotWrapper;
import org.novau2333.npebot.fakeplayer.FakePlayerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("NPEBOT v0.1.0. Written by Novau2333(Star_Stream)");
        org.novau2333.npebot.ConfigManager.init();
        boolean enableProxy = ConfigManager.fakePlayerConfig.getBoolean("enableProxy");
        boolean runProxyAutoUpdate = ConfigManager.fakePlayerConfig.getBoolean("runProxyAutoUpdate");
        long proxyUpdateInterval = ConfigManager.fakePlayerConfig.getLong("proxyUpdateInterval") * 60;
        String howToGetProxy = ConfigManager.fakePlayerConfig.getString("howToGetProxy");
        FakePlayerFactory.whenLoginToExecute =  ConfigManager.fakePlayerConfig.getJSONArray("executeCommandWhenJoined");
        if(enableProxy) {
            logger.warn("Proxy is enabled!");
            switch (howToGetProxy) {
                case "file":
                    ProxyPool.getProxyFromFile();
                    break;
                case "api":
                    ProxyPool.getProxyFromAPIIs();
                    break;
                case "both":
                    ProxyPool.getProxyFromFile();
                    ProxyPool.getProxyFromAPIIs();
                    break;
            }
            if (runProxyAutoUpdate) {
                ProxyPool.runUpdateProxyTask(proxyUpdateInterval);
            }
        }
        InetSocketAddress address = new InetSocketAddress(ConfigManager.fakePlayerConfig.getString("server"), ConfigManager.fakePlayerConfig.getInteger("port"));
        FakePlayerFactory.password = ConfigManager.fakePlayerConfig.getString("password");
        String accessToken = ConfigManager.fakePlayerConfig.getString("accessToken");
        if (accessToken.equals("nope")) {
            logger.warn("Access token is not set. Will using offline mode.");
            accessToken = null;
        }
        FakePlayerFactory.loginPluginEnabled = ConfigManager.fakePlayerConfig.getBoolean("authmemode");
        BotWrapper.runNewBot(address,ConfigManager.fakePlayerConfig.getString("id"),accessToken,enableProxy);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
