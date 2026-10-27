package com.mathgulag;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ClawSkeletonRenderer extends MobEntityRenderer<ClawSkeletonEntity, ClawSkeletonModel> {
    static final Identifier TEX = Identifier.of("mathgulag", "textures/entity/claw_skeleton.png");
    public ClawSkeletonRenderer(EntityRendererFactory.Context ctx) { super(ctx, new ClawSkeletonModel(ctx.getPart(ClawSkeletonModel.LAYER)), 0.6f); }
    @Override public Identifier getTexture(ClawSkeletonEntity e) { return TEX; }
    @Override protected void scale(ClawSkeletonEntity e, MatrixStack m, float t) { m.scale(1.3f, 1.3f, 1.3f); }
}
