package moscow.rockstar.framework.msdf;

import com.mojang.blaze3d.vertex.VertexConsumer;
import moscow.rockstar.framework.msdf.FontData;
import org.joml.Matrix4f;

public final class MsdfGlyph {
    private final int code;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float advance;
    private final float topPosition;
    private final float width;
    private final float height;

    public MsdfGlyph(FontData.GlyphData data, float atlasWidth, float atlasHeight) {
        this.code = data.unicode();
        this.advance = data.advance();
        FontData.BoundsData atlasBounds = data.atlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.left() / atlasWidth;
            this.maxU = atlasBounds.right() / atlasWidth;
            this.minV = 1.0f - atlasBounds.top() / atlasHeight;
            this.maxV = 1.0f - atlasBounds.bottom() / atlasHeight;
        } else {
            this.maxV = 0.0f;
            this.minV = 0.0f;
            this.maxU = 0.0f;
            this.minU = 0.0f;
        }
        FontData.BoundsData planeBounds = data.planeBounds();
        if (planeBounds != null) {
            this.width = planeBounds.right() - planeBounds.left();
            this.height = planeBounds.top() - planeBounds.bottom();
            this.topPosition = planeBounds.top();
        } else {
            this.topPosition = 0.0f;
            this.height = 0.0f;
            this.width = 0.0f;
        }
    }

    public float apply(Matrix4f matrix, VertexConsumer consumer, float size, float x, float y, float z, int color) {
        float width = this.width * size;
        float height = this.height * size;
        y -= this.topPosition * size;
        consumer.addVertex(matrix, x, y, z).setUv(this.minU, this.minV).setColor(color);
        consumer.addVertex(matrix, x, y + height, z).setUv(this.minU, this.maxV).setColor(color);
        consumer.addVertex(matrix, x + width, y + height, z).setUv(this.maxU, this.maxV).setColor(color);
        consumer.addVertex(matrix, x + width, y, z).setUv(this.maxU, this.minV).setColor(color);
        return this.advance * size;
    }

    public float getWidth(float size) {
        return this.advance * size;
    }

    public int getCharCode() {
        return this.code;
    }
}
