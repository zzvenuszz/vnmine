package com.vnmine.item;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PillUseListener - Xử lý sử dụng đan dược khi click phải
 * Người chơi cầm đan dược trên tay và click phải để sử dụng
 */
public class PillUseListener implements Listener {

    private final VNMinePlugin plugin;

    // Cooldown sử dụng đan dược (tránh spam click)
    private final Map<UUID, Long> useCooldowns = new HashMap<>();
    private static final long USE_COOLDOWN_MS = 500; // 0.5 giây

    // Theo dõi Phi Thăng Đan đã dùng theo đại cảnh giới (level / 10)
    private final Map<UUID, Integer> phiThangDanUsedRealms = new HashMap<>();

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

        // Kiểm tra item có phải đan dược không
        String pillType = getPillType(item);
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

        // Xử lý tác dụng theo loại đan
        switch (pillType) {
            case "HOI_LINH_DAN":
                useHoiLinhDan(player, data);
                break;
            case "DAI_HOI_LINH_DAN":
                useDaiHoiLinhDan(player, data);
                break;
            case "CUONG_THE_DAN":
                useCuongTheDan(player, data);
                break;
            case "THANH_TAM_DAN":
                useThanhTamDan(player, data);
                break;
            case "TOC_THANH_DAN":
                useTocThanhDan(player, data);
                break;
            case "TU_LUYEN_DAN":
                useTuLuyenDan(player, data);
                break;
            case "PHI_THANG_DAN":
                usePhiThangDan(player, data);
                break;
        }
    }

    /**
     * Nhận diện loại đan dược dựa vào tên item
     */
    private String getPillType(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;

        String displayName = stripColor(meta.getDisplayName());

        if (displayName.contains("Hồi Linh Đan") && !displayName.contains("Đại")) {
            return "HOI_LINH_DAN";
        }
        if (displayName.contains("Đại Hồi Linh Đan")) {
            return "DAI_HOI_LINH_DAN";
        }
        if (displayName.contains("Cương Thể Đan")) {
            return "CUONG_THE_DAN";
        }
        if (displayName.contains("Thanh Tâm Đan")) {
            return "THANH_TAM_DAN";
        }
        if (displayName.contains("Tốc Thánh Đan")) {
            return "TOC_THANH_DAN";
        }
        if (displayName.contains("Tu Luyện Đan")) {
            return "TU_LUYEN_DAN";
        }
        if (displayName.contains("Phi Thăng Đan")) {
            return "PHI_THANG_DAN";
        }

        return null;
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    /**
     * Tiêu hao 1 đan dược (giảm số lượng hoặc xóa item)
     */
    private void consumePill(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand != null && hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        player.updateInventory();
    }

    // ==================== TÁC DỤNG CỦA TỪNG LOẠI ĐAN ====================

    /**
     * Hồi Linh Đan - Hồi 30 linh lực
     */
    private void useHoiLinhDan(Player player, PlayerCultivationData data) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return;
        }
        data.regenMana(30);
        consumePill(player);
        MessageUtils.send(player, "&a✦ Hồi Linh Đan: Hồi phục &b30 &alinh lực!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Đại Hồi Linh Đan - Hồi 100 linh lực + hiệu ứng hồi phục 30s
     */
    private void useDaiHoiLinhDan(Player player, PlayerCultivationData data) {
        if (data.getMana() >= data.getMaxMana()) {
            MessageUtils.send(player, "&cLinh lực đã đầy, không thể sử dụng!");
            return;
        }
        data.regenMana(100);
        // Thêm hiệu ứng tái tạo linh lực trong 30s (regeneration effect)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 0, false, true, true));
        consumePill(player);
        MessageUtils.send(player, "&b✦ Đại Hồi Linh Đan: Hồi phục &b100 &7linh lực + hồi phục 30s!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Cương Thể Đan - Tăng 20% sát thương trong 60 giây
     */
    private void useCuongTheDan(Player player, PlayerCultivationData data) {
        // Strength effect: cấp 0 = +20% sát thương gần
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0, false, true, true));
        consumePill(player);
        MessageUtils.send(player, "&c✦ Cương Thể Đan: Tăng &c20% sát thương &7trong 60 giây!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Thanh Tâm Đan - Giải trừ mọi trạng thái xấu
     */
    private void useThanhTamDan(Player player, PlayerCultivationData data) {
        // Xóa các hiệu ứng xấu
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
        consumePill(player);
        MessageUtils.send(player, "&a✦ Thanh Tâm Đan: Giải trừ mọi trạng thái xấu!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Tốc Thánh Đan - Tăng 50% tốc độ trong 30 giây
     */
    private void useTocThanhDan(Player player, PlayerCultivationData data) {
        // Speed effect: cấp 1 = +50% tốc độ
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1, false, true, true));
        consumePill(player);
        MessageUtils.send(player, "&b✦ Tốc Thánh Đan: Tăng &b50% tốc độ &7trong 30 giây!");
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Tu Luyện Đan - +50 EXP tu luyện
     */
    private void useTuLuyenDan(Player player, PlayerCultivationData data) {
        consumePill(player);
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, 50);
        MessageUtils.send(player, "&5✦ Tu Luyện Đan: +50 EXP tu luyện!");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }

    /**
     * Phi Thăng Đan - +500 EXP (1 lần/đại cảnh giới)
     * Đại cảnh giới = cấp / 10 (làm tròn xuống)
     */
    private void usePhiThangDan(Player player, PlayerCultivationData data) {
        int currentRealm = data.getLevel() / 10;
        UUID uuid = player.getUniqueId();
        Integer lastUsedRealm = phiThangDanUsedRealms.get(uuid);

        if (lastUsedRealm != null && lastUsedRealm == currentRealm) {
            MessageUtils.send(player, "&cBạn đã dùng Phi Thăng Đan ở đại cảnh giới này rồi!");
            MessageUtils.send(player, "&cHãy thăng cấp lên đại cảnh giới tiếp theo để dùng tiếp.");
            return;
        }

        consumePill(player);
        phiThangDanUsedRealms.put(uuid, currentRealm);
        int oldLevel = data.getLevel();
        plugin.getCultivationManager().addExperience(player, 500);
        MessageUtils.send(player, "&6✦ Phi Thăng Đan: +500 EXP!");
        if (data.getLevel() > oldLevel) {
            MessageUtils.send(player, "&d&l✦ THĂNG CẤP! Bạn đã lên cấp &e" + data.getLevel());
        }
        MessageUtils.playSound(player, Sound.ENTITY_GENERIC_DRINK);
    }
}