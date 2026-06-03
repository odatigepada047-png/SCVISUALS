/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 */
package moscow.rockstar.utility.render.batching.impl;

import lombok.Generated;
import moscow.rockstar.utility.render.batching.Batching;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

public class BlurBatching
extends Batching {
    private final PoseStack matrices;
    private final float width;
    private final float height;

    public BlurBatching(VertexFormat vertexFormat, PoseStack matrices, float width, float height) {
        super(vertexFormat);
        this.matrices = matrices;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw() {
        if (active == this) {
            active = null;
        }
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }
}

