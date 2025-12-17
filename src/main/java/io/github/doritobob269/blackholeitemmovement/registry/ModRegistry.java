package io.github.doritobob269.blackholeitemmovement.registry;

import io.github.doritobob269.blackholeitemmovement.BlackHoleMod;
import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.item.BlackHoleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BlackHoleMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlackHoleMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BlackHoleMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, BlackHoleMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BlackHoleMod.MODID);

    public static final DeferredBlock<BlackHoleBlock> BLACK_HOLE_BLOCK = BLOCKS.register("black_hole", () -> new BlackHoleBlock(Block.Properties.of().noCollission().strength(0.1f)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlackHoleBlockEntity>> BLACK_HOLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("black_hole", () -> BlockEntityType.Builder.of(BlackHoleBlockEntity::new, BLACK_HOLE_BLOCK.get()).build(null));

    public static final DeferredItem<BlackHoleItem> BLACK_HOLE_ITEM = ITEMS.register("black_hole_item", () -> new BlackHoleItem(new Item.Properties()));

    public static final DeferredItem<BlockItem> BLACK_HOLE_BLOCK_ITEM = ITEMS.register("black_hole_block", () -> new BlockItem(BLACK_HOLE_BLOCK.get(), new Item.Properties()) {
        @Override
        public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
            consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
                @Override
                public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return io.github.doritobob269.blackholeitemmovement.client.ClientSetup.getChestItemRenderer();
                }
            });
        }
    });

    public static final DeferredHolder<MenuType<?>, MenuType<io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu>> BLACK_HOLE_CHEST_MENU = MENUS.register("black_hole_chest", () ->
        new MenuType<>((windowId, inv) -> new io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu(windowId, inv), net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLACK_HOLE_TAB = CREATIVE_TABS.register("black_hole_tab", () -> CreativeModeTab.builder()
        .title(Component.literal("Black Hole"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> BLACK_HOLE_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(BLACK_HOLE_ITEM.get());
            output.accept(BLACK_HOLE_BLOCK_ITEM.get());
        })
        .build());
}
