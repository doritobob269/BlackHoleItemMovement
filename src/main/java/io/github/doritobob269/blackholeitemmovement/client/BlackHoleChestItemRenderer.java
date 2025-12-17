package io.github.doritobob269.blackholeitemmovement.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BlackHoleChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private BlackHoleBlockEntity chestBlockEntity;

    public BlackHoleChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (chestBlockEntity == null) {
            chestBlockEntity = new BlackHoleBlockEntity(BlockPos.ZERO, ModRegistry.BLACK_HOLE_BLOCK.get().defaultBlockState());
        }

        // Get the renderer for our block entity
        BlockEntityRenderer<BlackHoleBlockEntity> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(chestBlockEntity);

        if (renderer != null) {
            renderer.render(chestBlockEntity, 0, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }
}
