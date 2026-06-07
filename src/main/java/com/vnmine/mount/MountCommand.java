package com.vnmine.mount;

import com.vnmine.VNMinePlugin;
import com.vnmine.mount.MountManager.MountConfig;
import com.vnmine.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * MountCommand - /mount summon|dismiss|list
 */
public class MountCommand implements CommandExecutor {

    private final VNMinePlugin plugin;
    private final MountManager mountManager;

    public MountCommand(VNMinePlugin plugin, MountManager mountManager) {
        this.plugin = plugin;
        this.mountManager = mountManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "summon":
                if (args.length < 2) {
                    MessageUtils.send(player, "&cSử dụng: /mount summon <id>");
                    MessageUtils.send(player, "&7Các ID: PHUONG_HOANG, BACH_HO, THANH_LONG");
                    return true;
                }
                mountManager.summonMount(player, args[1].toUpperCase());
                return true;
            case "dismiss":
            case "huy":
                mountManager.dismissMount(player);
                MessageUtils.send(player, "&7Đã hủy tọa kỵ.");
                return true;
            case "list":
                listMounts(player);
                return true;
            default:
                sendHelp(player);
                return true;
        }
    }

    private void sendHelp(Player player) {
        MessageUtils.send(player, "&6=== Tọa Kỵ Phi Hành ===");
        MessageUtils.send(player, "&e/mount summon <id> &f- Triệu hồi tọa kỵ");
        MessageUtils.send(player, "&e/mount dismiss &f- Hủy tọa kỵ");
        MessageUtils.send(player, "&e/mount list &f- Danh sách tọa kỵ");
        MessageUtils.send(player, "&7ID: PHUONG_HOANG (C30), BACH_HO (C40), THANH_LONG (C50)");
        MessageUtils.send(player, "&7Linh lực tiêu hao: &b3/giây");
    }

    private void listMounts(Player player) {
        MessageUtils.send(player, "&6=== Tọa Kỵ Của Bạn ===");
        for (MountConfig config : mountManager.getMountConfigs()) {
            boolean unlocked = player.getScoreboardTags().contains("vnmine_mount_" + config.id.toLowerCase());
            String status = unlocked ? "&a✓ Đã mở" : "&c✗ Chưa mở";
            MessageUtils.send(player, (unlocked ? "&a" : "&7") + "• " + config.name + " &7(Cấp " + config.requiredLevel + ") " + status);
        }
    }
}