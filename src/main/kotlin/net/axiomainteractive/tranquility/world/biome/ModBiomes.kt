package net.axiomainteractive.tranquility.world.biome

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.GenerationSettings.LookupBackedBuilder
import net.minecraft.world.gen.feature.DefaultBiomeFeatures

object ModBiomes {

    fun boostrap(context: Registerable<Biome>) {
        // No biomes to register currently
    }

    fun globalOverworldGeneration(builder: LookupBackedBuilder) {
        DefaultBiomeFeatures.addLandCarvers(builder)
        DefaultBiomeFeatures.addAmethystGeodes(builder)
        DefaultBiomeFeatures.addDungeons(builder)
        DefaultBiomeFeatures.addMineables(builder)
        DefaultBiomeFeatures.addSprings(builder)
        DefaultBiomeFeatures.addFrozenTopLayer(builder)
    }
}