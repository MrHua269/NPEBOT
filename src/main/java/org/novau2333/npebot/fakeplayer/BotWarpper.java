package main.java.org.novau2333.npebot.fakeplayer;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import main.java.org.novau2333.npebot.ProxyPool;
import org.apache.logging.log4j.Logger;
import org.novau2333.npebot.fakeplayer.FakePlayerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.novau2333.npebot.fakeplayer.FakePlayerFactory.getNewSession;

public class BotWarpper {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger();
    private static String[] commands = new String[8];
    public static boolean isCommand(String input) {
        for (String command : commands) {
            if (input.equalsIgnoreCase(command)) {
                return true;
            }
        }
        return false;
    }
    public static void handleCommand(String input, AtomicReference<TcpClientSession> session){
        commands[0] = "fun";
        commands[1] = "stop";
        commands[2] = "exit";
        commands[3] = "serverinfo";
        AtomicReference<Thread> fun = new AtomicReference<>();
        if (input.equalsIgnoreCase("fun")){
            if (fun.get() !=null) {
                fun.get().stop();
            }
            fun.set(new Thread(() -> {
                while (true) {
                    for (int i = 0; i < 10; i++) {
                        ServerboundMovePlayerPosRotPacket packet = new ServerboundMovePlayerPosRotPacket(true, 0F, 0F, 0F, 0.2F, 0.3F);
                        session.get().send(packet);
                    }
                    try {
                        Thread.sleep(1000);
                        logger.info("[MCChatBot] Fun packets sent");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
            fun.get().start();
            return;
        }
        if (input.equalsIgnoreCase("serverinfo")){
            WorldInfo info = FakePlayerFactory.sessionInfo.get(session.get());
            info.printOutStatus(logger);
        }
        if (input.equalsIgnoreCase("stop")) {
            try {
                fun.get().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (input.equalsIgnoreCase("exit")){
            logger.info("[MCChatBot] Exiting...");
            System.exit(0);
        }
    }
    public static void runNewBot(InetSocketAddress address, String username, String accessToken,boolean enableProxty){
        AtomicReference<TcpClientSession> session = new AtomicReference<>();
        new Thread(() -> {
            while (true){
                ProxyPool.proxys.forEach(p -> {
                    try {
                        String[] _p = p.split(":");
                        ProxyInfo proxy = new ProxyInfo(ProxyInfo.Type.HTTP, new InetSocketAddress(_p[0], Integer.parseInt(_p[1])));
                        Proxy p1 = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_p[0], Integer.parseInt(_p[1])));
                        session.set(getNewSession(address, username, accessToken,proxy,enableProxty));
                        try {
                            MotdSender.send(address, true, p1, enableProxty);
                        } catch (Exception e) {
                            logger.error("[MCChatBot] Failed to send motd");
                        }
                        session.get().connect();
                        new Thread(()->{
                            Scanner scanner = new Scanner(System.in);
                            while (scanner.hasNextLine()){
                                String input = scanner.nextLine();
                                handleCommand(input,session);
                                if (isCommand( input)){
                                    continue;
                                }
                                session.get().send(new ServerboundChatPacket(input));
                            }
                        },"Command-Handler").start();
                        Thread.sleep(10000);
                        while (session.get().isConnected())
                        {
                            Thread.sleep(10);
                        }
                        if(!org.novau2333.npebot.ConfigManager.fakePlayerConfig.getBoolean("autoreconnect")){
                            session.get().disconnect("Bot disconnected");
                            logger.info("[MCChatBot] Exiting...");
                            System.exit(0);
                        }
                        logger.info("[MCChatBot] Reconnecting");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
        },"MCChatBot-Thread").start();
    }
}
