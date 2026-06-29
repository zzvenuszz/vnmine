package com.vnmine.item;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PillConfig;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PillUseListener - Xử lý sử dụng đan dược khi click phải
 * Sử dụng PersistentDataContainer để nhận diện loại đan và charge count
 * Mỗi lọ đan dược có 10 charges, giảm dần khi dùng
 * Tác dụng scale theo phẩm cấp (gradeIndex 0-11)
 */
public class PillUseListener implements Listener {

    private final VNMinePlugin plugin;

    // Cooldown sử dụng đan dược (tránh spam click)
    private final Map<UUID, Long> useCooldowns = new HashMap<>();
    private static final long USE_COOLDOWN_MS = 500; // 0.5 giây

    // Theo dõi Phi Thăng Đan đã dùng theo đại cảnh giới (level / 10)
    private final Map<UUID, Integer> phiThangDanUsedRealms = new HashMap<>();

    // Theo dõi Phê Ma Đan (tăng sát thương vs quái)
    private final Map<UUID, Long> pheMaDanExpiry = new HashMap<>();

    // Theo dõi Trường Thọ Đan (hồi sinh)
    private final Map<UUID, Long> truongThoDanExpiry = new HashMap<>();

    // Keys cho persistent data
    private static final String KEY_PILL_TYPE = "vnmine_pill_type";
    private static final String KEY_PILL_CHARGES = "vnmine_pill_charges";
    private static final String KEY_PILL_GRADE = "vnmine_pill_grade";
    private static final int DEFAULT_CHARGES = 10;

    // Grade multipliers (đồng bộ với AlchemyCraftGUI)
    private static final double[] GRADE_MULTIPLIERS = {
        1.0, 1.3, 1.6, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 10.0
    };

    public PillUseListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Xử lý click phải để dùng đan dược
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Kiểm tra cooldown chống spam
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastUse = useCooldowns.get(uuid);
        if (lastUse != null && (now - lastUse) < USE_COOLDOWN_MS) {
            return;
        }

        // Kiểm tra item có phải đan dược không (qua persistent data)
        String pillType = getPillTypeFromNBT(item);
        if (pillType == null) {
            return;
        }

        event.setCancelled(true);
        useCooldowns.put(uuid, now);

        // Lấy dữ liệu tu luyện của player
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(uuid);
        if (data == null) {
            MessageUtils.send(player, "&cBạn chưa bắt đầu tu tiên! Dùng &e/vn start &cđể bắt đầu.");
            return;
        }

        plugin.getLogger().info("[PillDebug] " + player.getName() + " used " + pillType
            + " grade=" + getPillGrade(item) + " charges=" + getCharges(item));

        // Xử lý tác dụng theo loại đan
        boolean consumed = false;
        switch (pillType) {
            case "HOI_LINH_DAN":
                consumed = useHoiLinhDan(player, data, item);
                break;
            case "DAI_HOI_LINH_DAN":
                consumed = useDaiHoiLinhDan(player, data, item);
                break;
            case "CUONG_THE_DAN":
                consumed = useCuongTheDan(player, data, item);
                break;
            case "THANH_TAM_DAN":
                consumed = useThanhTamDan(player, data, item);
                break;
            case "TOC_THANH_DAN":
                consumed = useTocThanhDan(player, data, item);
                break;
            case "TU_LUYEN_DAN":
                consumed = useTuLuyenDan(player, data, item);
                break;
            case "PHI_THANG_DAN":
                consumed = usePhiThangDan(player, data, item);
                break;
            case "BACH_DOC_DAN":
                consumed = useBachDocDan(player, data, item);
                break;
            case "THIEN_HOI_DAN":
                consumed = useThienHoiDan(player, data, item);
                break;
            case "PHE_MA_DAN":
                consumed = usePheMaDan(player, data, item);
                break;
            case "TRUONG_THO_DAN":
                consumed = useTruongThoDan(player, data, item);
                break;
        }

        // Nếu đã dùng thành công, giảm charge
        if (consumed) {
            consumeCharge(player, item);
        }
    }

    /**
     * Lấy loại đan dược từ PersistentDataContainer (NBT tag)
     */
    private String getPillTypeFromNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        // Kiểm tra persistent data
        String pillType = ItemBuilder.getPersistentData(item, KEY_PILL_TYPE);
        if (pillType != null && !pillType.isEmpty()) {
            return pillType;
        }

        // Fallback: kiểm tra bằng tên hiển thị (cho item cũ)
        if (meta.hasDisplayName()) {
            String displayName = stripColor(meta.getDisplayName());
            if (displayName.contains("Đại Hồi Linh Đan")) return "DAI_HOI_LINH_DAN";
            if (displayName.contains("Hồi Linh Đan")) return "HOI_LINH_DAN";
            if (displayName.contains("Cương Thể Đan")) return "CUONG_THE_DAN";
            if (displayName.contains("Thanh Tâm Đan")) return "THANH_TAM_DAN";
            if (displayName.contains("Tốc Thánh Đan")) return "TOC_THANH_DAN";
            if (displayName.contains("Tu Luyện Đan")) return "TU_LUYEN_DAN";
            if (displayName.contains("Phi Thăng Đan")) return "PHI_THANG_DAN";
            if (displayName.contains("Bách Độc Đan")) return "BACH_DOC_DAN";
            if (displayName.contains("Thiên Hồi Đan")) return "THIEN_HOI_DAN";
            if (displayName.contains("Phê Ma Đan")) return "PHE_MA_DAN";
            if (displayName.contains("Trường Thọ Đan")) return "TRUONG_THO_DAN";
        }

        return null;
    }

    /**
     * Lấy grade index từ item (0=Hoàng Hạ -> 11=Thiên Thượng)
     */
    private int getPillGrade(ItemStack item) {
        String gradeStr = ItemBuilder.getPersistentData(item, KEY_PILL_GRADE);
        if (gradeStr != null) {
            try {
                return Integer.parseInt(gradeStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Lấy multiplier từ grade index
     */
    private double getGradeMultiplier(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_MULTIPLIERS.length) return 1.0;
        return GRADE_MULTIPLIERS[gradeIndex];
    }

    /**
     * Lấy số charge còn lại từ item
     */
    private int getCharges(ItemStack item) {
        String chargesStr = ItemBuilder.getPersistentData(item, KEY_PILL_CHARGES);
        if (chargesStr != null) {
            try {
                int charges = Integer.parseInt(chargesStr);
                if (charges > 0) return charges;
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        // Fallback: không có persistent data -> kiểm tra stack size
        if (item.getAmount() > 1) {
            // Item cũ dùng stack amount làm charge
            return item.getAmount();
        }
        return 10; // Default cho item mới
    }

    /**
     * Tiêu hao 1 charge (giảm hoặc xóa item)
     */
    private void consumeCharge(Player player, ItemStack item) {
        int charges = getCharges(item);
        plugin.getLogger().info("[PillDebug] consumeCharge: current charges=" + charges + " for " + player.getName());
        charges--;

        if (charges <= 0) {
            // Hết charge - xóa item
            player.getInventory().setItemInMainHand(null);
            MessageUtils.send(player, "&cLọ đan dược đã hết!");
            plugin.getLogger().info("[PillDebug] Item removed (charges depleted) for " + player.getName());
        } else {
            // Còn charge - update persistent data
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(plugin, KEY_PILL_CHARGES);
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.valueOf(charges));

                // Cập nhật lore để hiển thị số lần còn lại (ví dụ: 10/10)
                List<String> lore = meta.getLore();
                if (lore != null) {
                    for (int i = 0; i < lore.size(); i++) {
                        if (lore.get(i).contains("Lượng dùng:")) {
                            lore.set(i, ColorUtils.colorize("&7Lượng dùng: &e" + charges + "/" + DEFAULT_CHARGES + " &7lần"));
                            break;
                        }
                    }
                    meta.setLore(lore);
                }

                item.setItemMeta(meta);
                player.updateInventory();
                plugin.getLogger().info("[PillDebug] Charges updated to " + charges + " for " + player.getName());
            }
        }
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    // ==================== TÁC DỤNG CỦA TỪNG LOẠI ĐAN ====================
    // Trả về true nếu dùng thành công (để giảm charge)

    /**
     * Hồi Linh Đan - Hồi linh lực (đọc từ config)
     */
    private boolean useHoiLinhDan(Player player, PlayerCultivationData data, ItemStack item) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return false;
        }
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("HOI_LINH_DAN");
        int manaRegen = (effect != null) ? (int)(effect.baseRecover * mult) : (int)(30 * mult);
        data.regenMana(manaRegen);
        MessageUtils.send(player, "&a✦ Hồi Linh Đan: Hồi phục &b" + manaRegen + " &alinh lực! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Đại Hồi Linh Đan - Hồi linh lực lớn + hồi phục (đọc từ config)
     */
    private boolean useDaiHoiLinhDan(Player player, PlayerCultivationData data, ItemStack item) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return false;
        }
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("DAI_HOI_LINH_DAN");
        int manaRegen = (effect != null) ? (int)(effect.baseRecover * mult) : (int)(100 * mult);
        int regenDuration = (effect != null) ? (int)(effect.baseDuration * mult) : (int)(30 * mult);
        data.regenMana(manaRegen);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration * 20, 0, false, true, true));
        MessageUtils.send(player, "&b✦ Đại Hồi Linh Đan: Hồi phục &b" + manaRegen + " &7linh lực + hồi phục " + regenDuration + "s! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Cương Thể Đan - Tăng sát thương (đọc từ config)
     */
    private boolean useCuongTheDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("CUONG_THE_DAN");
        int baseDmg = (effect != null) ? effect.baseDmg : 20;
        int baseDuration = (effect != null) ? effect.baseDuration : 60;
        int level = Math.min(4, (int)(mult / 2));
        int bonusDmg = level * 5;
        int duration = (int)(baseDuration * mult);
        int totalDmg = baseDmg + bonusDmg;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, level, false, true, true));
        MessageUtils.send(player, "&c✦ Cương Thể Đan: Tăng &c" + totalDmg + "% sát thương &7trong " + duration + " giây! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Thanh Tâm Đan - Giải trừ mọi trạng thái xấu
     */
    private boolean useThanhTamDan(Player player, PlayerCultivationData data, ItemStack item) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.setFireTicks(0);
        MessageUtils.send(player, "&a✦ Thanh Tâm Đan: Giải trừ mọi trạng thái xấu!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Tốc Thánh Đan - Tăng tốc độ (đọc từ config)
     */
    private boolean useTocThanhDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("TOC_THANH_DAN");
        int baseRegen = (effect != null) ? effect.baseRegen : 50;
        int baseDuration = (effect != null) ? effect.baseDuration : 300;
        int level = Math.min(4, (int)(mult / 2));
        int bonusRegen = level * 20;
        int duration = (int)(baseDuration * mult);
        int totalRegen = baseRegen + bonusRegen;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, level, false, true, true));
        MessageUtils.send(player, "&b✦ Tốc Thánh Đan: Tăng &b" + totalRegen + "% tốc độ &7trong " + duration + " giây! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Tu Luyện Đan - +EXP tu luyện (đọc từ config)
     */
    private boolean useTuLuyenDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("TU_LUYEN_DAN");
        int baseExp = (effect != null) ? effect.baseExp : 50;
        int expAmount = (int)(baseExp * mult);
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, expAmount);
        MessageUtils.send(player, "&5✦ Tu Luyện Đan: +" + expAmount + " EXP tu luyện! (x" + String.format("%.1f", mult) + ")");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Phi Thăng Đan - +EXP lớn (đọc từ config)
     */
    private boolean usePhiThangDan(Player player, PlayerCultivationData data, ItemStack item) {
        int currentRealm = data.getLevel() / 10;
        UUID uuid = player.getUniqueId();
        Integer lastUsedRealm = phiThangDanUsedRealms.get(uuid);

        if (lastUsedRealm != null && lastUsedRealm == currentRealm) {
            MessageUtils.send(player, "&cBạn đã dùng Phi Thăng Đan ở đại cảnh giới này rồi!");
            MessageUtils.send(player, "&cHãy thăng cấp lên đại cảnh giới tiếp theo để dùng tiếp.");
            return false;
        }

        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("PHI_THANG_DAN");
        int baseExp = (effect != null) ? effect.baseExp : 500;
        int expAmount = (int)(baseExp * mult);
        phiThangDanUsedRealms.put(uuid, currentRealm);
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, expAmount);
        MessageUtils.send(player, "&6✦ Phi Thăng Đan: +" + expAmount + " EXP tu luyện! (x" + String.format("%.1f", mult) + ")");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        return true;
    }

    /**
     * Bách Độc Đan - Miễn nhiễm độc (đọc từ config)
     */
    private boolean useBachDocDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("BACH_DOC_DAN");
        int baseDuration = (effect != null) ? effect.baseDuration : 300;
        int duration = (int)(baseDuration * mult);
        player.removePotionEffect(PotionEffectType.POISON);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, duration * 20, 0, false, true, true));
        MessageUtils.send(player, "&9✦ Bách Độc Đan: Miễn nhiễm độc &9" + duration + " &7giây! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Thiên Hồi Đan - Hồi HP + Linh lực (đọc từ config)
     */
    private boolean useThienHoiDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("THIEN_HOI_DAN");
        int baseHeal = (effect != null) ? effect.baseHeal : 50;
        int baseRecover = (effect != null) ? effect.baseRecover : 50;
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();
        double healPercent = Math.min(1.0, (baseHeal * mult) / 100.0);
        double healAmount = maxHealth * healPercent;
        player.setHealth(Math.min(maxHealth, currentHealth + healAmount));
        int manaHeal = (int)(data.getMaxMana() * (baseRecover * mult / 100.0));
        data.regenMana(manaHeal);
        MessageUtils.send(player, "&6✦ Thiên Hồi Đan: Hồi &a" + (int)(healPercent*100) + "% HP &7+ &b" + (int)(baseRecover * mult) + "% linh lực! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        return true;
    }

    /**
     * Phê Ma Đan - Tăng sát thương vs quái (đọc từ config)
     */
    private boolean usePheMaDan(Player player, PlayerCultivationData data, ItemStack item) {
        double mult = getGradeMultiplier(getPillGrade(item));
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect("PHE_MA_DAN");
        int baseDmg = (effect != null) ? effect.baseDmg : 30;
        int baseDuration = (effect != null) ? effect.baseDuration : 120;
        int level = Math.min(4, (int)(mult / 2));
        int bonusDmg = level * 10;
        int duration = (int)(baseDuration * mult);
        int totalDmg = baseDmg + bonusDmg;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, level + 1, false, true, true));
        pheMaDanExpiry.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 50L));
        MessageUtils.send(player, "&8✦ Phê Ma Đan: Tăng &c" + totalDmg + "% sát thương &7vs quái trong " + duration + " giây! (x" + String.format("%.1f", mult) + ")");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Trường Thọ Đan - Hồi sinh 1 lần (CD 1h)
     */
    private boolean useTruongThoDan(Player player, PlayerCultivationData data, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Long lastUsed = truongThoDanExpiry.get(uuid);
        long now = System.currentTimeMillis();

        if (lastUsed != null && now < lastUsed) {
            long remaining = (lastUsed - now) / 1000;
            MessageUtils.send(player, "&cTrường Thọ Đan đang trong thời gian hồi (" + remaining + "s)!");
            return false;
        }

        double mult = getGradeMultiplier(getPillGrade(item));
        long cdMs = (long)(3600000 / Math.min(mult, 10.0));
        truongThoDanExpiry.put(uuid, now + cdMs);
        MessageUtils.send(player, "&6✦ Trường Thọ Đan: Đã kích hoạt hồi sinh! &7(CD " + (cdMs/1000/60) + " phút)");
        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        return true;
    }

    /**
     * Kiểm tra Trường Thọ Đan có đang active không (gọi từ listener khác)
     */
    public boolean isTruongThoDanActive(UUID uuid) {
        Long expiry = truongThoDanExpiry.get(uuid);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    /**
     * Kích hoạt lại sau khi hồi sinh (reset cooldown)
     */
    public void resetTruongThoDan(UUID uuid) {
        truongThoDanExpiry.remove(uuid);
    }

    /**
     * Kiểm tra Phê Ma Đan có đang active không
     */
    public boolean isPheMaDanActive(UUID uuid) {
        Long expiry = pheMaDanExpiry.get(uuid);
        return expiry != null && System.currentTimeMillis() < expiry;
    }
}