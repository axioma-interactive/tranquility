package net.axiomainteractive.tranquility.item

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.Tranquility.logger
import net.axiomainteractive.tranquility.block.ModBlocks
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ModItemGroups {
    val TRANQUILITY_ITEMS_GROUP : ItemGroup = Registry.register(Registries.ITEM_GROUP,
        Identifier.of(Tranquility.MOD_ID, "tranquility_items"),
        FabricItemGroup.builder().icon( { ItemStack(ModBlocks.CRIMSON_OBSIDIAN) } )
            .displayName(Text.translatable("itemgroup.tranquility.tranquility_items"))
            .entries {
                displayContext, entries ->
                entries.add(ModBlocks.MASONIC_STONE)
                entries.add(ModBlocks.MASONIC_COBBLESTONE)
                entries.add(ModBlocks.MOSSY_MASONIC_COBBLESTONE)
                entries.add(ModBlocks.CRIMSON_OBSIDIAN)
                entries.add(ModBlocks.ALUMINIUM_ORE)
                entries.add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE)
                entries.add(ModItems.RAW_ALUMINIUM)
                entries.add(ModBlocks.RAW_ALUMINIUM_BLOCK)
                entries.add(ModItems.ALUMINIUM_INGOT)
                entries.add(ModBlocks.ALUMINIUM_BLOCK)
                entries.add(ModBlocks.PHILOSOPHERS_STONE)
            }.build())

    fun registerItemGroups() {
        logger.info("Registering Item Groups for " + Tranquility.MOD_ID)
    }
}