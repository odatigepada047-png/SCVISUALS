package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Generated;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.render.batching.Batching;

public class IconBatching extends Batching {
    private final PoseStack matrices;

    public IconBatching(VertexFormat vertexFormat, PoseStack matrices) {
        super(vertexFormat);
        this.matrices = matrices;
    }

    public IconBatching(VertexFormat vertexFormat) {
        this(vertexFormat, new PoseStack());
    }

    @Override
    public void draw() {
        if (this.builder != null) {
            GlProgram.usePositionTexColor();
            this.build();
            DrawUtility.drawEnd();
            TextureBinder.unbind();
        }
        if (active == this) {
            active = null;
        }
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }
}
