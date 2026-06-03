package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import com.mojang.blaze3d.vertex.BufferBuilder;
// import net.minecraft.client.renderer.BufferRenderer;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@ModuleInfo(name = "Animations", category = ModuleCategory.VISUALS, desc = "Эмоции и анимации персонажа")
public class Animations extends BaseModule implements IMinecraft {

    private final BindSetting wheelKey = new BindSetting(this, "Кнопка колеса");

    // ── Wheel state ───────────────────────────────────────────────────────────
    private int selectedSlot = -1;

    // ── Playing ───────────────────────────────────────────────────────────────
    private boolean playing   = false;
    private long    playStart = 0L;
    private static final long ANIM_DURATION = 3600L;

    // ── Slot config ───────────────────────────────────────────────────────────
    private static final boolean[] SLOT_AVAIL = { true, false, false, false, false, false, false, false };

    // ──────────────────────────────────────────────────────────────────────────
    // Events
    // ──────────────────────────────────────────────────────────────────────────

    private final EventListener<KeyPressEvent> onKey = event -> {
        if (!this.wheelKey.isKey(event.getKey())) return;
        if (event.getAction() == 1 && mc.screen == null) {
            this.playAnimation();
        }
    };

    private final EventListener<MouseEvent> onMouse = event -> {
        if (!this.wheelKey.isKey(event.getButton())) return;
        if (event.getAction() == 1 && mc.screen == null) {
            this.playAnimation();
        }
    };

    private void playAnimation() {
        this.selectedSlot = 0;
        this.playing = true;
        this.playStart = System.currentTimeMillis();
    }

    /**
     * GameTickEvent: обновляем таймер анимации.
     */
    private final EventListener<GameTickEvent> onTick = event -> {
        if (this.playing) {
            long elapsed = System.currentTimeMillis() - this.playStart;
            if (elapsed >= ANIM_DURATION) {
                this.playing = false;
            }
        }
    };

    /**
     * HandRenderEvent — для вида от 1-го лица.
     * ВАЖНО: НЕ используем push/pop, чтобы трансформы остались в стеке.
     * Это заставит рендерер применить нашу позицию к руке.
     */
    private final EventListener<HandRenderEvent> onHand = event -> {
        if (!this.playing || this.selectedSlot != 0) return;

        long elapsed = System.currentTimeMillis() - this.playStart;
        float t = clamp01((float) elapsed / ANIM_DURATION);

        // Применяем анимацию к обеим рукам без push/pop
        this.applyAnim67(event.pose(), t, event.getArm());
        event.cancel();
    };

    /**
     * Render3DEvent — для вида от 3-го лица (F5).
     * Рисуем светящиеся "руки" как billboard-спрайты в мировом пространстве
     * относительно позиции игрока.
     */
    private final EventListener<Render3DEvent> on3D = event -> {
        if (!this.playing || this.selectedSlot != 0) return;
        // Не показываем 3D-руки от 1-го лица
        if (mc.options.getCameraType().isFirstPerson()) return;
        if (mc.player == null) return;

        long elapsed = System.currentTimeMillis() - this.playStart;
        float t = clamp01((float) elapsed / ANIM_DURATION);

        this.render3DHands(event.pose(), event.getGameTimeDeltaPartialTick(), t,
                mc.gameRenderer.getMainCamera());
    };

    // ──────────────────────────────────────────────────────────────────────────
    // 3D InteractionHand rendering (третье лицо / F5)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Рисуем два светящихся квадрата, имитирующих поднятые руки.
     * Позиция рассчитывается относительно тела игрока.
     */
    private void render3DHands(PoseStack ms, float tickDelta, float t, Camera camera) {
        float raise = ease(clamp01(t / 0.18f));
        float lower = ease(clamp01((t - 0.75f) / 0.25f));
        float upAmt = raise * (1f - lower);

        float holdT = clamp01((t - 0.18f) / 0.57f);
        float sway  = (float) Math.sin(holdT * Math.PI * 4f) * 0.1f * upAmt;
        float alpha = 0.7f + 0.3f * upAmt;

        Vec3 playerPos = mc.player.getPosition(tickDelta)
                .add(0, mc.player.getEyeHeight(mc.player.getPose()) * 0.7, 0);

        float yaw = (float) Math.toRadians(
                mc.player.yBodyRotO + (mc.player.yBodyRot - mc.player.yBodyRotO) * tickDelta);

        // Смещения рук в локальных координатах тела
        // Right hand: +X, Left hand: -X, обе подняты на upAmt * 0.9
        double[][][] hands = {
            // { { localX, localY, localZ }, rightHand }
            {{ 0.45 + sway, 0.4 * upAmt - 0.1, -0.1 }},  // right
            {{-0.45 - sway, 0.4 * upAmt - 0.1, -0.1 }},  // left
        };

        // Commented out legacy OpenGL state changes
//         RenderSystem.blendFunc(
//             com.mojang.blaze3d.platform.GlStateManager.SrcFactor.SRC_ALPHA,
//             com.mojang.blaze3d.platform.GlStateManager.DstFactor.ONE);
//         RenderSystem.enableDepthTest();
//         RenderSystem.disableCull();
//         RenderSystem.depthMask(false);
        Identifier bloomTex = Rockstar.id("textures/bloom.png");
//         RenderSystem.setShaderTexture(0, bloomTex);
//         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        for (double[][] hand : hands) {
            double lx = hand[0][0];
            double ly = hand[0][1];
            double lz = hand[0][2];

            // Вращаем локальное смещение по yaw игрока
            double worldX = lx * Math.cos(yaw) - lz * Math.sin(yaw);
            double worldZ = lx * Math.sin(yaw) + lz * Math.cos(yaw);

            Vec3 handPos = playerPos.add(worldX, ly, worldZ);

            ms.pushPose();
            RenderUtility.prepareMatrices(ms, handPos);
            ms.mulPose(camera.rotation()); // billboard

            float size = 0.35f * upAmt;
            ColorRGBA col = new ColorRGBA(180f, 200f, 255f, 255f * alpha * upAmt);

//             BufferBuilder bb = RenderSystem.renderThreadTesselator()
//                     .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//             Matrix4f mat = ms.peek().getPositionMatrix();
//             bb.vertex(mat, -size, -size, 0f).setUv(0f, 0f).setColor(col.getRGB());
//             bb.vertex(mat, -size,  size, 0f).setUv(0f, 1f).setColor(col.getRGB());
//             bb.vertex(mat,  size,  size, 0f).setUv(1f, 1f).setColor(col.getRGB());
//             bb.vertex(mat,  size, -size, 0f).setUv(1f, 0f).setColor(col.getRGB());
//             MeshData b = bb.build();
//             if (b != null) BufferRenderer.drawWithGlobalProgram(b);

            ms.popPose();
        }

//         RenderSystem.depthMask(true);
//         RenderSystem.setShaderTexture(0, 0);
//         RenderSystem.disableBlend();
//         RenderSystem.enableCull();
//         RenderSystem.disableDepthTest();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Анимация "67" — первое лицо (HandRenderEvent)
    //
    //  НЕ используем push/pop — трансформы должны остаться!
    //  Фазы: 0..0.18 → подъём | 0.18..0.75 → hold+sway | 0.75..1 → опускание
    // ──────────────────────────────────────────────────────────────────────────

    private void applyAnim67(PoseStack matrices, float t, HumanoidArm arm) {
        boolean right = (arm == HumanoidArm.RIGHT);

        float raise = ease(clamp01(t / 0.18f));
        float lower = ease(clamp01((t - 0.75f) / 0.25f));
        float upAmt = raise * (1f - lower);

        float holdT = clamp01((t - 0.18f) / 0.57f);
        float sway  = (float) Math.sin(holdT * Math.PI * 4f) * 0.06f * upAmt;
        float pulse = (float) Math.sin(holdT * Math.PI * 8f) * 0.02f  * upAmt;

        float yShift  = -upAmt * 0.40f;
        float zShift  = -upAmt * 0.18f;
        float xSpread = right ?  upAmt * 0.12f : -upAmt * 0.12f;
        float xSway   = right ?  sway           : -sway;

        // БЕЗ push/pop — трансформы остаются в стеке
        matrices.translate(xSpread + xSway, yShift + pulse, zShift);
        matrices.mulPose(Axis.XP.rotationDegrees(upAmt * 28f));
        matrices.mulPose(Axis.YP.rotationDegrees(right ? -upAmt * 12f : upAmt * 12f));
        matrices.mulPose(Axis.ZP.rotationDegrees(right ? -upAmt *  8f : upAmt *  8f));
    }    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onDisable() {
        this.playing = false;
    }

    private static float ease(float t) {
        return t < 0.5f ? 2f * t * t : -1f + (4f - 2f * t) * t;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
