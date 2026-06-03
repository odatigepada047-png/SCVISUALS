/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.client.session.User
 *  net.minecraft.client.session.User$AccountType
 */
package moscow.rockstar.systems.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import moscow.rockstar.ui.menu.modern.components.ModernModels;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.file.ClientFile;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.file.api.FileInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.modules.modules.other.AutoAuth;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.User;

@FileInfo(name="client")
public class ClientDataFile
extends ClientFile
implements IMinecraft {
    @Override
    public void write() {
        JsonObject json = new JsonObject();
        json.addProperty("username", mc.getUser().getName());
        json.addProperty("theme", Rockstar.getInstance().getThemeManager().getCurrentTheme().name());
        json.add("accentColor", (JsonElement)this.getColorJsonObject(Rockstar.getInstance().getThemeManager().getAccentColor()));
        json.addProperty("swing", Rockstar.getInstance().getSwingManager().getCurrent());
        json.add("hudElements", (JsonElement)this.getHudElementsJsonArray());
        json.add("friends", (JsonElement)this.getFriendsJsonArray());
        json.add("colorPickerPresets", (JsonElement)this.getColorPickerPresetsJsonArray());
        
        JsonArray favoritesJsonArray = new JsonArray();
        for (moscow.rockstar.systems.modules.Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            if (module.isFavorite()) {
                favoritesJsonArray.add(module.getName());
            }
        }
        json.add("favorites", favoritesJsonArray);
        ConfigFile currentConfig = Rockstar.getInstance().getConfigManager().getCurrent();
        if (currentConfig != null) {
            json.addProperty("lastConfig", currentConfig.getFileName());
        }
        json.addProperty("figuraModel", ModernModels.getSelectedModelPath());
        try (FileWriter writer = new FileWriter(this.file);){
            writer.write(FileManager.GSON.toJson((JsonElement)json));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read() {
        try (FileReader reader = new FileReader(this.getFile());){
            JsonObject object = (JsonObject)FileManager.GSON.fromJson((Reader)reader, JsonObject.class);
            if (object.has("username")) {
                String username = object.get("username").getAsString();
                // Username from saved data (used for display only — session creation removed for 1.21.11 compat)
            }
            if (object.has("password")) {
                this.loadPass(object.getAsJsonArray("password"));
            }
            if (object.has("swing")) {
                String swing = object.get("swing").getAsString();
                SwingManager swingManager = Rockstar.getInstance().getSwingManager();
                SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
                for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
                    if (!value.getName().equals(swing)) continue;
                    swingManager.getBezier().start(value.getBezierStart()).end(value.getBezierEnd());
                    swingManager.getBack().enabled(value.isSwingBack());
                    swingManager.getSpeed().setCurrentValue(value.getSpeed());
                    SwingPhase start = swingManager.getStartPhase();
                    start.getAnchorX().setCurrentValue(value.getFrom().anchorX());
                    start.getAnchorY().setCurrentValue(value.getFrom().anchorY());
                    start.getAnchorZ().setCurrentValue(value.getFrom().anchorZ());
                    start.getMoveX().setCurrentValue(value.getFrom().moveX());
                    start.getMoveY().setCurrentValue(value.getFrom().moveY());
                    start.getMoveZ().setCurrentValue(value.getFrom().moveZ());
                    start.getRotateX().setCurrentValue(value.getFrom().rotateX());
                    start.getRotateY().setCurrentValue(value.getFrom().rotateY());
                    start.getRotateZ().setCurrentValue(value.getFrom().rotateZ());
                    SwingPhase end = swingManager.getEndPhase();
                    end.getAnchorX().setCurrentValue(value.getTo().anchorX());
                    end.getAnchorY().setCurrentValue(value.getTo().anchorY());
                    end.getAnchorZ().setCurrentValue(value.getTo().anchorZ());
                    end.getMoveX().setCurrentValue(value.getTo().moveX());
                    end.getMoveY().setCurrentValue(value.getTo().moveY());
                    end.getMoveZ().setCurrentValue(value.getTo().moveZ());
                    end.getRotateX().setCurrentValue(value.getTo().rotateX());
                    end.getRotateY().setCurrentValue(value.getTo().rotateY());
                    end.getRotateZ().setCurrentValue(value.getTo().rotateZ());
                    swingManager.setCurrent(swing);
                }
            }
            if (object.has("theme")) {
                String themeName = object.get("theme").getAsString();
                try {
                    Theme theme = Theme.valueOf(themeName);
                    Rockstar.getInstance().getThemeManager().setCurrentTheme(theme);
                }
                catch (IllegalArgumentException e) {
                    Rockstar.getInstance().getThemeManager().setCurrentTheme(Theme.DARK);
                }
            }
            if (object.has("accentColor")) {
                JsonObject accentObject = object.getAsJsonObject("accentColor");
                Rockstar.getInstance().getThemeManager().setAccentColor(this.parseColor(accentObject));
            }
            if (object.has("friends")) {
                JsonArray friendsArray = object.getAsJsonArray("friends");
                Rockstar.getInstance().getFriendManager().clear();
                for (JsonElement friendElement : friendsArray) {
                    Rockstar.getInstance().getFriendManager().add(friendElement.getAsString());
                }
            }
            if (object.has("favorites")) {
                JsonArray favoritesArray = object.getAsJsonArray("favorites");
                for (moscow.rockstar.systems.modules.Module module : Rockstar.getInstance().getModuleManager().getModules()) {
                    module.setFavorite(false);
                }
                for (JsonElement favElement : favoritesArray) {
                    try {
                        moscow.rockstar.systems.modules.Module module = Rockstar.getInstance().getModuleManager().getModule(favElement.getAsString());
                        if (module != null) {
                            module.setFavorite(true);
                        }
                    } catch (Exception e) {}
                }
            }
            if (object.has("colorPickerPresets")) {
                this.loadColorPickerPresets(object.getAsJsonArray("colorPickerPresets"));
            }
            if (object.has("hudElements") && Rockstar.getInstance().getHud() != null) {
                JsonArray hudElementsArray = object.getAsJsonArray("hudElements");
                for (JsonElement elemObj : hudElementsArray) {
                    JsonObject elementObject = elemObj.getAsJsonObject();
                    String name = elementObject.get("name").getAsString();
                    float x = elementObject.get("x").getAsFloat();
                    float y = elementObject.get("y").getAsFloat();
                    boolean showing = elementObject.get("showing").getAsBoolean();
                    Object element = Rockstar.getInstance().getHud().getElementByName(name);
                    if (element == null) continue;
                    ((HudElement)element).setX(x);
                    ((HudElement)element).setY(y);
                    ((HudElement)element).setShowing(showing);
                    if (!elementObject.has("settings")) continue;
                    JsonObject settingsObject = elementObject.getAsJsonObject("settings");
                    for (Setting setting : ((HudElement)element).getSettings()) {
                        if (!settingsObject.has(setting.getName())) continue;
                        setting.load(settingsObject.get(setting.getName()));
                    }
                }
            }
            if (object.has("lastConfig")) {
                String configName = object.get("lastConfig").getAsString();
                ConfigFile config = Rockstar.getInstance().getConfigManager().getConfig(configName);
                if (config != null) {
                    config.load();
                }
            }
            if (object.has("figuraModel")) {
                ModernModels.setSelectedModelPath(object.get("figuraModel").getAsString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonArray getHudElementsJsonArray() {
        JsonArray hudElementsArray = new JsonArray();
        for (HudElement element : Rockstar.getInstance().getHud().getElements()) {
            JsonObject elementObject = new JsonObject();
            elementObject.addProperty("name", element.getName());
            elementObject.addProperty("x", (Number)Float.valueOf(element.getX()));
            elementObject.addProperty("y", (Number)Float.valueOf(element.getY()));
            elementObject.addProperty("showing", Boolean.valueOf(element.isShowing()));
            elementObject.add("settings", (JsonElement)this.getSettingsJsonObject(element));
            hudElementsArray.add((JsonElement)elementObject);
        }
        return hudElementsArray;
    }

    private JsonObject getSettingsJsonObject(HudElement element) {
        JsonObject settingsObject = new JsonObject();
        for (Setting setting : element.getSettings()) {
            settingsObject.add(setting.getName(), setting.save());
        }
        return settingsObject;
    }

    private JsonArray getFriendsJsonArray() {
        JsonArray friendsJsonArray = new JsonArray();
        for (String friendsName : Rockstar.getInstance().getFriendManager().listFriends()) {
            friendsJsonArray.add(friendsName);
        }
        return friendsJsonArray;
    }

    private JsonArray getPassword() {
        JsonArray passwordJsonArray = new JsonArray();
        for (Map.Entry<String, String> pass : Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).listPassword().entrySet()) {
            JsonObject passObject = new JsonObject();
            passObject.addProperty("nick", pass.getValue());
            passObject.addProperty("pass", pass.getKey());
            passwordJsonArray.add((JsonElement)passObject);
        }
        return passwordJsonArray;
    }

    private JsonArray getColorPickerPresetsJsonArray() {
        JsonArray presetsArray = new JsonArray();
        List<ColorPicker.Preset> presets = ColorPicker.COLOR_PRESETS;
        for (ColorPicker.Preset preset : presets) {
            if (!preset.isShowing()) continue;
            JsonObject presetObject = new JsonObject();
            ColorRGBA color = preset.getColor();
            presetObject.addProperty("red", (Number)Float.valueOf(color.getRed()));
            presetObject.addProperty("green", (Number)Float.valueOf(color.getGreen()));
            presetObject.addProperty("blue", (Number)Float.valueOf(color.getBlue()));
            presetObject.addProperty("alpha", (Number)Float.valueOf(color.getAlpha()));
            presetsArray.add((JsonElement)presetObject);
        }
        return presetsArray;
    }

    private JsonObject getColorJsonObject(ColorRGBA color) {
        JsonObject colorObject = new JsonObject();
        colorObject.addProperty("red", (Number)Float.valueOf(color.getRed()));
        colorObject.addProperty("green", (Number)Float.valueOf(color.getGreen()));
        colorObject.addProperty("blue", (Number)Float.valueOf(color.getBlue()));
        colorObject.addProperty("alpha", (Number)Float.valueOf(color.getAlpha()));
        return colorObject;
    }

    private ColorRGBA parseColor(JsonObject colorObject) {
        float red = colorObject.get("red").getAsFloat();
        float green = colorObject.get("green").getAsFloat();
        float blue = colorObject.get("blue").getAsFloat();
        float alpha = colorObject.get("alpha").getAsFloat();
        return new ColorRGBA(red, green, blue, alpha);
    }

    private void loadColorPickerPresets(JsonArray presetsArray) {
        ArrayList<ColorPicker.Preset> loadedPresets = new ArrayList<ColorPicker.Preset>();
        for (JsonElement presetElement : presetsArray) {
            JsonObject presetObject = presetElement.getAsJsonObject();
            float red = presetObject.get("red").getAsFloat();
            float green = presetObject.get("green").getAsFloat();
            float blue = presetObject.get("blue").getAsFloat();
            float alpha = presetObject.get("alpha").getAsFloat();
            ColorRGBA color = new ColorRGBA(red, green, blue, alpha);
            loadedPresets.add(new ColorPicker.Preset(color));
        }
        ColorPicker.setColorPresets(loadedPresets);
    }

    private void loadPass(JsonArray password) {
        for (JsonElement passElement : password) {
            JsonObject passObject = passElement.getAsJsonObject();
            String nick = passObject.get("nick").getAsString();
            String pass = passObject.get("pass").getAsString();
            Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).put(nick, pass);
        }
    }
}
