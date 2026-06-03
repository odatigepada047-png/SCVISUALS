/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.framework.msdf;

import lombok.Generated;
import moscow.rockstar.framework.msdf.MsdfFont;
import net.minecraft.network.chat.Component;

public class Font {
    private MsdfFont font;
    private float size;

    public float height() {
        return this.size * 0.7f;
    }

    public float width(String text) {
        return this.font.getWidth(text, this.size);
    }

    public float width(Component text) {
        return this.font.getTextWidth(text, this.size);
    }

    @Generated
    public MsdfFont getFont() {
        return this.font;
    }

    @Generated
    public float getSize() {
        return this.size;
    }

    @Generated
    public Font(MsdfFont font, float size) {
        this.font = font;
        this.size = size;
    }
}

