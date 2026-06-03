/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.ChatFormatting
 *  pw.lucent.bridge.client.handler.PacketHandler
 *  pw.lucent.bridge.shared.packet.impl.server.community.S2CPacketIRCMessage
 */
package moscow.rockstar.systems.bridge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import pw.lucent.bridge.client.handler.PacketHandler;
import pw.lucent.bridge.shared.packet.impl.server.community.S2CPacketIRCMessage;

public class ServerPacketHandler
extends PacketHandler {
    public void handle(S2CPacketIRCMessage packet) {
        if (Minecraft.getInstance().player != null) {
            var user = packet.getUserInfo();
            String name = user != null ? String.valueOf(user) : "?";
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.valueOf(ChatFormatting.DARK_RED) + "[IRC] " + ChatFormatting.WHITE + name + ": " + ChatFormatting.GRAY + packet.getMessage()));
        }
    }
}

