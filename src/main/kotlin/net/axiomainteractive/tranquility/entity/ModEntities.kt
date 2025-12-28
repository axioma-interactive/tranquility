package net.axiomainteractive.tranquility.entity

import net.axiomainteractive.tranquility.Tranquility
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object ModEntities {
    val STALKER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Tranquility.MOD_ID, "stalker"))

    val STALKER: EntityType<StalkerEntity> = Registry.register(
        Registries.ENTITY_TYPE,
        STALKER_KEY,
        EntityType.Builder.create(::StalkerEntity, SpawnGroup.MONSTER)
            .dimensions(0.6f, 2.9f)
            .build(STALKER_KEY)
    )

    fun registerModEntities() {
        Tranquility.logger.info("Registering Entities for " + Tranquility.MOD_ID)
        FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createStalkerAttributes())
    }
}
