package io.github.doritobob269.blackholeitemmovement.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;

public class PlayerBlackHoleInventory {
    private final ItemStackHandler inventory = new ItemStackHandler(27);

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.put("Inventory", inventory.serializeNBT());
    }

    public void loadNBTData(CompoundTag nbt) {
        if (nbt.contains("Inventory")) {
            inventory.deserializeNBT(nbt.getCompound("Inventory"));
        }
    }
}
