package com.doritobob.blackhole.registry;

import com.doritobob.blackhole.BlackHoleMod;
import com.doritobob.blackhole.block.BlackHoleBlock;
import com.doritobob.blackhole.blockentity.BlackHoleBlockEntity;
import com.doritobob.blackhole.item.BlackHoleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlackHoleMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlackHoleMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, BlackHoleMod.MODID);

    public static final RegistryObject<Block> BLACK_HOLE_BLOCK = BLOCKS.register("black_hole", () -> new BlackHoleBlock(Block.Properties.of(net.minecraft.world.level.material.Material.PORTAL).noCollission().strength(0.1f)));

    public static final RegistryObject<BlockEntityType<BlackHoleBlockEntity>> BLACK_HOLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("black_hole", () -> BlockEntityType.Builder.of(BlackHoleBlockEntity::new, BLACK_HOLE_BLOCK.get()).build(null));

    public static final RegistryObject<Item> BLACK_HOLE_ITEM = ITEMS.register("black_hole_item", () -> new BlackHoleItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Also register a BlockItem for the placeable block so creative builders can place it manually
    public static final RegistryObject<Item> BLACK_HOLE_BLOCK_ITEM = ITEMS.register("black_hole_block", () -> new BlockItem(BLACK_HOLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
