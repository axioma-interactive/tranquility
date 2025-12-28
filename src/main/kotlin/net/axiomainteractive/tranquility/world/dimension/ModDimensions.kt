package net.axiomainteractive.tranquility.world.dimension

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.dimension.DimensionType

object ModDimensions {

    val GLASS_SPONGE_DIMENSION_KEY: RegistryKey<DimensionOptions> = RegistryKey.of(
        RegistryKeys.DIMENSION,
        Identifier.of(Tranquility.MOD_ID, "glass_sponge")
    )

    val GLASS_SPONGE_LEVEL_KEY: RegistryKey<net.minecraft.world.World> = RegistryKey.of(
        RegistryKeys.WORLD,
        Identifier.of(Tranquility.MOD_ID, "glass_sponge")
    )

    fun bootstrapType(context: Registerable<DimensionType>) {
        // We do not register the type here via code anymore as we use JSON for 1.21 data driven correctness
        // but we keep the key for reference
    }
    
    fun bootstrapDimension(context: Registerable<DimensionOptions>) {
        // Did not implement pure code bootstrap to favor JSON data pack approach for stability
    }
}
