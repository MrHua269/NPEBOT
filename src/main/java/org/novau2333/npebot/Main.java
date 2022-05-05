package main.java.org.novau2333.npebot;

import main.java.org.novau2333.npebot.fakeplayer.BotWarpper;
import main.java.org.novau2333.npebot.fakeplayer.MotdSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novau2333.npebot.ConfigManager;
import org.novau2333.npebot.fakeplayer.FakePlayerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) throws IOException, InterruptedException {
        org.novau2333.npebot.ConfigManager.init();
        InetSocketAddress address = new InetSocketAddress(ConfigManager.fakePlayerConfig.getString("server"), ConfigManager.fakePlayerConfig.getInteger("port"));
        MotdSender.send(address,true);
        FakePlayerFactory.password = ConfigManager.fakePlayerConfig.getString("password");
        String accessToken = ConfigManager.fakePlayerConfig.getString("accessToken");
        if (accessToken.equals("nope")) {
            logger.warn("Access token is not set. Will using offline mode.");
            accessToken = null;
        }

        FakePlayerFactory.loginPluginEnabled = ConfigManager.fakePlayerConfig.getBoolean("authmemode");
        BotWarpper.runNewBot(address,ConfigManager.fakePlayerConfig.getString("id"),accessToken);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
