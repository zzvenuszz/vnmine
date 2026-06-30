package com.vnmine.npc;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.npc.NPCData.NPCTrade;
import com.vnmine.spiritfarm.SpiritHerb;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * NPCShopGUI - GUI shop của NPC
 */
public class NPCShopGUI implements Listener {

    private final VNMinePlugin plugin;
    private final NPCManager npcManager;

    // Map: player UUID -> instance ID của NPC đang mở shop
    private final Map<UUID, String> openShops;
    private final Map<UUID, Map<String, Long>> tradeCooldowns;

    public NPCShopGUI(VNMinePlugin plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.openShops = new HashMap<>();
        this.tradeCooldowns = new HashMap<>();
    }

    /**
     * Mở shop NPC cho người chơi
     * @param player người chơi
     * @param instanceId ID của NPC instance
     */
    public void openShop(Player player, String instanceId) {
        NPCData data = npcManager.getNPCDataFromInstance(instanceId);
        if (data == null) return;

        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ " + ColorUtils.stripColor(data.getName()) + " ✦"));

        // Danh sách item
        int slot = 9;
        for (NPCTrade trade : data.getTrades()) {
            if (slot >= 54) break;

            Material iconMat = Material.getMaterial(trade.getMaterial());
            if (iconMat == null) iconMat = Material.STONE;

            List<String> lore = new ArrayList<>(ColorUtils.colorize(trade.getLore()));
            lore.add("");
            lore.add("&fGiá: &e" + trade.getPriceAmount() + " " + getPriceName(trade.getPriceMaterial()));

            if (trade.getStock() > 0) {
                lore.add("&fTồn kho: &e" + trade.getStock());
            } else {
                lore.add("&fTồn kho: &aVô hạn");
            }

            if (trade.getCooldownSeconds() > 0) {
                UUID uuid = player.getUniqueId();
                Map<String, Long> cooldowns = tradeCooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
                long lastBuy = cooldowns.getOrDefault(trade.getId(), 0L);
                long timeLeft = (lastBuy + (trade.getCooldownSeconds() * 1000L)) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    lore.add("&cHồi chiêu: " + (timeLeft / 1000) + "s");
                }
            }

            lore.add("");
            lore.add("&eClick để mua!");

            gui.setItem(slot, new ItemBuilder(iconMat)
                    .setName(trade.getDisplayName().isEmpty() ? 
                            ColorUtils.colorize("&f" + trade.getId()) : 
                            ColorUtils.colorize(trade.getDisplayName()))
                    .setAmount(trade.getAmount())
                    .setLore(lore)
                    .build());
            slot++;
        }

        // Viền
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }

        gui.setItem(49, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Đóng")
                .build());

        openShops.put(player.getUniqueId(), instanceId);
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_CHEST_OPEN);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String instanceId = openShops.get(player.getUniqueId());
        if (instanceId == null) return;

        // Chỉ cancel khi click vào top inventory (GUI slots 0-53)
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < 54) {
            event.setCancelled(true);

            if (slot == 49) {
                player.closeInventory();
                return;
            }

            if (slot < 9 || slot >= 54) return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            // Lấy NPCData từ instance ID
            NPCData data = npcManager.getNPCDataFromInstance(instanceId);
            if (data == null) return;

            int index = slot - 9;
            List<NPCTrade> trades = data.getTrades();
            if (index >= trades.size()) return;

            NPCTrade trade = trades.get(index);
            handlePurchase(player, trade);
        }
    }

    private void handlePurchase(Player player, NPCTrade trade) {
        // Kiểm tra cooldown
        UUID uuid = player.getUniqueId();
        Map<String, Long> cooldowns = tradeCooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        long lastBuy = cooldowns.getOrDefault(trade.getId(), 0L);
        long timeLeft = (lastBuy + (trade.getCooldownSeconds() * 1000L)) - System.currentTimeMillis();
        if (timeLeft > 0) {
            MessageUtils.send(player, "&cVui lòng chờ &e" + (timeLeft / 1000) + " &cgiây để mua lại!");
            return;
        }

        // Kiểm tra nguyên liệu trả giá
        Material priceMat = Material.getMaterial(trade.getPriceMaterial());
        if (priceMat == null) {
            MessageUtils.send(player, "&cLỗi: Vật phẩm thanh toán không hợp lệ!");
            return;
        }

        if (!hasItems(player, priceMat, trade.getPriceAmount())) {
            MessageUtils.send(player, "&cBạn không có đủ &e" + trade.getPriceAmount() + " " + 
                    getPriceName(trade.getPriceMaterial()) + "&c!");
            return;
        }

        // Trừ nguyên liệu
        removeItems(player, priceMat, trade.getPriceAmount());

        // Xử lý theo loại
        switch (trade.getType().toUpperCase()) {
            case "SKILL":
                giveSkill(player, trade);
                break;
            case "ARTIFACT":
                giveArtifact(player, trade);
                break;
            case "PILL":
                givePill(player, trade);
                break;
            case "CURRENCY_BUY":
                handleCurrencyBuy(player, trade);
                break;
            case "CURRENCY_SELL":
                handleCurrencySell(player, trade);
                break;
            case "HERB":
                giveHerb(player, trade);
                break;
            default:
                giveItem(player, trade);
        }

        // Set cooldown
        if (trade.getCooldownSeconds() > 0) {
            cooldowns.put(trade.getId(), System.currentTimeMillis());
        }

        MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
    }

    private void giveSkill(Player player, NPCTrade trade) {
        String skillId = trade.getSkillId();
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        if (data.hasLearnedSkill(skillId)) {
            MessageUtils.send(player, "&cBạn đã học kỹ năng này rồi!");
            return;
        }
        data.learnSkill(skillId);
        MessageUtils.send(player, "&a✦ Bạn đã học được công pháp mới!");
        // Reload shop để cập nhật
        String instanceId = openShops.get(player.getUniqueId());
        if (instanceId != null) {
            openShop(player, instanceId);
        }
    }

    private void giveArtifact(Player player, NPCTrade trade) {
        String itemId = trade.getItemId();
        ItemStack artifact = createArtifactItem(itemId);
        if (artifact != null) {
            player.getInventory().addItem(artifact);
            MessageUtils.send(player, "&a✦ Bạn đã mua &e" + ColorUtils.stripColor(trade.getDisplayName()));
        } else {
            MessageUtils.send(player, "&cPháp bảo '" + itemId + "' không tồn tại!");
        }
    }

    private void givePill(Player player, NPCTrade trade) {
        Material mat = Material.getMaterial(trade.getMaterial());
        if (mat == null) mat = Material.GLOWSTONE_DUST;
        ItemStack pill = new ItemBuilder(mat)
                .setName(trade.getDisplayName())
                .setAmount(trade.getAmount())
                .setLore(trade.getLore())
                .setGlow(true)
                .build();
        player.getInventory().addItem(pill);
        MessageUtils.send(player, "&a✦ Bạn đã mua &e" + trade.getAmount() + "x " + 
                ColorUtils.stripColor(trade.getDisplayName()));
    }

    private void handleCurrencyBuy(Player player, NPCTrade trade) {
        Material sellMat = Material.getMaterial(trade.getMaterial());
        if (sellMat == null) return;
        if (!hasItems(player, sellMat, trade.getAmount())) {
            MessageUtils.send(player, "&cBạn không có đủ &e" + trade.getAmount() + " " + 
                    trade.getMaterial());
            return;
        }
        removeItems(player, sellMat, trade.getAmount());
        plugin.getCurrencyManager().deposit(player, trade.getPriceAmount());
        MessageUtils.send(player, "&a✦ Bạn đã bán và nhận &b" + trade.getPriceAmount() + " Linh Thạch");
    }

    private void handleCurrencySell(Player player, NPCTrade trade) {
        if (!plugin.getCurrencyManager().withdraw(player, trade.getPriceAmount())) {
            MessageUtils.send(player, "&cBạn không có đủ Linh Thạch!");
            return;
        }
        givePill(player, trade);
        MessageUtils.send(player, "&a✦ Bạn đã mua bằng &b" + trade.getPriceAmount() + " Linh Thạch");
    }

    private void giveItem(Player player, NPCTrade trade) {
        Material mat = Material.getMaterial(trade.getMaterial());
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemBuilder(mat)
                .setName(trade.getDisplayName())
                .setAmount(trade.getAmount())
                .build();
        player.getInventory().addItem(item);
    }

    /**
     * Give linh thảo cho người chơi từ NPC shop
     * Tạo item qua SpiritHerb.createHerbItem() để có persistent data đầy đủ
     */
    private void giveHerb(Player player, NPCTrade trade) {
        String herbId = trade.getItemId();
        if (herbId == null || herbId.isEmpty()) {
            // Fallback: dùng material name làm herbId
            String matName = trade.getMaterial();
            for (Map.Entry<String, SpiritHerb> entry : SpiritHerb.getAllHerbs().entrySet()) {
                if (entry.getValue().getMaterial().name().equalsIgnoreCase(matName)) {
                    herbId = entry.getKey();
                    break;
                }
            }
        }

        SpiritHerb herb = SpiritHerb.getHerb(herbId);
        if (herb == null) {
            MessageUtils.send(player, "&cLinh thảo '" + herbId + "' không tồn tại!");
            return;
        }

        // Mặc định 10 Năm tuổi
        SpiritHerb.HerbQuality quality = SpiritHerb.HerbQuality.MUOI_NAM;
        ItemStack herbItem = herb.createHerbItem(quality, trade.getAmount());
        player.getInventory().addItem(herbItem);
        MessageUtils.send(player, "&a✦ Bạn đã mua &e" + trade.getAmount() + "x " +
                ColorUtils.stripColor(trade.getDisplayName()));
    }

    private ItemStack createArtifactItem(String itemId) {
        switch (itemId.toUpperCase()) {
            case "FLYING_SWORD":
                return new ItemBuilder(Material.DIAMOND_SWORD)
                        .setName("&b&l◆ Kiếm Phi Hành ◆").setGlow(true)
                        .setLore("", "&7Click phải để ngự kiếm phi hành", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "SPIRIT_BELL":
                return new ItemBuilder(Material.BELL)
                        .setName("&6&l◆ Linh Chung ◆").setGlow(true)
                        .setLore("", "&7Click phải: Làm choáng quái AOE", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "BAGUA_MIRROR":
                return new ItemBuilder(Material.SHIELD)
                        .setName("&5&l◆ Bát Quái Kính ◆").setGlow(true)
                        .setLore("", "&7Cầm tay: Giảm 30% sát thương", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "SOUL_JADE":
                return new ItemBuilder(Material.EMERALD)
                        .setName("&a&l◆ Hồn Ngọc ◆").setGlow(true)
                        .setLore("", "&7Tự động hồi 50% máu khi HP<20%", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "HEAVEN_SHIELD":
                return new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                        .setName("&4&l◆ Thiên Linh Thuẫn ◆").setGlow(true)
                        .setLore("", "&7Kích hoạt: Bất tử 5 giây", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "THUNDER_SEAL":
                return new ItemBuilder(Material.TRIDENT)
                        .setName("&e&l◆ Lôi Ấn ◆").setGlow(true)
                        .setLore("", "&7Click quái: Gọi sét đánh", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            case "PHOENIX_REBIRTH":
                return new ItemBuilder(Material.FEATHER)
                        .setName("&6&l◆ Phượng Hoàng Lệnh ◆").setGlow(true)
                        .setLore("", "&7Tự động hồi sinh 1 lần", "", "&6&l✦ Pháp bảo thượng phẩm ✦")
                        .build();
            default:
                return null;
        }
    }

    private boolean hasItems(Player player, Material material, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    break;
                }
                if (remaining <= 0) break;
            }
        }
    }

    private String getPriceName(String material) {
        switch (material.toUpperCase()) {
            case "EMERALD": return "Ngọc Lục Bảo";
            case "DIAMOND": return "Kim Cương";
            case "GOLD_INGOT": return "Vàng";
            default: return material;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openShops.remove(event.getPlayer().getUniqueId());
    }
}