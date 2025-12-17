package io.github.doritobob269.blackholeitemmovement.registry;

import io.github.doritobob269.blackholeitemmovement.BlackHoleMod;
import io.github.doritobob269.blackholeitemmovement.block.BlackHoleBlock;
import io.github.doritobob269.blackholeitemmovement.blockentity.BlackHoleBlockEntity;
import io.github.doritobob269.blackholeitemmovement.item.BlackHoleItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlackHoleMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlackHoleMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BlackHoleMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BlackHoleMod.MODID);
    // TODO: Fix creative tab registration for 1.21.1
    // public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(ResourceKey.createRegistryKey(new net.minecraft.resources.ResourceLocation("minecraft", "creative_mode_tab")), BlackHoleMod.MODID);

    public static final RegistryObject<Block> BLACK_HOLE_BLOCK = BLOCKS.register("black_hole", () -> new BlackHoleBlock(Block.Properties.of().noCollission().strength(0.1f)));

    public static final RegistryObject<BlockEntityType<BlackHoleBlockEntity>> BLACK_HOLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("black_hole", () -> BlockEntityType.Builder.of(BlackHoleBlockEntity::new, BLACK_HOLE_BLOCK.get()).build(null));

    public static final RegistryObject<Item> BLACK_HOLE_ITEM = ITEMS.register("black_hole_item", () -> new BlackHoleItem(new Item.Properties()));

    public static final RegistryObject<Item> BLACK_HOLE_BLOCK_ITEM = ITEMS.register("black_hole_block", () -> new BlockItem(BLACK_HOLE_BLOCK.get(), new Item.Properties()) {
        @Override
        public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
            consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
                @Override
                public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return io.github.doritobob269.blackholeitemmovement.client.ClientSetup.getChestItemRenderer();
                }
            });
        }
    });

    public static final RegistryObject<MenuType<io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu>> BLACK_HOLE_CHEST_MENU = MENUS.register("black_hole_chest", () ->
        new MenuType<>(io.github.doritobob269.blackholeitemmovement.menu.BlackHoleChestMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

    // TODO: Fix creative tab registration for 1.21.1
    /*
    public static final RegistryObject<CreativeModeTab> BLACK_HOLE_TAB = CREATIVE_TABS.register("black_hole_tab", () -> CreativeModeTab.builder()
        .title(Component.literal("Black Hole"))
        .icon(() -> new ItemStack(BLACK_HOLE_ITEM.get()))
        .displayItems((parameters, output) -> {
            output.accept(BLACK_HOLE_ITEM.get());
            output.accept(BLACK_HOLE_BLOCK_ITEM.get());
        })
        .build());
    */
}
