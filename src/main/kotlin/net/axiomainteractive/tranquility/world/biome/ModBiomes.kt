package net.axiomainteractive.tranquility.world.biome

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BiomeMoodSound
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeEffects
import net.minecraft.world.biome.GenerationSettings
import net.minecraft.world.biome.SpawnSettings
import net.minecraft.world.gen.feature.DefaultBiomeFeatures

object ModBiomes {
    val CREATORS_GARDEN: RegistryKey<Biome> = RegistryKey.of(
        RegistryKeys.BIOME,
        Identifier.of(Tranquility.MOD_ID, "creators_garden")
    )

    fun bootstrap(context: Registerable<Biome>) {
        println("ModBiomes: bootstrap called")
        context.register(CREATORS_GARDEN, creatorsGarden(context))
    }

    private fun creatorsGarden(context: Registerable<Biome>): Biome {
        val spawnSettings = SpawnSettings.Builder()
        // No farm animals on surface
        // Add hostile mobs        
        DefaultBiomeFeatures.addBatsAndMonsters(spawnSettings)

        val generationSettings = GenerationSettings.LookupBackedBuilder(
            context.getRegistryLookup(RegistryKeys.PLACED_FEATURE),
            context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER)
        )
        
        // Add basic world generation features
        DefaultBiomeFeatures.addDefaultOres(generationSettings)
        // Removed DefaultDisks to reduce water/clay patches
        DefaultBiomeFeatures.addDefaultGrass(generationSettings)
        DefaultBiomeFeatures.addDefaultMushrooms(generationSettings)
        
        // NO TREES - This is intentional to match user requirements

        return Biome.Builder()
            .precipitation(false) // Desert-like: No rain
            .temperature(2.0f)    // Desert-like: Hot
            .downfall(0.0f)       // Desert-like: Dry
            .effects(
                BiomeEffects.Builder()
                    .waterColor(0x666666)
                    .waterFogColor(0x333333)
                    .fogColor(0x444444)
                    .skyColor(0x555555)
                    .grassColor(0x555555)
                    .foliageColor(0x555555)
                    .moodSound(BiomeMoodSound.CAVE)
                    .build()
            )
            .spawnSettings(spawnSettings.build())
            .generationSettings(generationSettings.build())
            .build()
    }
}
