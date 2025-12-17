package io.github.doritobob269.blackholeitemmovement.item;

import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.List;

public class BlackHoleItem extends Item {
    public BlackHoleItem(Properties props) { super(props); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        if (customData.contains("TargetPos")) {
            long longPos = customData.copyTag().getLong("TargetPos");
            BlockPos target = BlockPos.of(longPos);
            tooltip.add(Component.literal("Linked to: " + target.toShortString()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Not bound to any chest").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Sneak + Right-click a black hole chest to bind").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();

        BlockPos clicked = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        if (!level.isClientSide && clickedState.is(ModRegistry.BLACK_HOLE_BLOCK.get()) && !clickedState.getValue(BlockStateProperties.ATTACHED)) {
            ItemStack stack = ctx.getItemInHand();
            var tag = new net.minecraft.nbt.CompoundTag();
            tag.putLong("TargetPos", clicked.asLong());
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
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

        // If not crouching, allow the inventory to open normally
        if (player != null && !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        // Check if the item is bound to a chest
        ItemStack stack = ctx.getItemInHand();
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        if (!customData.contains("TargetPos")) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.literal("Black hole portal must be bound to a black hole chest first!").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        // Place the black hole portal on the side of the container when crouching
        BlockPos placePos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!level.isClientSide) {
            BlockState state = ModRegistry.BLACK_HOLE_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace()).setValue(BlockStateProperties.ATTACHED, true);
            level.setBlock(placePos, state, 3);
            level.playSound(null, placePos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);

            BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof BlackHoleBlockEntity) {
                long longPos = customData.copyTag().getLong("TargetPos");
                BlockPos target = BlockPos.of(longPos);
                ((BlackHoleBlockEntity) be).setTarget(target);
            }

            if (player != null && !player.isCreative()) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.CONSUME;
    }
}
