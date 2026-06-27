package com.vnmine.cultivation;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import java.util.UUID;

public class ActiveMeditationSession {

    private final UUID playerId;
    private final UUID armorStandId;
    private final long startTicks;
    private final Location sitLocation;
    private int ticksSinceLastExp;

    public ActiveMeditationSession(UUID playerId, UUID armorStandId, Location sitLocation) {
        this.playerId = playerId;
        this.armorStandId = armorStandId;
        this.sitLocation = sitLocation.clone();
        this.startTicks = getCurrentTicks();
        this.ticksSinceLastExp = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getArmorStandId() {
        return armorStandId;
    }

    public Location getSitLocation() {
        return sitLocation;
    }

    public long getStartTicks() {
        return startTicks;
    }

    public int getTicksSinceLastExp() {
        return ticksSinceLastExp;
    }

    public void setTicksSinceLastExp(int ticks) {
        this.ticksSinceLastExp = ticks;
    }

    public void incrementTicksSinceLastExp() {
        this.ticksSinceLastExp++;
    }

    private long getCurrentTicks() {
        return org.bukkit.Bukkit.getWorlds().get(0).getFullTime();
    }
}
