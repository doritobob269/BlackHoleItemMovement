package com.doritobob.blackhole.item;

import com.doritobob.blackhole.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlackHoleItem extends Item {
    public BlackHoleItem(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        Player player = ctx.getPlayer();

        // If clicking on an existing black hole receiver, bind this item to that receiver
        BlockPos clicked = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        if (!level.isClientSide && clickedState.is(ModRegistry.BLACK_HOLE_BLOCK.get()) && !clickedState.getValue(BlockStateProperties.ATTACHED)) {
            ItemStack stack = ctx.getItemInHand();
            stack.getOrCreateTag().putLong("TargetPos", clicked.asLong());
            if (player != null) player.displayClientMessage(new net.minecraft.network.chat.TextComponent("Bound to receiver at " + clicked.toShortString()), true);
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            // Place an attached black hole on the clicked face
            BlockPos placePos = ctx.getClickedPos().relative(ctx.getClickedFace());
            BlockState state = ModRegistry.BLACK_HOLE_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace()).setValue(BlockStateProperties.ATTACHED, true);
            level.setBlock(placePos, state, 3);
            level.playSound(null, placePos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);

            // If the item has a bound target, write it into the placed block entity
            ItemStack stack = ctx.getItemInHand();
            if (stack.hasTag() && stack.getTag().contains("TargetPos")) {
                long longPos = stack.getTag().getLong("TargetPos");
                BlockPos target = BlockPos.of(longPos);
                BlockEntity be = level.getBlockEntity(placePos);
                if (be instanceof com.doritobob.blackhole.blockentity.BlackHoleBlockEntity) {
                    ((com.doritobob.blackhole.blockentity.BlackHoleBlockEntity) be).setTarget(target);
                }
            }

            if (player != null && !player.isCreative()) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
