package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.CultivationManager;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.skill.SkillManager;
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
 * MainMenuGUI - Menu Inventory quản lý tất cả chức năng
 */
public class MainMenuGUI implements Listener {

    private final VNMinePlugin plugin;
    private final CultivationManager cultivationManager;
    private final SkillManager skillManager;

    private final Map<UUID, String> openMenus;
    private final Map<UUID, AlchemyCraftGUI> alchemyGUIs;
    private final Map<UUID, ArtifactCraftGUI> artifactGUIs;

    public MainMenuGUI(VNMinePlugin plugin, CultivationManager cultivationManager, 
                       SkillManager skillManager) {
        this.plugin = plugin;
        this.cultivationManager = cultivationManager;
        this.skillManager = skillManager;
        this.openMenus = new HashMap<>();
        this.alchemyGUIs = new HashMap<>();
        this.artifactGUIs = new HashMap<>();
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
                ColorUtils.colorize("&8✦ VNMine - Tu Tiên Giới ✦"));

        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        if (data == null) {
            data = cultivationManager.getOrCreatePlayerData(player.getUniqueId(), player.getName());
        }

        String realmPrefix = data.getRealmPrefix();
        double expPercent = data.getExpPercent();
        double manaPercent = data.getManaPercent();
        int level = data.getLevel();

        gui.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullOwner(player.getName())
                .setName("&b&l◆ Tu Vi Của Bạn ◆")
                .setLore(
                        "",
                        "&fNgười chơi: &e" + player.getName(),
                        "&fCảnh giới: " + realmPrefix + "&r&f]",
                        "&fCấp độ: &e" + level + " &7/ 100",
                        "&fTu vi: &e" + (int) data.getExperience() + " &7/ " + (int) data.getMaxExperience(),
                        "&fTiến độ: &a" + String.format("%.1f", expPercent) + "%",
                        "",
                        "&fLinh lực: &b" + data.getMana() + " &7/ " + data.getMaxMana(),
                        "&fThống kê:",
                        "&7  Sát: &f" + data.getMobsKilled() + " &7quái &8| &7Tinh anh: &f" + data.getElitesKilled(),
                        "&7  Boss: &f" + data.getBossesKilled() + " &8| &7Đan: &f" + data.getPillsCrafted(),
                        "",
                        "&eClick để xem chi tiết!"
                ).build());

        gui.setItem(12, new ItemBuilder(Material.BOOK)
                .setGlow(true)
                .setName("&d&l◆ Quản Lý Skill Bar ◆")
                .setLore(
                        "",
                        "&7Gán và sắp xếp kỹ năng vào",
                        "&7Skill Bar để thi triển nhanh",
                        "",
                        "&fSkill đã học: &e" + skillManager.getLearnedSkillCount(player),
                        "",
                        "&eClick để mở quản lý Skill Bar!"
                ).build());

        gui.setItem(14, new ItemBuilder(Material.BREWING_STAND)
                .setGlow(true)
                .setName("&a&l◆ Luyện Đan ◆")
                .setLore(
                        "",
                        "&7Luyện chế các loại đan dược",
                        "&7Nguyên liệu: Linh thảo + Phụ liệu",
                        "&eClick để mở lò luyện đan!"
                ).build());

        gui.setItem(16, new ItemBuilder(Material.ANVIL)
                .setGlow(true)
                .setName("&6&l◆ Luyện Chế Pháp Bảo ◆")
                .setLore(
                        "",
                        "&7Chế tạo và cường hóa pháp bảo",
                        "&7Nguyên liệu: Quặng quý + Linh thạch",
                        "&eClick để mở lò luyện chế!"
                ).build());

        gui.setItem(28, new ItemBuilder(Material.FARMLAND)
                .setName("&a&l◆ Linh Điền ◆")
                .setLore("", "&7Quản lý linh điền trồng linh thảo", "", "&eClick để mở!")
                .build());

        gui.setItem(30, new ItemBuilder(Material.OAK_SAPLING)
                .setName("&2&l◆ Linh Thảo & Hạt Giống ◆")
                .setLore("", "&7Xem kho linh thảo và hạt giống", "", "&eClick để mở!")
                .build());

        gui.setItem(32, new ItemBuilder(Material.POTION)
                .setName("&5&l◆ Đan Dược Đã Luyện ◆")
                .setLore("", "&7Xem các loại đan dược đang có", "", "&eClick để mở!")
                .build());

        gui.setItem(34, new ItemBuilder(Material.MAP)
                .setName("&6&l◆ Hướng Dẫn Tu Tiên ◆")
                .setLore(
                        "",
                        "&7Hướng dẫn chi tiết cách chơi",
                        "&7Các công thức luyện đan, luyện chế",
                        "&7Bảng cảnh giới, kỹ năng...",
                        "",
                        "&eClick để xem!"
                ).build());

        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }

        gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .setName("&c&lĐóng")
                .build());

        player.openInventory(gui);
        openMenus.put(player.getUniqueId(), "main");
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    public void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 10: openCultivationInfo(player); break;
            case 12:
                if (plugin.getSkillBarGUI() != null) {
                    plugin.getSkillBarGUI().openSkillManagement(player);
                } else {
                    MessageUtils.send(player, "&cHệ thống Skill Bar chưa được kích hoạt!");
                }
                break;
            case 14: openAlchemyMenu(player); break;
            case 16: openArtifactCraftMenu(player); break;
            case 28: MessageUtils.send(player, "&aTính năng Linh Điền đang phát triển..."); break;
            case 30: MessageUtils.send(player, "&aTính năng đang phát triển..."); break;
            case 32: MessageUtils.send(player, "&aTính năng đang phát triển..."); break;
            case 34: openGuide(player); break;
            case 49: player.closeInventory(); break;
        }
    }

    private void openCultivationInfo(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ColorUtils.colorize("&8✦ Tu Vi Chi Tiết ✦"));

        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        String realmPrefix = data.getRealmPrefix();

        gui.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullOwner(player.getName())
                .setName("&b&l" + player.getName())
                .setLore(
                        "",
                        "&fCảnh giới: " + realmPrefix + "&r&f]",
                        "&fCấp độ: &e" + data.getLevel() + " &7/ 100",
                        "&fTu vi: &e" + (int) data.getExperience() + " &7/ " + (int) data.getMaxExperience(),
                        "&fLinh lực: &b" + data.getMana() + " &7/ " + data.getMaxMana()
                ).build());

        gui.setItem(10, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("&a&lTu Vi (EXP)")
                .setLore(
                        "",
                        "&fTiến trình: &a" + String.format("%.1f", data.getExpPercent()) + "%",
                        "&fNguồn exp:",
                        "&7- Giết quái: +" + (int) cultivationManager.getExpKillMob() + " exp",
                        "&7- Giết tinh anh: +" + (int) cultivationManager.getExpKillElite() + " exp",
                        "&7- Giết boss: +" + (int) cultivationManager.getExpKillBoss() + " exp"
                ).build());

        gui.setItem(12, new ItemBuilder(Material.BLUE_DYE)
                .setName("&b&lLinh Lực (Mana)")
                .setLore(
                        "",
                        "&fLinh lực: &b" + data.getMana() + " &7/ " + data.getMaxMana(),
                        "&fHồi phục: &b2 &7linh lực mỗi 2 giây"
                ).build());

        gui.setItem(14, new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&c&lThống Kê Chiến Đấu")
                .setLore(
                        "",
                        "&fQuái thường: &e" + data.getMobsKilled(),
                        "&fTinh anh: &e" + data.getElitesKilled(),
                        "&fBoss: &e" + data.getBossesKilled(),
                        "&fĐan dược luyện: &e" + data.getPillsCrafted()
                ).build());

        gui.setItem(22, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build());

        // Nút Độ Kiếp - chỉ hiện khi cần (level 10,20,30...)
        if (data.isWaitingForTribulation()) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (data.isTribulationInProgress()) {
                lore.add(ColorUtils.colorize("&c⚡ ĐANG TRONG QUÁ TRÌNH ĐỘ KIẾP! ⚡"));
                lore.add(ColorUtils.colorize("&7Không thể thao tác lúc này!"));
            } else {
                int strikes = data.getLevel() / 10;
                lore.add(ColorUtils.colorize("&fSố đòn lôi kiếp: &e" + strikes + " đòn"));
                lore.add(ColorUtils.colorize("&fSát thương: &ctăng dần"));
                lore.add(ColorUtils.colorize("&fBán kính: &e" + String.format("%.1f", data.getLevel() * 1.5) + "m"));
                lore.add(ColorUtils.colorize("&7Yêu cầu: Ở nơi có thể thấy bầu trời"));
                lore.add("");
                lore.add(ColorUtils.colorize("&eClick để bắt đầu độ kiếp!"));
            }
            gui.setItem(16, new ItemBuilder(Material.END_CRYSTAL)
                    .setGlow(true)
                    .setName("&c&l✦ ĐỘ KIẾP ✦")
                    .setLore(lore.toArray(new String[0]))
                    .build());
        } else {
            gui.setItem(16, new ItemBuilder(Material.BARRIER)
                    .setName("&7&l✦ Độ Kiếp ✦")
                    .setLore(
                            "",
                            "&7Chưa cần độ kiếp",
                            "&7Sẽ yêu cầu ở các cấp: &e10, 20, 30, 40, 50, 60, 70, 80, 90"
                    ).build());
        }

        player.openInventory(gui);
        openMenus.put(player.getUniqueId(), "cultivation_info");
    }

    /**
     * Mở menu luyện đan - PUBLIC để VNMinePlugin gọi được
     */
    public void openAlchemyMenu(Player player) {
        AlchemyCraftGUI gui = alchemyGUIs.computeIfAbsent(player.getUniqueId(), 
                k -> new AlchemyCraftGUI(plugin, this));
        gui.open(player);
        // Note: openMenus.put is inside gui.open()'s player.openInventory(),
        // so we put it AFTER to avoid InventoryCloseEvent clearing it
        openMenus.put(player.getUniqueId(), "alchemy");
    }

    private void openArtifactCraftMenu(Player player) {
        ArtifactCraftGUI gui = artifactGUIs.computeIfAbsent(player.getUniqueId(),
                k -> new ArtifactCraftGUI(plugin, this));
        gui.open(player);
        // Note: openMenus.put is inside gui.open()'s player.openInventory(),
        // so we put it AFTER to avoid InventoryCloseEvent clearing it
        openMenus.put(player.getUniqueId(), "artifact");
    }

    private void openGuide(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ColorUtils.colorize("&8✦ Hướng Dẫn Tu Tiên ✦"));

        gui.setItem(10, new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&6&lGiới Thiệu")
                .setLore(
                        "",
                        "&7VNMine - Plugin Tu Tiên Huyền Huyễn",
                        "&7Mang phong cách tiên hiệp vào Minecraft",
                        "",
                        "&7Hệ thống:",
                        "&7- &aTu Luyện: Lên cấp, độ kiếp, đột phá",
                        "&7- &dCông Pháp: Học và thi triển kỹ năng",
                        "&7- &aLuyện Đan: Chế tạo đan dược",
                        "&7- &6Pháp Bảo: Chế tạo và sử dụng pháp bảo",
                        "&7- &cQuái Tinh Anh: Elite & boss",
                        "",
                        "&eLệnh: /vn để mở menu chính"
                ).build());

        gui.setItem(12, new ItemBuilder(Material.GOLD_INGOT)
                .setName("&6&lCảnh Giới & Cấp Bậc")
                .setLore(
                        "",
                        "&7Cứ 10 cấp là 1 đại cảnh giới:",
                        "&7Cấp 1-10: Khí Động", "&7Cấp 11-20: Luyện Khí",
                        "&7Cấp 21-30: Trúc Cơ", "&7Cấp 31-40: Kim Đan",
                        "&7Cấp 41-50: Nguyên Anh", "&7Cấp 51-60: Hóa Thần",
                        "&7Cấp 61-70: Hợp Thể", "&7Cấp 71-80: Độ Kiếp",
                        "&7Cấp 81-90: Đại Thừa", "&7Cấp 91-100: Phi Thăng",
                        "",
                        "&7Cấp chẵn (10,20,30...) sẽ gặp lôi kiếp!"
                ).build());

        gui.setItem(14, new ItemBuilder(Material.POTION)
                .setName("&a&lCông Thức Luyện Đan")
                .setLore(
                        "",
                        "&aHồi Linh Đan: &73 Linh Thảo + 1 Nước → Hồi 30 mana",
                        "&bĐại Hồi Linh Đan: &72 Hồi Linh Đan + 2 Huyết LT + 5 LT",
                        "&cCương Thể Đan: &73 Huyết LT + 5 LT + 1 Blaze",
                        "&5Tu Luyện Đan: &710 LT + 5 Huyết LT + 2 Long Huyết + 1 Vàng"
                ).build());

        gui.setItem(16, new ItemBuilder(Material.ANVIL)
                .setName("&6&lCông Thức Pháp Bảo")
                .setLore(
                        "",
                        "&bKiếm Phi Hành: &71 Kiếm DC + 8 DC + 4 Lông",
                        "&6Linh Chung: &71 Chuông + 4 Vàng + 2 DC",
                        "&5Bát Quái Kính: &71 Khiên + 4 Obsidian + 4 Ngọc",
                        "&6Phượng Hoàng Lệnh: &71 Lông + 8 Vàng + 4 Netherite + 1 Trứng Rồng"
                ).build());

        gui.setItem(18, new ItemBuilder(Material.CREEPER_HEAD)
                .setName("&c&lQuái Tinh Anh & Boss")
                .setLore(
                        "",
                        "&7Giết đủ quái → sinh Tinh Anh",
                        "&7Tỉ lệ 10% tinh anh thành Boss",
                        "&7HP x5-20, DMG x3-8, kỹ năng đặc biệt",
                        "&7Rơi item & exp gấp nhiều lần"
                ).build());

        gui.setItem(22, new ItemBuilder(Material.REDSTONE)
                .setName("&e&lLinh Lực (Spirit Power)")
                .setLore(
                        "",
                        "&7Dùng để thi triển công pháp,",
                        "&7sử dụng pháp bảo, ngự kiếm phi hành",
                        "&7Hồi phục tự động: +2 mỗi 2 giây",
                        "&7Max = 100 + (Cấp * 10)"
                ).build());

        gui.setItem(26, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build());

        player.openInventory(gui);
        openMenus.put(player.getUniqueId(), "guide");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String currentMenu = openMenus.get(player.getUniqueId());
        if (currentMenu == null) return;

        // Chỉ cancel khi click vào top inventory (GUI slots 0-53)
        // Cho phép click vào bottom inventory (kho đồ người chơi, slot >= 54)
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < 54) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            switch (currentMenu) {
                case "main":
                    handleMainMenuClick(player, slot);
                    break;
                case "cultivation_info":
                    if (slot == 22) {
                        openMainMenu(player);
                    } else if (slot == 16) {
                        // Nút Độ Kiếp
                        cultivationManager.startTribulation(player);
                        player.closeInventory();
                    }
                    break;
                case "guide":
                    if (slot == 26) openMainMenu(player);
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    public void cleanupPlayer(UUID uuid) {
        openMenus.remove(uuid);
        alchemyGUIs.remove(uuid);
        artifactGUIs.remove(uuid);
    }
}