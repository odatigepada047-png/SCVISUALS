/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.game;

import moscow.rockstar.utility.interfaces.IMinecraft;

public class ScaledResolution
implements IMinecraft {
    public Number getNumberScaledWidth() {
        return mc.getWindow().getGuiScaledWidth();
    }

    public Number getNumberScaledHeight() {
        return mc.getWindow().getGuiScaledHeight();
    }

    public Number getNumberScaleFactor() {
        return mc.getWindow().getGuiScale();
    }

    public float getGuiScaledWidth() {
        return mc.getWindow().getGuiScaledWidth();
    }

    public float getGuiScaledHeight() {
        return mc.getWindow().getGuiScaledHeight();
    }

    public double getScaleFactor() {
        return mc.getWindow().getGuiScale();
    }
}

