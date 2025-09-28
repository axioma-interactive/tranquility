package hu.benhorv.tranquility.item

import hu.benhorv.tranquility.Tranquility
import hu.benhorv.tranquility.Tranquility.logger
import hu.benhorv.tranquility.block.ModBlocks
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
                entries.add(ModBlocks.MASONIC_COBBLESTONE)
                entries.add(ModBlocks.MOSSY_MASONIC_COBBLESTONE)
                entries.add(ModBlocks.CRIMSON_OBSIDIAN)
                entries.add(ModBlocks.ALUMINIUM_BLOCK)
                entries.add(ModBlocks.RAW_ALUMINIUM_BLOCK)
                entries.add(ModBlocks.ALUMINIUM_ORE)
            }.build())

    fun registerItemGroups() {
        logger.info("Registering Item Groups for " + Tranquility.MOD_ID)
    }
}