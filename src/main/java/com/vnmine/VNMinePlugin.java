package com.vnmine;

import com.vnmine.cultivation.CultivationListener;
import com.vnmine.cultivation.CultivationManager;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.drop.BlockDropListener;
import com.vnmine.drop.DropManager;
import com.vnmine.gui.AlchemyCraftGUI;
import com.vnmine.gui.ArtifactCraftGUI;
import com.vnmine.gui.MainMenuGUI;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VNMinePlugin extends JavaPlugin implements TabCompleter {

    private TimeManager timeManager;
    private int dayMinutes;
    private int nightMinutes;

    // New systems
    private PermissionManager permissionManager;
    private PermissionCommand permissionCommand;
    private WorldManager worldManager;
    private DropManager dropManager;
    private BlockDropListener blockDropListener;

    // === NEW CULTIVATION SYSTEMS ===
    private CultivationManager cultivationManager;
    private CultivationListener cultivationListener;
    private SkillManager skillManager;
    private MainMenuGUI mainMenuGUI;
    private AlchemyCraftGUI alchemyCraftGUI;
    private ArtifactCraftGUI artifactCraftGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        // Initialize time manager (existing)
        timeManager = new TimeManager();

        // Initialize new systems
        permissionManager = new PermissionManager(this);
        permissionCommand = new PermissionCommand(permissionManager);
        worldManager = new WorldManager(this);
        dropManager = new DropManager(this);
        blockDropListener = new BlockDropListener(this, dropManager);

        // === INITIALIZE CULTIVATION SYSTEMS ===
        cultivationManager = new CultivationManager(this);
        cultivationListener = new CultivationListener(this, cultivationManager);
        skillManager = new SkillManager(this);
        mainMenuGUI = new MainMenuGUI(this, cultivationManager, skillManager);
        alchemyCraftGUI = new AlchemyCraftGUI(this, mainMenuGUI);
        artifactCraftGUI = new ArtifactCraftGUI(this, mainMenuGUI);

        // Load all configurations
        permissionManager.load();
        worldManager.load();
        dropManager.load();

        // Register events
        getServer().getPluginManager().registerEvents(blockDropListener, this);
        getServer().getPluginManager().registerEvents(cultivationListener, this);
        getServer().getPluginManager().registerEvents(mainMenuGUI, this);
        getServer().getPluginManager().registerEvents(alchemyCraftGUI, this);
        getServer().getPluginManager().registerEvents(artifactCraftGUI, this);

        // Set command executor
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

        getLogger().info(ColorUtils.colorize("&aVNMine plugin da duoc bat! &7Version " + getDescription().getVersion()));
        getLogger().info(ColorUtils.colorize("&e✦ Hệ thống Tu Tiên đã được kích hoạt! ✦"));
    }

    @Override
    public void onDisable() {
        if (timeManager != null) {
            timeManager.stop();
        }

        // Save cultivation data
        if (cultivationManager != null) {
            cultivationManager.saveData();
        }

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
        if (permissionManager != null) {
            return permissionManager.hasPermission(player, permission);
        }
        return player.hasPermission(permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "tps":
                return handleTps(sender);
            case "save-all":
                return handleSaveAll(sender);
            case "vnmine":
            case "vn":
                return handleVnmine(sender, command, label, args);
            case "vnitem":
                return handleItemCommand(sender, args);
            case "vnskill":
                return handleSkillCommand(sender, args);
            case "vnalchemy":
                return handleAlchemyCommand(sender, args);
            case "vnfarm":
                return handleFarmCommand(sender, args);
            default:
                return false;
        }
    }

    // ==================== TPS ====================
    private boolean handleTps(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.tps")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền xem TPS!");
            return true;
        }
        double[] tps = Bukkit.getTPS();
        double tps1m = Math.min(20.0, tps[0]);
        double tps5m = Math.min(20.0, tps[1]);
        double tps15m = Math.min(20.0, tps[2]);
        sender.sendMessage("§6=== Server TPS ===");
        sender.sendMessage("§f1 phút: " + getTpsColor(tps1m) + String.format("%.2f", tps1m));
        sender.sendMessage("§f5 phút: " + getTpsColor(tps5m) + String.format("%.2f", tps5m));
        sender.sendMessage("§f15 phút: " + getTpsColor(tps15m) + String.format("%.2f", tps15m));
        return true;
    }

    private String getTpsColor(double tps) {
        if (tps >= 18.0) return "§a";
        if (tps >= 15.0) return "§e";
        return "§c";
    }

    // ==================== SAVE ALL ====================
    private boolean handleSaveAll(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.saveall")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        sender.sendMessage("§6[VNMine] §aĐang lưu dữ liệu...");
        for (World world : Bukkit.getWorlds()) {
            world.save();
        }
        if (cultivationManager != null) {
            cultivationManager.saveData();
        }
        sender.sendMessage("§6[VNMine] §aĐã lưu toàn bộ dữ liệu!");
        return true;
    }

    // ==================== VNMINE MAIN COMMAND ====================
    private boolean handleVnmine(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "vnmine.command.vnmine")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length < 1) {
            // Mở menu chính nếu là player
            if (sender instanceof Player && mainMenuGUI != null) {
                mainMenuGUI.openMainMenu((Player) sender);
            } else {
                sendMainHelp(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "menu":
            case "gui":
                if (sender instanceof Player && mainMenuGUI != null) {
                    mainMenuGUI.openMainMenu((Player) sender);
                }
                break;
            case "time":
                return handleTime(sender, args);
            case "perm":
                if (!sender.hasPermission("vnmine.perm.admin")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                return permissionCommand.onCommand(sender, command, label, args);
            case "world":
                return handleWorld(sender, args);
            case "drop":
                return handleDrop(sender, args);
            case "cultivate":
            case "cultivation":
                return handleCultivationCommand(sender, args);
            case "elite":
                return handleEliteCommand(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendMainHelp(sender);
                return true;
        }
        return true;
    }

    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNMine Plugin Help ===");
        sender.sendMessage("§e/vn §f- Mở menu chính");
        sender.sendMessage("§e/vnmine time ... §f- Quản lý thời gian");
        sender.sendMessage("§e/vnmine perm ... §f- Quản lý phân quyền");
        sender.sendMessage("§e/vnmine cultivate §f- Hệ thống tu luyện");
        sender.sendMessage("§e/vnmine elite §f- Hệ thống quái tinh anh");
        sender.sendMessage("§e/vnmine world ... §f- Quản lý world");
        sender.sendMessage("§e/vnmine drop ... §f- Quản lý block drop");
        sender.sendMessage("§e/vnskill §f- Hệ thống công pháp/kỹ năng");
        sender.sendMessage("§e/vnalchemy §f- Luyện đan");
        sender.sendMessage("§e/vnitem §f- Quản lý item/pháp bảo");
        sender.sendMessage("§e/vnmine reload §f- Reload toàn bộ config");
        sender.sendMessage("§e/tps §f- Xem TPS server");
        sender.sendMessage("§e/save-all §f- Lưu toàn bộ dữ liệu");
    }

    // ==================== CULTIVATION COMMAND ====================
    private boolean handleCultivationCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6=== VNMine Tu Luyện ===");
            sender.sendMessage("§e/vnmine cultivate info §f- Xem thông tin tu vi");
            sender.sendMessage("§e/vnmine cultivate toggle §f- Bật/tắt hệ thống");
            if (sender instanceof Player) {
                if (mainMenuGUI != null) {
                    mainMenuGUI.openMainMenu((Player) sender);
                }
            }
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

    // ==================== ELITE COMMAND ====================
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
                boolean eliteEnabled = getConfig().getBoolean("elite-mob-settings.enabled", true);
                eliteEnabled = !eliteEnabled;
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

    // ==================== ITEM COMMAND ====================
    private boolean handleItemCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "vnmine.command.vnmine")) {
            sender.sendMessage("§cBạn không có quyền!");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§6=== VNItem Commands ===");
            sender.sendMessage("§e/vnitem toggle §f- Bật/tắt");
            sender.sendMessage("§e/vnitem reload §f- Reload config");
            sender.sendMessage("§e/vnitem list §f- Danh sách item");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "toggle":
                boolean itemEnabled = getConfig().getBoolean("items.enabled", true);
                itemEnabled = !itemEnabled;
                getConfig().set("items.enabled", itemEnabled);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (itemEnabled ? "bật" : "tắt") + " hệ thống item!");
                break;
            case "reload":
                reloadConfig();
                sender.sendMessage("§6[VNMine] §aĐã reload config!");
                break;
            case "list":
                sender.sendMessage("§6=== Danh Sách Item ===");
                sender.sendMessage("§b- Kiếm Phi Hành: &b&l◆ Kiếm Phi Hành ◆");
                sender.sendMessage("§6- Linh Chung: &6&l◆ Linh Chung ◆");
                sender.sendMessage("§5- Bát Quái Kính: &5&l◆ Bát Quái Kính ◆");
                sender.sendMessage("§a- Hồn Ngọc: &a&l◆ Hồn Ngọc ◆");
                sender.sendMessage("§4- Thiên Linh Thuẫn: &4&l◆ Thiên Linh Thuẫn ◆");
                sender.sendMessage("§e- Lôi Ấn: &e&l◆ Lôi Ấn ◆");
                sender.sendMessage("§6- Phượng Hoàng Lệnh: &6&l◆ Phượng Hoàng Lệnh ◆");
                break;
            default:
                sender.sendMessage("§cSử dụng: /vnitem <toggle|reload|list>");
        }
        return true;
    }

    // ==================== SKILL COMMAND ====================
    private boolean handleSkillCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player && skillManager != null) {
                skillManager.openSkillMenu((Player) sender);
            }
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
                if (skillManager != null) {
                    skillManager.reload();
                    sender.sendMessage("§6[VNMine] §aĐã reload công pháp!");
                }
                break;
            case "my":
                if (sender instanceof Player && skillManager != null) {
                    skillManager.openSkillMenu((Player) sender);
                }
                break;
            default:
                sender.sendMessage("§cSử dụng: /vnskill <toggle|reload|my>");
        }
        return true;
    }

    // ==================== ALCHEMY COMMAND ====================
    private boolean handleAlchemyCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player && mainMenuGUI != null) {
            mainMenuGUI.openAlchemyMenu((Player) sender);
        }
        return true;
    }

    // ==================== FARM COMMAND ====================
    private boolean handleFarmCommand(CommandSender sender, String[] args) {
        sender.sendMessage("§6=== Linh Điền ===");
        sender.sendMessage("§eTính năng đang phát triển...");
        return true;
    }

    // ==================== TIME COMMANDS ====================
    private boolean handleTime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time <set day <phut>|set night <phut>|on|off|status>");
            return true;
        }
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time set <day|night> <phut>");
                    return true;
                }
                handleSet(sender, args[2].toLowerCase(), args[3]);
                break;
            case "on": handleOn(sender); break;
            case "off": handleOff(sender); break;
            case "status": handleStatus(sender); break;
            default:
                sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time <set day <phut>|set night <phut>|on|off|status>");
        }
        return true;
    }

    private void handleSet(CommandSender sender, String type, String minutesStr) {
        if (!hasPermission(sender, "vnmine.time.set")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen!");
            return;
        }
        int minutes;
        try {
            minutes = Integer.parseInt(minutesStr);
            if (minutes < 1) { sender.sendMessage("§6[VNMine] §cThoi gian phai lon hon 0 phut!"); return; }
        } catch (NumberFormatException e) {
            sender.sendMessage("§6[VNMine] §cThoi gian phai la so nguyen duong!"); return;
        }
        switch (type) {
            case "day":
                dayMinutes = minutes;
                getConfig().set("day-minutes", minutes);
                saveConfig();
                if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes);
                sender.sendMessage("§6[VNMine] §aDa set thoi gian ban ngay thanh §e" + minutes + " §aphut!");
                break;
            case "night":
                nightMinutes = minutes;
                getConfig().set("night-minutes", minutes);
                saveConfig();
                if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes);
                sender.sendMessage("§6[VNMine] §aDa set thoi gian ban dem thanh §e" + minutes + " §aphut!");
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time set <day|night> <phut>");
        }
    }

    private void handleOn(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return;
        }
        if (timeManager.isRunning()) {
            sender.sendMessage("§6[VNMine] §eCustom time cycle dang chay roi!"); return;
        }
        timeManager.start(dayMinutes, nightMinutes);
        sender.sendMessage("§6[VNMine] §aDa bat custom time cycle! Ngay: §e" + dayMinutes + "§a phut, Dem: §e" + nightMinutes + "§a phut.");
    }

    private void handleOff(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return;
        }
        if (!timeManager.isRunning()) {
            sender.sendMessage("§6[VNMine] §eCustom time cycle chua duoc bat!"); return;
        }
        timeManager.stop();
        sender.sendMessage("§6[VNMine] §aDa tat custom time cycle, tra ve mac dinh Minecraft.");
    }

    private void handleStatus(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.status")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen!"); return;
        }
        boolean active = timeManager.isRunning();
        World world = Bukkit.getWorlds().get(0);
        long time = world.getFullTime();
        long timeOfDay = time % 24000;
        String phase = (timeOfDay < 12000) ? "§6Ban ngay" : "§8Ban dem";
        sender.sendMessage("§6=== VNMine Time Status ===");
        sender.sendMessage("§fTrang thai: " + (active ? "§aBAT" : "§cTAT"));
        sender.sendMessage("§fBan ngay: §e" + dayMinutes + " §fphut | Ban dem: §e" + nightMinutes + " §fphut");
        sender.sendMessage("§fThoi gian hien tai: " + phase + " §f(tick: " + timeOfDay + ")");
    }

    // ==================== WORLD COMMANDS ====================
    private boolean handleWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world <gen <world>|toggle>");
            return true;
        }
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "gen":
                if (!sender.hasPermission("vnmine.world.gen")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true;
                }
                if (args.length < 3) { sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world gen <world>"); return true; }
                String worldName = args[2];
                sender.sendMessage("§6[VNMine] §aĐang tạo world '" + worldName + "'...");
                boolean created = worldManager.generateWorld(worldName);
                sender.sendMessage("§6[VNMine] " + (created ? "§aĐã tạo" : "§eWorld đã tồn tại") + " world '" + worldName + "'!");
                break;
            case "toggle":
                if (!sender.hasPermission("vnmine.world.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true;
                }
                boolean newState = !worldManager.isEnabled();
                worldManager.setEnabled(newState);
                getConfig().set("world-settings.enabled", newState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " world generation!");
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world <gen <world>|toggle>");
        }
        return true;
    }

    // ==================== DROP COMMANDS ====================
    private boolean handleDrop(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop <toggle|status|replace toggle|break toggle|explode toggle>");
            return true;
        }
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "toggle": {
                if (!sender.hasPermission("vnmine.drop.toggle")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                boolean newState = !dropManager.isEnabled();
                dropManager.setEnabled(newState);
                getConfig().set("block-drop-settings.enabled", newState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " block drop system!");
                break;
            }
            case "status": {
                if (!sender.hasPermission("vnmine.drop.status")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                sender.sendMessage("§6=== Block Drop Status ===");
                sender.sendMessage("§fHệ thống: " + (dropManager.isEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fThay thế block: " + (dropManager.isReplaceEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fGãy cúp: " + (dropManager.isBreakEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fPhát nổ: " + (dropManager.isExplodeEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fSố lượng rules: §e" + dropManager.getRules().size());
                break;
            }
            case "replace": case "break": case "explode":
                // Toggle các sub-features
                if (!sender.hasPermission("vnmine.drop.toggle")) { sender.sendMessage("§6[VNMine] §cBạn không có quyền!"); return true; }
                boolean toggleState;
                String configPath;
                if (subCommand.equals("replace")) {
                    toggleState = !dropManager.isReplaceEnabled();
                    dropManager.setReplaceEnabled(toggleState);
                    configPath = "block-drop-settings.replace-enabled";
                } else if (subCommand.equals("break")) {
                    toggleState = !dropManager.isBreakEnabled();
                    dropManager.setBreakEnabled(toggleState);
                    configPath = "block-drop-settings.break-enabled";
                } else {
                    toggleState = !dropManager.isExplodeEnabled();
                    dropManager.setExplodeEnabled(toggleState);
                    configPath = "block-drop-settings.explode-enabled";
                }
                getConfig().set(configPath, toggleState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (toggleState ? "bật" : "tắt") + " " + subCommand + "!");
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop <toggle|status|replace toggle|break toggle|explode toggle>");
        }
        return true;
    }

    // ==================== RELOAD ====================
    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.reload")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền reload!"); return true;
        }
        reloadConfig();
        loadConfig();
        permissionManager.load();
        worldManager.load();
        dropManager.load();
        if (cultivationManager != null) cultivationManager.reload();
        if (skillManager != null) skillManager.reload();
        if (timeManager.isRunning()) timeManager.updateSpeeds(dayMinutes, nightMinutes);
        sender.sendMessage("§6[VNMine] §aĐã reload toàn bộ config thành công!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmdName.equals("vnmine") || cmdName.equals("vn")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("menu", "time", "perm", "world", "drop", "cultivate", "elite", "reload"));
            } else if (args.length == 2) {
                String first = args[0].toLowerCase();
                switch (first) {
                    case "time": completions.addAll(Arrays.asList("set", "on", "off", "status")); break;
                    case "world": completions.addAll(Arrays.asList("gen", "toggle")); break;
                    case "drop": completions.addAll(Arrays.asList("toggle", "status", "replace", "break", "explode")); break;
                    case "cultivate": case "cultivation": completions.addAll(Arrays.asList("info", "toggle")); break;
                    case "elite": completions.addAll(Arrays.asList("toggle", "info")); break;
                }
            }
        } else if (cmdName.equals("vnitem")) {
            if (args.length == 1) completions.addAll(Arrays.asList("toggle", "reload", "list"));
        } else if (cmdName.equals("vnskill")) {
            if (args.length == 1) completions.addAll(Arrays.asList("toggle", "reload", "my"));
        } else if (cmdName.equals("vnalchemy")) {
            if (args.length == 0) completions.add("menu");
        } else if (cmdName.equals("vnfarm")) {
            if (args.length == 1) completions.add("info");
        }

        return completions;
    }

    // ==================== GETTERS ====================
    public CultivationManager getCultivationManager() { return cultivationManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public MainMenuGUI getMainMenuGUI() { return mainMenuGUI; }
    public PermissionManager getPermissionManager() { return permissionManager; }

    // ==================== TimeManager ====================
    private class TimeManager {
        private BukkitRunnable task;
        private boolean running = false;
        private double daySpeed;
        private double nightSpeed;
        private double fractionalTime = 0;
        private static final int FULL_CYCLE = 24000;
        private static final int DAY_TICKS = 12000;
        private static final int NIGHT_TICKS = 12000;

        public void start(int dayMin, int nightMin) {
            if (running) return;
            updateSpeeds(dayMin, nightMin);
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            }
            task = new BukkitRunnable() {
                @Override
                public void run() { tick(); }
            };
            task.runTaskTimer(VNMinePlugin.this, 0L, 1L);
            running = true;
        }

        public void stop() {
            if (!running) return;
            running = false;
            if (task != null) { task.cancel(); task = null; }
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
            fractionalTime = 0;
        }

        public boolean isRunning() { return running; }
        public void updateSpeeds(int dayMin, int nightMin) {
            daySpeed = (double) DAY_TICKS / (dayMin * 60.0 * 20.0);
            nightSpeed = (double) NIGHT_TICKS / (nightMin * 60.0 * 20.0);
        }

        private void tick() {
            World world = Bukkit.getWorlds().get(0);
            if (world == null) return;
            long currentTime = world.getFullTime();
            long timeOfDay = currentTime % FULL_CYCLE;
            double currentSpeed = (timeOfDay < DAY_TICKS) ? daySpeed : nightSpeed;
            fractionalTime += currentSpeed;
            long ticksToAdd = (long) fractionalTime;
            if (ticksToAdd > 0) {
                fractionalTime -= ticksToAdd;
                world.setFullTime(currentTime + ticksToAdd);
            }
        }
    }
}