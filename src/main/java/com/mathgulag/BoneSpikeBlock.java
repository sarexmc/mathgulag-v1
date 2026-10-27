package com.mathgulag;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import net.minecraft.world.BlockView;
import net.minecraft.block.ShapeContext;

public class BoneSpikeBlock extends Block {
    public static final MapCodec<BoneSpikeBlock> CODEC = createCodec(BoneSpikeBlock::new);
    public static final BooleanProperty HANGING = Properties.HANGING;
    static final VoxelShape UP = VoxelShapes.union(Block.createCuboidShape(4, 0, 4, 12, 6, 12), Block.createCuboidShape(5, 6, 5, 11, 12, 11), Block.createCuboidShape(7, 12, 7, 9, 16, 9));
    static final VoxelShape DOWN = VoxelShapes.union(Block.createCuboidShape(4, 10, 4, 12, 16, 12), Block.createCuboidShape(5, 4, 5, 11, 10, 11), Block.createCuboidShape(7, 0, 7, 9, 4, 9));
    public BoneSpikeBlock(AbstractBlock.Settings s) { super(s); setDefaultState(getDefaultState().with(HANGING, false)); }
    @Override protected MapCodec<? extends Block> getCodec() { return CODEC; }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> b) { b.add(HANGING); }
    @Override protected VoxelShape getOutlineShape(BlockState s, BlockView w, BlockPos p, ShapeContext c) { return s.get(HANGING) ? DOWN : UP; }
}
