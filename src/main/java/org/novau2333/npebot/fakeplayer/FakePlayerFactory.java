package org.novau2333.npebot.fakeplayer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.ClientCommand;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class FakePlayerFactory {
    private static final Logger logger = LogManager.getLogger();
    public static boolean loginPluginEnabled = true;
    public static String password;
    public static JSONArray whenLoginToExecute;
    public static final ConcurrentMap<Session, WorldInfo> sessionInfo = new ConcurrentHashMap<>();
    public static Queue<String> messageTask = new ConcurrentLinkedQueue<>();
    private static final ConcurrentMap<Session,Thread> autoRespawns = new ConcurrentHashMap<>();
    public static TcpClientSession getNewSession(InetSocketAddress address, String userName, String accessToken, ProxyInfo proxy,boolean enableProxy){
        if (enableProxy && proxy == null) {
            throw new NullPointerException("Proxy cannot be null!");
        }
        TcpClientSession session;
        if (accessToken != null && enableProxy) {
            session = new TcpClientSession(address.getHostName(),address.getPort(),new MinecraftProtocol(new GameProfile((String) null,userName),accessToken),proxy);
        } else if (accessToken != null && !enableProxy) {
            session = new TcpClientSession(address.getHostName(),address.getPort(),new MinecraftProtocol(new GameProfile((String) null,userName),accessToken));
        }else if (accessToken == null && enableProxy) {
            session = new TcpClientSession(address.getHostName(),address.getPort(),new MinecraftProtocol(userName),proxy);
        }else{
            session = new TcpClientSession(address.getHostName(),address.getPort(),new MinecraftProtocol(userName));
        }
        session.setReadTimeout(3000);
        final BotHandler botHandler = new BotHandler();
        session.setWriteTimeout(3000);
        session.addListener(new SessionListener() {
            @Override
            public void packetReceived(Session session, Packet packet) {
                if(packet instanceof ClientboundChatPacket){
                    ClientboundChatPacket chatPacket = (ClientboundChatPacket) packet;
                    Component message = chatPacket.getMessage();
                    String textComponent = GsonComponentSerializer.gson().serialize(message);
                    JSONObject jsonObject = JSON.parseObject(textComponent);
                    logger.info("[PacketListener][ChatPacket] {}",jsonObject.toString());
                }
                if (packet instanceof ClientboundLoginPacket){
                    logger.info("[MCChatBot] Logged in as entityId {}",((ClientboundLoginPacket) packet).getEntityId());
                    ClientboundLoginPacket loginPacket = (ClientboundLoginPacket) packet;
                    sessionInfo.put(session,new WorldInfo(loginPacket));
                    session.setFlag("logged",true);
                    Thread autoRespawner = new Thread(()->{
                        while (session.isConnected()){
                            try {
                                for (String message : messageTask){
                                    session.send(new ServerboundChatPacket(message));
                                    messageTask.remove(message);
                                }
                                session.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                                Thread.sleep(1000);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    },"AutoRespawner-"+Thread.currentThread().getId());
                    autoRespawner.setDaemon(true);
                    autoRespawner.start();
                    autoRespawns.put(session,autoRespawner);
                    botHandler.handleAuthmePlugin(session);
                }
            }
            @Override
            public void packetSending(PacketSendingEvent packetSendingEvent) {}
            @Override
            public void packetSent(Session session, Packet packet) {}
            @Override
            public void packetError(PacketErrorEvent packetErrorEvent) {}
            @Override
            public void connected(ConnectedEvent connectedEvent) {}
            @Override
            public void disconnecting(DisconnectingEvent disconnectingEvent) {}
            @Override
            public void disconnected(DisconnectedEvent disconnectedEvent) {
                botHandler.handleDisconnect(disconnectedEvent); //Handle disconnect
            }
        });
        return session;
    }

    private static final class BotHandler{
        public void handleDisconnect(DisconnectedEvent event){
            logger.info("[MCChatBot] Disconnected from server.Reason: {}",event.getReason());
            if (event.getCause()!=null){
                event.getCause().printStackTrace();
            }
            if (autoRespawns.get(event.getSession()).isAlive()){
                autoRespawns.get(event.getSession()).stop();
            }
            autoRespawns.remove(event.getSession());
            event.getSession().setFlag("logged",false);
        }

        public void handleAuthmePlugin(Session session){
            if (loginPluginEnabled) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Register the fake player
                session.send(new ServerboundChatPacket("/register "+password+" "+password));
                session.send(new ServerboundChatPacket("/l "+password));
            }
            for (Object message : whenLoginToExecute){
                session.send(new ServerboundChatPacket("/"+message));
            }
        }
    }

}
