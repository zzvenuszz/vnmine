package com.vnmine.permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionCommand implements CommandExecutor, TabCompleter {
    private final PermissionManager permissionManager;

    public PermissionCommand(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vnmine.perm.admin")) {
            sender.sendMessage("§6[VNMine] §cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        // args[0] is "perm", strip it
        String[] permArgs = (args[0].equalsIgnoreCase("perm")) 
                ? Arrays.copyOfRange(args, 1, args.length) 
                : args;

        if (permArgs.length < 1) {
            sendHelp(sender);
            return true;
        }

        String action = permArgs[0].toLowerCase();

        switch (action) {
            case "group":
                return handleGroupCommand(sender, permArgs);
            case "player":
                return handlePlayerCommand(sender, permArgs);
            case "check":
                return handleCheckCommand(sender, permArgs);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNMine Permission Commands ===");
        sender.sendMessage("§e/vnmine perm group list §f- Danh sách nhóm");
        sender.sendMessage("§e/vnmine perm group info <group> §f- Thông tin nhóm");
        sender.sendMessage("§e/vnmine perm group create <group> §f- Tạo nhóm mới");
        sender.sendMessage("§e/vnmine perm group delete <group> §f- Xóa nhóm");
        sender.sendMessage("§e/vnmine perm group setweight <group> <weight> §f- Set weight");
        sender.sendMessage("§e/vnmine perm group setprefix <group> <prefix> §f- Set prefix");
        sender.sendMessage("§e/vnmine perm group setsuffix <group> <suffix> §f- Set suffix");
        sender.sendMessage("§e/vnmine perm group addperm <group> <perm> §f- Thêm permission");
        sender.sendMessage("§e/vnmine perm group removeperm <group> <perm> §f- Xóa permission");
        sender.sendMessage("§e/vnmine perm group addparent <group> <parent> §f- Thêm group cha");
        sender.sendMessage("§e/vnmine perm group removeparent <group> §f- Xóa group cha");
        sender.sendMessage("§e/vnmine perm group setdefault <group> §f- Set làm default");
        sender.sendMessage("§e/vnmine perm player info <player> §f- Xem thông tin player");
        sender.sendMessage("§e/vnmine perm player setgroup <player> <group> §f- Gán group");
        sender.sendMessage("§e/vnmine perm player addperm <player> <perm> §f- Thêm perm riêng");
        sender.sendMessage("§e/vnmine perm player removeperm <player> <perm> §f- Xóa perm riêng");
        sender.sendMessage("§e/vnmine perm check <player> <perm> §f- Kiểm tra permission");
        sender.sendMessage("§e/vnmine perm reload §f- Reload permission config");
    }

    private boolean handleGroupCommand(CommandSender sender, String[] args) {
        // args: ["group", "list"] or ["group", "info", "admin"]
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group <action> [args]");
            return true;
        }

        String subAction = args[1].toLowerCase();

        switch (subAction) {
            case "list":
                return handleGroupList(sender);
            case "info":
                return handleGroupInfo(sender, args);
            case "create":
                return handleGroupCreate(sender, args);
            case "delete":
                return handleGroupDelete(sender, args);
            case "setweight":
                return handleGroupSetWeight(sender, args);
            case "setprefix":
                return handleGroupSetPrefix(sender, args);
            case "setsuffix":
                return handleGroupSetSuffix(sender, args);
            case "addperm":
                return handleGroupAddPerm(sender, args);
            case "removeperm":
                return handleGroupRemovePerm(sender, args);
            case "addparent":
                return handleGroupAddParent(sender, args);
            case "removeparent":
                return handleGroupRemoveParent(sender, args);
            case "setdefault":
                return handleGroupSetDefault(sender, args);
            default:
                sender.sendMessage("§6[VNMine] §cHành động không hợp lệ!");
                return true;
        }
    }

    private boolean handleGroupList(CommandSender sender) {
        sender.sendMessage("§6=== Danh sách Groups ===");
        for (Group group : permissionManager.getGroups().values()) {
            String def = group.isDefault() ? " §a(DEFAULT)" : "";
            sender.sendMessage(" §e" + group.getName() + "§f - Weight: " + group.getWeight() +
                    " | Perms: " + group.getPermissions().size() + " | Parents: " + group.getParents().size() + def);
        }
        return true;
    }

    private boolean handleGroupInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group info <group>");
            return true;
        }
        String groupName = args[2];
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        sender.sendMessage("§6=== Group: " + groupName + " ===");
        sender.sendMessage("§fWeight: §e" + group.getWeight());
        sender.sendMessage("§fPrefix: §e" + group.getPrefix());
        sender.sendMessage("§fSuffix: §e" + group.getSuffix());
        sender.sendMessage("§fDefault: " + (group.isDefault() ? "§aCó" : "§cKhông"));
        sender.sendMessage("§fParents: §e" + (group.getParents().isEmpty() ? "Không có" : String.join(", ", group.getParents())));
        sender.sendMessage("§fPermissions: ");
        for (String perm : group.getPermissions()) {
            sender.sendMessage("  §7- " + perm);
        }
        return true;
    }

    private boolean handleGroupCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group create <group>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        if (permissionManager.getGroup(groupName) != null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' đã tồn tại!");
            return true;
        }
        Group group = new Group(groupName);
        permissionManager.addGroup(group);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã tạo group '" + groupName + "' thành công!");
        return true;
    }

    private boolean handleGroupDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group delete <group>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        if (group.isDefault()) {
            sender.sendMessage("§6[VNMine] §cKhông thể xóa group mặc định!");
            return true;
        }
        permissionManager.removeGroup(groupName);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã xóa group '" + groupName + "' thành công!");
        return true;
    }

    private boolean handleGroupSetWeight(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group setweight <group> <weight>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        try {
            int weight = Integer.parseInt(args[3]);
            group.setWeight(weight);
            permissionManager.saveToConfig();
            sender.sendMessage("§6[VNMine] §aĐã set weight của group '" + groupName + "' thành " + weight);
        } catch (NumberFormatException e) {
            sender.sendMessage("§6[VNMine] §cWeight phải là số nguyên!");
        }
        return true;
    }

    private boolean handleGroupSetPrefix(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group setprefix <group> <prefix>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        String prefix = args[3];
        if (args.length > 4) {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 4; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            prefix = sb.toString();
        }
        group.setPrefix(prefix);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã set prefix của group '" + groupName + "' thành '" + prefix + "'");
        return true;
    }

    private boolean handleGroupSetSuffix(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group setsuffix <group> <suffix>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        String suffix = args[3];
        if (args.length > 4) {
            StringBuilder sb = new StringBuilder(suffix);
            for (int i = 4; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            suffix = sb.toString();
        }
        group.setSuffix(suffix);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã set suffix của group '" + groupName + "' thành '" + suffix + "'");
        return true;
    }

    private boolean handleGroupAddPerm(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group addperm <group> <permission>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        String perm = args[3];
        group.addPermission(perm);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã thêm permission '" + perm + "' vào group '" + groupName + "'");
        return true;
    }

    private boolean handleGroupRemovePerm(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group removeperm <group> <permission>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        String perm = args[3];
        group.removePermission(perm);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã xóa permission '" + perm + "' khỏi group '" + groupName + "'");
        return true;
    }

    private boolean handleGroupAddParent(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group addparent <group> <parent>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        String parent = args[3].toLowerCase();
        if (permissionManager.getGroup(parent) == null) {
            sender.sendMessage("§6[VNMine] §cGroup cha '" + parent + "' không tồn tại!");
            return true;
        }
        group.addParent(parent);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã thêm parent '" + parent + "' cho group '" + groupName + "'");
        return true;
    }

    private boolean handleGroupRemoveParent(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group removeparent <group>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        if (args.length >= 4) {
            group.removeParent(args[3].toLowerCase());
        } else {
            group.getParents().clear();
        }
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã xóa parent của group '" + groupName + "'");
        return true;
    }

    private boolean handleGroupSetDefault(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm group setdefault <group>");
            return true;
        }
        String groupName = args[2].toLowerCase();
        Group group = permissionManager.getGroup(groupName);
        if (group == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }
        // Remove default from all groups
        for (Group g : permissionManager.getGroups().values()) {
            g.setDefault(false);
        }
        group.setDefault(true);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã set group '" + groupName + "' làm default");
        return true;
    }

    private boolean handlePlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm player <action> [args]");
            return true;
        }

        String subAction = args[1].toLowerCase();

        switch (subAction) {
            case "info":
                return handlePlayerInfo(sender, args);
            case "setgroup":
                return handlePlayerSetGroup(sender, args);
            case "addperm":
                return handlePlayerAddPerm(sender, args);
            case "removeperm":
                return handlePlayerRemovePerm(sender, args);
            default:
                sender.sendMessage("§6[VNMine] §cHành động không hợp lệ!");
                return true;
        }
    }

    private boolean handlePlayerInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm player info <player>");
            return true;
        }
        String playerName = args[2];
        PlayerData data = permissionManager.getPlayerData(playerName);
        if (data == null) {
            data = new PlayerData(playerName);
        }

        List<String> groups = permissionManager.getPlayerGroups(playerName);

        sender.sendMessage("§6=== Player: " + playerName + " ===");
        sender.sendMessage("§fPrimary Group: §e" + data.getPrimaryGroup());
        sender.sendMessage("§fAll Groups: §e" + String.join(", ", groups));
        sender.sendMessage("§fPrefix: §e" + data.getPrefix());
        sender.sendMessage("§fSuffix: §e" + data.getSuffix());
        sender.sendMessage("§fPlayer Permissions: ");
        for (String perm : data.getPermissions()) {
            sender.sendMessage("  §7- " + perm);
        }
        return true;
    }

    private boolean handlePlayerSetGroup(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm player setgroup <player> <group>");
            return true;
        }
        String playerName = args[2];
        String groupName = args[3].toLowerCase();
        if (permissionManager.getGroup(groupName) == null) {
            sender.sendMessage("§6[VNMine] §cGroup '" + groupName + "' không tồn tại!");
            return true;
        }

        PlayerData data = permissionManager.getOrCreatePlayerData(playerName);
        data.setPrimaryGroup(groupName);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã set group cho '" + playerName + "' thành '" + groupName + "'");
        return true;
    }

    private boolean handlePlayerAddPerm(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm player addperm <player> <perm>");
            return true;
        }
        String playerName = args[2];
        String perm = args[3];
        PlayerData data = permissionManager.getOrCreatePlayerData(playerName);
        data.addPermission(perm);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã thêm permission '" + perm + "' cho '" + playerName + "'");
        return true;
    }

    private boolean handlePlayerRemovePerm(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm player removeperm <player> <perm>");
            return true;
        }
        String playerName = args[2];
        String perm = args[3];
        PlayerData data = permissionManager.getOrCreatePlayerData(playerName);
        data.removePermission(perm);
        permissionManager.saveToConfig();
        sender.sendMessage("§6[VNMine] §aĐã xóa permission '" + perm + "' khỏi '" + playerName + "'");
        return true;
    }

    private boolean handleCheckCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6[VNMine] §cSử dụng: /vnmine perm check <player> <permission>");
            return true;
        }
        String playerName = args[1];
        String node = args[2];
        Player player = Bukkit.getPlayerExact(playerName);
        boolean hasPerm = false;

        if (player != null && player.isOnline()) {
            hasPerm = permissionManager.hasPermission(player, node);
        } else {
            // Offline check using groups
            List<String> groups = permissionManager.getPlayerGroups(playerName);
            PlayerData data = permissionManager.getPlayerData(playerName);
            Set<String> allPerms = new HashSet<>();
            for (String groupName : groups) {
                Group group = permissionManager.getGroup(groupName);
                if (group != null) {
                    allPerms.addAll(group.getPermissions());
                }
            }
            if (data != null) {
                allPerms.addAll(data.getPermissions());
            }
            // Simple check without Player object
            for (String perm : allPerms) {
                if (perm.equals(node) || (perm.endsWith(".*") && node.startsWith(perm.substring(0, perm.length() - 2)))) {
                    hasPerm = true;
                    break;
                }
            }
        }

        sender.sendMessage("§6=== Permission Check ===");
        sender.sendMessage("§fPlayer: §e" + playerName);
        sender.sendMessage("§fNode: §e" + node);
        sender.sendMessage("§fResult: " + (hasPerm ? "§aCÓ" : "§cKHÔNG"));
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        permissionManager.load();
        sender.sendMessage("§6[VNMine] §aĐã reload permission config!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("group");
            completions.add("player");
            completions.add("check");
            completions.add("reload");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("group")) {
                completions.add("list");
                completions.add("info");
                completions.add("create");
                completions.add("delete");
                completions.add("setweight");
                completions.add("setprefix");
                completions.add("setsuffix");
                completions.add("addperm");
                completions.add("removeperm");
                completions.add("addparent");
                completions.add("removeparent");
                completions.add("setdefault");
            } else if (args[0].equalsIgnoreCase("player")) {
                completions.add("info");
                completions.add("setgroup");
                completions.add("addperm");
                completions.add("removeperm");
            } else if (args[0].equalsIgnoreCase("check")) {
                // Suggest online players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group") && (args[1].equalsIgnoreCase("info") ||
                    args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("setweight") ||
                    args[1].equalsIgnoreCase("setprefix") || args[1].equalsIgnoreCase("setsuffix") ||
                    args[1].equalsIgnoreCase("addperm") || args[1].equalsIgnoreCase("removeperm") ||
                    args[1].equalsIgnoreCase("addparent") || args[1].equalsIgnoreCase("removeparent") ||
                    args[1].equalsIgnoreCase("setdefault"))) {
                completions.addAll(permissionManager.getGroups().keySet());
            } else if (args[0].equalsIgnoreCase("player") && (args[1].equalsIgnoreCase("info") ||
                    args[1].equalsIgnoreCase("setgroup") || args[1].equalsIgnoreCase("addperm") ||
                    args[1].equalsIgnoreCase("removeperm"))) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                // Suggest common permission nodes
                completions.add("vnmine.time.set");
                completions.add("vnmine.time.toggle");
                completions.add("vnmine.time.status");
                completions.add("vnmine.command.tps");
                completions.add("vnmine.command.saveall");
                completions.add("vnmine.command.reload");
                completions.add("vnmine.perm.admin");
                completions.add("vnmine.world.gen");
                completions.add("vnmine.drop.toggle");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group") && (args[1].equalsIgnoreCase("addparent") ||
                    args[1].equalsIgnoreCase("setweight") || args[1].equalsIgnoreCase("setprefix") ||
                    args[1].equalsIgnoreCase("setsuffix") || args[1].equalsIgnoreCase("addperm") ||
                    args[1].equalsIgnoreCase("removeperm"))) {
                // No specific completions for these values
            } else if (args[0].equalsIgnoreCase("player") && args[1].equalsIgnoreCase("setgroup")) {
                completions.addAll(permissionManager.getGroups().keySet());
            } else if (args[0].equalsIgnoreCase("player") && (args[1].equalsIgnoreCase("addperm") ||
                    args[1].equalsIgnoreCase("removeperm"))) {
                completions.add("vnmine.*");
                completions.add("vnmine.time.set");
                completions.add("vnmine.time.toggle");
                completions.add("vnmine.command.tps");
            }
        }

        return completions;
    }
}