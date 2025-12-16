package io.github.doritobob269.blackholeitemmovement.block;

import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import javax.annotation.Nullable;

public class BlackHoleBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    private static final VoxelShape SHAPE = Block.box(0,0,0,16,2,16);

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
        Direction face = context.getClickedFace();
        // Regular placement (chest blocks) should have ATTACHED=false
        // Portable black holes set ATTACHED=true when placed
        return this.defaultBlockState().setValue(FACING, face).setValue(ATTACHED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlackHoleBlockEntity) {
                ((BlackHoleBlockEntity) be).setOwner(placer.getUUID());
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlackHoleBlockEntity) {
                BlackHoleBlockEntity bhbe = (BlackHoleBlockEntity) be;
                // Only open GUI if not attached to a container (portable black holes have ATTACHED=true and targetPos)
                boolean isPortableBlackHole = state.getValue(ATTACHED) && bhbe.getTarget() != null;
                if (!isPortableBlackHole) {
                    // This is a Black Hole Chest, open GUI
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
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == io.github.doritobob269.blackholeitemmovement.registry.ModRegistry.BLACK_HOLE_BLOCK_ENTITY.get()) {
            return (BlockEntityTicker<T>) io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity.createTicker((BlockEntityType<T>) type);
        }
        return null;
    }
}
