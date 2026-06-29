package com.vnmine.item;

import com.vnmine.VNMinePlugin;
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

        plugin.getLogger().info("[PillDebug] " + player.getName() + " used " + pillType);

        // Xử lý tác dụng theo loại đan
        boolean consumed = false;
        switch (pillType) {
            case "HOI_LINH_DAN":
                consumed = useHoiLinhDan(player, data);
                break;
            case "DAI_HOI_LINH_DAN":
                consumed = useDaiHoiLinhDan(player, data);
                break;
            case "CUONG_THE_DAN":
                consumed = useCuongTheDan(player, data);
                break;
            case "THANH_TAM_DAN":
                consumed = useThanhTamDan(player, data);
                break;
            case "TOC_THANH_DAN":
                consumed = useTocThanhDan(player, data);
                break;
            case "TU_LUYEN_DAN":
                consumed = useTuLuyenDan(player, data);
                break;
            case "PHI_THANG_DAN":
                consumed = usePhiThangDan(player, data);
                break;
            case "BACH_DOC_DAN":
                consumed = useBachDocDan(player, data);
                break;
            case "THIEN_HOI_DAN":
                consumed = useThienHoiDan(player, data);
                break;
            case "PHE_MA_DAN":
                consumed = usePheMaDan(player, data);
                break;
            case "TRUONG_THO_DAN":
                consumed = useTruongThoDan(player, data);
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
     * Lấy số charge còn lại từ item
     */
    private int getCharges(ItemStack item) {
        String chargesStr = ItemBuilder.getPersistentData(item, KEY_PILL_CHARGES);
        if (chargesStr != null) {
            try {
                return Integer.parseInt(chargesStr);
            } catch (NumberFormatException e) {
                return 1; // fallback
            }
        }
        return 1; // item cũ không có charge data
    }

    /**
     * Tiêu hao 1 charge (giảm hoặc xóa item)
     */
    private void consumeCharge(Player player, ItemStack item) {
        int charges = getCharges(item);
        charges--;

        if (charges <= 0) {
            // Hết charge - xóa item
            player.getInventory().setItemInMainHand(null);
            MessageUtils.send(player, "&cLọ đan dược đã hết!");
        } else {
            // Còn charge - update persistent data
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(plugin, KEY_PILL_CHARGES);
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.valueOf(charges));

                // Cập nhật lore để hiển thị số lần còn lại
                List<String> lore = meta.getLore();
                if (lore != null) {
                    for (int i = 0; i < lore.size(); i++) {
                        if (lore.get(i).contains("Lượng dùng:")) {
                            lore.set(i, ColorUtils.colorize("&7Lượng dùng: &e" + charges + " &7lần"));
                            break;
                        }
                    }
                    meta.setLore(lore);
                }

                item.setItemMeta(meta);
                player.updateInventory();
            }
        }
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    // ==================== TÁC DỤNG CỦA TỪNG LOẠI ĐAN ====================
    // Trả về true nếu dùng thành công (để giảm charge)

    /**
     * Hồi Linh Đan - Hồi 30 linh lực
     */
    private boolean useHoiLinhDan(Player player, PlayerCultivationData data) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return false;
        }
        data.regenMana(30);
        MessageUtils.send(player, "&a✦ Hồi Linh Đan: Hồi phục &b30 &alinh lực!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Đại Hồi Linh Đan - Hồi 100 linh lực + hiệu ứng hồi phục 30s
     */
    private boolean useDaiHoiLinhDan(Player player, PlayerCultivationData data) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return false;
        }
        data.regenMana(100);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 0, false, true, true));
        MessageUtils.send(player, "&b✦ Đại Hồi Linh Đan: Hồi phục &b100 &7linh lực + hồi phục 30s!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Cương Thể Đan - Tăng 20% sát thương trong 60 giây
     */
    private boolean useCuongTheDan(Player player, PlayerCultivationData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0, false, true, true));
        MessageUtils.send(player, "&c✦ Cương Thể Đan: Tăng &c20% sát thương &7trong 60 giây!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Thanh Tâm Đan - Giải trừ mọi trạng thái xấu
     */
    private boolean useThanhTamDan(Player player, PlayerCultivationData data) {
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
     * Tốc Thánh Đan - Tăng 50% tốc độ trong 30 giây
     */
    private boolean useTocThanhDan(Player player, PlayerCultivationData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1, false, true, true));
        MessageUtils.send(player, "&b✦ Tốc Thánh Đan: Tăng &b50% tốc độ &7trong 30 giây!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Tu Luyện Đan - +50 EXP tu luyện
     */
    private boolean useTuLuyenDan(Player player, PlayerCultivationData data) {
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, 50);
        MessageUtils.send(player, "&5✦ Tu Luyện Đan: +50 EXP tu luyện!");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Phi Thăng Đan - +500 EXP (1 lần/đại cảnh giới)
     */
    private boolean usePhiThangDan(Player player, PlayerCultivationData data) {
        int currentRealm = data.getLevel() / 10;
        UUID uuid = player.getUniqueId();
        Integer lastUsedRealm = phiThangDanUsedRealms.get(uuid);

        if (lastUsedRealm != null && lastUsedRealm == currentRealm) {
            MessageUtils.send(player, "&cBạn đã dùng Phi Thăng Đan ở đại cảnh giới này rồi!");
            MessageUtils.send(player, "&cHãy thăng cấp lên đại cảnh giới tiếp theo để dùng tiếp.");
            return false;
        }

        phiThangDanUsedRealms.put(uuid, currentRealm);
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, 500);
        MessageUtils.send(player, "&6✦ Phi Thăng Đan: +500 EXP tu luyện!");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        return true;
    }

    /**
     * Bách Độc Đan - Miễn nhiễm độc 5 phút
     */
    private boolean useBachDocDan(Player player, PlayerCultivationData data) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 6000, 0, false, true, true));
        MessageUtils.send(player, "&9✦ Bách Độc Đan: Miễn nhiễm độc &95 &7phút!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Thiên Hồi Đan - Hồi 50% HP + 50% Linh lực
     */
    private boolean useThienHoiDan(Player player, PlayerCultivationData data) {
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();
        double healAmount = maxHealth * 0.5;
        player.setHealth(Math.min(maxHealth, currentHealth + healAmount));

        int manaHeal = data.getMaxMana() / 2;
        data.regenMana(manaHeal);

        MessageUtils.send(player, "&6✦ Thiên Hồi Đan: Hồi &a50% HP &7+ &b50% linh lực!");
        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        return true;
    }

    /**
     * Phê Ma Đan - Tăng 30% sát thương vs quái 2 phút
     */
    private boolean usePheMaDan(Player player, PlayerCultivationData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 2400, 1, false, true, true));
        pheMaDanExpiry.put(player.getUniqueId(), System.currentTimeMillis() + 120000);
        MessageUtils.send(player, "&8✦ Phê Ma Đan: Tăng &c30% sát thương &7vs quái trong 2 phút!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
        return true;
    }

    /**
     * Trường Thọ Đan - Hồi sinh 1 lần (CD 1h)
     */
    private boolean useTruongThoDan(Player player, PlayerCultivationData data) {
        UUID uuid = player.getUniqueId();
        Long lastUsed = truongThoDanExpiry.get(uuid);
        long now = System.currentTimeMillis();

        if (lastUsed != null && now < lastUsed) {
            long remaining = (lastUsed - now) / 1000;
            MessageUtils.send(player, "&cTrường Thọ Đan đang trong thời gian hồi (" + remaining + "s)!");
            return false;
        }

        truongThoDanExpiry.put(uuid, now + 3600000);
        MessageUtils.send(player, "&6✦ Trường Thọ Đan: Đã kích hoạt hồi sinh! &7(CD 1h)");
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