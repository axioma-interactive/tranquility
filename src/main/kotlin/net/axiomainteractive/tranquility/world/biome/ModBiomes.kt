package net.axiomainteractive.tranquility.world.biome

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.client.sound.Sound
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.BiomeMoodSound
import net.minecraft.sound.MusicType
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeEffects
import net.minecraft.world.biome.GenerationSettings.LookupBackedBuilder
import net.minecraft.world.biome.SpawnSettings
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.carver.ConfiguredCarver
import net.minecraft.world.gen.feature.DefaultBiomeFeatures
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.VegetationPlacedFeatures

object ModBiomes {
    val TEST_BIOME: RegistryKey<Biome?>? = RegistryKey.of<Biome?>(
        RegistryKeys.BIOME,
        Identifier.of(Tranquility.MOD_ID, "test_biome")
    )

    fun boostrap(context: Registerable<Biome>) {
        context.register(TEST_BIOME, testBiome(context))
    }

    fun globalOverworldGeneration(builder: LookupBackedBuilder) {
        DefaultBiomeFeatures.addLandCarvers(builder)
        DefaultBiomeFeatures.addAmethystGeodes(builder)
        DefaultBiomeFeatures.addDungeons(builder)
        DefaultBiomeFeatures.addMineables(builder)
        DefaultBiomeFeatures.addSprings(builder)
        DefaultBiomeFeatures.addFrozenTopLayer(builder)
    }

    fun testBiome(context: Registerable<Biome>): Biome {
        val spawnBuilder = SpawnSettings.Builder()

        spawnBuilder.spawn(SpawnGroup.CREATURE, 2,SpawnEntry(EntityType.WOLF, 5, 4))

        DefaultBiomeFeatures.addFarmAnimals(spawnBuilder)
        DefaultBiomeFeatures.addBatsAndMonsters(spawnBuilder)

        val biomeBuilder =
            LookupBackedBuilder(
                context.getRegistryLookup<PlacedFeature?>(RegistryKeys.PLACED_FEATURE),
                context.getRegistryLookup<ConfiguredCarver<*>?>(RegistryKeys.CONFIGURED_CARVER)
            )

        globalOverworldGeneration(biomeBuilder)
        DefaultBiomeFeatures.addMossyRocks(biomeBuilder)
        DefaultBiomeFeatures.addDefaultOres(biomeBuilder)
        DefaultBiomeFeatures.addExtraGoldOre(biomeBuilder)

        biomeBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_PLAINS)
        DefaultBiomeFeatures.addForestFlowers(biomeBuilder)
        DefaultBiomeFeatures.addLargeFerns(biomeBuilder)

        DefaultBiomeFeatures.addDefaultMushrooms(biomeBuilder)
        DefaultBiomeFeatures.addDefaultVegetation(biomeBuilder, true)

        return Biome.Builder()
            .precipitation(true)
            .downfall(0.4f)
            .temperature(0.7f)
            .generationSettings(biomeBuilder.build())
            .spawnSettings(spawnBuilder.build())
            .effects(
                (BiomeEffects.Builder())
                    .waterColor(0x4C4B50)
                    .waterFogColor(0x4C4B50)
                    .skyColor(0x4C4B50)
                    .grassColor(0x4C4B50)
                    .foliageColor(0x4C4B50)
                    .fogColor(0x4C4B50)
                    .moodSound(BiomeMoodSound.CAVE)
                    .build()
            )
            .build()
    }
}