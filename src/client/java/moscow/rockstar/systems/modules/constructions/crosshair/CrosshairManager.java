package moscow.rockstar.systems.modules.constructions.crosshair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moscow.rockstar.utility.colors.ColorRGBA;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CrosshairManager {
    private static final int GRID_SIZE = 19;
    private final boolean[][] grid = new boolean[GRID_SIZE][GRID_SIZE];
    private ColorRGBA color = new ColorRGBA(255, 255, 255, 255);
    private CrosshairType type = CrosshairType.STATIC;
    private float animationProgress = 0.0f;
    private final File saveFile;
    
    public CrosshairManager() {
        File rockstarDir = new File("Rockstar");
        if (!rockstarDir.exists()) {
            rockstarDir.mkdirs();
        }
        this.saveFile = new File(rockstarDir, "crosshair.json");
        load();
        // Without a saved grid every cell stays false — mixin hides vanilla crosshair → invisible aim.
        if (!hasAnyPixels()) {
            applyDefaultCrosshairPattern();
        }
    }

    /**
     * @return whether any cell in the editor grid is enabled
     */
    public boolean hasAnyPixels() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (grid[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Classic "+" crosshair centered on the grid (used when {@link #hasAnyPixels()} is false).
     */
    public void applyDefaultCrosshairPattern() {
        int c = GRID_SIZE / 2;
        int arm = 4;
        for (int d = -arm; d <= arm; d++) {
            setPixel(c + d, c, true);
            setPixel(c, c + d, true);
        }
    }
    
    public boolean[][] getGrid() {
        return grid;
    }
    
    public ColorRGBA getColor() {
        return color;
    }
    
    public CrosshairType getType() {
        return type;
    }
    
    public int getGridSize() {
        return GRID_SIZE;
    }
    
    public void setPixel(int x, int y, boolean enabled) {
        if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
            grid[x][y] = enabled;
        }
    }
    
    public boolean getPixel(int x, int y) {
        if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
            return grid[x][y];
        }
        return false;
    }
    
    public void clear() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                grid[x][y] = false;
            }
        }
    }
    
    public void setColor(ColorRGBA color) {
        this.color = color;
    }
    
    public void setType(CrosshairType type) {
        this.type = type;
    }
    
    public float getAnimationProgress() {
        return animationProgress;
    }
    
    public void updateAnimation(float delta) {
        float target = type == CrosshairType.DYNAMIC ? 1.0f : 0.0f;
        float speed = 10.0f * delta;
        
        if (Math.abs(target - animationProgress) < 0.001f) {
            animationProgress = target;
        } else {
            animationProgress += (target - animationProgress) * speed;
        }
    }
    
    public void save() {
        try {
            JsonObject root = new JsonObject();
            
            // Сохраняем сетку
            JsonArray gridArray = new JsonArray();
            for (int x = 0; x < GRID_SIZE; x++) {
                JsonArray row = new JsonArray();
                for (int y = 0; y < GRID_SIZE; y++) {
                    row.add(grid[x][y]);
                }
                gridArray.add(row);
            }
            root.add("grid", gridArray);
            
            // Сохраняем цвет
            JsonObject colorObj = new JsonObject();
            colorObj.addProperty("r", color.getRed());
            colorObj.addProperty("g", color.getGreen());
            colorObj.addProperty("b", color.getBlue());
            colorObj.addProperty("a", color.getAlpha());
            root.add("color", colorObj);
            
            // Сохраняем тип
            root.addProperty("type", type.name());
            
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(root.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void load() {
        if (!saveFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(saveFile)) {
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonObject root = parser.parse(reader).getAsJsonObject();
            
            // Загружаем сетку
            if (root.has("grid")) {
                JsonArray gridArray = root.getAsJsonArray("grid");
                for (int x = 0; x < Math.min(GRID_SIZE, gridArray.size()); x++) {
                    JsonArray row = gridArray.get(x).getAsJsonArray();
                    for (int y = 0; y < Math.min(GRID_SIZE, row.size()); y++) {
                        grid[x][y] = row.get(y).getAsBoolean();
                    }
                }
            }
            
            // Загружаем цвет (в JSON могут быть и целые, и дробные числа после Gson save)
            if (root.has("color")) {
                JsonObject colorObj = root.getAsJsonObject("color");
                float r = jsonColorChannel(colorObj.get("r"));
                float g = jsonColorChannel(colorObj.get("g"));
                float b = jsonColorChannel(colorObj.get("b"));
                float a = jsonColorChannel(colorObj.get("a"));
                color = new ColorRGBA(r, g, b, a);
            }
            
            // Загружаем тип
            if (root.has("type")) {
                String typeName = root.get("type").getAsString();
                type = CrosshairType.valueOf(typeName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static float jsonColorChannel(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return 255.0f;
        }
        try {
            return element.getAsFloat();
        } catch (Exception e) {
            return 255.0f;
        }
    }

    public enum CrosshairType {
        STATIC("Статический"),
        DYNAMIC("Динамический");
        
        private final String displayName;
        
        CrosshairType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
