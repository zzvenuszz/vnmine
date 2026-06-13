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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;

import java.util.*;

/**
 * SkillManager - Quản lý hệ thống công pháp / kỹ năng
 */
public class SkillManager implements Listener {

    private final VNMinePlugin plugin;
    private boolean enabled;

    private final Map<String, SkillConfig> registeredSkills;

    public SkillManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.registeredSkills = new LinkedHashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        registeredSkills.clear();

        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection == null) {
            enabled = false;
            return;
        }

        enabled = skillsSection.getBoolean("enabled", true);

        ConfigurationSection skillsList = skillsSection.getConfigurationSection("skills");
        if (skillsList == null) return;

        for (String skillId : skillsList.getKeys(false)) {
            ConfigurationSection skill = skillsList.getConfigurationSection(skillId);
            if (skill == null) continue;

            if (!skill.getBoolean("enabled", true)) continue;

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
                    skill.getString("executor", ""),
                    skill.getConfigurationSection("executor-config")
            );
            registeredSkills.put(skillId, sc);
        }

        plugin.getLogger().info("Loaded " + registeredSkills.size() + " skills.");
    }

    public void openSkillMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ Công Pháp & Kỹ Năng ✦"));

        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        int learnedCount = getLearnedSkillCount(player);
        int level = (data != null) ? data.getLevel() : 1;

        gui.setItem(4, new ItemBuilder(Material.BOOK)
                .setGlow(true)
                .setName("&d&lCông Pháp Đã Học")
                .setLore(
                        "",
                        "&fĐã học: &e" + learnedCount + " &7/ " + registeredSkills.size(),
                        "&fCấp hiện tại: &e" + level,
                        "",
                        "&7Click vào skill để thi triển (ACTIVE)",
                        "&7Shift+Click để bật/tắt (PASSIVE)"
                ).build());

        int slot = 9;
        for (SkillConfig skill : registeredSkills.values()) {
            boolean learned = data != null && data.hasLearnedSkill(skill.id);
            Material iconMat = Material.getMaterial(skill.icon.toUpperCase());
            if (iconMat == null) iconMat = Material.STONE;

            List<String> lore = new ArrayList<>(skill.description);
            lore.add("");
            lore.add("&fLoại: &e" + (skill.type.equals("ACTIVE") ? "Chủ động" : "Thụ động"));
            lore.add("&fYêu cầu cấp: &e" + skill.requiredLevel);
            lore.add("&fLinh lực: &b" + skill.manaCost);
            lore.add("&fEXP để học: &5" + (int) skill.expCost);
            lore.add("");

            if (learned) {
                lore.add("&a&l✓ Đã học");
                if (skill.type.equals("PASSIVE") && data != null) {
                    boolean active = data.isPassiveActive(skill.id);
                    lore.add((active ? "&a[ĐANG KÍCH HOẠT]" : "&7[ĐANG TẮT]") +
                             " &eShift+Click để chuyển đổi");
                }
            } else {
                if (level >= skill.requiredLevel) {
                    lore.add("&eClick để học!");
                } else {
                    lore.add("&cYêu cầu cấp " + skill.requiredLevel);
                }
            }

            ItemBuilder builder = new ItemBuilder(iconMat)
                    .setName((learned ? "&a" : "&7") + "◈ " + skill.name)
                    .setLore(lore);

            if (learned) builder.setGlow(true);

            if (slot < 54) {
                gui.setItem(slot, builder.build());
                slot++;
            }
        }

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

        int index = slot - 9;
        List<SkillConfig> skillList = new ArrayList<>(registeredSkills.values());
        if (index >= skillList.size()) return;

        SkillConfig skill = skillList.get(index);
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        boolean learned = data.hasLearnedSkill(skill.id);

        if (!learned) {
            if (data.getLevel() < skill.requiredLevel) {
                MessageUtils.send(player, "&cBạn chưa đủ cấp để học kỹ năng này! (Cần cấp " + skill.requiredLevel + ")");
                return;
            }
            if (skill.expCost > 0 && !data.consumeExperience(skill.expCost)) {
                MessageUtils.send(player, "&cBạn không đủ tu vi (exp)!");
                return;
            }
            data.learnSkill(skill.id);
            MessageUtils.send(player, "&a✦ Bạn đã học được công pháp: &e" + skill.name);
            MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
            openSkillMenu(player);
            return;
        }

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
     * Cast skill gốc (từ skill menu, không có proficiency bonus)
     */
    public void castSkill(Player player, SkillConfig skill, PlayerCultivationData data) {
        castSkill(player, skill, data, 1.0);
    }

    /**
     * Cast skill với proficiency multiplier
     */
    public void castSkill(Player player, SkillConfig skill, PlayerCultivationData data, double proficiencyMultiplier) {
        if (skill.manaCost > 0) {
            if (!plugin.getCultivationManager().consumeMana(player, skill.manaCost)) {
                MessageUtils.send(player, "&cKhông đủ linh lực! (Cần &b" + skill.manaCost + " &clinh lực)");
                return;
            }
        }

        MessageUtils.send(player, "&d✦ Thi triển: &e" + skill.name);
        MessageUtils.playSound(player, Sound.ENTITY_BLAZE_SHOOT);

        // Áp dụng proficiency multiplier vào các chỉ số skill
        switch (skill.id) {
            case "BASIC_HEAL": {
                double healAmount = skill.getDouble("heal-amount", 10) * proficiencyMultiplier;
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                MessageUtils.send(player, "&a✦ Hồi phục +" + (int)healAmount + " HP!");
                break;
            }
            case "QI_SHIELD": {
                double shieldAmount = skill.getDouble("shield-amount", 20) * proficiencyMultiplier;
                int durationSeconds = skill.getInt("duration-seconds", 10);
                double durationMult = 1.0; // duration multiplier from proficiency
                player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), shieldAmount));
                // Set shield với thời gian hiệu lực dài hơn nếu proficiency cao
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.getAbsorptionAmount() > 0 && player.getAbsorptionAmount() <= shieldAmount) {
                        player.setAbsorptionAmount(0);
                    }
                }, (long)(durationSeconds * 20L * durationMult));
                MessageUtils.send(player, "&b✦ Khiên linh khí: Hấp thụ &b" + (int)shieldAmount + " &7sát thương!");
                break;
            }
            case "FIRE_BALL": {
                double damage = skill.getDouble("damage", 15) * proficiencyMultiplier;
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setYield((float)(damage / 15.0)); // Tăng yield theo damage
                fireball.setIsIncendiary(damage > 20);
                MessageUtils.send(player, "&c✦ Hỏa Cầu Thuật! (Sát thương: &c" + (int)damage + "&c)");
                break;
            }
            case "LIGHTNING_STRIKE": {
                double lightningDamage = skill.getDouble("damage", 25) * proficiencyMultiplier;
                Block target = player.getTargetBlockExact(50);
                if (target != null) {
                    target.getWorld().strikeLightning(target.getLocation());
                } else {
                    player.getWorld().strikeLightning(player.getLocation().add(
                            player.getLocation().getDirection().multiply(10)));
                }
                MessageUtils.send(player, "&e✦ Thiên Lôi Dẫn: Gây &e" + (int)lightningDamage + " &7sát thương!");
                break;
            }
            case "SPEED_STEP": {
                double speedMult = skill.getDouble("speed-multiplier", 1.4);
                int speedDuration = skill.getInt("duration-seconds", 15);
                // Tăng tốc độ và thời gian theo proficiency
                double enhancedSpeed = speedMult * (0.8 + 0.4 * (proficiencyMultiplier / 2.0));
                double enhancedDuration = speedDuration * (0.8 + 0.4 * (proficiencyMultiplier / 2.0));
                player.setWalkSpeed((float) Math.min(1.0, 0.2 * enhancedSpeed));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.setWalkSpeed(0.2f);
                }, (long)(enhancedDuration * 20L));
                MessageUtils.send(player, "&b✦ Phi Vân Bộ: Tăng &b" + (int)((enhancedSpeed-1)*100) + "% &7tốc độ!");
                break;
            }
            case "TELEPORT": {
                int maxDist = (int)(skill.getInt("max-distance", 50) * (0.8 + 0.4 * (proficiencyMultiplier / 2.0)));
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
                double windDamage = skill.getDouble("damage", 12) * proficiencyMultiplier;
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setDamage(windDamage);
                // Tăng tốc độ mũi tên theo proficiency
                arrow.setVelocity(arrow.getVelocity().multiply(0.8 + 0.4 * (proficiencyMultiplier / 2.0)));
                MessageUtils.send(player, "&f✦ Phong Nhẫn: Gây &f" + (int)windDamage + " &7sát thương xuyên thấu!");
                break;
            }
            case "FIRE_CONTROL": {
                // Khống Hỏa Thuật - PASSIVE: hiển thị thông tin thành thục
                PlayerSkillData psd = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
                if (psd != null) {
                    PlayerSkillData.ProficiencyLevel profLevel = psd.getProficiencyLevel("FIRE_CONTROL");
                    double timeReduction = psd.getAlchemyTimeReduction();
                    int gradeBonus = psd.getAlchemyGradeBonus();
                    int usage = psd.getSkillUsageCount("FIRE_CONTROL");
                    MessageUtils.send(player, "&c✦ Khống Hỏa Thuật ✦");
                    MessageUtils.send(player, "&7Độ thuần thục: " + profLevel.getDisplayName());
                    MessageUtils.send(player, "&7Lần sử dụng: &e" + usage);
                    MessageUtils.send(player, "&7Giảm thời gian luyện đan: &c" + (int)(timeReduction * 100) + "%");
                    MessageUtils.send(player, "&7Tăng phẩm cấp đan dược: &e+" + gradeBonus);
                }
                break;
            }
            case "FORGE_MASTERY": {
                // Luyện Khí Thuật - PASSIVE: hiển thị thông tin thành thục
                PlayerSkillData psd2 = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
                if (psd2 != null) {
                    PlayerSkillData.ProficiencyLevel profLevel2 = psd2.getProficiencyLevel("FORGE_MASTERY");
                    double manaReduction2 = psd2.getForgeManaReduction();
                    int gradeBonus2 = psd2.getForgeGradeBonus();
                    int usage2 = psd2.getSkillUsageCount("FORGE_MASTERY");
                    MessageUtils.send(player, "&6✦ Luyện Khí Thuật ✦");
                    MessageUtils.send(player, "&7Độ thuần thục: " + profLevel2.getDisplayName());
                    MessageUtils.send(player, "&7Lần sử dụng: &e" + usage2);
                    MessageUtils.send(player, "&7Giảm tiêu hao linh lực: &6" + (int)(manaReduction2 * 100) + "%");
                    MessageUtils.send(player, "&7Tăng phẩm cấp pháp khí: &e+" + gradeBonus2);
                }
                break;
            }
            case "METEOR_STORM": {
                int meteorCount = (int)(skill.getInt("meteor-count", 20) * (0.8 + 0.4 * (proficiencyMultiplier / 2.0)));
                int actualCount = Math.min(meteorCount, 20);
                for (int i = 0; i < actualCount; i++) {
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
            default:
                MessageUtils.send(player, "&e✦ Thi triển: " + skill.name);
        }
    }

    public int getLearnedSkillCount(Player player) {
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;
        return data.getLearnedSkills().size();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void reload() { loadConfig(); }
    public Collection<SkillConfig> getSkills() { return registeredSkills.values(); }

    // ==================== GUI EVENT HANDLERS ====================

    /**
     * Xử lý click trong skill menu.
     * Cancel tất cả slot 0-53 để ngăn người chơi lấy item ra khỏi GUI.
     * Click slot 49 = quay lại menu chính (MainMenu).
     * Click skill items = học/thi triển/bật tắt skill.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Chỉ xử lý skill menu (kiểm tra bằng tên inventory)
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

            // Click vào skill item
            boolean isShiftClick = event.isShiftClick();
            handleSkillClick(player, slot, isShiftClick);
        }
    }

    /**
     * Dọn dẹp khi đóng skill menu (không cần thiết lắm vì không có session,
     * nhưng thêm vào để đồng bộ với pattern các GUI khác)
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Không cần cleanup gì đặc biệt cho skill menu
        // Chỉ để đồng bộ pattern
    }

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
        public final String executor;
        private final ConfigurationSection executorConfig;

        SkillConfig(String id, String name, List<String> description, String type,
                   int requiredLevel, double expCost, int manaCost, int cooldownSeconds,
                   String icon, String executor, ConfigurationSection executorConfig) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.requiredLevel = requiredLevel;
            this.expCost = expCost;
            this.manaCost = manaCost;
            this.cooldownSeconds = cooldownSeconds;
            this.icon = icon;
            this.executor = executor;
            this.executorConfig = executorConfig;
        }

        public double getDouble(String path, double def) {
            return executorConfig != null ? executorConfig.getDouble(path, def) : def;
        }

        public int getInt(String path, int def) {
            return executorConfig != null ? executorConfig.getInt(path, def) : def;
        }

        public String getString(String path, String def) {
            return executorConfig != null ? executorConfig.getString(path, def) : def;
        }
    }
}