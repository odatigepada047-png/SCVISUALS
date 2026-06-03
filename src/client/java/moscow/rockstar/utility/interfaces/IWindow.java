/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.util.Window
 */
package moscow.rockstar.utility.interfaces;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;

public interface IWindow {
    public static final Window mw = Minecraft.getInstance().getWindow();
}

