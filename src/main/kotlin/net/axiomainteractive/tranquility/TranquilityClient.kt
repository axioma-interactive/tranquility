package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.entity.ModEntities
import net.axiomainteractive.tranquility.entity.client.CrimsonSlimeModel
import net.axiomainteractive.tranquility.entity.client.CrimsonSlimeRenderer
import net.axiomainteractive.tranquility.entity.custom.CrimsonSlimeEntity
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRendererFactory

object TranquilityClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Register the model layer for the entity model
        EntityModelLayerRegistry.registerModelLayer(CrimsonSlimeModel.CRIMSON_SLIME, CrimsonSlimeModel::getTexturedModelData);

        // FIX: Use a lambda (or function reference) that matches the expected signature (Context -> Renderer)
        // EntityRendererRegistry.register expects a lambda like: (context: EntityRendererFactory.Context) -> CrimsonSlimeRenderer
        EntityRendererRegistry.register(ModEntities.CRIMSON_SLIME) { context: EntityRendererFactory.Context ->
            CrimsonSlimeRenderer(context)
        };

        // Alternatively, if you prefer the simpler Kotlin syntax for single-parameter lambdas:
        // EntityRendererRegistry.register(ModEntities.CRIMSON_SLIME) { context -> CrimsonSlimeRenderer(context) }

        // Note: The second argument is a lambda function, which is often placed outside the parentheses in Kotlin.
    }
}