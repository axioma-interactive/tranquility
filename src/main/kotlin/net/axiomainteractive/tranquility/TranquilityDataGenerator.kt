package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.world.biome.ModBiomes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.biome.Biome


object TranquilityDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
	}

    public override fun buildRegistry(registryBuilder: RegistryBuilder) {
        registryBuilder.addRegistry(
            RegistryKeys.BIOME,
            ModBiomes::boostrap)
    }

}
