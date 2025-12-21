package net.axiomainteractive.tranquility.entity.client

import net.axiomainteractive.tranquility.entity.StalkerEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EndermanEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class StalkerRenderer(ctx: EntityRendererFactory.Context) : MobEntityRenderer<StalkerEntity, StalkerRenderState, EndermanEntityModel<StalkerRenderState>>(ctx, EndermanEntityModel(ctx.getPart(EntityModelLayers.ENDERMAN)), 0.5f) {
    
    companion object {
        val TEXTURE = Identifier.of("tranquility", "textures/entity/stalker.png")
    }

    override fun createRenderState(): StalkerRenderState {
        return StalkerRenderState()
    }

    override fun getTexture(state: StalkerRenderState): Identifier {
        return TEXTURE
    }

    override fun updateRenderState(entity: StalkerEntity, state: StalkerRenderState, f: Float) {
        super.updateRenderState(entity, state, f)
        // Enderman state (angry etc) - set defaults or based on behavior?
        // Stalker logic: Angry when chasing (not frozen)?
        // Default Enderman model uses state.angry for mouth opening?
        state.angry = entity.dataTracker.get(StalkerEntity.IS_AGGRESSIVE)
    }


}
