package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Generated;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.batching.Batching;

public class RectBatching extends Batching {
    private final PoseStack matrices;

    public RectBatching(VertexFormat vertexFormat, PoseStack matrices) {
        super(vertexFormat);
        this.matrices = matrices;
    }

    public RectBatching(VertexFormat vertexFormat) {
        this(vertexFormat, new PoseStack());
    }

    @Override
    public void draw() {
        GlProgram.usePositionColor();
        DrawUtility.drawSetup();
        this.build();
        DrawUtility.drawEnd();
        if (active == this) {
            active = null;
        }
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }
}
