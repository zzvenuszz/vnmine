package com.vnmine.currency;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CurrencyCommand - /vnbalance, /vnpay
 */
public class CurrencyCommand implements CommandExecutor {

    private final VNMinePlugin plugin;
    private final CurrencyManager currencyManager;

    public CurrencyCommand(VNMinePlugin plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "vnbalance":
                return handleBalance(sender, args);
            case "vnpay":
                return handlePay(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }
        Player player = (Player) sender;
        int balance = currencyManager.countCurrency(player);
        MessageUtils.send(player, "&6◆ Số dư Linh Thạch: &b" + balance + " &6◆");
        MessageUtils.send(player, "&7Bạn có thể kiểm tra bằng cách mở túi đồ.");
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể dùng lệnh này!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cSử dụng: /vnpay <người_chơi> <số_lượng>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cNgười chơi '" + args[0] + "' không trực tuyến!");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage("§cSố lượng phải lớn hơn 0!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cSố lượng không hợp lệ!");
            return true;
        }

        if (!currencyManager.withdraw(player, amount)) {
            MessageUtils.send(player, "&cBạn không có đủ Linh Thạch! (Cần: &b" + amount + "&c)");
            return true;
        }

        currencyManager.deposit(target, amount);
        MessageUtils.send(player, "&a✦ Bạn đã chuyển &b" + amount + " &aLinh Thạch cho &e" + target.getName());
        MessageUtils.send(target, "&a✦ &e" + player.getName() + " &ađã chuyển cho bạn &b" + amount + " &aLinh Thạch!");
        return true;
    }
}