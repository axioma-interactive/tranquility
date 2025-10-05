package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.entity.ModEntities
import net.axiomainteractive.tranquility.entity.custom.CrimsonSlimeEntity
import net.axiomainteractive.tranquility.item.ModItemGroups
import net.axiomainteractive.tranquility.item.ModItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import org.slf4j.LoggerFactory

object Tranquility : ModInitializer {
    val MOD_ID = "tranquility"
    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
        ModItemGroups.registerItemGroups()
        ModEntities.registerModEntities()

        FabricDefaultAttributeRegistry.register(ModEntities.CRIMSON_SLIME, CrimsonSlimeEntity.createAttributes());

		logger.info("Hello Fabric world!")
	}
}