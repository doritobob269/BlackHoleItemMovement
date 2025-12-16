package io.github.doritobob269.blackholeitemmovement.blockentity;

import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlackHoleBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos targetPos;
    @Nullable
    private java.util.UUID ownerUUID;

    // Chest animation fields
    private int openCount;
    private float openness;
    private float oOpenness;

    public BlackHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), pos, state);
    }

    public void startOpen() {
        this.openCount++;
    }

    public void stopOpen() {
        this.openCount--;
    }

    public float getOpenNess(float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, this.oOpenness, this.openness);
    }

    public void updateOpenness() {
        this.oOpenness = this.openness;
        if (this.openCount > 0) {
            this.openness = Math.min(1.0f, this.openness + 0.1f);
        } else {
            this.openness = Math.max(0.0f, this.openness - 0.1f);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TargetPos")) {
            this.targetPos = BlockPos.of(tag.getLong("TargetPos"));
        }
        if (tag.contains("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.targetPos != null) {
            tag.putLong("TargetPos", this.targetPos.asLong());
        }
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    public void setTarget(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getTarget() {
        return targetPos;
    }

    public void setOwner(java.util.UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    @Nullable
    public java.util.UUID getOwnerUUID() {
        return ownerUUID;
    }

    private static ItemStack insertToHandler(IItemHandler dest, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < dest.getSlots(); i++) {
            if (remaining.isEmpty()) break;
            remaining = dest.insertItem(i, remaining, false);
        }
        return remaining;
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(BlockEntityType<T> type) {
        return (level, pos, state, be) -> {
            if (!(be instanceof BlackHoleBlockEntity)) return;

            BlackHoleBlockEntity blackHole = (BlackHoleBlockEntity) be;

            // Update chest animation on both client and server
            blackHole.updateOpenness();

            if (level.isClientSide) return;
            BlockState bs = state;
            boolean attached = bs.hasProperty(BlackHoleBlock.ATTACHED) && bs.getValue(BlackHoleBlock.ATTACHED);
            Direction facing = bs.hasProperty(BlackHoleBlock.FACING) ? bs.getValue(BlackHoleBlock.FACING) : Direction.NORTH;

            if (attached) {
                BlockPos sourcePos = pos.relative(facing.getOpposite());
                BlockEntity sourceBE = level.getBlockEntity(sourcePos);
                if (sourceBE == null) return;

                LazyOptional<IItemHandler> srcCap = sourceBE.getCapability(ForgeCapabilities.ITEM_HANDLER, facing);
                if (!srcCap.isPresent()) {
                    srcCap = sourceBE.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                }
                if (!srcCap.isPresent()) return;

                IItemHandler source = srcCap.orElse(null);

                BlockPos target = blackHole.getTarget();
                IItemHandler receiver = null;
                if (target != null) {
                    BlockEntity targetBE = level.getBlockEntity(target);
                    if (targetBE != null) {
                        LazyOptional<IItemHandler> recvCap = targetBE.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                        if (recvCap.isPresent()) receiver = recvCap.orElse(null);
                    }
                }

                // Get owner's global inventory
                java.util.UUID ownerUUID = blackHole.getOwnerUUID();
                IItemHandler globalInventory = null;
                if (ownerUUID != null && level.getServer() != null) {
                    net.minecraft.server.level.ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUUID);
                    if (player != null) {
                        globalInventory = player.getCapability(io.github.doritobob269.blackholeitemmovement.capability.BlackHoleCapabilities.PLAYER_BLACK_HOLE_INVENTORY)
                            .map(inv -> inv.getInventory()).orElse(null);
                    }
                }

                // Try to pull items from source slots
                boolean extractedAny = false;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack avail = source.extractItem(i, 64, true);
                    if (avail.isEmpty()) continue;

                    ItemStack remainder = avail;
                    if (globalInventory != null) {
                        remainder = insertToHandler(globalInventory, avail.copy());
                    }

                    int inserted = avail.getCount() - remainder.getCount();
                    if (inserted > 0) {
                        source.extractItem(i, inserted, false);
                        extractedAny = true;
                    }
                }

                // Check if source has any extractable items left
                boolean sourceEmpty = true;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack check = source.extractItem(i, 1, true);
                    if (!check.isEmpty()) { sourceEmpty = false; break; }
                }

                if (sourceEmpty) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                } else if (extractedAny) {
                    blackHole.setChanged();
                }
            }
        };
    }
}
