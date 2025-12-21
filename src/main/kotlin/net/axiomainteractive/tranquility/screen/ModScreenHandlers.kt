package net.axiomainteractive.tranquility.screen

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier

object ModScreenHandlers {
    val PHILOSOPHERS_STONE_SCREEN_HANDLER: ScreenHandlerType<PhilosophersStoneScreenHandler> = Registry.register(
        Registries.SCREEN_HANDLER,
        Identifier.of(Tranquility.MOD_ID, "philosophers_stone"),
        ScreenHandlerType(::PhilosophersStoneScreenHandler, FeatureSet.empty())
    )

    fun registerScreenHandlers() {
        Tranquility.logger.info("Registering Screen Handlers for " + Tranquility.MOD_ID)
    }
}
