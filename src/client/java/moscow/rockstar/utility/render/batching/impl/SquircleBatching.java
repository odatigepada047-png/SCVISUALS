package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.batching.Batching;
import org.joml.Matrix4f;

/**
 * «Батч» для squircle-прямоугольников.
 * Реально батчить нельзя — каждый элемент требует отдельных юниформов Size/Radius/CornerSmoothness.
 * Поэтому каждый add() рисует немедленно своим draw call'ом.
 * Не открывает Tesselator-буфер в конструкторе — нет варнингов.
 */
public class SquircleBatching extends Batching {
    private final GlProgram squircleProgram = DrawUtility.getSquircleProgram();
    private final float smoothness;
    private final float squirt;

    public SquircleBatching(float squirt) {
        // Используем no-buffer конструктор: не открываем Tesselator batch
        super();
        this.smoothness = 0.5f;
        this.squirt = squirt;
    }

    /**
     * Рисует squircle-прямоугольник немедленно с правильными юниформами.
     */
    public void add(Matrix4f matrix, float x, float y, float width, float height,
                    float radiusTL, float radiusBL, float radiusTR, float radiusBR, int rgba) {
        float s = this.smoothness;

        this.squircleProgram.use();
        this.squircleProgram.findUniform("Size").set(width, height);
        this.squircleProgram.findUniform("Radius").set(radiusTL, radiusBL, radiusTR, radiusBR);
        this.squircleProgram.findUniform("Smoothness").set(s);
        this.squircleProgram.findUniform("CornerSmoothness").set(this.squirt);

        float hp = -s / 2.0f + s * 2.0f;
        float vp =  s / 2.0f + s;
        float ax = x - hp / 2.0f;
        float ay = y - vp / 2.0f;
        float aw = width + hp;
        float ah = height + vp;

        BufferBuilder bb = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bb.addVertex(matrix, ax,      ay,      0.0f).setColor(rgba);
        bb.addVertex(matrix, ax,      ay + ah, 0.0f).setColor(rgba);
        bb.addVertex(matrix, ax + aw, ay + ah, 0.0f).setColor(rgba);
        bb.addVertex(matrix, ax + aw, ay,      0.0f).setColor(rgba);

        MeshData mesh = bb.build();
        if (mesh != null) {
            MeshDrawHelper.drawBuilt(mesh);
        }
    }

    @Override
    public void draw() {
        // Всё уже нарисовано в add(). Только очищаем active.
        GlProgram.clearActive();
        if (active == this) {
            active = null;
        }
    }
}