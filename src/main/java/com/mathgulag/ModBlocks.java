package com.mathgulag;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.*;
import net.minecraft.block.MapColor;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static Block FLESH, FLESH_WALL, FLESH_VEIN, FLESH_SLAB, BONE, BONE_SPIKE, HEART_BLOCK;
    public static Item SPIKE_SKULL;
    static Block reg(String n, Block b) {
        Identifier id = Identifier.of("mathgulag", n);
        Registry.register(Registries.ITEM, id, new BlockItem(b, new Item.Settings()));
        return Registry.register(Registries.BLOCK, id, b);
    }
    static AbstractBlock.Settings flesh() { return AbstractBlock.Settings.create().mapColor(MapColor.RED).strength(1.2f).sounds(BlockSoundGroup.MUD); }
    public static void init() {
        FLESH = reg("flesh_block", new Block(flesh().luminance(st -> 6)));
        FLESH_WALL = reg("flesh_wall", new Block(flesh().luminance(st -> 3)));
        HEART_BLOCK = reg("heart_block", new Block(flesh().luminance(st -> 15).nonOpaque()));
        FLESH_VEIN = reg("flesh_vein", new Block(flesh().luminance(st -> 12)));
        FLESH_SLAB = reg("flesh_slab", new SlabBlock(flesh().luminance(st -> 9)));
        BONE = reg("bone_block", new Block(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).strength(2f).sounds(BlockSoundGroup.BONE)));
        BONE_SPIKE = reg("bone_spike", new BoneSpikeBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).strength(2f).nonOpaque().sounds(BlockSoundGroup.BONE)));
        SPIKE_SKULL = Registry.register(Registries.ITEM, Identifier.of("mathgulag", "spike_skull"), new Item(new Item.Settings().maxCount(1)));
        Registry.register(Registries.ITEM_GROUP, Identifier.of("mathgulag", "main"), FabricItemGroup.builder()
            .icon(() -> new ItemStack(SPIKE_SKULL)).displayName(Text.translatable("itemGroup.mathgulag"))
            .entries((ctx, e) -> { e.add(FLESH); e.add(FLESH_WALL); e.add(HEART_BLOCK); e.add(FLESH_VEIN); e.add(FLESH_SLAB); e.add(BONE); e.add(BONE_SPIKE); e.add(SPIKE_SKULL); }).build());
    }
}
