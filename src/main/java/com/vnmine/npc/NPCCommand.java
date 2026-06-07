package com.vnmine.npc;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * NPCCommand - /vnnpc create|remove|list|tp|reload
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
                return handleList(sender);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNNPC Commands ===");
        sender.sendMessage("§e/vnnpc create <id> §f- Tạo NPC tại vị trí đang đứng");
        sender.sendMessage("§e/vnnpc remove <id> §f- Xóa NPC");
        sender.sendMessage("§e/vnnpc list §f- Danh sách NPC");
        sender.sendMessage("§e/vnnpc tp <id> §f- Dịch chuyển đến NPC");
        sender.sendMessage("§e/vnnpc reload §f- Reload config");
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc create <id>");
            return true;
        }

        String npcId = args[1].toLowerCase();
        Player player = (Player) sender;

        // Kiểm tra ID có trong config không
        if (npcManager.getNPCData(npcId) == null) {
            // Tạo NPC config tạm
            sender.sendMessage("§cNPC ID '" + npcId + "' không tồn tại trong config.yml!");
            sender.sendMessage("§cCác ID hợp lệ: skill_master, artifact_master, pill_master, currency_master");
            return true;
        }

        // Xóa NPC cũ nếu đã tồn tại
        npcManager.despawnNPC(npcId);

        // Lưu vị trí
        npcManager.saveNPCLocation(npcId, player);

        // Spawn lại
        npcManager.spawnNPC(npcId);

        MessageUtils.send(player, "&a✦ Đã tạo NPC '" + npcId + "' tại vị trí của bạn!");
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc remove <id>");
            return true;
        }
        String npcId = args[1].toLowerCase();
        npcManager.despawnNPC(npcId);
        sender.sendMessage("§aĐã xóa NPC '" + npcId + "'!");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage("§6=== Danh sách NPC ===");
        for (String id : npcManager.getNPCConfigs().keySet()) {
            NPCData data = npcManager.getNPCData(id);
            if (data != null) {
                sender.sendMessage("§e- " + id + " §7(" + data.getName() + "§7)");
            }
        }
        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnnpc tp <id>");
            return true;
        }
        sender.sendMessage("§eTính năng đang phát triển...");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        npcManager.reload();
        sender.sendMessage("§aĐã reload NPC config!");
        return true;
    }
}