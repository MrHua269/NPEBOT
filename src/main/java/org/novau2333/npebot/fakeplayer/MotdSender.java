package org.novau2333.npebot.fakeplayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MotdSender {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void send(@NotNull InetSocketAddress address, boolean offMain, Proxy proxy,boolean enableProxy) {
       Runnable runnable = () -> {
           try {
               Socket socket;
               if (enableProxy && proxy != null) {
                   socket = new Socket(proxy);
               } else {
                   socket = new Socket();
               }
               socket.connect(address);
               if(socket.isConnected()) {
                   LOGGER.info("[BotPinger]Connected to " + address.getHostName() + ":" + address.getPort());
                   OutputStream out = socket.getOutputStream();
                   out.write(new byte[]{0x07, 0x00, 0x05, 0x01, 0x30, 0x63, (byte) 0xDD, 0x01});
                   out.flush();
                   for (int i = 0; i < 10; i++) {
                       out.write(new byte[]{0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00});
                   }
                   out.flush();
                   byte[] bytes = new byte[1024];
                   int len = socket.getInputStream().read(bytes);
                  //替换掉字符中的乱码
                   String str = new String(bytes, 0, len, StandardCharsets.UTF_8);
                   str = str.replaceAll("\u0000", "");
                   //替换掉�j�j
                   str = str.replaceAll("�j�j", "");
                   LOGGER.info("[BotPinger]Server response:"+str);
                   try {
                       out.close();
                       socket.close();
                   } catch (IOException ignored) {}
               }
           } catch (Exception e) {
               LOGGER.error("Error",e);
           }
       };
       if(offMain) {
           new Thread(runnable).start();
           return;
       }
       runnable.run();
    }
}
