package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeditationCommand implements CommandExecutor {
    private final VNMinePlugin plugin;
    private final MeditationManager meditationManager;

    public MeditationCommand(VNMinePlugin plugin, MeditationManager meditationManager) {
        this.plugin = plugin;
        this.meditationManager = meditationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Chỉ người chơi mới dùng tọa thiền!");
            return true;
        }
        Player player = (Player) sender;
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section == null || !section.getBoolean("enabled", true)) {
            player.sendMessage("Tọa thiền đang tắt!");
            return true;
        }
        meditationManager.handleSneakChange(player, true);
        return true;
    }
}
