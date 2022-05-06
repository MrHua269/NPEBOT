package org.novau2333.npebot;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class ConfigManager {
    private static final Executor consoleWriter = Executors.newCachedThreadPool();
    public static JSONObject fakePlayerConfig;
    private final static Logger logger = LogManager.getLogger();
    public static void init() throws IOException {
        File file = new File("botconfig.json");
        if(!file.exists()){
            logger.error("Config file didn't found.Using internal config and creating...");
            InputStream stream =  ConfigManager.class.getClassLoader().getResourceAsStream("botconfig.json");
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            //load default config
            fakePlayerConfig = JSONObject.parseObject(new String(buffer));
            stream.close();
            file.createNewFile();
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
