package main.java.org.novau2333.npebot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public class ProxyPool {
    public static final Set<String> proxys= ConcurrentHashMap.newKeySet();
    private static final Logger logger = LogManager.getLogger();

    public static void getProxysFromAPIs() {
        logger.info("正在使用API获取代理..");
        getProxysFromAPI("http://www.66ip.cn/mo.php?tqsl=9999");
        getProxysFromAPI("http://www.89ip.cn/tqdl.html?api=1&num=9999");
        logger.info("代理更新完成!数量:"+proxys.size());
    }

    public static void getProxysFromAPI(String url) {
        String ips = EndMinecraftUltraUtils.sendGet(url);
        Matcher matcher = EndMinecraftUltraUtils.matches(ips, "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\:\\d{1,5}");
        while (matcher.find()) {
            String ip = matcher.group();
            if (!proxys.contains(ip)) {
                proxys.add(ip);
            }
        }
    }

    public static void getProxysFromFile() {
        try {
            File file=new File("http.txt");
            if(!file.exists()) return;
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String tempString=null;
            while ((tempString=reader.readLine())!=null) {
                proxys.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("从本地读取代理完成!数量:"+proxys.size());
    }

    public static void runUpdateProxysTask(final long time) {
        new Thread(()-> {
            while(true) {
                try {
                    Thread.sleep(time * 1000);
                    proxys.clear();
                    getProxysFromAPIs();
                    getProxysFromFile();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
