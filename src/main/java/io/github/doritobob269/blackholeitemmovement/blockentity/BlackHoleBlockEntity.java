package io.github.doritobob269.blackholeitemmovement.blockentity;

import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlackHoleBlockEntity extends BlockEntity implements MenuProvider {
    @Nullable
    private BlockPos targetPos;
    @Nullable
    private net.minecraft.resources.ResourceKey<Level> targetDimension;

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

    // Portal lifetime timer - portal will close after 100 ticks (5 seconds)
    private int portalLifetime = 0;

    public BlackHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void startOpen(Player player) {
        if (this.level == null) return;

        // Increment on both sides
        this.openCount++;

        // Sync to client via block event (server only)
        if (!this.level.isClientSide) {
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
        }
    }

    public void stopOpen(Player player) {
        if (this.level == null) return;

        // Decrement on both sides
        this.openCount--;

        // Sync to client via block event (server only)
        if (!this.level.isClientSide) {
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.openCount = type;
            return true;
        }
        return super.triggerEvent(id, type);
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
        if (tag.contains("TargetDimension")) {
            this.targetDimension = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.parse(tag.getString("TargetDimension"))
            );
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.targetPos != null) {
            tag.putLong("TargetPos", this.targetPos.asLong());
        }
        if (this.targetDimension != null) {
            tag.putString("TargetDimension", this.targetDimension.location().toString());
        }
        tag.put("Inventory", inventory.serializeNBT(provider));
    }

    public void setTarget(BlockPos pos, net.minecraft.resources.ResourceKey<Level> dimension) {
        this.targetPos = pos;
        this.targetDimension = dimension;
        setChanged();
    }

    @Nullable
    public BlockPos getTarget() {
        return targetPos;
    }

    @Nullable
    public net.minecraft.resources.ResourceKey<Level> getTargetDimension() {
        return targetDimension;
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

                IItemHandler source = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, sourcePos, facing);
                if (source == null) {
                    source = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, sourcePos, null);
                }
                if (source == null) return;

                // Get target chest's inventory (cross-dimensional support)
                BlockPos target = blackHole.getTarget();
                net.minecraft.resources.ResourceKey<Level> targetDimension = blackHole.getTargetDimension();
                IItemHandler targetInventory = null;

                if (target != null && targetDimension != null) {
                    // Get the target level (dimension)
                    net.minecraft.server.MinecraftServer server = level.getServer();
                    if (server != null) {
                        net.minecraft.server.level.ServerLevel targetLevel = server.getLevel(targetDimension);
                        if (targetLevel != null) {
                            // Force load the chunk at the target position
                            targetLevel.getChunkAt(target);

                            BlockEntity targetBE = targetLevel.getBlockEntity(target);
                            if (targetBE instanceof BlackHoleBlockEntity) {
                                targetInventory = ((BlackHoleBlockEntity) targetBE).getInventory();
                            }
                        }
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

                // Increment portal lifetime
                blackHole.portalLifetime++;

                // Remove portal after 5 seconds (100 ticks) from placement
                if (blackHole.portalLifetime >= 100) {
                    level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8f, 1.5f);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    return;
                }

                // Mark as changed if items were extracted
                if (extractedAny) {
                    blackHole.setChanged();
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
