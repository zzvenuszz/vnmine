package com.vnmine.util;

import com.vnmine.cultivation.CultivationManager;
import com.vnmine.cultivation.PlayerCultivationData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * NameTagManager - Quản lý hiển thị tên người chơi kèm prefix tu tiên
 * Cập nhật DisplayName, PlayerListName và CustomName (NameTag trên đầu)
 */
public class NameTagManager {

    private final CultivationManager cultivationManager;

    public NameTagManager(CultivationManager cultivationManager) {
        this.cultivationManager = cultivationManager;
    }

    /**
     * Cập nhật tất cả các hiển thị tên: DisplayName, PlayerListName, CustomName, Team prefix
     */
    public void updateNameTag(Player player) {
        if (player == null) return;

        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        String prefix = getPrefix(data);
        String playerName = player.getName();

        // 1. DisplayName - Hiển thị trong chat, title, actionbar, item lore...
        String displayName = ColorUtils.colorize(prefix + "&r&f]&r " + playerName);
        player.setDisplayName(displayName);

        // 2. PlayerListName - Hiển thị trong danh sách người chơi (Tab list)
        player.setPlayerListName(ColorUtils.colorize(prefix + "&r&f]&r " + playerName));

        // 3. CustomName - NameTag trên đầu nhân vật
        player.setCustomName(ColorUtils.colorize(prefix + "&r&f]&r " + playerName));
        player.setCustomNameVisible(true);

        // 4. Team prefix để nhiều plugin khác nhận diện
        updateTeamNameTag(player, prefix);
    }

    /**
     * Cập nhật Team prefix thông qua Scoreboard
     * Giúp các plugin survival, chat, tab list khác hiển thị đúng
     */
    private void updateTeamNameTag(Player player, String prefix) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "vn_" + player.getName();

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.setPrefix("");
        team.addEntry(player.getName());
    }

    /**
     * Xóa Team name tag khi player logout
     */
    public void removeTeam(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "vn_" + player.getName();

        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.unregister();
        }
    }

    /**
     * Lấy prefix tu tiên từ data
     */
    private String getPrefix(PlayerCultivationData data) {
        if (data == null) {
            return "&7[Phàm Nhân";
        }
        return data.getRealmPrefix();
    }
}