package com.vnmine.command;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PillConfig;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.spiritfarm.SpiritHerb;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import java.util.Map;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * GiveCommand - /vngive <player> <type> <id> [amount] [ageCode]
 * Types: skill, pill, artifact, herb, currency, exp, level, mount
 */
public class GiveCommand implements CommandExecutor {

    private final VNMinePlugin plugin;
    
    private static final int DEFAULT_CHARGES = 10;
    private static final String[] GRADE_DISPLAY = {
        "&7&oHoàng cấp &fHạ phẩm", "&7&oHoàng cấp &eTrung phẩm", "&7&oHoàng cấp &aThượng phẩm",
        "&b&oHuyền cấp &fHạ phẩm", "&b&oHuyền cấp &eTrung phẩm", "&b&oHuyền cấp &aThượng phẩm",
        "&5&oĐịa cấp &fHạ phẩm", "&5&oĐịa cấp &eTrung phẩm", "&5&oĐịa cấp &aThượng phẩm",
        "&6&oThiên cấp &fHạ phẩm", "&6&oThiên cấp &eTrung phẩm", "&6&oThiên cấp &aThượng phẩm"
    };
    private static final Color[] GRADE_COLORS = {
        Color.WHITE, Color.YELLOW, Color.LIME,
        Color.AQUA, Color.ORANGE, Color.GREEN,
        Color.PURPLE, Color.fromRGB(0xFF00FF), Color.RED,
        Color.fromRGB(0xFFD700), Color.fromRGB(0x00FFFF), Color.fromRGB(0x8B00FF)
    };
    private static final double[] GRADE_MULTIPLIERS = {
        1.0, 1.3, 1.6, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 10.0
    };

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

        switch (type) {
            case "skill":
                return giveSkill(sender, target, id);
            case "pill":
                return givePill(sender, target, id, amount);
            case "artifact":
                return giveArtifact(sender, target, id);
            case "herb":
                int ageCode = 3; // Mặc định: 10 Năm (level 3)
                if (args.length >= 5) {
                    try { ageCode = Integer.parseInt(args[4]); } catch (NumberFormatException ignored) {}
                }
                return giveHerb(sender, target, id, amount, ageCode);
            case "currency":
                return giveCurrency(sender, target, amount);
            case "exp":
                return giveExp(sender, target, amount);
            case "level":
                return giveLevel(sender, target, amount);
            case "mount":
                return giveMount(sender, target, id);
            default:
                sender.sendMessage("§cLoại không hợp lệ! Các loại: skill, pill, artifact, herb, currency, exp, level, mount");
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== VNGive Commands ===");
        sender.sendMessage("§e/vngive <player> skill <skill_id> §f- Học công pháp");
        sender.sendMessage("§e/vngive <player> pill <pill_id> [amount] §f- Give đan dược");
        sender.sendMessage("§e/vngive <player> artifact <art_id> §f- Give pháp bảo");
        sender.sendMessage("§e/vngive <player> herb <herb_id> [amount] [age_code] §f- Give linh thảo");
        sender.sendMessage("§e/vngive <player> currency <amount> §f- Give linh thạch");
        sender.sendMessage("§e/vngive <player> exp <amount> §f- Give EXP tu luyện");
        sender.sendMessage("§e/vngive <player> level <level> §f- Set cấp độ");
        sender.sendMessage("§e/vngive <player> mount <mount_id> §f- Mở khóa tọa kỵ");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Herb ID ===");
        sender.sendMessage("§eHạ Phẩm: LINH_THAO, NGUYET_QUANG_THAO, BINH_LINH_THAO, LAM_LINH_THAO, LOI_LINH_THAO");
        sender.sendMessage("§eTrung Phẩm: HUYEN_BINH_THAO, HUYET_LINH_THAO, HOA_LINH_THAO, HAC_LINH_THAO, KIM_LINH_THAO");
        sender.sendMessage("§eThượng Phẩm: LONG_HUYET_THAO, THIEN_LINH_THAO, PHUNG_LINH_THAO, VAN_NIEN_LINH_CHI, LUYEN_THAN_THAO");
        sender.sendMessage("§eTiên Phẩm: TIEN_THAO, LONG_LINH_THAO, THANH_LONG_THAO, HONG_MONG_THAO, THIEN_HA_THAO");
        sender.sendMessage("§eAge Code: 0=Mầm Non, 1=Trưởng Thành, 2=1 Năm, 3=10 Năm, 4=100 Năm, 5=1000 Năm, 6=1 Vạn Năm");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Skill ID ===");
        sender.sendMessage("§eBASIC_HEAL, QI_SHIELD, FIRE_BALL, WIND_BLADE, LIGHTNING_STRIKE, SPEED_STEP, TELEPORT, METEOR_STORM");
        sender.sendMessage("§eFIRE_CONTROL, FORGE_MASTERY");
        sender.sendMessage("");
        sender.sendMessage("§6=== Danh sách Pill ID ===");
        sender.sendMessage("§eHOI_LINH_DAN, DAI_HOI_LINH_DAN, CUONG_THE_DAN, THANH_TAM_DAN, TOC_THANH_DAN");
        sender.sendMessage("§eTU_LUYEN_DAN, PHI_THANG_DAN, BACH_DOC_DAN, THIEN_HOI_DAN, PHE_MA_DAN, TRUONG_THO_DAN");
        sender.sendMessage("§eKIM_CUONG_DAN, LINH_NHIEN_DAN, TIEM_HANH_DAN, PHAP_TUONG_DAN, THAN_LONG_DAN");
        sender.sendMessage("§eCUONG_LUC_DAN, HAN_BANG_DAN, LINH_PHONG_DAN, HOA_LONG_DAN, THIEN_LINH_DAN, DAC_COC_DAN, VO_THUONG_DAN");
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
        for (int i = 0; i < Math.min(amount, 64); i++) {
            ItemStack pill = createPillItem(pillId, 6); // Mặc định Thiên Thượng phẩm
            if (pill == null) {
                sender.sendMessage("§cPill ID '" + pillId + "' không hợp lệ!");
                return true;
            }
            Map<Integer, ItemStack> leftover = target.getInventory().addItem(pill);
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }
        MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + "x đan dược");
        sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + "x " + pillId);
        return true;
    }

    private boolean giveHerb(CommandSender sender, Player target, String herbId, int amount, int ageCode) {
        SpiritHerb herb = SpiritHerb.getHerb(herbId);
        if (herb == null) {
            sender.sendMessage("§cHerb ID '" + herbId + "' không hợp lệ!");
            return true;
        }
        SpiritHerb.HerbQuality quality = SpiritHerb.HerbQuality.fromAgeCode(ageCode);
        ItemStack herbItem = herb.createHerbItem(quality, amount);
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(herbItem);
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }
        MessageUtils.send(target, "&a✦ Bạn nhận được &e" + amount + "x " + herb.getName());
        sender.sendMessage("§aĐã cho " + target.getName() + " " + amount + "x " + herbId + " (age=" + ageCode + ")");
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
        target.addScoreboardTag("vnmine_mount_" + mountId.toLowerCase());
        MessageUtils.send(target, "&6✦ Bạn đã mở khóa tọa kỵ: &e" + mountId);
        sender.sendMessage("§aĐã mở khóa tọa kỵ " + mountId + " cho " + target.getName());
        return true;
    }

    // ==================== CREATE ITEMS ====================

    private ItemBuilder tagItem(ItemBuilder builder) {
        return builder.setPersistentData("vnmine_item", "true");
    }

    @SuppressWarnings("deprecation")
    private ItemStack createPillItem(String pillId, int gradeIndex) {
        if (plugin.getPillConfig() == null) return null;

        // Map pill ID to pill material
        Material mat;
        switch (pillId) {
            case "HOI_LINH_DAN": mat = Material.POTION; break;
            case "DAI_HOI_LINH_DAN": mat = Material.LINGERING_POTION; break;
            case "CUONG_THE_DAN": mat = Material.SPLASH_POTION; break;
            case "THANH_TAM_DAN": mat = Material.HONEY_BOTTLE; break;
            case "TOC_THANH_DAN": mat = Material.POTION; break;
            case "TU_LUYEN_DAN": mat = Material.EXPERIENCE_BOTTLE; break;
            case "PHI_THANG_DAN": mat = Material.DRAGON_BREATH; break;
            case "BACH_DOC_DAN": mat = Material.POTION; break;
            case "THIEN_HOI_DAN": mat = Material.LINGERING_POTION; break;
            case "PHE_MA_DAN": mat = Material.SPLASH_POTION; break;
            case "TRUONG_THO_DAN": mat = Material.HONEY_BOTTLE; break;
            case "KIM_CUONG_DAN": mat = Material.SPLASH_POTION; break;
            case "LINH_NHIEN_DAN": mat = Material.POTION; break;
            case "TIEM_HANH_DAN": mat = Material.POTION; break;
            case "PHAP_TUONG_DAN": mat = Material.LINGERING_POTION; break;
            case "THAN_LONG_DAN": mat = Material.DRAGON_BREATH; break;
            case "CUONG_LUC_DAN": mat = Material.SPLASH_POTION; break;
            case "HAN_BANG_DAN": mat = Material.LINGERING_POTION; break;
            case "LINH_PHONG_DAN": mat = Material.POTION; break;
            case "HOA_LONG_DAN": mat = Material.DRAGON_BREATH; break;
            case "THIEN_LINH_DAN": mat = Material.EXPERIENCE_BOTTLE; break;
            case "DAC_COC_DAN": mat = Material.HONEY_BOTTLE; break;
            case "VO_THUONG_DAN": mat = Material.DRAGON_BREATH; break;
            default: return null;
        }

        // Lấy display name và lore từ PillConfig
        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect(pillId);
        String effectLore = (effect != null) ? effect.getLore(GRADE_MULTIPLIERS[gradeIndex]) : "&7Đan dược quý giá";
        String flavorLore = plugin.getPillConfig().getRandomFlavor(pillId);
        String gradeDisplay = GRADE_DISPLAY[gradeIndex];

        // Build display name từ pillId
        String displayName;
        switch (pillId) {
            case "HOI_LINH_DAN": displayName = "&aHồi Linh Đan"; break;
            case "DAI_HOI_LINH_DAN": displayName = "&bĐại Hồi Linh Đan"; break;
            case "CUONG_THE_DAN": displayName = "&cCương Thể Đan"; break;
            case "THANH_TAM_DAN": displayName = "&aThanh Tâm Đan"; break;
            case "TOC_THANH_DAN": displayName = "&bTốc Thánh Đan"; break;
            case "TU_LUYEN_DAN": displayName = "&5Tu Luyện Đan"; break;
            case "PHI_THANG_DAN": displayName = "&6&l◆ Phi Thăng Đan ◆"; break;
            case "BACH_DOC_DAN": displayName = "&9Bách Độc Đan"; break;
            case "THIEN_HOI_DAN": displayName = "&6Thiên Hồi Đan"; break;
            case "PHE_MA_DAN": displayName = "&8Phê Ma Đan"; break;
            case "TRUONG_THO_DAN": displayName = "&6Trường Thọ Đan"; break;
            case "KIM_CUONG_DAN": displayName = "&bKim Cương Đan"; break;
            case "LINH_NHIEN_DAN": displayName = "&aLinh Nhiên Đan"; break;
            case "TIEM_HANH_DAN": displayName = "&8Tiềm Hành Đan"; break;
            case "PHAP_TUONG_DAN": displayName = "&5Pháp Tướng Đan"; break;
            case "THAN_LONG_DAN": displayName = "&6&l◆ Thần Long Đan ◆"; break;
            case "CUONG_LUC_DAN": displayName = "&cCường Lực Đan"; break;
            case "HAN_BANG_DAN": displayName = "&bHàn Băng Đan"; break;
            case "LINH_PHONG_DAN": displayName = "&aLinh Phong Đan"; break;
            case "HOA_LONG_DAN": displayName = "&6&l◆ Hóa Long Đan ◆"; break;
            case "THIEN_LINH_DAN": displayName = "&d&l◆ Thiên Linh Đan ◆"; break;
            case "DAC_COC_DAN": displayName = "&5Đặc Cốc Đan"; break;
            case "VO_THUONG_DAN": displayName = "&4&l◆ Vô Thượng Đan ◆"; break;
            default: return null;
        }

        ItemBuilder builder = new ItemBuilder(mat)
                .setName(displayName)
                .setGlow(true)
                .setPersistentData("vnmine_pill_type", pillId)
                .setPersistentData("vnmine_pill_charges", String.valueOf(DEFAULT_CHARGES))
                .setPersistentData("vnmine_pill_grade", String.valueOf(gradeIndex))
                .setLore("",
                        gradeDisplay,
                        effectLore,
                        flavorLore,
                        "",
                        "&7Lượng dùng: &e" + DEFAULT_CHARGES + "/" + DEFAULT_CHARGES + " &7lần",
                        "&a✦ Click phải để sử dụng!");

        // Set màu potion
        if (mat == Material.POTION || mat == Material.LINGERING_POTION || mat == Material.SPLASH_POTION) {
            builder.setPotionColor(GRADE_COLORS[gradeIndex]);
            ItemStack temp = builder.build();
            if (temp.getItemMeta() instanceof PotionMeta meta) {
                meta.addCustomEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 0, 0, true, false, false), true);
                temp.setItemMeta(meta);
                return temp;
            }
        }

        builder.hideAll();
        return tagItem(builder).build();
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