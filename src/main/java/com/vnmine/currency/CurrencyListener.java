package com.vnmine.currency;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

/**
 * CurrencyListener - Drop linh thạch từ quái và quặng
 */
public class CurrencyListener implements Listener {

    private final VNMinePlugin plugin;
    private final CurrencyManager currencyManager;
    private final Random random;

    public CurrencyListener(VNMinePlugin plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!currencyManager.isEnabled()) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        boolean isElite = event.getEntity().hasMetadata("vnmine_elite");
        boolean isBoss = event.getEntity().hasMetadata("vnmine_boss");

        double chance;
        String amountStr;

        if (isBoss) {
            chance = currencyManager.getBossDropChance();
            amountStr = currencyManager.getBossDropAmount();
        } else if (isElite) {
            chance = currencyManager.getEliteDropChance();
            amountStr = currencyManager.getEliteDropAmount();
        } else if (event.getEntity() instanceof Monster) {
            chance = currencyManager.getMobDropChance();
            amountStr = currencyManager.getMobDropAmount();
        } else {
            return;
        }

        if (random.nextDouble() * 100 < chance) {
            int amount = currencyManager.parseAmount(amountStr);
            if (amount > 0) {
                event.getDrops().add(currencyManager.createCurrency(amount));
                MessageUtils.sendActionBar(killer, "&b✦ +" + amount + " Linh Thạch");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!currencyManager.isEnabled() || event.isCancelled()) return;

        Material blockType = event.getBlock().getType();
        if (blockType.name().endsWith("_ORE")) {
            if (random.nextDouble() * 100 < currencyManager.getOreDropChance()) {
                int amount = currencyManager.getOreDropAmount();
                if (amount > 0) {
                    event.getBlock().getWorld().dropItemNaturally(
                            event.getBlock().getLocation(),
                            currencyManager.createCurrency(amount)
                    );
                }
            }
        }
    }
}