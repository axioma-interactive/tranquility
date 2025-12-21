package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.block.entity.ModBlockEntities
import net.axiomainteractive.tranquility.item.ModItemGroups
import net.axiomainteractive.tranquility.item.ModItems
import net.axiomainteractive.tranquility.screen.ModScreenHandlers
import net.fabricmc.api.ModInitializer
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.axiomainteractive.tranquility.world.biome.ModBiomes
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

object Tranquility : ModInitializer {
    val MOD_ID = "tranquility"
    val logger = LoggerFactory.getLogger(MOD_ID)
    var creatorsGarden: RegistryEntry<Biome>? = null

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
        ModItemGroups.registerItemGroups()
        ModBlockEntities.registerBlockEntities()
        ModScreenHandlers.registerScreenHandlers()

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            val overworld = server.getWorld(World.OVERWORLD)
            if (overworld != null) {
                val border = overworld.worldBorder
                border.size = 5000.0
                border.setCenter(0.0, 0.0)
                logger.info("Server started. Set overworld border to 5000 blocks wide centered at (0,0)")
            }

            // Initialize creatorsGarden reference
            val biomeRegistry = server.registryManager.getOrThrow(RegistryKeys.BIOME)
            val optionalGarden = biomeRegistry.getOptional(ModBiomes.CREATORS_GARDEN)
            creatorsGarden = if (optionalGarden.isPresent) optionalGarden.get() else null
            
            if (creatorsGarden != null) {
                logger.info("Initialized Creator's Garden biome reference for outer world generation")
            }
        }

		logger.info("Hello Fabric world!")
	}
}