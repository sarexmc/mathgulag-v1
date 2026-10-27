package com.mathgulag;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
/** C2S: digito pulsado (0-9). */
public record AnswerPayload(int digit) implements CustomPayload {
    public static final Id<AnswerPayload> ID = new Id<>(Identifier.of("mathgulag", "answer"));
    public static final PacketCodec<RegistryByteBuf, AnswerPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, AnswerPayload::digit, AnswerPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
