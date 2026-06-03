package moscow.rockstar.utility.render;

import org.lwjgl.opengl.GL11;

public final class GLStateSnapshot {
    private final boolean depthTest;
    private final boolean depthMask;
    private final boolean blend;
    private final boolean cull;

    private GLStateSnapshot() {
        this.depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        this.depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        this.blend = GL11.glIsEnabled(GL11.GL_BLEND);
        this.cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    }

    public static GLStateSnapshot capture() {
        return new GLStateSnapshot();
    }

    public void restore() {
        if (this.cull) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        if (this.blend) {
            GL11.glEnable(GL11.GL_BLEND);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }

        if (this.depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        GL11.glDepthMask(this.depthMask);
    }
}
