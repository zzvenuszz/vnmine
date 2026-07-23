package com.vnmine.item;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemDefinition - Định nghĩa một loại item trong game
 * Được load từ file YML trong thư mục items/
 * 
 * Hỗ trợ mã màu & trong tất cả các field String
 */
public class ItemDefinition {

    private String id;
    private String name;
    private String type;           // Loại: Linh thảo, Đan dược, Pháp bảo, Công pháp...
    private String rank;           // Phẩm cấp: Hạ Phẩm, Hoàng cấp Hạ phẩm...
    private String element;        // Thuộc tính: Mộc, Hỏa, Thủy...
    private String age;            // Tuổi (cho linh thảo)
    private String rarity;         // Độ hiếm
    private String material;       // Material trong game
    private int customModelData;   // Custom model data (0 = không dùng)
    private String category;       // Danh mục: "pill", "herb", "artifact", "skill", "material", "mount", "currency"
    
    // Thông số đan dược
    private int baseRecover;
    private int baseHeal;
    private int baseRegen;
    private int baseDuration;
    private int baseDmg;
    private int baseExp;
    private boolean scaleWithGrade;
    private boolean scaleDuration;
    
    // Thông số chế tạo / luyện chế
    private int cookingTime;               // Thời gian nấu (giây)
    private double successChance;          // Tỷ lệ thành công cơ bản (%)
    private int requiredLevel;             // Cấp tu luyện yêu cầu
    private List<String> requiredSkills;   // ["SKILL_ID:level"]
    private List<String> ingredients;      // ["herb:ID:count", "mat:MATERIAL:count"]
    
    // Danh sách
    private List<String> effects = new ArrayList<>();
    private List<String> sideEffects = new ArrayList<>();
    private List<String> growEnvironment = new ArrayList<>();
    private List<String> compatibleAlchemy = new ArrayList<>();
    private List<String> lore = new ArrayList<>();           // Lore tuỳ chỉnh
    private List<String> skillIds = new ArrayList<>();       // Cho skill books
    private Map<String, String> additional = new HashMap<>(); // Thông tin bổ sung
    private List<String> artifactSkills = new ArrayList<>(); // Artifact skill IDs
    private Map<String, String> clickBehavior = new HashMap<>(); // Click behavior: right-click -> skill ID

    public ItemDefinition() {}

    // ==================== GETTERS & SETTERS ====================
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    
    public String getElement() { return element; }
    public void setElement(String element) { this.element = element; }
    
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    
    public Material getMaterial() {
        if (material == null) return Material.STONE;
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }
    public void setMaterial(String material) { this.material = material; }
    
    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getBaseRecover() { return baseRecover; }
    public void setBaseRecover(int baseRecover) { this.baseRecover = baseRecover; }
    
    public int getBaseHeal() { return baseHeal; }
    public void setBaseHeal(int baseHeal) { this.baseHeal = baseHeal; }
    
    public int getBaseRegen() { return baseRegen; }
    public void setBaseRegen(int baseRegen) { this.baseRegen = baseRegen; }
    
    public int getBaseDuration() { return baseDuration; }
    public void setBaseDuration(int baseDuration) { this.baseDuration = baseDuration; }
    
    public int getBaseDmg() { return baseDmg; }
    public void setBaseDmg(int baseDmg) { this.baseDmg = baseDmg; }
    
    public int getBaseExp() { return baseExp; }
    public void setBaseExp(int baseExp) { this.baseExp = baseExp; }
    
    public boolean isScaleWithGrade() { return scaleWithGrade; }
    public void setScaleWithGrade(boolean scaleWithGrade) { this.scaleWithGrade = scaleWithGrade; }
    
    public boolean isScaleDuration() { return scaleDuration; }
    public void setScaleDuration(boolean scaleDuration) { this.scaleDuration = scaleDuration; }
    
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    
    public double getSuccessChance() { return successChance; }
    public void setSuccessChance(double successChance) { this.successChance = successChance; }
    
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getEffects() { return effects; }
    public void setEffects(List<String> effects) { this.effects = effects; }
    
    public List<String> getSideEffects() { return sideEffects; }
    public void setSideEffects(List<String> sideEffects) { this.sideEffects = sideEffects; }
    
    public List<String> getGrowEnvironment() { return growEnvironment; }
    public void setGrowEnvironment(List<String> growEnvironment) { this.growEnvironment = growEnvironment; }
    
    public List<String> getCompatibleAlchemy() { return compatibleAlchemy; }
    public void setCompatibleAlchemy(List<String> compatibleAlchemy) { this.compatibleAlchemy = compatibleAlchemy; }
    
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    
    public List<String> getSkillIds() { return skillIds; }
    public void setSkillIds(List<String> skillIds) { this.skillIds = skillIds; }
    
    public Map<String, String> getAdditional() { return additional; }
    public void setAdditional(Map<String, String> additional) { this.additional = additional; }

    public List<String> getArtifactSkills() { return artifactSkills; }
    public void setArtifactSkills(List<String> artifactSkills) { this.artifactSkills = artifactSkills; }

    public Map<String, String> getClickBehavior() { return clickBehavior; }
    public void setClickBehavior(Map<String, String> clickBehavior) { this.clickBehavior = clickBehavior; }
    
    /**
     * Kiểm tra có phải đan dược không (có effect numbers)
     */
    public boolean isPill() {
        return "Đan dược".equalsIgnoreCase(type) || baseRecover > 0 || baseHeal > 0 || baseExp > 0;
    }
    
    /**
     * Kiểm tra có phải linh thảo không
     */
    public boolean isHerb() {
        return "Linh thảo".equalsIgnoreCase(type) || "Linh mộc".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra có phải pháp bảo không
     */
    public boolean isArtifact() {
        return "Pháp bảo".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra có phải công pháp không
     */
    public boolean isSkill() {
        return "Công pháp".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra có phải nguyên liệu không
     */
    public boolean isMaterial() {
        return "Nguyên liệu".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra có phải tọa kỵ không
     */
    public boolean isMount() {
        return "Tọa kỵ".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra có phải linh thạch không
     */
    public boolean isCurrency() {
        return "Linh thạch".equalsIgnoreCase(type);
    }
    
    /**
     * Kiểm tra item này có công thức chế tạo không
     */
    public boolean hasRecipe() {
        return ingredients != null && !ingredients.isEmpty();
    }
}