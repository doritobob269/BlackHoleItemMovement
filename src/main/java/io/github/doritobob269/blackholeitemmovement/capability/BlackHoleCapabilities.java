package io.github.doritobob269.blackholeitemmovement.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import io.github.doritobob269.blackholeitemmovement.BlackHoleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = BlackHoleMod.MODID)
public class BlackHoleCapabilities {
    public static final Capability<PlayerBlackHoleInventory> PLAYER_BLACK_HOLE_INVENTORY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerBlackHoleInventory.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            PlayerBlackHoleInventoryProvider provider = new PlayerBlackHoleInventoryProvider();
            event.addCapability(new ResourceLocation(BlackHoleMod.MODID, "black_hole_inventory"), provider);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PLAYER_BLACK_HOLE_INVENTORY).ifPresent(oldInv -> {
                event.getEntity().getCapability(PLAYER_BLACK_HOLE_INVENTORY).ifPresent(newInv -> {
                    CompoundTag nbt = new CompoundTag();
                    oldInv.saveNBTData(nbt);
                    newInv.loadNBTData(nbt);
                });
            });
        }
    }

    public static class PlayerBlackHoleInventoryProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final PlayerBlackHoleInventory inventory = new PlayerBlackHoleInventory();
        private final LazyOptional<PlayerBlackHoleInventory> optional = LazyOptional.of(() -> inventory);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
            if (cap == PLAYER_BLACK_HOLE_INVENTORY) {
                return optional.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            inventory.saveNBTData(nbt);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            inventory.loadNBTData(nbt);
        }
    }
}
