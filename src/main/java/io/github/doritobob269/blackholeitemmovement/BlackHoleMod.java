package io.github.doritobob269.blackholeitemmovement;

import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BlackHoleMod.MODID)
public class BlackHoleMod {
    public static final String MODID = "blackholeitemmovement";

    public BlackHoleMod() {
        ModRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRegistry.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
