package com.mathgulag;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ClawSkeletonModel extends EntityModel<ClawSkeletonEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Identifier.of("mathgulag", "claw_skeleton"), "main");
    private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;

    public ClawSkeletonModel(ModelPart root) {
        this.root = root; head = root.getChild("head");
        rightArm = root.getChild("right_arm"); leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg"); leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData(); ModelPartData r = md.getRoot();
        ModelPartData h = r.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8), ModelTransform.pivot(0, 0, 0));
        h.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-5, -10, -5, 10, 2, 10), ModelTransform.NONE);
        h.addChild("hat_top", ModelPartBuilder.create().uv(0, 32).cuboid(-3, -12, -3, 6, 2, 6), ModelTransform.NONE);
        ModelPartData body = r.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4, 0, -2, 8, 12, 4)
            .uv(32, 0).cuboid(-5, 2, -3, 10, 1, 1).cuboid(-5, 5, -3, 10, 1, 1).cuboid(-5, 8, -3, 10, 1, 1), ModelTransform.pivot(0, 0, 0));
        for (int i = 0; i < 3; i++) {
            float f = i - 1;
            body.addChild("spike_l" + i, ModelPartBuilder.create().uv(0, 48).cuboid(-1, 0, 0, 2, 10, 2), ModelTransform.of(3 + i, 1 + i, 2, 0.9f - 0.2f * i, 0, 0.5f + 0.3f * f));
            body.addChild("spike_r" + i, ModelPartBuilder.create().uv(0, 48).cuboid(-1, 0, 0, 2, 10, 2), ModelTransform.of(-3 - i, 1 + i, 2, 0.9f - 0.2f * i, 0, -0.5f - 0.3f * f));
        }
        for (int side = 0; side < 2; side++) {
            String n = side == 0 ? "right" : "left"; float x = side == 0 ? -5 : 5;
            ModelPartData arm = r.addChild(n + "_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-1, -2, -1, 2, 14, 2), ModelTransform.pivot(x, 2, 0));
            for (int c = 0; c < 3; c++)
                arm.addChild("claw" + c, ModelPartBuilder.create().uv(56, 0).cuboid(-0.5f, 0, -0.5f, 1, 7, 1), ModelTransform.of(-0.6f + 0.6f * c, 12, 0, 0.25f, 0, (c - 1) * 0.35f));
            r.addChild(n + "_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-1, 0, -1, 2, 12, 2), ModelTransform.pivot(side == 0 ? -2 : 2, 12, 0));
        }
        return TexturedModelData.of(md, 64, 64);
    }

    @Override public void setAngles(ClawSkeletonEntity e, float limbAngle, float limbDist, float anim, float headYaw, float headPitch) {
        head.yaw = headYaw * 0.017453292F; head.pitch = headPitch * 0.017453292F;
        if (e.isAttacking()) { rightArm.pitch = leftArm.pitch = -1.6f + MathHelper.sin(anim * 0.4f) * 0.15f; }
        else { rightArm.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.2f * limbDist; leftArm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.2f * limbDist; }
        rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4f * limbDist;
        leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4f * limbDist;
    }
    @Override public void render(MatrixStack m, VertexConsumer v, int light, int overlay, int color) { root.render(m, v, light, overlay, color); }
}
