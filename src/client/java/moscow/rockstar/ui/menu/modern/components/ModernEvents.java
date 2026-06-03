package moscow.rockstar.ui.menu.modern.components;

import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModernEvents extends CustomComponent implements IMinecraft {
    private final List<EventData> events = new ArrayList<>();
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private final File eventsFile;
    private long lastRefresh;

    public ModernEvents() {
        this.eventsFile = new File(mc.gameDirectory, "rockstar/events.json");
    }

    public void refreshEvents() {
        this.lastRefresh = System.currentTimeMillis();
    }

    @Override
    protected void renderComponent(UIContext context) {
        // TODO: 26.1 - restore events panel rendering
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
    }

    public boolean onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollHandler.scroll(verticalAmount);
        return true;
    }

    public static class EventData {
        public String anarchy;
        public String name;
        public String status;
        public String loot;
        public String coords;
        public String warp;
        public boolean is_legendary;
        public double end_time;
    }
}
