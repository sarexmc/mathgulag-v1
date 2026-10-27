package com.mathgulag;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import java.util.*;
import static net.minecraft.server.command.CommandManager.literal;

public class MathGulag implements ModInitializer {
    public static final String ID = "mathgulag", VERSION = "2.2.0";
    public static EntityType<ClawSkeletonEntity> CLAW_SKELETON;
    static final Random RNG = new Random();
    public static final String HEART_NBT = "{Tags:[\"mg_heart\"],item:{id:\"minecraft:stick\",count:1,components:{\"minecraft:custom_model_data\":1}},item_display:\"none\",brightness:{sky:15,block:15},view_range:4f,transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[12f,12f,12f]}}";

    // ---- estado del gulag (partida unica) ----
    static final int SOLVE_TICKS = 25 * 20, PASS_TICKS = 15 * 20;
    static boolean running; static final Set<UUID> alive = new LinkedHashSet<>(), out = new HashSet<>();
    static UUID holder, label; static int phase /*0 resolver, 1 pasar*/, timer, a, b, seed, typed, nd, need, pulse;
    static MinecraftServer server;

    @Override public void onInitialize() {
        ModSounds.init(); ModBlocks.init();
        PayloadTypeRegistry.playS2C().register(OpenPayload.ID, OpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AnswerPayload.ID, AnswerPayload.CODEC);
        CLAW_SKELETON = Registry.register(Registries.ENTITY_TYPE, Identifier.of(ID, "claw_skeleton"),
            EntityType.Builder.create(ClawSkeletonEntity::new, SpawnGroup.MONSTER).dimensions(0.8f, 2.6f).build("claw_skeleton"));
        FabricDefaultAttributeRegistry.register(CLAW_SKELETON, ClawSkeletonEntity.attrs());
        Registry.register(Registries.ITEM, Identifier.of(ID, "claw_skeleton_spawn_egg"), new SpawnEggItem(CLAW_SKELETON, 0xE8E0C0, 0xAA0000, new Item.Settings()));
        ServerPlayNetworking.registerGlobalReceiver(AnswerPayload.ID, (p, ctx) -> answer(ctx.player(), p.digit()));
        ServerTickEvents.END_SERVER_TICK.register(MathGulag::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> { if (running && alive.contains(h.player.getUuid())) eliminate(h.player, false); });
        AttackEntityCallback.EVENT.register((pl, w, hand, e, hit) -> {
            if (w.isClient() || !running || phase != 1 || !(pl instanceof ServerPlayerEntity from) || !from.getUuid().equals(holder)) return ActionResult.PASS;
            if (e instanceof ServerPlayerEntity to && alive.contains(to.getUuid()) && to != from) { pass(from, to); return ActionResult.FAIL; }
            return ActionResult.PASS;
        });
        CommandRegistrationCallback.EVENT.register((d, reg, env) -> register(d));
    }

    static void register(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("mathgulag").requires(s -> s.hasPermissionLevel(2))
            .then(literal("start").executes(c -> { start(c.getSource().getServer(), c.getSource().getPlayerOrThrow()); return 1; }))
            .then(literal("stop").executes(c -> { end(c.getSource().getServer(), "Gulag detenido."); return 1; }))
            .then(literal("arena").executes(c -> { ServerPlayerEntity p = c.getSource().getPlayerOrThrow(); p.sendMessage(Text.literal("Construyendo corazon..."), true);
                ArenaBuilder.build(p.getServerWorld(), p.getBlockPos()); p.teleport(p.getServerWorld(), p.getX() - 20, p.getY() + 1, p.getZ(), p.getYaw(), p.getPitch()); return 1; }))
            .then(literal("heart").executes(c -> { ServerPlayerEntity p = c.getSource().getPlayerOrThrow(); cmdAt(p.getServerWorld(), "summon minecraft:item_display " + p.getX() + " " + (p.getY() + 12) + " " + p.getZ() + " " + HEART_NBT); return 1; }))
            .then(literal("skeleton").executes(c -> { ServerPlayerEntity p = c.getSource().getPlayerOrThrow(); cmdAt(p.getServerWorld(), "summon mathgulag:claw_skeleton " + p.getX() + " " + p.getY() + " " + p.getZ()); return 1; }))
            .then(literal("version").executes(c -> { c.getSource().sendFeedback(() -> Text.literal("MathGulag v" + VERSION), false); return 1; })));
    }
    public static void cmdAt(ServerWorld w, String c) {
        MinecraftServer s = w.getServer(); s.getCommandManager().executeWithPrefix(s.getCommandSource().withWorld(w).withLevel(4).withSilent(), c);
    }
    static ServerPlayerEntity P(UUID u) { return u == null || server == null ? null : server.getPlayerManager().getPlayer(u); }
    static void sound(ServerPlayerEntity p, SoundEvent s, float vol, float pitch) { p.getServerWorld().playSound(null, p.getBlockPos(), s, SoundCategory.PLAYERS, vol, pitch); }
    static void all(Text t) { server.getPlayerManager().broadcast(t, false); }

    // ---- flujo ----
    static void start(MinecraftServer s, ServerPlayerEntity starter) {
        end(s, null); server = s; running = true; alive.clear(); out.clear();
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) if (!p.isSpectator()) {
            alive.add(p.getUuid());
            if (ArenaBuilder.center != null && p.getServerWorld() == ArenaBuilder.world) { BlockPos sp = ArenaBuilder.randomSpawn(RNG); p.teleport(ArenaBuilder.world, sp.getX() + .5, sp.getY(), sp.getZ() + .5, p.getYaw(), 0); }
        }
        all(Text.literal("§c[Gulag] §fEmpieza. §7Jugadores: " + alive.size()));
        giveBomb(alive.stream().skip(RNG.nextInt(alive.size())).findFirst().get());
    }
    static void giveBomb(UUID u) {
        ServerPlayerEntity p = P(u); if (p == null) return;
        holder = u; phase = 0; timer = SOLVE_TICKS;
        p.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, new ItemStack(ModBlocks.SPIKE_SKULL));
        sound(p, ModSounds.SKULL_ON, 1f, 1f);
        killLabel();
        try {
            Entity e = EntityType.TEXT_DISPLAY.create(p.getServerWorld());
            e.readNbt(StringNbtReader.parse("{billboard:\"center\",Tags:[\"mg_label\"],text:'{\"text\":\"?\"}',transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0.7f,0f],scale:[1f,1f,1f]}}"));
            e.setPosition(p.getPos()); p.getServerWorld().spawnEntity(e); e.startRiding(p, true); label = e.getUuid();
        } catch (Exception ex) { ex.printStackTrace(); }
        a = 2 + RNG.nextInt(11); b = 2 + RNG.nextInt(11); typed = 0; nd = 0; seed = RNG.nextInt();
        need = String.valueOf(a * b).length(); push(p, 0);
    }
    static void killLabel() { if (label != null && server != null) { for (ServerWorld w : server.getWorlds()) { Entity e = w.getEntity(label); if (e != null) e.discard(); } label = null; } }
    static void push(ServerPlayerEntity p, int flash) {
        String t = nd == 0 ? "?" : String.valueOf(typed);
        ServerPlayNetworking.send(p, new OpenPayload(a, b, seed, t, flash));
        if (label != null) cmdAt(p.getServerWorld(), "data merge entity " + label + " {text:'{\"text\":\"" + a + " x " + b + " = " + t + "\"}'}");
    }
    static void answer(ServerPlayerEntity p, int d) {
        if (!running || phase != 0 || !p.getUuid().equals(holder) || d < 0 || d > 9) return;
        typed = typed * 10 + d; nd++;
        if (nd < need) { push(p, 0); return; }
        if (typed != a * b) { ServerPlayNetworking.send(p, new OpenPayload(-1, 0, 0, "", 0)); explode(p); return; }   // mal = explota
        phase = 1; timer = PASS_TICKS; killLabel();
        ServerPlayNetworking.send(p, new OpenPayload(a, b, seed, String.valueOf(typed), 2));                             // venas verdes y se cierra
        sound(p, ModSounds.CORRECT, 1f, 1f);
        if (alive.size() < 2) { p.sendMessage(Text.literal("§aCorrecto. §7(modo solo: nueva pregunta)"), true); giveBomb(p.getUuid()); }
    }
    static void pass(ServerPlayerEntity from, ServerPlayerEntity to) {
        from.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
        sound(from, ModSounds.PASS, 1.2f, 1f); sound(to, ModSounds.PASS, 1.2f, 0.8f);
        giveBomb(to.getUuid());
    }
    static void explode(ServerPlayerEntity p) {
        ServerWorld w = p.getServerWorld();
        w.createExplosion(null, p.getX(), p.getY() + 1, p.getZ(), 4f, World.ExplosionSourceType.NONE);
        w.playSound(null, p.getBlockPos(), ModSounds.BOOM, SoundCategory.PLAYERS, 3f, 1f);
        eliminate(p, true);
    }
    static void eliminate(ServerPlayerEntity p, boolean announce) {
        alive.remove(p.getUuid()); out.add(p.getUuid());
        p.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
        ServerPlayNetworking.send(p, new OpenPayload(-1, 0, 0, "", 0));
        if (announce) { p.changeGameMode(GameMode.SPECTATOR); all(Text.literal("§c" + p.getName().getString() + " perdió")); }
        killLabel();
        if (alive.size() == 1) { ServerPlayerEntity win = P(alive.iterator().next()); all(Text.literal("§6¡" + (win != null ? win.getName().getString() : "?") + " gana el gulag!")); end(server, null); }
        else if (alive.isEmpty()) end(server, null);
        else if (running) giveBomb(alive.stream().skip(RNG.nextInt(alive.size())).findFirst().get());
    }
    static void end(MinecraftServer s, String msg) {
        if (!running) { if (msg != null && s != null) s.getPlayerManager().broadcast(Text.literal(msg), false); return; }
        running = false; killLabel();
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            p.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
            ServerPlayNetworking.send(p, new OpenPayload(-1, 0, 0, "", 0));
            if (out.contains(p.getUuid())) p.changeGameMode(GameMode.SURVIVAL);
        }
        alive.clear(); out.clear(); holder = null;
        if (msg != null) s.getPlayerManager().broadcast(Text.literal(msg), false);
    }

    static String beat(double sc) { return "execute as @e[type=minecraft:item_display,tag=mg_heart] run data merge entity @s {start_interpolation:0,interpolation_duration:" + (sc > 12 ? 3 : 6) + ",transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[" + sc + "f," + sc + "f," + sc + "f]}}"; }
    static void tick(MinecraftServer s) {
        server = s; pulse = (pulse + 1) % 40;
        if (ArenaBuilder.world != null) switch (pulse) {
            case 0 -> { cmdAt(ArenaBuilder.world, beat(13.6)); heartSound(1f); }
            case 4, 14 -> cmdAt(ArenaBuilder.world, beat(12.0));
            case 10 -> { cmdAt(ArenaBuilder.world, beat(12.9)); heartSound(0.8f); }
            default -> {}
        }
        if (!running) return;
        ServerPlayerEntity h = P(holder); if (h == null) return;
        if (h.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).getItem() != ModBlocks.SPIKE_SKULL) h.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, new ItemStack(ModBlocks.SPIKE_SKULL));
        if (alive.size() >= 2 || phase == 0) timer--;
        if (timer % 20 == 0) {
            int sec = timer / 20;
            h.sendMessage(Text.literal(phase == 0 ? "§cResuelve: §f" + sec + "s" : "§e¡Golpea a otro jugador para pasar la bomba! §f" + sec + "s"), true);
            if (sec <= 5) sound(h, ModSounds.TICK, 1f, 1f + (5 - sec) * 0.1f);
        }
        if (timer <= 0) { ServerPlayNetworking.send(h, new OpenPayload(-1, 0, 0, "", 0)); explode(h); }
    }
    static void heartSound(float v) {
        ServerWorld w = ArenaBuilder.world; BlockPos c = ArenaBuilder.center;
        w.playSound(null, c.getX(), c.getY() + 20, c.getZ(), ModSounds.HEARTBEAT, SoundCategory.AMBIENT, 6f * v, 1f);
    }
}
