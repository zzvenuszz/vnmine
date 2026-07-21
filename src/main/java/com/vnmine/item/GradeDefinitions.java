package com.vnmine.item;

import com.vnmine.util.ColorUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GradeDefinitions - Định nghĩa phẩm cấp từ grades.yml
 * 
 * Hỗ trợ: skill grades (4×3), artifact tiers (21), pill multipliers
 * Tất cả đều configurable từ YML, không hardcode.
 */
public class GradeDefinitions {

    private final List<SkillGrade> skillGrades = new ArrayList<>();
    private final List<ArtifactGrade> artifactGrades = new ArrayList<>();
    private double[] pillMultipliers = new double[0];
    private String[] pillGradeDisplay = new String[0];
    private String[] pillGradeColors = new String[0];

    /**
     * Load grades từ file grades.yml
     */
    public void load(File gradesFile) {
        if (!gradesFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(gradesFile);
        
        // Load pill multipliers
        List<Integer> multInts = config.getIntegerList("pill-grade-multipliers");
        if (!multInts.isEmpty()) {
            pillMultipliers = multInts.stream().mapToDouble(i -> i).toArray();
        } else {
            List<Double> multDoubles = config.getDoubleList("pill-grade-multipliers");
            pillMultipliers = multDoubles.stream().mapToDouble(d -> d).toArray();
        }
        
        // Load pill grade display names
        List<String> display = config.getStringList("pill-grade-display");
        pillGradeDisplay = display.toArray(new String[0]);
        
        // Load pill grade colors
        List<String> colors = config.getStringList("pill-grade-colors");
        pillGradeColors = colors.toArray(new String[0]);
        
        // Load skill grades
        List<?> skillGradeList = config.getList("skill-grades");
        if (skillGradeList != null) {
            skillGrades.clear();
            for (Object obj : config.getList("skill-grades")) {
                if (obj instanceof ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection) obj;
                    SkillGrade sg = new SkillGrade();
                    sg.name = sec.getString("name", "Hoàng");
                    sg.color = sec.getString("color", "&7");
                    
                    List<?> subList = sec.getList("subs");
                    if (subList != null) {
                        sg.subs.clear();
                        for (Object subObj : subList) {
                            if (subObj instanceof ConfigurationSection) {
                                ConfigurationSection subSec = (ConfigurationSection) subObj;
                                SkillSubGrade sub = new SkillSubGrade();
                                sub.name = subSec.getString("name", "Hạ");
                                sub.color = subSec.getString("color", "&f");
                                sg.subs.add(sub);
                            }
                        }
                    }
                    skillGrades.add(sg);
                }
            }
        }
        
        // Load artifact grades
        List<?> artList = config.getList("artifact-grades");
        if (artList != null) {
            artifactGrades.clear();
            for (Object obj : artList) {
                if (obj instanceof ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection) obj;
                    ArtifactGrade ag = new ArtifactGrade();
                    ag.giai = sec.getString("giai", "Phàm Giai");
                    ag.giaiColor = ColorUtils.colorize(sec.getString("giai-color", "&7"));
                    ag.cap = sec.getString("cap", "Phàm Cấp");
                    ag.capColor = ColorUtils.colorize(sec.getString("cap-color", "&7"));
                    ag.multiplier = sec.getDouble("multiplier", 0.0);
                    artifactGrades.add(ag);
                }
            }
        }
    }

    // ==================== GETTERS ====================
    
    public double[] getPillMultipliers() { return pillMultipliers; }
    public double getPillMultiplier(int index) {
        if (index < 0 || index >= pillMultipliers.length) return 1.0;
        return pillMultipliers[index];
    }
    
    public String getPillGradeDisplay(int index) {
        if (index < 0 || index >= pillGradeDisplay.length) return "";
        return ColorUtils.colorize(pillGradeDisplay[index]);
    }
    
    public Color getPillGradeColor(int index) {
        if (index < 0 || index >= pillGradeColors.length) return Color.WHITE;
        try {
            String colorStr = pillGradeColors[index].toUpperCase();
            if (colorStr.startsWith("#")) {
                int rgb = Integer.parseInt(colorStr.substring(1), 16);
                return Color.fromRGB(rgb);
            }
            // Manual mapping instead of Color.valueOf() which doesn't exist in Paper API
            switch (colorStr) {
                case "WHITE": return Color.WHITE;
                case "SILVER": return Color.SILVER;
                case "GRAY": return Color.GRAY;
                case "BLACK": return Color.BLACK;
                case "RED": return Color.RED;
                case "MAROON": return Color.MAROON;
                case "YELLOW": return Color.YELLOW;
                case "OLIVE": return Color.OLIVE;
                case "LIME": return Color.LIME;
                case "GREEN": return Color.GREEN;
                case "AQUA": return Color.AQUA;
                case "TEAL": return Color.TEAL;
                case "BLUE": return Color.BLUE;
                case "NAVY": return Color.NAVY;
                case "FUCHSIA": return Color.FUCHSIA;
                case "PURPLE": return Color.PURPLE;
                case "ORANGE": return Color.ORANGE;
                default: return Color.WHITE;
            }
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
    
    public List<SkillGrade> getSkillGrades() { return skillGrades; }
    
    public int getSkillGradeCount() { return skillGrades.size(); }
    public int getSkillSubCount() { return skillGrades.isEmpty() ? 3 : skillGrades.get(0).subs.size(); }
    
    public List<ArtifactGrade> getArtifactGrades() { return artifactGrades; }
    public int getArtifactGradeCount() { return artifactGrades.size(); }
    
    /**
     * Format tên skill book: "&7[Hoàng Cấp]-&f[Hạ Phẩm] &f&lTên Skill"
     */
    public String formatSkillName(String baseName, int gradeIndex, int subIndex) {
        if (gradeIndex < 0 || gradeIndex >= skillGrades.size()) return baseName;
        SkillGrade sg = skillGrades.get(gradeIndex);
        String gc = sg.color;
        String gn = sg.name;
        String sc = sg.subs.isEmpty() ? "&f" : sg.subs.get(Math.min(subIndex, sg.subs.size() - 1)).color;
        String sn = sg.subs.isEmpty() ? "Hạ" : sg.subs.get(Math.min(subIndex, sg.subs.size() - 1)).name;
        return ColorUtils.colorize(gc + "[" + gn + " Cấp]-" + sc + "[" + sn + " Phẩm] &f&l" + baseName);
    }
    
    /**
     * Format tên pháp bảo: "&7[Phàm Giai]-&7[Phàm Cấp] &f&lTên"
     */
    public String formatArtifactName(String baseName, int tierIndex) {
        if (tierIndex < 0 || tierIndex >= artifactGrades.size()) return baseName;
        ArtifactGrade ag = artifactGrades.get(tierIndex);
        return ColorUtils.colorize(ag.giaiColor + "[" + ag.giai + "]-" + ag.capColor + "[" + ag.cap + "] &f&l" + baseName);
    }

    // ==================== INNER CLASSES ====================
    
    public static class SkillGrade {
        public String name = "Hoàng";
        public String color = "&7";
        public final List<SkillSubGrade> subs = new ArrayList<>();
    }
    
    public static class SkillSubGrade {
        public String name = "Hạ";
        public String color = "&f";
    }
    
    public static class ArtifactGrade {
        public String giai = "Phàm Giai";
        public String giaiColor = "&7";
        public String cap = "Phàm Cấp";
        public String capColor = "&7";
        public double multiplier = 0.0;
    }
}