package com.mathgulag;
import net.minecraft.registry.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static SoundEvent CLICK, CORRECT, BOOM, HEARTBEAT, PASS, SKULL_ON, TICK;
    static SoundEvent reg(String n) { Identifier id = Identifier.of("mathgulag", n); return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id)); }
    public static void init() { CLICK = reg("click"); CORRECT = reg("correct"); BOOM = reg("boom"); HEARTBEAT = reg("heartbeat"); PASS = reg("pass"); SKULL_ON = reg("skull_on"); TICK = reg("tick"); }
}
