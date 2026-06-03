package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.hud.impl.TrapTimeHud;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.GLStateSnapshot;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

@ModuleInfo(name="Object Info", category=ModuleCategory.PLAYER, desc="Показывает информацию о трапках и пластах в мире")
public class ObjectInfo
extends BaseModule {
    private final Map<BlockPos, Info> infos = new HashMap<BlockPos, Info>();
    private final Timer timer = new Timer();

    private BlockPos findCenter(BlockPos pos, ObjType type) {
        if (IMinecraft.mc.level == null) return pos;

        net.minecraft.world.level.block.Block targetBlock = null;
        if (type == ObjType.DRAGON_FT || type == ObjType.DRAGON_ST) {
            targetBlock = net.minecraft.world.level.block.Blocks.RESPAWN_ANCHOR;
        } else if (type == ObjType.TRAP) {
            targetBlock = net.minecraft.world.level.block.Blocks.COBBLESTONE;
        }

        if (targetBlock != null) {
            int lowestY = Integer.MAX_VALUE;
            java.util.List<BlockPos> blocksAtLowestY = new java.util.ArrayList<>();

            for (int y = -8; y <= 4; y++) {
                boolean foundAtThisY = false;
                for (int x = -8; x <= 8; x++) {
                    for (int z = -8; z <= 8; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        if (IMinecraft.mc.level.getBlockState(p).is(targetBlock)) {
                            if (y < lowestY) {
                                lowestY = y;
                                blocksAtLowestY.clear();
                                blocksAtLowestY.add(p);
                                foundAtThisY = true;
                            } else if (y == lowestY) {
                                blocksAtLowestY.add(p);
                            }
                        }
                    }
                }
                if (foundAtThisY) break;
            }

            if (!blocksAtLowestY.isEmpty()) {
                long sumX = 0, sumZ = 0;
                for (BlockPos p : blocksAtLowestY) {
                    sumX += p.getX();
                    sumZ += p.getZ();
                }
                return new BlockPos((int)Math.floor((double)sumX / blocksAtLowestY.size()), lowestY + pos.getY(), (int)Math.floor((double)sumZ / blocksAtLowestY.size()));
            }
        }
        return pos;
    }

    private final EventListener<ReceivePacketEvent> onSoundInstanceEvent = event -> {
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof ClientboundSoundPacket) {
            BlockPos pos;
            ClientboundSoundPacket sound = (ClientboundSoundPacket)patt0$temp;
            String soundName = sound.getSound().unwrapKey().map(key -> key.identifier().toString()).orElse("");
            if (soundName.contains("minecraft:block.piston.extend") || soundName.contains("minecraft:block.piston.contract")) {
                pos = BlockPos.containing(sound.getX(), sound.getY(), sound.getZ());
                if ((sound.getVolume() == 0.5f || sound.getVolume() == 0.7f) && sound.getPitch() == 0.5f) {
                    BlockPos center = findCenter(pos, ObjType.TRAP);
                    this.infos.put(center, new Info(center, ObjType.TRAP));
                }
                this.timer.reset();
            }
            if (soundName.contains("minecraft:block.anvil.place")) {
                pos = BlockPos.containing(sound.getX(), sound.getY(), sound.getZ());
                if (!(sound.getVolume() != 0.5f && sound.getVolume() != 0.7f || sound.getPitch() != 1.1f && sound.getPitch() != 0.5f)) {
                    this.infos.put(pos, new Info(pos.above(), ObjType.PLAST));
                }
                this.timer.reset();
            }
            if (soundName.contains("entity.evoker_fangs.attack")) {
                pos = BlockPos.containing(sound.getX(), sound.getY(), sound.getZ());
                if (sound.getVolume() == 0.5f || sound.getVolume() == 0.7f) {
                    ObjType type = sound.getPitch() == 0.85f ? ObjType.DRAGON_FT : (sound.getPitch() == 1.0f ? ObjType.DRAGON_ST : null);
                    if (type != null) {
                        BlockPos center = findCenter(pos, type);
                        this.infos.put(center, new Info(center, type));
                    }
                }
                this.timer.reset();
            }
        }
    };
    private final EventListener<PreHudRenderEvent> onRender2D = event -> {
        BlockPos toRemove = null;
        TrapTimeHud trapTimeHud = Rockstar.getInstance().getHud().getElementByName("hud.traptime");
        if (trapTimeHud != null) {
            trapTimeHud.clear();
        }
        long trapRemainedMs = -1L;
        long trapTotalMs = 0L;
        long dragonRemainedMs = -1L;
        long dragonTotalMs = 0L;
        for (Map.Entry<BlockPos, Info> entry : this.infos.entrySet()) {
            Info info = entry.getValue();
            info.draw((PreHudRenderEvent)event);
            
            if (IMinecraft.mc.player != null) {
                BlockPos playerPos = IMinecraft.mc.player.blockPosition();
                int dx = Math.abs(playerPos.getX() - info.pos.getX());
                int dz = Math.abs(playerPos.getZ() - info.pos.getZ());
                long remainedMs = info.type.getTime() - info.start.getElapsedTime();
                
                if (info.type == ObjType.TRAP && dx <= 1 && dz <= 1) {
                    trapRemainedMs = remainedMs;
                    trapTotalMs = info.type.getTime();
                } else if ((info.type == ObjType.DRAGON_FT || info.type == ObjType.DRAGON_ST) && dx <= 3 && dz <= 3) {
                    dragonRemainedMs = remainedMs;
                    dragonTotalMs = info.type.getTime();
                }
            }
            
            if (!info.start.finished(info.getType().getTime())) continue;
            toRemove = entry.getKey();
        }
        if (trapTimeHud != null) {
            if (trapRemainedMs >= 0L) {
                trapTimeHud.showTrap(trapRemainedMs, trapTotalMs);
            } else if (dragonRemainedMs >= 0L) {
                trapTimeHud.showDragon(dragonRemainedMs, dragonTotalMs);
            }
        }
        
        if (toRemove != null) {
            this.infos.remove(toRemove);
        }
    };
    private final EventListener<Render3DEvent> onRender3D = event -> {
        BlockPos toRemove = null;
        for (Map.Entry<BlockPos, Info> entry : this.infos.entrySet()) {
            Info info = entry.getValue();
            info.draw3D((Render3DEvent)event);
            if (!info.start.finished(info.getType().getTime())) continue;
            toRemove = entry.getKey();
        }
        if (toRemove != null) {
            this.infos.remove(toRemove);
        }
    };

    static class Info {
        final BlockPos pos;
        final ObjType type;
        Timer start = new Timer();

        void draw(PreHudRenderEvent e) {
            int remained = (int)((float)(this.type.getTime() - this.start.getElapsedTime()) / 1000.0f);
            Matrix3x2fStack matrices = e.getContext().pose();
            BlockPos renderPos = this.pos;
            Vec3 renderPosAdjusted = renderPos.getCenter().add(0.0, 1.0, 0.0);
            Vec2 screenPos = Utils.worldToScreen(renderPosAdjusted);
            if (screenPos != null) {
                float distance = (float)IMinecraft.mc.player.position().distanceTo(new Vec3(renderPos.getX(), renderPos.getY(), renderPos.getZ()));
                float scale = Mth.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
                matrices.pushMatrix();
                matrices.translate(screenPos.x, screenPos.y);
                matrices.scale(scale, scale);
                
                String text = this.type.getName() + " (" + remained + " sec)";
                float textWidth = (float)Fonts.MEDIUM.getFont(11.0f).width(text);
                float totalWidth = textWidth + 28;
                float totalHeight = 14.0f;
                float x = -totalWidth / 2.0f;
                float y = 0.0f;
                float radius = totalHeight / 2.0f;

                // Simple Clean Background
//                 e.getContext().drawRoundedRect(x, y, totalWidth, totalHeight, BorderRadius.all(radius), new ColorRGBA(10, 10, 10, 180));
                
                // Item and Component (Aligned perfectly)
                float itemScale = 0.55f;
                e.getContext().drawItem(new ItemStack(this.type.getItem()), x + 5, y + (totalHeight - 16 * itemScale) / 2.0f, itemScale);
                e.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x + 19, y + (totalHeight - Fonts.MEDIUM.getFont(11.0f).height()) / 2.0f + 0.5f, ColorRGBA.WHITE);
                
                matrices.popMatrix();
            }
        }

        void draw3D(Render3DEvent e) {
            if (IMinecraft.mc.level == null || IMinecraft.mc.player == null || this.type == ObjType.PLAST) {
                return;
            }
            PoseStack matrices = e.pose();
            Camera camera = IMinecraft.mc.gameRenderer.getMainCamera();
            Vec3 cameraPos = camera.position();
            matrices.pushPose();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            GLStateSnapshot glState = GLStateSnapshot.capture();
            try {
                AABB box;
                if (this.type == ObjType.DRAGON_FT || this.type == ObjType.DRAGON_ST) {
                    // Centered on Respawn Anchor, 7x7 area, 7 high
                    box = new AABB(this.pos.getX() - 3, this.pos.getY(), this.pos.getZ() - 3, 
                                 this.pos.getX() + 4, this.pos.getY() + 7, this.pos.getZ() + 4);
                } else if (this.type == ObjType.TRAP) {
                    // Centered on Cobblestone floor block, 5x5 area, 5 high, starting 1 block lower
                    box = new AABB(this.pos.getX() - 2, this.pos.getY() - 1, this.pos.getZ() - 2, 
                                 this.pos.getX() + 3, this.pos.getY() + 4, this.pos.getZ() + 3);
                } else {
                    box = new AABB(this.pos).inflate(1.0);
                }
                
                ColorRGBA accent = moscow.rockstar.utility.colors.Colors.getAccentColor();
                
                // Draw Outlined AABB (Stripes)
    //             RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
                BufferBuilder bufferLines = com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                Draw3DUtility.renderOutlinedBox(matrices, bufferLines, box, accent.withAlpha(150.0f));
                MeshData builtLines = bufferLines.build();
                if (builtLines != null) {
                    builtLines.close();
                }
            } finally {
                glState.restore();
                matrices.popPose();
            }
        }

        @Generated
        public BlockPos getPos() {
            return this.pos;
        }

        @Generated
        public ObjType getType() {
            return this.type;
        }

        @Generated
        public Timer getStart() {
            return this.start;
        }

        @Generated
        public Info(BlockPos pos, ObjType type) {
            this.pos = pos;
            this.type = type;
        }
    }

    static enum ObjType {
        TRAP("Трапка", Items.NETHERITE_SCRAP, 15000L),
        DRAGON_FT("Драконка", Items.NETHERITE_SCRAP, 30000L),
        DRAGON_ST("Драконка", Items.NETHERITE_SCRAP, 30000L),
        PLAST("Пласт", Items.DRIED_KELP, 20000L);

        final String name;
        final Item item;
        final long time;

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public Item getItem() {
            return this.item;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        private ObjType(String name, Item item, long time) {
            this.name = name;
            this.item = item;
            this.time = time;
        }
    }
}
