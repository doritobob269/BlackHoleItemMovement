package io.github.doritobob269.blackholeitemmovement;

import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BlackHoleMod.MODID)
public class BlackHoleMod {
    public static final String MODID = "blackholeitemmovement";

    public BlackHoleMod(IEventBus modBus) {
        ModRegistry.BLOCKS.register(modBus);
        ModRegistry.ITEMS.register(modBus);
        ModRegistry.BLOCK_ENTITIES.register(modBus);
        ModRegistry.MENUS.register(modBus);
        ModRegistry.CREATIVE_TABS.register(modBus);
    }
}
