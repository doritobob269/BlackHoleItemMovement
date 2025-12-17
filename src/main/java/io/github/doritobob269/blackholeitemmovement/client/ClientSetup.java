package io.github.doritobob269.blackholeitemmovement.client;

import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "blackholeitemmovement", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final BlackHoleChestItemRenderer CHEST_ITEM_RENDERER = new BlackHoleChestItemRenderer();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), BlackHoleChestRenderer::new);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModRegistry.BLACK_HOLE_BLOCK.get(), RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistry.BLACK_HOLE_CHEST_MENU.get(), BlackHoleChestScreen::new);
    }

    public static BlackHoleChestItemRenderer getChestItemRenderer() {
        return CHEST_ITEM_RENDERER;
    }
}
