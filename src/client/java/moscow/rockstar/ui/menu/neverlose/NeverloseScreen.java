package moscow.rockstar.ui.menu.neverlose;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NeverloseScreen extends MenuScreen implements IMinecraft, IScaledResolution {

    private static final ColorRGBA BG          = new ColorRGBA(15.0f,  17.0f,  23.0f);
    private static final ColorRGBA SIDEBAR     = new ColorRGBA(10.0f,  12.0f,  17.0f);
    private static final ColorRGBA CARD        = new ColorRGBA(22.0f,  25.0f,  33.0f);
    private static final ColorRGBA CARD_HEAD   = new ColorRGBA(17.0f,  20.0f,  27.0f);
    private static final ColorRGBA SUNKEN      = new ColorRGBA(11.0f,  13.0f,  18.0f);
    private static final ColorRGBA BORDER      = new ColorRGBA(30.0f,  35.0f,  46.0f);
    private static final ColorRGBA TEXT        = new ColorRGBA(225.0f, 230.0f, 240.0f);
    private static final ColorRGBA TEXT_DIM    = new ColorRGBA(120.0f, 128.0f, 145.0f);
    private static final ColorRGBA TEXT_FAINT  = new ColorRGBA(80.0f,  86.0f,  100.0f);
    private static final ColorRGBA ACCENT      = new ColorRGBA(212.0f, 232.0f, 95.0f);
    private static final ColorRGBA ACCENT_SOFT = new ColorRGBA(212.0f, 232.0f, 95.0f, 30.0f);
    private static final ColorRGBA DANGER      = new ColorRGBA(235.0f, 92.0f,  92.0f);

    private static final float WIDTH       = 620.0f;
    private static final float HEIGHT      = 400.0f;
    private static final float SIDEBAR_W   = 145.0f;
    private static final float HEADER_H    = 36.0f;
    private static final float CAT_ITEM_H  = 26.0f;

    private float x, y;
    private boolean drag;
    private float dragX, dragY;

    private MenuCategory current = MenuCategory.COMBAT;
    private final Animation catHighlight = new Animation(280L, Easing.QUAD_OUT);
    private final ScrollHandler scroll = new ScrollHandler();

    private final Map<Module, Boolean> expanded = new LinkedHashMap<>();
    private final Map<ModeSetting, Boolean> modeOpen = new LinkedHashMap<>();
    private BindSetting bindingNow;
    private SliderSetting draggingSlider;
    private RangeRef draggingRange;

    private record RangeRef(SliderSetting setting, float trackX, float trackW) {}

    public NeverloseScreen() {
        this.x = sr.getGuiScaledWidth()  / 2.0f - WIDTH  / 2.0f;
        this.y = sr.getGuiScaledHeight() / 2.0f - HEIGHT / 2.0f;
    }

    @Override
    protected void init() {
        this.closing = false;
        this.x = sr.getGuiScaledWidth()  / 2.0f - WIDTH  / 2.0f;
        this.y = sr.getGuiScaledHeight() / 2.0f - HEIGHT / 2.0f;
        super.init();
    }

    @Override
    public void render(UIContext context) {
        this.menuAnimation.setEasing(this.closing ? Easing.QUAD_OUT : Easing.BAKEK);
        this.menuAnimation.setDuration(350L);
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        this.scroll.update();

        if (this.drag) {
            this.x = context.getMouseX() - this.dragX;
            this.y = context.getMouseY() - this.dragY;
        }

        float anim  = Math.min(1.0f, this.menuAnimation.getRGB());
        float alpha = anim * 255.0f;
        float scale = 0.92f + 0.08f * anim;

        RenderUtility.scale(context.pose(), this.x + WIDTH / 2.0f, this.y + HEIGHT / 2.0f, scale);

        context.drawShadow(this.x + 6.0f, this.y + 8.0f, WIDTH, HEIGHT, 28.0f, BorderRadius.all(10.0f),
            new ColorRGBA(0.0f, 0.0f, 0.0f, 140.0f).mulAlpha(anim));

        context.drawRoundedRect(this.x, this.y, WIDTH, HEIGHT, BorderRadius.all(8.0f), BG.withAlpha(alpha));
        context.drawRoundedRect(this.x, this.y, SIDEBAR_W, HEIGHT, BorderRadius.left(8.0f, 8.0f), SIDEBAR.withAlpha(alpha));
        context.drawRoundedRect(this.x + SIDEBAR_W, this.y, 1.0f, HEIGHT, BorderRadius.ZERO, BORDER.withAlpha(alpha));
        context.drawRoundedRect(this.x, this.y + HEADER_H, WIDTH, 1.0f, BorderRadius.ZERO, BORDER.withAlpha(alpha * 0.7f));

        renderSidebar(context, alpha);
        renderHeader(context, alpha);
        renderContent(context, alpha);

        RenderUtility.end(context.pose());
    }

    private void renderHeader(UIContext context, float alpha) {
        FontBatching semi = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(11.0f), "rockstar",
            this.x + 16.0f, this.y + 12.0f, TEXT.withAlpha(alpha));
        context.drawText(Fonts.SEMIBOLD.getFont(11.0f), ".cc",
            this.x + 16.0f + Fonts.SEMIBOLD.getFont(11.0f).width("rockstar"),
            this.y + 12.0f, ACCENT.withAlpha(alpha));
        semi.draw();

        FontBatching reg = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        String version = "v1.0.0";
        float vw = Fonts.MEDIUM.getFont(6.5f).width(version);
        context.drawText(Fonts.MEDIUM.getFont(6.5f), version,
            this.x + WIDTH - 16.0f - vw, this.y + 14.5f, TEXT_FAINT.withAlpha(alpha));
        reg.draw();
    }

    private void renderSidebar(UIContext context, float alpha) {
        float baseY = this.y + HEADER_H + 14.0f;

        FontBatching faint = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        context.drawText(Fonts.MEDIUM.getFont(5.5f), "GENERAL",
            this.x + 16.0f, baseY, TEXT_FAINT.withAlpha(alpha));
        faint.draw();
        baseY += 12.0f;

        MenuCategory[] cats = MenuCategory.values();
        int activeIdx = 0;
        for (int i = 0; i < cats.length; i++) {
            if (cats[i] == this.current) { activeIdx = i; break; }
        }
        this.catHighlight.setDuration(220L);
        this.catHighlight.setEasing(Easing.QUAD_OUT);
        this.catHighlight.update(activeIdx * CAT_ITEM_H);

        float pillY = baseY + this.catHighlight.getRGB();
        context.drawRoundedRect(this.x + 10.0f, pillY, SIDEBAR_W - 20.0f, CAT_ITEM_H - 4.0f,
            BorderRadius.all(5.0f), ACCENT_SOFT.mulAlpha(alpha / 255.0f));
        context.drawRoundedRect(this.x + 10.0f, pillY + 4.0f, 2.0f, CAT_ITEM_H - 12.0f,
            BorderRadius.all(1.0f), ACCENT.withAlpha(alpha));

        FontBatching medium = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        for (int i = 0; i < cats.length; i++) {
            MenuCategory cat = cats[i];
            boolean active = cat == this.current;
            ColorRGBA c = (active ? TEXT : TEXT_DIM).withAlpha(alpha);
            context.drawText(Fonts.MEDIUM.getFont(7.5f), cat.getName(),
                this.x + 20.0f, baseY + i * CAT_ITEM_H + 6.0f, c);
        }
        medium.draw();

        float bottomY = this.y + HEIGHT - 70.0f;
        context.drawRoundedRect(this.x + 10.0f, bottomY, SIDEBAR_W - 20.0f, 1.0f,
            BorderRadius.ZERO, BORDER.withAlpha(alpha * 0.6f));

        FontBatching footFaint = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        context.drawText(Fonts.MEDIUM.getFont(5.5f), "CLIENT",
            this.x + 16.0f, bottomY + 8.0f, TEXT_FAINT.withAlpha(alpha));
        footFaint.draw();

        FontBatching footMed = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        context.drawText(Fonts.MEDIUM.getFont(7.5f), "Settings",
            this.x + 20.0f, bottomY + 22.0f, TEXT_DIM.withAlpha(alpha));
        context.drawText(Fonts.MEDIUM.getFont(7.5f), "Log out",
            this.x + 20.0f, bottomY + 38.0f, DANGER.withAlpha(alpha));
        footMed.draw();
    }

    private void renderContent(UIContext context, float alpha) {
        float left = this.x + SIDEBAR_W + 14.0f;
        float top  = this.y + HEADER_H + 14.0f;
        float w    = WIDTH - SIDEBAR_W - 28.0f;
        float h    = HEIGHT - HEADER_H - 28.0f;

        FontBatching head = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(10.0f), this.current.getName(),
            left, top, TEXT.withAlpha(alpha));
        head.draw();

        top += 18.0f;
        h   -= 18.0f;

        List<Module> modules = Rockstar.getInstance().getModuleManager().getModules().stream()
            .filter(m -> m.getCategory().equals(this.current.getCategory()))
            .sorted(Comparator.comparing(Module::getName))
            .toList();

        ScissorUtility.push(context.pose(), left - 4.0f, top, w + 8.0f, h);

        float colW   = (w - 12.0f) / 2.0f;
        float padding = 12.0f;
        float[] colY = { (float) (top + this.scroll.getRGB()), (float) (top + this.scroll.getRGB()) };

        for (Module module : modules) {
            int col = colY[0] <= colY[1] ? 0 : 1;
            float cx = left + col * (colW + padding);
            float cy = colY[col];
            float ch = renderModuleCard(context, module, cx, cy, colW, alpha);
            colY[col] = cy + ch + padding;
        }

        ScissorUtility.pop();

        float contentH = Math.max(colY[0], colY[1]) - (top + (float) this.scroll.getRGB());
        this.scroll.setMax(-Math.max(0.0f, contentH - h + padding));
    }

    private float renderModuleCard(UIContext context, Module module, float cx, float cy, float cw, float alpha) {
        boolean isExpanded = this.expanded.getOrDefault(module, false);
        List<Setting> visible = collectVisible(module, isExpanded);

        float headerH = 30.0f;
        float bodyH = 0.0f;
        for (Setting s : visible) bodyH += settingHeight(s);
        if (!visible.isEmpty()) bodyH += 10.0f;
        float total = headerH + bodyH;

        context.drawRoundedRect(cx, cy, cw, total, BorderRadius.all(6.0f), CARD.withAlpha(alpha));
        context.drawRoundedRect(cx, cy, cw, headerH, BorderRadius.top(6.0f, 6.0f), CARD_HEAD.withAlpha(alpha));
        if (!visible.isEmpty()) {
            context.drawRoundedRect(cx, cy + headerH, cw, 1.0f, BorderRadius.ZERO, BORDER.withAlpha(alpha * 0.7f));
        }

        if (module.isEnabled()) {
            context.drawRoundedRect(cx, cy, 2.0f, headerH, BorderRadius.left(6.0f, 0.0f),
                ACCENT.withAlpha(alpha));
        }

        FontBatching name = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        ColorRGBA nameColor = (module.isEnabled() ? TEXT : TEXT_DIM).withAlpha(alpha);
        context.drawText(Fonts.MEDIUM.getFont(8.5f), module.getName(),
            cx + 12.0f, cy + 11.0f, nameColor);
        name.draw();

        renderToggle(context, cx + cw - 30.0f, cy + 10.5f, module.isEnabled(), alpha);

        float sy = cy + headerH + 6.0f;
        for (Setting s : visible) {
            float h = settingHeight(s);
            renderSetting(context, s, cx + 12.0f, sy, cw - 24.0f, alpha);
            sy += h;
        }

        return total;
    }

    private void renderToggle(UIContext context, float tx, float ty, boolean on, float alpha) {
        float tw = 20.0f, th = 10.0f;
        ColorRGBA track = on ? ACCENT.withAlpha(alpha) : SUNKEN.withAlpha(alpha);
        context.drawRoundedRect(tx, ty, tw, th, BorderRadius.all(5.0f), track);
        float knobX = on ? tx + tw - th + 1.0f : tx + 1.0f;
        ColorRGBA knob = on ? new ColorRGBA(15.0f, 17.0f, 23.0f).withAlpha(alpha) : TEXT_DIM.withAlpha(alpha);
        context.drawRoundedRect(knobX, ty + 1.0f, th - 2.0f, th - 2.0f, BorderRadius.all(4.0f), knob);
    }

    private List<Setting> collectVisible(Module module, boolean expanded) {
        List<Setting> out = new ArrayList<>();
        if (!expanded) return out;
        for (Setting s : module.getSettings()) {
            if (!s.isVisible()) continue;
            if (s instanceof BooleanSetting || s instanceof SliderSetting
                    || s instanceof ModeSetting || s instanceof BindSetting) {
                out.add(s);
            }
        }
        return out;
    }

    private float settingHeight(Setting s) {
        if (s instanceof BooleanSetting) return 16.0f;
        if (s instanceof SliderSetting)  return 26.0f;
        if (s instanceof ModeSetting m)  return this.modeOpen.getOrDefault(m, false) ? 22.0f + m.getValues().size() * 14.0f : 22.0f;
        if (s instanceof BindSetting)    return 16.0f;
        return 0.0f;
    }

    private void renderSetting(UIContext context, Setting s, float sx, float sy, float sw, float alpha) {
        Font labelFont = Fonts.MEDIUM.getFont(7.0f);
        Font valueFont = Fonts.MEDIUM.getFont(6.5f);

        if (s instanceof BooleanSetting b) {
            float boxSize = 9.0f;
            float bx = sx + sw - boxSize;
            float by = sy + 3.0f;
            ColorRGBA fill = b.isEnabled() ? ACCENT.withAlpha(alpha) : SUNKEN.withAlpha(alpha);
            context.drawRoundedRect(bx, by, boxSize, boxSize, BorderRadius.all(2.0f), fill);
            if (!b.isEnabled()) {
                context.drawRoundedRect(bx + 1.0f, by + 1.0f, boxSize - 2.0f, boxSize - 2.0f,
                    BorderRadius.all(2.0f), CARD.withAlpha(alpha));
            }
            FontBatching fb = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            context.drawText(labelFont, Localizator.translate(b.getName()),
                sx, sy + 4.5f, TEXT_DIM.withAlpha(alpha));
            fb.draw();
            return;
        }

        if (s instanceof SliderSetting sl) {
            FontBatching fb = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            context.drawText(labelFont, Localizator.translate(sl.getName()),
                sx, sy + 2.0f, TEXT_DIM.withAlpha(alpha));
            String val = formatNumber(sl.getCurrentValue()) + sl.getSuffix();
            float vw = valueFont.width(val);
            context.drawText(valueFont, val, sx + sw - vw, sy + 3.0f, TEXT.withAlpha(alpha));
            fb.draw();

            float trackY = sy + 16.0f;
            float trackH = 3.0f;
            context.drawRoundedRect(sx, trackY, sw, trackH, BorderRadius.all(1.5f), SUNKEN.withAlpha(alpha));
            float pct = (sl.getCurrentValue() - sl.getMin()) / Math.max(0.001f, sl.getMax() - sl.getMin());
            pct = Math.max(0.0f, Math.min(1.0f, pct));
            float fillW = sw * pct;
            context.drawRoundedRect(sx, trackY, fillW, trackH, BorderRadius.all(1.5f), ACCENT.withAlpha(alpha));
            float knobX = sx + fillW - 3.0f;
            context.drawRoundedRect(knobX, trackY - 2.5f, 6.0f, 8.0f, BorderRadius.all(2.0f), TEXT.withAlpha(alpha));
            return;
        }

        if (s instanceof ModeSetting m) {
            FontBatching fb = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            context.drawText(labelFont, Localizator.translate(m.getName()),
                sx, sy + 2.0f, TEXT_DIM.withAlpha(alpha));
            fb.draw();

            float boxW = Math.min(80.0f, sw * 0.55f);
            float boxX = sx + sw - boxW;
            float boxY = sy + 12.0f;
            float boxH = 14.0f;
            context.drawRoundedRect(boxX, boxY, boxW, boxH, BorderRadius.all(3.0f), SUNKEN.withAlpha(alpha));
            String curName = m.getValue() != null ? Localizator.translate(m.getValue().getName()) : "-";
            FontBatching fb2 = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            context.drawText(valueFont, curName, boxX + 6.0f, boxY + 4.0f, TEXT.withAlpha(alpha));
            String arrow = this.modeOpen.getOrDefault(m, false) ? "^" : "v";
            float aw = valueFont.width(arrow);
            context.drawText(valueFont, arrow, boxX + boxW - 6.0f - aw, boxY + 4.0f, TEXT_DIM.withAlpha(alpha));
            fb2.draw();

            if (this.modeOpen.getOrDefault(m, false)) {
                float listY = boxY + boxH + 2.0f;
                float listH = m.getValues().size() * 14.0f;
                context.drawRoundedRect(boxX, listY, boxW, listH, BorderRadius.all(3.0f), SUNKEN.withAlpha(alpha));
                FontBatching fb3 = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
                for (int i = 0; i < m.getValues().size(); i++) {
                    ModeSetting.Value v = m.getValues().get(i);
                    ColorRGBA c = v.isSelected() ? ACCENT.withAlpha(alpha) : TEXT_DIM.withAlpha(alpha);
                    context.drawText(valueFont, Localizator.translate(v.getName()),
                        boxX + 6.0f, listY + 4.0f + i * 14.0f, c);
                }
                fb3.draw();
            }
            return;
        }

        if (s instanceof BindSetting bnd) {
            FontBatching fb = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            context.drawText(labelFont, Localizator.translate(bnd.getName()),
                sx, sy + 4.0f, TEXT_DIM.withAlpha(alpha));
            fb.draw();

            String keyName;
            if (this.bindingNow == bnd) keyName = "...";
            else if (bnd.getKey() == -1) keyName = "NONE";
            else keyName = TextUtility.getKeyName(bnd.getKey());

            float kw = valueFont.width(keyName) + 12.0f;
            float kx = sx + sw - kw;
            float ky = sy + 1.0f;
            ColorRGBA bg = this.bindingNow == bnd ? ACCENT_SOFT.mulAlpha(alpha / 255.0f) : SUNKEN.withAlpha(alpha);
            context.drawRoundedRect(kx, ky, kw, 14.0f, BorderRadius.all(3.0f), bg);
            FontBatching fb2 = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
            ColorRGBA kc = this.bindingNow == bnd ? ACCENT.withAlpha(alpha) : TEXT.withAlpha(alpha);
            context.drawText(valueFont, keyName, kx + 6.0f, ky + 4.0f, kc);
            fb2.draw();
        }
    }

    private String formatNumber(float v) {
        if (v == (int) v) return Integer.toString((int) v);
        return String.format("%.2f", v);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bindingNow != null) {
            if (button == MouseButton.RIGHT) {
                this.bindingNow.setKey(-1);
            } else {
                this.bindingNow.setKey(-100 - button.getButtonIndex());
            }
            this.bindingNow = null;
            super.onMouseClicked(mouseX, mouseY, button);
            return;
        }

        float baseY = this.y + HEADER_H + 14.0f + 12.0f;
        MenuCategory[] cats = MenuCategory.values();
        for (int i = 0; i < cats.length; i++) {
            if (GuiUtility.isHovered(this.x + 10.0f, baseY + i * CAT_ITEM_H,
                    SIDEBAR_W - 20.0f, CAT_ITEM_H - 4.0f, mouseX, mouseY)) {
                this.current = cats[i];
                this.scroll.reset();
                super.onMouseClicked(mouseX, mouseY, button);
                return;
            }
        }

        float left = this.x + SIDEBAR_W + 14.0f;
        float top  = this.y + HEADER_H + 14.0f + 18.0f;
        float w    = WIDTH - SIDEBAR_W - 28.0f;
        float colW = (w - 12.0f) / 2.0f;
        float padding = 12.0f;
        float[] colY = { (float) (top + this.scroll.getRGB()), (float) (top + this.scroll.getRGB()) };

        List<Module> modules = Rockstar.getInstance().getModuleManager().getModules().stream()
            .filter(m -> m.getCategory().equals(this.current.getCategory()))
            .sorted(Comparator.comparing(Module::getName))
            .toList();

        for (Module module : modules) {
            int col = colY[0] <= colY[1] ? 0 : 1;
            float cx = left + col * (colW + padding);
            float cy = colY[col];
            float ch = computeCardHeight(module);

            if (GuiUtility.isHovered(cx + colW - 32.0f, cy + 8.0f, 24.0f, 16.0f, mouseX, mouseY)
                && button == MouseButton.LEFT) {
                module.setEnabled(!module.isEnabled(), false);
                super.onMouseClicked(mouseX, mouseY, button);
                return;
            }
            if (GuiUtility.isHovered(cx, cy, colW, 30.0f, mouseX, mouseY)
                && button == MouseButton.RIGHT) {
                this.expanded.put(module, !this.expanded.getOrDefault(module, false));
                super.onMouseClicked(mouseX, mouseY, button);
                return;
            }

            if (this.expanded.getOrDefault(module, false)
                && handleSettingClick(module, cx + 12.0f, cy + 30.0f + 6.0f, colW - 24.0f, mouseX, mouseY, button)) {
                super.onMouseClicked(mouseX, mouseY, button);
                return;
            }

            colY[col] = cy + ch + padding;
        }

        if (GuiUtility.isHovered(this.x, this.y, WIDTH, HEADER_H, mouseX, mouseY)) {
            this.drag = true;
            this.dragX = (float) (mouseX - this.x);
            this.dragY = (float) (mouseY - this.y);
        }

        super.onMouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSettingClick(Module module, float sx, float sy, float sw,
                                       double mouseX, double mouseY, MouseButton button) {
        for (Setting s : collectVisible(module, true)) {
            float h = settingHeight(s);

            if (s instanceof BooleanSetting b) {
                if (button == MouseButton.LEFT && GuiUtility.isHovered(sx, sy, sw, h, mouseX, mouseY)) {
                    b.toggle();
                    return true;
                }
            } else if (s instanceof SliderSetting sl) {
                float trackY = sy + 14.0f;
                if (button == MouseButton.LEFT
                    && GuiUtility.isHovered(sx - 2.0f, trackY - 4.0f, sw + 4.0f, 12.0f, mouseX, mouseY)) {
                    this.draggingRange = new RangeRef(sl, sx, sw);
                    updateSliderFromMouse(sl, sx, sw, mouseX);
                    return true;
                }
            } else if (s instanceof ModeSetting m) {
                float boxW = Math.min(80.0f, sw * 0.55f);
                float boxX = sx + sw - boxW;
                float boxY = sy + 10.0f;
                float boxH = 14.0f;
                if (GuiUtility.isHovered(boxX, boxY, boxW, boxH, mouseX, mouseY)) {
                    if (button == MouseButton.LEFT) {
                        this.modeOpen.put(m, !this.modeOpen.getOrDefault(m, false));
                    } else if (button == MouseButton.RIGHT) {
                        cycleMode(m, 1);
                    }
                    return true;
                }
                if (this.modeOpen.getOrDefault(m, false)) {
                    float listY = boxY + boxH + 2.0f;
                    for (int i = 0; i < m.getValues().size(); i++) {
                        if (button == MouseButton.LEFT
                            && GuiUtility.isHovered(boxX, listY + i * 14.0f, boxW, 14.0f, mouseX, mouseY)) {
                            m.setValue(m.getValues().get(i));
                            this.modeOpen.put(m, false);
                            return true;
                        }
                    }
                }
            } else if (s instanceof BindSetting bnd) {
                if (GuiUtility.isHovered(sx + sw - 60.0f, sy, 60.0f, h, mouseX, mouseY)) {
                    if (button == MouseButton.LEFT) {
                        this.bindingNow = bnd;
                    } else if (button == MouseButton.RIGHT) {
                        bnd.setKey(-1);
                    }
                    return true;
                }
            }

            sy += h;
        }
        return false;
    }

    private void cycleMode(ModeSetting m, int dir) {
        List<ModeSetting.Value> vs = m.getValues();
        if (vs.isEmpty()) return;
        int idx = 0;
        for (int i = 0; i < vs.size(); i++) if (vs.get(i).isSelected()) { idx = i; break; }
        idx = Math.floorMod(idx + dir, vs.size());
        m.setValue(vs.get(idx));
    }

    private void updateSliderFromMouse(SliderSetting sl, float trackX, float trackW, double mouseX) {
        float pct = (float) ((mouseX - trackX) / trackW);
        pct = Math.max(0.0f, Math.min(1.0f, pct));
        sl.setCurrentValue(sl.getMin() + pct * (sl.getMax() - sl.getMin()));
    }

    private float computeCardHeight(Module module) {
        float h = 30.0f;
        if (this.expanded.getOrDefault(module, false)) {
            float body = 0.0f;
            for (Setting s : collectVisible(module, true)) body += settingHeight(s);
            if (body > 0.0f) h += body + 10.0f;
        }
        return h;
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        this.draggingRange = null;
        this.draggingSlider = null;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        if (this.draggingRange != null) {
            updateSliderFromMouse(this.draggingRange.setting(),
                this.draggingRange.trackX(), this.draggingRange.trackW(), mouseX);
        }
        super.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (GuiUtility.isHovered(this.x + SIDEBAR_W, this.y + HEADER_H,
                WIDTH - SIDEBAR_W, HEIGHT - HEADER_H, mouseX, mouseY)) {
            this.scroll.scroll(verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (this.bindingNow != null) {
            if (key == 256) {
                this.bindingNow.setKey(-1);
            } else {
                this.bindingNow.setKey(key);
            }
            this.bindingNow = null;
            return true;
        }
        if (key == 256) {
            this.closing = true;
            Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).disable();
            return true;
        }
        return super.keyPressed(event);
    }
}
