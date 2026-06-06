package com.vnmine;

import com.vnmine.drop.BlockDropListener;
import com.vnmine.drop.DropManager;
import com.vnmine.permission.PermissionCommand;
import com.vnmine.permission.PermissionManager;
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

        // Load all configurations
        permissionManager.load();
        worldManager.load();
        dropManager.load();

        // Register events
        getServer().getPluginManager().registerEvents(blockDropListener, this);

        // Set command executor
        getCommand("vnmine").setExecutor(this);
        getCommand("vnmine").setTabCompleter(this);

        getLogger().info("VNMine plugin da duoc bat!");
    }

    @Override
    public void onDisable() {
        if (timeManager != null) {
            timeManager.stop();
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
        // Console va CommandBlock khong can permission
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        // Use PermissionManager for players if available
        if (permissionManager != null) {
            return permissionManager.hasPermission(player, permission);
        }

        // Fallback to Bukkit permission
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
                return handleVnmine(sender, command, label, args);
            default:
                return false;
        }
    }

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

    private boolean handleSaveAll(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.saveall")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        sender.sendMessage("§6[VNMine] §aĐang lưu dữ liệu...");
        for (World world : Bukkit.getWorlds()) {
            world.save();
        }
        sender.sendMessage("§6[VNMine] §aĐã lưu toàn bộ dữ liệu!");
        return true;
    }

    private boolean handleVnmine(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "vnmine.command.vnmine")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length < 1) {
            sendMainHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "time":
                return handleTime(sender, args);
            case "perm":
                // Delegate to PermissionCommand
                if (!sender.hasPermission("vnmine.perm.admin")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                return permissionCommand.onCommand(sender, command, label, args);
            case "world":
                return handleWorld(sender, args);
            case "drop":
                return handleDrop(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendMainHelp(sender);
                return true;
        }
    }

    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNMine Plugin Help ===");
        sender.sendMessage("§e/vnmine time <set day/night <phut>|on|off|status> §f- Quản lý thời gian");
        sender.sendMessage("§e/vnmine perm ... §f- Quản lý phân quyền");
        sender.sendMessage("§e/vnmine world gen <world> §f- Tạo world mới");
        sender.sendMessage("§e/vnmine world toggle §f- Bật/tắt world generation");
        sender.sendMessage("§e/vnmine drop toggle §f- Bật/tắt block drop system");
        sender.sendMessage("§e/vnmine drop status §f- Xem trạng thái");
        sender.sendMessage("§e/vnmine drop replace toggle §f- Bật/tắt thay thế block");
        sender.sendMessage("§e/vnmine drop break toggle §f- Bật/tắt gãy cúp");
        sender.sendMessage("§e/vnmine drop explode toggle §f- Bật/tắt phát nổ");
        sender.sendMessage("§e/vnmine reload §f- Reload toàn bộ config");
        sender.sendMessage("§e/tps §f- Xem TPS server");
        sender.sendMessage("§e/save-all §f- Lưu toàn bộ dữ liệu");
    }

    // ==================== TIME COMMANDS (Existing) ====================
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
            case "on":
                handleOn(sender);
                break;
            case "off":
                handleOff(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time <set day <phut>|set night <phut>|on|off|status>");
        }
        return true;
    }

    private void handleSet(CommandSender sender, String type, String minutesStr) {
        if (!hasPermission(sender, "vnmine.time.set")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen su dung lenh nay!");
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(minutesStr);
            if (minutes < 1) {
                sender.sendMessage("§6[VNMine] §cThoi gian phai lon hon 0 phut!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§6[VNMine] §cThoi gian phai la so nguyen duong!");
            return;
        }

        switch (type) {
            case "day":
                dayMinutes = minutes;
                getConfig().set("day-minutes", minutes);
                saveConfig();
                if (timeManager.isRunning()) {
                    timeManager.updateSpeeds(dayMinutes, nightMinutes);
                }
                sender.sendMessage("§6[VNMine] §aDa set thoi gian ban ngay thanh §e" + minutes + " §aphut!");
                break;
            case "night":
                nightMinutes = minutes;
                getConfig().set("night-minutes", minutes);
                saveConfig();
                if (timeManager.isRunning()) {
                    timeManager.updateSpeeds(dayMinutes, nightMinutes);
                }
                sender.sendMessage("§6[VNMine] §aDa set thoi gian ban dem thanh §e" + minutes + " §aphut!");
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSu dung: /vnmine time set <day|night> <phut>");
        }
    }

    private void handleOn(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen su dung lenh nay!");
            return;
        }

        if (timeManager.isRunning()) {
            sender.sendMessage("§6[VNMine] §eCustom time cycle dang chay roi!");
            return;
        }

        timeManager.start(dayMinutes, nightMinutes);
        sender.sendMessage("§6[VNMine] §aDa bat custom time cycle! Ngay: §e" + dayMinutes + "§a phut, Dem: §e" + nightMinutes + "§a phut.");
    }

    private void handleOff(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.toggle")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen su dung lenh nay!");
            return;
        }

        if (!timeManager.isRunning()) {
            sender.sendMessage("§6[VNMine] §eCustom time cycle chua duoc bat!");
            return;
        }

        timeManager.stop();
        sender.sendMessage("§6[VNMine] §aDa tat custom time cycle, tra ve mac dinh Minecraft.");
    }

    private void handleStatus(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.time.status")) {
            sender.sendMessage("§6[VNMine] §cBan khong co quyen su dung lenh nay!");
            return;
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
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine world gen <world>");
                    return true;
                }
                String worldName = args[2];
                sender.sendMessage("§6[VNMine] §aĐang tạo world '" + worldName + "'...");
                boolean created = worldManager.generateWorld(worldName);
                if (created) {
                    sender.sendMessage("§6[VNMine] §aĐã tạo world '" + worldName + "' thành công!");
                } else {
                    sender.sendMessage("§6[VNMine] §eWorld '" + worldName + "' đã tồn tại hoặc không thể tạo.");
                }
                break;
            case "toggle":
                if (!sender.hasPermission("vnmine.world.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
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
            case "toggle":
                if (!sender.hasPermission("vnmine.drop.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                boolean newState = !dropManager.isEnabled();
                dropManager.setEnabled(newState);
                getConfig().set("block-drop-settings.enabled", newState);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newState ? "bật" : "tắt") + " block drop system!");
                break;
            case "status":
                if (!sender.hasPermission("vnmine.drop.status")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                sender.sendMessage("§6=== Block Drop Status ===");
                sender.sendMessage("§fHệ thống: " + (dropManager.isEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fThay thế block: " + (dropManager.isReplaceEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fGãy cúp: " + (dropManager.isBreakEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fPhát nổ: " + (dropManager.isExplodeEnabled() ? "§aBẬT" : "§cTẮT"));
                sender.sendMessage("§fSố lượng rules: §e" + dropManager.getRules().size());
                break;
            case "replace":
                if (args.length < 3 || !args[2].equalsIgnoreCase("toggle")) {
                    sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop replace toggle");
                    return true;
                }
                if (!sender.hasPermission("vnmine.drop.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                boolean newReplace = !dropManager.isReplaceEnabled();
                dropManager.setReplaceEnabled(newReplace);
                getConfig().set("block-drop-settings.replace-enabled", newReplace);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newReplace ? "bật" : "tắt") + " thay thế block!");
                break;
            case "break":
                if (args.length < 3 || !args[2].equalsIgnoreCase("toggle")) {
                    sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop break toggle");
                    return true;
                }
                if (!sender.hasPermission("vnmine.drop.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                boolean newBreak = !dropManager.isBreakEnabled();
                dropManager.setBreakEnabled(newBreak);
                getConfig().set("block-drop-settings.break-enabled", newBreak);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newBreak ? "bật" : "tắt") + " gãy cúp!");
                break;
            case "explode":
                if (args.length < 3 || !args[2].equalsIgnoreCase("toggle")) {
                    sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop explode toggle");
                    return true;
                }
                if (!sender.hasPermission("vnmine.drop.toggle")) {
                    sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
                    return true;
                }
                boolean newExplode = !dropManager.isExplodeEnabled();
                dropManager.setExplodeEnabled(newExplode);
                getConfig().set("block-drop-settings.explode-enabled", newExplode);
                saveConfig();
                sender.sendMessage("§6[VNMine] §aĐã " + (newExplode ? "bật" : "tắt") + " phát nổ!");
                break;
            default:
                sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine drop <toggle|status|replace toggle|break toggle|explode toggle>");
        }
        return true;
    }

    // ==================== RELOAD COMMAND ====================
    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender, "vnmine.command.reload")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền reload!");
            return true;
        }

        reloadConfig();
        loadConfig();
        permissionManager.load();
        worldManager.load();
        dropManager.load();

        if (timeManager.isRunning()) {
            timeManager.updateSpeeds(dayMinutes, nightMinutes);
        }

        sender.sendMessage("§6[VNMine] §aĐã reload toàn bộ config thành công!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmdName.equals("vnmine")) {
            if (args.length == 1) {
                completions.add("time");
                completions.add("perm");
                completions.add("world");
                completions.add("drop");
                completions.add("reload");
            } else if (args.length == 2) {
                String first = args[0].toLowerCase();
                switch (first) {
                    case "time":
                        completions.add("set");
                        completions.add("on");
                        completions.add("off");
                        completions.add("status");
                        break;
                    case "perm":
                        completions.add("group");
                        completions.add("player");
                        completions.add("check");
                        completions.add("reload");
                        break;
                    case "world":
                        completions.add("gen");
                        completions.add("toggle");
                        break;
                    case "drop":
                        completions.add("toggle");
                        completions.add("status");
                        completions.add("replace");
                        completions.add("break");
                        completions.add("explode");
                        break;
                }
            } else if (args.length == 3) {
                String first = args[0].toLowerCase();
                String second = args[1].toLowerCase();
                if (first.equals("time") && second.equals("set")) {
                    completions.add("day");
                    completions.add("night");
                } else if (first.equals("world") && second.equals("gen")) {
                    // Suggest configured worlds
                    if (worldManager != null) {
                        completions.addAll(worldManager.getWorldConfigs().keySet());
                    }
                } else if (first.equals("drop") && (second.equals("replace") ||
                        second.equals("break") || second.equals("explode"))) {
                    completions.add("toggle");
                } else if (first.equals("perm")) {
                    // Delegate to PermissionCommand tab completion
                    return permissionCommand.onTabComplete(sender, command, alias, args);
                }
            } else if (args.length == 4) {
                String first = args[0].toLowerCase();
                if (first.equals("time") && args[1].equalsIgnoreCase("set")) {
                    completions.add("<so phut>");
                } else if (first.equals("perm")) {
                    return permissionCommand.onTabComplete(sender, command, alias, args);
                }
            }
        }

        return completions;
    }

    // ==================== TimeManager (Inner Class, unchanged) ====================
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
                public void run() {
                    tick();
                }
            };
            task.runTaskTimer(VNMinePlugin.this, 0L, 1L);
            running = true;
        }

        public void stop() {
            if (!running) return;

            running = false;
            if (task != null) {
                task.cancel();
                task = null;
            }

            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }

            fractionalTime = 0;
        }

        public boolean isRunning() {
            return running;
        }

        public void updateSpeeds(int dayMin, int nightMin) {
            daySpeed = (double) DAY_TICKS / (dayMin * 60.0 * 20.0);
            nightSpeed = (double) NIGHT_TICKS / (nightMin * 60.0 * 20.0);
        }

        private void tick() {
            World world = Bukkit.getWorlds().get(0);
            if (world == null) return;

            long currentTime = world.getFullTime();
            long timeOfDay = currentTime % FULL_CYCLE;

            double currentSpeed;
            if (timeOfDay < DAY_TICKS) {
                currentSpeed = daySpeed;
            } else {
                currentSpeed = nightSpeed;
            }

            fractionalTime += currentSpeed;

            long ticksToAdd = (long) fractionalTime;
            if (ticksToAdd > 0) {
                fractionalTime -= ticksToAdd;
                world.setFullTime(currentTime + ticksToAdd);
            }
        }
    }
}