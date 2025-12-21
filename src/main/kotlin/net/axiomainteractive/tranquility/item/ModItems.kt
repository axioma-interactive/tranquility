package net.axiomainteractive.tranquility.item

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.Tranquility.logger
import net.axiomainteractive.tranquility.block.ModBlocks
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object ModItems {
    val QUARRY_BEER : Item = registerItem("quarry_beer", Item.Settings())
    val EMPTY_QUARRY_BEER : Item = registerItem("empty_quarry_beer", Item.Settings())
    val RAW_ALUMINIUM : Item = registerItem("raw_aluminium", Item.Settings())
    val ALUMINIUM_INGOT : Item = registerItem("aluminium_ingot", Item.Settings())
    val CHARGED_REDSTONE_DUST : Item = let {
        val id = Identifier.of(Tranquility.MOD_ID, "charged_redstone_dust")
        val key = RegistryKey.of(RegistryKeys.ITEM, id)
        val settings = Item.Settings().fireproof().registryKey(key)
        val item = ChargedRedstoneDustItem(settings)
        Registry.register(Registries.ITEM, key, item)
    }



    private fun registerItem(name: String, itemSettings: Item.Settings) : Item {
        val id: Identifier = Identifier.of(Tranquility.MOD_ID, name)
        val key: RegistryKey<Item> = RegistryKey.of(RegistryKeys.ITEM, id)
        val settings: Item.Settings = itemSettings.registryKey(key)
        val item = Registry.register(Registries.ITEM, key, Item(settings))
        return item
    }

    fun registerModItems() {
        logger.info("Registering Mod Items for " + Tranquility.MOD_ID)

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { entries ->
            entries.add(QUARRY_BEER)
            entries.add(EMPTY_QUARRY_BEER)
            entries.add(RAW_ALUMINIUM)
            entries.add(ALUMINIUM_INGOT)
            entries.add(CHARGED_REDSTONE_DUST)
        }
    }
}