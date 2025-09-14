package hu.benhorv.fasom

import hu.benhorv.fasom.block.ModBlocks
import hu.benhorv.fasom.item.ModItems
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Fasom : ModInitializer {
    val MOD_ID = "fasom"
    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
		logger.info("Hello Fabric world!")
	}
}