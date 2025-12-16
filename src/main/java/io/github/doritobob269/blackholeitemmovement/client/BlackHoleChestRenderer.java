package io.github.doritobob269.blackholeitemmovement.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.InventoryMenu;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BlackHoleChestRenderer implements BlockEntityRenderer<BlackHoleBlockEntity> {

    private static final Material CHEST_MATERIAL = new Material(
        InventoryMenu.BLOCK_ATLAS,
        new ResourceLocation("minecraft", "block/obsidian")
    );

    private static final Material LATCH_MATERIAL = new Material(
        InventoryMenu.BLOCK_ATLAS,
        new ResourceLocation("minecraft", "block/iron_block")
    );

    public BlackHoleChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlackHoleBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        BlockState state = blockEntity.getBlockState();

        // Only render for chest mode (not attached portable black holes)
        if (state.getValue(BlackHoleBlock.ATTACHED)) {
            return;
        }

        poseStack.pushPose();

        // Get the facing direction
        Direction facing = state.getValue(BlackHoleBlock.FACING);

        // Translate to center of block
        poseStack.translate(0.5, 0, 0.5);

        // Rotate based on facing direction
        float rotation = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            case EAST -> 270f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Translate back
        poseStack.translate(-0.5, 0, -0.5);

        // Get lid angle based on open time
        float lidAngle = blockEntity.getOpenNess(partialTicks);

        // Render chest base
        renderChestBase(poseStack, bufferSource, combinedLight, combinedOverlay);

        // Render chest lid with rotation
        renderChestLid(poseStack, bufferSource, combinedLight, combinedOverlay, lidAngle);

        poseStack.popPose();
    }

    private void renderChestBase(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int combinedLight, int combinedOverlay) {
        VertexConsumer vertexConsumer = CHEST_MATERIAL.buffer(bufferSource, RenderType::entityCutout);

        // Render the base of the chest (1/16 to 15/16 on X/Z, 0 to 10/16 on Y)
        float x1 = 1f / 16f;
        float x2 = 15f / 16f;
        float y1 = 0f;
        float y2 = 10f / 16f;
        float z1 = 1f / 16f;
        float z2 = 15f / 16f;

        // Front face (Z-)
        addVerticalQuad(vertexConsumer, poseStack, x1, y1, z1, x2, y2, z1, 0, 0, -1, combinedLight, combinedOverlay);
        // Back face (Z+)
        addVerticalQuad(vertexConsumer, poseStack, x2, y1, z2, x1, y2, z2, 0, 0, 1, combinedLight, combinedOverlay);
        // Left face (X-)
        addVerticalQuad(vertexConsumer, poseStack, x1, y1, z2, x1, y2, z1, -1, 0, 0, combinedLight, combinedOverlay);
        // Right face (X+)
        addVerticalQuad(vertexConsumer, poseStack, x2, y1, z1, x2, y2, z2, 1, 0, 0, combinedLight, combinedOverlay);
        // Top face (Y+)
        addHorizontalQuad(vertexConsumer, poseStack, x1, y2, z1, x2, z2, 0, 1, 0, combinedLight, combinedOverlay);
        // Bottom face (Y-)
        addHorizontalQuad(vertexConsumer, poseStack, x1, y1, z2, x2, z1, 0, -1, 0, combinedLight, combinedOverlay);
    }

    private void renderChestLid(PoseStack poseStack, MultiBufferSource bufferSource,
                                 int combinedLight, int combinedOverlay, float lidAngle) {
        poseStack.pushPose();

        // Translate to the back edge of the lid (the hinge)
        float x1 = 1f / 16f;
        float x2 = 15f / 16f;
        float y1 = 9f / 16f;
        float y2 = 14f / 16f;
        float z1 = 1f / 16f;
        float z2 = 15f / 16f;

        // Translate to hinge position (back of lid at z2)
        poseStack.translate(0.5, y1, z2);

        // Rotate lid around X axis (opens away from player, outward)
        poseStack.mulPose(Axis.XP.rotationDegrees(lidAngle * 90f));

        // Translate back
        poseStack.translate(-0.5, -y1, -z2);

        VertexConsumer vertexConsumer = CHEST_MATERIAL.buffer(bufferSource, RenderType::entityCutout);

        // Render the lid
        // Front face (Z-)
        addVerticalQuad(vertexConsumer, poseStack, x1, y1, z1, x2, y2, z1, 0, 0, -1, combinedLight, combinedOverlay);
        // Back face (Z+) - this is the hinge side
        addVerticalQuad(vertexConsumer, poseStack, x2, y1, z2, x1, y2, z2, 0, 0, 1, combinedLight, combinedOverlay);
        // Left face (X-)
        addVerticalQuad(vertexConsumer, poseStack, x1, y1, z2, x1, y2, z1, -1, 0, 0, combinedLight, combinedOverlay);
        // Right face (X+)
        addVerticalQuad(vertexConsumer, poseStack, x2, y1, z1, x2, y2, z2, 1, 0, 0, combinedLight, combinedOverlay);
        // Top face (Y+)
        addHorizontalQuad(vertexConsumer, poseStack, x1, y2, z1, x2, z2, 0, 1, 0, combinedLight, combinedOverlay);

        // Render latch (small centered piece on lid)
        renderLatch(poseStack, bufferSource, combinedLight, combinedOverlay);

        poseStack.popPose();
    }

    private void renderLatch(PoseStack poseStack, MultiBufferSource bufferSource,
                             int combinedLight, int combinedOverlay) {
        VertexConsumer vertexConsumer = LATCH_MATERIAL.buffer(bufferSource, RenderType::entityCutout);

        // Small latch in center of lid front
        float x1 = 7f / 16f;
        float x2 = 9f / 16f;
        float y1 = 9f / 16f;
        float y2 = 11f / 16f;
        float z1 = 1f / 16f;
        float z2 = 2f / 16f;

        // Front face (Z-)
        addVerticalQuad(vertexConsumer, poseStack, x1, y1, z1, x2, y2, z1, 0, 0, -1, combinedLight, combinedOverlay);
        // Top face (Y+)
        addHorizontalQuad(vertexConsumer, poseStack, x1, y2, z1, x2, z2, 0, 1, 0, combinedLight, combinedOverlay);
    }

    private void addVerticalQuad(VertexConsumer consumer, PoseStack poseStack,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  float nx, float ny, float nz,
                                  int combinedLight, int combinedOverlay) {
        PoseStack.Pose pose = poseStack.last();

        // For proper face culling, vertices must be in counter-clockwise order when viewed from outside
        consumer.vertex(pose.pose(), x1, y1, z1).color(255, 255, 255, 255).uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x1, y2, z1).color(255, 255, 255, 255).uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x2, y2, z2).color(255, 255, 255, 255).uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x2, y1, z2).color(255, 255, 255, 255).uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
    }

    private void addHorizontalQuad(VertexConsumer consumer, PoseStack poseStack,
                                    float x1, float y, float z1, float x2, float z2,
                                    float nx, float ny, float nz,
                                    int combinedLight, int combinedOverlay) {
        PoseStack.Pose pose = poseStack.last();

        // For proper face culling, vertices must be in counter-clockwise order when viewed from outside
        consumer.vertex(pose.pose(), x1, y, z1).color(255, 255, 255, 255).uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x1, y, z2).color(255, 255, 255, 255).uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x2, y, z2).color(255, 255, 255, 255).uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
        consumer.vertex(pose.pose(), x2, y, z1).color(255, 255, 255, 255).uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(pose.normal(), nx, ny, nz).endVertex();
    }
}
