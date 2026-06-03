/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility;

import lombok.Generated;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class CustomParticle implements IMinecraft {
    private Vec3 position;
    private Vec3 prevPosition;
    private Vec3 velocity;
    private float size = 3.0f;
    private final Identifier texture;
    private int age;
    private int maxAge = 100;
    private double gravityStrength = 0.04;
    private boolean alive;
    private boolean collidesWithWorld;
    private final Animation alphaAnimation;
    private final Animation sizeAnimation;
    private final ColorRGBA color;

    public CustomParticle(Vec3 position, Vec3 velocity, Identifier texture, ColorRGBA color) {
        this.position = position;
        this.prevPosition = position;
        this.velocity = velocity;
        this.texture = texture;
        this.color = color;
        this.age = 0;
        long animationDuration = this.maxAge * 5;
        this.alphaAnimation = new Animation(animationDuration, Easing.CUBIC_IN_OUT);
        this.sizeAnimation = new Animation(animationDuration, Easing.LINEAR);
        this.alive = true;
    }

    public void tick() {
        ++this.age;
        if (this.age >= this.maxAge) {
            this.markDead();
        }
        this.prevPosition = this.position;
        this.position = this.position.add(this.velocity);
    }

    public void render(BufferBuilder builder, Camera camera) {
        this.alphaAnimation.update(this.isDead() ? 0.0f : 1.0f);
        this.sizeAnimation.update(this.isDead() ? 0.0f : 1.0f);
        // TODO: 26.1 - particle billboard rendering needs BufferBuilder/PoseStack API migration
    }

    public void setPosition(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
    }

    public void setVelocity(double x, double y, double z) {
        this.velocity = new Vec3(x, y, z);
    }

    private void markDead() {
        this.alive = false;
    }

    public boolean isDead() {
        return !this.alive;
    }

    public boolean shouldRemove() {
        return this.isDead() && this.alphaAnimation.getRGB() == 0.0f;
    }

    @Generated
    public Vec3 getPosition() {
        return this.position;
    }

    @Generated
    public Vec3 getPrevPosition() {
        return this.prevPosition;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public float getSize() {
        return this.size;
    }

    @Generated
    public Identifier getTexture() {
        return this.texture;
    }

    @Generated
    public int getAge() {
        return this.age;
    }

    @Generated
    public int getMaxAge() {
        return this.maxAge;
    }

    @Generated
    public double getGravityStrength() {
        return this.gravityStrength;
    }

    @Generated
    public boolean isAlive() {
        return this.alive;
    }

    @Generated
    public boolean isCollidesWithWorld() {
        return this.collidesWithWorld;
    }

    @Generated
    public Animation getAlphaAnimation() {
        return this.alphaAnimation;
    }

    @Generated
    public Animation getSizeAnimation() {
        return this.sizeAnimation;
    }

    @Generated
    public ColorRGBA getColor() {
        return this.color;
    }

    @Generated
    public void setPosition(Vec3 position) {
        this.position = position;
    }

    @Generated
    public void setPrevPosition(Vec3 prevPosition) {
        this.prevPosition = prevPosition;
    }

    @Generated
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    @Generated
    public void setSize(float size) {
        this.size = size;
    }

    @Generated
    public void setAge(int age) {
        this.age = age;
    }

    @Generated
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Generated
    public void setGravityStrength(double gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    @Generated
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Generated
    public void setCollidesWithWorld(boolean collidesWithWorld) {
        this.collidesWithWorld = collidesWithWorld;
    }
}
