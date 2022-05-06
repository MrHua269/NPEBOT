package org.novau2333.npebot;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConfigManager {
    private static final Executor consoleWriter = Executors.newCachedThreadPool();
    public static JSONObject fakePlayerConfig;
    private final static Logger logger = LogManager.getLogger();
    public static void init() throws IOException {
        File file = new File("botconfig.json");
        if(!file.exists()){
            fakePlayerConfig = new JSONObject();
            fakePlayerConfig.put("id", "jlnpebot001");
            fakePlayerConfig.put("server", "124.221.233.18");
            fakePlayerConfig.put("port", 25565);
            fakePlayerConfig.put("authmemode", true);
            fakePlayerConfig.put("password", "123456AABB");
            fakePlayerConfig.put("autoreconnect",true);
            fakePlayerConfig.put("accessToken","nope");
            fakePlayerConfig.put("enableProxy",false);
            fakePlayerConfig.put("howToGetProxy","both");
            fakePlayerConfig.put("runProxyAutoUpdate",true);
            fakePlayerConfig.put("proxyUpdateInterval",10);
            consoleWriter.execute(()->{
                logger.info("Writing default config file...");
                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write(fakePlayerConfig.toJSONString());
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }else{
            logger.info("Loading config file...");
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();
            fakePlayerConfig = JSONObject.parseObject(new String(bytes));
        }
    }
}
