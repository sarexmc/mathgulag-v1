package com.mathgulag;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;

public class MathGulagClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ClawSkeletonModel.LAYER, ClawSkeletonModel::getTexturedModelData);
        EntityRendererRegistry.register(MathGulag.CLAW_SKELETON, ClawSkeletonRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(OpenPayload.ID, (pl, ctx) -> {
            MinecraftClient mc = ctx.client();
            if (pl.a() < 0) { if (mc.currentScreen instanceof MathScreen) mc.setScreen(null); return; }
            if (mc.currentScreen instanceof MathScreen s) s.update(pl); else mc.setScreen(new MathScreen(pl));
        });
    }
}
