package net.axiomainteractive.tranquility.block

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.Tranquility.logger
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier


object ModBlocks {

    // Tranquility mod

    val MASONIC_COBBLESTONE: Block = registerBlock("masonic_cobblestone", AbstractBlock.Settings.create()
        .strength(2f, 6f)
        .requiresTool()
        .sounds(BlockSoundGroup.STONE)
    )

    val MOSSY_MASONIC_COBBLESTONE: Block = registerBlock("mossy_masonic_cobblestone", AbstractBlock.Settings.create()
        .strength(4f)
        .requiresTool()
        .sounds(BlockSoundGroup.STONE)
    )

    val CRIMSON_OBSIDIAN: Block = registerBlock("crimson_obsidian", AbstractBlock.Settings.create()
        .strength(50f)
        .resistance(1200f)
        .requiresTool()
        .sounds(BlockSoundGroup.STONE)
    )

    val ALUMINIUM_BLOCK: Block = registerBlock("aluminium_block", AbstractBlock.Settings.create()
        .strength(4.5f, 6f)
        .requiresTool()
        .sounds(BlockSoundGroup.IRON)
    )

    val RAW_ALUMINIUM_BLOCK: Block = registerBlock("raw_aluminium_block", AbstractBlock.Settings.create()
        .strength(4.5f, 6f)
        .requiresTool()
        .sounds(BlockSoundGroup.STONE)
    )

    val ALUMINIUM_ORE: Block = registerBlock("aluminium_ore", AbstractBlock.Settings.create()
        .strength(3f, 3f)
        .requiresTool()
        .sounds(BlockSoundGroup.STONE)
    )

    val DEEPSLATE_ALUMINIUM_ORE: Block = registerBlock("deepslate_aluminium_ore", AbstractBlock.Settings.create()
        .strength(4.5f, 3f)
        .requiresTool()
        .sounds(BlockSoundGroup.DEEPSLATE)
    )

    private fun registerBlock(name: String, blockSettings: AbstractBlock.Settings) : Block {
        val key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tranquility.MOD_ID, name))
        val block = Block(blockSettings.registryKey(key))
        registerBlockItem(name, block)
        return Registry.register(Registries.BLOCK, key, block)
    }

    private fun registerBlockItem(name: String, block: Block) {
        val key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tranquility.MOD_ID, name))
        val item = BlockItem(block, Item.Settings().registryKey(key))
        Registry.register(Registries.ITEM, key, item)
    }

    fun registerModBlocks() {
        logger.info("Registering Mod Blocks for " + Tranquility.MOD_ID)

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register { entries ->
            entries.add(ModBlocks.ALUMINIUM_BLOCK)
            entries.add(ModBlocks.RAW_ALUMINIUM_BLOCK)
            entries.add(ModBlocks.ALUMINIUM_ORE)
            entries.add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE)
        }
    }
}