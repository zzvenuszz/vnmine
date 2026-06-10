package com.vnmine.npc;

import com.vnmine.VNMinePlugin;
import com.vnmine.npc.NPCManager.NPCDisplayInfo;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NPCCommand - Quản lý NPC trong game
 * 
 * Lệnh:
 *   /vnnpc create <id> <type> [name]    - Tạo NPC
 *   /vnnpc remove <id>                   - Xóa NPC
 *   /vnnpc list [all|<type>]             - Danh sách NPC
 *   /vnnpc tp <id>                       - Dịch chuyển đến NPC
 *   /vnnpc movehere <id>                 - Di chuyển NPC đến chỗ bạn
 *   /vnnpc rename <id> <name>            - Đổi tên NPC
 *   /vnnpc removeall [type]              - Xóa tất cả NPX / xóa theo loại
 *   /vnnpc skin <id> <playerName>        - Đổi skin NPC
 *   /vnnpc reload                        - Reload config
 */
public class NPCCommand implements CommandExecutor {

    private final VNMinePlugin plugin;
    private final NPCManager npcManager;

    public NPCCommand(VNMinePlugin plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vnmine.command.npc")) {
            sender.sendMessage("§cBạn không có quyền!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "movehere":
                return handleMoveHere(sender, args);
            case "rename":
                return handleRename(sender, args);
            case "removeall":
                return handleRemoveAll(sender, args);
            case "skin":
                return handleSkin(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNNPC Commands ===");
        sender.sendMessage("§e/vnnpc create <id> <type> [tên] §f- Tạo NPC");
        sender.sendMessage("§e/vnnpc remove <id> §f- Xóa NPC");
        sender.sendMessage("§e/vnnpc list [all|<type>] §f- Danh sách NPC");
        sender.sendMessage("§e/vnnpc tp <id> §f- Dịch chuyển đến NPC");
        sender.sendMessage("§e/vnnpc movehere <id> §f- Di chuyển NPC đến chỗ bạn");
        sender.sendMessage("§e/vnnpc rename <id> <tên> §f- Đổi tên NPC");
        sender.sendMessage("§e/vnnpc removeall [type] §f- Xóa tất cả NPC");
        sender.sendMessage("§e/vnnpc skin <id> <player> §f- Đổi skin NPC");
        sender.sendMessage("§e/vnnpc reload §f- Reload config");
    }

    /**
     * /vnnpc create <id> <type> [name]
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cSử dụng: /vnnpc create <id> <type> [tên]");
            sender.sendMessage("§7Các loại (type) có sẵn: " + String.join(", ", npcManager.getTemplateIds()));
            return true;
        }

        String instanceId = args[1].toLowerCase();
        String type = args[2].toLowerCase();

        // Kiểm tra type có tồn tại trong config không
        if (!npcManager.hasTemplate(type)) {
            sender.sendMessage("§cLoại NPC '" + type + "' không tồn tại trong config.yml!");
            sender.sendMessage("§7Các loại có sẵn: " + String.join(", ", npcManager.getTemplateIds()));
            return true;
        }

        // Lấy tên tùy chỉnh nếu có
        String customName = null;
        if (args.length >= 4) {
            // Ghép các argument còn lại thành tên
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (nameBuilder.length() > 0) nameBuilder.append(" ");
                nameBuilder.append(args[i]);
            }
            customName = nameBuilder.toString();
            // Remove quotes if present
            if (customName.startsWith("\"") && customName.endsWith("\"")) {
                customName = customName.substring(1, customName.length() - 1);
            }
            // Hỗ trợ mã màu &
            customName = ColorUtils.colorize(customName);
        }

        Player player = (Player) sender;

        // Đã tồn tại ID
        if (npcManager.getDisplayInfo(instanceId) != null) {
            sender.sendMessage("§cNPC ID '" + instanceId + "' đã tồn tại! Hãy dùng ID khác hoặc xóa NPC cũ trước.");
            return true;
        }

        boolean success = npcManager.createNPC(instanceId, type, player, customName);
        if (success) {
            String nameDisplay = npcManager.getInstanceType(instanceId);
            sender.sendMessage("§a✦ Đã tạo NPC '" + instanceId + "' (loại: " + type + ") tại vị trí của bạn!");
        } else {
            sender.sendMessage("§cKhông thể tạo NPC! Vui lòng kiểm tra lại.");
        }

        return true;
    }

    /**
     * /vnnpc remove <id>
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc remove <id>");
            return true;
        }

        String instanceId = args[1].toLowerCase();

        if (npcManager.removeNPC(instanceId)) {
            sender.sendMessage("§aĐã xóa NPC '" + instanceId + "'!");
        } else {
            sender.sendMessage("§cKhông tìm thấy NPC '" + instanceId + "'!");
        }

        return true;
    }

    /**
     * /vnnpc list [all|<type>]
     */
    private boolean handleList(CommandSender sender, String[] args) {
        String filter = (args.length >= 2) ? args[1].toLowerCase() : "all";

        List<String> instanceIds;
        if ("all".equals(filter)) {
            instanceIds = npcManager.getAllInstanceIds();
        } else {
            instanceIds = npcManager.getInstanceIdsByType(filter);
        }

        if (instanceIds.isEmpty()) {
            if ("all".equals(filter)) {
                sender.sendMessage("§6=== Danh sách NPC ===");
                sender.sendMessage("§7Chưa có NPC nào được tạo.");
            } else {
                sender.sendMessage("§6=== Danh sách NPC (loại: " + filter + ") ===");
                sender.sendMessage("§7Không có NPC nào thuộc loại '" + filter + "'.");
            }

            // Hiển thị các loại có sẵn
            sender.sendMessage("§7Các loại NPC có sẵn: " + String.join(", ", npcManager.getTemplateIds()));
            return true;
        }

        if ("all".equals(filter)) {
            sender.sendMessage("§6=== Danh sách NPC (" + instanceIds.size() + ") ===");
        } else {
            sender.sendMessage("§6=== Danh sách NPC loại " + filter + " (" + instanceIds.size() + ") ===");
        }

        for (String id : instanceIds) {
            NPCDisplayInfo info = npcManager.getDisplayInfo(id);
            if (info != null) {
                String status = info.isSpawned() ? "§a✔" : "§c✘";
                String nameDisplay = info.hasCustomName() ? info.getName() : "(mặc định)";
                String skinDisplay = info.getSkin() != null ? " §7[skin: " + info.getSkin() + "]" : "";
                sender.sendMessage("  " + status + " §e" + id + " §7(" + info.getType() + ") §f" + nameDisplay + skinDisplay);
            }
        }

        return true;
    }

    /**
     * /vnnpc tp <id>
     */
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc tp <id>");
            return true;
        }

        String instanceId = args[1].toLowerCase();
        NPCManager.NPCInstanceData data = npcManager.getInstanceData(instanceId);

        if (data == null || data.getEntity() == null) {
            sender.sendMessage("§cNPC '" + instanceId + "' không tồn tại hoặc chưa được spawn!");
            return true;
        }

        Player player = (Player) sender;
        player.teleport(data.getEntity().getLocation());
        sender.sendMessage("§aĐã dịch chuyển đến NPC '" + instanceId + "'!");

        return true;
    }

    /**
     * /vnnpc movehere <id>
     */
    private boolean handleMoveHere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc movehere <id>");
            return true;
        }

        String instanceId = args[1].toLowerCase();
        Player player = (Player) sender;

        if (npcManager.moveNPC(instanceId, player)) {
            sender.sendMessage("§aĐã di chuyển NPC '" + instanceId + "' đến vị trí của bạn!");
        } else {
            sender.sendMessage("§cKhông tìm thấy NPC '" + instanceId + "'!");
        }

        return true;
    }

    /**
     * /vnnpc rename <id> <name>
     */
    private boolean handleRename(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cSử dụng: /vnnpc rename <id> <tên mới>");
            return true;
        }

        String instanceId = args[1].toLowerCase();

        // Ghép các argument còn lại thành tên
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (nameBuilder.length() > 0) nameBuilder.append(" ");
            nameBuilder.append(args[i]);
        }
        String newName = nameBuilder.toString();
        // Remove quotes if present
        if (newName.startsWith("\"") && newName.endsWith("\"")) {
            newName = newName.substring(1, newName.length() - 1);
        }
        // Hỗ trợ mã màu
        newName = ColorUtils.colorize(newName);

        if (npcManager.renameNPC(instanceId, newName)) {
            sender.sendMessage("§aĐã đổi tên NPC '" + instanceId + "' thành: " + newName);
        } else {
            sender.sendMessage("§cKhông tìm thấy NPC '" + instanceId + "'!");
        }

        return true;
    }

    /**
     * /vnnpc removeall [type]
     */
    private boolean handleRemoveAll(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            // Xóa theo loại
            String type = args[1].toLowerCase();
            int count = npcManager.removeNPCsByType(type);
            sender.sendMessage("§aĐã xóa " + count + " NPC thuộc loại '" + type + "'!");
        } else {
            // Xóa tất cả
            npcManager.removeAllNPCs();
            sender.sendMessage("§aĐã xóa tất cả NPC!");
        }

        return true;
    }

    /**
     * /vnnpc skin <id> <playerName>
     */
    private boolean handleSkin(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cSử dụng: /vnnpc skin <id> <tên_người_chơi>");
            return true;
        }

        String instanceId = args[1].toLowerCase();
        String skinName = args[2];

        // Kiểm tra NPC tồn tại
        NPCDisplayInfo info = npcManager.getDisplayInfo(instanceId);
        if (info == null) {
            sender.sendMessage("§cKhông tìm thấy NPC '" + instanceId + "'!");
            return true;
        }

        sender.sendMessage("§eĐang cập nhật skin cho NPC '" + instanceId + "' thành '" + skinName + "'...");
        sender.sendMessage("§7(Lưu ý: Skin chỉ hiển thị khi có ProtocolLib. Hiện tại NPC sẽ spawn dạng Villager.)");

        if (npcManager.setSkinNPC(instanceId, skinName)) {
            sender.sendMessage("§aĐã lưu skin '" + skinName + "' cho NPC '" + instanceId + "'!");
        } else {
            sender.sendMessage("§cKhông thể cập nhật skin!");
        }

        return true;
    }

    /**
     * /vnnpc reload
     */
    private boolean handleReload(CommandSender sender) {
        npcManager.reload();
        sender.sendMessage("§aĐã reload NPC config!");
        return true;
    }
}