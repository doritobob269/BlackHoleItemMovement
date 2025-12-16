package io.github.doritobob269.blackholeitemmovement.item;

import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlackHoleItem extends Item {
    public BlackHoleItem(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();

        BlockPos clicked = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        if (!level.isClientSide && clickedState.is(ModRegistry.BLACK_HOLE_BLOCK.get()) && !clickedState.getValue(BlockStateProperties.ATTACHED)) {
            ItemStack stack = ctx.getItemInHand();
            stack.getOrCreateTag().putLong("TargetPos", clicked.asLong());
            if (player != null) player.displayClientMessage(Component.literal("Bound to receiver at " + clicked.toShortString()), true);
            return InteractionResult.SUCCESS;
        }

        // Check if the clicked block has an inventory
        BlockEntity clickedBE = level.getBlockEntity(clicked);
        if (clickedBE == null) {
            return InteractionResult.FAIL;
        }

        boolean hasInventory = clickedBE.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, ctx.getClickedFace()).isPresent();
        if (!hasInventory) {
            hasInventory = clickedBE.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, null).isPresent();
        }

        if (!hasInventory) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.literal("This block has no inventory!"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockPos placePos = ctx.getClickedPos().relative(ctx.getClickedFace());
            BlockState state = ModRegistry.BLACK_HOLE_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace()).setValue(BlockStateProperties.ATTACHED, true);
            level.setBlock(placePos, state, 3);
            level.playSound(null, placePos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);

            ItemStack stack = ctx.getItemInHand();
            BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof BlackHoleBlockEntity && player != null) {
                ((BlackHoleBlockEntity) be).setOwner(player.getUUID());
                if (stack.hasTag() && stack.getTag().contains("TargetPos")) {
                    long longPos = stack.getTag().getLong("TargetPos");
                    BlockPos target = BlockPos.of(longPos);
                    ((BlackHoleBlockEntity) be).setTarget(target);
                }
            }

            if (player != null && !player.isCreative()) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
