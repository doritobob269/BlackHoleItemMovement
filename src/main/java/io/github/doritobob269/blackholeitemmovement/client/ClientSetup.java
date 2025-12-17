package io.github.doritobob269.blackholeitemmovement.client;

import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "blackholeitemmovement", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final BlackHoleChestItemRenderer CHEST_ITEM_RENDERER = new BlackHoleChestItemRenderer();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModRegistry.BLACK_HOLE_CHEST_MENU.get(), BlackHoleChestScreen::new);
            BlockEntityRenderers.register(ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get(), BlackHoleChestRenderer::new);

            // Set render type for transparent textures
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.BLACK_HOLE_BLOCK.get(), RenderType.cutout());
        });
    }

    public static BlackHoleChestItemRenderer getChestItemRenderer() {
        return CHEST_ITEM_RENDERER;
    }
}
