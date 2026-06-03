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
 * «Батч» для закруглённых прямоугольников.
 * Реально батчить нельзя — каждый элемент требует отдельных юниформов Size/Radius.
 * Поэтому каждый add() рисует немедленно своим draw call'ом.
 * Не открывает Tesselator-буфер в конструкторе — нет варнингов.
 */
public class RoundedRectBatching extends Batching {
    private final GlProgram rectangleProgram = DrawUtility.rectangleProgram;
    private float smoothness = 0.5f;

    public RoundedRectBatching() {
        // Используем no-buffer конструктор: не открываем Tesselator batch
        super();
    }

    public RoundedRectBatching smoothness(float s) {
        this.smoothness = s;
        return this;
    }

    /**
     * Рисует прямоугольник немедленно с правильными юниформами.
     */
    public void add(Matrix4f matrix, float x, float y, float width, float height,
                    float radiusTL, float radiusBL, float radiusTR, float radiusBR, int rgba) {
        float s = this.smoothness;

        this.rectangleProgram.use();
        this.rectangleProgram.findUniform("Size").set(width, height);
        this.rectangleProgram.findUniform("Radius").set(radiusTL, radiusBL, radiusTR, radiusBR);
        this.rectangleProgram.findUniform("Smoothness").set(s);

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