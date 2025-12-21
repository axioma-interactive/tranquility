package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.screen.ModScreenHandlers
import net.axiomainteractive.tranquility.screen.PhilosophersStoneScreen
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.RedstoneWireBlock
import net.minecraft.client.color.block.BlockColorProvider
import net.minecraft.client.render.BlockRenderLayer

object TranquilityClient : ClientModInitializer {
    override fun onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.PHILOSOPHERS_STONE_SCREEN_HANDLER, ::PhilosophersStoneScreen)
    }
}