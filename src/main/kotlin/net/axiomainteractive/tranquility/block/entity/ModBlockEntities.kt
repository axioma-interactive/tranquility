package net.axiomainteractive.tranquility.block.entity

import net.axiomainteractive.tranquility.Tranquility
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.axiomainteractive.tranquility.block.ModBlocks

object ModBlockEntities {
    val PHILOSOPHERS_STONE_BLOCK_ENTITY: BlockEntityType<PhilosophersStoneBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(Tranquility.MOD_ID, "philosophers_stone_block_entity"),
        FabricBlockEntityTypeBuilder.create(::PhilosophersStoneBlockEntity, ModBlocks.PHILOSOPHERS_STONE).build()
    )

    fun registerBlockEntities() {
        Tranquility.logger.info("Registering Block Entities for " + Tranquility.MOD_ID)
    }
}
