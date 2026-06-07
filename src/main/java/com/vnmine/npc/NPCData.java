package com.vnmine.npc;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import java.util.*;

/**
 * NPCData - Dữ liệu cấu hình cho một NPC
 */
public class NPCData {
    private final String id;
    private String name;
    private String entityType; // VILLAGER
    private String profession;
    private boolean enabled;
    private List<NPCTrade> trades;

    public NPCData(String id) {
        this.id = id;
        this.trades = new ArrayList<>();
        this.enabled = true;
        this.profession = "NITWIT";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<NPCTrade> getTrades() { return trades; }
    public void setTrades(List<NPCTrade> trades) { this.trades = trades; }

    public static class NPCTrade {
        private final String id;
        private final String type; // SKILL, ARTIFACT, PILL, CURRENCY_BUY, CURRENCY_SELL
        private final String displayName;
        private final String material;
        private final int amount;
        private final List<String> lore;

        // Giá
        private final String priceMaterial;
        private final int priceAmount;

        // Thông tin thêm
        private final String skillId;
        private final String itemId;
        private final int stock;
        private final int cooldownSeconds;

        public NPCTrade(String id, String type, String displayName, String material, int amount,
                       List<String> lore, String priceMaterial, int priceAmount,
                       String skillId, String itemId, int stock, int cooldownSeconds) {
            this.id = id;
            this.type = type;
            this.displayName = displayName;
            this.material = material;
            this.amount = amount;
            this.lore = lore != null ? lore : new ArrayList<>();
            this.priceMaterial = priceMaterial;
            this.priceAmount = priceAmount;
            this.skillId = skillId;
            this.itemId = itemId;
            this.stock = stock;
            this.cooldownSeconds = cooldownSeconds;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getMaterial() { return material; }
        public int getAmount() { return amount; }
        public List<String> getLore() { return lore; }
        public String getPriceMaterial() { return priceMaterial; }
        public int getPriceAmount() { return priceAmount; }
        public String getSkillId() { return skillId; }
        public String getItemId() { return itemId; }
        public int getStock() { return stock; }
        public int getCooldownSeconds() { return cooldownSeconds; }

        public static NPCTrade fromConfig(String id, ConfigurationSection section) {
            return new NPCTrade(
                    id,
                    section.getString("type", "SELL"),
                    section.getString("display-name", ""),
                    section.getString("material", "STONE"),
                    section.getInt("amount", 1),
                    section.getStringList("lore"),
                    section.getString("price.material", "EMERALD"),
                    section.getInt("price.amount", 1),
                    section.getString("skill-id", ""),
                    section.getString("item-id", ""),
                    section.getInt("stock", -1),
                    section.getInt("cooldown-seconds", 0)
            );
        }
    }
}