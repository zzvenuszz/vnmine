package com.vnmine.item;

import org.bukkit.Material;

import java.util.*;

/**
 * ArtifactRecipe - Công thức luyện chế pháp bảo, được parse từ ItemDefinition
 */
public class ArtifactRecipe {

    public static class IngredientDef {
        final String herbId;      // null nếu không phải linh thảo
        final Material material;  // Material
        final int count;
        final boolean isHerb;

        public IngredientDef(String herbId, Material material, int count, boolean isHerb) {
            this.herbId = herbId;
            this.material = material;
            this.count = count;
            this.isHerb = isHerb;
        }

        public String getHerbId() { return herbId; }
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
    private final String requiredSkill;
    private final int skillLevel;
    private final List<String> effects;

    public ArtifactRecipe(String id, String displayName, Material resultMaterial, String lore,
                          List<IngredientDef> ingredients, int requiredLevel,
                          int cookingTime, double successChance,
                          String requiredSkill, int skillLevel,
                          List<String> effects) {
        this.id = id;
        this.displayName = displayName;
        this.resultMaterial = resultMaterial;
        this.lore = lore;
        this.ingredients = ingredients;
        this.requiredLevel = requiredLevel;
        this.cookingTime = cookingTime;
        this.successChance = successChance;
        this.requiredSkill = requiredSkill;
        this.skillLevel = skillLevel;
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getResultMaterial() { return resultMaterial; }
    public String getLore() { return lore; }
    public List<IngredientDef> getIngredients() { return ingredients; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getCookingTime() { return cookingTime; }
    public double getSuccessChance() { return successChance; }
    public String getRequiredSkill() { return requiredSkill; }
    public int getSkillLevel() { return skillLevel; }
    public List<String> getEffects() { return effects; }

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
                result.add(new IngredientDef(value, Material.SHORT_GRASS, count, true));
            } else if ("MAT".equals(type)) {
                Material mat;
                try {
                    mat = Material.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    mat = Material.STONE;
                }
                result.add(new IngredientDef(null, mat, count, false));
            }
        }
        return result;
    }
}