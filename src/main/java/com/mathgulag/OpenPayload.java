package com.mathgulag;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
/** S2C: abre/actualiza la pantalla. a<0 = cerrar. flash=1 -> error (verde). */
public record OpenPayload(int a, int b, int seed, String typed, int flash) implements CustomPayload {
    public static final Id<OpenPayload> ID = new Id<>(Identifier.of("mathgulag", "open"));
    public static final PacketCodec<RegistryByteBuf, OpenPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, OpenPayload::a, PacketCodecs.INTEGER, OpenPayload::b, PacketCodecs.INTEGER, OpenPayload::seed,
        PacketCodecs.STRING, OpenPayload::typed, PacketCodecs.INTEGER, OpenPayload::flash, OpenPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
