package net.axiomainteractive.tranquility.entity.client

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.entity.client.feature.CrimsonSlimeOverlayFeatureRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.SlimeEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.SlimeEntityModel
import net.minecraft.client.render.entity.state.SlimeEntityRenderState
import net.minecraft.util.Identifier


// ⚠️ FIX: Remove the type argument <CrimsonSlimeEntity> from the superclass call
class CrimsonSlimeRenderer(context: EntityRendererFactory.Context) : SlimeEntityRenderer(context) {

    init {
        // 🚨 CRITICAL: Remove the vanilla SlimeOverlayFeatureRenderer
        this.addFeature(CrimsonSlimeOverlayFeatureRenderer(this, context.entityModels))
    }

    // ... (rest of your existing code)
    val TEXTURE: Identifier = Identifier.of(Tranquility.MOD_ID, "textures/entity/crimson_slime.png")

    override fun getTexture(entity: SlimeEntityRenderState): Identifier {
        return TEXTURE
    }
}