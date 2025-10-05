package net.axiomainteractive.tranquility.entity

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.entity.custom.CrimsonSlimeEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier


object ModEntities {

    private val CRIMSON_SLIME_KEY: RegistryKey<EntityType<*>> =
        RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Tranquility.MOD_ID, "crimson_slime"))

    val CRIMSON_SLIME: EntityType<CrimsonSlimeEntity> = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(Tranquility.MOD_ID, "crimson_slime"),
        EntityType.Builder.create(::CrimsonSlimeEntity, SpawnGroup.MONSTER)
            .dimensions(1.2f, 1.2f)
            .build(CRIMSON_SLIME_KEY)
    )

    fun registerModEntities() {
        Tranquility.logger.info("Registering Mod Entities for " + Tranquility.MOD_ID)
    }
}