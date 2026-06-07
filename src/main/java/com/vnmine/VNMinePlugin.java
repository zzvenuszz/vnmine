package com.vnmine;

import com.vnmine.command.GiveCommand;
import com.vnmine.cultivation.CultivationListener;
import com.vnmine.cultivation.CultivationManager;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.currency.CurrencyCommand;
import com.vnmine.currency.CurrencyListener;
import com.vnmine.currency.CurrencyManager;
import com.vnmine.drop.BlockDropListener;
import com.vnmine.drop.DropManager;
import com.vnmine.gui.AlchemyCraftGUI;
import com.vnmine.gui.ArtifactCraftGUI;
import com.vnmine.gui.MainMenuGUI;
import com.vnmine.mount.MountCommand;
import com.vnmine.mount.MountManager;
import com.vnmine.npc.NPCCommand;
import com.vnmine.npc.NPCListener;
import com.vnmine.npc.NPCManager;
import com.vnmine.npc.NPCShopGUI;
import com.vnmine.permission.PermissionCommand;
import com.vnmine.permission.PermissionManager;
import com.vnmine.skill.SkillManager;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import com.vnmine.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class VNMinePlugin extends JavaPlugin implements TabCompleter {

    private TimeManager timeManager;
    private int dayMinutes;
    private int nightMinutes;

    private PermissionManager permissionManager;
    private PermissionCommand permissionCommand;
    private WorldManager worldManager;
    private DropManager dropManager;
    private BlockDropListener blockDropListener;

    private CultivationManager cultivationManager;
    private CultivationListener cultivationListener;
    private SkillManager skillManager;
    private MainMenuGUI mainMenuGUI;
    private AlchemyCraftGUI alchemyCraftGUI;
    private ArtifactCraftGUI artifactCraftGUI;

    // === NEW SYSTEMS: NPC, CURRENCY, MOUNT ===
    private NPCManager npcManager;
    private NPCShopGUI npcShopGUI;
    private NPCListener npcListener;
    private NPCCommand npcCommand;
    private CurrencyManager currencyManager;
    private CurrencyListener currencyListener;
    private CurrencyCommand currencyCommand;
    private MountManager mountManager;
    private MountCommand mountCommand;
    private GiveCommand giveCommand;

    // === GETTERS ===
    public CultivationManager getCultivationManager() { return cultivationManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public MainMenuGUI getMainMenuGUI() { return mainMenuGUI; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public NPCManager getNPCManager() { return npcManager; }
    public MountManager getMountManager() { return mountManager; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        timeManager = new TimeManager();

        // Initialize systems
        permissionManager = new PermissionManager(this);
        permissionCommand = new PermissionCommand(permissionManager);
        worldManager = new WorldManager(this);
        dropManager = new DropManager(this);
        blockDropListener = new BlockDropListener(this, dropManager);

        cultivationManager = new CultivationManager(this);
        cultivationListener = new CultivationListener(this, cultivationManager);
        skillManager = new SkillManager(this);
        mainMenuGUI = new MainMenuGUI(this, cultivationManager, skillManager);
        alchemyCraftGUI = new AlchemyCraftGUI(this, mainMenuGUI);
        artifactCraftGUI = new ArtifactCraftGUI(this, mainMenuGUI);

        // Initialize NEW systems
        currencyManager = new CurrencyManager(this);
        currencyListener = new CurrencyListener(this, currencyManager);
        currencyCommand = new CurrencyCommand(this, currencyManager);

        npcManager = new NPCManager(this);
        npcShopGUI = new NPCShopGUI(this, npcManager);
        npcListener = new NPCListener(this, npcManager, npcShopGUI);
        npcCommand = new NPCCommand(this, npcManager);

        mountManager = new MountManager(this);
        mountCommand = new MountCommand(this, mountManager);

        giveCommand = new GiveCommand(this);

        // Load configs
        permissionManager.load();
        worldManager.load();
        dropManager.load();

        // Register events
        getServer().getPluginManager().registerEvents(blockDropListener, this);
        getServer().getPluginManager().registerEvents(cultivationListener, this);
        getServer().getPluginManager().registerEvents(mainMenuGUI, this);
        getServer().getPluginManager().registerEvents(alchemyCraftGUI, this);
        getServer().getPluginManager().registerEvents(artifactCraftGUI, this);
        getServer().getPluginManager().registerEvents(currencyListener, this);
        getServer().getPluginManager().registerEvents(npcListener, this);
        getServer().getPluginManager().registerEvents(npcShopGUI, this);

        // Register commands
        getCommand("vnmine").setExecutor(this);
        getCommand("vnmine").setTabCompleter(this);
        getCommand("vn").setExecutor(this);
        getCommand("vn").setTabCompleter(this);
        getCommand("vnitem").setExecutor(this);
        getCommand("vnitem").setTabCompleter(this);
        getCommand("vnskill").setExecutor(this);
        getCommand("vnskill").setTabCompleter(this);
        getCommand("vnalchemy").setExecutor(this);
        getCommand("vnalchemy").setTabCompleter(this);
        getCommand("vnfarm").setExecutor(this);
        getCommand("vnfarm").setTabCompleter(this);
        getCommand("vnnpc").setExecutor(npcCommand);
        getCommand("vngive").setExecutor(giveCommand);
        getCommand("mount").setExecutor(mountCommand);
        getCommand("vnbalance").setExecutor(currencyCommand);
        getCommand("vnpay").setExecutor(currencyCommand);

        getLogger().info(ColorUtils.colorize("&aVNMine v2.1.0 da duoc bat! &7Big Update Tu Tien"));
        getLogger().info(ColorUtils.colorize("&e✦ NPC, Linh Thach, Toa Ky da san sang! ✦"));
    }

    @Override
    public void onDisable() {
        if (timeManager != null) timeManager.stop();
        if (cultivationManager != null) cultivationManager.saveData();
        if (npcManager != null) npcManager.despawnAll();
        if (mountManager != null) mountManager.reload();
        getLogger().info("VNMine plugin da duoc tat!");
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        dayMinutes = config.getInt("day-minutes", 10);
        nightMinutes = config.getInt("night-minutes", 10);
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (permissionManager != null) return permissionManager.hasPermission(player, permission);
        return player.hasPermission(permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "tps": return handleTps(sender);
            case "save-all": return handleSaveAll(sender);
            case "vnmine":
            case "vn":
                return handleVnmine(sender, command, label, args);
            case "vnitem": return handleItemCommand(sender, args);
            case "vnskill": return handleSkillCommand(sender, args);
            case "vnalchemy":
                if (sender instanceof Player && mainMenuGUI != null) {
                    mainMenuGUI.openAlchemyMenu((Player) sender);
                }
                return true;
            case "vnfarm":
                sender.sendMessage("§6=== Linh Điền ===\n§eTính năng đang phát triển...");
                return true;
            default: return false;
        }
    }

    private boolean handleTps(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.tps")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền xem TPS!");
            return true;
        }
        double[] tps = Bukkit.getTPS();
        sender.sendMessage("§6=== Server TPS ===");
        sender.sendMessage("§f1 phút: " + getTpsColor(tps[0]) + String.format("%.2f", Math.min(20.0, tps[0])));
        sender.sendMessage("§f5 phút: " + getTpsColor(tps[1]) + String.format("%.2f", Math.min(20.0, tps[1])));
        sender.sendMessage("§f15 phút: " + getTpsColor(tps[2]) + String.format("%.2f", Math.min(20.0, tps[2])));
        return true;
    }

    private String getTpsColor(double tps) {
        if (tps >= 18.0) return "§a";
        if (tps >= 15.0) return "§e";
        return "§c";
    }

    private boolean handleSaveAll(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.saveall")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền!");
            return true;
        }
        sender.sendMessage("§6[VNMine] §aĐang lưu dữ liệu...");
        for (World world : Bukkit.getWorlds()) world.save();
        if (cultivationManager != null) cultivationManager.saveData();
        sender.sendMessage("§6[VNMine] §aĐã lưu toàn bộ dữ liệu!");
        return true;
    }

    private boolean handleVnmine(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "vnmine.command.vnmine")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền!");
            return true;
        }

        if (args.length < 1) {
            if (sender instanceof Player && mainMenuGUI != null) {
                mainMenuGUI.openMainMenu((Player) sender);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "menu":
            case "gui":
                if (sender instanceof Player && mainMenuGUI != null)
                    mainMenuGUI.openMainMenu((Player) sender);
                return true;
            case "start":
                return handleStart(sender);
            case "time": return handleTime(sender, args);
            case "perm":
                if (!sender.hasPermission("vnmine.perm.admin")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền!");
                    return true;
                }
                return permissionCommand.onCommand(sender, command, label, args);
            case "world": return handleWorld(sender, args);
            case "drop": return handleDrop(sender, args);
            case "cultivate":
            case "cultivation":
                return handleCultivationCommand(sender, args);
            case "elite": return handleEliteCommand(sender, args);
            case "reload": return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleStart(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Kiểm tra đã có dữ liệu chưa
        if (cultivationManager.getPlayerData(uuid) != null) {
            MessageUtils.send(player, "&cBạn đã bắt đầu tu tiên rồi! Dùng &e/vn &cđể mở menu.");
            return true;
        }

        // Tạo dữ liệu mới
        PlayerCultivationData data = cultivationManager.getOrCreatePlayerData(uuid, player.getName());

        // Thông báo
        MessageUtils.sendTitle(player, "&6&l✦ BẮT ĐẦU TU TIÊN ✦",
                "&fChào mừng đến với &eTu Tiên Giới&f!", 10, 60, 10);
        MessageUtils.send(player, "&a✦ Bạn đã bắt đầu hành trình tu tiên!");
        MessageUtils.send(player, "&a✦ Hiện tại: " + data.getRealmPrefix() + "&r&a]");
        MessageUtils.send(player, "&a✦ Dùng &e/vn &ađể mở menu chính.");
        MessageUtils.send(player, "&a✦ Dùng &e/vnskill &ađể xem công pháp.");
        MessageUtils.send(player, "&a✦ Dùng &e/vnalchemy &ađể luyện đan.");

        // Give starter items
        currencyManager.deposit(player, 10);
        MessageUtils.send(player, "&b✦ Khởi đầu: +10 Linh Thạch!");

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNMine v2.1.0 Help ===");
        sender.sendMessage("§e/vn §f- Mở menu chính");
        sender.sendMessage("§e/vn start §f- Bắt đầu tu tiên");
        sender.sendMessage("§e/vnskill §f- Công pháp/kỹ năng");
        sender.sendMessage("§e/vnalchemy §f- Luyện đan");
        sender.sendMessage("§e/vnitem list §f- Danh sách pháp bảo");
        sender.sendMessage("§e/mount list §f- Tọa kỵ phi hành");
        sender.sendMessage("§e/vnbalance §f- Xem linh thạch");
        sender.sendMessage("§e/vnmine elite §f- Quái tinh anh");
        sender.sendMessage("§e/vnmine reload §f- Reload config");
    }

    // ==================== CULTIVATION ====================
    private boolean handleCultivationCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6=== VNMine Tu Luyện ===");
            sender.sendMessage("§e/vnmine cultivate info §f- Xem thông tin tu vi");
            sender.sendMessage("§e/vnmine cultivate toggle §f- Bật/tắt hệ thống");
            if (sender instanceof Player && mainMenuGUI != null)
                mainMenuGUI.openMainMenu((Player) sender);
            return true;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "info":
                if (sender instanceof Player && cultivationManager != null) {
                    PlayerCultivationData data = cultivationManager.getPlayerData(((Player) sender).getUniqueId());
                    if (data != null) {
                        sender.sendMessage("§6=== Tu Vi Của Bạn ===");
                        sender.sendMessage("§fCảnh giới: " + data.getRealmPrefix() + "§r§f]");
                        sender.sendMessage("§fCấp độ: §e" + data.getLevel() + " §7/ 100");
                        sender.sendMessage("§fTu vi: §e" + (int) data.getExperience() + " §7/ " + (int) data.getMaxExperience());
                        sender.sendMessage("§fLinh lực: §b" + data.getMana() + " §7/ " + data.getMaxMana());
                    }
                }
                break;
            case "toggle":
                if (cultivationManager != null) {
                    boolean newState = !cultivationManager.isEnabled();
                    cultivationManager.setEnabled(newState);
                    getConfig().set("cultivation.enabled", newState);
                    saveConfig();
                    sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " hệ thống tu luyện!");
                }
                break;
            default:
                sender.sendMessage("§cSử dụng: /vnmine cultivate <info|toggle>");
        }
        return true;
    }

    // ==================== ELITE ====================
    private boolean handleEliteCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6=== VNMine Elite Mob ===");
            sender.sendMessage("§e/vnmine elite toggle §f- Bật/tắt");
            sender.sendMessage("§e/vnmine elite info §f- Xem trạng thái");
            return true;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "toggle":
                boolean eliteEnabled = !getConfig().getBoolean("elite-mob-settings.enabled", true);
                getConfig().set("elite-mob-settings.enabled", eliteEnabled);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (eliteEnabled ? "bật" : "tắt") + " elite mob system!");
                break;
            case "info":
                sender.sendMessage("§6=== Elite Mob Status ===");
                sender.sendMessage("§fHệ thống: " + (getConfig().getBoolean("elite-mob-settings.enabled", true) ? "§aBẬT" : "§cTẮT"));
                break;
            default:
                sender.sendMessage("§cSử dụng: /vnmine elite <toggle|info>");
        }
        return true;
    }

    // ==================== ITEM ====================
    private boolean handleItemCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "vnmine.command.vnmine")) {
            sender.sendMessage("§cBạn không có quyền!");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§6=== VNItem ===");
            sender.sendMessage("§e/vnitem toggle §f- Bật/tắt");
            sender.sendMessage("§e/vnitem reload §f- Reload config");
            sender.sendMessage("§e/vnitem list §f- Danh sách item");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "toggle":
                boolean itemEnabled = !getConfig().getBoolean("items.enabled", true);
                getConfig().set("items.enabled", itemEnabled);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (itemEnabled ? "bật" : "tắt") + " hệ thống item!");
                break;
            case "reload": reloadConfig(); sender.sendMessage("§6[VNMine] §aĐã reload config!"); break;
            case "list":
                sender.sendMessage("§6=== Danh Sách Item ===");
                sender.sendMessage("§b- &b&l◆ Kiếm Phi Hành ◆");
                sender.sendMessage("§6- &6&l◆ Linh Chung ◆");
                sender.sendMessage("§5- &5&l◆ Bát Quái Kính ◆");
                sender.sendMessage("§a- &a&l◆ Hồn Ngọc ◆");
                sender.sendMessage("§4- &4&l◆ Thiên Linh Thuẫn ◆");
                sender.sendMessage("§e- &e&l◆ Lôi Ấn ◆");
                sender.sendMessage("§6- &6&l◆ Phượng Hoàng Lệnh ◆");
                break;
            default: sender.sendMessage("§cSử dụng: /vnitem <toggle|reload|list>");
        }
        return true;
    }

    // ==================== SKILL ====================
    private boolean handleSkillCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player && skillManager != null) skillManager.openSkillMenu((Player) sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "toggle":
                if (skillManager != null) {
                    skillManager.setEnabled(!skillManager.isEnabled());
                    getConfig().set("skills.enabled", skillManager.isEnabled());
                    saveConfig();
                    sender.sendMessage("§6[VNMine] §aĐã " + (skillManager.isEnabled() ? "bật" : "tắt") + " công pháp!");
                }
                break;
            case "reload":
                if (skillManager != null) { skillManager.reload(); sender.sendMessage("§6[VNMine] §aĐã reload công pháp!"); }
                break;
            case "my":
                if (sender instanceof Player && skillManager != null) skillManager.openSkillMenu((Player) sender);
                break;
            default: sender.sendMessage("§cSử dụng: /vnskill <toggle|reload|my>");
        }
        return true;
    }

    // ==================== TIME ====================
    private boolean handleTime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time <set day <phut>|set night <phut>|on|off|status>");
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "set":
                if (args.length < 4) { sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time set <day|night> <phut>"); return true; }
                handleSet(sender, args[2].toLowerCase(), args[3]);
                break;
            case "on": handleOn(sender); break;
            case "off": handleOff(sender); break;
            case "status": handleStatus(sender); break;
            default: sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time <set day <phut>|set night <phut>|on|off|status>");
        }
        return true;
    }

    private void handleSet(CommandSender sender, String type, String minutesStr) {
        if (!hasPermission(sender, "vnmine.time.set")) { sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return; }
        int minutes;
        try { minutes = Integer.parseInt(minutesStr); if (minutes < 1) { sender.sendMessage("§6[VNMine] §cThoi gian phai lon hon 0 phut!"); return; } }
        catch (NumberFormatException e) { sender.sendMessage("§6[VNMine] §cThoi gian phai la so nguyen duong!"); return; }
        if (type.equals("day")) { dayMinutes = minutes; getConfig().set("day-minutes", minutes); saveConfig(); if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes); sender.sendMessage("§6[VNMine] §aDa set ban ngay thanh §e" + minutes + " §aphut!"); }
        else if (type.equals("night")) { nightMinutes = minutes; getConfig().set("night-minutes", minutes); saveConfig(); if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes); sender.sendMessage("§6[VNMine] §aDa set ban dem thanh §e" + minutes + " §aphut!"); }
        else sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time set <day|night> <phut>");
    }

    private void handleOn(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) { sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return; }
        if (timeManager.isRunning()) { sender.sendMessage("§6[VNMine] §eCustom time cycle dang chay roi!"); return; }
        timeManager.start(dayMinutes, nightMinutes);
        sender.sendMessage("§6[VNMine] §aDa bat custom time cycle!");
    }

    private void handleOff(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) { sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return; }
        if (!timeManager.isRunning()) { sender.sendMessage("§6[VNMine] §eCustom time cycle chua duoc bat!"); return; }
        timeManager.stop();
        sender.sendMessage("§6[VNMine] §aDa tat custom time cycle!");
    }

    private void handleStatus(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.status")) { sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return; }
        boolean active = timeManager.isRunning();
        sender.sendMessage("§6=== VNMine Time Status ===");
        sender.sendMessage("§fTrang thai: " + (active ? "§aBAT" : "§cTAT"));
        sender.sendMessage("§fBan ngay: §e" + dayMinutes + " §fphut | Ban dem: §e" + nightMinutes + " §fphut");
    }

    // ==================== WORLD ====================
    private boolean handleWorld(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world <gen <world>|toggle>"); return true; }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "gen":
                if (!sender.hasPermission("vnmine.world.gen")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                if (args.length < 3) { sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world gen <world>"); return true; }
                boolean created = worldManager.generateWorld(args[2]);
                sender.sendMessage("§6[VNMine] " + (created ? "§aĐã tạo" : "§eĐã tồn tại") + " world!");
                break;
            case "toggle":
                if (!sender.hasPermission("vnmine.world.toggle")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                boolean newState = !worldManager.isEnabled();
                worldManager.setEnabled(newState);
                getConfig().set("world-settings.enabled", newState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " world generation!");
                break;
            default: sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world <gen <world>|toggle>");
        }
        return true;
    }

    // ==================== DROP ====================
    private boolean handleDrop(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop <toggle|status|replace toggle|break toggle|explode toggle>"); return true; }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "toggle": {
                if (!sender.hasPermission("vnmine.drop.toggle")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                boolean newState = !dropManager.isEnabled();
                dropManager.setEnabled(newState);
                getConfig().set("block-drop-settings.enabled", newState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " block drop!");
                break;
            }
            case "status": {
                if (!sender.hasPermission("vnmine.drop.status")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                sender.sendMessage("§6=== Block Drop Status ===");
                sender.sendMessage("§fHệ thống: " + (dropManager.isEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fReplace: " + (dropManager.isReplaceEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fBreak: " + (dropManager.isBreakEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fExplode: " + (dropManager.isExplodeEnabled() ? "§aBẬT" : "§cTẮT"));
                break;
            }
            default: sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop <toggle|status|replace toggle|break toggle|explode toggle>");
        }
        return true;
    }

    // ==================== RELOAD ====================
    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.reload")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền reload!"); return true; }
        reloadConfig();
        loadConfig();
        permissionManager.load();
        worldManager.load();
        dropManager.load();
        if (cultivationManager != null) cultivationManager.reload();
        if (skillManager != null) skillManager.reload();
        if (currencyManager != null) currencyManager.loadConfig();
        if (npcManager != null) npcManager.reload();
        if (mountManager != null) mountManager.reload();
        if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes);
        sender.sendMessage("§6[VNMine] §aĐã reload toàn bộ config thành công!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();
        if (cmd.equals("vnmine") || cmd.equals("vn")) {
            if (args.length == 1) completions.addAll(Arrays.asList("menu", "start", "time", "perm", "world", "drop", "cultivate", "elite", "reload"));
        }
        return completions;
    }

    // ==================== TimeManager ====================
    private class TimeManager {
        private BukkitRunnable task;
        private boolean running = false;
        private double daySpeed, nightSpeed, fractionalTime = 0;
        private static final int FULL_CYCLE = 24000;
        private static final int DAY_TICKS = 12000;

        public void start(int dayMin, int nightMin) {
            if (running) return;
            updateSpeeds(dayMin, nightMin);
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            task = new BukkitRunnable() { @Override public void run() { tick(); } };
            task.runTaskTimer(VNMinePlugin.this, 0L, 1L);
            running = true;
        }
        public void stop() {
            if (!running) return;
            running = false;
            if (task != null) { task.cancel(); task = null; }
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            fractionalTime = 0;
        }
        public boolean isRunning() { return running; }
        public void updateSpeeds(int dayMin, int nightMin) {
            daySpeed = (double) DAY_TICKS / (dayMin * 60.0 * 20.0);
            nightSpeed = (double) (FULL_CYCLE - DAY_TICKS) / (nightMin * 60.0 * 20.0);
        }
        private void tick() {
            World w = Bukkit.getWorlds().get(0);
            if (w == null) return;
            long t = w.getFullTime();
            fractionalTime += (t % FULL_CYCLE < DAY_TICKS) ? daySpeed : nightSpeed;
            long add = (long) fractionalTime;
            if (add > 0) { fractionalTime -= add; w.setFullTime(t + add); }
        }
    }
}