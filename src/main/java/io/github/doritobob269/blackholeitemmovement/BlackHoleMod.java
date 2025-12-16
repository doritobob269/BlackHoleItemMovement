package io.github.doritobob269.blackholeitemmovement;

import io.github.doritobob269.blackholeitemmovement.capability.BlackHoleCapabilities;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BlackHoleMod.MODID)
public class BlackHoleMod {
    public static final String MODID = "blackholeitemmovement";

    public BlackHoleMod() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.BLOCKS.register(modBus);
        ModRegistry.ITEMS.register(modBus);
        ModRegistry.BLOCK_ENTITIES.register(modBus);
        ModRegistry.MENUS.register(modBus);
        modBus.addListener(BlackHoleCapabilities::register);
    }
}
