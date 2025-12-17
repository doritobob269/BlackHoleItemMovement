package io.github.doritobob269.blackholeitemmovement.blockentity;

import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

public class BlackHoleBlockEntity extends BlockEntity implements MenuProvider {
    @Nullable
    private BlockPos targetPos;

    // Chest inventory (27 slots)
    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // Chest animation fields
    private int openCount;
    private float openness;
    private float oOpenness;

    // Portal inactivity timer (in ticks, 100 ticks = 5 seconds)
    private int ticksSinceLastExtraction = 0;

    public BlackHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    private LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventoryCapability = LazyOptional.of(() -> inventory);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // Expose inventory on all sides for chest blocks (not portals)
        if (!this.getBlockState().getValue(BlackHoleBlock.ATTACHED) && cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCapability.cast();
        }
        return super.getCapability(cap, side);
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
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("TargetPos")) {
            this.targetPos = BlockPos.of(tag.getLong("TargetPos"));
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
        if (tag.contains("TicksSinceLastExtraction")) {
            this.ticksSinceLastExtraction = tag.getInt("TicksSinceLastExtraction");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.targetPos != null) {
            tag.putLong("TargetPos", this.targetPos.asLong());
        }
        tag.put("Inventory", inventory.serializeNBT(provider));
        tag.putInt("TicksSinceLastExtraction", this.ticksSinceLastExtraction);
    }

    public void setTarget(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getTarget() {
        return targetPos;
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

                // Get target chest's inventory
                BlockPos target = blackHole.getTarget();
                IItemHandler targetInventory = null;
                if (target != null) {
                    BlockEntity targetBE = level.getBlockEntity(target);
                    if (targetBE instanceof BlackHoleBlockEntity) {
                        targetInventory = ((BlackHoleBlockEntity) targetBE).getInventory();
                    }
                }

                // Try to pull items from source slots
                boolean extractedAny = false;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack avail = source.extractItem(i, 64, true);
                    if (avail.isEmpty()) continue;

                    ItemStack remainder = avail;
                    if (targetInventory != null) {
                        remainder = insertToHandler(targetInventory, avail.copy());
                    }

                    int inserted = avail.getCount() - remainder.getCount();
                    if (inserted > 0) {
                        source.extractItem(i, inserted, false);
                        extractedAny = true;
                    }
                }

                // Reset timer if items were extracted
                if (extractedAny) {
                    blackHole.ticksSinceLastExtraction = 0;
                    blackHole.setChanged();
                } else {
                    // Increment timer if no items were moved
                    blackHole.ticksSinceLastExtraction++;

                    // Remove portal after 5 seconds (100 ticks) of inactivity
                    if (blackHole.ticksSinceLastExtraction >= 100) {
                        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8f, 1.5f);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        return;
                    }
                }

                // Check if source has any extractable items left
                boolean sourceEmpty = true;
                for (int i = 0; i < source.getSlots(); i++) {
                    ItemStack check = source.extractItem(i, 1, true);
                    if (!check.isEmpty()) { sourceEmpty = false; break; }
                }

                if (sourceEmpty) {
                    level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8f, 1.5f);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        };
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.literal("Black Hole Chest");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu(windowId, playerInventory, this.worldPosition, player.level());
    }
}
