package com.mathgulag;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;

public class MathScreen extends Screen {
    static final Identifier SMOKE = Identifier.of("mathgulag", "textures/gui/smoke.png");
    static final Identifier VEINS = Identifier.of("mathgulag", "textures/gui/veins.png");
    int a, b, seed, flash, tick, doneAt = -1; String typed = "?"; boolean built = false; float lastT;
    final float[] bx = new float[10], by = new float[10], ph = new float[10], am = new float[10], sp = new float[10];

    public MathScreen(OpenPayload p) { super(Text.literal("MathGulag")); apply(p); }
    void apply(OpenPayload p) {
        a = p.a(); b = p.b(); flash = p.flash(); typed = p.typed();
        if (flash == 2) { doneAt = tick + 24; }
        if (p.seed() != seed || !built) { seed = p.seed(); built = false; }
    }
    public void update(OpenPayload p) { apply(p); layout(); }
    @Override protected void init() { layout(); }

    void layout() {
        if (width == 0 || built) return;
        Random r = new Random(seed); int placed = 0, guard = 0; List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 10; i++) order.add(i); Collections.shuffle(order, r);
        while (placed < 10 && guard++ < 4000) {
            float x = 70 + r.nextFloat() * (width - 140), y = 70 + r.nextFloat() * (height - 140);
            if (x > width * 0.22 && x < width * 0.78 && y < 215) continue;           // no tapar la multiplicacion
            boolean ok = true; for (int k = 0; k < placed; k++) if (Math.hypot(bx[order.get(k)] - x, by[order.get(k)] - y) < 105) { ok = false; break; }
            if (!ok) continue; int d = order.get(placed++); bx[d] = x; by[d] = y;
        }
        for (int i = 0; i < 10; i++) { ph[i] = r.nextFloat() * 6.28f; am[i] = 25 + r.nextFloat() * 40; sp[i] = 0.05f + r.nextFloat() * 0.06f; }
        built = true;
    }
    float cx(int i, float t) { return bx[i] + (float) Math.sin(t * sp[i] + ph[i]) * am[i]; }          // se mueven de lado a lado
    float cy(int i, float t) { return by[i] + (float) Math.cos(t * sp[i] * 0.7 + ph[i]) * 7; }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { }
    @Override public void tick() { tick++; if (doneAt >= 0 && tick >= doneAt) client.setScreen(null); }

    void glyph(DrawContext c, String name, float x, float y, int w, int h, int tw, int th) {
        c.drawTexture(Identifier.of("mathgulag", "textures/font/" + name + ".png"), (int) x, (int) y, w, h, 0, 0, tw, th, tw, th);
    }
    void bigText(DrawContext c, String s, int cxm, int y) {
        int adv = 62, w = s.length() * adv, x = cxm - w / 2;
        for (char ch : s.toCharArray()) { String n = ch == 'x' ? "nx" : ch == '?' ? "nq" : ch == ' ' ? null : "n" + ch; if (n != null) glyph(c, n, x, y, 62, 83, 192, 256); x += adv; }
    }
    static void play(String n, float pitch) { MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(Identifier.of("mathgulag", n)), pitch, 1f)); }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        if (!built) layout();
        float t = tick + delta; lastT = t;
        int ph2 = (int) (t % 40);
        double pul = Math.exp(-Math.pow(Math.min(ph2, 40 - ph2) / 2.0, 2)) + 0.75 * Math.exp(-Math.pow((ph2 - 10) / 2.0, 2));   // lub-dub, igual que el corazon
        float scale = 1.06f + 0.045f * (float) pul, alpha = (float) Math.min(1, 0.55 + 0.45 * pul);
        boolean green = flash == 2;
        RenderSystem.enableBlend();
        if (flash != 2) c.fill(0, 0, width, height, 0x3AE8102A);   // tinte rojo brillante como el original
        c.getMatrices().push();
        c.getMatrices().translate(width / 2f, height / 2f, 0); c.getMatrices().scale(scale, scale, 1); c.getMatrices().translate(-width / 2f, -height / 2f, 0);
        if (green) c.setShaderColor(0.25f, 1f, 0.35f, 1f); else c.setShaderColor(1f, 0.3f, 0.35f, alpha);
        c.drawTexture(VEINS, 0, 0, width, height, 0, 0, 1024, 1024, 1024, 1024);
        c.setShaderColor(1, 1, 1, 1); c.getMatrices().pop();
        if (green) { RenderSystem.disableBlend(); return; }                                                  // al acertar: solo venas verdes y se quita todo
        c.setShaderColor(1, 1, 1, 0.9f); c.drawTexture(SMOKE, width / 2 - 250, -30, 500, 250, 0, 0, 512, 256, 512, 256); c.setShaderColor(1, 1, 1, 1);   // nube oscura suave
        bigText(c, a + " x " + b, width / 2, 8);
        bigText(c, typed, width / 2, 96);
        for (int i = 0; i < 10; i++) {
            float x = cx(i, t), y = cy(i, t); boolean hov = Math.hypot(mx - x, my - y) < 44;
            int sz = (int) (92 + Math.sin(t * 0.3 + i) * 4 + (hov ? 14 : 0));
            glyph(c, "b" + i, x - sz / 2f, y - sz / 2f, sz, sz, 192, 192);
        }
        RenderSystem.disableBlend();
    }
    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (flash == 2) return true;
        int need = String.valueOf(a * b).length();
        for (int i = 0; i < 10; i++) if (Math.hypot(mx - cx(i, lastT), my - cy(i, lastT)) < 44) {
            String cur = typed.equals("?") ? "" : typed;
            if (cur.length() < need) { typed = cur + i; play("click", 0.9f + i * 0.03f); ClientPlayNetworking.send(new AnswerPayload(i)); }   // el numero aparece debajo al instante
            return true;
        }
        return true;
    }
}
