package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.gui.MainMenuGUI;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

/**
 * SkillManager - Quản lý hệ thống công pháp / kỹ năng
 * Load từ skills.yml, tích hợp grade/quality/mastery/quickbar
 */
public class SkillManager implements Listener {

    private final VNMinePlugin plugin;
    private boolean enabled;
    private boolean masteryEnabled;
    private boolean quickbarEnabled;
    private int bookKeepOnFailChance;

    private final Map<String, SkillConfig> registeredSkills;
    private SkillBookListener bookListener;
    private QuickbarGUI quickbarGUI;
    private SkillCastListener castListener;

    public SkillManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.registeredSkills = new LinkedHashMap<>();
        loadConfig();
    }

    public SkillBookListener getBookListener() { return bookListener; }
    public QuickbarGUI getQuickbarGUI() { return quickbarGUI; }
    public SkillCastListener getCastListener() { return castListener; }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        registeredSkills.clear();

        enabled = config.getBoolean("enabled", true);

        // Book config
        ConfigurationSection booksSection = config.getConfigurationSection("books");
        if (booksSection != null) {
            bookKeepOnFailChance = booksSection.getInt("keep-on-fail-chance", 10);
        }

        // Mastery config
        ConfigurationSection masterySection = config.getConfigurationSection("mastery");
        if (masterySection != null) {
            masteryEnabled = masterySection.getBoolean("enabled", true);
        }

        // Quickbar config
        ConfigurationSection quickbarSection = config.getConfigurationSection("quickbar");
        if (quickbarSection != null) {
            quickbarEnabled = quickbarSection.getBoolean("enabled", true);
        }

        // Load skills
        ConfigurationSection skillsList = config.getConfigurationSection("skills.skills");
        if (skillsList == null) {
            plugin.getLogger().warning("No skills found in skills.yml!");
            return;
        }

        for (String skillId : skillsList.getKeys(false)) {
            ConfigurationSection skill = skillsList.getConfigurationSection(skillId);
            if (skill == null) continue;
            if (!skill.getBoolean("enabled", true)) continue;

            SkillGrade grade = SkillGrade.fromString(skill.getString("grade", "HOANG"));
            SkillQuality quality = SkillQuality.fromString(skill.getString("quality", "HA"));

            SkillConfig sc = new SkillConfig(
                    skillId,
                    skill.getString("name", "Unknown"),
                    skill.getStringList("description"),
                    skill.getString("type", "ACTIVE"),
                    skill.getInt("required-level", 1),
                    skill.getDouble("exp-cost", 0),
                    skill.getInt("mana-cost", 0),
                    skill.getInt("cooldown-seconds", 0),
                    skill.getString("icon", "STONE"),
                    grade,
                    quality,
                    skill.getConfigurationSection("effects")
            );
            registeredSkills.put(skillId, sc);
        }

        plugin.getLogger().info("Loaded " + registeredSkills.size() + " skills from skills.yml.");
    }

    public SkillConfig getSkill(String skillId) {
        return registeredSkills.get(skillId);
    }

    public int getBookKeepOnFailChance() { return bookKeepOnFailChance; }

    /**
     * Mở menu kỹ năng - chỉ hiển thị skill đã học
     */
    public void openSkillMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ Công Pháp & Kỹ Năng ✦"));

        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            MessageUtils.send(player, "&cHãy bắt đầu tu tiên trước! (/vn start)");
            return;
        }

        int learnedCount = getLearnedSkillCount(player);
        int level = data.getLevel();

        // Thông tin tổng quan
        gui.setItem(4, new ItemBuilder(Material.BOOK)
                .setGlow(true)
                .setName("&d&lCông Pháp Đã Học")
                .setLore(
                        "",
                        "&fĐã học: &e" + learnedCount + " &7/ " + registeredSkills.size(),
                        "&fCấp hiện tại: &e" + level,
                        "",
                        "&7Click vào skill để xem thông tin chi tiết",
                        "&7Dùng &e/skillbar &7để gán skill vào thanh nhanh"
                ).build());

        // Nút mở menu gán skill
        gui.setItem(8, new ItemBuilder(Material.NETHER_STAR)
                .setGlow(true)
                .setName("&b&l✦ Gán Kỹ Năng ✦")
                .setLore(
                        "",
                        "&7Click để mở menu gán skill",
                        "&7vào thanh dùng nhanh (hotbar)",
                        "",
                        "&b&lLệnh: &e/skillbar"
                ).build());

        // Chỉ hiển thị skill đã học
        int slot = 9;
        for (SkillConfig skill : registeredSkills.values()) {
            boolean learned = data.hasLearnedSkill(skill.id);
            if (!learned) continue; // Chỉ hiển thị skill đã học

            Material iconMat = Material.getMaterial(skill.icon.toUpperCase());
            if (iconMat == null) iconMat = Material.STONE;

            String gradeColor = skill.grade.getColor();

            List<String> lore = new ArrayList<>(skill.description);
            lore.add("");
            lore.add("&8Phẩm cấp: " + skill.grade.getName() + " &8- " + skill.quality.getName());
            lore.add("&8Loại: &e" + (skill.type.equals("ACTIVE") ? "Chủ động" : "Thụ động"));
            lore.add("&8Linh lực: &b" + skill.manaCost);
            lore.add("&8Hồi chiêu: &e" + skill.cooldownSeconds + "s");

            // Mastery info
            if (masteryEnabled) {
                SkillMastery mastery = data.getSkillData().getMasteryLevel(skill.id);
                int masteryExp = data.getSkillData().getMasteryExp(skill.id);
                double progressPct = SkillMastery.getProgressPercent(masteryExp, mastery);
                String barColor = skill.grade.getMasteryBarColor();

                lore.add("");
                lore.add("&8✦ Thành thục: " + mastery.getName());
                lore.add("&8Tiến trình: " + barColor + String.format("%.0f", progressPct) + "%");
            }

            // Trạng thái passive
            if (skill.type.equals("PASSIVE")) {
                boolean active = data.isPassiveActive(skill.id);
                lore.add("");
                lore.add((active ? "&a[ĐANG KÍCH HOẠT]" : "&7[ĐANG TẮT]") +
                         " &eShift+Click để chuyển đổi");
            }

            // Quickbar info
            int assignedSlot = data.getSkillData().findSkillSlot(skill.id);
            if (assignedSlot >= 0) {
                lore.add("");
                lore.add("&a✦ Đã gán: Số " + (assignedSlot + 1));
                lore.add("&eClick chuột phải với skill trong tay để thi triển!");
            } else {
                lore.add("");
                lore.add("&7Click để thi triển (ACTIVE)");
                lore.add("&7Chưa gán vào thanh nhanh");
            }

            ItemBuilder builder = new ItemBuilder(iconMat)
                    .setName(gradeColor + "◈ " + skill.name)
                    .setLore(lore);

            // Skill đã học thì phát sáng
            builder.setGlow(true);

            if (slot < 54) {
                gui.setItem(slot, builder.build());
                slot++;
            }
        }

        // Fill empty slots
        gui.setItem(49, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại Menu Chính")
                .build());

        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }

        player.openInventory(gui);
    }

    public void handleSkillClick(Player player, int slot, boolean isShiftClick) {
        if (slot < 9 || slot >= 54) return;

        // Nút gán skill (slot 8)
        if (slot == 8 && quickbarGUI != null) {
            quickbarGUI.openAssignMenu(player);
            return;
        }

        // Xử lý click vào skill
        int index = slot - 9;
        List<SkillConfig> skillList = new ArrayList<>();
        for (SkillConfig sc : registeredSkills.values()) {
            if (sc.isLearned(player, plugin)) {
                skillList.add(sc);
            }
        }
        if (index >= skillList.size()) return;

        SkillConfig skill = skillList.get(index);
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        if (skill.type.equals("ACTIVE")) {
            castSkill(player, skill, data);
        } else if (skill.type.equals("PASSIVE") && isShiftClick) {
            data.togglePassive(skill.id);
            boolean nowActive = data.isPassiveActive(skill.id);
            MessageUtils.send(player, (nowActive ? "&aĐã kích hoạt" : "&cĐã tắt") + " kỹ năng: " + skill.name);
            MessageUtils.playSound(player, Sound.BLOCK_LEVER_CLICK);
            openSkillMenu(player);
        }
    }

    /**
     * Thi triển kỹ năng - tích hợp mastery, grade, quality, cooldown
     */
    public void castSkill(Player player, SkillConfig skill, PlayerCultivationData data) {
        // Kiểm tra cooldown
        if (data.getSkillData().isOnCooldown(skill.id)) {
            long remaining = data.getSkillData().getCooldownRemaining(skill.id) / 1000;
            MessageUtils.send(player, "&cKỹ năng đang hồi chiêu! Còn &e" + remaining + " &cgiây.");
            return;
        }

        // Tính mana cost với mastery reduction
        int manaCost = skill.manaCost;
        if (masteryEnabled) {
            SkillMastery mastery = data.getSkillData().getMasteryLevel(skill.id);
            double reduction = 1.0 - mastery.getManaCostReduction();
            manaCost = (int) Math.max(1, Math.ceil(manaCost * reduction * skill.grade.getManaCostMultiplier()));
        }

        if (manaCost > 0) {
            if (!plugin.getCultivationManager().consumeMana(player, manaCost)) {
                MessageUtils.send(player, "&cKhông đủ linh lực! (Cần &b" + manaCost + " &clinh lực)");
                return;
            }
        }

        // Lấy grade color cho message
        String gradeColor = skill.grade.getColor();

        MessageUtils.send(player, gradeColor + "✦ Thi triển: &e" + skill.name);
        MessageUtils.playSound(player, Sound.ENTITY_BLAZE_SHOOT);

        // Tính damage/heal multiplier dựa trên mastery
        double damageMultiplier = 1.0;
        double healMultiplier = 1.0;
        double defenseMultiplier = 1.0;

        if (masteryEnabled) {
            SkillMastery mastery = data.getSkillData().getMasteryLevel(skill.id);
            damageMultiplier = mastery.getDamageMultiplier() * skill.grade.getDamageMultiplier();
            healMultiplier = mastery.getHealMultiplier() * skill.grade.getHealMultiplier();
            defenseMultiplier = mastery.getDefenseMultiplier() * skill.grade.getDefenseMultiplier();
        } else {
            damageMultiplier = skill.grade.getDamageMultiplier();
            healMultiplier = skill.grade.getHealMultiplier();
            defenseMultiplier = skill.grade.getDefenseMultiplier();
        }

        // Tính cooldown với mastery reduction
        int cooldownSeconds = (int) Math.max(1, Math.ceil(skill.cooldownSeconds * skill.grade.getCooldownMultiplier()));
        if (masteryEnabled) {
            SkillMastery mastery = data.getSkillData().getMasteryLevel(skill.id);
            double reduction = 1.0 - mastery.getCooldownReduction();
            cooldownSeconds = (int) Math.max(1, Math.ceil(cooldownSeconds * reduction));
        }

        // Đặt cooldown
        data.getSkillData().setCooldown(skill.id, cooldownSeconds);

        // Cast skill dựa trên id
        switch (skill.id) {
            case "BASIC_HEAL": {
                double baseHeal = skill.getDouble("heal-amount", 10);
                double healAmount = baseHeal * healMultiplier;
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                MessageUtils.send(player, String.format("&a✦ Hồi phục +%.0f HP! (%.0f x %.1f)", healAmount, baseHeal, healMultiplier));
                break;
            }
            case "QI_SHIELD": {
                double baseShield = skill.getDouble("shield-amount", 20);
                double shieldAmount = baseShield * defenseMultiplier;
                int duration = skill.getInt("duration-seconds", 10);
                player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), shieldAmount));
                MessageUtils.send(player, String.format("&b✦ Khiên linh khí: Hấp thụ %.0f sát thương!", shieldAmount));
                break;
            }
            case "FIRE_BALL":
            case "HOA_CAU_THUAT": {
                double baseDamage = skill.getDouble("damage", 15);
                double finalDamage = baseDamage * damageMultiplier;
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setYield(0);
                fireball.setIsIncendiary(false);
                MessageUtils.send(player, String.format("&c✦ Hỏa Cầu: %.0f sát thương!", finalDamage));
                break;
            }
            case "LIGHTNING_STRIKE": {
                double baseDamage = skill.getDouble("damage", 25);
                double finalDamage = baseDamage * damageMultiplier;
                Block target = player.getTargetBlockExact(50);
                if (target != null) {
                    target.getWorld().strikeLightning(target.getLocation());
                } else {
                    player.getWorld().strikeLightning(player.getLocation().add(
                            player.getLocation().getDirection().multiply(10)));
                }
                MessageUtils.send(player, String.format("&e✦ Thiên Lôi Dẫn: %.0f sát thương!", finalDamage));
                break;
            }
            case "SPEED_STEP": {
                double speedMult = skill.getDouble("speed-multiplier", 1.4);
                int duration = skill.getInt("duration-seconds", 15);
                float newSpeed = (float) Math.min(1.0, 0.2 * speedMult);
                player.setWalkSpeed(newSpeed);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.setWalkSpeed(0.2f);
                }, duration * 20L);
                MessageUtils.send(player, String.format("&b✦ Phi Vân Bộ: +%.0f%% tốc độ %ds!", (speedMult-1)*100, duration));
                break;
            }
            case "TELEPORT": {
                int maxDist = skill.getInt("max-distance", 50);
                Block teleBlock = player.getTargetBlockExact(maxDist);
                if (teleBlock != null) {
                    player.teleport(teleBlock.getLocation().add(0.5, 1, 0.5));
                    MessageUtils.send(player, "&d✦ Thuấn Di: Di chuyển tức thời!");
                } else {
                    MessageUtils.send(player, "&cKhông thể dịch chuyển đến vị trí đó!");
                }
                break;
            }
            case "WIND_BLADE": {
                double baseDamage = skill.getDouble("damage", 12);
                double finalDamage = baseDamage * damageMultiplier;
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setDamage(finalDamage);
                MessageUtils.send(player, String.format("&f✦ Phong Nhẫn: %.0f sát thương!", finalDamage));
                break;
            }
            case "METEOR_STORM": {
                int meteorCount = skill.getInt("meteor-count", 20);
                double radius = skill.getDouble("radius", 12.0);
                for (int i = 0; i < Math.min(meteorCount, 10); i++) {
                    org.bukkit.util.Vector dir = new org.bukkit.util.Vector(
                            Math.random() - 0.5, 1, Math.random() - 0.5);
                    Fireball fb = player.getWorld().spawn(
                            player.getEyeLocation().add(dir.clone().multiply(2)),
                            Fireball.class);
                    fb.setDirection(dir);
                }
                MessageUtils.send(player, "&6✦ Tinh Thần Bạo: Hủy diệt khu vực!");
                MessageUtils.broadcast("&6&l" + player.getName() + " &r&6đã thi triển &eTinh Thần Bạo&6!",
                        Sound.ENTITY_ENDER_DRAGON_HURT);
                break;
            }
            case "KIM_CUONG_CHU": {
                int armorBonus = skill.getInt("armor-bonus", 4);
                int duration = skill.getInt("duration-seconds", 10);
                // Dùng Resistance effect để tăng giáp
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
                        duration * 20, armorBonus - 1, true));
                MessageUtils.send(player, String.format("&f✦ Kim Cương Chú: +%d giáp %ds!", armorBonus, duration));
                break;
            }
            case "DON_THO_THUAT": {
                int duration = skill.getInt("duration-seconds", 3);
                // Invisibility + chui xuống đất
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                        duration * 20, 0, true));
                // Đưa player xuống dưới block
                player.teleport(player.getLocation().add(0, -1, 0));
                MessageUtils.send(player, String.format("&6✦ Độn Thổ: Ẩn thân %ds!", duration));
                break;
            }
            case "HOA_DIEM_SON": {
                double damagePerSec = skill.getDouble("damage-per-second", 8) * damageMultiplier;
                int duration = skill.getInt("duration-seconds", 5);
                double radius = skill.getDouble("radius", 5.0);
                // Tạo lửa xung quanh
                for (double x = -radius; x <= radius; x += 1) {
                    for (double z = -radius; z <= radius; z += 1) {
                        if (x*x + z*z <= radius*radius) {
                            Block block = player.getLocation().add(x, 0, z).getBlock();
                            if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) {
                                block.setType(Material.FIRE);
                            }
                        }
                    }
                }
                MessageUtils.send(player, String.format("&c✦ Hỏa Diệm Sơn: %.0f dmg/s x%ds R=%.0f!", 
                        damagePerSec, duration, radius));
                break;
            }
            case "BANG_TAM_QUYET": {
                int duration = skill.getInt("duration-seconds", 8);
                int wallLength = skill.getInt("wall-length", 5);
                int wallHeight = skill.getInt("wall-height", 3);
                // Tạo tường băng phía trước mặt
                org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize();
                for (int i = 0; i < wallLength; i++) {
                    for (int h = 0; h < wallHeight; h++) {
                        Block block = player.getLocation().add(
                                dir.getX() * (i + 2), 
                                h, 
                                dir.getZ() * (i + 2)
                        ).getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.PACKED_ICE);
                        }
                    }
                }
                MessageUtils.send(player, String.format("&b✦ Băng Tâm Quyết: Tường băng %dx%d trong %ds!", 
                        wallLength, wallHeight, duration));
                break;
            }
            case "THIEM_DIEN_BO": {
                double dashDist = skill.getDouble("dash-distance", 8);
                double baseDamage = skill.getDouble("damage", 12);
                double finalDamage = baseDamage * damageMultiplier;
                // Dash về phía trước
                org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize();
                player.teleport(player.getLocation().add(dir.multiply(dashDist)));
                MessageUtils.send(player, String.format("&e✦ Thiểm Điện Bộ: Dash %.0f block, %.0f dmg!", 
                        dashDist, finalDamage));
                break;
            }
            case "HUYEN_BANG_CHI": {
                double baseDamage = skill.getDouble("damage", 15);
                double finalDamage = baseDamage * damageMultiplier;
                int slowDuration = skill.getInt("slow-duration-seconds", 3);
                // Bắn mũi tên băng
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setDamage(finalDamage);
                arrow.setCustomName("Huyền Băng Chỉ");
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 
                        slowDuration * 20, 1), true);
                MessageUtils.send(player, String.format("&b✦ Huyền Băng Chỉ: %.0f dmg + làm chậm %ds!", 
                        finalDamage, slowDuration));
                break;
            }
            case "HUYEN_ANH_BO": {
                int cloneCount = skill.getInt("clone-count", 2);
                int duration = skill.getInt("duration-seconds", 8);
                // Spawn bản sao (dùng zombie không target)
                for (int i = 0; i < cloneCount; i++) {
                    org.bukkit.entity.Zombie clone = player.getWorld().spawn(
                            player.getLocation().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1),
                            org.bukkit.entity.Zombie.class);
                    clone.setCustomName(player.getName() + " (Ảo Ảnh)");
                    clone.setCustomNameVisible(true);
                    clone.setAI(true);
                    clone.setTarget(null);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (clone.isValid()) clone.remove();
                    }, duration * 20L);
                }
                MessageUtils.send(player, String.format("&5✦ Huyễn Ảnh Bộ: %d ảo ảnh %ds!", cloneCount, duration));
                break;
            }
            case "PHONG_BAO_THUAT": {
                double baseDamage = skill.getDouble("damage", 10);
                double finalDamage = baseDamage * damageMultiplier;
                double radius = skill.getDouble("radius", 7.0);
                double knockback = skill.getDouble("knockback-power", 2.0);
                // Gây damage + đẩy lùi tất cả entity trong vùng
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity) {
                        org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                        living.damage(finalDamage, player);
                        org.bukkit.util.Vector kb = living.getLocation().toVector()
                                .subtract(player.getLocation().toVector()).normalize().multiply(knockback);
                        living.setVelocity(kb);
                    }
                }
                MessageUtils.send(player, String.format("&f✦ Phong Bạo: %.0f dmg R=%.0f!", finalDamage, radius));
                break;
            }
            case "TU_VI_THAN_KIEM": {
                double baseDamage = skill.getDouble("damage", 30);
                double finalDamage = baseDamage * damageMultiplier;
                int pierce = skill.getInt("pierce-count", 3);
                // Bắn mũi tên xuyên thấu
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setDamage(finalDamage);
                arrow.setPierceLevel(pierce);
                MessageUtils.send(player, String.format("&e✦ Tử Vi Thần Kiếm: %.0f dmg xuyên %d!", 
                        finalDamage, pierce));
                break;
            }
            case "PHAN_THAN_THUAT": {
                int duration = skill.getInt("duration-seconds", 15);
                double cloneDmgMult = skill.getDouble("clone-damage-multiplier", 0.4);
                // Tạo bản sao mạnh hơn (dùng skeleton hoặc wither skeleton)
                org.bukkit.entity.WitherSkeleton clone = player.getWorld().spawn(
                        player.getLocation().add(1, 0, 0),
                        org.bukkit.entity.WitherSkeleton.class);
                clone.setCustomName(player.getName() + " (Phân Thân)");
                clone.setCustomNameVisible(true);
                clone.setAI(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (clone.isValid()) clone.remove();
                }, duration * 20L);
                MessageUtils.send(player, String.format("&5✦ Phân Thân Thuật: %ds, %.0f%% dmg!", 
                        duration, cloneDmgMult * 100));
                break;
            }
            case "VAN_KIEM_QUY_TONG": {
                int waves = skill.getInt("waves", 6);
                double damagePerWave = skill.getDouble("damage-per-wave", 8) * damageMultiplier;
                double radius = skill.getDouble("radius", 8.0);
                // Tạo mưa kiếm - nhiều đợt sét/skeleton arrow
                for (int w = 0; w < waves; w++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (int i = 0; i < 5; i++) {
                            double angle = Math.random() * 2 * Math.PI;
                            double r = Math.random() * radius;
                            double x = player.getLocation().getX() + r * Math.cos(angle);
                            double z = player.getLocation().getZ() + r * Math.sin(angle);
                            player.getWorld().strikeLightningEffect(
                                    new org.bukkit.Location(player.getWorld(), x, 
                                            player.getLocation().getY(), z));
                        }
                    }, w * 10L);
                }
                MessageUtils.send(player, String.format("&6✦ Vạn Kiếm Quy Tông: %d đợt x%.0f dmg!", 
                        waves, damagePerWave));
                break;
            }
            case "THIEN_DIA_DONG_THO": {
                double healPercent = skill.getDouble("heal-percent", 0.5);
                int invulDuration = skill.getInt("invulnerable-duration-seconds", 3);
                double healAmount = player.getMaxHealth() * healPercent;
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
                        invulDuration * 20, 4, true));
                MessageUtils.send(player, String.format("&a✦ Thiên Địa Đồng Thọ: +%.0f HP, miễn nhiễm %ds!", 
                        healAmount, invulDuration));
                SkillEffects.playGradeEffect(player, skill.grade);
                break;
            }
            case "DIET_THAN_NHAT_KICH": {
                double baseDamage = skill.getDouble("damage", 50);
                double finalDamage = baseDamage * damageMultiplier;
                boolean lightning = skill.getBoolean("lightning-effect", true);
                if (lightning) {
                    player.getWorld().strikeLightning(player.getTargetBlockExact(50) != null ? 
                            player.getTargetBlockExact(50).getLocation() : player.getLocation());
                }
                // Bắn mũi tên siêu mạnh
                Arrow arrow2 = player.launchProjectile(Arrow.class);
                arrow2.setDamage(finalDamage);
                arrow2.setPierceLevel(10);
                MessageUtils.send(player, String.format("&c✦ Diệt Thần Nhất Kích: %.0f sát thương hủy diệt!", 
                        finalDamage));
                MessageUtils.broadcast("&4&l" + player.getName() + " &r&4đã thi triển &cDiệt Thần Nhất Kích&4!",
                        Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
                SkillEffects.playGradeEffect(player, skill.grade);
                break;
            }
            case "HUY_DIET_THIEN_DIA": {
                double baseDamage = skill.getDouble("damage", 60);
                double finalDamage = baseDamage * damageMultiplier;
                double radius = skill.getDouble("radius", 15.0);
                boolean destroyBlocks = skill.getBoolean("destroy-blocks", false);
                // Tạo vụ nổ lớn
                player.getWorld().createExplosion(player.getLocation(), 
                        destroyBlocks ? (float)radius : 0, false, true);
                // Gây damage cho entity trong vùng
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(player)) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(finalDamage, player);
                    }
                }
                MessageUtils.send(player, String.format("&4✦ Hủy Diệt Thiên Địa: %.0f dmg R=%.0f!", 
                        finalDamage, radius));
                MessageUtils.broadcast("&4&l" + player.getName() + " &r&4đã thi triển &4&lHỦY DIỆT THIÊN ĐỊA&4!",
                        Sound.ENTITY_WITHER_SPAWN);
                SkillEffects.playGradeEffect(player, skill.grade);
                break;
            }
            case "THIEN_DIA_VO_CUC": {
                double dmgMult = skill.getDouble("damage-multiplier", 2.0);
                double defMult = skill.getDouble("defense-multiplier", 2.0);
                double speedMult = skill.getDouble("speed-multiplier", 1.5);
                int duration = skill.getInt("duration-seconds", 30);
                // Buff toàn diện
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                        duration * 20, (int)(dmgMult * 2 - 1), true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
                        duration * 20, (int)(defMult * 2 - 1), true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                        duration * 20, (int)(speedMult * 2), true));
                MessageUtils.send(player, String.format("&6✦ Thiên Địa Vô Cực: x%.1f dmg x%.1f def x%.1f speed %ds!", 
                        dmgMult, defMult, speedMult, duration));
                MessageUtils.broadcast("&6&l" + player.getName() + " &r&6đã khai mở &e&lTHIÊN ĐỊA VÔ CỰC&6!",
                        Sound.BLOCK_BEACON_ACTIVATE);
                SkillEffects.playGradeEffect(player, skill.grade);
                break;
            }
            case "PHUC_SINH_THUAT": {
                // Passive - xử lý lúc player chết (sẽ implement trong death listener)
                MessageUtils.send(player, "&d✦ Phục Sinh Thuật (Bị động): Sẽ hồi sinh bạn 1 lần khi chết!");
                break;
            }
            default:
                MessageUtils.send(player, gradeColor + "✦ Thi triển: " + skill.name);
        }

        // Tăng mastery EXP sau khi cast
        if (masteryEnabled) {
            SkillMastery oldMastery = data.getSkillData().getMasteryLevel(skill.id);
            int baseExp = 10; // Từ config
            double gradeExpMult = 1.0;
            try {
                String gradeName = skill.grade.name();
                // Parse từ config
            } catch (Exception ignored) {}
            data.getSkillData().addMasteryExp(skill.id, baseExp);
            SkillMastery newMastery = data.getSkillData().getMasteryLevel(skill.id);
            if (newMastery != oldMastery) {
                MessageUtils.send(player, "&a✦ Thành thục " + skill.name + " tăng lên: " + newMastery.getName() + "!");
                MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
            }
        }
    }

    public int getLearnedSkillCount(Player player) {
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;
        return data.getLearnedSkills().size();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isMasteryEnabled() { return masteryEnabled; }
    public boolean isQuickbarEnabled() { return quickbarEnabled; }

    public void reload() { loadConfig(); }
    public Collection<SkillConfig> getSkills() { return registeredSkills.values(); }

    // ==================== GUI EVENT HANDLERS ====================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains("Công Pháp & Kỹ Năng")) return;

        int slot = event.getRawSlot();
        if (slot >= 0 && slot < 54) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Nút quay lại menu chính
            if (slot == 49) {
                MainMenuGUI mainMenu = plugin.getMainMenuGUI();
                if (mainMenu != null) {
                    mainMenu.openMainMenu(player);
                } else {
                    player.closeInventory();
                }
                return;
            }

            boolean isShiftClick = event.isShiftClick();
            handleSkillClick(player, slot, isShiftClick);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Dọn dẹp nếu cần
    }

    // ==================== SKILL CONFIG ====================

    public static class SkillConfig {
        public final String id;
        public final String name;
        public final List<String> description;
        public final String type;
        public final int requiredLevel;
        public final double expCost;
        public final int manaCost;
        public final int cooldownSeconds;
        public final String icon;
        public final SkillGrade grade;
        public final SkillQuality quality;
        private final ConfigurationSection effects;

        SkillConfig(String id, String name, List<String> description, String type,
                   int requiredLevel, double expCost, int manaCost, int cooldownSeconds,
                   String icon, SkillGrade grade, SkillQuality quality,
                   ConfigurationSection effects) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.requiredLevel = requiredLevel;
            this.expCost = expCost;
            this.manaCost = manaCost;
            this.cooldownSeconds = cooldownSeconds;
            this.icon = icon;
            this.grade = grade;
            this.quality = quality;
            this.effects = effects;
        }

        public double getDouble(String path, double def) {
            return effects != null ? effects.getDouble(path, def) : def;
        }

        public int getInt(String path, int def) {
            return effects != null ? effects.getInt(path, def) : def;
        }

        public String getString(String path, String def) {
            return effects != null ? effects.getString(path, def) : def;
        }

        public boolean getBoolean(String path, boolean def) {
            return effects != null ? effects.getBoolean(path, def) : def;
        }

        /**
         * Kiểm tra player đã học skill này chưa
         */
        public boolean isLearned(Player player, VNMinePlugin plugin) {
            PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
            return data != null && data.hasLearnedSkill(id);
        }

        /**
         * Lấy thời gian hồi thực tế (sau khi áp dụng grade multiplier)
         */
        public int getActualCooldown() {
            return (int) Math.max(1, Math.ceil(cooldownSeconds * grade.getCooldownMultiplier()));
        }
    }
}