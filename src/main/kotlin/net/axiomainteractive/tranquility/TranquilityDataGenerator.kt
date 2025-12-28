package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.world.biome.ModBiomes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class TranquilityDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        Tranquility.logger.info("TranquilityDataGenerator: onInitializeDataGenerator called")
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::WorldGenProvider)
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        Tranquility.logger.info("TranquilityDataGenerator: buildRegistry called")
        registryBuilder.addRegistry(RegistryKeys.BIOME, ModBiomes::bootstrap)
    }

    private class WorldGenProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>) 
        : FabricDynamicRegistryProvider(output, registriesFuture) {
        override fun getName(): String = "Tranquility World Gen"
        
        override fun configure(entries: RegistryWrapper.WrapperLookup, provider: Entries) {
             Tranquility.logger.info("WorldGenProvider: configure called")
             val biomeLookup = entries.getOrThrow(RegistryKeys.BIOME)
             provider.add(biomeLookup, ModBiomes.CREATORS_GARDEN)
        }
    }
}
