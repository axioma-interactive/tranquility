package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.block.entity.ModBlockEntities
import net.axiomainteractive.tranquility.item.ModItemGroups
import net.axiomainteractive.tranquility.item.ModItems
import net.axiomainteractive.tranquility.screen.ModScreenHandlers
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tranquility : ModInitializer {
    val MOD_ID = "tranquility"
    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
        ModItemGroups.registerItemGroups()
        ModBlockEntities.registerBlockEntities()
        ModScreenHandlers.registerScreenHandlers()
		logger.info("Hello Fabric world!")
	}
}