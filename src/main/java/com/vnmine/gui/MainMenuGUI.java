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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * MainMenuGUI - Menu Inventory quản lý tất cả chức năng
 * Sử dụng title-based detection để tránh conflict với các GUI khác
 */
public class MainMenuGUI implements Listener {

    private final VNMinePlugin plugin;
    private final CultivationManager cultivationManager;
    private final SkillManager skillManager;
    private final AdminMenuGUI adminMenuGUI;

    // Các title mà GUI này quản lý
    private static final String TITLE_MAIN = "VNMine - Tu Tiên Giới";
    private static final String TITLE_CULTIVATION = "Tu Vi Chi Tiết";
    private static final String TITLE_GUIDE = "Hướng Dẫn Tu Tiên";

    private final Map<UUID, AlchemyCraftGUI> alchemyGUIs;
    private final Map<UUID, ArtifactCraftGUI> artifactGUIs;

    public MainMenuGUI(VNMinePlugin plugin, CultivationManager cultivationManager, 
                       SkillManager skillManager, AdminMenuGUI adminMenuGUI) {
        this.plugin = plugin;
        this.cultivationManager = cultivationManager;
        this.skillManager = skillManager;
        this.adminMenuGUI = adminMenuGUI;
        this.alchemyGUIs = new HashMap<>();
        this.artifactGUIs = new HashMap<>();
    }

    /**
     * Kiểm tra inventory hiện tại có phải do GUI này quản lý không
     */
    private boolean isOwnInventory(InventoryClickEvent event) {
        String title = ColorUtils.stripColor(event.getView().getTitle());
        return title.contains(TITLE_MAIN) || title.contains(TITLE_CULTIVATION) || title.contains(TITLE_GUIDE);
    }

    public void openMainMenu(Player player) {
        plugin.getLogger().info("[MenuDebug] openMainMenu called for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, 54, 
                ColorUtils.colorize("&8✦ " + TITLE_MAIN + " ✦"));

        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        if (data == null) {
            data = cultivationManager.getOrCreatePlayerData(player.getUniqueId(), player.getName());
        }

        String realmPrefix = data.getRealmPrefix();
        double expPercent = data.getExpPercent();
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

        // Nút Admin Menu - chỉ hiển thị nếu có quyền
        if (hasAdminPermission(player)) {
            gui.setItem(40, new ItemBuilder(Material.COMMAND_BLOCK)
                    .setGlow(true)
                    .setName("&c&l◆ Admin Menu ◆")
                    .setLore(
                            "",
                            "&7Quản lý và lấy item test",
                            "&7(Phân theo nhóm: Tu Luyện, Pháp Bảo,",
                            "&7Công Pháp, Linh Thảo, Nguyên Liệu, Tọa Kỵ)",
                            "",
                            "&c&lChỉ dành cho Admin!"
                    ).build());
        }

        gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .setName("&c&lĐóng")
                .build());

        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    public void handleMainMenuClick(Player player, int slot) {
        plugin.getLogger().info("[MenuDebug] handleMainMenuClick slot=" + slot + " player=" + player.getName());
        switch (slot) {
            case 10: openCultivationInfo(player); break;
            case 12:
                if (plugin.getSkillBarGUI() != null) {
                    plugin.getSkillBarGUI().openSkillManagement(player);
                } else {
                    MessageUtils.send(player, "&cHệ thống Skill Bar chưa được kích hoạt!");
                }
                break;
            case 14:
                MessageUtils.send(player, "&a✦ Đang mở lò luyện đan...");
                openAlchemyMenu(player);
                break;
            case 16:
                openArtifactCraftMenu(player);
                break;
            case 28: MessageUtils.send(player, "&aTính năng Linh Điền đang phát triển..."); break;
            case 30: MessageUtils.send(player, "&aTính năng đang phát triển..."); break;
            case 32: MessageUtils.send(player, "&aTính năng đang phát triển..."); break;
            case 34: openGuide(player); break;
            case 40:
                if (hasAdminPermission(player)) {
                    cleanupPlayer(player.getUniqueId());
                    adminMenuGUI.open(player);
                }
                break;
            case 49: player.closeInventory(); break;
        }
    }

    private boolean hasAdminPermission(Player player) {
        com.vnmine.permission.PermissionManager permManager = plugin.getPermissionManager();
        if (permManager != null && permManager.isEnabled()) {
            return permManager.hasPermission(player, "vnmine.command.admin");
        }
        return player.isOp();
    }

    private void openCultivationInfo(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ColorUtils.colorize("&8✦ " + TITLE_CULTIVATION + " ✦"));

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
                            "&7Sẽ yêu cầu ở các cấp: &e9, 19, 29, 39, 49, 59, 69, 79, 89"
                    ).build());
        }

        player.openInventory(gui);
    }

    public void openAlchemyMenu(Player player) {
        AlchemyCraftGUI gui = alchemyGUIs.computeIfAbsent(player.getUniqueId(), 
                k -> new AlchemyCraftGUI(plugin, this));
        gui.open(player);
    }

    private void openArtifactCraftMenu(Player player) {
        ArtifactCraftGUI gui = artifactGUIs.computeIfAbsent(player.getUniqueId(),
                k -> new ArtifactCraftGUI(plugin, this));
        gui.open(player);
    }

    private void openGuide(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ColorUtils.colorize("&8✦ " + TITLE_GUIDE + " ✦"));

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
                        "&7Cứ 9 cấp là 1 đại cảnh giới:",
                        "&7Cấp 1-9: Khí Động", "&7Cấp 11-19: Luyện Khí",
                        "&7Cấp 21-29: Trúc Cơ", "&7Cấp 31-39: Kim Đan",
                        "&7Cấp 41-49: Nguyên Anh", "&7Cấp 51-59: Hóa Thần",
                        "&7Cấp 61-69: Hợp Thể", "&7Cấp 71-79: Độ Kiếp",
                        "&7Cấp 81-89: Đại Thừa", "&7Cấp 91-99: Phi Thăng",
                        "",
                        "&7Cấp lẻ (9,19,29...) sẽ gặp lôi kiếp!"
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
    }

    /**
     * Xử lý drag trong MainMenu - chặn mọi drag
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains(TITLE_MAIN) && !title.contains(TITLE_CULTIVATION) && !title.contains(TITLE_GUIDE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        if (!isOwnInventory(event)) return;

        Player player = (Player) event.getWhoClicked();
        
        // Chặn mọi click type đặc biệt (shift, double, drop, number key, v.v.)
        ClickType click = event.getClick();
        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ||
            click == ClickType.DOUBLE_CLICK || click == ClickType.DROP ||
            click == ClickType.CONTROL_DROP ||
            click == ClickType.NUMBER_KEY || click == ClickType.WINDOW_BORDER_LEFT ||
            click == ClickType.WINDOW_BORDER_RIGHT) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = ColorUtils.stripColor(event.getView().getTitle());
        plugin.getLogger().info("[MenuDebug] onInventoryClick title='" + title + "' slot=" + slot + " player=" + player.getName());

        if (title.contains(TITLE_MAIN)) {
            handleMainMenuClick(player, slot);
        } else if (title.contains(TITLE_CULTIVATION)) {
            if (slot == 22) {
                openMainMenu(player);
            } else if (slot == 16) {
                cultivationManager.startTribulation(player);
                player.closeInventory();
            }
        } else if (title.contains(TITLE_GUIDE)) {
            if (slot == 26) openMainMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        plugin.getLogger().info("[MenuDebug] onInventoryClose player=" + event.getPlayer().getName() + " title='" + title + "'");
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    public void cleanupPlayer(UUID uuid) {
        alchemyGUIs.remove(uuid);
        artifactGUIs.remove(uuid);
    }
}