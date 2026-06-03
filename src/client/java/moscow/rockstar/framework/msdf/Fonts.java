/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.framework.msdf;

import lombok.Generated;
import moscow.rockstar.framework.msdf.MsdfFont;

public final class Fonts {
    public static final MsdfFont BOLD = MsdfFont.builder().atlas("bold").data("bold").build();
    public static final MsdfFont MEDIUM = MsdfFont.builder().atlas("medium").data("medium").build();
    public static final MsdfFont REGULAR = MsdfFont.builder().atlas("regular").data("regular").build();
    public static final MsdfFont SEMIBOLD = MsdfFont.builder().atlas("semibold").data("semibold").build();
    public static final MsdfFont ROUND_BOLD = MsdfFont.builder().atlas("roundbold").data("roundbold").build();
    public static final MsdfFont WAYPOINT_ICONS = MsdfFont.builder().atlas("waypoint_icons").data("waypoint_icons").build();

    public static void clearWidthCaches() {
        BOLD.clearWidthCache();
        MEDIUM.clearWidthCache();
        REGULAR.clearWidthCache();
        SEMIBOLD.clearWidthCache();
        ROUND_BOLD.clearWidthCache();
        WAYPOINT_ICONS.clearWidthCache();
    }

    @Generated
    private Fonts() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

