package io.github.doritobob269.blackholeitemmovement.menu;

import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BlackHoleChestMenu extends AbstractContainerMenu {
    private final BlackHoleBlockEntity blockEntity;
    private final net.neoforged.neoforge.items.ItemStackHandler chestInventory;
    private final ContainerLevelAccess access;

    // Client/Server constructor
    public BlackHoleChestMenu(int windowId, Inventory playerInv) {
        this(windowId, playerInv, null, ContainerLevelAccess.NULL);
    }

    // Server constructor
    public BlackHoleChestMenu(int windowId, Inventory playerInv, BlockPos pos, net.minecraft.world.level.Level level) {
        this(windowId, playerInv,
            level.getBlockEntity(pos) instanceof BlackHoleBlockEntity ? (BlackHoleBlockEntity) level.getBlockEntity(pos) : null,
            ContainerLevelAccess.create(level, pos));
    }

    private BlackHoleChestMenu(int windowId, Inventory playerInv, BlackHoleBlockEntity blockEntity, ContainerLevelAccess access) {
        super(ModRegistry.BLACK_HOLE_CHEST_MENU.get(), windowId);
        this.blockEntity = blockEntity;
        this.access = access;

        // Open chest - this works on both client and server
        if (this.blockEntity != null) {
            this.blockEntity.startOpen(playerInv.player);
        }

        // Get this chest's inventory
        this.chestInventory = this.blockEntity != null ? this.blockEntity.getInventory() : new net.neoforged.neoforge.items.ItemStackHandler(27);

        // Add black hole chest inventory slots (3 rows of 9 = 27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(chestInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < 27) {
                // Moving from chest to player inventory
                if (!this.moveItemStackTo(stackInSlot, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to chest
                if (!this.moveItemStackTo(stackInSlot, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModRegistry.BLACK_HOLE_BLOCK.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.blockEntity != null) {
            this.blockEntity.stopOpen(player);
        }
        access.execute((level, pos) -> {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
        });
    }
}
