package hu.benhorv.tranquility

import hu.benhorv.tranquility.block.ModBlocks
import hu.benhorv.tranquility.item.ModItems
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tranquility : ModInitializer {
    val MOD_ID = "tranquility"
    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
		logger.info("Hello Fabric world!")
	}
    // test comment
}