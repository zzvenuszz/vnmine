package com.vnmine.spiritfarm;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * SpiritFarmBlock - Đại diện cho 1 block linh điền trong thế giới
 * Lưu tọa độ, loại hạt, thời gian bắt đầu, phẩm cấp
 */
public class SpiritFarmBlock {

    private final UUID worldUID;
    private final int x, y, z;
    private String herbId;       // ID hạt giống đang trồng (null nếu chưa trồng)
    private int grade;           // Phẩm cấp block (0-11)
    private long plantTime;      // Thời gian bắt đầu trồng (millis)
    private int currentStage;    // Giai đoạn hiện tại (0-4)

    public SpiritFarmBlock(Location location, int grade) {
        this.worldUID = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.grade = grade;
        this.herbId = null;
        this.plantTime = 0;
        this.currentStage = 0;
    }

    public SpiritFarmBlock(UUID worldUID, int x, int y, int z, int grade,
                          String herbId, long plantTime, int currentStage) {
        this.worldUID = worldUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.grade = grade;
        this.herbId = herbId;
        this.plantTime = plantTime;
        this.currentStage = currentStage;
    }

    public Location getLocation(World world) {
        return new Location(world, x, y, z);
    }

    public UUID getWorldUID() { return worldUID; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getGrade() { return grade; }
    public String getHerbId() { return herbId; }
    public long getPlantTime() { return plantTime; }
    public int getCurrentStage() { return currentStage; }

    public void setHerbId(String herbId) { this.herbId = herbId; }
    public void setPlantTime(long plantTime) { this.plantTime = plantTime; }
    public void setCurrentStage(int currentStage) { this.currentStage = currentStage; }
    public void setGrade(int grade) { this.grade = grade; }

    public boolean hasHerb() { return herbId != null && !herbId.isEmpty(); }

    /**
     * Tính giai đoạn hiện tại dựa trên thời gian đã trồng
     * @param baseGrowthTime Thời gian cơ bản cho 1 giai đoạn (giây)
     * @param growthMultiplier Hệ số tăng trưởng của block
     * @return Giai đoạn hiện tại (0-4)
     */
    public int calculateStage(long baseGrowthTime, double growthMultiplier) {
        if (!hasHerb()) return 0;
        long elapsed = System.currentTimeMillis() - plantTime;
        long stageTime = (long)(baseGrowthTime * 1000 / growthMultiplier);
        int stage = (int)(elapsed / stageTime);
        return Math.min(stage, 4);
    }

    /**
     * Kiểm tra xem đã đến giai đoạn thu hoạch chưa (giai đoạn 4)
     */
    public boolean isReadyToHarvest(long baseGrowthTime, double growthMultiplier) {
        return calculateStage(baseGrowthTime, growthMultiplier) >= 4;
    }

    @Override
    public String toString() {
        return String.format("SpiritFarmBlock{world=%s, x=%d, y=%d, z=%d, grade=%d, herb=%s, stage=%d}",
                worldUID, x, y, z, grade, herbId, currentStage);
    }
}