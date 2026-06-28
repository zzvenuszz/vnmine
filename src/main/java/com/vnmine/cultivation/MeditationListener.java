package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.inventory.ClickType;
import com.vnmine.util.ColorUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class MeditationListener implements Listener {
    private final VNMinePlugin plugin;
    private final MeditationManager meditationManager;

    public MeditationListener(VNMinePlugin plugin, MeditationManager meditationManager) {
        this.plugin = plugin;
        this.meditationManager = meditationManager;
    }

    private void tryExitMeditation(Player player) {
        if (meditationManager.isMeditating(player)) {
            meditationManager.stopMeditation(player.getUniqueId(), false);
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section != null && !section.getBoolean("enabled", true)) return;
        if (meditationManager.isMeditating(player)) {
            meditationManager.stopMeditation(player.getUniqueId(), false);
            return;
        }
        if (!event.isSneaking()) return;
        meditationManager.handleSneakChange(player, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String guiTitle = "✧ Tọa Thiền ✧";
        String actualTitle = event.getView().getTitle();
        plugin.getLogger().info("[MeditationListener] ENTRY titleCheck=" + guiTitle + " actual=" + actualTitle + " slot=" + event.getRawSlot() + " click=" + event.getClick());
        
        if (actualTitle == null || !ColorUtils.stripColor(actualTitle).contains(guiTitle)) {
            plugin.getLogger().info("[MeditationListener] TITLE MISMATCH - returning");
            return;
        }
        
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
        plugin.getLogger().info("[MeditationListener] CANCELLED click");

        ClickType click = event.getClick();
        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ||
            click == ClickType.DOUBLE_CLICK || click == ClickType.DROP ||
            click == ClickType.CONTROL_DROP ||
            click == ClickType.NUMBER_KEY || click == ClickType.WINDOW_BORDER_LEFT ||
            click == ClickType.WINDOW_BORDER_RIGHT) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            player.closeInventory();
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            player.closeInventory();
            return;
        }

        if (slot == 2) {
            meditationManager.confirmMeditation(player);
            player.closeInventory();
        } else if (slot == 6) {
            meditationManager.cancelMeditation(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tryExitMeditation(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        tryExitMeditation(event.getPlayer());
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            tryExitMeditation(player);
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player) {
            tryExitMeditation(player);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        tryExitMeditation(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section == null || !section.getBoolean("cancel-on-interact", true)) return;
        if (!meditationManager.isMeditating(player)) return;

        Block block = event.getClickedBlock();
        if (block != null) {
            Material type = block.getType();
            if (isInteractableBlock(type)) {
                tryExitMeditation(player);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section == null || !section.getBoolean("cancel-on-interact", true)) return;
        if (!meditationManager.isMeditating(player)) return;

        String title = event.getView().getTitle();
        if (title != null && title.contains("Luyện Đan")) return;
        if (title != null && title.contains("Tọa Thiền")) return;

        tryExitMeditation(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section == null || !section.getBoolean("cancel-on-move", true)) return;
        if (!meditationManager.isMeditating(player)) return;

        var from = event.getFrom();
        var to = event.getTo();
        if (to == null) return;

        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 0.1) {
            tryExitMeditation(player);
        }
    }

    private boolean isInteractableBlock(Material type) {
        return type == Material.CHEST
                || type == Material.TRAPPED_CHEST
                || type == Material.FURNACE
                || type == Material.BLAST_FURNACE
                || type == Material.SMOKER
                || type == Material.CRAFTING_TABLE
                || type == Material.ANVIL
                || type == Material.CHIPPED_ANVIL
                || type == Material.DAMAGED_ANVIL
                || type == Material.ENCHANTING_TABLE
                || type == Material.ENDER_CHEST
                || type == Material.BARREL
                || type == Material.HOPPER
                || type == Material.DROPPER
                || type == Material.DISPENSER
                || type == Material.NOTE_BLOCK
                || type == Material.JUKEBOX
                || type == Material.LOOM
                || type == Material.SMITHING_TABLE
                || type == Material.GRINDSTONE
                || type == Material.STONECUTTER
                || type == Material.LECTERN
                || type == Material.BELL
                || type == Material.COMPOSTER
                || type == Material.CAKE
                || type == Material.LEVER
                || type == Material.STONE_BUTTON
                || type == Material.POLISHED_BLACKSTONE_BUTTON
                || type.name().contains("_DOOR")
                || type.name().contains("_TRAPDOOR")
                || type.name().contains("_FENCE_GATE")
                || type.name().contains("_BUTTON");
    }
}
