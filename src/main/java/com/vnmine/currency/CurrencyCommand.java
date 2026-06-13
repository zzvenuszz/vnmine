package com.vnmine.currency;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            case "vnexchange":
                return handleExchange(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("§cChi nguoi choi moi co the dung lenh nay!"); return true; }
        Player player = (Player) sender;
        int ha = currencyManager.countCurrencyHa(player);
        int trung = currencyManager.countCurrencyTrung(player);
        int thuong = currencyManager.countCurrencyThuong(player);
        int total = currencyManager.countTotalValue(player);
        MessageUtils.send(player, "&6◆ So du Linh Thach:");
        MessageUtils.send(player, "  &b" + ha + " &7Linh Thach Ha Pham");
        MessageUtils.send(player, "  &d" + trung + " &7Linh Thach Trung Pham");
        MessageUtils.send(player, "  &6" + thuong + " &7Linh Thach Thuong Pham");
        MessageUtils.send(player, "  &7Tong gia tri: &e" + total + " &7Linh Thach Ha Pham");
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("§cChi nguoi choi moi co the dung lenh nay!"); return true; }
        if (args.length < 2) { MessageUtils.send((Player) sender, "&cSu dung: /vnpay <nguoi_choi> <so_luong>"); return true; }
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { MessageUtils.send(player, "&cNguoi choi '" + args[0] + "' khong truc tuyen!"); return true; }
        int amount;
        try { amount = Integer.parseInt(args[1]); if (amount <= 0) { MessageUtils.send(player, "&cSo luong phai lon hon 0!"); return true; } }
        catch (NumberFormatException e) { MessageUtils.send(player, "&cSo luong khong hop le!"); return true; }
        if (!currencyManager.withdrawHa(player, amount)) { MessageUtils.send(player, "&cBan khong du Linh Thach! (Can: &b" + amount + "&c)"); return true; }
        currencyManager.depositHa(target, amount);
        MessageUtils.send(player, "&a✦ Ban da chuyen &b" + amount + " &aLinh Thach cho &e" + target.getName());
        MessageUtils.send(target, "&a✦ &e" + player.getName() + " &ada chuyen cho ban &b" + amount + " &aLinh Thach!");
        return true;
    }

    private boolean handleExchange(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("§cChi nguoi choi moi co the dung lenh nay!"); return true; }
        Player player = (Player) sender;

        // /vnexchange - mo menu huong dan
        if (args.length == 0) {
            int ha = currencyManager.countCurrencyHa(player);
            int trung = currencyManager.countCurrencyTrung(player);
            int thuong = currencyManager.countCurrencyThuong(player);
            int loss = currencyManager.getExchangeDowngradeLossPercent();
            MessageUtils.send(player, "&6&l===== DOI TIEN LINH THACH =====");
            MessageUtils.send(player, "&7Hien tai: &b" + ha + " Ha &7| &d" + trung + " Trung &7| &6" + thuong + " Thuong");
            MessageUtils.send(player, "");
            MessageUtils.send(player, "&eNang cap (len don vi lon hon):");
            MessageUtils.send(player, "  &7/vnexchange ha-trung <so_luong>  → &b100 Ha → 1 Trung");
            MessageUtils.send(player, "  &7/vnexchange trung-thuong <so_luong> → &d100 Trung → 1 Thuong");
            MessageUtils.send(player, "");
            MessageUtils.send(player, "&cHa cap xuong (xuong don vi nho hon):");
            MessageUtils.send(player, "  &7/vnexchange trung-ha <so_luong>    → &d1 Trung → " + (100 - loss) + " Ha &c(mat " + loss + "%)");
            MessageUtils.send(player, "  &7/vnexchange thuong-trung <so_luong> → &61 Thuong → " + (100 - loss) + " Trung &c(mat " + loss + "%)");
            MessageUtils.send(player, "");
            MessageUtils.send(player, "&a/ vnbalance  → Xem so du chi tiet");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.send(player, "&cSu dung: /vnexchange <loai> <so_luong>");
            MessageUtils.send(player, "&7Loai: ha-trung, trung-ha, trung-thuong, thuong-trung");
            return true;
        }

        String type = args[0].toLowerCase();
        int amount;
        try { amount = Integer.parseInt(args[1]); if (amount <= 0) { MessageUtils.send(player, "&cSo luong phai lon hon 0!"); return true; } }
        catch (NumberFormatException e) { MessageUtils.send(player, "&cSo luong khong hop le!"); return true; }

        String fromTier = null;
        String toTier = null;
        switch (type) {
            case "ha-trung": fromTier = "HA"; toTier = "TRUNG"; break;
            case "trung-ha": fromTier = "TRUNG"; toTier = "HA"; break;
            case "trung-thuong": fromTier = "TRUNG"; toTier = "THUONG"; break;
            case "thuong-trung": fromTier = "THUONG"; toTier = "TRUNG"; break;
            default:
                MessageUtils.send(player, "&cLoai doi khong hop le! Dung: ha-trung, trung-ha, trung-thuong, thuong-trung");
                return true;
        }

        // Calculate output for preview
        CurrencyManager.CurrencyTier from = currencyManager.getTier(fromTier);
        CurrencyManager.CurrencyTier to = currencyManager.getTier(toTier);
        double ratio = (double) from.value / (double) to.value;
        int output = (int) Math.floor(amount * ratio);
        if (from.value > to.value) {
            double loss = 1.0 - (currencyManager.getExchangeDowngradeLossPercent() / 100.0);
            output = (int) Math.floor(output * loss);
        }

        if (output <= 0) { MessageUtils.send(player, "&cSo luong qua nho de doi!"); return true; }

        // Check balance
        int fromCount = 0;
        switch (fromTier) {
            case "HA": fromCount = currencyManager.countCurrencyHa(player); break;
            case "TRUNG": fromCount = currencyManager.countCurrencyTrung(player); break;
            case "THUONG": fromCount = currencyManager.countCurrencyThuong(player); break;
        }
        if (fromCount < amount) {
            MessageUtils.send(player, "&cBan khong du &e" + from.name + "&c! (Co: &7" + fromCount + "&c, can: &e" + amount + "&c)");
            return true;
        }

        // Execute exchange
        boolean success = currencyManager.exchange(player, fromTier, toTier, amount);
        if (success) {
            MessageUtils.send(player, "&a✦ Doi tien thanh cong!");
            MessageUtils.send(player, "  &7Tra: &e" + amount + " " + ColorUtils.colorize(from.name));
            MessageUtils.send(player, "  &7Nhan: &a" + output + " " + ColorUtils.colorize(to.name));
            MessageUtils.playSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP);
        } else {
            MessageUtils.send(player, "&cDoi tien that bai! Vui long thu lai.");
        }

        return true;
    }
}