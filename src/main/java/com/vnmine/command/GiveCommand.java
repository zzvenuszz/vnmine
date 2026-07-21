package com.vnmine.command;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.ItemDefinition;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class GiveCommand implements CommandExecutor {

    private final VNMinePlugin plugin;

    public GiveCommand(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vnmine.command.give")) {
            sender.sendMessage("§cBạn không có quyền!");
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        // /vngive <player> <item_id|type> [amount]
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cNgười chơi '" + args[0] + "' không trực tuyến!");
            return true;
        }

        String idOrType = args[1].toUpperCase();
        int amount = 1;
        if (args.length >= 3) {
            try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
        }

        // Check if first arg is a type prefix (old style: /vngive <player> <type> <id>)
        String[] TYPE_PREFIXES = {"PILL", "HERB", "ARTIFACT", "MOUNT", "MATERIAL", "CURRENCY", "EXP", "LEVEL"};
        String originalIdOrType = idOrType;
        boolean isTypePrefix = false;
        for (String t : TYPE_PREFIXES) {
            if (idOrType.equals(t)) { isTypePrefix = true; break; }
        }

        if (isTypePrefix && args.length >= 3) {
            // Old style: /vngive <player> <type> <id> [amount]
            idOrType = args[2].toUpperCase();
            if (args.length >= 4) {
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
            }
        }

        // Try to find item in ItemDataLoader first
        ItemDefinition def = plugin.getItemDataLoader().getItem(idOrType);
        if (def != null) {
            return giveFromDefinition(sender, target, def, amount);
        }

        // Fallback to special types (check original type prefix, not the converted idOrType)
        if (originalIdOrType.equals("SKILL") && args.length >= 3) {
            return giveSkill(sender, target, args[2].toUpperCase());
        }
        if (originalIdOrType.equals("CURRENCY")) {
            return giveCurrency(sender, target, amount);
        }
        if (originalIdOrType.equals("EXP")) {
            return giveExp(sender, target, amount);
        }
        if (originalIdOrType.equals("LEVEL")) {
            return giveLevel(sender, target, amount);
        }

        sender.sendMessage("§cKhông tìm thấy item ID '" + idOrType + "'! Dùng /vngive <player> <item_id> [amount]");
        sender.sendMessage("§7Item ID: HOI_LINH_DAN, LINH_THAO, FLYING_SWORD, BASIC_HEAL...");
        return true;
    }

    private boolean giveFromDefinition(CommandSender sender, Player target, ItemDefinition def, int amount) {
        amount = Math.min(amount, 64);
        ItemStack item = ItemBuilder.buildFromDefinition(def);
        item.setAmount(amount);

        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }
        MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + "x &f" + def.getName());
        sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + "x " + def.getId());
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNGive Commands ===");
        sender.sendMessage("§e/vngive <player> <item_id> [amount] §f- Give item từ config YML");
        sender.sendMessage("§eItem ID: HOI_LINH_DAN, LINH_THAO, FLYING_SWORD, BASIC_HEAL...");
        sender.sendMessage("");
        sender.sendMessage("§e/vngive <player> SKILL <skill_id> §f- Học công pháp");
        sender.sendMessage("§e/vngive <player> CURRENCY <amount> §f- Give linh thạch");
        sender.sendMessage("§e/vngive <player> EXP <amount> §f- Give EXP tu luyện");
        sender.sendMessage("§e/vngive <player> LEVEL <level> §f- Set cấp độ");
    }

    private boolean giveSkill(CommandSender sender, Player target, String skillId) {
        PlayerCultivationData data = plugin.getCultivationManager().getOrCreatePlayerData(
                target.getUniqueId(), target.getName());
        if (data.hasLearnedSkill(skillId)) {
            sender.sendMessage("§c" + target.getName() + " đã học kỹ năng này rồi!");
            return true;
        }
        data.learnSkill(skillId);
        MessageUtils.send(target, "&a✦ Admin đã dạy bạn công pháp: &e" + skillId);
        sender.sendMessage("§aĐã cho " + target.getName() + " học skill " + skillId);
        return true;
    }

    private boolean giveCurrency(CommandSender sender, Player target, int amount) {
        if (plugin.getCurrencyManager() != null) {
            plugin.getCurrencyManager().deposit(target, amount);
            MessageUtils.send(target, "&b✦ Bạn nhận được &e" + amount + " &bLinh Thạch!");
            sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + " Linh Thạch");
        }
        return true;
    }

    private boolean giveExp(CommandSender sender, Player target, int amount) {
        if (plugin.getCultivationManager() != null) {
            plugin.getCultivationManager().addExperience(target, amount);
            MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + " &atu vi!");
            sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + " EXP");
        }
        return true;
    }

    private boolean giveLevel(CommandSender sender, Player target, int level) {
        PlayerCultivationData data = plugin.getCultivationManager().getOrCreatePlayerData(
                target.getUniqueId(), target.getName());
        data.setLevel(Math.min(level, 100));
        data.setExperience(0);
        data.setMaxMana(100 + (level * 10));
        MessageUtils.send(target, "&d✦ Cấp độ của bạn đã được set thành &e" + level);
        sender.sendMessage("§aĐã set level " + target.getName() + " thành " + level);
        return true;
    }
}