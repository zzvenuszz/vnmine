package com.vnmine.cultivation;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActiveMeditationSession {

    private final UUID playerId;
    private final UUID armorStandId;
    private final long startTicks;
    private final Location sitLocation;
    private final Location originalLocation; // Vị trí đứng ban đầu (trước khi ngồi)
    private int ticksSinceLastExp;
    private final List<UUID> displayItemIds = new ArrayList<>();
    private float rotationAngle = 0f; // Góc xoay cho hiệu ứng

    public ActiveMeditationSession(UUID playerId, UUID armorStandId, Location sitLocation, Location originalLocation) {
        this.playerId = playerId;
        this.armorStandId = armorStandId;
        this.sitLocation = sitLocation.clone();
        this.originalLocation = originalLocation.clone();
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

    public Location getOriginalLocation() {
        return originalLocation;
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

    public List<UUID> getDisplayItemIds() {
        return displayItemIds;
    }

    public void addDisplayItemId(UUID id) {
        displayItemIds.add(id);
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(float angle) {
        this.rotationAngle = angle;
    }

    public void incrementRotationAngle(float delta) {
        this.rotationAngle += delta;
    }

    private long getCurrentTicks() {
        return org.bukkit.Bukkit.getWorlds().get(0).getFullTime();
    }
}
