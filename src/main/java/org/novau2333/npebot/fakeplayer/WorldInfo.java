package main.java.org.novau2333.npebot.fakeplayer;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class WorldInfo {
    private final int entityId;
    private final String worldName;
    private final int worldCount;
    private final String[] worldList;
    private final boolean isHardcore;
    private final int maxPlayers;
    private final int viewDistance;
    private final GameMode gamemode;
    private final int simulationDistance;
    public WorldInfo(ClientboundLoginPacket packet) {
        this.entityId = packet.getEntityId();
        this.worldName = packet.getWorldName();
        this.worldCount = packet.getWorldCount();
        this.worldList = packet.getWorldNames();
        this.isHardcore = packet.isHardcore();
        this.maxPlayers = packet.getMaxPlayers();
        this.viewDistance = packet.getViewDistance();
        this.gamemode = packet.getGameMode();
        this.simulationDistance = packet.getSimulationDistance();
    }
    public void printOutStatus(Logger logger) {
        String build = "\n==========Server Info ==========\n" +
                "Entity ID: " + this.entityId + "\n" +
                "World Name: " + this.worldName + "\n" +
                "World Count: " + this.worldCount + "\n" +
                "World List: " + Arrays.toString(this.worldList) + "\n" +
                "Hardcore: " + this.isHardcore + "\n" +
                "Max Players: " + this.maxPlayers + "\n" +
                "View Distance: " + this.viewDistance + "\n" +
                "Game Mode: " + this.gamemode + "\n" +
                "Simulation Distance: " + this.simulationDistance + "\n" +
                "====================================\n";
        logger.info(build);
    }
}
