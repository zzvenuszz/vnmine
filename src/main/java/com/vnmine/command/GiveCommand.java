package com.vnmine.command;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.skill.SkillBookListener;
import com.vnmine.skill.SkillGrade;
import com.vnmine.skill.SkillQuality;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * GiveCommand - /vngive <player> <type> <id> [amount]
 * Types: skill, pill, artifact, currency, exp, level, mount
 */
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

        if (args.length < 3) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cNgười chơi '" + args[0] + "' không trực tuyến!");
            return true;
        }

        String type = args[1].toLowerCase();
        String id = args[2].toUpperCase();
        int amount = 1;
        if (args.length >= 4) {
            try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
        }

        // Optional: grade and quality for skillbook
        String gradeStr = args.length >= 5 ? args[4].toUpperCase() : "HOANG";
        String qualityStr = args.length >= 6 ? args[5].toUpperCase() : "HA";

        switch (type) {
            case "skill":
                return giveSkill(sender, target, id);
            case "skillbook":
                return giveSkillBook(sender, target, id, gradeStr, qualityStr, amount);
            case "pill":
                return givePill(sender, target, id, amount);
            case "artifact":
                return giveArtifact(sender, target, id);
            case "currency":
                return giveCurrency(sender, target, amount);
            case "exp":
                return giveExp(sender, target, amount);
            case "level":
                return giveLevel(sender, target, amount);
            case "mount":
                return giveMount(sender, target, id);
            default:
                sender.sendMessage("§cLoại không hợp lệ! Các loại: skill, skillbook, pill, artifact, currency, exp, level, mount");
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNGive Commands ===");
        sender.sendMessage("§e/vngive <player> skill <skill_id> §f- Học công pháp");
        sender.sendMessage("§e/vngive <player> skillbook <skill_id> [amount] [grade] [quality] §f- Give sách");
        sender.sendMessage("§e/vngive <player> pill <pill_id> [amount] §f- Give đan dược");
        sender.sendMessage("§e/vngive <player> artifact <art_id> §f- Give pháp bảo");
        sender.sendMessage("§e/vngive <player> currency <amount> §f- Give linh thạch");
        sender.sendMessage("§e/vngive <player> exp <amount> §f- Give EXP tu luyện");
        sender.sendMessage("§e/vngive <player> level <level> §f- Set cấp độ");
        sender.sendMessage("§e/vngive <player> mount <mount_id> §f- Mở khóa tọa kỵ");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Skill ID ===");
        sender.sendMessage("§eXem trong skills.yml");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Grade ===");
        sender.sendMessage("§eHOANG, HUYEN, DIA, THIEN");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Quality ===");
        sender.sendMessage("§eHA (20%), TRUNG (60%), THUONG (90%)");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Pill ID ===");
        sender.sendMessage("§eHOI_LINH_DAN, DAI_HOI_LINH_DAN, CUONG_THE_DAN, THANH_TAM_DAN, TOC_THANH_DAN, TU_LUYEN_DAN, PHI_THANG_DAN");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Artifact ID ===");
        sender.sendMessage("§eFLYING_SWORD, SPIRIT_BELL, BAGUA_MIRROR, SOUL_JADE, HEAVEN_SHIELD, THUNDER_SEAL, PHOENIX_REBIRTH");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Mount ID ===");
        sender.sendMessage("§ePHUONG_HOANG, BACH_HO, THANH_LONG");
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

    private boolean givePill(CommandSender sender, Player target, String pillId, int amount) {
        ItemStack pill = createPillItem(pillId, amount);
        if (pill == null) {
            sender.sendMessage("§cPill ID '" + pillId + "' không hợp lệ!");
            return true;
        }
        target.getInventory().addItem(pill);
        MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + "x đan dược");
        sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + "x " + pillId);
        return true;
    }

    private boolean giveArtifact(CommandSender sender, Player target, String artifactId) {
        ItemStack artifact = createArtifactItem(artifactId);
        if (artifact == null) {
            sender.sendMessage("§cArtifact ID '" + artifactId + "' không hợp lệ!");
            return true;
        }
        target.getInventory().addItem(artifact);
        MessageUtils.send(target, "&6✦ Bạn nhận được pháp bảo!");
        sender.sendMessage("§aĐã cho " + target.getName() + " pháp bảo " + artifactId);
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

    private boolean giveMount(CommandSender sender, Player target, String mountId) {
        // Lưu mount vào metadata player (dùng ScoreboardTag để lưu)
        target.addScoreboardTag("vnmine_mount_" + mountId.toLowerCase());
        MessageUtils.send(target, "&6✦ Bạn đã mở khóa tọa kỵ: &e" + mountId);
        sender.sendMessage("§aĐã mở khóa tọa kỵ " + mountId + " cho " + target.getName());
        return true;
    }

    // ==================== SKILL BOOK ====================
    
    private boolean giveSkillBook(CommandSender sender, Player target, String skillId, String gradeStr, String qualityStr, int amount) {
        try {
            SkillGrade grade = SkillGrade.fromString(gradeStr);
            SkillQuality quality = SkillQuality.fromString(qualityStr);
            
            SkillBookListener bookListener = plugin.getSkillManager().getBookListener();
            if (bookListener == null) {
                sender.sendMessage("§cHệ thống sách kỹ năng chưa sẵn sàng!");
                return true;
            }
            
            ItemStack book = bookListener.createSkillBook(skillId, grade, quality);
            if (book == null) {
                sender.sendMessage("§cSkill ID '" + skillId + "' không hợp lệ!");
                return true;
            }
            book.setAmount(Math.max(1, Math.min(amount, 64)));
            
            target.getInventory().addItem(book);
            MessageUtils.send(target, "&6✦ Bạn nhận được sách công pháp: &e" + skillId);
            sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + "x sách " + skillId + " (" + gradeStr + "/" + qualityStr + ")");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§cLỗi: " + e.getMessage());
            return true;
        }
    }

    // ==================== CREATE ITEMS ====================

    /**
     * Helper: thêm NBT tag "vnmine_item" để chặn đặt block
     */
    private ItemBuilder tagItem(ItemBuilder builder) {
        return builder.setPersistentData("vnmine_item", "true");
    }

    private ItemStack createPillItem(String pillId, int amount) {
        switch (pillId.toUpperCase()) {
            case "HOI_LINH_DAN":
                return tagItem(new ItemBuilder(Material.GLOWSTONE_DUST).setName("&aHồi Linh Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Hồi phục &b30 &7linh lực ngay lập tức")).build();
            case "DAI_HOI_LINH_DAN":
                return tagItem(new ItemBuilder(Material.GLOWSTONE).setName("&bĐại Hồi Linh Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Hồi phục &b100 &7linh lực + 20% hồi phục 30s")).build();
            case "CUONG_THE_DAN":
                return tagItem(new ItemBuilder(Material.REDSTONE_BLOCK).setName("&cCương Thể Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Tăng &c20% sát thương &7trong 60 giây")).build();
            case "THANH_TAM_DAN":
                return tagItem(new ItemBuilder(Material.SUGAR).setName("&aThanh Tâm Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Giải trừ mọi trạng thái xấu")).build();
            case "TOC_THANH_DAN":
                return tagItem(new ItemBuilder(Material.FEATHER).setName("&bTốc Thánh Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Tăng &b50% tốc độ &7trong 30 giây")).build();
            case "TU_LUYEN_DAN":
                return tagItem(new ItemBuilder(Material.PURPLE_DYE).setName("&5Tu Luyện Đan")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7Tăng &5+50 EXP &7khi sử dụng")).build();
            case "PHI_THANG_DAN":
                return tagItem(new ItemBuilder(Material.NETHER_STAR).setName("&6&l◆ Phi Thăng Đan ◆")
                        .setAmount(amount).setGlow(true)
                        .setLore("", "&7+500 EXP (1 lần/đại cảnh giới)")).build();
            default:
                return null;
        }
    }

    private ItemStack createArtifactItem(String artifactId) {
        switch (artifactId.toUpperCase()) {
            case "FLYING_SWORD":
                return tagItem(new ItemBuilder(Material.DIAMOND_SWORD).setName("&b&l◆ Kiếm Phi Hành ◆").setGlow(true)
                        .setLore("", "&7Click phải để ngự kiếm phi hành", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "SPIRIT_BELL":
                return tagItem(new ItemBuilder(Material.BELL).setName("&6&l◆ Linh Chung ◆").setGlow(true)
                        .setLore("", "&7Click phải: Làm choáng quái AOE", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "BAGUA_MIRROR":
                return tagItem(new ItemBuilder(Material.SHIELD).setName("&5&l◆ Bát Quái Kính ◆").setGlow(true)
                        .setLore("", "&7Cầm tay: Giảm 30% sát thương", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "SOUL_JADE":
                return tagItem(new ItemBuilder(Material.EMERALD).setName("&a&l◆ Hồn Ngọc ◆").setGlow(true)
                        .setLore("", "&7Tự động hồi 50% máu khi HP<20%", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "HEAVEN_SHIELD":
                return tagItem(new ItemBuilder(Material.NETHERITE_CHESTPLATE).setName("&4&l◆ Thiên Linh Thuẫn ◆").setGlow(true)
                        .setLore("", "&7Kích hoạt: Bất tử 5 giây", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "THUNDER_SEAL":
                return tagItem(new ItemBuilder(Material.TRIDENT).setName("&e&l◆ Lôi Ấn ◆").setGlow(true)
                        .setLore("", "&7Click quái: Gọi sét đánh", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            case "PHOENIX_REBIRTH":
                return tagItem(new ItemBuilder(Material.FEATHER).setName("&6&l◆ Phượng Hoàng Lệnh ◆").setGlow(true)
                        .setLore("", "&7Tự động hồi sinh 1 lần", "", "&6&l✦ Pháp bảo thượng phẩm ✦")).build();
            default:
                return null;
        }
    }
}