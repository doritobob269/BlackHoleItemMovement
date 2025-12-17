package io.github.doritobob269.blackholeitemmovement.block;

import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.RenderShape;
import javax.annotation.Nullable;

public class BlackHoleBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    // Full chest shape matching vanilla chest (for ATTACHED=false)
    private static final VoxelShape SHAPE_CHEST = Block.box(1, 0, 1, 15, 14, 15);

    // Thin shapes for each direction (for ATTACHED=true portable black holes)
    private static final VoxelShape SHAPE_DOWN = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_UP = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_WEST = Block.box(14, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 2, 16, 16);

    public BlackHoleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlackHoleBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // For chests, face the player's horizontal direction (like vanilla chests)
        // For portable black holes (placed via item), use clicked face
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing).setValue(ATTACHED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }



    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // If not attached, it's a chest block - use full chest shape
        if (!state.getValue(ATTACHED)) {
            return SHAPE_CHEST;
        }

        // Otherwise it's a portable black hole - use thin disc based on facing
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Use the same shape for collision as for visual bounds
        return getShape(state, level, pos, ctx);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Don't open GUI if player is holding the chest block item (they want to place another chest)
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() == io.github.doritobob269.blackholeitemmovement.registry.ModRegistry.BLACK_HOLE_BLOCK_ITEM.get()) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlackHoleBlockEntity) {
            BlackHoleBlockEntity bhbe = (BlackHoleBlockEntity) be;
            // Only open GUI if not attached to a container (portable black holes have ATTACHED=true)
            boolean isPortableBlackHole = state.getValue(ATTACHED);
            if (!isPortableBlackHole) {
                // This is a Black Hole Chest, open GUI
                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
                    NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Black Hole Chest");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
                            return new io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu(windowId, playerInv, be);
                        }
                    }, pos);
                }
                return InteractionResult.SUCCESS;
            } else {
                // Show toast with linked chest coordinates when clicking portal with empty hand
                if (heldItem.isEmpty()) {
                    if (!level.isClientSide) {
                        BlockPos target = bhbe.getTarget();
                        if (target != null) {
                            player.displayClientMessage(Component.literal("Linked to black hole chest at " + target.toShortString()), true);
                        } else {
                            player.displayClientMessage(Component.literal("Not linked to any chest"), true);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use MODEL for chest blocks (not attached) so they can use BlockEntityRenderer
        // Use ENTITYBLOCK_ANIMATED for chest animation
        if (!state.getValue(ATTACHED)) {
            return RenderShape.ENTITYBLOCK_ANIMATED;
        }
        // Use MODEL for portable black holes (they use static JSON models)
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Need ticker on both client and server - client for animation, server for extraction
        if (type == io.github.doritobob269.blackholeitemmovement.registry.ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get()) {
            return (BlockEntityTicker<T>) io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity.createTicker((BlockEntityType<T>) type);
        }
        return null;
    }
}
