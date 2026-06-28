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
import com.vnmine.gui.AdminMenuGUI;
import com.vnmine.gui.AlchemyCraftGUI;
import com.vnmine.gui.ArtifactCraftGUI;
import com.vnmine.gui.MainMenuGUI;
import com.vnmine.gui.QuickMenuListener;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.PillUseListener;
import com.vnmine.item.artifacts.abilities.ArtifactAbilityListener;
import com.vnmine.item.artifacts.abilities.MountItemListener;
import com.vnmine.item.artifacts.abilities.PhoenixRebirthListener;
import com.vnmine.item.block.BlockPlaceListener;
import com.vnmine.mount.MountCommand;
import com.vnmine.mount.MountManager;
import com.vnmine.npc.NPCCommand;
import com.vnmine.npc.NPCListener;
import com.vnmine.npc.NPCManager;
import com.vnmine.npc.NPCShopGUI;
import com.vnmine.permission.PermissionCommand;
import com.vnmine.permission.PermissionManager;
import com.vnmine.skill.PlayerSkillData;
import com.vnmine.skill.SkillManager;
import com.vnmine.skill.SkillBarGUI;
import com.vnmine.skill.SkillBookListener;
import com.vnmine.skill.SkillBookManager;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import com.vnmine.util.NameTagManager;
import com.vnmine.cultivation.MeditationManager;
import com.vnmine.cultivation.MeditationListener;
import com.vnmine.cultivation.MeditationCommand;
import com.vnmine.cultivation.MeditationConfig;
import com.vnmine.biome.BiomeQiManager;
import com.vnmine.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private NameTagManager nameTagManager;
    private SkillManager skillManager;
    private SkillBookManager skillBookManager;
    private SkillBarGUI skillBarGUI;
    private MainMenuGUI mainMenuGUI;
    private AlchemyCraftGUI alchemyCraftGUI;
    private ArtifactCraftGUI artifactCraftGUI;
    private AdminMenuGUI adminMenuGUI;

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
    private MeditationManager meditationManager;
    private MeditationListener meditationListener;
    private MeditationConfig meditationConfig;

    private BiomeQiManager biomeQiManager;

    // === GETTERS ===
    public CultivationManager getCultivationManager() { return cultivationManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public SkillBookManager getSkillBookManager() { return skillBookManager; }
    public SkillBarGUI getSkillBarGUI() { return skillBarGUI; }
    public MainMenuGUI getMainMenuGUI() { return mainMenuGUI; }
    public AdminMenuGUI getAdminMenuGUI() { return adminMenuGUI; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public NPCManager getNPCManager() { return npcManager; }
    public MountManager getMountManager() { return mountManager; }
    public MeditationManager getMeditationManager() { return meditationManager; }
    public MeditationConfig getMeditationConfig() { return meditationConfig; }
    public BiomeQiManager getBiomeQiManager() { return biomeQiManager; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("cultivation.yml", false);
        loadConfig();

        timeManager = new TimeManager();

        // Initialize systems
        permissionManager = new PermissionManager(this);
        permissionCommand = new PermissionCommand(permissionManager);
        worldManager = new WorldManager(this);
        dropManager = new DropManager(this);
        blockDropListener = new BlockDropListener(this, dropManager);

        nameTagManager = new NameTagManager(null);
        cultivationManager = new CultivationManager(this, nameTagManager);
        nameTagManager.setCultivationManager(cultivationManager);
        cultivationListener = new CultivationListener(this, cultivationManager, nameTagManager);
        skillManager = new SkillManager(this);
        skillBookManager = new SkillBookManager(this);
        skillBarGUI = new SkillBarGUI(this);
        adminMenuGUI = new AdminMenuGUI(this);
        mainMenuGUI = new MainMenuGUI(this, cultivationManager, skillManager, adminMenuGUI);
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

        meditationConfig = new MeditationConfig(this);
        meditationManager = new MeditationManager(this);
        biomeQiManager = new BiomeQiManager(this);
        meditationListener = new MeditationListener(this, meditationManager);

        // Load configs
        permissionManager.load();
        worldManager.load();
        dropManager.load();

        // Set plugin instance cho ItemBuilder (NBT keys)
        ItemBuilder.setPlugin(this);

        // Register artifact ability listener (pháp bảo)
        ArtifactAbilityListener artifactAbilityListener = new ArtifactAbilityListener(this);

        // Register pill use listener (đan dược)
        PillUseListener pillUseListener = new PillUseListener(this);

        // Register block place listener (chặn đặt item đặc biệt)
        BlockPlaceListener blockPlaceListener = new BlockPlaceListener();

        // Register new listeners
        MountItemListener mountItemListener = new MountItemListener(this);
        PhoenixRebirthListener phoenixRebirthListener = new PhoenixRebirthListener(this);

        // Register events
        getServer().getPluginManager().registerEvents(blockDropListener, this);
        getServer().getPluginManager().registerEvents(artifactAbilityListener, this);
        getServer().getPluginManager().registerEvents(cultivationListener, this);
        getServer().getPluginManager().registerEvents(mainMenuGUI, this);
        getServer().getPluginManager().registerEvents(alchemyCraftGUI, this);
        getServer().getPluginManager().registerEvents(artifactCraftGUI, this);
        getServer().getPluginManager().registerEvents(currencyListener, this);
        getServer().getPluginManager().registerEvents(npcListener, this);
        getServer().getPluginManager().registerEvents(npcShopGUI, this);
        getServer().getPluginManager().registerEvents(pillUseListener, this);
        getServer().getPluginManager().registerEvents(adminMenuGUI, this);
        getServer().getPluginManager().registerEvents(blockPlaceListener, this);
        getServer().getPluginManager().registerEvents(skillManager, this);
        getServer().getPluginManager().registerEvents(skillBarGUI, this);
        getServer().getPluginManager().registerEvents(new SkillBookListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickMenuListener(this), this);
        getServer().getPluginManager().registerEvents(mountItemListener, this);
        getServer().getPluginManager().registerEvents(phoenixRebirthListener, this);
        getServer().getPluginManager().registerEvents(meditationListener, this);

        // Register commands
        getCommand("vnmine").setExecutor(this);
        getCommand("vnmeditate").setExecutor(new MeditationCommand(this, meditationManager));
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
        getCommand("vnexchange").setExecutor(currencyCommand);
        getCommand("vnadmin").setExecutor(this);

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
            case "vnmeditate":
                if (meditationManager != null && sender instanceof Player) {
                    meditationManager.handleSneakChange((Player) sender, true);
                }
                return true;
            case "vnfarm":
                sender.sendMessage("§6=== Linh Điền ===\n§eTính năng đang phát triển...");
                return true;
            case "vnadmin":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
                    return true;
                }
                Player adminPlayer = (Player) sender;
                if (permissionManager != null && permissionManager.isEnabled()) {
                    if (!permissionManager.hasPermission(adminPlayer, "vnmine.command.admin")) {
                        sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
                        return true;
                    }
                } else if (!adminPlayer.isOp()) {
                    sender.sendMessage("§cBạn không có quyền sử dụng lệnh này! (Yêu cầu OP)");
                    return true;
                }
                if (adminMenuGUI != null) {
                    adminMenuGUI.open(adminPlayer);
                }
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
                String[] permArgs = Arrays.copyOfRange(args, 1, args.length);
                return permissionCommand.onCommand(sender, command, label, permArgs);
            case "world": return handleWorld(sender, args);
            case "drop": return handleDrop(sender, args);
            case "cultivate":
            case "cultivation":
                return handleCultivationCommand(sender, args);
            case "elite": return handleEliteCommand(sender, args);
            case "player":
                return handlePlayerCommand(sender, args);
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
        PlayerCultivationData data = cultivationManager.getPlayerData(uuid);
        if (data != null) {
            // Đã có dữ liệu => đồng bộ level game và cập nhật prefix
            int mcLevel = player.getLevel();
            data.setLevel(mcLevel);
            data.setMaxMana(cultivationManager.calculateMaxMana(mcLevel));
            if (data.getMana() > data.getMaxMana()) {
                data.setMana(data.getMaxMana());
            }
            // Cập nhật name tag
            nameTagManager.updateNameTag(player);
            MessageUtils.send(player, "&a✦ Đã cập nhật prefix tu tiên theo cấp độ hiện tại!");
            MessageUtils.send(player, "&a✦ Cảnh giới: " + data.getRealmPrefix() + "&r&a] &7(Cấp " + mcLevel + ")");
            return true;
        }

        // Chưa có dữ liệu => tạo mới
        data = cultivationManager.getOrCreatePlayerData(uuid, player.getName());

        // Đồng bộ level game ngay lập tức
        int mcLevel = player.getLevel();
        data.setLevel(mcLevel);
        data.setMaxMana(cultivationManager.calculateMaxMana(mcLevel));
        data.setMana(data.getMaxMana());

        // Cập nhật name tag
        nameTagManager.updateNameTag(player);

        // Thông báo
        MessageUtils.sendTitle(player, "&6&l✦ BẮT ĐẦU TU TIÊN ✦",
                "&fChào mừng đến với &eTu Tiên Giới&f!", 10, 60, 10);
        MessageUtils.send(player, "&a✦ Bạn đã bắt đầu hành trình tu tiên!");
        MessageUtils.send(player, "&a✦ Cảnh giới: " + data.getRealmPrefix() + "&r&a] &7(Cấp " + mcLevel + ")");
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
        sender.sendMessage("§e/vnskill §f- Quản lý Skill Bar");
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
                if (skillBookManager != null) { skillBookManager.reload(); sender.sendMessage("§6[VNMine] §aĐã reload sách công pháp!"); }
                break;
            case "my":
                if (sender instanceof Player && skillBarGUI != null) skillBarGUI.openSkillManagement((Player) sender);
                break;
            case "bar":
                if (sender instanceof Player && skillBarGUI != null) {
                    skillBarGUI.openSkillManagement((Player) sender);
                }
                break;
            case "cooldown":
                if (!sender.hasPermission("vnmine.skill.cooldown.bypass")) {
                    sender.sendMessage("§cBạn không có quyền sử dụng lệnh này! (Cần vnmine.skill.cooldown.bypass)");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
                    return true;
                }
                Player cooldownPlayer = (Player) sender;
                PlayerSkillData cooldownData = cultivationManager.getPlayerSkillData(cooldownPlayer.getUniqueId());
                if (cooldownData == null) {
                    sender.sendMessage("§cKhông có dữ liệu skill!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cSử dụng: /vnskill cooldown <on|off>");
                    sender.sendMessage("§7Trạng thái hiện tại: " + (cooldownData.isCooldownBypass() ? "§aON" : "§cOFF"));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "on":
                        cooldownData.setCooldownBypass(true);
                        sender.sendMessage("§aĐã bật bypass cooldown! Bạn có thể dùng skill không cần hồi chiêu.");
                        break;
                    case "off":
                        cooldownData.setCooldownBypass(false);
                        sender.sendMessage("§cĐã tắt bypass cooldown! Skill sẽ hồi chiêu bình thường.");
                        break;
                    default:
                        sender.sendMessage("§cSử dụng: /vnskill cooldown <on|off>");
                }
                return true;
            case "book":
                // Admin command: give a skill book
                if (sender.hasPermission("vnmine.command.admin") && args.length >= 4) {
                    String targetPlayer = args[1];
                    String skillId = args[2];
                    String grade = args[3].toUpperCase();
                    String subGrade = args.length >= 5 ? args[4].toUpperCase() : "HA";
                    Player target = Bukkit.getPlayer(targetPlayer);
                    if (target != null && skillBookManager != null) {
                        ItemStack book = skillBookManager.createSkillBook(skillId, grade, subGrade);
                        if (book != null) {
                            target.getInventory().addItem(book);
                            sender.sendMessage("§6[VNMine] §aĐã give sách công pháp cho " + target.getName());
                        } else {
                            sender.sendMessage("§cKhông thể tạo sách công pháp!");
                        }
                    } else {
                        sender.sendMessage("§cKhông tìm thấy người chơi!");
                    }
                } else {
                    sender.sendMessage("§cSử dụng: /vnskill book <player> <skill_id> <THIEN|DIA|HUYEN|HOANG> [THUONG|TRUNG|HA]");
                }
                break;
            default: sender.sendMessage("§cSử dụng: /vnskill <toggle|reload|my|bar|cooldown|book>");
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
        if (skillBookManager != null) skillBookManager.reload();
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
            if (args.length == 1) {
                completions.addAll(Arrays.asList("menu", "start", "player", "time", "perm", "world", "drop", "cultivate", "elite", "reload"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("player")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("player")) {
                completions.addAll(Arrays.asList("level", "exp", "mana", "skill"));
            } else if (args.length == 4 && args[0].equalsIgnoreCase("player")) {
                String type = args[2].toLowerCase();
                switch (type) {
                    case "level": completions.add("set"); break;
                    case "exp": completions.addAll(Arrays.asList("give", "set")); break;
                    case "mana": completions.add("set"); break;
                    case "skill": completions.addAll(Arrays.asList("learn", "remove")); break;
                }
            } else if (args.length == 5 && args[0].equalsIgnoreCase("player")) {
                String type = args[2].toLowerCase();
                if (type.equals("skill")) {
                    completions.addAll(Arrays.asList("BASIC_HEAL", "QI_SHIELD", "FIRE_BALL", "WIND_BLADE", "LIGHTNING_STRIKE", "SPEED_STEP", "TELEPORT", "METEOR_STORM", "FIRE_CONTROL", "FORGE_MASTERY"));
                }
            }
        }
        return completions;
    }

    // ==================== PLAYER COMMAND ====================
    /**
     * Xử lý lệnh /vnmine player <tên> <loại> <hành động> <giá trị>
     * Ví dụ:
     *   /vnmine player Steve level set 30
     *   /vnmine player Steve exp give 1000
     *   /vnmine player Steve exp set 500
     *   /vnmine player Steve mana set 200
     *   /vnmine player Steve skill learn FIRE_CONTROL
     *   /vnmine player Steve skill remove FIRE_CONTROL
     * Yêu cầu quyền vnmine.command.admin
     */
    private boolean handlePlayerCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vnmine.command.admin")) {
            MessageUtils.send(sender, "&cBạn không có quyền! (Cần vnmine.command.admin)");
            return true;
        }
        if (args.length < 2) {
            sendPlayerHelp(sender);
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtils.send(sender, "&cNgười chơi '" + playerName + "' không trực tuyến!");
            return true;
        }

        if (args.length < 3) {
            sendPlayerHelp(sender);
            return true;
        }

        String type = args[2].toLowerCase();
        String action = args.length >= 4 ? args[3].toLowerCase() : "";
        String valueStr = args.length >= 5 ? args[4] : "";

        PlayerCultivationData data = cultivationManager.getOrCreatePlayerData(target.getUniqueId(), target.getName());

        switch (type) {
            case "level":
                return handlePlayerLevel(sender, target, data, action, valueStr);
            case "exp":
                return handlePlayerExp(sender, target, data, action, valueStr);
            case "mana":
                return handlePlayerMana(sender, target, data, action, valueStr);
            case "skill":
                return handlePlayerSkill(sender, target, data, action, valueStr);
            case "help":
                sendPlayerHelp(sender);
                return true;
            default:
                sendPlayerHelp(sender);
                return true;
        }
    }

    private void sendPlayerHelp(CommandSender sender) {
        MessageUtils.send(sender, "&6&l=== Quản Lý Người Chơi ===");
        MessageUtils.send(sender, "&e/vn mine player <tên> level set <số> &f- Set cấp tu luyện");
        MessageUtils.send(sender, "&e/vnmine player <tên> exp give <số> &f- Thêm EXP");
        MessageUtils.send(sender, "&e/vnmine player <tên> exp set <số> &f- Set EXP");
        MessageUtils.send(sender, "&e/vnmine player <tên> mana set <số> &f- Set linh lực");
        MessageUtils.send(sender, "&e/vnmine player <tên> skill learn <id> &f- Học kỹ năng");
        MessageUtils.send(sender, "&e/vnmine player <tên> skill remove <id> &f- Xóa kỹ năng");
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "&7Skill ID: FIRE_CONTROL, FORGE_MASTERY, BASIC_HEAL, QI_SHIELD, FIRE_BALL, WIND_BLADE, LIGHTNING_STRIKE, SPEED_STEP, TELEPORT, METEOR_STORM");
    }

    private boolean handlePlayerLevel(CommandSender sender, Player target, PlayerCultivationData data, String action, String valueStr) {
        if (!action.equals("set")) {
            MessageUtils.send(sender, "&cSử dụng: /vnmine player <tên> level set <số>");
            return true;
        }
        int level;
        try {
            level = Integer.parseInt(valueStr);
            if (level < 1 || level > 100) {
                MessageUtils.send(sender, "&cCấp độ phải từ 1 đến 100!");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtils.send(sender, "&cCấp độ phải là số nguyên!");
            return true;
        }

        data.setLevel(level);
        data.setExperience(0);
        data.setMaxMana(cultivationManager.calculateMaxMana(level));
        data.setMana(data.getMaxMana());
        data.setWaitingForTribulation(false);
        data.setTribulationInProgress(false);

        nameTagManager.updateNameTag(target);

        MessageUtils.send(target, "&d✦ Admin đã set cấp độ tu luyện của bạn thành &e" + level + " &d(bỏ qua độ kiếp)!");
        MessageUtils.send(sender, "&a✦ Đã set cấp " + target.getName() + " thành &e" + level + " &a(bỏ qua độ kiếp)");
        return true;
    }

    private boolean handlePlayerExp(CommandSender sender, Player target, PlayerCultivationData data, String action, String valueStr) {
        int amount;
        try {
            amount = Integer.parseInt(valueStr);
            if (amount <= 0) {
                MessageUtils.send(sender, "&cSố lượng phải lớn hơn 0!");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtils.send(sender, "&cSố lượng phải là số nguyên!");
            return true;
        }

        switch (action) {
            case "give":
                cultivationManager.addExperience(target, amount);
                MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + " &atu vi!");
                MessageUtils.send(sender, "&a✦ Đã cộng &e" + amount + " &aEXP cho " + target.getName());
                break;
            case "set":
                data.setExperience(amount);
                MessageUtils.send(target, "&d✦ EXP của bạn đã được set thành &e" + amount);
                MessageUtils.send(sender, "&a✦ Đã set EXP " + target.getName() + " thành &e" + amount);
                break;
            default:
                MessageUtils.send(sender, "&cSử dụng: /vnmine player <tên> exp <give|set> <số>");
                return true;
        }
        return true;
    }

    private boolean handlePlayerMana(CommandSender sender, Player target, PlayerCultivationData data, String action, String valueStr) {
        if (!action.equals("set")) {
            MessageUtils.send(sender, "&cSử dụng: /vnmine player <tên> mana set <số>");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(valueStr);
            if (amount < 0) {
                MessageUtils.send(sender, "&cLinh lực không được âm!");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtils.send(sender, "&cLinh lực phải là số nguyên!");
            return true;
        }

        data.setMana(Math.min(amount, data.getMaxMana()));
        MessageUtils.send(target, "&b✦ Linh lực của bạn đã được set thành &e" + data.getMana());
        MessageUtils.send(sender, "&a✦ Đã set linh lực " + target.getName() + " thành &e" + data.getMana());
        return true;
    }

    private boolean handlePlayerSkill(CommandSender sender, Player target, PlayerCultivationData data, String action, String skillId) {
        if (skillId.isEmpty()) {
            MessageUtils.send(sender, "&cSử dụng: /vnmine player <tên> skill <learn|remove> <skill_id>");
            return true;
        }

        switch (action) {
            case "learn":
                if (data.hasLearnedSkill(skillId)) {
                    MessageUtils.send(sender, "&c" + target.getName() + " đã học kỹ năng này rồi!");
                    return true;
                }
                data.learnSkill(skillId);
                MessageUtils.send(target, "&a✦ Admin đã dạy bạn kỹ năng: &e" + skillId);
                MessageUtils.send(sender, "&a✦ Đã cho " + target.getName() + " học kỹ năng &e" + skillId);
                break;
            case "remove":
                if (!data.hasLearnedSkill(skillId)) {
                    MessageUtils.send(sender, "&c" + target.getName() + " chưa học kỹ năng này!");
                    return true;
                }
                data.getLearnedSkills().remove(skillId);
                MessageUtils.send(target, "&c✦ Admin đã xóa kỹ năng: &e" + skillId);
                MessageUtils.send(sender, "&a✦ Đã xóa kỹ năng &e" + skillId + " &acủa " + target.getName());
                break;
            default:
                MessageUtils.send(sender, "&cSử dụng: /vnmine player <tên> skill <learn|remove> <skill_id>");
                return true;
        }
        return true;
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