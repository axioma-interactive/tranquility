package net.axiomainteractive.tranquility

import net.fabricmc.api.ClientModInitializer
import net.axiomainteractive.tranquility.screen.ModScreenHandlers
import net.axiomainteractive.tranquility.screen.PhilosophersStoneScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens

object TranquilityClient : ClientModInitializer {
    override fun onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.PHILOSOPHERS_STONE_SCREEN_HANDLER, ::PhilosophersStoneScreen)
    }
}