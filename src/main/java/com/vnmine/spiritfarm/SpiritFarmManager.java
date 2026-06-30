package com.vnmine.spiritfarm;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpiritFarmManager - Quản lý toàn bộ hệ thống Linh Điền
 * Lưu dữ liệu vào file spiritfarm_data.yml
 * Chạy particle task và growth check định kỳ
 */
public class SpiritFarmManager {

    private final VNMinePlugin plugin;
    private SpiritFarmConfig config;
    private final Map<UUID, List<SpiritFarmBlock>> farmBlocks = new ConcurrentHashMap<>();
    private int taskId = -1;
    private File dataFile;
    private YamlConfiguration dataConfig;

    public SpiritFarmManager(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.config = new SpiritFarmConfig(plugin);
        config.load();

        // Load dữ liệu
        loadData();

        // Chạy task cập nhật particles & growth (mỗi 20 ticks = 1 giây)
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            tick();
        }, 20L, 20L);

        plugin.getLogger().info("[SpiritFarm] Hệ thống Linh Điền đã khởi tạo!");
        plugin.getLogger().info("[SpiritFarm] Đã load " + SpiritHerb.getAllHerbs().size() + " loại linh thảo");
    }

    /**
     * Tạo item block linh điền để đặt
     */
    public ItemStack createFarmBlockItem(int grade) {
        String[] gradeNames = {
            "Hoàng Hạ", "Hoàng Trung", "Hoàng Thượng",
            "Huyền Hạ", "Huyền Trung", "Huyền Thượng",
            "Địa Hạ", "Địa Trung", "Địa Thượng",
            "Thiên Hạ", "Thiên Trung", "Thiên Thượng"
        };
        String[] gradeColors = {"&7", "&7", "&7", "&b", "&b", "&b", "&5", "&5", "&5", "&6", "&6", "&6"};

        int materialIndex = grade / 4; // 0=stone, 1=iron, 2=diamond, 3=emerald
        Material mat;
        switch (materialIndex) {
            case 0: mat = Material.STONE; break;
            case 1: mat = Material.IRON_BLOCK; break;
            case 2: mat = Material.DIAMOND_BLOCK; break;
            default: mat = Material.EMERALD_BLOCK; break;
        }

        return new ItemBuilder(mat)
                .setName(gradeColors[grade] + "&lLinh Điền " + gradeNames[grade])
                .setLore("", "&7Phẩm cấp: " + gradeNames[grade], "",
                        "&7Đặt xuống đất để tạo linh điền!",
                        "", "&eClick phải để đặt")
                .setGlow(true)
                .setPersistentData("vnmine_farm_block", String.valueOf(grade))
                .build();
    }

    /**
     * Tạo item hạt giống (sử dụng SpiritHerb mới)
     */
    public ItemStack createSeedItem(String herbId, int amount) {
        SpiritHerb herb = SpiritHerb.getHerb(herbId);
        if (herb == null) return null;
        return herb.createSeedItem(amount);
    }

    /**
     * Đặt block linh điền xuống thế giới
     */
    public boolean placeFarmBlock(Player player, Location location, int grade) {
        if (getFarmBlockAt(location) != null) {
            player.sendMessage("§cNơi này đã có linh điền!");
            return false;
        }

        Block block = location.getBlock();
        block.setType(Material.FARMLAND);

        SpiritFarmBlock farmBlock = new SpiritFarmBlock(location, grade);
        addFarmBlock(farmBlock);

        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                location.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5);

        saveData();
        return true;
    }

    /**
     * Gieo hạt vào linh điền
     */
    public boolean plantSeed(Player player, Location location, String herbId) {
        SpiritFarmBlock farmBlock = getFarmBlockAt(location);
        if (farmBlock == null) {
            player.sendMessage("§cĐây không phải linh điền!");
            return false;
        }
        if (farmBlock.hasHerb()) {
            player.sendMessage("§cLinh điền này đã có cây trồng!");
            return false;
        }

        SpiritHerb herb = SpiritHerb.getHerb(herbId);
        if (herb == null) {
            player.sendMessage("§cLoại linh thảo không hợp lệ!");
            return false;
        }

        farmBlock.setHerbId(herbId);
        farmBlock.setPlantTime(System.currentTimeMillis());
        farmBlock.setCurrentStage(0);

        // Đặt block cây trồng theo loại linh thảo
        location.getBlock().setType(herb.getMaterial());

        player.sendMessage("§a✦ Đã gieo hạt vào linh điền!");
        saveData();
        return true;
    }

    /**
     * Thu hoạch linh điền
     */
    public boolean harvest(Player player, Location location) {
        SpiritFarmBlock farmBlock = getFarmBlockAt(location);
        if (farmBlock == null) {
            player.sendMessage("§cĐây không phải linh điền!");
            return false;
        }
        if (!farmBlock.hasHerb()) {
            player.sendMessage("§cLinh điền này chưa được gieo hạt!");
            return false;
        }

        SpiritFarmConfig.FarmBlockDef blockDef = config.getBlockDef(farmBlock.getGrade());
        double growthMultiplier = (blockDef != null) ? blockDef.growthMultiplier : 1.0;
        int stage = farmBlock.calculateStage(config.getBaseGrowthTime(), growthMultiplier);
        SpiritHerb herb = SpiritHerb.getHerb(farmBlock.getHerbId());
        if (herb == null) {
            player.sendMessage("§cLỗi: Không tìm thấy loại linh thảo!");
            return false;
        }

        int maxStage = herb.getTotalStages() - 1;
        if (stage < maxStage) {
            player.sendMessage("§eCây chưa trưởng thành! Giai đoạn: " + (stage + 1) + "/" + herb.getTotalStages());
            return false;
        }

        // Tính chất lượng thu hoạch dựa trên phẩm cấp linh điền (grade)
        SpiritHerb.HerbQuality quality;
        int grade = farmBlock.getGrade();
        if (grade >= 9) quality = SpiritHerb.HerbQuality.NGAN_NAM;      // Thiên cấp
        else if (grade >= 6) quality = SpiritHerb.HerbQuality.TRAM_NAM; // Địa cấp
        else if (grade >= 3) quality = SpiritHerb.HerbQuality.MUOI_NAM; // Huyền cấp
        else quality = SpiritHerb.HerbQuality.TRUONG_THANH;             // Hoàng cấp

        int yield = (blockDef != null) ? blockDef.maxYield : 1;
        int count = 1 + new Random().nextInt(yield);

        // Tạo item linh thảo bằng phương thức mới
        ItemStack harvest = herb.createHerbItem(quality, count);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(harvest);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }

        // Reset linh điền
        farmBlock.setHerbId(null);
        farmBlock.setPlantTime(0);
        farmBlock.setCurrentStage(0);
        location.getBlock().setType(Material.FARMLAND);

        player.sendMessage("§a✦ Thu hoạch thành công! Nhận &e" + count + " &a" + herb.getName()
                + " &7(" + quality.getDisplay() + "&7)");
        saveData();
        return true;
    }

    /**
     * Lấy SpiritFarmBlock tại vị trí
     */
    public SpiritFarmBlock getFarmBlockAt(Location location) {
        UUID worldUID = location.getWorld().getUID();
        List<SpiritFarmBlock> blocks = farmBlocks.get(worldUID);
        if (blocks == null) return null;
        int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();
        for (SpiritFarmBlock fb : blocks) {
            if (fb.getX() == bx && fb.getY() == by && fb.getZ() == bz) {
                return fb;
            }
        }
        return null;
    }

    private void addFarmBlock(SpiritFarmBlock fb) {
        farmBlocks.computeIfAbsent(fb.getWorldUID(), k -> new ArrayList<>()).add(fb);
    }

    /**
     * Task chạy mỗi giây: cập nhật particles và growth
     */
    private void tick() {
        for (Map.Entry<UUID, List<SpiritFarmBlock>> entry : farmBlocks.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world == null) continue;

            for (SpiritFarmBlock fb : entry.getValue()) {
                Location loc = fb.getLocation(world);
                Location center = loc.add(0.5, 1.0, 0.5);

                if (fb.hasHerb()) {
                    SpiritFarmConfig.FarmBlockDef blockDef = config.getBlockDef(fb.getGrade());
                    double growthMultiplier = (blockDef != null) ? blockDef.growthMultiplier : 1.0;
                    int stage = fb.calculateStage(config.getBaseGrowthTime(), growthMultiplier);

                    // Particle màu sắc theo giai đoạn
                    Particle particle;
                    switch (stage) {
                        case 1: particle = Particle.END_ROD; break;
                        case 2: particle = Particle.WAX_ON; break;
                        case 3: particle = Particle.TRIAL_SPAWNER_DETECTION; break;
                        case 4: particle = Particle.FLAME; break;
                        default: particle = Particle.END_ROD; break;
                    }
                    world.spawnParticle(particle, center, 3, 0.2, 0.2, 0.2, 0.01);

                    // Cập nhật giai đoạn cây trồng
                    if (stage != fb.getCurrentStage()) {
                        fb.setCurrentStage(stage);
                        updateCropBlock(loc, stage, fb.getHerbId());
                    }
                } else {
                    world.spawnParticle(Particle.END_ROD, center, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }
        }
    }

    /**
     * Cập nhật block cây trồng theo giai đoạn
     */
    private void updateCropBlock(Location location, int stage, String herbId) {
        Block block = location.getBlock();
        SpiritHerb herb = SpiritHerb.getHerb(herbId);
        if (herb == null) {
            block.setType(Material.WHEAT);
            return;
        }

        if (stage >= herb.getTotalStages() - 1) {
            // Sẵn sàng thu hoạch
            block.setType(Material.SUNFLOWER);
        } else {
            // Các giai đoạn tăng trưởng
            block.setType(herb.getMaterial());
        }
    }

    // ==================== DATA SAVE/LOAD ====================

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "spiritfarm_data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[SpiritFarm] Không thể tạo file spiritfarm_data.yml!");
                return;
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        farmBlocks.clear();

        for (String worldKey : dataConfig.getKeys(false)) {
            UUID worldUID;
            try {
                worldUID = UUID.fromString(worldKey);
            } catch (IllegalArgumentException e) {
                continue;
            }

            List<SpiritFarmBlock> blocks = new ArrayList<>();
            for (String blockKey : dataConfig.getConfigurationSection(worldKey).getKeys(false)) {
                String path = worldKey + "." + blockKey;
                int x = dataConfig.getInt(path + ".x");
                int y = dataConfig.getInt(path + ".y");
                int z = dataConfig.getInt(path + ".z");
                int grade = dataConfig.getInt(path + ".grade", 0);
                String herbId = dataConfig.getString(path + ".herb", null);
                long plantTime = dataConfig.getLong(path + ".plantTime", 0);
                int stage = dataConfig.getInt(path + ".stage", 0);
                blocks.add(new SpiritFarmBlock(worldUID, x, y, z, grade, herbId, plantTime, stage));
            }
            if (!blocks.isEmpty()) {
                farmBlocks.put(worldUID, blocks);
            }
        }

        plugin.getLogger().info("[SpiritFarm] Đã load " + countTotalBlocks() + " linh điền từ dữ liệu");
    }

    public void saveData() {
        if (dataConfig == null) return;
        dataConfig.getKeys(false).forEach(key -> dataConfig.set(key, null));

        for (Map.Entry<UUID, List<SpiritFarmBlock>> entry : farmBlocks.entrySet()) {
            String worldKey = entry.getKey().toString();
            List<SpiritFarmBlock> blocks = entry.getValue();
            for (int i = 0; i < blocks.size(); i++) {
                SpiritFarmBlock fb = blocks.get(i);
                String path = worldKey + "." + i;
                dataConfig.set(path + ".x", fb.getX());
                dataConfig.set(path + ".y", fb.getY());
                dataConfig.set(path + ".z", fb.getZ());
                dataConfig.set(path + ".grade", fb.getGrade());
                dataConfig.set(path + ".herb", fb.getHerbId());
                dataConfig.set(path + ".plantTime", fb.getPlantTime());
                dataConfig.set(path + ".stage", fb.getCurrentStage());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[SpiritFarm] Không thể lưu dữ liệu!");
        }
    }

    private int countTotalBlocks() {
        int count = 0;
        for (List<SpiritFarmBlock> blocks : farmBlocks.values()) {
            count += blocks.size();
        }
        return count;
    }

    public void shutdown() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        saveData();
        plugin.getLogger().info("[SpiritFarm] Đã tắt hệ thống Linh Điền");
    }

    public SpiritFarmConfig getConfig() { return config; }
    public Map<String, SpiritHerb> getHerbTypes() { return SpiritHerb.getAllHerbs(); }
}