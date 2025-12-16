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
    private final ItemStackHandler inventory = new ItemStackHandler(27);
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    @Nullable
    private BlockPos targetPos;

    public BlackHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("TargetPos")) {
            this.targetPos = BlockPos.of(tag.getLong("TargetPos"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        if (this.targetPos != null) {
            tag.putLong("TargetPos", this.targetPos.asLong());
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void setTarget(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getTarget() {
        return targetPos;
    }

    public ItemStackHandler getInventory() {
        return inventory;
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
            if (level.isClientSide) return;

            BlackHoleBlockEntity blackHole = (BlackHoleBlockEntity) be;
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

                boolean extractedAny = false;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack avail = source.extractItem(i, 64, true);
                    if (avail.isEmpty()) continue;

                    ItemStack remainder = avail;
                    if (receiver != null) {
                        remainder = insertToHandler(receiver, avail.copy());
                    }

                    int inserted = avail.getCount() - remainder.getCount();
                    if (inserted > 0) {
                        source.extractItem(i, inserted, false);
                        extractedAny = true;
                    } else {
                        ItemStack toStore = avail.copy();
                        ItemStack rem2 = insertToHandler(blackHole.inventory, toStore);
                        int stored = toStore.getCount() - rem2.getCount();
                        if (stored > 0) {
                            source.extractItem(i, stored, false);
                            extractedAny = true;
                        }
                    }
                }

                if (blackHole.inventory.getSlots() > 0 && blackHole.targetPos != null && receiver != null) {
                    for (int i = 0; i < blackHole.inventory.getSlots(); i++) {
                        ItemStack stack = blackHole.inventory.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        ItemStack rem = insertToHandler(receiver, stack.copy());
                        int moved = stack.getCount() - rem.getCount();
                        if (moved > 0) {
                            blackHole.inventory.extractItem(i, moved, false);
                            extractedAny = true;
                        }
                    }
                }

                boolean sourceEmpty = true;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack check = source.extractItem(i, 1, true);
                    if (!check.isEmpty()) { sourceEmpty = false; break; }
                }

                boolean bufferEmpty = true;
                for (int i = 0; i < blackHole.inventory.getSlots(); i++) if (!blackHole.inventory.getStackInSlot(i).isEmpty()) { bufferEmpty = false; break; }

                if (sourceEmpty && bufferEmpty) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                } else if (extractedAny) {
                    blackHole.setChanged();
                }
            }
        };
    }
}
