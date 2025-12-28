package net.axiomainteractive.tranquility.world.biome

import com.mojang.datafixers.util.Pair
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.source.util.MultiNoiseUtil.NoiseHypercube
import terrablender.api.ModifiedVanillaOverworldBuilder
import terrablender.api.Region
import terrablender.api.RegionType
import java.util.function.Consumer


class ModOverworldRegion(name: Identifier?, weight: Int) : Region(name, RegionType.OVERWORLD, weight) {
    override fun addBiomes(
        registry: Registry<Biome?>?,
        mapper: Consumer<Pair<NoiseHypercube?, RegistryKey<Biome?>?>?>?
    ) {
        this.addModifiedVanillaOverworldBiomes(
            mapper,
            Consumer { modifiedVanillaOverworldBuilder: ModifiedVanillaOverworldBuilder? ->
                // modifiedVanillaOverworldBuilder!!.replaceBiome(BiomeKeys.FOREST, ModBiomes.TEST_BIOME)
            })
    }
}