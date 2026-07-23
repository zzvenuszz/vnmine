package com.vnmine.item;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * PillRecipe - Công thức luyện đan, được parse từ ItemDefinition
 */
public class PillRecipe {

    public static class IngredientDef {
        final String herbId;      // null nếu không phải linh thảo
        final String itemId;      // item ID từ YML (cho material items, vd: "NUOC_TINH_KHIET")
        final Material material;  // Material để kiểm tra fallback
        final int count;
        final boolean isHerb;

        public IngredientDef(String herbId, String itemId, Material material, int count, boolean isHerb) {
            this.herbId = herbId;
            this.itemId = itemId;
            this.material = material;
            this.count = count;
            this.isHerb = isHerb;
        }

        public String getHerbId() { return herbId; }
        public String getItemId() { return itemId; }
        public Material getMaterial() { return material; }
        public int getCount() { return count; }
        public boolean isHerb() { return isHerb; }
    }

    private final String id;
    private final String displayName;
    private final Material resultMaterial;
    private final String lore;
    private final List<IngredientDef> ingredients;
    private final int requiredLevel;
    private final int cookingTime;
    private final double successChance;
    private final List<String> effects;
    private final List<String> sideEffects;

    public PillRecipe(String id, String displayName, Material resultMaterial, String lore,
                      List<IngredientDef> ingredients, int requiredLevel,
                      int cookingTime, double successChance,
                      List<String> effects, List<String> sideEffects) {
        this.id = id;
        this.displayName = displayName;
        this.resultMaterial = resultMaterial;
        this.lore = lore;
        this.ingredients = ingredients;
        this.requiredLevel = requiredLevel;
        this.cookingTime = cookingTime;
        this.successChance = successChance;
        this.effects = effects;
        this.sideEffects = sideEffects;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getResultMaterial() { return resultMaterial; }
    public String getLore() { return lore; }
    public List<IngredientDef> getIngredients() { return ingredients; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getCookingTime() { return cookingTime; }
    public double getSuccessChance() { return successChance; }
    public List<String> getEffects() { return effects; }
    public List<String> getSideEffects() { return sideEffects; }

    /**
     * Parse Ingredient từ ItemDefinition ingredients list
     * Format: "herb:ID:count" hoặc "mat:MATERIAL:count"
     */
    public static List<IngredientDef> parseIngredients(List<String> ingredientStrings) {
        List<IngredientDef> result = new ArrayList<>();
        if (ingredientStrings == null) return result;

        for (String ing : ingredientStrings) {
            if (ing == null || ing.isEmpty()) continue;
            String[] parts = ing.split(":");
            if (parts.length < 3) continue;

            String type = parts[0].toUpperCase();
            String value = parts[1];
            int count;
            try {
                count = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                continue;
            }

            if ("HERB".equals(type)) {
                // herbId, material sẽ được resolve sau
                result.add(new IngredientDef(value, null, Material.SHORT_GRASS, count, true));
            } else if ("MAT".equals(type)) {
                Material mat;
                try {
                    mat = Material.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    mat = Material.STONE;
                }
                // Lưu cả itemId gốc (value) để so sánh với vnmine_item_id
                result.add(new IngredientDef(null, value.toUpperCase(), mat, count, false));
            }
        }
        return result;
    }
}