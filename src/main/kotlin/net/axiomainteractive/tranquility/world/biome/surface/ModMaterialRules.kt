package net.axiomainteractive.tranquility.world.biome.surface

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.world.biome.ModBiomes
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.world.gen.surfacebuilder.MaterialRules
import net.minecraft.world.gen.surfacebuilder.MaterialRules.MaterialRule

object ModMaterialRules {
    private val DIRT = makeStateRule(Blocks.DIRT)
    private val GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK)

    fun makeRules(): MaterialRule {
        val isAtOrAboveWaterLevel = MaterialRules.water(-1, 0)

        val grassSurface = MaterialRules.sequence(MaterialRules.condition(isAtOrAboveWaterLevel, GRASS_BLOCK), DIRT)

        return MaterialRules.sequence(
            MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, grassSurface)
            );
    }

    private fun makeStateRule(block: Block): MaterialRule {
        return MaterialRules.block(block.getDefaultState())
    }
}