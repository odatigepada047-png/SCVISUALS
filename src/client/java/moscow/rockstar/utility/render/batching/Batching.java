/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  moscow.rockstar.utility.render.MeshDrawHelper
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$DrawMode
 */
package moscow.rockstar.utility.render.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import com.mojang.blaze3d.vertex.BufferBuilder;
import moscow.rockstar.utility.render.MeshDrawHelper;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

public abstract class Batching {
    protected static Batching active;
    protected BufferBuilder builder;

    public Batching(VertexFormat vertexFormat) {
        this.builder = com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, vertexFormat);
        active = this;
    }

    /**
     * No-buffer constructor for deferred batchers (RoundedRectBatching, SquircleBatching).
     */
    protected Batching() {
        this.builder = null;
        active = this;
    }

    protected void build() {
        MeshData mesh = this.builder.build();
        if (mesh != null) {
            if (mesh.drawState().vertexCount() > 0) {
                MeshDrawHelper.drawBuilt(mesh);
            } else {
                mesh.close();
            }
        }
    }

    public abstract void draw();

    @Generated
    public BufferBuilder getBuilder() {
        return this.builder;
    }

    @Generated
    public static Batching getActive() {
        return active;
    }

    /** Clears leaked active batching between deferred GUI flushes. */
    public static void clearActive() {
        active = null;
    }
}

