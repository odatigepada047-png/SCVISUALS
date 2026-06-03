/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.systems.theme;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.colors.ColorRGBA;

public class ThemeManager {
    private Theme currentTheme = Theme.DARK;
    private ColorRGBA accentColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private final List<CustomTheme> customThemes = new ArrayList<>();
    private final File themesFolder;

    public ThemeManager() {
        File configFolder = new File(FileManager.DIRECTORY, "configs");
        this.themesFolder = new File(configFolder, "themes");
        if (!this.themesFolder.exists()) {
            this.themesFolder.mkdirs();
        }
        
        this.loadThemes();
    }

    public void loadThemes() {
        this.customThemes.clear();
        this.customThemes.add(new CustomTheme("Purple", new ColorRGBA(151.0f, 71.0f, 255.0f)));
        
        if (this.themesFolder.exists() && this.themesFolder.isDirectory()) {
            File[] files = this.themesFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".theme")) continue;
                    try (FileReader reader = new FileReader(file)) {
                        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                        String name = object.get("name").getAsString();
                        
                        // Избегаем дублирования дефолтной темы
                        if (name.equalsIgnoreCase("Purple")) continue;
                        
                        JsonObject colorObject = object.getAsJsonObject("color");
                        float red = colorObject.get("red").getAsFloat();
                        float green = colorObject.get("green").getAsFloat();
                        float blue = colorObject.get("blue").getAsFloat();
                        float alpha = colorObject.get("alpha").getAsFloat();
                        ColorRGBA color = new ColorRGBA(red, green, blue, alpha);
                        
                        this.customThemes.add(new CustomTheme(name, color));
                    } catch (Exception e) {
                        System.err.println("Failed to load theme: " + file.getName());
                    }
                }
            }
        }
    }

    public void saveTheme(CustomTheme theme) {
        if (theme.getName().equalsIgnoreCase("Purple")) return;
        
        if (!this.themesFolder.exists()) {
            this.themesFolder.mkdirs();
        }
        
        File file = new File(this.themesFolder, theme.getName() + ".theme");
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject object = new JsonObject();
            object.addProperty("name", theme.getName());
            
            JsonObject colorObject = new JsonObject();
            colorObject.addProperty("red", theme.getAccentColor().getRed());
            colorObject.addProperty("green", theme.getAccentColor().getGreen());
            colorObject.addProperty("blue", theme.getAccentColor().getBlue());
            colorObject.addProperty("alpha", theme.getAccentColor().getAlpha());
            
            object.add("color", colorObject);
            
            writer.write(FileManager.GSON.toJson(object));
        } catch (Exception e) {
            System.err.println("Failed to save theme: " + theme.getName());
        }
    }

    public void deleteTheme(CustomTheme theme) {
        if (theme.getName().equalsIgnoreCase("Purple")) return;
        
        File file = new File(this.themesFolder, theme.getName() + ".theme");
        if (file.exists()) {
            file.delete();
        }
    }

    public void switchTheme() {
        this.currentTheme = this.currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
    }

    public List<CustomTheme> getCustomThemes() {
        return this.customThemes;
    }

    public Theme getCurrentTheme() {
        if (Interface.glassSelected()) {
            return Theme.DARK;
        }
        return this.currentTheme;
    }

    public ColorRGBA getAccentColor() {
        return this.accentColor;
    }

    @Generated
    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    public void setAccentColor(ColorRGBA accentColor) {
        this.accentColor = accentColor != null ? accentColor : new ColorRGBA(151.0f, 71.0f, 255.0f);
    }
}
