package main.java.org.novau2333.npebot.fakeplayer;

import com.alibaba.fastjson.JSONObject;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPingPacket;
import com.github.steveice10.mc.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import com.github.steveice10.mc.protocol.packet.status.serverbound.ServerboundPingRequestPacket;
import com.github.steveice10.mc.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class MotdSender {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void send(@NotNull InetSocketAddress address,boolean offMain){
       Runnable runnable = () -> {
           try {
               Socket socket = new Socket();
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
                   String str = new String(bytes, 0, len, "UTF-8");
                   str = str.replaceAll("\u0000", "");
                   //替换掉�j�j
                   str = str.replaceAll("�j�j", "");
                   LOGGER.info("[BotPinger]Server response:"+str);
                   try {
                       out.close();
                       socket.close();
                   } catch (IOException e) {}
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
