package com.vnmine.drop;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BlockDropListener implements Listener {
    private final JavaPlugin plugin;
    private final DropManager dropManager;
    private final Random random;

    public BlockDropListener(JavaPlugin plugin, DropManager dropManager) {
        this.plugin = plugin;
        this.dropManager = dropManager;
        this.random = ThreadLocalRandom.current();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!dropManager.isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        // Get the rule for this block
        DropManager.DropRule rule = dropManager.getRule(blockType);
        if (rule == null) return;

        // Get the tool type the player is using
        ItemStack tool = player.getInventory().getItemInMainHand();
        String toolType = getToolType(tool);
        if (toolType == null) return;

        // Get tool-specific config
        DropManager.ToolConfig tc = rule.getToolConfig(toolType);
        if (tc == null) {
            // Try generic fallback
            tc = rule.getToolConfig("any");
            if (tc == null) return;
        }

        Location loc = block.getLocation();
        World world = block.getWorld();

        // 1. Handle block replace (drop different block)
        if (dropManager.isReplaceEnabled() && tc.isDropReplaceEnabled()) {
            double chance = tc.getDropReplaceChance();
            if (random.nextDouble() * 100 < chance) {
                event.setDropItems(false);
                Material replaceBlock = tc.getReplaceBlock();
                if (replaceBlock != null) {
                    world.dropItemNaturally(loc, new ItemStack(replaceBlock, 1));
                    player.sendMessage("§6[VNMine] §7Khối " + formatName(blockType.name()) + " rơi ra " + formatName(replaceBlock.name()) + "!");
                }
            }
        }

        // 2. Handle tool break
        if (dropManager.isBreakEnabled() && tc.isToolBreakEnabled()) {
            double chance = tc.getToolBreakChance();
            if (random.nextDouble() * 100 < chance) {
                // Break the tool
                player.getInventory().setItemInMainHand(null);
                String message = tc.getToolBreakMessage();
                if (message != null && !message.isEmpty()) {
                    player.sendMessage("§6[VNMine] " + message);
                }
            }
        }

        // 3. Handle explosion
        if (dropManager.isExplodeEnabled() && tc.isExplodeEnabled()) {
            double chance = tc.getExplodeChance();
            if (random.nextDouble() * 100 < chance) {
                float power = tc.getExplodePower();
                boolean breakBlocks = tc.isExplodeBreakBlocks();
                boolean setFire = tc.isExplodeSetFire();

                // Create explosion
                world.createExplosion(loc, power, setFire, breakBlocks);

                String message = tc.getExplodeMessage();
                if (message != null && !message.isEmpty()) {
                    player.sendMessage("§6[VNMine] " + message);
                }
            }
        }
    }

    private String getToolType(ItemStack tool) {
        if (tool == null || tool.getType() == Material.AIR) return "hand";
        Material type = tool.getType();

        // Determine tool type
        switch (type) {
            // Wooden tools
            case WOODEN_PICKAXE: return "wooden_pickaxe";
            case WOODEN_AXE: return "wooden_axe";
            case WOODEN_SHOVEL: return "wooden_shovel";
            case WOODEN_HOE: return "wooden_hoe";
            case WOODEN_SWORD: return "wooden_sword";

            // Stone tools
            case STONE_PICKAXE: return "stone_pickaxe";
            case STONE_AXE: return "stone_axe";
            case STONE_SHOVEL: return "stone_shovel";
            case STONE_HOE: return "stone_hoe";
            case STONE_SWORD: return "stone_sword";

            // Iron tools
            case IRON_PICKAXE: return "iron_pickaxe";
            case IRON_AXE: return "iron_axe";
            case IRON_SHOVEL: return "iron_shovel";
            case IRON_HOE: return "iron_hoe";
            case IRON_SWORD: return "iron_sword";

            // Diamond tools
            case DIAMOND_PICKAXE: return "diamond_pickaxe";
            case DIAMOND_AXE: return "diamond_axe";
            case DIAMOND_SHOVEL: return "diamond_shovel";
            case DIAMOND_HOE: return "diamond_hoe";
            case DIAMOND_SWORD: return "diamond_sword";

            // Netherite tools
            case NETHERITE_PICKAXE: return "netherite_pickaxe";
            case NETHERITE_AXE: return "netherite_axe";
            case NETHERITE_SHOVEL: return "netherite_shovel";
            case NETHERITE_HOE: return "netherite_hoe";
            case NETHERITE_SWORD: return "netherite_sword";

            default: return "any";
        }
    }

    private String formatName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}